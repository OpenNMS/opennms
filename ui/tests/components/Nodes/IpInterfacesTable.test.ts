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
import { LOADING_DELAY_MS } from '@/components/Nodes/hooks/useDelayedLoading'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { SEARCH_DEBOUNCE_MS } from '@/components/Nodes/hooks/useDebouncedSearch'
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

describe('IpInterfacesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let nodeStore: ReturnType<typeof useNodeStore>

  // The store action must be mocked BEFORE mounting — the component fetches in
  // onMounted, and an unmocked action would fire a real network request.
  const mountTable = () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    nodeStore = useNodeStore(pinia)
    nodeStore.getNodeIpInterfaces = vi.fn().mockResolvedValue(undefined)
    useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any

    return mount(IpInterfacesTable, {
      global: {
        plugins: [pinia, PrimeVue]
      }
    })
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountTable()
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

    it('renders the four columns in order', () => {
      expect(wrapper.findAll('th').map(h => h.text()))
        .toEqual(['IP Address', 'IP Host Name', 'SNMP ifIndex', 'Status'])
    })

    // Spelling the status out makes the column too wide for a half-page panel, so this table
    // scrolls like the SNMP one rather than compressing its columns.
    it('scrolls horizontally', () => {
      const table = wrapper.findComponent({ name: 'OnmsTable' })
      expect(table.props('scrollable')).toBe(true)
      expect(table.props('tableStyle')).toContain('min-width')
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

  describe('IP Address links', () => {
    const mountRows = async (ipInterfaces: Record<string, unknown>[]) => {
      nodeStore.ipInterfaces = ipInterfaces as any
      nodeStore.ipInterfacesTotalCount = ipInterfaces.length
      await nextTick()
    }

    it('links the address to the legacy interface page for this node', async () => {
      await mountRows([{ id: '1', ipAddress: '10.0.0.44' }])

      const link = wrapper.find('[data-test="ip-address-link"]')
      expect(link.text()).toBe('10.0.0.44')
      expect(link.attributes('href'))
        .toBe(`/opennms/element/interface.jsp?node=${mockNodeId}&intf=10.0.0.44`)
    })

    // The page takes the interface by address, and an IPv6 address is full of reserved
    // characters -- a zone index especially, whose '%' would otherwise start an escape.
    it('encodes the address', async () => {
      await mountRows([{ id: '1', ipAddress: 'fe80::1%eth0' }])

      const link = wrapper.find('[data-test="ip-address-link"]')
      expect(link.text()).toBe('fe80::1%eth0')
      expect(link.attributes('href'))
        .toBe(`/opennms/element/interface.jsp?node=${mockNodeId}&intf=fe80%3A%3A1%25eth0`)
    })

    it('links each row to its own address', async () => {
      await mountRows([{ id: '1', ipAddress: '10.0.0.44' }, { id: '2', ipAddress: '10.0.0.45' }])

      expect(wrapper.findAll('[data-test="ip-address-link"]').map(l => l.attributes('href')))
        .toEqual([
          `/opennms/element/interface.jsp?node=${mockNodeId}&intf=10.0.0.44`,
          `/opennms/element/interface.jsp?node=${mockNodeId}&intf=10.0.0.45`
        ])
    })

    // Follows the node the user navigates to, the same way the fetch does.
    it('points at the new node after a node change', async () => {
      ;(useRoute() as any).params.id = '99'
      await flushPromises()
      await mountRows([{ id: '1', ipAddress: '10.0.0.44' }])

      expect(wrapper.find('[data-test="ip-address-link"]').attributes('href'))
        .toBe('/opennms/element/interface.jsp?node=99&intf=10.0.0.44')
      ;(useRoute() as any).params.id = '42'
    })

    it('shows N/A rather than an empty link when there is no address', async () => {
      await mountRows([{ id: '1', ipAddress: '' }])

      expect(wrapper.find('[data-test="ip-address-link"]').exists()).toBe(false)
      expect(wrapper.find('tbody tr').text()).toContain('N/A')
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

  describe('Sorting', () => {
    const addresses = () => wrapper.findAll('[data-test="ip-address-link"]').map(l => l.text())

    const headerFor = (label: string) =>
      wrapper.findAll('th').find(th => th.text() === label.toUpperCase() || th.text() === label)!

    beforeEach(async () => {
      nodeStore.ipInterfaces = [
        { id: '1', ipAddress: '10.0.0.7', hostName: 'beta', ifIndex: 3, isManaged: 'M' },
        { id: '2', ipAddress: '10.0.0.2', hostName: 'Alpha', ifIndex: 1, isManaged: 'U' },
        { id: '3', ipAddress: '10.0.0.5', hostName: 'charlie', ifIndex: 2, isManaged: 'M' }
      ] as any
      nodeStore.ipInterfacesTotalCount = 3
      await nextTick()
    })

    it('defaults to IP address ascending', () => {
      expect(addresses()).toEqual(['10.0.0.2', '10.0.0.5', '10.0.0.7'])
    })

    // These fixtures order the same lexically and numerically, so they cannot tell the two
    // apart. .10 sorting after .2 is the part that can only come from a numeric comparison --
    // lexically '10.0.0.10' < '10.0.0.2', since '1' precedes '2'.
    it('sorts the last octet numerically, not lexically', async () => {
      nodeStore.ipInterfaces = [
        { id: '1', ipAddress: '10.0.0.10', hostName: 'ten', ifIndex: 1, isManaged: 'M' },
        { id: '2', ipAddress: '10.0.0.2', hostName: 'two', ifIndex: 2, isManaged: 'M' }
      ] as any
      nodeStore.ipInterfacesTotalCount = 2
      await nextTick()

      expect(addresses()).toEqual(['10.0.0.2', '10.0.0.10'])
    })

    // Addresses are sorted as text, not taken apart into their numeric parts, which for IPv4
    // happens to come out the same. IPv6 is where the two part company, because the numeric
    // comparison above reads hex digit groups as decimal: '10' is sixteen in an address but ten
    // to the comparator, so it lands before 'a' instead of after it.
    //
    // This pins the text behaviour deliberately. If address-aware sorting is ever added, the
    // right order becomes fe80::9, fe80::a, fe80::10 and this test SHOULD fail -- update it
    // rather than work around it.
    it('sorts IPv6 addresses as text, so ::10 comes before ::a', async () => {
      nodeStore.ipInterfaces = [
        { id: '1', ipAddress: 'fe80::10', hostName: 'sixteen', ifIndex: 1, isManaged: 'M' },
        { id: '2', ipAddress: 'fe80::a', hostName: 'ten', ifIndex: 2, isManaged: 'M' },
        { id: '3', ipAddress: 'fe80::9', hostName: 'nine', ifIndex: 3, isManaged: 'M' }
      ] as any
      nodeStore.ipInterfacesTotalCount = 3
      await nextTick()

      expect(addresses()).toEqual(['fe80::9', 'fe80::10', 'fe80::a'])
    })

    it('reverses when the header is clicked', async () => {
      await headerFor('IP Address').trigger('click')
      await nextTick()

      expect(addresses()).toEqual(['10.0.0.7', '10.0.0.5', '10.0.0.2'])
    })

    it('sorts on another column when its header is clicked', async () => {
      await headerFor('SNMP ifIndex').trigger('click')
      await nextTick()

      expect(addresses()).toEqual(['10.0.0.2', '10.0.0.5', '10.0.0.7'])
    })

    // 'Alpha' before 'beta' is the case-insensitive part.
    it('sorts case-insensitively', async () => {
      await headerFor('IP Host Name').trigger('click')
      await nextTick()

      expect(wrapper.findAll('tbody tr').map(r => r.findAll('td')[1].text()))
        .toEqual(['Alpha', 'beta', 'charlie'])
    })
  })

  describe('Filtering', () => {
    const search = () => wrapper.find('[data-test="ip-interfaces-search"]')
    // By the link rather than by <tr>: PrimeVue renders the #empty slot as a row of its own.
    const addresses = () => wrapper.findAll('[data-test="ip-address-link"]').map(l => l.text())

    const type = async (value: string) => {
      await search().setValue(value)
      vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS)
      await nextTick()
    }

    beforeEach(async () => {
      vi.useFakeTimers()
      nodeStore.ipInterfaces = [
        { id: '1', ipAddress: '10.0.0.7', hostName: 'router-a', ifIndex: 3, isManaged: 'M' },
        { id: '2', ipAddress: '10.0.0.2', hostName: 'Switch-B', ifIndex: 1, isManaged: 'U' },
        { id: '3', ipAddress: '192.168.1.1', hostName: 'gateway', ifIndex: 2, isManaged: 'M' }
      ] as any
      nodeStore.ipInterfacesTotalCount = 3
      await nextTick()
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('renders a search input', () => {
      expect(search().exists()).toBe(true)
    })

    it('filters on a single character', async () => {
      await type('9')

      expect(addresses()).toEqual(['192.168.1.1'])
    })

    it('matches case-insensitively', async () => {
      await type('switch-b')

      expect(addresses()).toEqual(['10.0.0.2'])
    })

    // The point of spelling the code out: the raw column only ever answered to 'M'.
    it('matches on the spelled-out status', async () => {
      await type('unmanaged')

      expect(addresses()).toEqual(['10.0.0.2'])
    })

    // Substring matching, so the broader term takes in the narrower labels too -- 'Unmanaged'
    // and 'Forced Unmanaged' both contain 'managed'.
    it('matches every managed state on managed', async () => {
      await type('managed')

      expect(addresses()).toEqual(['10.0.0.2', '10.0.0.7', '192.168.1.1'])
    })

    it('matches on the address', async () => {
      await type('10.0.0.')

      expect(addresses()).toEqual(['10.0.0.2', '10.0.0.7'])
    })

    it('shows the empty state when nothing matches', async () => {
      await type('nothing-matches-this')

      expect(addresses()).toEqual([])
      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
    })

    it('does not filter until the debounce elapses', async () => {
      await search().setValue('9')
      vi.advanceTimersByTime(SEARCH_DEBOUNCE_MS - 50)
      await nextTick()

      expect(addresses().length).toBe(3)
    })

    it('restores every row when the term is cleared', async () => {
      await type('9')
      await type('')

      expect(addresses().length).toBe(3)
    })

    // Filtering does not go back to the server -- every row is already in hand.
    it('does not refetch', async () => {
      vi.mocked(nodeStore.getNodeIpInterfaces).mockClear()
      await type('router')

      expect(nodeStore.getNodeIpInterfaces).not.toHaveBeenCalled()
    })
  })

  describe('Pagination', () => {
    // Client-side now: the table holds every row, so turning a page is not a request.
    it('does not refetch when the page changes', async () => {
      vi.mocked(nodeStore.getNodeIpInterfaces).mockClear()
      await wrapper.vm.onPage({ first: 5, rows: 5, page: 1, pageCount: 2 })
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).not.toHaveBeenCalled()
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

      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledWith({
        id: '99',
        queryParameters: { limit: 0 }
      })
      ;(useRoute() as any).params.id = '42'
    })
  })

  // The details page mounts both tabs' tables at once, so fetching on mount meant every node
  // details load paid for every IP interface on the node -- the expensive one on a switch
  // with thousands of them -- even for a user who never opened this tab.
  describe('Deferred fetch', () => {
    const mountInactive = () => {
      const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
      nodeStore = useNodeStore(pinia)
      nodeStore.getNodeIpInterfaces = vi.fn().mockResolvedValue(undefined)
      useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any

      return mount(IpInterfacesTable, {
        props: { active: false },
        global: { plugins: [pinia, PrimeVue] }
      })
    }

    it('does not fetch while its tab is hidden', async () => {
      mountInactive()
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).not.toHaveBeenCalled()
    })

    it('fetches when its tab is first shown', async () => {
      const hidden = mountInactive()
      await flushPromises()

      await hidden.setProps({ active: true })
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledTimes(1)
    })

    // Switching away and back must not pay for the rows a second time.
    it('does not refetch when the tab is hidden and shown again', async () => {
      const hidden = mountInactive()
      await hidden.setProps({ active: true })
      await flushPromises()
      await hidden.setProps({ active: false })
      await hidden.setProps({ active: true })
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledTimes(1)
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
      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledTimes(1)

      await hidden.setProps({ active: true })
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledTimes(2)
      expect(nodeStore.getNodeIpInterfaces).toHaveBeenLastCalledWith(expect.objectContaining({ id: '99' }))
      ;(useRoute() as any).params.id = '42'
    })

    it('refetches for a new node while the tab is showing', async () => {
      const shown = mountInactive()
      await shown.setProps({ active: true })
      await flushPromises()

      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledTimes(2)
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
      nodeStore.getNodeIpInterfaces = vi.fn().mockReturnValue(pending)
      useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any

      const w = mount(IpInterfacesTable, {
        global: { plugins: [pinia, PrimeVue] }
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

  // isManaged is an unconstrained char(1) with no enum behind it; the labels are ElementUtil's
  // m_interfaceStatusMap, the only place in the product that turns these codes into words.
  describe('Status column', () => {
    const mountRows = async (codes: Array<string | null>) => {
      nodeStore.ipInterfaces = codes.map((isManaged, i) => ({
        id: String(i + 1),
        ipAddress: `10.0.0.${i + 1}`,
        hostName: `host-${i + 1}`,
        ifIndex: i + 1,
        isManaged
      })) as any
      nodeStore.ipInterfacesTotalCount = codes.length
      await nextTick()
    }

    it.each([
      ['M', 'Managed', 'success'],
      ['U', 'Unmanaged', 'warn'],
      ['F', 'Forced Unmanaged', 'warn'],
      ['N', 'Not Monitored', 'secondary'],
      ['D', 'Deleted', 'danger'],
      ['A', 'Unknown (A)', 'warn']
    ])('renders %s as "%s" with %s severity', async (code, label, severity) => {
      await mountRows([code])

      expect(wrapper.find('[data-test="status-tag"]').text()).toBe(label)
      expect(wrapper.findComponent({ name: 'OnmsTag' }).props('severity')).toBe(severity)
    })

    // The column is a bare char(1), so a code nobody has defined is possible. Naming it beats
    // rendering an empty cell, which is what the JSP does (ElementUtil returns null).
    it('names an undefined code rather than rendering nothing', async () => {
      await mountRows(['X'])

      expect(wrapper.find('[data-test="status-tag"]').text()).toBe('Unknown (X)')
      expect(wrapper.findComponent({ name: 'OnmsTag' }).props('severity')).toBe('warn')
    })

    it('reads a missing code as Unknown', async () => {
      await mountRows([null])

      expect(wrapper.find('[data-test="status-tag"]').text()).toBe('Unknown')
    })

    it('tags each row on its own status', async () => {
      await mountRows(['M', 'U', 'F', 'D'])

      expect(wrapper.findAll('[data-test="status-tag"]').map(t => t.text()))
        .toEqual(['Managed', 'Unmanaged', 'Forced Unmanaged', 'Deleted'])
    })

    // status is derived, not stored, so it is decorated onto the rows to be sortable.
    it('sorts on the derived label', async () => {
      await mountRows(['U', 'M', 'D'])
      const header = wrapper.findAll('th').find(th => th.text() === 'STATUS' || th.text() === 'Status')!
      await header.trigger('click')
      await nextTick()

      expect(wrapper.findAll('[data-test="status-tag"]').map(t => t.text()))
        .toEqual(['Deleted', 'Managed', 'Unmanaged'])
    })
  })

  // The panel used to ask for isManaged==U,P,N,M: 'P' is an isSnmpPrimary code, not an interface
  // status, and 'F' -- the state the admin Unmanage action writes -- was missing, so every
  // explicitly unmanaged interface was absent from the table.
  describe('Fetching every interface', () => {
    it('does not narrow the fetch by isManaged', async () => {
      expect(nodeStore.getNodeIpInterfaces).toHaveBeenCalledWith({
        id: '42',
        queryParameters: { limit: 0 }
      })
    })

    it('shows an interface an operator has unmanaged', async () => {
      nodeStore.ipInterfaces = [
        { id: '1', ipAddress: '10.0.0.1', hostName: 'host-1', ifIndex: 1, isManaged: 'F' }
      ] as any
      nodeStore.ipInterfacesTotalCount = 1
      await nextTick()

      expect(wrapper.findAll('[data-test="ip-address-link"]').map(l => l.text())).toEqual(['10.0.0.1'])
      expect(wrapper.find('[data-test="status-tag"]').text()).toBe('Forced Unmanaged')
    })
  })

})
