// ui/tests/components/Nodes/NodesTable.test.ts
import NodesTable from '@/components/Nodes/NodesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { FilterTypeEnum } from '@/types'
import { defaultColumns } from '@/components/Nodes/utils'
import { SORT } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
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

vi.mock('@/components/Nodes/hooks/useNodeQuery', () => {
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
      buildUpdatedNodeStructureQueryParameters: vi.fn().mockImplementation(params => params),
      getExtendedSearchValues: vi.fn().mockReturnValue([]),
      getDefaultNodeQueryFilter: makeDefaultFilter,
      getDefaultNodeQueryForeignSourceParams: () => ({ foreignId: '', foreignSource: '', foreignSourceId: '' }),
      getDefaultNodeQuerySnmpParams: () => ({ snmpIfAlias: '', snmpIfDescription: '', snmpIfIndex: '', snmpIfName: '', snmpIfType: '' }),
      getDefaultNodeQuerySysParams: () => ({ sysContact: '', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '' }),
      buildNodeQueryFilterFromQueryString: vi.fn().mockReturnValue(makeDefaultFilter()),
      queryStringHasTrackedValues: vi.fn().mockReturnValue(false)
    }),
    sanitizeSearchTerm: (s?: string) => (s || '').replace(/[,;]/g, ' ')
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
  OnmsMessageDialog: { name: 'OnmsMessageDialog', template: '<div></div>', props: ['visible', 'relative', 'maxHeight', 'maxWidth', 'title'] },
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
      expect(footer.text().replace(/\s+/g, ' ').trim()).toBe('1 Node, 1 Interface on this page')
    })

    it('pluralizes node/interface counts when greater than one', async () => {
      const wrapper = mountTable()
      const ns = useNodeStore()
      const structure = useNodeStructureStore()

      ns.nodes = [{ id: '1' }, { id: '2' }] as any
      ns.totalCount = 2
      ns.nodeToIpInterfaceMap = new Map([
        ['1', [{ id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' }]],
        ['2', [{ id: 'ip2', ipAddress: '10.0.0.2', isManaged: 'M' }]]
      ]) as any
      await nextTick()

      structure.setShowInterfaces(true)
      await nextTick()

      const footer = wrapper.find('[data-test="interfaces-footer"]')
      expect(footer.text().replace(/\s+/g, ' ').trim()).toBe('2 Nodes, 2 Interfaces on this page')
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
})
