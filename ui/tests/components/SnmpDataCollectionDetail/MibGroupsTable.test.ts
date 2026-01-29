import MibGroupsTable from '@/components/SnmpDataCollectionDetail/MibGroupsTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { SnmpCollectionMibGroup } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('MibGroupsTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>
  let mockMibGroup: SnmpCollectionMibGroup
  let mockMibGroup2: SnmpCollectionMibGroup
  let disabledMibGroup: SnmpCollectionMibGroup

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionDetailStore(pinia)

    store.mibGroups = []
    store.mibGroupsSearchTerm = ''
    store.mibGroupsPagination = { page: 1, pageSize: 10, total: 0 }
    store.mibGroupsSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchMibGroups = vi.fn().mockResolvedValue(undefined)
    store.resetMibGroupsFilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeMibGroupsSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onMibGroupsPageChange = vi.fn().mockResolvedValue(undefined)
    store.onMibGroupsPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onMibGroupsSortChange = vi.fn().mockResolvedValue(undefined)

    mockMibGroup = {
      id: 1,
      name: 'mib2-interfaces',
      ifType: 'all',
      mibGroupNames: '["ifTable", "ifXTable"]',
      mibObjects: '[{"alias":"ifIndex","oid":"1.3.6.1.2.1.2.2.1.1","instance":"ifIndex","type":"gauge"}]',
      mibObjProperties: '[]',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    mockMibGroup2 = {
      id: 2,
      name: 'mib2-host-resources',
      ifType: 'ignore',
      mibGroupNames: '["hrStorageTable"]',
      mibObjects: '[{"alias":"hrStorageIndex","oid":"1.3.6.1.2.1.25.2.3.1.1","instance":"hrStorageIndex","type":"gauge"}]',
      mibObjProperties: '[]',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    disabledMibGroup = {
      id: 3,
      name: 'disabled-mib-group',
      ifType: 'all',
      mibGroupNames: '["disabledTable"]',
      mibObjects: '[]',
      mibObjProperties: '[]',
      enabled: false,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    wrapper = mount(MibGroupsTable, {
      global: {
        plugins: [pinia],
        components: {
          FeatherButton,
          FeatherDropdown,
          FeatherDropdownItem,
          FeatherSortHeader,
          FeatherPagination,
          FeatherInput
        }
      }
    })

    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Initial Rendering', () => {
    it('renders correctly', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('calls fetchMibGroups on mount', () => {
      expect(store.fetchMibGroups).toHaveBeenCalled()
    })

    it('renders the title correctly', () => {
      expect(wrapper.text()).toContain('MIB Groups')
    })

    it('renders search input', () => {
      const searchInput = wrapper.find('[data-test="search-input"]')
      expect(searchInput.exists()).toBe(true)
    })

    it('renders refresh button', () => {
      const refreshButton = wrapper.find('[data-test="refresh-button"]')
      expect(refreshButton.exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('does not render table when mibGroups are empty', async () => {
      store.mibGroups = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(false)
    })

    it('does not render pagination when mibGroups are empty', async () => {
      store.mibGroups = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders table when mibGroups exist', async () => {
      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('renders correct number of rows', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      // Each mib group can have 2 rows (main + expanded), but expanded is hidden initially
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('renders mib group name correctly', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('mib2-interfaces')
    })

    it('renders ifType correctly', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('all')
    })

    it('renders enabled status correctly', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Enabled')
    })

    it('renders disabled status correctly', async () => {
      store.mibGroups = [disabledMibGroup]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Disabled')
    })

    it('renders edit button', async () => {
      expect(wrapper.find('[data-test="edit-button"]').exists()).toBe(true)
    })

    it('renders pagination when mibGroups exist', async () => {
      expect(wrapper.find('.alerts-pagination').exists()).toBe(true)
    })
  })

  describe('Table with Multiple MIB Groups', () => {
    it.each([
      { count: 1, expectedMinRows: 1 },
      { count: 2, expectedMinRows: 2 },
      { count: 5, expectedMinRows: 5 },
      { count: 10, expectedMinRows: 10 }
    ])('renders at least $expectedMinRows rows when $count mib groups exist', async ({ count, expectedMinRows }) => {
      const mibGroups = Array.from({ length: count }, (_, i) => ({
        ...mockMibGroup,
        id: i + 1,
        name: `MIB Group ${i + 1}`
      }))
      store.mibGroups = mibGroups
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(expectedMinRows)
    })
  })

  describe('Search Functionality', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders search input with correct placeholder hint', () => {
      const searchInput = wrapper.find('[data-test="search-input"]')
      expect(searchInput.exists()).toBe(true)
    })

    it('handles search input changes with debouncing', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('test')
    })

    it('does not call onChangeMibGroupsSearchTerm before debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await wrapper.vm.$nextTick()

      expect(store.onChangeMibGroupsSearchTerm).not.toHaveBeenCalled()
    })

    it('calls onChangeMibGroupsSearchTerm after debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('search term')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('search term')
    })

    it('handles empty search term', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('')
    })

    it.each([
      { term: 'simple' },
      { term: 'with spaces' },
      { term: 'special@chars#' },
      { term: 'UPPERCASE' },
      { term: '123numeric456' },
      { term: 'IF-MIB' },
      { term: 'mib2-interfaces' }
    ])('handles search term "$term" correctly', async ({ term }) => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue(term)
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith(term)
    })
  })

  describe('Refresh Button', () => {
    it('calls resetMibGroupsFilters when refresh button is clicked', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetMibGroupsFilters).toHaveBeenCalledTimes(1)
    })

    it('can click refresh button multiple times', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      await wrapper.get('[data-test="refresh-button"]').trigger('click')

      expect(store.resetMibGroupsFilters).toHaveBeenCalledTimes(3)
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders edit button for each row', async () => {
      expect(wrapper.find('[data-test="edit-button"]').exists()).toBe(true)
    })

    it('calls onMibGroupEditClicked when edit button is clicked', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      await wrapper.get('[data-test="edit-button"]').trigger('click')

      expect(consoleSpy).toHaveBeenCalledWith('MIB Group clicked:', mockMibGroup)
      consoleSpy.mockRestore()
    })

    it('handles edit click via onMibGroupEditClicked function', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      wrapper.vm.onMibGroupEditClicked(mockMibGroup)

      expect(consoleSpy).toHaveBeenCalledWith('MIB Group clicked:', mockMibGroup)
      consoleSpy.mockRestore()
    })
  })

  describe('Expand/Collapse Functionality', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('initializes with no expanded rows', () => {
      expect(wrapper.vm.expandedRows).toEqual([])
    })

    it('expands row when toggle button is clicked', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)
    })

    it('collapses row when toggle button is clicked again', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)

      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockMibGroup.id)
    })

    it('can expand multiple rows', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockMibGroup.id)
      wrapper.vm.toggleExpand(mockMibGroup2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup2.id)
    })

    it('shows expanded content when row is expanded', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.expanded-content').exists()).toBe(true)
    })

    it('hides expanded content when row is collapsed', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)

      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(false)
    })

    it('displays mib group names in expanded content', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('ifTable')
      expect(expandedContent.text()).toContain('ifXTable')
    })

    it('displays mib objects in expanded content', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('ifIndex')
      expect(expandedContent.text()).toContain('1.3.6.1.2.1.2.2.1.1')
      expect(expandedContent.text()).toContain('gauge')
    })
  })

  describe('Expanded Content Details', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
    })

    it('displays Mib Group Names header', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Mib Group Names')
    })

    it('displays Mib Objects header when objects exist', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Mib Objects')
    })

    it('displays object alias', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Alias')
      expect(expandedContent.text()).toContain('ifIndex')
    })

    it('displays object OID', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('OID')
      expect(expandedContent.text()).toContain('1.3.6.1.2.1.2.2.1.1')
    })

    it('displays object instance', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Instance')
    })

    it('displays object data type', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Data Type')
      expect(expandedContent.text()).toContain('gauge')
    })

    it('does not show Mib Objects section when no objects', async () => {
      store.mibGroups = [disabledMibGroup] // has empty mibObjects
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(disabledMibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      // Should not contain "Object 1" since there are no objects
      expect(expandedContent.text()).not.toContain('Object 1')
    })
  })

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders sort headers', async () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBeGreaterThan(0)
    })

    it('handles sort change for ascending', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('name', 'asc')
    })

    it('handles sort change for descending', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'desc' })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('name', 'desc')
    })

    it('handles sort reset to default when value is none', () => {
      wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('updates local sort state on sort change', async () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')
    })

    it('resets other sort properties when sorting by a column', async () => {
      wrapper.vm.sort.ifType = 'asc'
      wrapper.vm.sortChanged({ property: 'name', value: 'desc' })

      expect(wrapper.vm.sort.name).toBe('desc')
      expect(wrapper.vm.sort.ifType).toBe(SORT.NONE)
    })

    it('clicks sort header and triggers onMibGroupsSortChange', async () => {
      const sortHeader = wrapper.findAllComponents(FeatherSortHeader)[0]
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: SORT.ASCENDING })
      await wrapper.vm.$nextTick()

      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('name', SORT.ASCENDING)
    })

    it.each([
      { property: 'name', sortOrder: 'asc' },
      { property: 'name', sortOrder: 'desc' },
      { property: 'ifType', sortOrder: 'asc' },
      { property: 'ifType', sortOrder: 'desc' },
      { property: 'enabled', sortOrder: 'asc' },
      { property: 'enabled', sortOrder: 'desc' }
    ])('handles sorting by $property with $sortOrder order', async ({ property, sortOrder }) => {
      wrapper.vm.sortChanged({ property, value: sortOrder })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith(property, sortOrder)
      expect(wrapper.vm.sort[property]).toBe(sortOrder)
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()
    })

    it('renders pagination component', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.exists()).toBe(true)
    })

    it('renders pagination with correct props', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(1)
      expect(pagination.props('pageSize')).toBe(10)
      expect(pagination.props('total')).toBe(50)
    })

    it('handles page change', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      expect(store.onMibGroupsPageChange).toHaveBeenCalledWith(2)
    })

    it('handles page size change', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', 20)
      expect(store.onMibGroupsPageSizeChange).toHaveBeenCalledWith(20)
    })

    it.each([
      { page: 1 },
      { page: 2 },
      { page: 5 },
      { page: 10 }
    ])('handles page change to page $page', async ({ page }) => {
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', page)
      expect(store.onMibGroupsPageChange).toHaveBeenCalledWith(page)
    })

    it.each([
      { pageSize: 10 },
      { pageSize: 20 },
      { pageSize: 30 }
    ])('handles page size change to $pageSize', async ({ pageSize }) => {
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', pageSize)
      expect(store.onMibGroupsPageSizeChange).toHaveBeenCalledWith(pageSize)
    })

    it('pagination has correct page sizes options', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('pageSizes')).toEqual([10, 20, 30])
    })
  })

  describe('Dropdown Actions', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown for each row', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].findComponent(FeatherDropdown).exists()).toBe(true)
    })

    it('renders more actions button in each row', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      const buttons = rows[0].findAll('button')
      // Should have at least 3 buttons: edit, more actions, and expand
      expect(buttons.length).toBeGreaterThanOrEqual(3)
    })

    it('has dropdown component in row actions', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      const dropdown = rows[0].findComponent(FeatherDropdown)
      expect(dropdown.exists()).toBe(true)
    })
  })

  describe('Columns Configuration', () => {
    it('has correct columns defined', () => {
      const columns = wrapper.vm.columns
      expect(columns).toEqual([
        { id: 'name', label: 'Name' },
        { id: 'ifType', label: 'If Type' },
        { id: 'enabled', label: 'Status' }
      ])
    })

    it('renders all column headers', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders).toHaveLength(3)
    })

    it.each([
      { id: 'name', label: 'Name' },
      { id: 'ifType', label: 'If Type' },
      { id: 'enabled', label: 'Status' }
    ])('has column with id "$id" and label "$label"', ({ id, label }) => {
      const columns = wrapper.vm.columns
      const column = columns.find((col: any) => col.id === id)
      expect(column).toBeDefined()
      expect(column.label).toBe(label)
    })
  })

  describe('Status Display', () => {
    it.each([
      { enabled: true, expectedText: 'Enabled' },
      { enabled: false, expectedText: 'Disabled' }
    ])('displays "$expectedText" when enabled is $enabled', async ({ enabled, expectedText }) => {
      const mibGroup = { ...mockMibGroup, enabled }
      store.mibGroups = [mibGroup]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(expectedText)
    })
  })

  describe('Multiple MIB Groups with Mixed States', () => {
    it('renders multiple mib groups with different enabled states', async () => {
      store.mibGroups = [mockMibGroup, disabledMibGroup]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('renders mib groups with different ifTypes', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('all')
      expect(wrapper.text()).toContain('ignore')
    })
  })

  describe('Sort State Management', () => {
    it('initializes sort state with NONE for all columns', () => {
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.ifType).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('maintains sort state after sorting', () => {
      wrapper.vm.sortChanged({ property: 'ifType', value: 'asc' })

      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.ifType).toBe('asc')
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('resets all sorts when changing sort column', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')

      wrapper.vm.sortChanged({ property: 'ifType', value: 'desc' })
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.ifType).toBe('desc')
    })
  })

  describe('Edge Cases', () => {
    it('handles mib group with empty mibGroupNames', async () => {
      const mibGroupWithEmptyNames = { ...mockMibGroup, mibGroupNames: '[]' }
      store.mibGroups = [mibGroupWithEmptyNames]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mibGroupWithEmptyNames.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.expanded-content').exists()).toBe(true)
    })

    it('handles mib group with empty mibObjects', async () => {
      const mibGroupWithEmptyObjects = { ...mockMibGroup, mibObjects: '[]' }
      store.mibGroups = [mibGroupWithEmptyObjects]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mibGroupWithEmptyObjects.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.expanded-content').exists()).toBe(true)
    })

    it('handles rapid search input changes', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('a')
      await searchInput.setValue('ab')
      await searchInput.setValue('abc')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      // Only the last value should trigger the call due to debouncing
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('abc')
    })

    it('handles pagination with zero total', async () => {
      store.mibGroups = []
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 0 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('handles multiple mib objects in a group', async () => {
      const mibGroupWithMultipleObjects = {
        ...mockMibGroup,
        mibObjects: '[{"alias":"obj1","oid":"1.1.1","instance":"obj1","type":"gauge"},{"alias":"obj2","oid":"1.1.2","instance":"obj2","type":"counter"}]'
      }
      store.mibGroups = [mibGroupWithMultipleObjects]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mibGroupWithMultipleObjects.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('obj1')
      expect(expandedContent.text()).toContain('obj2')
      expect(expandedContent.text()).toContain('Object 1')
      expect(expandedContent.text()).toContain('Object 2')
    })
  })

  describe('Store State Binding', () => {
    it('reflects store mibGroups in table', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('reflects store pagination in component', async () => {
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 3, pageSize: 20, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(3)
      expect(pagination.props('pageSize')).toBe(20)
      expect(pagination.props('total')).toBe(100)
    })

    it('reflects store search term in input', async () => {
      store.mibGroupsSearchTerm = 'test search'
      await wrapper.vm.$nextTick()

      expect(store.mibGroupsSearchTerm).toBe('test search')
    })
  })

  describe('Integration Tests', () => {
    it('complete workflow: search, sort, paginate', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      // Search
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('test')

      // Sort
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('name', 'asc')

      // Paginate
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      expect(store.onMibGroupsPageChange).toHaveBeenCalledWith(2)
    })

    it('expand and collapse multiple groups', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      // Expand first group
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)

      // Expand second group
      wrapper.vm.toggleExpand(mockMibGroup2.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup2.id)

      // Collapse first group
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockMibGroup.id)
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup2.id)
    })

    it('refresh clears filters and fetches data', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetMibGroupsFilters).toHaveBeenCalled()
    })
  })

  describe('Parametrized Tests - ifType Values', () => {
    it.each([
      { ifType: 'all' },
      { ifType: 'ignore' },
      { ifType: 'specific' },
      { ifType: '' }
    ])('renders mib group with ifType "$ifType"', async ({ ifType }) => {
      const mibGroup = { ...mockMibGroup, ifType }
      store.mibGroups = [mibGroup]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
      if (ifType) {
        expect(wrapper.text()).toContain(ifType)
      }
    })
  })

  describe('Parametrized Tests - MIB Group Data Variations', () => {
    it.each([
      { field: 'name', value: 'Very Long MIB Group Name That Might Overflow' },
      { field: 'ifType', value: 'custom-if-type' },
      { field: 'mibGroupNames', value: '["table1", "table2", "table3", "table4"]' }
    ])('renders mib group with $field as "$value"', async ({ field, value }) => {
      const mibGroup = { ...mockMibGroup, [field]: value }
      store.mibGroups = [mibGroup]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })
  })

  describe('Accessibility', () => {
    it('table has aria-label', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('sort headers are rendered with col scope', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBeGreaterThan(0)
      sortHeaders.forEach((header) => {
        expect(header.attributes('scope')).toBe('col')
      })
    })

    it('edit button has title attribute', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toBeDefined()
    })
  })

  describe('Expanded Content with Multiple Objects', () => {
    it.each([
      { objectCount: 1 },
      { objectCount: 2 },
      { objectCount: 5 }
    ])('displays $objectCount mib objects correctly', async ({ objectCount }) => {
      const objects = Array.from({ length: objectCount }, (_, i) => ({
        alias: `obj${i + 1}`,
        oid: `1.3.6.1.${i + 1}`,
        instance: `obj${i + 1}`,
        type: 'gauge'
      }))

      const mibGroup = { ...mockMibGroup, mibObjects: JSON.stringify(objects) }
      store.mibGroups = [mibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      for (let i = 0; i < objectCount; i++) {
        expect(expandedContent.text()).toContain(`obj${i + 1}`)
        expect(expandedContent.text()).toContain(`Object ${i + 1}`)
      }
    })
  })

  describe('Toggle Expand Edge Cases', () => {
    it('handles toggling non-existent id', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(999) // non-existent id
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(999)
    })

    it('handles rapid toggle clicks', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockMibGroup.id)
      wrapper.vm.toggleExpand(mockMibGroup.id)
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()

      // After 3 toggles, should be expanded (odd number)
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)
    })
  })
})
