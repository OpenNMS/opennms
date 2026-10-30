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

import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import ForecastGraph from '@/components/Resources/ForecastGraph.vue'

// Chart.js needs a real 2d canvas context that happy-dom lacks; the forecast
// the forecast is computed server-side; stub the chart so what this test
// exercises is the component's data wiring, not the canvas.
vi.mock('chart.js', () => {
  class Chart {
    static register() {}
    destroy() {}
  }
  return { Chart, registerables: [] }
})

const getDefinitionData = vi.fn()
const getGraphMetrics = vi.fn()
vi.mock('@/services', () => ({
  default: {
    getDefinitionData: (...args: unknown[]) => getDefinitionData(...args),
    getGraphMetrics: (...args: unknown[]) => getGraphMetrics(...args)
  }
}))

// The converter turns a graph definition into a model; drive it directly so the
// component sees a controlled set of (forecastable) series.
let converterModel: unknown
vi.mock('@/components/Resources/utils/RrdGraphConverter.class', () => ({
  default: class {
    model: unknown
    constructor() {
      this.model = converterModel
    }
  }
}))

const RESOURCE_ID = 'node[1].interfaceSnmp[eth0]'

const forecastableModel = () => ({
  title: 'Bits In',
  verticalLabel: 'bits/s',
  metrics: [{ name: 'ifInOctets', attribute: 'ifInOctets', resourceId: RESOURCE_ID, aggregation: 'AVERAGE' }],
  series: [{ name: 'In', metric: 'ifInOctets', color: '#7EE600', type: 'line', title: 'In' }]
})

// A CDEF/expression-drawn series — forecastable since the full model is posted
// with the series name as the filter inputColumn (most stock graphs draw CDEFs).
const computedOnlyModel = () => ({
  title: 'Derived',
  verticalLabel: '',
  metrics: [
    { name: 'a', attribute: 'ifInOctets', resourceId: RESOURCE_ID, aggregation: 'AVERAGE', transient: true },
    { name: 'derived', expression: 'a * 8', aggregation: 'AVERAGE', attribute: '', resourceId: '' }
  ],
  series: [{ name: 'Derived', metric: 'derived', color: '#fff', type: 'line', title: 'Derived' }]
})

// no drawable series at all — the only remaining non-forecastable shape
const seriesLessModel = () => ({
  title: 'Empty',
  verticalLabel: '',
  metrics: [{ name: 'a', attribute: 'x', resourceId: RESOURCE_ID, aggregation: 'AVERAGE' }],
  series: []
})

const metricsResponse = { timestamps: [1000, 2000, 3000, 4000], columns: [{ values: [1, 2, 3, 4] }] }

const mountGraph = () =>
  mount(ForecastGraph, {
    props: { label: 'Bits', forecastDefinition: 'mib2.bits', forecastResourceId: RESOURCE_ID },
    global: { plugins: [PrimeVue] }
  })

