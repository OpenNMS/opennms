// ui/tests/components/Nodes/NodesTable.test.ts
import NodesTable from '@/components/Nodes/NodesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { FilterTypeEnum } from '@/types'
import { defaultColumns } from '@/components/Nodes/utils'
import { SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
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
    })
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
  MessageDialog: { name: 'MessageDialog', template: '<div></div>', props: ['visible', 'relative', 'maxHeight', 'maxWidth', 'title'] },
  EmptyList: { name: 'EmptyList', template: '<div class="empty-list-stub"></div>', props: ['content'] }
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
})
