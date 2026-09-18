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
import { LOADING_DELAY_MS } from '@/components/Nodes/hooks/useDelayedLoading'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { OnmsTooltip } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { SEARCH_DEBOUNCE_MS } from '@/components/Nodes/hooks/useDebouncedSearch'
import { nextTick } from 'vue'
import { useRoute } from 'vue-router'

const mockNodeId = '42'

// Hoisted: vi.mock is lifted above these declarations, and useSnackbar is pulled in at module
// init by services/index, so a plain const would be read before it exists.
const { showSnackBar, getFlowGraphUrl } = vi.hoisted(() => ({
  showSnackBar: vi.fn(),
  getFlowGraphUrl: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar, hideSnackbar: vi.fn() })
}))

vi.mock('@/services/flowService', () => ({
  getFlowGraphUrl: (...args: unknown[]) => getFlowGraphUrl(...args)
}))

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
    useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any

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

    // Order matters as much as membership: Index and Name lead because they are what say which
    // interface a row is.
    it('renders the seven columns in order', () => {
      expect(wrapper.findAll('th').map(h => h.text()))
        .toEqual(['Index', 'Name', 'Status', 'Alias', 'Speed', 'Descr', 'Flows'])
    })

    // Seven columns do not fit the panel, so the table scrolls horizontally instead of
    // compressing them. No column is frozen: pinning the first two left barely 250px of
    // scrolling region in a half-width panel.
    it('scrolls horizontally with no frozen column', () => {
      const table = wrapper.findComponent({ name: 'OnmsTable' })
      expect(table.props('scrollable')).toBe(true)
      expect(table.props('tableStyle')).toContain('min-width')

      const frozen = wrapper.findAllComponents({ name: 'Column' })
        .filter(c => c.props('frozen'))
      expect(frozen).toHaveLength(0)
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

    it('shows N/A fallback for a null ifName, ifAlias and ifDescr', async () => {
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

      expect(wrapper.find('[data-test="if-name"]').text()).toBe('N/A')
      expect(wrapper.find('[data-test="if-descr"]').text()).toBe('N/A')
    })

    it('renders ifDescr in its own column', async () => {
      nodeStore.snmpInterfaces = [
        { id: 1, ifIndex: 1, ifName: 'eth0', ifDescr: 'GigabitEthernet0/1' }
      ] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      expect(wrapper.find('[data-test="if-descr"]').text()).toBe('GigabitEthernet0/1')
    })


    it('renders ifSpeed formatted rather than as raw bits per second', async () => {
      nodeStore.snmpInterfaces = [{ id: 3, ifIndex: 3, ifName: 'lo', ifSpeed: 100000000 }] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      expect(wrapper.find('[data-test="if-speed"]').text()).toBe('100 Mbps')
    })

    // The cell used to render with v-html, which had nothing to render -- ifSpeed is a number
    // from the API -- and would have executed markup had one ever arrived.
    it('renders a speed containing markup as text', async () => {
      nodeStore.snmpInterfaces = [{ id: 3, ifIndex: 3, ifName: 'lo', ifSpeed: '<b>evil</b>' }] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()

      const cell = wrapper.find('[data-test="if-speed"]')
      expect(cell.find('b').exists()).toBe(false)
      expect(cell.text()).toContain('<b>evil</b>')
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

  describe('ifIndex links', () => {
    const mountRows = async (snmpInterfaces: Record<string, unknown>[]) => {
      nodeStore.snmpInterfaces = snmpInterfaces as any
      nodeStore.snmpInterfacesTotalCount = snmpInterfaces.length
      await nextTick()
    }

    it('links the ifIndex to the SNMP interface page for this node', async () => {
      await mountRows([{ id: 1, ifIndex: 14 }])

      const link = wrapper.find('[data-test="if-index-link"]')
      expect(link.text()).toBe('14')
      expect(link.attributes('href'))
        .toBe(`/opennms/element/snmpinterface.jsp?node=${mockNodeId}&ifindex=14`)
    })

    it('links each row to its own ifIndex', async () => {
      await mountRows([{ id: 1, ifIndex: 1 }, { id: 2, ifIndex: 14 }])

      expect(wrapper.findAll('[data-test="if-index-link"]').map(l => l.attributes('href')))
        .toEqual([
          `/opennms/element/snmpinterface.jsp?node=${mockNodeId}&ifindex=1`,
          `/opennms/element/snmpinterface.jsp?node=${mockNodeId}&ifindex=14`
        ])
    })

    // Follows the node the user navigates to, the same way the fetch does.
    it('points at the new node after a node change', async () => {
      ;(useRoute() as any).params.id = '99'
      await flushPromises()
      await mountRows([{ id: 1, ifIndex: 14 }])

      expect(wrapper.find('[data-test="if-index-link"]').attributes('href'))
        .toBe('/opennms/element/snmpinterface.jsp?node=99&ifindex=14')
      ;(useRoute() as any).params.id = '42'
    })
  })

  describe('Status column', () => {
    // snmpInterfaceStatus owns which status a row has (see utils.test.ts). What matters here is
    // the tag it renders and the severity that colors it.
    it.each([
      [1, 1, 'UP', 'success'],
      [1, 2, 'DOWN', 'danger'],
      [1, 3, 'TESTING', 'info'],
      [2, 1, 'DISABLED', 'warn'],
      [3, 1, 'TESTING', 'info'],
      [undefined, undefined, 'UNKNOWN', 'info']
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
        { id: 3, ifIndex: 3, ifAdminStatus: 2, ifOperStatus: 2 },
        { id: 4, ifIndex: 4, ifAdminStatus: 3, ifOperStatus: 1 },
        { id: 5, ifIndex: 5 }
      ] as any
      nodeStore.snmpInterfacesTotalCount = 5
      await nextTick()

      expect(wrapper.findAll('[data-test="status-tag"]').map(t => t.text()))
        .toEqual(['UP', 'DOWN', 'DISABLED', 'TESTING', 'UNKNOWN'])
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

    // Nothing here is clickable, so the cursor is the only cue that a tooltip exists.
    it('marks the status tag as hoverable', async () => {
      await mountRow({ ifAdminStatus: 1, ifOperStatus: 1, ifName: 'eth0' })

      expect(wrapper.find('[data-test="status-tag"]').classes()).toContain('tooltip-target')
    })

    // ifDescr has a column of its own now, so the name cell has nothing left to explain.
    it('leaves the ifName cell without a tooltip', async () => {
      await mountRow({ ifName: 'eth0', ifDescr: 'Uplink port' })

      expect(tooltipOf(wrapper.find('[data-test="if-name"]').element)).toBeUndefined()
    })
  })

  describe('Sorting', () => {
    const rows = () => wrapper.findAll('[data-test="if-index-link"]').map(l => l.text())

    const headerFor = (label: string) =>
      wrapper.findAll('th').find(th => th.text() === label.toUpperCase() || th.text() === label)!

    beforeEach(async () => {
      nodeStore.snmpInterfaces = [
        { id: 1, ifIndex: 10, ifName: 'beta', ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 2, ifIndex: 2, ifName: 'Alpha', ifAdminStatus: 1, ifOperStatus: 2 },
        { id: 3, ifIndex: 1, ifName: 'charlie', ifAdminStatus: 2, ifOperStatus: 2 }
      ] as any
      await nextTick()
    })

    // The ordering the panel opens on. It is a client-side sort now the table holds every row,
    // rather than the orderBy it used to ask the server for while it was paging server-side.
    it('defaults to ifIndex ascending', () => {
      expect(rows()).toEqual(['1', '2', '10'])
    })

    // Numeric, not lexical: a lexical sort would put 10 before 2.
    it('sorts ifIndex numerically', async () => {
      await headerFor('Index').trigger('click')
      await nextTick()

      expect(rows()).toEqual(['10', '2', '1'])
    })

    it('sorts on another column when its header is clicked', async () => {
      await headerFor('Name').trigger('click')
      await nextTick()

      expect(wrapper.findAll('[data-test="if-name"]').map(c => c.text()))
        .toEqual(['Alpha', 'beta', 'charlie'])
    })

    // 'Alpha' sorting before 'beta' is the case-insensitive part: a case-sensitive sort would
    // put every capitalised name ahead of every lowercase one.
    it('sorts case-insensitively', async () => {
      nodeStore.snmpInterfaces = [
        { id: 1, ifIndex: 1, ifName: 'beta', ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 2, ifIndex: 2, ifName: 'Alpha', ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 3, ifIndex: 3, ifName: 'Charlie', ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 4, ifIndex: 4, ifName: 'delta', ifAdminStatus: 1, ifOperStatus: 1 }
      ] as any
      await nextTick()
      await headerFor('Name').trigger('click')
      await nextTick()

      expect(wrapper.findAll('[data-test="if-name"]').map(c => c.text()))
        .toEqual(['Alpha', 'beta', 'Charlie', 'delta'])
    })

    // Sorting the formatted labels would be lexical: '1 Gbps' before '100 Mbps'. The column
    // keeps the raw number as its sort field for exactly this reason.
    it('sorts ifSpeed by the raw value, not the formatted label', async () => {
      nodeStore.snmpInterfaces = [
        { id: 1, ifIndex: 1, ifSpeed: 100000000, ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 2, ifIndex: 2, ifSpeed: 1000000000, ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 3, ifIndex: 3, ifSpeed: 10000000, ifAdminStatus: 1, ifOperStatus: 1 }
      ] as any
      await nextTick()
      await headerFor('Speed').trigger('click')
      await nextTick()

      expect(wrapper.findAll('[data-test="if-speed"]').map(c => c.text()))
        .toEqual(['10 Mbps', '100 Mbps', '1 Gbps'])
    })

    // status is derived, not a stored field, so it is decorated onto the rows to be sortable.
    it('sorts on the derived status column', async () => {
      await headerFor('Status').trigger('click')
      await nextTick()

      expect(wrapper.findAll('[data-test="status-tag"]').map(t => t.text()))
        .toEqual(['DISABLED', 'DOWN', 'UP'])
    })
  })

  describe('Filtering', () => {
    const search = () => wrapper.find('[data-test="snmp-interfaces-search"]')
    // By the link rather than by <tr>: PrimeVue renders the #empty slot as a row of its own,
    // so a 'tbody tr' count is never zero.
    const ifIndexes = () => wrapper.findAll('[data-test="if-index-link"]').map(l => l.text())

    const type = async (value: string) => {
      await search().setValue(value)
      vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS)
      await nextTick()
    }

    beforeEach(async () => {
      vi.useFakeTimers()
      nodeStore.snmpInterfaces = [
        { id: 1, ifIndex: 1, ifName: 'eth0', ifAlias: 'Uplink', ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 2, ifIndex: 7, ifName: 'eth1', ifAlias: 'Spare', ifAdminStatus: 1, ifOperStatus: 2 },
        { id: 3, ifIndex: 17, ifName: 'lo0', ifAlias: 'Loopback', ifAdminStatus: 2, ifOperStatus: 2 }
      ] as any
      await nextTick()
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('renders a search input', () => {
      expect(search().exists()).toBe(true)
    })

    // Deliberately unlike the legacy page, which ignored a one-character term: on this table a
    // single digit is the only way to reach a single-digit ifIndex.
    it('filters on a single character', async () => {
      await type('7')

      expect(ifIndexes()).toEqual(['7', '17'])
    })

    it('matches case-insensitively', async () => {
      await type('UPLINK')

      expect(ifIndexes()).toEqual(['1'])
    })

    it('matches on ifName', async () => {
      await type('lo0')

      expect(ifIndexes()).toEqual(['17'])
    })

    // The filter matches what the column shows, which is the formatted label -- so 'Mbps'
    // finds rows whose raw ifSpeed contains no such text.
    it('matches on the formatted speed', async () => {
      nodeStore.snmpInterfaces = [
        { id: 1, ifIndex: 1, ifSpeed: 100000000, ifAdminStatus: 1, ifOperStatus: 1 },
        { id: 2, ifIndex: 2, ifSpeed: 1000000000, ifAdminStatus: 1, ifOperStatus: 1 }
      ] as any
      await nextTick()
      await type('mbps')

      expect(ifIndexes()).toEqual(['1'])
    })

    // The Status column is displayed, so it filters like any other column.
    it('matches on the derived status', async () => {
      await type('disabled')

      expect(ifIndexes()).toEqual(['17'])
    })

    // A useful side effect of labelling it DISABLED rather than ADMIN-DOWN: 'down' now finds only
    // the interfaces that are actually down, not the ones somebody turned off.
    it('does not match a disabled interface on down', async () => {
      await type('down')

      expect(ifIndexes()).toEqual(['7'])
    })

    it('shows the empty state when nothing matches', async () => {
      await type('nothing-matches-this')

      expect(ifIndexes()).toEqual([])
      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })

    // Until the debounce elapses the table still shows the unfiltered set.
    it('does not filter until the debounce elapses', async () => {
      await search().setValue('7')
      vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS - 50)
      await nextTick()

      expect(ifIndexes()).toEqual(['1', '7', '17'])
    })

    it('restores every row when the term is cleared', async () => {
      await type('7')
      await type('')

      expect(ifIndexes()).toEqual(['1', '7', '17'])
    })

    // Page 2 of the unfiltered set is past the end of a one-row result.
    it('returns to the first page when the filter narrows the set', async () => {
      await wrapper.vm.onPage({ first: 5, rows: 5, page: 1, pageCount: 2 })
      await type('uplink')

      expect(ifIndexes()).toEqual(['1'])
    })

    // Filtering does not go back to the server -- every row is already in hand.
    it('does not refetch', async () => {
      vi.mocked(nodeStore.getNodeSnmpInterfaces).mockClear()
      await type('eth')

      expect(nodeStore.getNodeSnmpInterfaces).not.toHaveBeenCalled()
    })
  })

  describe('Pagination', () => {
    // Client-side now: the table holds every row, so turning a page is not a request.
    it('does not refetch when the page changes', async () => {
      vi.mocked(nodeStore.getNodeSnmpInterfaces).mockClear()
      await wrapper.vm.onPage({ first: 5, rows: 5, page: 1, pageCount: 2 })
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).not.toHaveBeenCalled()
    })
  })

  describe('Node changes under the same component instance', () => {
    // /node/42 -> /node/99 reuses this instance, so a table that only reads the route id at
    // mount would keep showing the interfaces of the node the user navigated away from.
    it('refetches every interface for the new node', async () => {
      await wrapper.vm.onPage({ first: 20, rows: 10, page: 2, pageCount: 3 } as any)
      await flushPromises()
      vi.clearAllMocks()

      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledWith({
        id: '99',
        queryParameters: { limit: 0 }
      })
      ;(useRoute() as any).params.id = '42'
    })

    // The old node's search term says nothing about the new node's interfaces.
    it('clears the filter', async () => {
      nodeStore.snmpInterfaces = [{ id: 1, ifIndex: 1, ifName: 'eth0' }] as any
      await nextTick()
      await wrapper.find('[data-test="snmp-interfaces-search"]').setValue('nothing-matches')
      await new Promise(r => setTimeout(r, SEARCH_DEBOUNCE_MS + 25))
      await nextTick()
      expect(wrapper.findAll('[data-test="if-index-link"]').length).toBe(0)

      ;(useRoute() as any).params.id = '99'
      await flushPromises()
      nodeStore.snmpInterfaces = [{ id: 1, ifIndex: 1, ifName: 'eth0' }] as any
      await nextTick()

      expect(wrapper.findAll('[data-test="if-index-link"]').length).toBe(1)
      ;(useRoute() as any).params.id = '42'
    })
  })

  // The details page mounts both tabs' tables at once, so fetching on mount meant every node
  // details load paid for every SNMP interface on the node -- the expensive one on a switch
  // with thousands of them -- even for a user who never opened this tab.
  describe('Deferred fetch', () => {
    const mountInactive = () => {
      const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
      nodeStore = useNodeStore(pinia)
      nodeStore.getNodeSnmpInterfaces = vi.fn().mockResolvedValue(undefined)
      useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any

      return mount(SnmpInterfacesTable, {
        props: { active: false },
        global: { plugins: [pinia, PrimeVue], directives: { 'onms-tooltip': OnmsTooltip }}
      })
    }

    it('does not fetch while its tab is hidden', async () => {
      mountInactive()
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).not.toHaveBeenCalled()
    })

    it('fetches when its tab is first shown', async () => {
      const hidden = mountInactive()
      await flushPromises()

      await hidden.setProps({ active: true })
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledTimes(1)
    })

    // Switching away and back must not pay for the rows a second time.
    it('does not refetch when the tab is hidden and shown again', async () => {
      const hidden = mountInactive()
      await hidden.setProps({ active: true })
      await flushPromises()
      await hidden.setProps({ active: false })
      await hidden.setProps({ active: true })
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledTimes(1)
    })

    // A node changed behind a hidden tab is picked up when the user returns to it, so they never
    // see the previous node's rows.
    it('fetches the new node when shown again after a node change', async () => {
      const hidden = mountInactive()
      await hidden.setProps({ active: true })
      await flushPromises()
      await hidden.setProps({ active: false })

      ;(useRoute() as any).params.id = '99'
      await flushPromises()
      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledTimes(1)

      await hidden.setProps({ active: true })
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledTimes(2)
      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenLastCalledWith(expect.objectContaining({ id: '99' }))
      ;(useRoute() as any).params.id = '42'
    })

    it('refetches for a new node while the tab is showing', async () => {
      const shown = mountInactive()
      await shown.setProps({ active: true })
      await flushPromises()

      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(nodeStore.getNodeSnmpInterfaces).toHaveBeenCalledTimes(2)
      ;(useRoute() as any).params.id = '42'
    })
  })

  // The spinner is scoped to the table, not the app-wide useSpinner overlay, and it waits out
  // LOADING_DELAY_MS so a fast fetch never flashes one.
  describe('Loading state', () => {
    const mountPending = () => {
      let release: () => void = () => undefined
      const pending = new Promise<void>((r) => {
        release = r
      })
      const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
      nodeStore = useNodeStore(pinia)
      nodeStore.getNodeSnmpInterfaces = vi.fn().mockReturnValue(pending)
      useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any

      const w = mount(SnmpInterfacesTable, {
        global: { plugins: [pinia, PrimeVue], directives: { 'onms-tooltip': OnmsTooltip }}
      })

      return { wrapper: w, release }
    }

    const tableLoading = (w: any) => w.findComponent({ name: 'OnmsTable' }).props('loading')

    it('shows no spinner before the delay elapses', async () => {
      vi.useFakeTimers()
      const { wrapper: w } = mountPending()
      await nextTick()
      vi.advanceTimersByTime(LOADING_DELAY_MS - 25)
      await nextTick()

      expect(tableLoading(w)).toBe(false)
      vi.useRealTimers()
    })

    it('shows the spinner once the fetch outlives the delay', async () => {
      vi.useFakeTimers()
      const { wrapper: w } = mountPending()
      await nextTick()
      vi.advanceTimersByTime(LOADING_DELAY_MS)
      await nextTick()

      expect(tableLoading(w)).toBe(true)
      vi.useRealTimers()
    })

    it('clears the spinner when the fetch resolves', async () => {
      vi.useFakeTimers()
      const { wrapper: w, release } = mountPending()
      await nextTick()
      vi.advanceTimersByTime(LOADING_DELAY_MS)
      await nextTick()
      expect(tableLoading(w)).toBe(true)

      vi.useRealTimers()
      release()
      await flushPromises()

      expect(tableLoading(w)).toBe(false)
    })

    // The tables clear their rows when a fetch starts, so without this the empty state would say
    // 'No results found.' while the fetch is still running -- a claim about the data rather than
    // about still waiting for it.
    it('does not claim there are no results while fetching', async () => {
      const { wrapper: w, release } = mountPending()
      await nextTick()

      expect(w.find('[data-test="empty-list"]').exists()).toBe(false)

      release()
      await flushPromises()

      expect(w.find('[data-test="empty-list"]').exists()).toBe(true)
    })
  })

  // Which directions an interface carries is on the row already (hasIngressFlows /
  // hasEgressFlows); only the URL behind the button costs a request.
  describe('Flows column', () => {
    const mountRow = async (snmpInterface: Record<string, unknown>) => {
      nodeStore.snmpInterfaces = [{ id: 1, ifIndex: 14, ifName: 'eth0', ...snmpInterface }] as any
      nodeStore.snmpInterfacesTotalCount = 1
      await nextTick()
    }

    const tags = () => wrapper.findAll('[data-test^="flows-"][data-test$="-tag"]').map(t => t.text())

    it('shows an I tag for an interface with only ingress flows', async () => {
      await mountRow({ hasIngressFlows: true, hasEgressFlows: false })

      expect(tags()).toEqual(['I'])
    })

    it('shows an E tag for an interface with only egress flows', async () => {
      await mountRow({ hasIngressFlows: false, hasEgressFlows: true })

      expect(tags()).toEqual(['E'])
    })

    it('shows both tags for an interface carrying flows in both directions', async () => {
      await mountRow({ hasIngressFlows: true, hasEgressFlows: true })

      expect(tags()).toEqual(['I', 'E'])
    })

    it('colours the tags as success', async () => {
      await mountRow({ hasIngressFlows: true, hasEgressFlows: true })

      const severities = wrapper.findAllComponents({ name: 'OnmsTag' })
        .filter(t => ['I', 'E'].includes(t.props('value') as string))
        .map(t => t.props('severity'))
      expect(severities).toEqual(['success', 'success'])
    })

    // Both tags say the same thing: the tooltip describes the row, not the tag it hangs off.
    it.each([
      [true, false, 'Ingress flow data available'],
      [false, true, 'Egress flow data available'],
      [true, true, 'Ingress/egress flow data available']
    ])('explains %s/%s flows with "%s"', async (hasIngressFlows, hasEgressFlows, text) => {
      await mountRow({ hasIngressFlows, hasEgressFlows })

      const tooltips = wrapper.findAll('[data-test^="flows-"][data-test$="-tag"]')
        .map(t => (t.element as never as Record<string, unknown>).$_ptooltipValue)
      expect(tooltips.length).toBeGreaterThan(0)
      tooltips.forEach(tooltip => expect(tooltip).toBe(text))
    })

    // 'If none of the 3 flows conditions applies, leave the field empty.'
    it('leaves the cell empty for an interface with no flows', async () => {
      await mountRow({ hasIngressFlows: false, hasEgressFlows: false })

      expect(wrapper.find('[data-test="flows"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="flows-button"]').exists()).toBe(false)
    })

    it('offers the graphs button whenever there are flows', async () => {
      await mountRow({ hasIngressFlows: false, hasEgressFlows: true })

      expect(wrapper.find('[data-test="flows-button"]').exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'OnmsIconButton' }).props('tooltip'))
        .toBe('View Egress flow graphs.')
    })

    describe('opening the graphs', () => {
      let tab: { location: { href: string }, close: ReturnType<typeof vi.fn>, opener: unknown }

      beforeEach(async () => {
        tab = { location: { href: '' }, close: vi.fn(), opener: {}}
        vi.stubGlobal('open', vi.fn().mockReturnValue(tab))
        showSnackBar.mockClear()
        getFlowGraphUrl.mockReset()
        await mountRow({ hasIngressFlows: true, hasEgressFlows: true })
      })

      afterEach(() => {
        vi.unstubAllGlobals()
      })

      const click = async () => {
        await wrapper.find('[data-test="flows-button"]').trigger('click')
        await flushPromises()
      }

      // Resolved for the row that was clicked, on the node currently being shown.
      it('asks for the URL of that interface only when clicked', async () => {
        getFlowGraphUrl.mockResolvedValue('https://grafana:3000/d/flows?node=42&interface=14')

        expect(getFlowGraphUrl).not.toHaveBeenCalled()

        await click()

        expect(getFlowGraphUrl).toHaveBeenCalledWith(mockNodeId, 14)
      })

      // The tab is opened inside the click, before the await -- a tab opened afterwards is
      // blocked, the user gesture having expired.
      it('opens the tab on the click and navigates it once the URL arrives', async () => {
        const url = 'https://grafana:3000/d/flows?node=42&interface=14'
        getFlowGraphUrl.mockResolvedValue(url)

        await click()

        expect(window.open).toHaveBeenCalledWith('', '_blank')
        expect(tab.opener).toBeNull()
        expect(tab.location.href).toBe(url)
        expect(tab.close).not.toHaveBeenCalled()
      })

      // flowGraphUrl is unset by default, and the endpoint then answers 204 with no body.
      it('reports an unconfigured flowGraphUrl and closes the tab', async () => {
        getFlowGraphUrl.mockResolvedValue('')

        await click()

        expect(tab.close).toHaveBeenCalled()
        expect(showSnackBar).toHaveBeenCalledWith({
          msg: 'No \'flowGraphUrl\' was configured.',
          error: true
        })
      })

      // Distinct from the unconfigured case: the flows feature may be absent altogether.
      it('reports a failed lookup and closes the tab', async () => {
        getFlowGraphUrl.mockRejectedValue(new Error('404'))

        await click()

        expect(tab.close).toHaveBeenCalled()
        expect(showSnackBar).toHaveBeenCalledWith({
          msg: 'Could not look up the flow graph URL.',
          error: true
        })
      })

      // Nothing at all happens otherwise: the blank tab never appeared to explain itself.
      it('reports a blocked pop-up', async () => {
        vi.stubGlobal('open', vi.fn().mockReturnValue(null))
        getFlowGraphUrl.mockResolvedValue('https://grafana:3000/d/flows')

        await click()

        expect(showSnackBar).toHaveBeenCalledWith({
          msg: 'The browser blocked the new tab. Allow pop-ups for this site to open flow graphs.',
          error: true
        })
      })

      // A second click while the first is in flight would open a second tab.
      it('ignores a click while a lookup is already running', async () => {
        let resolve: (url: string) => void = () => undefined
        getFlowGraphUrl.mockReturnValue(new Promise<string>((r) => {
          resolve = r
        }))

        await wrapper.find('[data-test="flows-button"]').trigger('click')
        await nextTick()
        await wrapper.find('[data-test="flows-button"]').trigger('click')

        expect(getFlowGraphUrl).toHaveBeenCalledTimes(1)

        resolve('https://grafana:3000/d/flows')
        await flushPromises()
      })
    })
  })

})
