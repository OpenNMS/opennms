///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///
import { rest } from './axiosInstances'
import { type Timeframe } from '@/types/dashboard'
import { timeframeRange } from '@/components/Dashboard/timeframe'

// A KPI describes how to find the measurement source for each ranked entity and
// how to present its value. Extensible — add interface traffic, CPU, etc. here.
export interface TopnKpiDef {
  id: string
  label: string
  unit: string
  scale: number // multiply the raw RRD value (e.g. icmp is microseconds -> ms)
  // returns the attribute name to query if this child resource carries the KPI
  match: (childResourceId: string, attributeKeys: string[]) => string | null
}

// Poller response-time resources are "node[..].responseTime[<address>]" with one
// attribute per monitored service, named after the service in lower case.
const responseTimeKpi = (id: string, service: string, label: string): TopnKpiDef => ({
  id,
  label,
  unit: 'ms',
  scale: 0.001,
  match: (resourceId, attrs) => (resourceId.includes('responseTime') && attrs.includes(service) ? service : null)
})

export const TOPN_KPIS: TopnKpiDef[] = [
  responseTimeKpi('response-time', 'icmp', 'Node Response Time (ICMP)'),
  responseTimeKpi('snmp-response-time', 'snmp', 'SNMP Response Time'),
  responseTimeKpi('http-response-time', 'http', 'HTTP Response Time'),
  responseTimeKpi('https-response-time', 'https', 'HTTPS Response Time'),
  responseTimeKpi('dns-response-time', 'dns', 'DNS Response Time'),
  responseTimeKpi('ssh-response-time', 'ssh', 'SSH Response Time')
]

export const DEFAULT_TOPN_KPI = 'response-time'
export const DEFAULT_TOPN_N = 5
export const MAX_TOPN_N = 50

// a stored N may be missing, non-numeric or out of range; the same rule applies
// when it is read back as when it is saved
export const clampTopnN = (value: unknown): number => {
  const n = Math.floor(Number(value))
  return Number.isFinite(n) && n > 0 ? Math.min(MAX_TOPN_N, n) : DEFAULT_TOPN_N
}

export interface TopnRow {
  label: string
  value: number
  unit: string
}

// sources per /measurements request; larger candidate sets are queried in batches
export const SOURCES_PER_REQUEST = 250
// points per source: enough for a fair average, small enough that a batch stays cheap
const POINTS_PER_SOURCE = 24
const MIN_STEP_MS = 300_000
// the resource tree is large and changes slowly; every panel shares one copy until
// the next dashboard refresh (see invalidateKpiSources) or until this lapses
const SOURCES_TTL_MS = 5 * 60_000

interface RawResource {
  id?: string
  label?: string
  rrdGraphAttributes?: Record<string, unknown>
  children?: { resource?: RawResource[] }
}

interface RawResourceTree {
  resource?: RawResource[]
}

export interface MeasurementSource {
  // stable identity: the resource id is what a panel stores
  resourceId: string
  attribute: string
  // display only: the node label, qualified by the interface address when the
  // node carries the KPI on more than one interface; may change over time
  label: string
}

// "node[1].responseTime[10.0.0.1]" -> "10.0.0.1"
const trailingKey = (resourceId: string): string | null => {
  const m = /\[([^\]]+)\]$/.exec(resourceId)
  return m ? m[1] : null
}

export const collectSources = (root: RawResourceTree, kpi: TopnKpiDef): MeasurementSource[] => {
  const out: MeasurementSource[] = []
  for (const node of root?.resource ?? []) {
    const nodeLabel = node.label ?? node.id ?? 'node'
    const matches: { id: string; attribute: string }[] = []
    for (const child of node.children?.resource ?? []) {
      const attribute = kpi.match(child.id ?? '', Object.keys(child.rrdGraphAttributes ?? {}))
      if (attribute && child.id) {
        matches.push({ id: child.id, attribute })
      }
    }
    for (const m of matches) {
      const key = matches.length > 1 ? trailingKey(m.id) : null
      out.push({ resourceId: m.id, attribute: m.attribute, label: key ? `${nodeLabel} (${key})` : nodeLabel })
    }
  }
  return out
}

