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

import SnmpInterfacesTable from '@/components/Nodes/SnmpInterfacesTable.vue'
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

describe('SnmpInterfacesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let nodeStore: ReturnType<typeof useNodeStore>

  const mountTable = () =>
    mount(SnmpInterfacesTable, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue]
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountTable()
    nodeStore = useNodeStore()
    nodeStore.getNodeSnmpInterfaces = vi.fn().mockResolvedValue(undefined)
    nodeStore.snmpInterfaces = []
    nodeStore.snmpInterfacesTotalCount = 0
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Rendering', () => {
    it('renders the DataTable with data-test attribute', () => {
      expect(wrapper.find('[data-test="snmp-interfaces-table"]').exists()).toBe(true)
    })

    it('renders all 5 column headers', () => {
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('SNMP ifIndex')
      expect(headerTexts).toContain('SNMP ifDescr')
      expect(headerTexts).toContain('SNMP ifName')
      expect(headerTexts).toContain('SNMP ifAlias')
      expect(headerTexts).toContain('SNMP ifSpeed')
    })

    it('renders rows for each SNMP interface', async () => {
      nodeStore.snmpInterfaces = [
        {
          id: 1,
          ifIndex: 1,
          ifDescr: 'eth0',
          ifName: 'eth0',
          ifAlias: 'Uplink',
          ifSpeed: '1000000000'
        }
      ] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(1)
      expect(rows[0].text()).toContain('eth0')
      expect(rows[0].text()).toContain('Uplink')
    })

    it('shows N/A fallback for null ifDescr, ifName, ifAlias', async () => {
      nodeStore.snmpInterfaces = [
        {
          id: 2,
          ifIndex: 2,
          ifDescr: null,
          ifName: null,
          ifAlias: null,
          ifSpeed: '0'
        }
      ] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      const rowText = wrapper.find('tbody tr').text()
      expect(rowText).toContain('N/A')
    })

    it('renders ifSpeed with v-html (as a span)', async () => {
      nodeStore.snmpInterfaces = [
        {
          id: 3,
          ifIndex: 3,
          ifDescr: 'lo',
          ifName: 'lo',
          ifAlias: '',
          ifSpeed: '<b>100 Mbps</b>'
        }
      ] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      const speedCell = wrapper.find('tbody tr td:last-child')
      expect(speedCell.find('span').exists()).toBe(true)
    })
  })

  describe('Empty state', () => {
    it('shows EmptyList when there are no rows', async () => {
      nodeStore.snmpInterfaces = []
      nodeStore.snmpInterfacesTotalCount = 0
      await nextTick()

      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('Lazy pagination — onPage', () => {
    it('calls getNodeSnmpInterfaces with updated offset and limit, preserving node id', async () => {
      await wrapper.vm.onPage({ first: 5, rows: 5, page: 1, pageCount: 2 })
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledWith(
        expect.objectContaining({
          id: mockNodeId,
          queryParameters: expect.objectContaining({
            offset: 5,
            limit: 5
          })
        })
      )
    })

    it('calls getNodeSnmpInterfaces with the node route id', async () => {
      await wrapper.vm.onPage({ first: 0, rows: 10, page: 0, pageCount: 1 })
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledWith(
        expect.objectContaining({ id: mockNodeId })
      )
    })
  })
})
