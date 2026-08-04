// ui/tests/components/Nodes/NodesTable.test.ts
import NodesTable from '@/components/Nodes/NodesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { FilterTypeEnum } from '@/types'
import { defaultColumns } from '@/components/Nodes/utils'
import { SORT } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import Column from 'primevue/column'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

// ── Module mocks ───────────────────────────────────────────────────────────────

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: vi.fn() })
}))

vi.mock('@/services', () => ({
  default: {
    getNodes: vi.fn().mockResolvedValue({ nodes: [], totalCount: 0 }),
    getCategories: vi.fn().mockResolvedValue([]),
    getMonitoringLocations: vi.fn().mockResolvedValue([]),
    getServiceTypes: vi.fn().mockResolvedValue([])
  }
}))

vi.mock('@/services/ipInterfaceService', () => ({
  getNodeIpInterfaceQuery: vi.fn().mockResolvedValue([]),
  getIpInterfaces: vi.fn().mockResolvedValue({ ipInterfaces: [], totalCount: 0 })
}))

vi.mock('@/components/Nodes/hooks/useNodeExport', () => ({
  useNodeExport: () => ({
    generateBlob: vi.fn(),
    generateDownload: vi.fn(),
    getExportData: vi.fn().mockResolvedValue(null)
  })
}))

vi.mock('@/components/Nodes/hooks/useNodeQuery', async () => {
  // Real (unmocked) module — delegated to below for sanitizeSearchTerm and for
  // buildUpdatedNodeStructureQueryParameters, so tests that write a searchTerm directly into the
  // store can assert on the ACTUAL request shape (e.g. that a term with FIQL-special characters
  // really does get double-encoded end-to-end), rather than a naive passthrough stub.
  const actual = await vi.importActual<typeof import('@/components/Nodes/hooks/useNodeQuery')>(
    '@/components/Nodes/hooks/useNodeQuery'
  )

  const makeDefaultFilter = () => ({
    searchTerm: '',
    categoryMode: 'Union',
    selectedCategories: [],
    selectedCategories2: [],
    selectedServices: [],
    selectedFlows: [],
    selectedMonitoringLocations: [],
    ipAddress: '',
    macAddress: '',
    topology: '',
    nodesWithDownAggregateStatus: false,
    nodesWithAssets: false,
    nodesWithOutages: false,
    assetFilters: [],
    extendedSearch: {
      foreignSourceParams: { foreignId: '', foreignSource: '', foreignSourceId: '' },
      snmpParams: { snmpIfAlias: '', snmpIfDescription: '', snmpIfIndex: '', snmpIfName: '', snmpIfType: '' },
      sysParams: { sysContact: '', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '' }
    }
  })
  return {
    useNodeQuery: () => ({
      buildUpdatedNodeStructureQueryParameters: actual.useNodeQuery().buildUpdatedNodeStructureQueryParameters,
      getExtendedSearchValues: vi.fn().mockReturnValue([]),
      getDefaultNodeQueryFilter: makeDefaultFilter,
      getDefaultNodeQueryForeignSourceParams: () => ({ foreignId: '', foreignSource: '', foreignSourceId: '' }),
      getDefaultNodeQuerySnmpParams: () => ({ snmpIfAlias: '', snmpIfDescription: '', snmpIfIndex: '', snmpIfName: '', snmpIfType: '' }),
      getDefaultNodeQuerySysParams: () => ({ sysContact: '', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '' }),
      buildNodeQueryFilterFromQueryString: vi.fn().mockReturnValue(makeDefaultFilter()),
      queryStringHasTrackedValues: vi.fn().mockReturnValue(false)
    }),
    sanitizeSearchTerm: actual.sanitizeSearchTerm
  }
})

