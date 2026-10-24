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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import NodeCollectionPicker from '@/components/GraphCollections/NodeCollectionPicker.vue'
import { syntheticCollectionRoute } from '@/components/GraphCollections/syntheticCollectionRoute'

const { getResources, push } = vi.hoisted(() => ({ getResources: vi.fn(), push: vi.fn() }))
vi.mock('@/services/resourceService', () => ({ getResources }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

const resource = (id: string, label: string, typeLabel: string) => ({ id, label, typeLabel })

const mountPicker = async () => {
  const wrapper = mount(NodeCollectionPicker, { global: { plugins: [PrimeVue] }})
  await flushPromises()
  return wrapper
}

const labels = (wrapper: ReturnType<typeof mount>) =>
  (wrapper.findComponent({ name: 'OnmsListbox' }).props('options') as { label: string }[]).map(o => o.label)

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
    getResources.mockResolvedValue({
      count: 4, offset: 0, totalCount: 4,
      resource: [
        resource('node[2]', 'switch-02', 'Node'),
        resource('domain[lab]', 'lab', 'Domain'),
        resource('nodeSource[Routers:rtr-01]', 'rtr-01', 'Node'),
        resource('responseTime[127.0.0.1]', 'Response Time', 'Response Time')
      ]
    })
  })

  it('lists domains and nodes with an on-the-fly collection, sorted by kind then label', async () => {
    const wrapper = await mountPicker()
    expect(labels(wrapper)).toEqual(['lab', 'rtr-01', 'switch-02'])
  })

  it('filters by label and opens the collection of the picked resource', async () => {
    const wrapper = await mountPicker()
    wrapper.findComponent({ name: 'OnmsSearchInput' }).vm.$emit('update:modelValue', 'RTR')
    await flushPromises()
    expect(labels(wrapper)).toEqual(['rtr-01'])
    const [picked] = wrapper.findComponent({ name: 'OnmsListbox' }).props('options') as unknown[]
    wrapper.findComponent({ name: 'OnmsListbox' }).vm.$emit('update:modelValue', picked)
    expect(push).toHaveBeenCalledWith('/graph-collections/nodeSource/Routers%3Artr-01')

    wrapper.findComponent({ name: 'OnmsSearchInput' }).vm.$emit('update:modelValue', 'zzz')
    await flushPromises()
    expect(wrapper.find('[data-test="ncp-empty"]').text()).toBe('No node or domain matches the filter.')
  })

  it('says when nothing has performance data yet, and when the list cannot be read', async () => {
    getResources.mockResolvedValue({ count: 0, offset: 0, totalCount: 0, resource: [] })
    const empty = await mountPicker()
    expect(empty.find('[data-test="ncp-empty"]').text()).toBe('No node or domain has performance data yet.')

    getResources.mockResolvedValue(null)
    const failed = await mountPicker()
    expect(failed.find('[data-test="ncp-load-error"]').text()).toBe('Failed to load the nodes and domains.')
    expect(failed.findComponent({ name: 'OnmsListbox' }).exists()).toBe(false)
  })
})
