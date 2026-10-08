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
import API from '@/services'
import { FilterDef, GraphMetricsPayload, GraphMetricsResponse, Metric } from '@/types'
import {
  ForecastColumn,
  ForecastLineDataset,
  ForecastModel,
  ForecastOptions,
  ForecastPoint
} from '@/components/Resources/types'

export const DAY_MS = 86400 * 1000

export const FORECAST_COLORS = {
  data: '#7EE600',
  bounds: '#ff0000',
  boundsFill: 'rgba(255,0,0,0.12)',
  fit: '#9d4edd',
  trend: '#00ffff'
}

export const metricFor = (model: ForecastModel | null, seriesMetricName: string): Metric | undefined => {
  return model?.metrics.find(m => m.name === seriesMetricName)
}

// Every drawn series is forecastable: the whole model is posted — DEFs as
// sources, CDEFs as expressions, the same shape Graph.vue builds — and the
// selected series' metric name is the filter chain's inputColumn.
export const buildModelPayload = (
  model: ForecastModel | null,
  selected: string,
  startMs: number,
  endMs: number,
  filter?: FilterDef[]
): GraphMetricsPayload => {
  const metrics = model?.metrics ?? []
  const source: Metric[] = metrics.filter(m => !m.expression).map(m => ({
    aggregation: m.aggregation || 'AVERAGE',
    attribute: m.attribute,
    label: m.name,
    resourceId: m.resourceId,
    // the selected column must come back in the response; everything else
    // keeps its model-declared visibility
    transient: m.name === selected ? false : !!m.transient
  }))
  const expression = metrics.filter(m => Boolean(m.expression)).map(m => ({
    value: m.expression as string,
    label: m.name ?? '',
    transient: m.name === selected ? false : !!m.transient
  }))
  const payload: GraphMetricsPayload = {
    start: startMs,
    end: endMs,
    step: Math.max(1, Math.floor((endMs - startMs) / 1000)),
    source
  }
  if (expression.length) {
    payload.expression = expression
  }
  if (filter) {
    payload.filter = filter
  }
  return payload
}

export const columnByLabel = (resp: GraphMetricsResponse, name: string): number[] => {
  const idx = (resp.labels ?? []).indexOf(name)
  return idx < 0 ? [] : (resp.columns?.[idx]?.values ?? []).map(v => (typeof v === 'number' ? v : NaN))
}

// the selected column by its label; older single-column responses (no labels
// array) fall back to the first column
export const selectedColumn = (resp: GraphMetricsResponse, name: string): number[] => {
  const values = resp.labels ? columnByLabel(resp, name) : (resp.columns?.[0]?.values ?? [])
  return values.map((v: unknown) => (typeof v === 'number' ? v : NaN))
}

export const fetchColumn = async (
  model: ForecastModel | null,
  seriesMetricName: string,
  startMs: number,
  endMs: number
): Promise<ForecastColumn | null> => {
  if (!metricFor(model, seriesMetricName)) {
    return null
  }
  const resp = await API.getGraphMetrics(buildModelPayload(model, seriesMetricName, startMs, endMs))
  if (!resp) {
    return null
  }
  const timestamps = resp.timestamps ?? []
  const values = selectedColumn(resp, seriesMetricName)
  // the API's step and returned timestamp spacing are already in milliseconds
  const stepMs = timestamps.length > 1 ? timestamps[1] - timestamps[0] : Math.max(1, Math.floor((endMs - startMs) / 1000))
  return { timestamps, values, stepMs }
}

export const toPoints = (ts: number[], vals: number[]): ForecastPoint[] => {
  return ts.map((x, i) => ({ x, y: vals[i] })).filter(p => Number.isFinite(p.y))
}

export const line = (
  label: string,
  color: string,
  points: ForecastPoint[],
  dash = false,
  fill = false
): ForecastLineDataset => {
  return {
    label,
    data: points,
    borderColor: color,
    backgroundColor: color,
    borderDash: dash ? [4, 4] : [],
    fill: fill ? '-1' : false,
    radius: 0,
    hitRadius: 4,
    borderWidth: 1.5,
    tension: 0
  }
}