let treeCache: { at: number; tree: Promise<RawResourceTree> } | null = null

// Called on every dashboard refresh so newly provisioned nodes show up at once.
export const invalidateKpiSources = () => {
  treeCache = null
}

// Depth 1 is nodes plus their child resources, which already carry the attributes.
const loadResourceTree = async (): Promise<RawResourceTree> => {
  if (treeCache && Date.now() - treeCache.at < SOURCES_TTL_MS) {
    return treeCache.tree
  }
  const tree = rest
    .get('/resources?depth=1', { headers: { Accept: 'application/json' }})
    .then(resp => (resp.data ?? {}) as RawResourceTree)
  const entry = { at: Date.now(), tree }
  treeCache = entry
  try {
    return await tree
  } catch (err) {
    if (treeCache === entry) {
      treeCache = null
    }
    throw err
  }
}

export const findKpi = (kpiId: string): TopnKpiDef => TOPN_KPIS.find(k => k.id === kpiId) ?? TOPN_KPIS[0]

// All entities (resources) carrying the given KPI — shared by Top-N and the metric chart.
export const listKpiSources = async (kpiId: string): Promise<MeasurementSource[]> =>
  collectSources(await loadResourceTree(), findKpi(kpiId))

// The KPIs that at least one resource on this system carries, for the option
// selectors. Falls back to the full registry when nothing carries any KPI, so the
// selectors are never empty.
export const listAvailableKpis = async (): Promise<TopnKpiDef[]> => {
  const tree = await loadResourceTree()
  const available = TOPN_KPIS.filter(kpi => collectSources(tree, kpi).length > 0)
  return available.length ? available : TOPN_KPIS
}

// The measurements API returns labels/columns in hash order, not request order, so
// each column is matched to its source through the "s{index}" label it was sent with.
const averageBatch = async (
  sources: MeasurementSource[],
  offset: number,
  start: number,
  end: number,
  step: number
): Promise<Map<number, number>> => {
  const payload = {
    start,
    end,
    step,
    maxrows: POINTS_PER_SOURCE * 2,
    relaxed: true,
    source: sources.map((s, i) => ({
      label: `s${offset + i}`,
      resourceId: s.resourceId,
      attribute: s.attribute,
      aggregation: 'AVERAGE',
      transient: false
    }))
  }
  const resp = await rest.post('/measurements', payload, { headers: { Accept: 'application/json' }})
  const labels: string[] = resp.data?.labels ?? []
  const columns: { values?: number[] }[] = resp.data?.columns ?? []
  const averages = new Map<number, number>()
  labels.forEach((label, i) => {
    const m = /^s(\d+)$/.exec(label)
    if (!m) {
      return
    }
    const values = (columns[i]?.values ?? []).filter(v => Number.isFinite(v))
    if (values.length) {
      averages.set(Number(m[1]), values.reduce((a, b) => a + b, 0) / values.length)
    }
  })
  return averages
}

// Ranks every source carrying the KPI. Batches run one after another so a large
// system is not hit by every batch at once on each refresh tick. Failures
// propagate so the panel can tell an error from an empty result.
export const queryTopn = async (
  kpiId: string,
  timeframe: Timeframe,
  n: number,
  direction: 'asc' | 'desc'
): Promise<TopnRow[]> => {
  const kpi = findKpi(kpiId)
  const sources = await listKpiSources(kpi.id)
  if (!sources.length) {
    return []
  }
  const { start, end } = timeframeRange(timeframe)
  const step = Math.max(MIN_STEP_MS, Math.floor((end - start) / POINTS_PER_SOURCE))
  const rows: TopnRow[] = []
  for (let offset = 0; offset < sources.length; offset += SOURCES_PER_REQUEST) {
    const averages = await averageBatch(sources.slice(offset, offset + SOURCES_PER_REQUEST), offset, start, end, step)
    averages.forEach((avg, index) => {
      const source = sources[index]
      if (source) {
        rows.push({ label: source.label, value: avg * kpi.scale, unit: kpi.unit })
      }
    })
  }
  rows.sort((a, b) => (direction === 'asc' ? a.value - b.value : b.value - a.value))
  return rows.slice(0, clampTopnN(n))
}