describe('ForecastGraph.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    converterModel = forecastableModel()
    getDefinitionData.mockResolvedValue({})
    getGraphMetrics.mockResolvedValue(metricsResponse)
  })

  it('loads forecastable metrics and fetches the initial series', async () => {
    const wrapper = mountGraph()
    await flushPromises()

    expect(getDefinitionData).toHaveBeenCalledWith('mib2.bits')
    expect(wrapper.find('[data-test="forecast-load-error"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="forecast-metric"]').exists()).toBe(true)
    // onMounted selects the first metric and draws its recent data
    expect(getGraphMetrics).toHaveBeenCalledTimes(1)
    // a metric is selected, so forecasting is allowed
    expect(wrapper.find('[data-test="forecast-run"]').attributes('disabled')).toBeUndefined()
  })

  it('reports a load error only when the graph has no series at all', async () => {
    converterModel = seriesLessModel()
    const wrapper = mountGraph()
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-load-error"]').exists()).toBe(true)
    expect(getGraphMetrics).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="forecast-run"]').attributes('disabled')).toBeDefined()
  })

  it('forecasts a CDEF-drawn series by posting the whole model with inputColumn', async () => {
    // the old single-source label:'data' payload dropped every expression-backed
    // series, dead-ending most stock graph definitions
    converterModel = computedOnlyModel()
    const wrapper = mountGraph()
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-load-error"]').exists()).toBe(false)
    expect(getGraphMetrics).toHaveBeenCalledTimes(1)
    const initial = getGraphMetrics.mock.calls[0][0]
    // DEFs ride as sources, the CDEF as an expression, selected column non-transient
    expect(initial.source.map((m: { label: string }) => m.label)).toEqual(['a'])
    expect(initial.expression).toEqual([expect.objectContaining({ label: 'derived', value: 'a * 8', transient: false })])

    getGraphMetrics.mockClear()
    getGraphMetrics.mockResolvedValue({
      timestamps: [1000, 2000, 3000],
      labels: ['derived', 'HWFit', 'HWLwr', 'HWUpr', 'Trend'],
      columns: [
        { values: [1, 2, 3] },
        { values: [1, 2, 3] },
        { values: [1, 1, 2] },
        { values: [2, 3, 4] },
        { values: [1, 2, 3] }
      ]
    })
    await wrapper.find('[data-test="forecast-run"]').trigger('click')
    await flushPromises()

    const payload = getGraphMetrics.mock.calls[0][0]
    const hw = payload.filter.find((f: { name: string }) => f.name === 'HoltWinters')
    expect(hw.parameter).toContainEqual({ key: 'inputColumn', value: 'derived' })
    expect(wrapper.find('[data-test="forecast-warning"]').exists()).toBe(false)
  })

  it('surfaces a failure to load the graph definition', async () => {
    getDefinitionData.mockRejectedValue(new Error('boom'))
    const wrapper = mountGraph()
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-load-error"]').text()).toContain('Failed to load')
    expect(getGraphMetrics).not.toHaveBeenCalled()
  })

  it('runs a forecast: posts the server filter chain and renders its columns', async () => {
    const wrapper = mountGraph()
    await flushPromises()
    getGraphMetrics.mockClear()
    // the server returns the forecast columns keyed by label
    getGraphMetrics.mockResolvedValue({
      timestamps: [1000, 2000, 3000],
      labels: ['ifInOctets', 'HWFit', 'HWLwr', 'HWUpr', 'Trend'],
      columns: [
        { values: [1, 2, NaN] },
        { values: [NaN, NaN, 3] },
        { values: [NaN, NaN, 2] },
        { values: [NaN, NaN, 4] },
        { values: [1, 2, 3] }
      ]
    })

    await wrapper.find('[data-test="forecast-run"]').trigger('click')
    await flushPromises()

    expect(getGraphMetrics).toHaveBeenCalledTimes(1)
    const payload = getGraphMetrics.mock.calls[0][0]
    const filterNames = payload.filter.map((f: { name: string }) => f.name)
    expect(filterNames).toEqual(['Outlier', 'HoltWinters', 'Trend', 'Chomp'])
    // every filter parameter value is serialized as a string
    const hw = payload.filter.find((f: { name: string }) => f.name === 'HoltWinters')
    expect(hw.parameter.every((p: { value: unknown }) => typeof p.value === 'string')).toBe(true)
    expect(wrapper.find('[data-test="forecast-warning"]').exists()).toBe(false)
  })

  it('warns when the fit is all NaN because the metric reaches zero in-season', async () => {
    const wrapper = mountGraph()
    await flushPromises()
    getGraphMetrics.mockResolvedValue({
      timestamps: [1000, 2000, 3000],
      labels: ['ifInOctets', 'HWFit', 'HWLwr', 'HWUpr', 'Trend'],
      columns: [
        { values: [1, 0, 2] }, // the metric touches zero within the window
        { values: [NaN, NaN, NaN] }, // multiplicative Holt-Winters yields no fit
        { values: [NaN, NaN, NaN] },
        { values: [NaN, NaN, NaN] },
        { values: [1, 2, 3] }
      ]
    })

    await wrapper.find('[data-test="forecast-run"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-warning"]').text()).toContain('reaches zero')
  })

  it('warns when the response has no HWFit column', async () => {
    const wrapper = mountGraph()
    await flushPromises()
    getGraphMetrics.mockResolvedValue({
      timestamps: [1000, 2000],
      labels: ['ifInOctets'],
      columns: [{ values: [1, 2] }]
    })

    await wrapper.find('[data-test="forecast-run"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-warning"]').text()).toContain('could not be produced')
  })

  it('warns when the confidence bounds have zero width', async () => {
    const wrapper = mountGraph()
    await flushPromises()
    getGraphMetrics.mockResolvedValue({
      timestamps: [1000, 2000, 3000],
      labels: ['ifInOctets', 'HWFit', 'HWLwr', 'HWUpr', 'Trend'],
      columns: [
        { values: [5, 5, 5] },
        { values: [5, 5, 5] }, // valid flat fit
        { values: [5, 5, 5] }, // lower == upper -> zero width
        { values: [5, 5, 5] },
        { values: [5, 5, 5] }
      ]
    })

    await wrapper.find('[data-test="forecast-run"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-warning"]').text()).toContain('zero width')
  })
})