// The forecast is computed server-side: the measurements query runs an Outlier
// -> HoltWinters -> Trend -> Chomp filter chain and returns the extra HWFit/
// HWLwr/HWUpr/Trend columns. Chomp runs last so training uses the full window
// while only the graph-start..horizon range is returned for display.
export const forecastFilters = (label: string, o: ForecastOptions, nowMs = Date.now()): FilterDef[] => {
  const graphStartMs = nowMs - Math.max(1, o.graphStart) * DAY_MS
  const horizonSeconds = o.forecasts * o.season * 86400
  return [
    {
      name: 'Outlier',
      parameter: [
        { key: 'inputColumn', value: label },
        { key: 'quantile', value: String(o.outlierThreshold) }
      ]
    },
    {
      name: 'HoltWinters',
      parameter: [
        { key: 'inputColumn', value: label },
        { key: 'outputPrefix', value: 'HW' },
        { key: 'periodInSeconds', value: String(Math.round(o.season * 86400)) },
        { key: 'numPeriodsToForecast', value: String(o.forecasts) },
        { key: 'confidenceLevel', value: String(o.confidenceLevel) }
      ]
    },
    {
      name: 'Trend',
      parameter: [
        { key: 'inputColumn', value: label },
        { key: 'outputColumn', value: 'Trend' },
        { key: 'polynomialOrder', value: String(o.trendOrder) },
        { key: 'secondsAhead', value: String(Math.round(horizonSeconds)) }
      ]
    },
    {
      name: 'Chomp',
      parameter: [
        { key: 'cutoffDate', value: String(graphStartMs) }
      ]
    }
  ]
}

// The data line plus the forecast overlay: confidence bounds (the high bound
// fills down to the low one), the Holt-Winters fit and the trend.
export const buildForecastDatasets = (
  resp: GraphMetricsResponse,
  seriesName: string,
  selectedMetric: string
): ForecastLineDataset[] => {
  const ts = resp.timestamps
  return [
    line(seriesName, FORECAST_COLORS.data, toPoints(ts, selectedColumn(resp, selectedMetric))),
    line('HW Bounds (low)', FORECAST_COLORS.bounds, toPoints(ts, columnByLabel(resp, 'HWLwr')), true),
    {
      ...line('HW Bounds (high)', FORECAST_COLORS.boundsFill, toPoints(ts, columnByLabel(resp, 'HWUpr')), true, true),
      borderColor: FORECAST_COLORS.bounds
    },
    line('HW Fit', FORECAST_COLORS.fit, toPoints(ts, columnByLabel(resp, 'HWFit'))),
    line('Trend', FORECAST_COLORS.trend, toPoints(ts, columnByLabel(resp, 'Trend')))
  ]
}

// The server's Holt-Winters filter can return a response whose timestamps are
// present but whose forecast columns are empty or all-NaN — the legacy forecast
// page surfaces these cases via checkForecastWarning.js, and the new page must
// too, or the user just sees a bare data line and empty legend entries. Returns
// a human-readable reason, or null when the forecast looks healthy.
export const forecastWarningFor = (resp: GraphMetricsResponse, dataLabel: string): string | null => {
  const fit = columnByLabel(resp, 'HWFit')
  if (!fit.length) {
    return 'Forecast could not be produced. The most common cause is that the selected training window does not have enough historical data.'
  }
  if (!fit.some(v => !Number.isNaN(v))) {
    // Holt-Winters here is multiplicative, so a series that touches zero inside
    // its season divides by zero and yields no fit at all
    if (selectedColumn(resp, dataLabel).some(v => v === 0)) {
      return 'Forecast produced no valid values because the metric reaches zero within its season, which the multiplicative Holt-Winters model cannot forecast. Try a metric or a training window that stays above zero.'
    }
    return 'Forecast produced no valid values. This typically means gaps or outliers in the training window left too few usable samples after filtering.'
  }
  const lwr = columnByLabel(resp, 'HWLwr')
  const upr = columnByLabel(resp, 'HWUpr')
  if (lwr.length && upr.length
      && !lwr.some((lo, i) => !Number.isNaN(lo) && !Number.isNaN(upr[i]) && Math.abs(upr[i] - lo) > 1e-12)) {
    return 'Confidence bounds have zero width (training residuals had no variance); the upper and lower bounds coincide with the fit line.'
  }
  return null
}
