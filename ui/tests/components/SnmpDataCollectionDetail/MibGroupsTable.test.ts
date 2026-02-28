import MibGroupsTable from '@/components/SnmpDataCollectionDetail/MibGroupsTable.vue'
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
import { nextTick, ref } from 'vue'

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
    it('renders correctly', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('calls fetchMibGroups on mount', () => {
      expect(store.fetchMibGroups).toHaveBeenCalled()
    })

    it('renders search input', () => {
      const searchInput = wrapper.find('[data-test="search-input"]')
      expect(searchInput.exists()).toBe(true)
    })

    it('renders refresh button', () => {
      const refreshButton = wrapper.find('[data-test="refresh-button"]')
      expect(refreshButton.exists()).toBe(true)
    })

    it('renders add mib group button', () => {
      const addButton = wrapper.find('[data-test="add-mib-group-button"]')
      expect(addButton.exists()).toBe(true)
    })

    it('add mib group button has correct text', () => {
      const addButton = wrapper.find('[data-test="add-mib-group-button"]')
      expect(addButton.text()).toBe('Add MIB Group')
    })

    it('renders within mib-groups-table-container', () => {
      expect(wrapper.find('.mib-groups-table-container').exists()).toBe(true)
    })

    it('renders header with section-left and section-right', () => {
      expect(wrapper.find('.header .section-left').exists()).toBe(true)
      expect(wrapper.find('.header .section-right').exists()).toBe(true)
    })

    it('renders search input with correct hint text', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('hint')).toBe('Search by Name or Interface Type')
    })

    it('renders search input with type search', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('type')).toBe('search')
    })

    it('renders add button inside section-right .add container', () => {
      const sectionRight = wrapper.find('.section-right')
      expect(sectionRight.find('.add').exists()).toBe(true)
      expect(sectionRight.find('[data-test="add-mib-group-button"]').exists()).toBe(true)
    })

    it('renders DeleteConfirmationDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })
  })

  describe('Add MIB Group Button', () => {
    it('should call openMibGroupCreationDrawer with Create mode when clicked', async () => {
      const addButton = wrapper.find('[data-test="add-mib-group-button"]')
      await addButton.trigger('click')
      await wrapper.vm.$nextTick()

      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('should be clickable multiple times', async () => {
      const addButton = wrapper.find('[data-test="add-mib-group-button"]')
      await addButton.trigger('click')
      await addButton.trigger('click')
      await addButton.trigger('click')

      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledTimes(3)
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

    it('displays EmptyList component with correct message when no data', async () => {
      store.mibGroups = []
      await wrapper.vm.$nextTick()

      const emptyMessage = wrapper.text()
      expect(emptyMessage).toContain('No MIB Groups found.')
    })

    it('should still show header with add button, search and refresh when empty', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-mib-group-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('renders EmptyList component when no data', async () => {
      store.mibGroups = []
      await wrapper.vm.$nextTick()

      const emptyList = wrapper.findComponent({ name: 'EmptyList' })
      expect(emptyList.exists()).toBe(true)
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
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('hint')).toBe('Search by Name or Interface Type')
    })

    it('renders search input with correct label', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('label')).toBe('Search')
    })

    it('renders search input with type search', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
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

    it('should call openMibGroupCreationDrawer with Edit mode when clicked', async () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      await editButton.trigger('click')
      await wrapper.vm.$nextTick()

      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledWith(mockMibGroup, CreateEditMode.Edit)
    })

    it('should render multiple edit buttons for multiple rows', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons.length).toBe(2)
    })

    it('should have correct title attribute', async () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toContain('Edit')
    })

    it('should call openMibGroupCreationDrawer with correct mibGroup for second row', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      await editButtons[1].trigger('click')
      await wrapper.vm.$nextTick()

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

    it('expanded content td has colspan="5"', async () => {
      const expandedRow = wrapper.find('.expanded-content')
      const td = expandedRow.find('td')
      expect(td.attributes('colspan')).toBe('5')
    })

    it('renders .description element for mib group names', async () => {
      const description = wrapper.find('.expanded-content .description')
      expect(description.exists()).toBe(true)
      expect(description.text()).toContain('ifTable, ifXTable')
    })

    it('renders mib group names as comma-separated text', async () => {
      const description = wrapper.find('.expanded-content .description')
      // Uses text interpolation {{ }}, not v-html
      expect(description.text()).toBe('ifTable, ifXTable')
    })

    it('renders h5 headers for section titles', async () => {
      const h5s = wrapper.findAll('.expanded-content h5')
      expect(h5s.length).toBeGreaterThanOrEqual(1)
      expect(h5s[0].text()).toBe('Mib Group Names')
    })

    it('renders h6 header for object numbering', async () => {
      const h6s = wrapper.findAll('.expanded-content h6')
      expect(h6s.length).toBeGreaterThanOrEqual(1)
      expect(h6s[0].text()).toBe('Object 1')
    })

    it('renders strong tags for field labels', async () => {
      const strongs = wrapper.findAll('.expanded-content strong')
      const strongTexts = strongs.map((s) => s.text())
      expect(strongTexts).toContain('Alias:')
      expect(strongTexts).toContain('OID:')
      expect(strongTexts).toContain('Instance:')
      expect(strongTexts).toContain('Data Type:')
    })

    it('renders Mib Objects h5 when objects exist', async () => {
      const h5s = wrapper.findAll('.expanded-content h5')
      const mibObjectsH5 = h5s.filter((h) => h.text() === 'Mib Objects:')
      expect(mibObjectsH5.length).toBe(1)
    })

    it('does not render Mib Objects h5 when no objects', async () => {
      store.mibGroups = [disabledMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(disabledMibGroup.id)
      await wrapper.vm.$nextTick()

      const h5s = wrapper.findAll('.expanded-content h5')
      const mibObjectsH5 = h5s.filter((h) => h.text() === 'Mib Objects:')
      expect(mibObjectsH5.length).toBe(0)
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

    it('handles sort reset to default when value is none', () => {
      wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
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

    it('pagination has correct page sizes options', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('pageSizes')).toEqual([10, 20, 30])
    })

    it('has data-test attribute on FeatherPagination', () => {
      const pagination = wrapper.find('[data-test="FeatherPagination"]')
      expect(pagination.exists()).toBe(true)
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

    it('renders action container for each row', async () => {
      const actionContainers = wrapper.findAll('.action-container')
      expect(actionContainers.length).toBe(1)
    })

    it('renders multiple dropdowns for multiple rows', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })

    it('renders more actions button in each row', async () => {
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

  describe('Columns Configuration', () => {
    it('has correct number of columns defined', () => {
      expect(wrapper.vm.columns.length).toBe(3)
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

    it('renders 3 sort headers for columns', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBe(3)
    })

    it('renders Actions column header (non-sortable)', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const ths = wrapper.findAll('th')
      const actionsHeader = ths.filter((th) => th.text() === 'Actions')
      expect(actionsHeader.length).toBe(1)
    })
  })

  describe('Status Display', () => {
    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('displays "$expectedText" with class "$expectedClass" when enabled=$enabled', async ({ enabled, expectedText, expectedClass }) => {
      const mibGroup = { ...mockMibGroup, enabled }
      store.mibGroups = [mibGroup]
      await wrapper.vm.$nextTick()

      const statusTag = wrapper.find('[data-test="status-tag"]')
      expect(statusTag.exists()).toBe(true)
      expect(statusTag.text()).toBe(expectedText)
      expect(statusTag.classes()).toContain(expectedClass)
    })

    it('renders FeatherChip for status in each row', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      await wrapper.vm.$nextTick()

      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips.length).toBe(2)
    })

    it('renders mixed enabled/disabled states', async () => {
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

  describe('Multiple MIB Groups with Mixed States', () => {
    it('renders mib groups with different enabled states', async () => {
      store.mibGroups = [mockMibGroup, disabledMibGroup]
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Enabled')
      expect(wrapper.text()).toContain('Disabled')
    })

    it('renders mib groups with different interface types', async () => {
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

    it('handles zero id value', async () => {
      const zeroIdMibGroup: SnmpCollectionMibGroup = {
        ...mockMibGroup,
        id: 0,
        name: 'zeroIdMibGroup'
      }

      store.mibGroups = [zeroIdMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
      expect(rows[0].text()).toContain('zeroIdMibGroup')
    })

    it('handles negative id value', async () => {
      const negativeIdMibGroup: SnmpCollectionMibGroup = {
        ...mockMibGroup,
        id: -1,
        name: 'negativeIdMibGroup'
      }

      store.mibGroups = [negativeIdMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
      expect(rows[0].text()).toContain('negativeIdMibGroup')
    })

    it('handles large pagination total counts', async () => {
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 100000 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(100000)
    })

    it('handles preserved expanded state when data updates', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      // Expand first row
      wrapper.vm.toggleExpand(mockMibGroup.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)

      // Add a new mib group
      const newMibGroup: SnmpCollectionMibGroup = {
        ...mockMibGroup,
        id: 4,
        name: 'newMibGroup'
      }
      store.mibGroups = [mockMibGroup, mockMibGroup2, newMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 3 }
      await wrapper.vm.$nextTick()

      // Expanded state should be preserved
      expect(wrapper.vm.expandedRows).toContain(mockMibGroup.id)
    })

    it('handles multiple expanded rows with some removed from data', async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      // Expand both rows
      wrapper.vm.toggleExpand(mockMibGroup.id)
      wrapper.vm.toggleExpand(mockMibGroup2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows.length).toBe(2)

      // Remove second item from data
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Only one expanded content should show
      const expandedContent = wrapper.findAll('.expanded-content')
      expect(expandedContent.length).toBe(1)
    })

    it('handles mib group with special characters in mibGroupNames', async () => {
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

    it('handles mib group with single mibGroupName', async () => {
      const singleNameMibGroup = { ...mockMibGroup, mibGroupNames: ['onlyTable'] }
      store.mibGroups = [singleNameMibGroup]
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(singleNameMibGroup.id)
      await wrapper.vm.$nextTick()

      const description = wrapper.find('.expanded-content .description')
      expect(description.text()).toBe('onlyTable')
    })

    it('does not call search before debounce completes when input cleared', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await searchInput.setValue('')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      // Only the empty string call should go through (debounce cancels previous)
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('')
    })
  })

  describe('Content Data Display', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()
    })

    it('renders mib group name in row', async () => {
      expect(wrapper.text()).toContain('mib2-interfaces')
    })

    it('renders ifType in row', async () => {
      expect(wrapper.text()).toContain('all')
    })

    it('renders .tag container for status chip', async () => {
      const tag = wrapper.find('.tag')
      expect(tag.exists()).toBe(true)
    })

    it('renders search-container within section-left', async () => {
      expect(wrapper.find('.section-left .search-container').exists()).toBe(true)
    })

    it('renders refresh container within section-left', async () => {
      expect(wrapper.find('.section-left .refresh').exists()).toBe(true)
    })

    it('renders data in .container wrapper', async () => {
      expect(wrapper.find('.container').exists()).toBe(true)
      expect(wrapper.find('.container .data-table').exists()).toBe(true)
    })

    it('renders pagination inside .alerts-pagination', async () => {
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(true)
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

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('test search')
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

  describe('Parametrized Tests - Interface Type Values', () => {
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
  })

  describe('Parametrized Tests - MIB Group Data Variations', () => {
    it.each([
      { field: 'name', value: 'Very Long MIB Group Name That Might Overflow' },
      { field: 'ifType', value: 'custom-if-type' },
      { field: 'mibGroupNames', value: ['table1', 'table2', 'table3', 'table4'] }
    ])('renders mib group with $field as "$value"', async ({ field, value }) => {
      const mibGroup = { ...mockMibGroup, [field]: value }
      store.mibGroups = [mibGroup]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })
  })

  describe('Accessibility', () => {
    it('table has aria-label with correct value', async () => {
      store.mibGroups = [mockMibGroup]
      await wrapper.vm.$nextTick()

      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBe('Events Table')
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
      expect(editButton.attributes('title')).toContain('Edit')
    })
  })

  describe('Expanded Content with Multiple Objects', () => {
    it.each([{ objectCount: 1 }, { objectCount: 2 }, { objectCount: 5 }])(
      'displays $objectCount mib objects correctly',
      async ({ objectCount }) => {
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
      }
    )
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

    it('has openDeleteMibGroupDialog method available', async () => {
      // Verify the component has the openDeleteMibGroupDialog method
      expect(typeof wrapper.vm.openDeleteMibGroupDialog).toBe('function')
    })

    it('opens delete dialog via openDeleteMibGroupDialog method', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup?.id).toBe(mockMibGroup.id)
      expect(wrapper.vm.selectedMibGroup?.name).toBe(mockMibGroup.name)
    })

    it('sets selectedMibGroup correctly for different mib groups', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedMibGroup?.id).toBe(mockMibGroup2.id)
      expect(wrapper.vm.selectedMibGroup?.name).toBe(mockMibGroup2.name)
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

    it('renders DeleteConfirmationDialog component', async () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
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

    it('closes dialog after successful deletion', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })

    it('fetches mib groups after successful deletion', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const fetchSpy = vi.spyOn(store, 'fetchMibGroups')

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(fetchSpy).toHaveBeenCalled()
    })

    it('does not call deleteMibGroups when type does not match', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'wrong-type')
      await flushPromises()

      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteMibGroups when selected id does not match', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: 999, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteMibGroups when selected name does not match', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: 'wrong-name' }, 'mib-group')
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

    it('does not call deleteMibGroups when selected is null', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup(null, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteMibGroups when selected id is missing', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteMibGroup({ name: mockMibGroup.name } as any, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
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
        hideSnackbar: vi.fn(),
        isDisplayed: ref(false),
        isCentered: ref(false),
        hasError: ref(false),
        message: ref(''),
        setTimeout: ref(5000)
      })

      // Remount wrapper to pick up mocked snackbar
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

  describe('Delete Button in Dropdown', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown with delete option for each row', async () => {
      // FeatherDropdown components should be rendered for each row
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })

  })

  describe('Delete Confirmation Dialog Events', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
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

    it('handles confirm event from DeleteConfirmationDialog', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteMibGroupsSpy = vi.spyOn(snmpDataCollectionService, 'deleteMibGroups').mockResolvedValue(true)

      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).toHaveBeenCalledWith(1, [mockMibGroup.id])
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
      const longNameMibGroup = {
        ...mockMibGroup,
        id: 11,
        name: longName
      }
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
      expect(wrapper.vm.selectedMibGroup?.name).toBe(mockMibGroup.name)
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
})


