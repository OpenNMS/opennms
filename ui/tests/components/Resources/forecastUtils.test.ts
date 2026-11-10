import { describe, expect, it, vi } from 'vitest'
import { GraphMetricsResponse } from '@/types'
import { ForecastModel, ForecastOptions } from '@/components/Resources/types'
import { forecastOptionProblems } from '@/components/Resources/utils/validation'
import {
  FORECAST_COLORS,
  buildForecastDatasets,
  buildModelPayload,
  forecastFilters,
  forecastWarningFor
} from '@/components/Resources/utils/forecastUtils'

vi.mock('@/services', () => ({ default: { getGraphMetrics: vi.fn() }}))

const options: ForecastOptions = {
  trainingStart: 14,
  graphStart: 7,
  season: 1,
  forecasts: 1,
  outlierThreshold: 0.975,
  confidenceLevel: 0.95,
  trendOrder: 3
}

const model: ForecastModel = {
  title: 'Bits',
  verticalLabel: 'bits/s',
  metrics: [
    { name: 'a', attribute: 'ifInOctets', resourceId: 'node[1].interfaceSnmp[eth0]', aggregation: 'AVERAGE', transient: true },
    { name: 'derived', expression: 'a * 8', aggregation: 'AVERAGE', attribute: '', resourceId: '' }
  ],
  series: [{ name: 'Derived', metric: 'derived', color: '#fff', type: 'line', title: 'Derived' }]
}

const response = (labels: string[], columns: number[][]): GraphMetricsResponse => ({
  labels,
  columns: columns.map(values => ({ values })),
  timestamps: [1000, 2000, 3000]
} as unknown as GraphMetricsResponse)

describe('forecastOptionProblems', () => {
  it('accepts the default template', () => {
    expect(forecastOptionProblems(options)).toEqual({})
  })

  it('flags every out-of-range option by name', () => {
    const problems = forecastOptionProblems({ ...options, trainingStart: 0, season: 10, forecasts: 1.5, outlierThreshold: 1, confidenceLevel: 0, trendOrder: 0 })
    expect(Object.keys(problems).sort()).toEqual(['confidenceLevel', 'forecasts', 'outlierThreshold', 'season', 'trainingStart', 'trendOrder'])
  })
})

describe('buildModelPayload', () => {
  it('posts DEFs as sources and CDEFs as expressions with the selected column visible', () => {
    const payload = buildModelPayload(model, 'derived', 0, 10_000, forecastFilters('derived', options, 10_000))
    expect(payload.source).toEqual([expect.objectContaining({ label: 'a', transient: true })])
    expect(payload.expression).toEqual([{ label: 'derived', value: 'a * 8', transient: false }])
    expect(payload.filter?.map(f => f.name)).toEqual(['Outlier', 'HoltWinters', 'Trend', 'Chomp'])
  })
})

describe('buildForecastDatasets', () => {
  it('draws the data line and the four overlay series with the shared colours', () => {
    const datasets = buildForecastDatasets(response(['derived', 'HWFit', 'HWLwr', 'HWUpr', 'Trend'], [[1, 2, 3], [1, 2, 3], [0, 1, 2], [2, 3, 4], [1, 2, 3]]), 'Derived', 'derived')
    expect(datasets.map(d => d.label)).toEqual(['Derived', 'HW Bounds (low)', 'HW Bounds (high)', 'HW Fit', 'Trend'])
    expect(datasets[0].borderColor).toBe(FORECAST_COLORS.data)
    expect(datasets[2].fill).toBe('-1')
    expect(datasets[2].borderColor).toBe(FORECAST_COLORS.bounds)
    expect(datasets[0].data).toEqual([{ x: 1000, y: 1 }, { x: 2000, y: 2 }, { x: 3000, y: 3 }])
  })
})

describe('forecastWarningFor', () => {
  it('is quiet for a healthy forecast', () => {
    expect(forecastWarningFor(response(['d', 'HWFit', 'HWLwr', 'HWUpr'], [[1, 2, 3], [1, 2, 3], [0, 1, 2], [2, 3, 4]]), 'd')).toBeNull()
  })

  it('explains a missing fit, an all-NaN fit on a zero-touching series, and zero-width bounds', () => {
    expect(forecastWarningFor(response(['d'], [[1, 2, 3]]), 'd')).toMatch(/could not be produced/)
    expect(forecastWarningFor(response(['d', 'HWFit'], [[0, 2, 3], [NaN, NaN, NaN]]), 'd')).toMatch(/reaches zero/)
    expect(forecastWarningFor(response(['d', 'HWFit'], [[1, 2, 3], [NaN, NaN, NaN]]), 'd')).toMatch(/no valid values/)
    expect(forecastWarningFor(response(['d', 'HWFit', 'HWLwr', 'HWUpr'], [[1, 2, 3], [1, 2, 3], [1, 2, 3], [1, 2, 3]]), 'd')).toMatch(/zero width/)
  })
})
