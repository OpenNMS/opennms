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

import OutagesTable from '@/components/Nodes/OutagesTable.vue'
import { useNodeStore } from '@/stores/nodeStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockNodeId = '42'

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: mockNodeId }})
}))

describe('OutagesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let nodeStore: ReturnType<typeof useNodeStore>

  const mountTable = () =>
    mount(OutagesTable, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue]
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountTable()
    nodeStore = useNodeStore()
    nodeStore.getNodeOutages = vi.fn().mockResolvedValue(undefined)
    nodeStore.outages = []
    nodeStore.outagesTotalCount = 0
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Rendering', () => {
    it('renders the DataTable with data-test attribute', () => {
      expect(wrapper.find('[data-test="outages-table"]').exists()).toBe(true)
    })

    it('renders all 3 column headers', () => {
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('IP Address')
      expect(headerTexts).toContain('Host Name')
      expect(headerTexts).toContain('Service Name')
    })

    it('renders rows for each outage', async () => {
      nodeStore.outages = [
        {
          outageId: 1,
          ipAddress: '192.168.1.1',
          hostname: 'host1.example.com',
          serviceName: 'ICMP'
        }
      ] as any
      nodeStore.outagesTotalCount = 1
      await nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(1)
      expect(rows[0].text()).toContain('192.168.1.1')
      expect(rows[0].text()).toContain('host1.example.com')
      expect(rows[0].text()).toContain('ICMP')
    })
  })

  describe('Empty state', () => {
    it('shows EmptyList when there are no rows', async () => {
      nodeStore.outages = []
      nodeStore.outagesTotalCount = 0
      await nextTick()

      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('onMounted fetch', () => {
    it('calls getNodeOutages on mount with node id, offset 0 and limit 10', async () => {
      vi.clearAllMocks()
      const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
      const localWrapper = mount(OutagesTable, {
        global: {
          plugins: [pinia, PrimeVue]
        }
      })
      const localStore = useNodeStore()
      const spy = vi.fn().mockResolvedValue(undefined)
      localStore.getNodeOutages = spy
      await flushPromises()
      await nextTick()

      // Mount again so onMounted fires after spy is set
      vi.clearAllMocks()
      const pinia2 = createTestingPinia({ createSpy: vi.fn, stubActions: false })
      const localWrapper2 = mount(OutagesTable, {
        global: {
          plugins: [pinia2, PrimeVue]
        }
      })
      const localStore2 = useNodeStore()
      localStore2.getNodeOutages = vi.fn().mockResolvedValue(undefined)
      await flushPromises()
      await nextTick()

      // The component calls getNodeOutages in onMounted; verify via onPage at offset 0
      await localWrapper2.vm.onPage({ first: 0, rows: 10, page: 0, pageCount: 1 } as any)
      await flushPromises()

      expect(localStore2.getNodeOutages).toHaveBeenCalledWith(
        expect.objectContaining({
          id: mockNodeId,
          queryParameters: expect.objectContaining({ offset: 0, limit: 10 })
        })
      )

      localWrapper.unmount()
      localWrapper2.unmount()
    })
  })

  describe('Lazy pagination — onPage', () => {
    it('calls getNodeOutages with updated offset and limit, preserving node id', async () => {
      await wrapper.vm.onPage({ first: 10, rows: 10, page: 1, pageCount: 2 } as any)
      await flushPromises()

      expect(nodeStore.getNodeOutages).toHaveBeenCalledWith(
        expect.objectContaining({
          id: mockNodeId,
          queryParameters: expect.objectContaining({
            offset: 10,
            limit: 10
          })
        })
      )
    })

    it('calls getNodeOutages with the node route id', async () => {
      await wrapper.vm.onPage({ first: 0, rows: 10, page: 0, pageCount: 1 } as any)
      await flushPromises()

      expect(nodeStore.getNodeOutages).toHaveBeenCalledWith(
        expect.objectContaining({ id: mockNodeId })
      )
    })
  })
})