// Stub heavy child components that have their own dependencies
const stubs = {
  NodeAdvancedFiltersDrawer: { name: 'NodeAdvancedFiltersDrawer', template: '<div></div>' },
  ColumnSelectionDrawer: { name: 'ColumnSelectionDrawer', template: '<div></div>' },
  NodeDetailsDialog: { name: 'NodeDetailsDialog', template: '<div></div>', props: ['visible', 'node', 'computeNodeLink', 'computeNodeIpInterfaceLink'] },
  NodeDownloadDropdown: { name: 'NodeDownloadDropdown', template: '<div></div>', props: ['onCsvDownload', 'onJsonDownload'] },
  NodeActionsDropdown: { name: 'NodeActionsDropdown', template: '<div></div>', props: ['baseHref', 'node', 'triggerNodeInfo'] },
  NodeTooltipCell: { name: 'NodeTooltipCell', template: '<span></span>', props: ['text'] },
  ManagementIPTooltipCell: { name: 'ManagementIPTooltipCell', template: '<span></span>', props: ['computeNodeIpInterfaceLink', 'node', 'nodeToIpInterfaceMap'] },
  FlowTooltipCell: { name: 'FlowTooltipCell', template: '<span></span>', props: ['node'] },
  OnmsMessageDialog: { name: 'OnmsMessageDialog', template: '<div><slot name="content" /></div>', props: ['visible', 'relative', 'maxHeight', 'maxWidth', 'title'] },
  EmptyList: { name: 'EmptyList', template: '<div class="empty-list-stub"></div>', props: ['content'] },
  NodeInterfacesPanel: { name: 'NodeInterfacesPanel', template: '<div class="node-interfaces-panel-stub"></div>', props: ['node'] }
}

// ── Mount helper ───────────────────────────────────────────────────────────────

