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

import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { getActivePinia, setActivePinia } from 'pinia'
import { defineComponent, h } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdhocCollectionGraph from '@/components/GraphCollections/AdhocCollectionGraph.vue'

const getNodes = vi.fn()
const getNodesByFilterRule = vi.fn()
const getResourceForNode = vi.fn()
const getGraphMetrics = vi.fn()

vi.mock('@/services', () => ({
  default: {
    getNodes: (...args: unknown[]) => getNodes(...args),
    getNodesByFilterRule: (...args: unknown[]) => getNodesByFilterRule(...args),
    getResourceForNode: (...args: unknown[]) => getResourceForNode(...args),
    getGraphMetrics: (...args: unknown[]) => getGraphMetrics(...args)
  }
}))

// Chart.js needs a real 2d context; what matters here is what the chart is handed.
vi.mock('@/components/AdhocGraphs/AdhocChart.vue', () => ({
  default: defineComponent({
    name: 'AdhocChart',
    props: {
      canvasId: { type: String, default: 'adhoc-graph' },
      config: { type: Object, required: true },
      measurements: { type: Object, default: null },
      time: { type: Object, required: true },
      loading: { type: Boolean, default: false },
      error: { type: String, default: '' }
    },
    setup(props) {
      return () => h('div', {
        'data-test': 'chart-stub',
        'data-canvas': props.canvasId,
        'data-series': String(props.config.series.length),
        'data-start': String(props.time.startTime),
        'data-loading': String(props.loading),
        'data-error': props.error
      })
    }
  })
}))

const RESOURCE_ID = 'node[1].interfaceSnmp[eth0]'
const KEY_IN = `${RESOURCE_ID}|ifHCInOctets`
const STATE = `pn=1&pr=${encodeURIComponent(RESOURCE_ID)}&pd=${encodeURIComponent(KEY_IN)}&n=1&title=Uplink`
const END = Date.UTC(2026, 9, 16, 12, 0, 0)
const HOUR = 3600 * 1000

const nodeResource = {
  id: 'node[1]',
  label: 'switch-01',
  name: '1',
  children: {
    resource: [{
      id: RESOURCE_ID,
      label: 'eth0',
      name: 'eth0',
      typeLabel: 'SNMP Interface Data',
      rrdGraphAttributes: { ifHCOutOctets: {}, ifHCInOctets: {}}
    }]
  }
}

const mountGraph = async (props: Record<string, unknown> = {}) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
  setActivePinia(pinia)
  const wrapper = mount(AdhocCollectionGraph, {
    props: { adhocState: STATE, start: END - HOUR, end: END, canvasId: 'gc-3-1', ...props },
    global: { plugins: [pinia] }
  })
  await flushPromises()
  await flushPromises()
  return wrapper
}

describe('AdhocCollectionGraph.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getNodes.mockResolvedValue({ node: [], totalCount: 0, count: 0, offset: 0 })
    getNodesByFilterRule.mockResolvedValue({ nodes: [] })
    getResourceForNode.mockResolvedValue(nodeResource)
    getGraphMetrics.mockResolvedValue({ step: 60000, start: END - HOUR, end: END, timestamps: [END], labels: ['switch_01_eth0_ifHCInOctets'], columns: [{ values: [1] }] })
  })

  it('restores the stored picks against the server and queries them over the collection\'s window', async () => {
    const wrapper = await mountGraph()
    expect(getResourceForNode).toHaveBeenCalledWith('1')
    expect(getGraphMetrics).toHaveBeenCalledTimes(1)
    const payload = getGraphMetrics.mock.calls[0][0]
    expect(payload).toMatchObject({ start: END - HOUR, end: END })
    expect(payload.source.map((s: { resourceId: string, attribute: string }) => [s.resourceId, s.attribute])).toEqual([[RESOURCE_ID, 'ifHCInOctets']])
    const chart = wrapper.find('[data-test="chart-stub"]')
    expect(chart.attributes('data-canvas')).toBe('gc-3-1')
    expect(chart.attributes('data-series')).toBe('1')
    expect(chart.attributes('data-start')).toBe(String((END - HOUR) / 1000))
    expect(chart.attributes('data-loading')).toBe('false')
    expect(chart.attributes('data-error')).toBe('')
  })

  it('queries again when the collection\'s window moves', async () => {
    const wrapper = await mountGraph()
    await wrapper.setProps({ start: END - 2 * HOUR })
    await flushPromises()
    expect(getGraphMetrics).toHaveBeenCalledTimes(2)
    expect(getGraphMetrics.mock.calls[1][0]).toMatchObject({ start: END - 2 * HOUR, end: END })
  })

  it('says what became of the stored sources instead of asking for a selection', async () => {
    // a filter rule that matched something once and matches nothing now
    getNodesByFilterRule.mockResolvedValue({ nodes: [] })
    const nothing = await mountGraph({ adhocState: 'nf=catincRouters&df=ifHCInOctets&n=2' })
    expect(getNodesByFilterRule).toHaveBeenCalled()
    expect(getGraphMetrics).not.toHaveBeenCalled()
    expect(nothing.find('[data-test="chart-stub"]').attributes('data-error')).toContain('match nothing now')
    expect(nothing.find('[data-test="chart-stub"]').attributes('data-loading')).toBe('false')

    // a rule the server refuses
    getNodesByFilterRule.mockResolvedValue({ error: 'failed' })
    const broken = await mountGraph({ adhocState: 'nf=catincRouters&df=ifHCInOctets&n=2', canvasId: 'gc-3-2' })
    const error = broken.find('[data-test="chart-stub"]').attributes('data-error') ?? ''
    expect(error).toContain('The stored node filter could not be applied')
    expect(error).toMatch(/could not be evaluated/)
  })

  it('says so instead of drawing when the stored state is not a graph', async () => {
    const wrapper = await mountGraph({ adhocState: 'nonsense=1' })
    expect(wrapper.find('[data-test="gc-adhoc-error"]').text()).toContain('could not be read')
    expect(wrapper.find('[data-test="chart-stub"]').exists()).toBe(false)
    expect(getGraphMetrics).not.toHaveBeenCalled()
  })

  it('keeps a store of its own and takes it along when unmounted', async () => {
    const wrapper = await mountGraph()
    const pinia = getActivePinia()
    expect(Object.keys(pinia?.state.value ?? {})).toContain('adhocGraph:gc-3-1')
    expect(Object.keys(pinia?.state.value ?? {})).not.toContain('adhocGraphStore')
    wrapper.unmount()
    expect(Object.keys(pinia?.state.value ?? {})).not.toContain('adhocGraph:gc-3-1')
  })
})
