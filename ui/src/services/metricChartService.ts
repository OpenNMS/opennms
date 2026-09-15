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
import type { Timeframe } from '@/types/dashboard'
import { TOPN_KPIS, listKpiSources, type MeasurementSource } from './topnService'
import { timeframeRange } from '@/components/Dashboard/timeframe'

// Metric-chart panel: one entity x one metric over the resolved timeframe.
// Metrics are the same registry the Top-N panel uses (TOPN_KPIS).
export const DEFAULT_CHART_METRIC = 'response-time'

export interface MetricSeries {
  entity: string // the label actually charted, which matters when none was configured
  timestamps: number[]
  values: (number | null)[] // null = gap (NaN from RRD); the chart breaks the line there
  unit: string
}

const byLabel = (a: MeasurementSource, b: MeasurementSource) => a.label.localeCompare(b.label)

// Entity labels that carry the given metric, for the options dropdown.
export const listMetricEntities = async (metricId: string): Promise<string[]> => {
  const sources = await listKpiSources(metricId)
  return [...sources].sort(byLabel).map(s => s.label)
}

// The configured entity, or the first one carrying the metric when nothing is
// configured yet. Null when the metric has no entity at all.
const resolveSource = (sources: MeasurementSource[], entityLabel: string): MeasurementSource | null => {
  if (!entityLabel) {
    return [...sources].sort(byLabel)[0] ?? null
  }
  return (
    sources.find(s => s.label === entityLabel) ??
    sources.find(s => s.label.toLowerCase() === entityLabel.toLowerCase()) ??
    null
  )
}

// Failures propagate so the panel can tell an error from an empty result.
export const queryMetricSeries = async (
  metricId: string,
  entityLabel: string,
  timeframe: Timeframe
): Promise<MetricSeries | null> => {
  const kpi = TOPN_KPIS.find(k => k.id === metricId) ?? TOPN_KPIS[0]
  const source = resolveSource(await listKpiSources(kpi.id), entityLabel)
  if (!source) {
    return null
  }
  const { start, end } = timeframeRange(timeframe)
  // ~200 points, never below the 5-minute collection interval
  const step = Math.max(300_000, Math.floor((end - start) / 200))
  const payload = {
    start,
    end,
    step,
    maxrows: 2000,
    relaxed: true,
    source: [
      {
        label: 'm',
        resourceId: source.resourceId,
        attribute: source.attribute,
        aggregation: 'AVERAGE',
        transient: false
      }
    ]
  }
  const resp = await rest.post('/measurements', payload, { headers: { Accept: 'application/json' }})
  const timestamps: number[] = resp.data?.timestamps ?? []
  const raw: number[] = resp.data?.columns?.[0]?.values ?? []
  const values = raw.map(v => (Number.isFinite(v) ? v * kpi.scale : null))
  if (!timestamps.length || values.every(v => v === null)) {
    return null
  }
  return { entity: source.label, timestamps, values, unit: kpi.unit }
}
