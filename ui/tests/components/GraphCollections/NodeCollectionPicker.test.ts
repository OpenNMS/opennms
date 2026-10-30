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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import NodeCollectionPicker from '@/components/GraphCollections/NodeCollectionPicker.vue'
import { syntheticCollectionRoute } from '@/components/GraphCollections/syntheticCollectionRoute'

const { getResources, searchNodes, push } = vi.hoisted(() => ({ getResources: vi.fn(), searchNodes: vi.fn(), push: vi.fn() }))
vi.mock('@/services/resourceService', () => ({ getResources }))
vi.mock('@/services/graphCollectionsService', () => ({ searchNodes }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

const NODE_PAGE_SIZE = 50
const SEARCH_DEBOUNCE_MS = 175

const resource = (id: string, label: string, typeLabel: string) => ({ id, label, typeLabel })
const found = (nodes: { id: number, label: string }[], totalCount = nodes.length) => ({ nodes, totalCount })

const mountPicker = async () => {
  const wrapper = mount(NodeCollectionPicker, { global: { plugins: [PrimeVue] }})
  await flushPromises()
  return wrapper
}

const labels = (wrapper: ReturnType<typeof mount>) =>
  (wrapper.findComponent({ name: 'OnmsListbox' }).props('options') as { label: string }[]).map(o => o.label)

const typeFilter = async (wrapper: ReturnType<typeof mount>, value: string) => {
  wrapper.findComponent({ name: 'OnmsSearchInput' }).vm.$emit('update:modelValue', value)
  await vi.advanceTimersByTimeAsync(SEARCH_DEBOUNCE_MS)
  await flushPromises()
}

describe('syntheticCollectionRoute', () => {
  it('maps the top-level resource kinds to their routes and nothing else', () => {
    expect(syntheticCollectionRoute('node[12]')).toBe('/graph-collections/node/12')
    expect(syntheticCollectionRoute('nodeSource[Routers:rtr-01]')).toBe('/graph-collections/nodeSource/Routers%3Artr-01')
    expect(syntheticCollectionRoute('domain[example.com]')).toBe('/graph-collections/domain/example.com')
    // a node stored by foreign source carries fs:fid inside node[...]
    expect(syntheticCollectionRoute('node[selfmonitor:1]')).toBe('/graph-collections/nodeSource/selfmonitor%3A1')
    expect(syntheticCollectionRoute('node[1].interfaceSnmp[eth0]')).toBeNull()
    expect(syntheticCollectionRoute('nodeSnmp[]')).toBeNull()
  })
})

describe('NodeCollectionPicker.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // lodash's debounce reads the clock as well as setting timers; setImmediate stays real so
    // flushPromises keeps working
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout', 'Date'] })
    searchNodes.mockImplementation(async (term: string) =>
      term ? found([{ id: 7, label: 'rtr-01' }]) : found([{ id: 7, label: 'rtr-01' }, { id: 2, label: 'switch-02' }]))
    getResources.mockResolvedValue({
      count: 3, offset: 0, totalCount: 3,
      resource: [
        resource('node[2]', 'switch-02', 'Node'),
        resource('domain[lab]', 'lab', 'Domain'),
        resource('domain[core]', 'core', 'Domain'),
        resource('responseTime[127.0.0.1]', 'Response Time', 'Response Time')
      ]
    })
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('searches the nodes on the server one page at a time, once the filter settles', async () => {
    const wrapper = await mountPicker()
    expect(searchNodes).toHaveBeenCalledTimes(1)
    expect(searchNodes).toHaveBeenCalledWith('', NODE_PAGE_SIZE)
    expect(labels(wrapper)).toEqual(['rtr-01', 'switch-02'])
    expect(getResources).not.toHaveBeenCalled()

    // a burst of keystrokes is one search, and it waits for the pause
    wrapper.findComponent({ name: 'OnmsSearchInput' }).vm.$emit('update:modelValue', 'r')
    wrapper.findComponent({ name: 'OnmsSearchInput' }).vm.$emit('update:modelValue', 'rt')
    await vi.advanceTimersByTimeAsync(SEARCH_DEBOUNCE_MS - 50)
    expect(searchNodes).toHaveBeenCalledTimes(1)
    await typeFilter(wrapper, ' rtr ')
    expect(searchNodes).toHaveBeenCalledTimes(2)
    expect(searchNodes).toHaveBeenLastCalledWith('rtr', NODE_PAGE_SIZE)
    expect(labels(wrapper)).toEqual(['rtr-01'])

    const [picked] = wrapper.findComponent({ name: 'OnmsListbox' }).props('options') as unknown[]
    wrapper.findComponent({ name: 'OnmsListbox' }).vm.$emit('update:modelValue', picked)
    expect(push).toHaveBeenCalledWith('/graph-collections/node/7')
  })

  it('keeps the answer of the latest search when an earlier one lands after it', async () => {
    let releaseFirst = (_: unknown) => {}
    searchNodes
      .mockImplementationOnce(() => new Promise((resolve) => {
        releaseFirst = resolve
      }))
      .mockResolvedValueOnce(found([{ id: 9, label: 'latest' }]))
    const wrapper = await mountPicker()
    await typeFilter(wrapper, 'late')
    expect(labels(wrapper)).toEqual(['latest'])

    releaseFirst(found([{ id: 1, label: 'stale' }]))
    await flushPromises()
    expect(labels(wrapper)).toEqual(['latest'])
  })

  it('says when more nodes match than it shows', async () => {
    searchNodes.mockResolvedValue(found([{ id: 7, label: 'rtr-01' }], 1234))
    const wrapper = await mountPicker()
    expect(wrapper.find('[data-test="ncp-truncated"]').text()).toBe('Showing 1 of 1234 matching nodes. Narrow the filter if necessary.')

    searchNodes.mockResolvedValue(found([{ id: 7, label: 'rtr-01' }]))
    await typeFilter(wrapper, 'rtr-01')
    expect(wrapper.find('[data-test="ncp-truncated"]').exists()).toBe(false)
  })

  it('lists the domains only on request, ahead of the nodes, and filters them with the nodes', async () => {
    const wrapper = await mountPicker()
    expect(wrapper.find('[data-test="ncp-show-domains"]').exists()).toBe(true)
    await wrapper.find('[data-test="ncp-show-domains"]').trigger('click')
    await flushPromises()
    expect(getResources).toHaveBeenCalledTimes(1)
    expect(wrapper.find('[data-test="ncp-show-domains"]').exists()).toBe(false)
    expect(labels(wrapper)).toEqual(['core', 'lab', 'rtr-01', 'switch-02'])

    await typeFilter(wrapper, 'LA')
    expect(labels(wrapper)).toEqual(['lab', 'rtr-01'])
    const [picked] = wrapper.findComponent({ name: 'OnmsListbox' }).props('options') as unknown[]
    wrapper.findComponent({ name: 'OnmsListbox' }).vm.$emit('update:modelValue', picked)
    expect(push).toHaveBeenCalledWith('/graph-collections/domain/lab')
  })

  it('reports an empty inventory, no match, a failed search and a failed domain load', async () => {
    searchNodes.mockResolvedValue(found([]))
    const wrapper = await mountPicker()
    expect(wrapper.find('[data-test="ncp-empty"]').text()).toBe('No node has been provisioned yet.')

    await typeFilter(wrapper, 'zzz')
    expect(wrapper.find('[data-test="ncp-empty"]').text()).toBe('No node matches the filter.')

    getResources.mockResolvedValue({ count: 0, offset: 0, totalCount: 0, resource: [] })
    await wrapper.find('[data-test="ncp-show-domains"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="ncp-empty"]').text()).toBe('No node or domain matches the filter.')
    expect(wrapper.find('[data-test="ncp-no-domains"]').text()).toBe('No domain has performance data.')

    searchNodes.mockResolvedValue(null)
    await typeFilter(wrapper, 'zzzz')
    expect(wrapper.find('[data-test="ncp-load-error"]').text()).toBe('Failed to search the nodes.')
    expect(wrapper.findComponent({ name: 'OnmsListbox' }).exists()).toBe(false)
    // the input stays, so the next keystroke is the retry
    expect(wrapper.find('[data-test="ncp-filter"]').exists()).toBe(true)

    getResources.mockResolvedValue(null)
    const failed = await mountPicker()
    await failed.find('[data-test="ncp-show-domains"]').trigger('click')
    await flushPromises()
    expect(failed.find('[data-test="ncp-domains-error"]').text()).toBe('Failed to load the domains.')
    expect(failed.find('[data-test="ncp-show-domains"]').exists()).toBe(true)
  })
})
