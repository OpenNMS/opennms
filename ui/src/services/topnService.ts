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
import { TimeframePreset, type Timeframe } from '@/types/dashboard'

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

export const TOPN_KPIS: TopnKpiDef[] = [
  {
    id: 'response-time',
    label: 'Node Response Time (ICMP)',
    unit: 'ms',
    scale: 0.001,
    match: (id, attrs) => (id.includes('responseTime') && attrs.includes('icmp') ? 'icmp' : null)
  }
]

export const DEFAULT_TOPN_KPI = 'response-time'
export const DEFAULT_TOPN_N = 5

export interface TopnRow {
  label: string
  value: number
  unit: string
}

const MAX_SOURCES = 250 // cap candidate resources per query

export const timeframeRange = (tf: Timeframe): { start: number; end: number } => {
  const now = Date.now()
  const hour = 3_600_000
  const day = 24 * hour
  const startOfDay = () => {
    const d = new Date()
    d.setHours(0, 0, 0, 0)
    return d.getTime()
  }
  switch (tf.preset) {
    case TimeframePreset.Today:
      return { start: startOfDay(), end: now }
    case TimeframePreset.Yesterday:
      return { start: startOfDay() - day, end: startOfDay() }
    case TimeframePreset.ThisWeek:
      return { start: now - 7 * day, end: now }
    case TimeframePreset.LastWeek:
      return { start: now - 14 * day, end: now - 7 * day }
    case TimeframePreset.Last7Days:
      return { start: now - 7 * day, end: now }
    case TimeframePreset.Last30Days:
    case TimeframePreset.ThisMonth:
      return { start: now - 30 * day, end: now }
    case TimeframePreset.LastMonth:
      return { start: now - 60 * day, end: now - 30 * day }
    case TimeframePreset.Custom:
      return { start: tf.from ? Date.parse(tf.from) : now - day, end: tf.to ? Date.parse(tf.to) : now }
    case TimeframePreset.Last24h:
    default:
      return { start: now - day, end: now }
  }
}

interface RawResource {
  id?: string
  label?: string
  rrdGraphAttributes?: Record<string, unknown>
  children?: { resource?: RawResource[] }
}

export interface MeasurementSource {
  resourceId: string
  attribute: string
  label: string
}

const collectSources = (root: { resource?: RawResource[] }, kpi: TopnKpiDef): MeasurementSource[] => {
  const out: MeasurementSource[] = []
  for (const node of root?.resource ?? []) {
    const nodeLabel = node.label ?? node.id ?? 'node'
    for (const child of node.children?.resource ?? []) {
      const attrs = Object.keys(child.rrdGraphAttributes ?? {})
      const attribute = kpi.match(child.id ?? '', attrs)
      if (attribute && child.id) {
        out.push({ resourceId: child.id, attribute, label: nodeLabel })
      }
    }
  }
  return out.slice(0, MAX_SOURCES)
}

// All entities (resources) carrying the given KPI — shared by Top-N and the metric chart.
export const listKpiSources = async (kpiId: string): Promise<MeasurementSource[]> => {
  const kpi = TOPN_KPIS.find((k) => k.id === kpiId) ?? TOPN_KPIS[0]
  const tree = await rest.get('/resources?depth=2', { headers: { Accept: 'application/json' } })
  return collectSources(tree.data ?? {}, kpi)
}

export const queryTopn = async (
  kpiId: string,
  timeframe: Timeframe,
  n: number,
  direction: 'asc' | 'desc'
): Promise<TopnRow[]> => {
  const kpi = TOPN_KPIS.find((k) => k.id === kpiId) ?? TOPN_KPIS[0]
  try {
    const sources = await listKpiSources(kpi.id)
    if (!sources.length) return []

    const { start, end } = timeframeRange(timeframe)
    // Use a normal resolution step (a single huge bucket returns NaN from RRD);
    // the series is averaged below. Cap to ~1000 points per source.
    const step = Math.max(300_000, Math.floor((end - start) / 1000))
    const payload = {
      start,
      end,
      step,
      maxrows: 2000,
      relaxed: true,
      source: sources.map((s, i) => ({
        label: `s${i}`,
        resourceId: s.resourceId,
        attribute: s.attribute,
        aggregation: 'AVERAGE',
        transient: false
      }))
    }
    const resp = await rest.post('/measurements', payload, { headers: { Accept: 'application/json' } })
    const labels: string[] = resp.data?.labels ?? []
    const columns: { values?: number[] }[] = resp.data?.columns ?? []

    const rows: TopnRow[] = []
    labels.forEach((label, i) => {
      // the backend returns labels/columns in HashMap (hash) order, NOT request
      // order, so column i does not map to sources[i]. Each label is the "s{k}"
      // we sent, which encodes the true source index — map through that.
      const sourceIndex = /^s(\d+)$/.test(label) ? Number(label.slice(1)) : i
      const source = sources[sourceIndex]
      if (!source) return
      const values = (columns[i]?.values ?? []).filter((v) => Number.isFinite(v))
      if (!values.length) return
      const avg = values.reduce((a, b) => a + b, 0) / values.length
      rows.push({ label: source.label, value: avg * kpi.scale, unit: kpi.unit })
    })

    rows.sort((a, b) => (direction === 'asc' ? a.value - b.value : b.value - a.value))
    return rows.slice(0, Math.max(1, n))
  } catch (err) {
    return []
  }
}
