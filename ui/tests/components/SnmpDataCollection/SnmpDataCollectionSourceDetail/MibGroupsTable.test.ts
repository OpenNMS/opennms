import MibGroupsTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/MibGroupsTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionMibGroup } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

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
    store.openMibGroupCreationDrawer = vi.fn()

    mockMibGroup = {
      id: 1,
      name: 'mib2-interfaces',
      ifType: 'all',
      mibGroupNames: ['ifTable', 'ifXTable'],
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
      mibGroupNames: ['hrStorageTable'],
      mibObjects:
        '[{"alias":"hrStorageIndex","oid":"1.3.6.1.2.1.25.2.3.1.1","instance":"hrStorageIndex","type":"gauge"}]',
      mibObjProperties: '[]',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    disabledMibGroup = {
      id: 3,
      name: 'disabled-mib-group',
      ifType: 'all',
      mibGroupNames: ['disabledTable'],
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
          FeatherInput,
          FeatherChip
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
    it('renders correctly with mib-groups-table-container', () => {
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('.mib-groups-table-container').exists()).toBe(true)
    })

    it('renders header with search, refresh, and add button', () => {
      expect(wrapper.find('.header .section-left').exists()).toBe(true)
      expect(wrapper.find('.header .section-right').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-mib-group-button"]').exists()).toBe(true)
    })

    it('renders add mib group button with correct text', () => {
      const addButton = wrapper.find('[data-test="add-mib-group-button"]')
      expect(addButton.text()).toBe('Add MIB Group')
    })

    it('renders DeleteConfirmationDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('renders MibGroupCreationDrawer component', () => {
      const drawer = wrapper.findComponent({ name: 'MibGroupCreationDrawer' })
      expect(drawer.exists()).toBe(true)
    })
  })

  describe('Add MIB Group Button', () => {
    it('calls openMibGroupCreationDrawer with Create mode when clicked', async () => {
      const addButton = wrapper.find('[data-test="add-mib-group-button"]')
      await addButton.trigger('click')

      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('can be clicked multiple times', async () => {
      const addButton = wrapper.find('[data-test="add-mib-group-button"]')
      await addButton.trigger('click')
      await addButton.trigger('click')
      await addButton.trigger('click')

      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledTimes(3)
    })
  })

  describe('Empty State', () => {
    it('does not render table or pagination when mibGroups are empty', async () => {
      store.mibGroups = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(false)
      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('displays EmptyList component with correct message when no data', async () => {
      store.mibGroups = []
      await wrapper.vm.$nextTick()

      const emptyList = wrapper.findComponent({ name: 'EmptyList' })
      expect(emptyList.exists()).toBe(true)
      expect(wrapper.text()).toContain('No MIB Groups found.')
    })

    it('still shows header elements when empty', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-mib-group-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('shows table then hides when data is cleared', async () => {
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(true)

      store.mibGroups = []
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(false)
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders table when mibGroups exist', () => {
      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('renders correct number of rows', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('renders mib group name and ifType in row', () => {
      expect(wrapper.text()).toContain('mib2-interfaces')
      expect(wrapper.text()).toContain('all')
    })

    it.each([
      { count: 1, expectedMinRows: 1 },
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

    it('renders search input with correct props', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('hint')).toBe('Search by Name or Interface Type')
      expect(searchInput.props('label')).toBe('Search')
      expect(searchInput.props('type')).toBe('search')
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

    it.each([
      { term: '' },
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

    it('debounces rapid input changes - only last value triggers call', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('a')
      await searchInput.setValue('ab')
      await searchInput.setValue('abc')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('abc')
    })

    it('reflects store search term in input', async () => {
      store.mibGroupsSearchTerm = 'test search'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('test search')
    })
  })

  describe('Refresh Button', () => {
    it('calls resetMibGroupsFilters when clicked', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetMibGroupsFilters).toHaveBeenCalledTimes(1)
    })

    it('can be clicked multiple times', async () => {
      const refreshButton = wrapper.get('[data-test="refresh-button"]')
      await refreshButton.trigger('click')
      await refreshButton.trigger('click')
      await refreshButton.trigger('click')

      expect(store.resetMibGroupsFilters).toHaveBeenCalledTimes(3)
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders edit button for each row with correct title', () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.exists()).toBe(true)
      expect(editButton.attributes('title')).toContain('Edit')
    })

    it('calls openMibGroupCreationDrawer with Edit mode when clicked', async () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      await editButton.trigger('click')

      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledWith(mockMibGroup, CreateEditMode.Edit)
    })

    it('renders multiple edit buttons for multiple rows and calls with correct mibGroup', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons.length).toBe(2)

      await editButtons[1].trigger('click')
      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledWith(mockMibGroup2, CreateEditMode.Edit)
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

    it('toggles row expansion when toggle function is called', async () => {
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

    it('displays mib group names and mib objects in expanded content', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('ifTable')
      expect(expandedContent.text()).toContain('ifXTable')
      expect(expandedContent.text()).toContain('ifIndex')
      expect(expandedContent.text()).toContain('1.3.6.1.2.1.2.2.1.1')
      expect(expandedContent.text()).toContain('gauge')
    })

    it('handles rapid toggle clicks correctly', async () => {
      wrapper.vm.toggleExpand(mockMibGroup.id)
      wrapper.vm.toggleExpand(mockMibGroup.id)
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()

      // After 3 toggles, should be expanded (odd number)
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)
    })

    it('handles toggling non-existent id', async () => {
      wrapper.vm.toggleExpand(999)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(999)
    })
  })

  describe('Expanded Content Details', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
    })

    it('displays proper headers and structure', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('MIB Group Names')
      expect(expandedContent.text()).toContain('MIB Objects')

      const td = expandedContent.find('td')
      expect(td.attributes('colspan')).toBe('5')
    })

    it('displays mib group names as comma-separated text', async () => {
      const description = wrapper.find('.expanded-content .description')
      expect(description.exists()).toBe(true)
      expect(description.text()).toBe('ifTable, ifXTable')
    })

    it('displays object details with correct labels', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Object 1')
      expect(expandedContent.text()).toContain('Alias:')
      expect(expandedContent.text()).toContain('OID:')
      expect(expandedContent.text()).toContain('Instance:')
      expect(expandedContent.text()).toContain('Data Type:')
    })

    it('does not show MIB Objects section when no objects', async () => {
      store.mibGroups = [disabledMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(disabledMibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).not.toContain('Object 1')
      expect(expandedContent.text()).not.toContain('MIB Objects:')
    })
  })

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders 3 sort headers for columns', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBe(3)
    })

    it('initializes sort state with NONE for all columns', () => {
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.ifType).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('handles sort reset to default when value is none', () => {
      wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
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

    it('resets all sorts when changing sort column', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')

      wrapper.vm.sortChanged({ property: 'ifType', value: 'desc' })
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.ifType).toBe('desc')
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()
    })

    it('renders pagination component with correct props', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(1)
      expect(pagination.props('pageSize')).toBe(10)
      expect(pagination.props('total')).toBe(50)
      expect(pagination.props('pageSizes')).toEqual([10, 20, 30])
    })

    it('has data-test attribute on FeatherPagination', () => {
      expect(wrapper.find('[data-test="FeatherPagination"]').exists()).toBe(true)
    })

    it.each([{ page: 1 }, { page: 2 }, { page: 5 }, { page: 10 }])(
      'handles page change to page $page',
      async ({ page }) => {
        const pagination = wrapper.getComponent(FeatherPagination)
        await pagination.vm.$emit('update:modelValue', page)
        expect(store.onMibGroupsPageChange).toHaveBeenCalledWith(page)
      }
    )

    it.each([{ pageSize: 10 }, { pageSize: 20 }, { pageSize: 30 }])(
      'handles page size change to $pageSize',
      async ({ pageSize }) => {
        const pagination = wrapper.getComponent(FeatherPagination)
        await pagination.vm.$emit('update:pageSize', pageSize)
        expect(store.onMibGroupsPageSizeChange).toHaveBeenCalledWith(pageSize)
      }
    )

    it('reflects store pagination in component', async () => {
      store.mibGroupsPagination = { page: 3, pageSize: 20, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(3)
      expect(pagination.props('pageSize')).toBe(20)
      expect(pagination.props('total')).toBe(100)
    })
  })

  describe('Dropdown Actions', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown and action container for each row', async () => {
      expect(wrapper.find('.action-container').exists()).toBe(true)
      expect(wrapper.findComponent(FeatherDropdown).exists()).toBe(true)
    })

    it('renders multiple dropdowns for multiple rows', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })

    it('renders dropdown with action button in each row', async () => {
      // FeatherDropdown is rendered in action container
      const dropdown = wrapper.findComponent(FeatherDropdown)
      expect(dropdown.exists()).toBe(true)

      // The dropdown trigger button should exist with More Options icon
      const triggerButton = dropdown.find('button')
      expect(triggerButton.exists()).toBe(true)
      expect(triggerButton.attributes('aria-haspopup')).toBe('true')
    })
  })

  describe('Columns Configuration', () => {
    it('has correct columns defined', () => {
      expect(wrapper.vm.columns.length).toBe(3)

      const columnIds = wrapper.vm.columns.map((c: any) => c.id)
      expect(columnIds).toContain('name')
      expect(columnIds).toContain('ifType')
      expect(columnIds).toContain('enabled')
    })

    it.each([
      { id: 'name', label: 'Name' },
      { id: 'ifType', label: 'Interface Type' },
      { id: 'enabled', label: 'Status' }
    ])('has column "$label" with id "$id"', ({ id, label }) => {
      const col = wrapper.vm.columns.find((c: any) => c.id === id)
      expect(col).toBeDefined()
      expect(col.label).toBe(label)
    })

    it('renders Actions column header (non-sortable)', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const ths = wrapper.findAll('th')
      const actionsHeader = ths.filter(th => th.text() === 'Actions')
      expect(actionsHeader.length).toBe(1)
    })
  })

  describe('Status Display', () => {
    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])(
      'displays "$expectedText" with class "$expectedClass" when enabled=$enabled',
      async ({ enabled, expectedText, expectedClass }) => {
        const mibGroup = { ...mockMibGroup, enabled }
        store.mibGroups = [mibGroup]
        await wrapper.vm.$nextTick()

        const statusTag = wrapper.find('[data-test="status-tag"]')
        expect(statusTag.exists()).toBe(true)
        expect(statusTag.text()).toBe(expectedText)
        expect(statusTag.classes()).toContain(expectedClass)
      }
    )

    it('renders mixed enabled/disabled states correctly', async () => {
      store.mibGroups = [mockMibGroup, disabledMibGroup]
      await wrapper.vm.$nextTick()

      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips.length).toBe(2)
      expect(chips[0].text()).toBe('Enabled')
      expect(chips[0].classes()).toContain('enabled-tag')
      expect(chips[1].text()).toBe('Disabled')
      expect(chips[1].classes()).toContain('disabled-tag')
    })
  })

  describe('Edge Cases', () => {
    it('handles mib group with empty mibGroupNames', async () => {
      const mibGroupWithEmptyNames = { ...mockMibGroup, mibGroupNames: [] }
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

    it('handles pagination with zero total', async () => {
      store.mibGroups = []
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 0 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('handles multiple mib objects in a group', async () => {
      const mibGroupWithMultipleObjects = {
        ...mockMibGroup,
        mibObjects:
          '[{"alias":"obj1","oid":"1.1.1","instance":"obj1","type":"gauge"},{"alias":"obj2","oid":"1.1.2","instance":"obj2","type":"counter"}]'
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

    it('handles unicode characters in mib group fields', async () => {
      const unicodeMibGroup: SnmpCollectionMibGroup = {
        ...mockMibGroup,
        name: 'MIB-グループ-テスト',
        ifType: 'Étiquette_日本語'
      }

      store.mibGroups = [unicodeMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('MIB-グループ-テスト')
      expect(rows[0].text()).toContain('Étiquette_日本語')
    })

    it('handles zero and negative id values', async () => {
      const zeroIdMibGroup = { ...mockMibGroup, id: 0, name: 'zeroIdMibGroup' }
      const negativeIdMibGroup = { ...mockMibGroup, id: -1, name: 'negativeIdMibGroup' }

      store.mibGroups = [zeroIdMibGroup, negativeIdMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
      expect(wrapper.text()).toContain('zeroIdMibGroup')
      expect(wrapper.text()).toContain('negativeIdMibGroup')
    })

    it('handles large pagination total counts', async () => {
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 100000 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(100000)
    })

    it('preserves expanded state when data updates', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)

      const newMibGroup: SnmpCollectionMibGroup = { ...mockMibGroup, id: 4, name: 'newMibGroup' }
      store.mibGroups = [mockMibGroup, mockMibGroup2, newMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 3 }
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)
    })

    it('handles special characters in mibGroupNames', async () => {
      const specialNamesMibGroup = {
        ...mockMibGroup,
        mibGroupNames: ['table-name_v2.0', 'table@special#chars', 'table[with]brackets']
      }
      store.mibGroups = [specialNamesMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(specialNamesMibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('table-name_v2.0')
      expect(expandedContent.text()).toContain('table@special#chars')
      expect(expandedContent.text()).toContain('table[with]brackets')
    })

    it('handles very long mibGroupNames list', async () => {
      const manyNamesMibGroup = {
        ...mockMibGroup,
        mibGroupNames: Array.from({ length: 50 }, (_, i) => `table${i + 1}`)
      }
      store.mibGroups = [manyNamesMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(manyNamesMibGroup.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('table1')
      expect(expandedContent.text()).toContain('table50')
    })

    it('handles single mibGroupName', async () => {
      const singleNameMibGroup = { ...mockMibGroup, mibGroupNames: ['onlyTable'] }
      store.mibGroups = [singleNameMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(singleNameMibGroup.id)
      await wrapper.vm.$nextTick()

      const description = wrapper.find('.expanded-content .description')
      expect(description.text()).toBe('onlyTable')
    })
  })

  describe('Interface Type Variations', () => {
    it.each([{ ifType: 'all' }, { ifType: 'ignore' }, { ifType: 'specific' }, { ifType: '' }])(
      'renders mib group with Interface Type "$ifType"',
      async ({ ifType }) => {
        const mibGroup = { ...mockMibGroup, ifType }
        store.mibGroups = [mibGroup]
        await wrapper.vm.$nextTick()

        expect(wrapper.find('.data-table').exists()).toBe(true)
        if (ifType) {
          expect(wrapper.text()).toContain(ifType)
        }
      }
    )

    it('renders mib groups with different interface types', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('all')
      expect(wrapper.text()).toContain('ignore')
    })
  })

  describe('Accessibility', () => {
    it('table has aria-label attribute', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('sort headers have col scope', async () => {
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
      expect(editButton.attributes('title')).toContain('Edit')
    })
  })

  describe('Delete MIB Group Dialog', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('initializes with delete dialog hidden', () => {
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('opens delete dialog and sets selectedMibGroup correctly', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup?.id).toBe(mockMibGroup.id)
      expect(wrapper.vm.selectedMibGroup?.name).toBe(mockMibGroup.name)
    })

    it('closes delete dialog and clears selection', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteMibGroupDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('passes correct props to DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')?.id).toBe(mockMibGroup.id)
      expect(dialog.props('selected')?.name).toBe(mockMibGroup.name)
      expect(dialog.props('type')).toBe('mib-group')
    })

    it('handles opening dialog with null', async () => {
      wrapper.vm.openDeleteMibGroupDialog(null)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('handles close event from DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('close')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })
  })

  describe('Delete MIB Group Action', () => {
    let deleteMibGroupsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      deleteMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'deleteMibGroups')

      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('calls deleteMibGroups service on successful delete', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).toHaveBeenCalledWith(1, [mockMibGroup.id])
    })

    it('closes dialog and fetches data after successful deletion', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const fetchSpy = vi.spyOn(store, 'fetchMibGroups')

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
      expect(fetchSpy).toHaveBeenCalled()
    })

    it.each([
      { desc: 'type does not match', params: { id: 1, name: 'mib2-interfaces' }, type: 'wrong-type' },
      { desc: 'selected id does not match', params: { id: 999, name: 'mib2-interfaces' }, type: 'mib-group' },
      { desc: 'selected name does not match', params: { id: 1, name: 'wrong-name' }, type: 'mib-group' }
    ])('does not call deleteMibGroups when $desc', async ({ params, type }) => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup(params, type)
      await flushPromises()

      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteMibGroups when selectedCollectionSource is missing', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteMibGroups when selected is null or missing id', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup(null, 'mib-group')
      await flushPromises()
      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()

      await wrapper.vm.deleteMibGroup({ name: mockMibGroup.name } as any, 'mib-group')
      await flushPromises()
      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('handles confirm event from DeleteConfirmationDialog', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).toHaveBeenCalledWith(1, [mockMibGroup.id])
    })
  })

  describe('Delete MIB Group Error Handling', () => {
    let deleteMibGroupsSpy: any
    let showSnackBarSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      deleteMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'deleteMibGroups')

      const useSnackbar = await import('@/composables/useSnackbar')
      showSnackBarSpy = vi.fn()
      vi.spyOn(useSnackbar, 'default').mockReturnValue({
        showSnackBar: showSnackBarSpy,
        hideSnackbar: vi.fn()
      })

      const pinia = createTestingPinia({
        createSpy: vi.fn,
        stubActions: false
      })
      store = useSnmpDataCollectionDetailStore(pinia)
      store.mibGroups = [mockMibGroup]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      store.fetchMibGroups = vi.fn().mockResolvedValue(undefined)

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
    })

    it('shows success snackbar when deletion succeeds', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `MIB Group '${mockMibGroup.name}' deleted successfully.`
      })
    })

    it('shows error snackbar when deletion fails', async () => {
      deleteMibGroupsSpy.mockResolvedValue(false)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete MIB Group '${mockMibGroup.name}'.`,
        error: true
      })
    })

    it('shows error snackbar when validation fails', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: 999, name: 'wrong-name' }, 'mib-group')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete MIB Group \'wrong-name\'.',
        error: true
      })
    })

    it('shows error with empty name when selected is null', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup(null, 'mib-group')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete MIB Group \'\'.',
        error: true
      })
    })

    it('keeps delete dialog open when deletion fails', async () => {
      deleteMibGroupsSpy.mockResolvedValue(false)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup).not.toBeNull()
    })
  })

  describe('Delete with Edge Cases', () => {
    beforeEach(async () => {
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
    })

    it('handles delete for disabled mib group', async () => {
      store.mibGroups = [disabledMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteMibGroupDialog(disabledMibGroup)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedMibGroup?.id).toBe(disabledMibGroup.id)
      expect(wrapper.vm.selectedMibGroup?.name).toBe(disabledMibGroup.name)
    })

    it('handles delete for mib group with special characters in name', async () => {
      const specialMibGroup = {
        ...mockMibGroup,
        id: 10,
        name: 'MIB<>Group&"Special\'Chars'
      }
      store.mibGroups = [specialMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteMibGroupDialog(specialMibGroup)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedMibGroup?.name).toBe('MIB<>Group&"Special\'Chars')
    })

    it('handles delete for mib group with very long name', async () => {
      const longName = 'A'.repeat(200)
      const longNameMibGroup = { ...mockMibGroup, id: 11, name: longName }
      store.mibGroups = [longNameMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteMibGroupDialog(longNameMibGroup)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedMibGroup?.name).toBe(longName)
    })

    it('handles rapid open/close of delete dialog', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      wrapper.vm.closeDeleteMibGroupDialog()
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      wrapper.vm.closeDeleteMibGroupDialog()
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup?.id).toBe(mockMibGroup.id)
    })

    it('handles switching selected mib group without closing dialog', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedMibGroup?.id).toBe(mockMibGroup2.id)
      expect(wrapper.vm.selectedMibGroup?.name).toBe(mockMibGroup2.name)
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

      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)

      wrapper.vm.toggleExpand(mockMibGroup2.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup2.id)

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

  describe('Change Status Dialog - State Management', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('change status dialog is initially hidden', () => {
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
    })

    it('selected mib group is initially null', () => {
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('opens change status dialog and sets selected mib group', () => {
      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup).toEqual(mockMibGroup)
    })

    it('closes change status dialog and clears selection', () => {
      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      wrapper.vm.closeChangeStatusDialog()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('opens and closes change status dialog multiple times', () => {
      for (let i = 0; i < 3; i++) {
        wrapper.vm.openChangeStatusDialog(mockMibGroup)
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
        wrapper.vm.closeChangeStatusDialog()
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      }
    })

    it('opens change status dialog with null', () => {
      wrapper.vm.openChangeStatusDialog(null)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('renders SnmpDataCollectionChangeStatusDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('passes correct props to SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('type')).toBe('mib-group')
      expect(dialog.props('selected')).toEqual(mockMibGroup)
    })

    it('passes correct status prop based on enabled state - enabled mib group shows Disable', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup, enabled: true })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')
    })

    it('passes correct status prop based on enabled state - disabled mib group shows Enable', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup, enabled: false })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Enable')
    })

    it('passes visible=false when change status dialog is closed', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(false)
    })

    it('handles close event from SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('close')

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })
  })

  describe('Change MIB Group Status - Successful Status Change', () => {
    let enableDisableSnmpMibGroupsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpMibGroups')

      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('calls enableDisableSnmpMibGroups service when disabling', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup, enabled: true })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, false, [mockMibGroup.id])
    })

    it('calls enableDisableSnmpMibGroups service when enabling', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup, enabled: false })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, true, [mockMibGroup.id])
    })

    it('closes dialog and fetches data after successful status change', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
      expect(store.fetchMibGroups).toHaveBeenCalled()
    })

    it('uses correct collection source id for status change service call', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = { id: 99, name: 'Custom Source' } as any

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(99, false, [mockMibGroup.id])
    })

    it('handles confirm event from SnmpDataCollectionChangeStatusDialog', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('confirm', { id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, false, [mockMibGroup.id])
    })
  })

  describe('Change MIB Group Status - Failed Status Change', () => {
    let enableDisableSnmpMibGroupsSpy: any
    let showSnackBarSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpMibGroups')

      const useSnackbar = (await import('@/composables/useSnackbar')).default
      showSnackBarSpy = vi.fn()
      vi.spyOn({ useSnackbar }, 'useSnackbar').mockReturnValue({
        showSnackBar: showSnackBarSpy
      } as any)

      store.mibGroups = [mockMibGroup]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('does not close dialog when status change fails', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(false)

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup).toEqual(mockMibGroup)
    })

    it('does not fetch mib groups when status change fails', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(false)
      store.fetchMibGroups = vi.fn()

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(store.fetchMibGroups).not.toHaveBeenCalled()
    })
  })

  describe('Change MIB Group Status - Validation Failures', () => {
    let enableDisableSnmpMibGroupsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpMibGroups')

      store.mibGroups = [mockMibGroup]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it.each([
      { desc: 'type does not match', params: { id: 1, name: 'mib2-interfaces' }, type: 'wrong-type' },
      { desc: 'selected id does not match', params: { id: 999, name: 'mib2-interfaces' }, type: 'mib-group' },
      { desc: 'selected name does not match', params: { id: 1, name: 'wrong-name' }, type: 'mib-group' }
    ])('does not call enableDisableSnmpMibGroups when $desc', async ({ params, type }) => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus(params, type)
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call enableDisableSnmpMibGroups when selectedCollectionSource is missing', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call enableDisableSnmpMibGroups when selected is null', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus(null, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call enableDisableSnmpMibGroups when selectedMibGroup is null (dialog not opened)', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).not.toHaveBeenCalled()
    })

    it.each([{ type: 'source' }, { type: 'system-def' }, { type: 'resource-type' }, { type: 'unknown' }, { type: '' }])(
      'rejects status change when type is "$type" instead of "mib-group"',
      async ({ type }) => {
        enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

        wrapper.vm.openChangeStatusDialog(mockMibGroup)
        await wrapper.vm.$nextTick()

        await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, type)
        await flushPromises()

        expect(enableDisableSnmpMibGroupsSpy).not.toHaveBeenCalled()
      }
    )
  })

  describe('Change Status with Edge Cases', () => {
    let enableDisableSnmpMibGroupsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpMibGroups')

      store.mibGroups = [mockMibGroup, mockMibGroup2, disabledMibGroup]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('handles status change for disabled mib group', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(disabledMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: disabledMibGroup.id, name: disabledMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, true, [disabledMibGroup.id])
    })

    it('switches selected mib group without closing dialog', () => {
      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      expect(wrapper.vm.selectedMibGroup).toEqual(mockMibGroup)

      wrapper.vm.openChangeStatusDialog(mockMibGroup2)
      expect(wrapper.vm.selectedMibGroup).toEqual(mockMibGroup2)
    })

    it('handles rapid open/close of change status dialog', () => {
      for (let i = 0; i < 5; i++) {
        wrapper.vm.openChangeStatusDialog(mockMibGroup)
        wrapper.vm.closeChangeStatusDialog()
      }
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('handles sequential status change operations correctly', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)

      // First operation - disable
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup, enabled: true })
      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()
      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, false, [mockMibGroup.id])

      enableDisableSnmpMibGroupsSpy.mockClear()

      // Second operation - enable
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup2, enabled: false })
      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup2.id, name: mockMibGroup2.name }, 'mib-group')
      await flushPromises()
      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, true, [mockMibGroup2.id])
    })

    it('handles mib group with special characters in name for status change', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)
      const specialMibGroup = { ...mockMibGroup, name: 'Test <MIB> & "Quotes"' }

      wrapper.vm.openChangeStatusDialog(specialMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: specialMibGroup.id, name: specialMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalled()
    })

    it('handles status change when selected mib group id is 0 (falsy)', async () => {
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)
      const zeroIdGroup = { ...mockMibGroup, id: 0 }

      wrapper.vm.openChangeStatusDialog(zeroIdGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeMibGroupStatus({ id: 0, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(enableDisableSnmpMibGroupsSpy).not.toHaveBeenCalled()
    })
  })

  describe('Multiple MIB Groups with Mixed States - Change Status', () => {
    beforeEach(async () => {
      store.mibGroups = [
        { ...mockMibGroup, enabled: true },
        { ...mockMibGroup2, enabled: false },
        { ...disabledMibGroup, enabled: false }
      ]
      await wrapper.vm.$nextTick()
    })

    it('passes correct status prop for each mib group based on enabled state', async () => {
      // Test enabled mib group shows Disable
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup, enabled: true })
      await wrapper.vm.$nextTick()
      let dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')

      // Test disabled mib group shows Enable
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup2, enabled: false })
      await wrapper.vm.$nextTick()
      dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Enable')
    })

    it('handles mixed mib groups status changes correctly', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const enableDisableSnmpMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpMibGroups')
      enableDisableSnmpMibGroupsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any

      // Disable an enabled mib group
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup, enabled: true })
      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()
      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, false, [mockMibGroup.id])

      enableDisableSnmpMibGroupsSpy.mockClear()

      // Enable a disabled mib group
      wrapper.vm.openChangeStatusDialog({ ...mockMibGroup2, enabled: false })
      await wrapper.vm.changeMibGroupStatus({ id: mockMibGroup2.id, name: mockMibGroup2.name }, 'mib-group')
      await flushPromises()
      expect(enableDisableSnmpMibGroupsSpy).toHaveBeenCalledWith(1, true, [mockMibGroup2.id])
    })
  })
})
