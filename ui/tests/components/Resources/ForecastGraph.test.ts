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
// math is covered by resourceForecasting.test.ts. Stub both so what this test
// exercises is the component's data wiring, not the chart or the statistics.
vi.mock('chart.js', () => {
  class Chart {
    static register() {}
    destroy() {}
  }
  return { Chart, registerables: [] }
})

const computeForecast = vi.fn()
vi.mock('@/components/Resources/utils/forecasting', () => ({
  computeForecast: (...args: unknown[]) => computeForecast(...args)
}))

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

// A CDEF/expression series with no fetchable backing metric — not forecastable.
const computedOnlyModel = () => ({
  title: 'Derived',
  verticalLabel: '',
  metrics: [{ name: 'derived', expression: 'a + b', aggregation: 'AVERAGE', attribute: '', resourceId: '' }],
  series: [{ name: 'Derived', metric: 'derived', color: '#fff', type: 'line', title: 'Derived' }]
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
    computeForecast.mockReturnValue({ timestamps: [], lower: [], upper: [], fit: [], trend: [], warning: null })
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

  it('reports a load error and blocks forecasting when nothing is forecastable', async () => {
    converterModel = computedOnlyModel()
    const wrapper = mountGraph()
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-load-error"]').exists()).toBe(true)
    // no metric could be selected, so no data was fetched and Forecast is disabled
    expect(getGraphMetrics).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="forecast-run"]').attributes('disabled')).toBeDefined()
  })

  it('surfaces a failure to load the graph definition', async () => {
    getDefinitionData.mockRejectedValue(new Error('boom'))
    const wrapper = mountGraph()
    await flushPromises()

    expect(wrapper.find('[data-test="forecast-load-error"]').text()).toContain('Failed to load')
    expect(getGraphMetrics).not.toHaveBeenCalled()
  })

  it('runs a forecast: refetches the training window and computes', async () => {
    const wrapper = mountGraph()
    await flushPromises()
    getGraphMetrics.mockClear()

    await wrapper.find('[data-test="forecast-run"]').trigger('click')
    await flushPromises()

    expect(getGraphMetrics).toHaveBeenCalledTimes(1)
    expect(computeForecast).toHaveBeenCalledTimes(1)
    expect(wrapper.find('[data-test="forecast-warning"]').exists()).toBe(false)
  })
})
