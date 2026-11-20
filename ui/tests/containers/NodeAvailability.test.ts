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

import NodeAvailability from '@/containers/NodeAvailability.vue'
import API from '@/services'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { createResultWithPayload } from '@/types/validation'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const routerReplace = vi.fn()
const routerPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({ params: { id: '42' }, query: {}})),
  useRouter: vi.fn(() => ({ push: routerPush, replace: routerReplace }))
}))

vi.mock('@/services', () => ({
  default: { resolveNodeId: vi.fn() }
}))

const NodeAvailabilityPanelStub = {
  name: 'NodeAvailabilityPanel',
  template: '<div data-test="panel-stub"></div>',
  props: ['node', 'baseHref']
}

describe('NodeAvailability.vue', () => {
  const mountComponent = (id = '42', loaded = false) => {
    const pinia = createTestingPinia({ stubActions: false })
    setActivePinia(pinia)

    const nodeStore = useNodeStore()
    nodeStore.getNodeById = vi.fn().mockResolvedValue(undefined)

    if (loaded) {
      nodeStore.node = { id: '42', label: 'srv-42' } as any
      nodeStore.nodeLoaded = true
    }

    const menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/home', baseHref: '/base/' } as any

    const wrapper = mount(NodeAvailability, {
      props: { id },
      global: {
        plugins: [PrimeVue, pinia],
        stubs: {
          BreadCrumbs: true,
          NodeAvailabilityPanel: NodeAvailabilityPanelStub,
          RouterLink: RouterLinkStub
        }
      }
    })

    return { wrapper, nodeStore }
  }

  const lookupMessage = (wrapper: any) => wrapper.findComponent({ name: 'NodeLookupMessage' })

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads the node for its label', async () => {
    const { nodeStore } = mountComponent('42')
    await flushPromises()

    expect(nodeStore.getNodeById).toHaveBeenCalledWith({ id: '42' })
  })

  it('names the node by label and id in the heading once it is loaded', async () => {
    const { wrapper } = mountComponent('42', true)
    await flushPromises()

    expect(wrapper.find('h2').text()).toBe('Availability for srv-42 (id: 42)')
  })

  it('leaves the heading unnamed while the node is still being fetched', async () => {
    const { wrapper } = mountComponent('42')
    await flushPromises()

    expect(wrapper.find('h2').text()).toBe('Availability for')
  })

  it('leads from Home through Nodes and the node\'s details to Availability', async () => {
    const { wrapper } = mountComponent('42', true)
    await flushPromises()

    const items = wrapper.findComponent({ name: 'BreadCrumbs' }).props('items')
    expect(items).toEqual([
      { label: 'Home', to: '/home', isAbsoluteLink: true },
      { label: 'Nodes', to: '/nodes' },
      { label: 'Node Details', to: '/node/42' },
      { label: 'Availability', to: '#', position: 'last' }
    ])
  })

  it('hands the panel the node and the base href once the node has loaded', async () => {
    const { wrapper, nodeStore } = mountComponent('42', true)
    await flushPromises()

    const panel = wrapper.findComponent({ name: 'NodeAvailabilityPanel' })
    expect(panel.props('node')).toEqual(nodeStore.node)
    expect(panel.props('baseHref')).toBe('/base/')
  })

  it('shows no panel until the node has loaded', async () => {
    const { wrapper } = mountComponent('42')
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeAvailabilityPanel' }).exists()).toBe(false)
  })

  // To this page, not to Node Details.
  it('resolves foreignSource:foreignId and replaces the route with this page for the node id', async () => {
    vi.mocked(API.resolveNodeId).mockResolvedValue(createResultWithPayload(true, '', '153'))
    const { wrapper, nodeStore } = mountComponent('fs:fid')

    expect(wrapper.find('[data-test="node-availability-resolving"]').text()).toContain('fs:fid')
    await flushPromises()

    expect(API.resolveNodeId).toHaveBeenCalledWith('fs:fid')
    expect(routerReplace).toHaveBeenCalledWith({ name: 'Node Availability', params: { id: '153' }, query: {}})
    expect(nodeStore.getNodeById).not.toHaveBeenCalled()
  })

  it('shows Invalid Node ID, and fetches nothing, for a bad id', async () => {
    const { wrapper, nodeStore } = mountComponent('abc')
    await flushPromises()

    expect(lookupMessage(wrapper).props()).toMatchObject({ state: 'invalid', value: 'abc' })
    expect(nodeStore.getNodeById).not.toHaveBeenCalled()
    expect(wrapper.findComponent({ name: 'NodeAvailabilityPanel' }).exists()).toBe(false)
  })

  it('shows Node Not Found when the node does not exist', async () => {
    const { wrapper, nodeStore } = mountComponent('42')
    nodeStore.nodeLoadError = 'notFound'
    await flushPromises()

    expect(lookupMessage(wrapper).props()).toMatchObject({ state: 'notFound', value: '42' })
    expect(wrapper.findComponent({ name: 'NodeAvailabilityPanel' }).exists()).toBe(false)
  })

  it('shows Node Lookup Failed when the foreign id lookup errors', async () => {
    vi.mocked(API.resolveNodeId).mockResolvedValue(createResultWithPayload<string | null>(false, 'Failed'))
    const { wrapper } = mountComponent('fs:1')
    await flushPromises()

    expect(lookupMessage(wrapper).props('state')).toBe('error')
  })

  it('looks the node up again when the lookup message asks to retry', async () => {
    const { wrapper, nodeStore } = mountComponent('42')
    nodeStore.nodeLoadError = 'failed'
    await flushPromises()

    lookupMessage(wrapper).vm.$emit('retry')
    await flushPromises()

    expect(nodeStore.getNodeById).toHaveBeenCalledTimes(2)
  })

  // The look-up stays on this page rather than sending the user to the node's details.
  it('looks another node up on this page', async () => {
    const { wrapper } = mountComponent('abc')
    await flushPromises()

    await wrapper.find('[data-test="node-lookup-input"]').setValue('fs:other')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(routerPush).toHaveBeenCalledWith({ name: 'Node Availability', params: { id: 'fs:other' }})
  })
})