const mountTable = () =>
  mount(NodesTable, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

// ── Tests ──────────────────────────────────────────────────────────────────────

describe('NodesTable.vue', () => {
  let nodeStore: ReturnType<typeof useNodeStore>
  let structure: ReturnType<typeof useNodeStructureStore>

  beforeEach(async () => {
    vi.clearAllMocks()

    // mount first so pinia initialises the stores
    const wrapper = mountTable()

    nodeStore = useNodeStore()
    structure = useNodeStructureStore()
    const menuStore = useMenuStore()

    // Seed stores
    menuStore.mainMenu = { baseHref: '/opennms/', homeUrl: '/opennms', baseNodeUrl: 'element/node.jsp?node=' } as any
    nodeStore.nodes = []
    nodeStore.totalCount = 0
    nodeStore.nodeQueryParameters = { limit: 50, offset: 0, orderBy: 'label' } as any
    nodeStore.getNodes = vi.fn().mockResolvedValue(undefined)
    nodeStore.setNodeQueryParameters = vi.fn().mockResolvedValue(undefined)

    // Seed structure store with default columns (several selected)
    structure.columns = defaultColumns.map(c => ({ ...c }))
    structure.selectedCategories = []
    structure.selectedCategories2 = []
    structure.selectedFlows = []
    structure.selectedServices = []
    structure.removeCategory = vi.fn()
    structure.removeCategory2 = vi.fn()
    structure.removeFlow = vi.fn()
    structure.removeMonitoringLocation = vi.fn()
    structure.removeService = vi.fn()

    await nextTick()

    wrapper.unmount()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders a column per selected store column plus Actions', () => {
    const wrapper = mountTable()
    const headers = wrapper.findAll('th').map(th => th.text().trim())
    expect(headers).toContain('Actions')
    // defaultColumns has label selected with label 'Node Label'
    expect(headers.some(h => /Node Label/i.test(h))).toBe(true)
  })

  it('onSort updates query order and refetches — descending', () => {
    const wrapper = mountTable()
    const ns = useNodeStore()
    ns.getNodes = vi.fn().mockResolvedValue(undefined)
    ;(wrapper.vm as any).onSort({ sortField: 'label', sortOrder: -1 })
    expect(ns.getNodes).toHaveBeenCalledWith(
      expect.objectContaining({ orderBy: 'label', order: SORT.DESCENDING }),
      true
    )
  })

  it('onSort updates query order and refetches — ascending', () => {
    const wrapper = mountTable()
    const ns = useNodeStore()
    ns.getNodes = vi.fn().mockResolvedValue(undefined)
    ;(wrapper.vm as any).onSort({ sortField: 'label', sortOrder: 1 })
    expect(ns.getNodes).toHaveBeenCalledWith(
      expect.objectContaining({ orderBy: 'label', order: SORT.ASCENDING }),
      true
    )
  })

  it('does not sort on the ipaddress column', () => {
    const wrapper = mountTable()
    const ns = useNodeStore()
    ns.getNodes = vi.fn().mockResolvedValue(undefined)
    ;(ns.getNodes as any).mockClear?.()
    ;(wrapper.vm as any).onSort({ sortField: 'ipaddress', sortOrder: 1 })
    expect(ns.getNodes).not.toHaveBeenCalled()
  })

  it('onPage advances the page — same page size sets correct offset', () => {
    // pageSize is seeded to 50 (nodeStore.nodeQueryParameters.limit)
    // page: 2 (0-indexed) + 1 = page 3, offset = (3-1) * 50 = 100
    const wrapper = mountTable()
    const ns = useNodeStore()
    ns.setNodeQueryParameters = vi.fn().mockResolvedValue(undefined)
    ;(wrapper.vm as any).onPage({ page: 2, rows: 50 })
    expect(ns.setNodeQueryParameters).toHaveBeenCalledWith(
      expect.objectContaining({ offset: 100 })
    )
  })

  it('onPage changes page size — resets offset to 0 and updates limit', () => {
    // rows !== pageSize (50) → updatePageSize path: limit = new rows, offset = 0
    const wrapper = mountTable()
    const ns = useNodeStore()
    ns.setNodeQueryParameters = vi.fn().mockResolvedValue(undefined)
    ;(wrapper.vm as any).onPage({ page: 0, rows: 20 })
    expect(ns.setNodeQueryParameters).toHaveBeenCalledWith(
      expect.objectContaining({ limit: 20, offset: 0 })
    )
  })

  it('removing a category chip calls the store', () => {
    // Seed a category before mounting so the chip renders
    const wrapper = mountTable()
    const str = useNodeStructureStore()
    str.removeCategory = vi.fn()
    ;(wrapper.vm as any).removeItem({ _text: 'Routers', _value: '1' }, FilterTypeEnum.Category)
    expect(str.removeCategory).toHaveBeenCalled()
  })

  // ── "Show interfaces" mode ──────────────────────────────────────────────────

  describe('Show interfaces mode', () => {
    it('renders no expander column and no caret buttons when showInterfaces is off', () => {
      const wrapper = mountTable()
      const selectedCount = defaultColumns.filter(c => c.selected).length
      const headers = wrapper.findAll('th')
      // no expander column: one per selected column + Actions
      expect(headers.length).toBe(selectedCount + 1)
      expect(wrapper.findAll('[data-test="row-expander-toggle"]').length).toBe(0)
    })

    it('renders a leading expander column once showInterfaces is on', async () => {
      const wrapper = mountTable()
      const structure = useNodeStructureStore()
      structure.setShowInterfaces(true)
      await nextTick()

      const selectedCount = defaultColumns.filter(c => c.selected).length
      const headers = wrapper.findAll('th')
      // expander column (no header text) + one per selected column + Actions
      expect(headers.length).toBe(selectedCount + 2)
    })

    it('toggle button flips nodeStructureStore.showInterfaces and updates its label', async () => {
      const wrapper = mountTable()
      const structure = useNodeStructureStore()
      expect(structure.showInterfaces).toBe(false)

      const toggleBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'show-interfaces-button')
      expect(toggleBtn).toBeDefined()
      expect(toggleBtn!.text()).toContain('Show interfaces')

      await toggleBtn!.trigger('click')

      expect(structure.showInterfaces).toBe(true)
    })

    it('expands only rows with expandable interface content when showInterfaces turns on, and collapses all when turned off', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1' }, { id: '2' }, { id: '3' }] as any
      ns.nodeToIpInterfaceMap = new Map([
        // '1': 2 IP interfaces -> expandable in default mode (> 1 threshold)
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]],
        // '2': 1 IP interface -> NOT expandable in default mode (already visible in IP column)
        ['2', [{ id: 'ip3', ipAddress: '10.0.0.3', isManaged: 'M' }]]
        // '3': no entry -> 0 interfaces -> NOT expandable
      ]) as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })

      structure.setShowInterfaces(false)
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({})
    })

    it('renders the footer with node/interface counts only when showInterfaces is on', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1' }] as any
      ns.totalCount = 1
      ns.nodeToIpInterfaceMap = new Map([['1', [{ id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' }]]]) as any
      await nextTick()

      expect(wrapper.find('[data-test="interfaces-footer"]').exists()).toBe(false)

      structure.setShowInterfaces(true)
      await nextTick()

      const footer = wrapper.find('[data-test="interfaces-footer"]')
      expect(footer.exists()).toBe(true)
      expect(footer.text().replace(/\s+/g, ' ').trim()).toBe('1 Node total. 1 node and 1 interface on this page')
    })

    it('pluralizes node/interface counts when greater than one', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1' }, { id: '2' }] as any
      ns.totalCount = 5
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [{ id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' }]],
        ['2', [{ id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }]]
      ]) as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      const footer = wrapper.find('[data-test="interfaces-footer"]')
      expect(footer.text().replace(/\s+/g, ' ').trim()).toBe('5 Nodes total. 2 nodes and 2 interfaces on this page')
    })
  })

  // ── Interface expander caret (per-row, threshold depends on mode) ───────────

  describe('Interface expander caret', () => {
    it('default mode: caret renders only for a node with more than one IP interface', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1', label: 'two-ips' }, { id: '2', label: 'one-ip' }, { id: '3', label: 'no-ips' }] as any
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]],
        ['2', [{ id: 'ip3', ipAddress: '10.0.0.3', isManaged: 'M' }]]
        // '3': no entry -> 0 interfaces
      ]) as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect((wrapper.vm as any).isRowExpandable({ id: '1' })).toBe(true)
      expect((wrapper.vm as any).isRowExpandable({ id: '2' })).toBe(false)
      expect((wrapper.vm as any).isRowExpandable({ id: '3' })).toBe(false)

      const carets = wrapper.findAll('[data-test="row-expander-toggle"]')
      expect(carets.length).toBe(1)
    })

    it('maclike mode: caret renders for a node with a single matching SNMP interface (>= 1 threshold)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.nodes = [{ id: '1', label: 'match' }] as any
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      ns.nodeToSnmpInterfaceMap = new Map([
        ['1', [{ id: 5, ifIndex: 2, physAddr: 'aabbccddeeff', collectFlag: 'N', ifName: 'eth0', ifDescr: null }]]
      ]) as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect((wrapper.vm as any).isRowExpandable({ id: '1' })).toBe(true)
      expect(wrapper.findAll('[data-test="row-expander-toggle"]').length).toBe(1)
      // The map was already populated before the toggle here, so the primary auto-expand watcher
      // sees qualifying data synchronously and expands it directly (no catch-up needed) — see the
      // race-ordered test below for the real-world async-fetch-resolves-after-toggle case.
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
    })

    it('auto-expands a qualifying row once the async SNMP fetch resolves AFTER the toggle (maclike mode), and leaves a 0-match row collapsed', async () => {
      // Reproduces real production ordering: nodeToSnmpInterfaceMap is EMPTY at toggle time (the
      // primary auto-expand watcher fires synchronously and sees no data), and the mocked
      // getSnmpInterfacesForNodes only populates the map once its promise is explicitly resolved
      // afterwards — unlike the test above, which pre-seeds the map before toggling and so can't
      // exercise the race the auto-expand watcher and the async fetch create.
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      let resolveFetch: () => void = () => undefined
      const pendingFetch = new Promise<void>((resolve) => {
        resolveFetch = resolve
      })
      ns.getSnmpInterfacesForNodes = vi.fn().mockImplementation(async () => {
        await pendingFetch
        // '1' matches the mac filter (1 matching SNMP interface -> qualifies, >= 1 threshold);
        // '2' has no entry at all -> 0 matches -> must stay collapsed.
        ns.nodeToSnmpInterfaceMap = new Map([
          ['1', [{ id: 5, ifIndex: 2, physAddr: 'aabbccddeeff', collectFlag: 'N', ifName: 'eth0', ifDescr: null }]]
        ]) as any
      })

      ns.nodes = [{ id: '1', label: 'match' }, { id: '2', label: 'no-match' }] as any
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      // Fetch has been kicked off but not yet resolved: map is still empty, so nothing has
      // auto-expanded yet — this is the bug being guarded against.
      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)
      expect((wrapper.vm as any).expandedRows).toEqual({})

      resolveFetch()
      await flushPromises()
      await nextTick()

      // Once the map is actually populated, the qualifying row catches up into expandedRows; the
      // 0-match row is never added.
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
    })

    it('does not force a manually-collapsed row back open when the SNMP map is replaced again for the same fetch generation', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      const snmpMap = () => new Map([
        ['1', [{ id: 5, ifIndex: 2, physAddr: 'aabbccddeeff', collectFlag: 'N', ifName: 'eth0', ifDescr: null }]]
      ]) as any

      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ns.getSnmpInterfacesForNodes = vi.fn().mockImplementation(async () => {
        ns.nodeToSnmpInterfaceMap = snmpMap()
      })

      ns.nodes = [{ id: '1', label: 'match' }] as any
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()
      await flushPromises()
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })

      // User manually collapses it.
      ;(wrapper.vm as any).toggleRowExpanded({ id: '1' })
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // A later, spurious replacement of the SAME fetch generation's map (e.g. an unrelated
      // re-render producing an identical-content new Map instance) must NOT force row '1' back
      // into expandedRows.
      ns.nodeToSnmpInterfaceMap = snmpMap()
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({})
    })

    it('clicking the caret expands the row and clicking again collapses it, reflected in aria-expanded', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1', label: 'node-1' }] as any
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      // Auto-expanded already, since this row qualifies (see the "expands only rows with
      // expandable interface content" test above).
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
      let caret = wrapper.find('[data-test="row-expander-toggle"]')
      expect(caret.exists()).toBe(true)
      expect(caret.attributes('aria-expanded')).toBe('true')
      expect(caret.attributes('aria-label')).toBe('Toggle interfaces for node-1')
      expect(wrapper.find('.node-interfaces-panel-stub').exists()).toBe(true)

      await caret.trigger('click')
      expect((wrapper.vm as any).expandedRows).toEqual({})
      caret = wrapper.find('[data-test="row-expander-toggle"]')
      expect(caret.attributes('aria-expanded')).toBe('false')
      expect(wrapper.find('.node-interfaces-panel-stub').exists()).toBe(false)

      await caret.trigger('click')
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
      caret = wrapper.find('[data-test="row-expander-toggle"]')
      expect(caret.attributes('aria-expanded')).toBe('true')
      expect(wrapper.find('.node-interfaces-panel-stub').exists()).toBe(true)
    })
  })

  // ── IP-interface catch-up (default mode, B1) ─────────────────────────────────
  // Mirrors the maclike/snmpParm SNMP catch-up tests above: nodeStore.getNodes assigns
  // nodes.value and only THEN fires getIpInterfacesForNodes without awaiting it, so the
  // auto-expand watcher's synchronous isRowExpandable() check sees a stale/empty
  // nodeToIpInterfaceMap for a freshly-arrived page. The IP-map catch-up watcher re-evaluates once
  // nodeToIpInterfaceMap is actually replaced (now wholesale — see nodeStore.ts).

  describe('IP-interface catch-up (default mode)', () => {
    it('auto-expands a qualifying row once the IP batch resolves AFTER nodes changes, and leaves 0/1-interface rows collapsed', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      // Reproduces real production ordering: nodes arrives first (e.g. a page change) while
      // nodeToIpInterfaceMap is still whatever the PREVIOUS page left behind — here, empty — and
      // only later does the (mocked) async IP batch replace the map.
      ns.nodes = [
        { id: '1', label: 'two-ips' },
        { id: '2', label: 'one-ip' },
        { id: '3', label: 'no-ips' }
      ] as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      // Map hasn't resolved yet: nothing has auto-expanded — this is the bug being guarded
      // against.
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // The async IP batch resolves and replaces the map wholesale.
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]],
        ['2', [{ id: 'ip3', ipAddress: '10.0.0.3', isManaged: 'M' }]]
        // '3': no entry -> 0 interfaces
      ]) as any
      await nextTick()

      // Only '1' (2 IP interfaces, > 1 threshold in default mode) catches up; '2' (1 interface)
      // and '3' (0 interfaces) stay collapsed.
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
    })

    it('does not force a manually-collapsed row back open when the IP map is replaced again for the same page (generation)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      const ipMap = () => new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any

      ns.nodes = [{ id: '1', label: 'two-ips' }] as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      ns.nodeToIpInterfaceMap = ipMap()
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })

      // User manually collapses it.
      ;(wrapper.vm as any).toggleRowExpanded({ id: '1' })
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // A later, spurious replacement of the SAME page's map (e.g. an unrelated re-render
      // producing an identical-content new Map instance) must NOT force row '1' back into
      // expandedRows.
      ns.nodeToIpInterfaceMap = ipMap()
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({})
    })

    it('applies catch-up again for a new page (new node ids reset the generation key)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1', label: 'two-ips' }] as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })

      // Page changes to a new set of node ids; the map hasn't been re-fetched for them yet.
      ns.nodes = [{ id: '2', label: 'two-ips-page-2' }] as any
      await nextTick()

      // The primary auto-expand watcher already reset expandedRows for the new page (node '2'
      // isn't in the still-stale map yet).
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // The async IP batch for the new page resolves.
      ns.nodeToIpInterfaceMap = new Map([
        ['2', [
          { id: 'ip4', ipAddress: '10.0.1.1', isManaged: 'M' },
          { id: 'ip5', ipAddress: '10.0.1.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()

      // Catch-up applies again for the new page's node id.
      expect((wrapper.vm as any).expandedRows).toEqual({ '2': true })
    })

    it('does not double-handle the IP batch in maclike mode (qualification stays governed by the SNMP map)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.nodes = [{ id: '1', label: 'no-snmp-match' }] as any
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // The IP batch resolves with 2 IP interfaces for node '1' — plenty to qualify under
      // DEFAULT mode's threshold, but irrelevant here since the active mode is maclike and
      // nodeToSnmpInterfaceMap (not nodeToIpInterfaceMap) governs qualification.
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()

      // Still collapsed: no matching SNMP interface, so node '1' doesn't qualify in maclike mode.
      expect((wrapper.vm as any).expandedRows).toEqual({})
    })
  })

  // ── Catch-up generation reset (PR review fix) ────────────────────────────────
  // Both *CatchUpAppliedForKey slots are a single never-reset value keyed on a "generation" key
  // that can legitimately recur (the same page's node ids, or the same nodeIds+narrowing pair)
  // after an intervening generation that never got recorded (nothing qualified) or was never
  // meant to touch that slot at all (a different mode). Without resetting the slot whenever the
  // primary auto-expand watcher runs (a new page, a toggle, or a mode change — always
  // synchronous, always strictly before the corresponding async map replacement can land), a
  // later, genuine resolution for a REPRODUCED key is silently discarded, reproducing the exact
  // B1 symptom this file otherwise fixes.

  describe('Catch-up generation reset (recurring key after an intervening generation)', () => {
    it('applies IP catch-up again when a page recurs after an intervening page with no qualifying rows (A -> B -> A)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1', label: 'page-a' }] as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      // Page A's IP batch resolves: node '1' qualifies (2 interfaces).
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })

      // Page changes to B (different node id): the primary watcher's wholesale recompute
      // collapses everything against the still-stale (page A's) map.
      ns.nodes = [{ id: '2', label: 'page-b' }] as any
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // Page B's IP batch resolves with a NON-qualifying row (1 interface) -- nothing to catch up,
      // so the IP catch-up watcher deliberately leaves its "applied" slot untouched here.
      ns.nodeToIpInterfaceMap = new Map([
        ['2', [{ id: 'ip3', ipAddress: '10.0.1.1', isManaged: 'M' }]]
      ]) as any
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // Page changes back to A (node id '1' recurs) -- same generation key as A's first visit.
      ns.nodes = [{ id: '1', label: 'page-a' }] as any
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // A fresh IP batch for THIS (repeat) visit to page A resolves. Catch-up must apply again
      // even though node id '1' already "caught up" once, several generations ago.
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
    })

    it('applies IP catch-up again after a default -> maclike -> default mode round-trip on the same page', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1', label: 'two-ips' }] as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      // Default mode: IP batch resolves, node '1' qualifies.
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })

      // Switch to maclike mode on the same page/node id: the primary watcher's wholesale
      // recompute uses the (still-empty) SNMP map under maclike's threshold, collapsing the row.
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // Switch back to default mode, same page/node id -- simulate the fresh IP batch for THIS
      // (repeat) visit not having landed yet by clearing the map in the interim.
      ns.nodeToIpInterfaceMap = new Map()
      structure.queryFilter = { ...structure.queryFilter, macAddress: '' }
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // The fresh IP batch for this repeat visit to default mode resolves. Catch-up must apply
      // again even though node id '1' already "caught up" once, several generations ago.
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [
          { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
          { id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }
        ]]
      ]) as any
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
    })

    it('applies SNMP catch-up again after a maclike -> default -> maclike mode round-trip on the same page', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1', label: 'match' }] as any
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // The SNMP batch for this (first) visit to maclike mode resolves with a match.
      ns.nodeToSnmpInterfaceMap = new Map([
        ['1', [{ id: 5, ifIndex: 2, physAddr: 'aabbccddeeff', collectFlag: 'N', ifName: 'eth0', ifDescr: null }]]
      ]) as any
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })

      // Switch to default mode, same page: the primary watcher's wholesale recompute uses the
      // (still-empty) IP map under default's threshold, collapsing the row.
      structure.queryFilter = { ...structure.queryFilter, macAddress: '' }
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // Switch back to maclike mode, same page/node id -- simulate the fresh SNMP batch for THIS
      // (repeat) visit not having landed yet by clearing the map in the interim. (lastSnmpFetchKey
      // reproduces the exact same value as the first visit, since nodeIds/narrowing are unchanged
      // -- that's precisely the case this fix guards.)
      ns.nodeToSnmpInterfaceMap = new Map()
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      await nextTick()
      expect((wrapper.vm as any).expandedRows).toEqual({})

      // The fresh SNMP batch for this repeat visit to maclike mode resolves. Catch-up must apply
      // again even though node id '1' already "caught up" once, several generations ago.
      ns.nodeToSnmpInterfaceMap = new Map([
        ['1', [{ id: 5, ifIndex: 2, physAddr: 'aabbccddeeff', collectFlag: 'N', ifName: 'eth0', ifDescr: null }]]
      ]) as any
      await nextTick()

      expect((wrapper.vm as any).expandedRows).toEqual({ '1': true })
    })
  })

  // ── SNMP interface narrowing fetch (maclike/snmpParm modes) ─────────────────

  describe('SNMP interface narrowing fetch', () => {
    it('does not fetch snmp interfaces in default mode', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1' }] as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect(ns.getSnmpInterfacesForNodes).not.toHaveBeenCalled()
      expect(wrapper.exists()).toBe(true)
    })

    it('fetches snmp interfaces narrowed to physAddr in maclike mode, normalizing the mac', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1' }, { id: '2' }] as any
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'AA:BB-CC' }
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)
      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledWith(['1', '2'], 'physAddr==*aabbcc*')
      expect(wrapper.exists()).toBe(true)
    })

    it('narrows using the fully-normalized MAC (dots/spaces stripped too, not just : and -)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1' }] as any
      // Cisco-style dotted MAC: buildSnmpNarrowing and buildMaclikeQuery/parseMaclike must all
      // normalize this the same way, or the filter (exact-match FIQL) and the panel narrowing
      // (physAddr==*...*) disagree and panels show "No interfaces".
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabb.ccdd' }
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)
      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledWith(['1'], 'physAddr==*aabbccdd*')
      expect(wrapper.exists()).toBe(true)
    })

    it('fetches snmp interfaces narrowed to the snmpParm attribute in snmpParm mode', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1' }] as any
      structure.setFilterWithSnmpParams('snmpIfAlias', 'uplink')
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)
      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledWith(['1'], 'ifAlias==*uplink*')
      expect(wrapper.exists()).toBe(true)
    })

    it('omits the attribute narrowing when the snmpParm value contains SQL wildcards (% or _)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1' }] as any
      structure.setFilterWithSnmpParams('snmpIfAlias', 'up%link')
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)
      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledWith(['1'], undefined)
      expect(wrapper.exists()).toBe(true)
    })

    // The under-fetch guard must also cover ',' ';' '(' ')': ',' / ';' are FIQL set operators that
    // sanitizeSearchTerm elsewhere neutralizes by replacing with spaces (making the server
    // narrowing no longer a superset of the client match, same failure mode as % and _); '(' / ')'
    // are FIQL grouping delimiters that, passed through raw, unbalance the expression and cause a
    // server-side FIQL parse error (surfacing client-side as "No interfaces").
    it.each([
      ['a comma', 'up,link'],
      ['a semicolon', 'up;link'],
      ['an unbalanced open paren', 'up(link'],
      ['an unbalanced close paren', 'up)link']
    ])('omits the attribute narrowing when the snmpParm value contains %s', async (_title, value) => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1' }] as any
      structure.setFilterWithSnmpParams('snmpIfAlias', value)
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)
      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledWith(['1'], undefined)
      expect(wrapper.exists()).toBe(true)
    })

    it('does not re-fetch when nodes/mode/narrowing are unchanged (dedupe)', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getSnmpInterfacesForNodes = vi.fn().mockResolvedValue(undefined)
      ns.getNodes = vi.fn().mockResolvedValue(undefined)

      ns.nodes = [{ id: '1' }] as any
      structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()
      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)

      // Toggling off then on again with the exact same nodes/mode should not re-issue the request.
      structure.setShowInterfaces(false)
      await nextTick()
      structure.setShowInterfaces(true)
      await nextTick()

      expect(ns.getSnmpInterfacesForNodes).toHaveBeenCalledTimes(1)
      expect(wrapper.exists()).toBe(true)
    })
  })

  // ── Flows sort fix ───────────────────────────────────────────────────────────

  describe('flows column sorting', () => {
    it('the Flows column is not sortable', () => {
      const wrapper = mountTable()
      const flowsColumn = wrapper.findAllComponents(Column).find(c => c.props('header') === 'Flows')
      expect(flowsColumn).toBeDefined()
      expect(flowsColumn!.props('sortable')).toBe(false)
    })

    it('does not sort on the flows column', () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ;(wrapper.vm as any).onSort({ sortField: 'flows', sortOrder: 1 })
      expect(ns.getNodes).not.toHaveBeenCalled()
    })
  })

  // ── Search box (NMS-20125 PR review: double-encoding replaces the blocklist) ─

  describe('search box', () => {
    it('typing a term with a former blocklist character (%) searches immediately, with no error UI', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ;(ns.getNodes as any).mockClear()

      const input = wrapper.find('input[data-test="search-input"]')
      await input.setValue('bad%term')
      await nextTick()

      expect(structure.queryFilter.searchTerm).toBe('bad%term')
      expect(wrapper.find('[data-test="search-field"]').text()).not.toContain('Search cannot contain')
      expect(ns.getNodes).toHaveBeenCalled()
      const [requestParams] = (ns.getNodes as any).mock.calls.at(-1)
      expect(requestParams._s).toBe('(label==*bad%2525term*);node.type!=D')
    })

    it.each([
      ['a hash', 'bad#term'],
      ['an ampersand', 'bad&term'],
      ['an open paren', 'bad(term'],
      ['a close paren', 'bad)term']
    ])('searches a term containing %s (R&D-sw1 / Core (bldg 3) style labels are now valid input)', async (_title, value) => {
      const wrapper = mountTable()
      const structure = useNodeStructureStore()

      const input = wrapper.find('input[data-test="search-input"]')
      await input.setValue(value)
      await nextTick()

      expect(structure.queryFilter.searchTerm).toBe(value)
      expect(wrapper.find('[data-test="search-field"]').text()).not.toContain('Search cannot contain')
    })

    it('allows a term containing only an asterisk wildcard', async () => {
      const wrapper = mountTable()
      const structure = useNodeStructureStore()

      const input = wrapper.find('input[data-test="search-input"]')
      await input.setValue('*serv*')
      await nextTick()

      expect(structure.queryFilter.searchTerm).toBe('*serv*')
      expect(wrapper.find('[data-test="search-field"]').text()).not.toContain('Search cannot contain')
    })

    it('a searchTerm written directly into the store (URL nodename param / restored preferences) flows through unchanged, no error UI', async () => {
      // Reproduces the programmatic-injection paths that never go through searchFilterHandler:
      // nodeStructureStore.setFromNodePreferences writes queryFilter.searchTerm straight from
      // either the URL `nodename` param (parseNodeLabel, via Nodes.vue's applyQueryFilter) or a
      // restored localStorage preference — both land here as a raw store mutation. Mutating
      // queryFilter directly, as done elsewhere in this file (e.g. the macAddress tests above),
      // exercises exactly that path without depending on setFromNodePreferences' other side effects.
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()
      ns.getNodes = vi.fn().mockResolvedValue(undefined)
      ;(ns.getNodes as any).mockClear()

      structure.queryFilter = { ...structure.queryFilter, searchTerm: 'bad%term' }
      await nextTick()

      const input = wrapper.find('input[data-test="search-input"]')
      expect((input.element as HTMLInputElement).value).toBe('bad%term')
      expect(wrapper.find('[data-test="search-field"]').text()).not.toContain('Search cannot contain')

      expect(ns.getNodes).toHaveBeenCalled()
      const [requestParams] = (ns.getNodes as any).mock.calls.at(-1)
      expect(requestParams._s).toBe('(label==*bad%2525term*);node.type!=D')
    })
  })

  // ── Node Search help dialog content ──────────────────────────────────────────

  describe('Node Search help dialog', () => {
    it('describes the * wildcard, drops the old underscore/percent claims, and no longer lists disallowed characters', () => {
      const wrapper = mountTable()
      const text = wrapper.text()

      expect(text).toContain('multiple-character wildcard')
      expect(text).not.toContain('are not allowed in searches')
      expect(text).not.toContain('underscore character acts as a single character wildcard')
      expect(text).not.toContain('percent character acts as a multiple character wildcard')
    })
  })

  // ── Asset filter chips ────────────────────────────────────────────────────────

  describe('Asset filter chips', () => {
    it('shows a proper title (not the raw key) for a non-curated asset column', async () => {
      const wrapper = mountTable()
      const structureStore = useNodeStructureStore()
      structureStore.queryFilter = {
        ...structureStore.queryFilter,
        assetFilters: [{ column: 'city', value: 'Pittsboro' }]
      }
      await nextTick()

      expect(wrapper.text()).toContain('Asset: City: Pittsboro')
    })
  })
})
