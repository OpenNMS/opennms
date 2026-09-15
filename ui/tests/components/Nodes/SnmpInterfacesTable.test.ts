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
import { SORT } from '@/types'
import { OnmsTooltip } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { useRoute } from 'vue-router'

const mockNodeId = '42'

// A reactive route, so a test can change the node id the way navigating to another node does.
// The details page keeps one component instance across those changes.
vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '42' }})

  return { useRoute: () => route }
})

describe('SnmpInterfacesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let nodeStore: ReturnType<typeof useNodeStore>

  // The store action must be mocked BEFORE mounting — the component fetches in
  // onMounted, and an unmocked action would fire a real network request.
  const mountTable = () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    nodeStore = useNodeStore(pinia)
    nodeStore.getNodeSnmpInterfaces = vi.fn().mockResolvedValue(undefined)

    return mount(SnmpInterfacesTable, {
      global: {
        plugins: [pinia, PrimeVue],
        // The real directive, registered app-wide in theme/primevue-setup.ts. It parks the
        // resolved text on the host element as $_ptooltipValue, which is what the tooltip
        // assertions below read.
        directives: { 'onms-tooltip': OnmsTooltip }
      }
    })
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountTable()
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
      expect(headerTexts).toContain('Status')
      expect(headerTexts).toContain('SNMP ifName')
      expect(headerTexts).toContain('SNMP ifAlias')
      expect(headerTexts).toContain('SNMP ifSpeed')
    })

    // ifDescr lost its column and is surfaced in the ifName tooltip instead.
    it('has no ifDescr column', () => {
      expect(wrapper.findAll('th').map(h => h.text())).not.toContain('SNMP ifDescr')
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

    it('shows N/A fallback for null ifName and ifAlias', async () => {
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

  describe('Status column', () => {
    // snmpInterfaceStatus owns which status a row has (see utils.test.ts). What matters here is
    // the tag it renders and the severity that colors it.
    it.each([
      [1, 1, 'UP', 'success'],
      [1, 2, 'DOWN', 'danger'],
      [2, 1, 'UNKNOWN', 'info']
    ])('renders %s/%s as a %s tag with %s severity', async (ifAdminStatus, ifOperStatus, label, severity) => {
      nodeStore.snmpInterfaces = [{ id: 1, ifIndex: 1, ifAdminStatus, ifOperStatus }] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.text()).toBe(label)
      expect(wrapper.findComponent({ name: 'OnmsTag' }).props('severity')).toBe(severity)
    })

    it('tags each row on its own status', async () => {
      nodeStore.snmpInterfaces = [
        { id: 1, ifIndex: 1, ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 2, ifIndex: 2, ifAdminStatus: 1, ifOperStatus: 2 },
        { id: 3, ifIndex: 3, ifAdminStatus: 2, ifOperStatus: 2 }
      ] as any
      nodeStore.snmpInterfacesTotalCount = 3
      await nextTick()

      expect(wrapper.findAll('[data-test="status-tag"]').map(t => t.text()))
        .toEqual(['UP', 'DOWN', 'UNKNOWN'])
    })

    // The rows no longer carry a status background — the tag is the only status signal.
    it('leaves the rows unclassed', async () => {
      nodeStore.snmpInterfaces = [{ id: 1, ifIndex: 1, ifAdminStatus: 1, ifOperStatus: 2 }] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      const classes = wrapper.find('tbody tr').classes().join(' ')
      expect(classes).not.toContain('onms-interface-status')
    })
  })

  describe('Tooltips', () => {
    // Where the directive parks its resolved text; see OnmsTooltip.test.ts.
    const tooltipOf = (el: Element) => (el as never as Record<string, unknown>).$_ptooltipValue

    const mountRow = async (snmpInterface: Record<string, unknown>) => {
      nodeStore.snmpInterfaces = [{ id: 1, ifIndex: 1, ...snmpInterface }] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()
    }

    it('explains the status tag with both raw IF-MIB statuses', async () => {
      await mountRow({ ifAdminStatus: 1, ifOperStatus: 5 })

      expect(tooltipOf(wrapper.find('[data-test="status-tag"]').element))
        .toBe('Admin Status: 1 (Up)\nOperational Status: 5 (Dormant)')
    })

    // ifDescr has no column any more, so this tooltip is the only place it shows.
    it('carries the name and description on the ifName cell', async () => {
      await mountRow({ ifName: 'eth0', ifDescr: 'Uplink port' })

      expect(tooltipOf(wrapper.find('[data-test="if-name"]').element))
        .toBe('Name: eth0\nDescription: Uplink port')
    })

    // Nothing here is clickable, so the cursor is the only cue that a tooltip exists.
    it('marks both tooltip hosts as hoverable', async () => {
      await mountRow({ ifAdminStatus: 1, ifOperStatus: 1, ifName: 'eth0' })

      expect(wrapper.find('[data-test="status-tag"]').classes()).toContain('tooltip-target')
      expect(wrapper.find('[data-test="if-name"]').classes()).toContain('tooltip-target')
    })
  })

  describe('Sorting', () => {
    // The table is lazy, so ordering has to be asked of the API -- sorting client-side would
    // only order the page in hand.
    it('fetches ordered by ifIndex ascending on mount', () => {
      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledWith(
        expect.objectContaining({
          queryParameters: expect.objectContaining({
            orderBy: 'ifIndex',
            order: SORT.ASCENDING
          })
        })
      )
    })

    it('keeps the ifIndex ordering when paging', async () => {
      await wrapper.vm.onPage({ first: 5, rows: 5, page: 1, pageCount: 2 })
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenLastCalledWith(
        expect.objectContaining({
          queryParameters: expect.objectContaining({
            orderBy: 'ifIndex',
            order: SORT.ASCENDING
          })
        })
      )
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

  describe('Node changes under the same component instance', () => {
    // /node/42 -> /node/99 reuses this instance, so a table that only reads the route id at
    // mount would keep showing the interfaces of the node the user navigated away from.
    it('refetches for the new node and returns to the first page', async () => {
      // rows-per-page is the user's choice and survives the node change; only the page resets.
      await wrapper.vm.onPage({ first: 20, rows: 10, page: 2, pageCount: 3 } as any)
      await flushPromises()
      vi.clearAllMocks()

      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledWith({
        id: '99',
        queryParameters: { limit: 10, offset: 0, orderBy: 'ifIndex', order: SORT.ASCENDING }
      })
      ;(useRoute() as any).params.id = '42'
    })
  })
})
