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

import IpInterfacesTable from '@/components/Nodes/IpInterfacesTable.vue'
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

describe('IpInterfacesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let nodeStore: ReturnType<typeof useNodeStore>

  const mountTable = () =>
    mount(IpInterfacesTable, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue]
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountTable()
    nodeStore = useNodeStore()
    nodeStore.getNodeIpInterfaces = vi.fn().mockResolvedValue(undefined)
    nodeStore.ipInterfaces = []
    nodeStore.ipInterfacesTotalCount = 0
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Rendering', () => {
    it('renders the DataTable with data-test attribute', () => {
      expect(wrapper.find('[data-test="ip-interfaces-table"]').exists()).toBe(true)
    })

    it('renders all 4 column headers', () => {
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('IP Address')
      expect(headerTexts).toContain('IP Host Name')
      expect(headerTexts).toContain('SNMP ifIndex')
      expect(headerTexts).toContain('Managed')
    })

    it('renders rows for each IP interface', async () => {
      nodeStore.ipInterfaces = [
        {
          id: '1',
          ipAddress: '192.168.1.1',
          hostName: 'host1.example.com',
          ifIndex: '1',
          isManaged: 'M',
          isDown: false,
          lastCapsdPoll: 0,
          lastEgressFlow: null,
          lastIngressFlow: null,
          monitoredServiceCount: 0,
          nodeId: 42,
          snmpInterface: {} as any,
          snmpPrimary: 'P'
        }
      ] as any
      nodeStore.ipInterfacesTotalCount = 1
      await nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(1)
      expect(rows[0].text()).toContain('192.168.1.1')
      expect(rows[0].text()).toContain('host1.example.com')
    })

    it('shows N/A fallback for null hostName and ifIndex', async () => {
      nodeStore.ipInterfaces = [
        {
          id: '2',
          ipAddress: '10.0.0.1',
          hostName: null,
          ifIndex: null,
          isManaged: null,
          isDown: false,
          lastCapsdPoll: 0,
          lastEgressFlow: null,
          lastIngressFlow: null,
          monitoredServiceCount: 0,
          nodeId: 42,
          snmpInterface: {} as any,
          snmpPrimary: 'P'
        }
      ] as any
      nodeStore.ipInterfacesTotalCount = 1
      await nextTick()

      const rowText = wrapper.find('tbody tr').text()
      expect(rowText).toContain('N/A')
    })
  })

  describe('Empty state', () => {
    it('shows EmptyList when there are no rows', async () => {
      nodeStore.ipInterfaces = []
      nodeStore.ipInterfacesTotalCount = 0
      await nextTick()

      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('Lazy pagination — onPage', () => {
    it('calls getNodeIpInterfaces with updated offset and limit, preserving _s filter and node id', async () => {
      await wrapper.vm.onPage({ first: 5, rows: 5, page: 1, pageCount: 2 })
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledWith(
        expect.objectContaining({
          id: mockNodeId,
          queryParameters: expect.objectContaining({
            offset: 5,
            limit: 5,
            _s: 'isManaged==U,isManaged==P,isManaged==N,isManaged==M'
          })
        })
      )
    })

    it('calls getNodeIpInterfaces with the node route id', async () => {
      await wrapper.vm.onPage({ first: 0, rows: 10, page: 0, pageCount: 1 })
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledWith(
        expect.objectContaining({ id: mockNodeId })
      )
    })
  })
})
