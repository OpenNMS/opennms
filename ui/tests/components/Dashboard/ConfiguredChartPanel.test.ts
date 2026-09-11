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
import { beforeEach, describe, expect, it, vi } from 'vitest'

const chartInstances: { config: any; destroy: () => void; resize: () => void }[] = []
vi.mock('chart.js/auto', () => ({
  default: class {
    config: any
    destroy = vi.fn()
    resize = vi.fn()
    constructor(_canvas: unknown, config: any) {
      this.config = config
      chartInstances.push(this)
    }
  }
}))
const getChart = vi.fn()
vi.mock('@/services/chartService', () => ({ getChart: (...args: unknown[]) => getChart(...args) }))

import ConfiguredChartPanel from '@/components/Dashboard/panels/ConfiguredChartPanel.vue'

const alarms = {
  name: 'sample-bar-chart', title: 'Alarms', subTitle: 'Severity Chart', domainAxisLabel: 'Severity', rangeAxisLabel: 'Count',
  categories: [{ key: '5', label: 'Minor' }, { key: '6', label: 'Major' }],
  series: [{ name: 'Events', color: '#ffff00', values: [185, 175] }, { name: 'Alarms', color: null, values: [11, null] }]
}

const mountWith = (options: Record<string, unknown>, refreshTick = 0) => mount(ConfiguredChartPanel, {
  props: {
    panelId: 'p1',
    options,
    filter: { categories: [], nodeQuery: '' } as any,
    timeframe: { key: 'last24h' } as any,
    refreshTick
  }
})

describe('ConfiguredChartPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    chartInstances.length = 0
    ;(globalThis as any).ResizeObserver = class {
      observe() {} disconnect() {}
    }
  })

  it('draws the chart named in the options as grouped bars with the configured colors', async () => {
    getChart.mockResolvedValue(alarms)
    mountWith({ chart: 'sample-bar-chart' })
    await flushPromises()

    expect(getChart).toHaveBeenCalledWith('sample-bar-chart')
    expect(chartInstances).toHaveLength(1)
    const config = chartInstances[0].config
    expect(config.type).toBe('bar')
    expect(config.data.labels).toEqual(['Minor', 'Major'])
    expect(config.data.datasets.map((d: any) => d.label)).toEqual(['Events', 'Alarms'])
    // the configured color edges the bar and fills it translucently
    expect(config.data.datasets[0].borderColor).toBe('#ffff00')
    expect(config.data.datasets[0].backgroundColor).toBe('rgba(255, 255, 0, 0.7)')
    // a series without a configured color still gets one
    expect(config.data.datasets[1].borderColor).toMatch(/^#/)
    // the legend is rendered as HTML under the plot, not by Chart.js
    expect(config.options.plugins.legend.display).toBe(false)
    expect(config.plugins.map((p: any) => p.id)).toEqual(['htmlLegend', 'configuredChartValues'])
    expect(config.data.datasets[1].data).toEqual([11, null])
    expect(config.options.scales.x.title.text).toBe('Severity')
    expect(config.options.scales.y.title.text).toBe('Count')
  })

  it('says when the chart cannot be loaded, has no data, or is not selected', async () => {
    getChart.mockResolvedValue(null)
    let w = mountWith({ chart: 'sample-bar-chart' })
    await flushPromises()
    expect(w.text()).toContain('Unable to load the chart')
    expect(chartInstances).toHaveLength(0)

    getChart.mockResolvedValue({ ...alarms, categories: [], series: [] })
    w = mountWith({ chart: 'sample-bar-chart' })
    await flushPromises()
    expect(w.text()).toContain('No data for this chart')

    w = mountWith({})
    await flushPromises()
    expect(w.text()).toContain('No chart selected')
    expect(getChart).toHaveBeenCalledTimes(2)
  })

  it('refetches on the refresh tick and when the chart option changes', async () => {
    getChart.mockResolvedValue(alarms)
    const w = mountWith({ chart: 'sample-bar-chart' })
    await flushPromises()
    await w.setProps({ refreshTick: 1 })
    await flushPromises()
    expect(getChart).toHaveBeenCalledTimes(2)
    expect(chartInstances[0].destroy).toHaveBeenCalled()

    await w.setProps({ options: { chart: 'sample-bar-chart3' }})
    await flushPromises()
    expect(getChart).toHaveBeenLastCalledWith('sample-bar-chart3')
  })
})
