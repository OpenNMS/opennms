import ResourceTypesTable from '@/components/SnmpDataCollectionDetail/ResourceTypesTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionResourceType } from '@/types/snmpDataCollection'
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

describe('ResourceTypesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>
  let mockResourceType: SnmpCollectionResourceType
  let mockResourceType2: SnmpCollectionResourceType
  let disabledResourceType: SnmpCollectionResourceType

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionDetailStore(pinia)

    store.resourceTypes = []
    store.resourceTypesSearchTerm = ''
    store.resourceTypesPagination = { page: 1, pageSize: 10, total: 0 }
    store.resourceTypesSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchResourceTypes = vi.fn().mockResolvedValue(undefined)
    store.resetResourceTypesFilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeResourceTypesSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onResourceTypesPageChange = vi.fn().mockResolvedValue(undefined)
    store.onResourceTypesPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onResourceTypesSortChange = vi.fn().mockResolvedValue(undefined)
    store.openResourceTypeCreationDrawer = vi.fn()

    mockResourceType = {
      id: 1,
      name: 'interfaceSnmp',
      label: 'SNMP Interface Data',
      resourceLabel: '${ifDescr}',
      persistenceSelectorStrategy: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
      persistenceSelectorParams: '{}',
      storageStrategy: 'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy',
      storageStrategyParams: '{"siblingColumnName":"ifDescr"}',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    mockResourceType2 = {
      id: 2,
      name: 'hrStorageIndex',
      label: 'Host Resources Storage',
      resourceLabel: '${hrStorageDescr}',
      persistenceSelectorStrategy: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
      persistenceSelectorParams: '{}',
      storageStrategy: 'org.opennms.netmgt.dao.support.IndexStorageStrategy',
      storageStrategyParams: '{}',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    disabledResourceType = {
      id: 3,
      name: 'disabledResourceType',
      label: 'Disabled Resource',
      resourceLabel: '${index}',
      persistenceSelectorStrategy: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
      persistenceSelectorParams: '{}',
      storageStrategy: 'org.opennms.netmgt.dao.support.IndexStorageStrategy',
      storageStrategyParams: '{}',
      enabled: false,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    wrapper = mount(ResourceTypesTable, {
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
    it('renders correctly with resource-types-table-container', () => {
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('.resource-types-table-container').exists()).toBe(true)
    })

    it('renders header with search, refresh, and add button', () => {
      expect(wrapper.find('.header .section-left').exists()).toBe(true)
      expect(wrapper.find('.header .section-right').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-resource-type-button"]').exists()).toBe(true)
    })

    it('renders add resource type button with correct text', () => {
      const addButton = wrapper.find('[data-test="add-resource-type-button"]')
      expect(addButton.text()).toBe('Add Resource Type')
    })

    it('renders search input with correct props', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('label')).toBe('Search')
      expect(searchInput.props('type')).toBe('search')
      expect(searchInput.props('hint')).toBe('Search by Name or Label')
    })

    it('renders DeleteConfirmationDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('renders ResourceTypeCreationDrawer component', () => {
      const drawer = wrapper.findComponent({ name: 'ResourceTypeCreationDrawer' })
      expect(drawer.exists()).toBe(true)
    })

    it('has container section for table and pagination', () => {
      expect(wrapper.find('.container').exists()).toBe(true)
    })
  })

  describe('Add Resource Type Button', () => {
    it('calls openResourceTypeCreationDrawer with Create mode when clicked', async () => {
      const addButton = wrapper.find('[data-test="add-resource-type-button"]')
      await addButton.trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('can be clicked multiple times', async () => {
      const addButton = wrapper.find('[data-test="add-resource-type-button"]')
      await addButton.trigger('click')
      await addButton.trigger('click')
      await addButton.trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledTimes(3)
    })
  })

  describe('Empty State', () => {
    it('does not render table or pagination when resourceTypes are empty', async () => {
      store.resourceTypes = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(false)
      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('displays EmptyList component with correct message when no data', async () => {
      store.resourceTypes = []
      await wrapper.vm.$nextTick()

      const emptyList = wrapper.findComponent({ name: 'EmptyList' })
      expect(emptyList.exists()).toBe(true)
      expect(emptyList.props('content')).toEqual({ msg: 'No Resource Types found.' })
    })

    it('still shows header elements when empty', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-resource-type-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('shows table then hides when data is cleared', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(true)

      store.resourceTypes = []
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(false)
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('renders table with correct aria-label', () => {
      const table = wrapper.find('.data-table')
      expect(table.exists()).toBe(true)
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('renders correct number of rows', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('renders resource type data in row', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockResourceType.name)
      expect(rows[0].text()).toContain(mockResourceType.label)
      expect(rows[0].text()).toContain(mockResourceType.resourceLabel)
      expect(rows[0].text()).toContain('Enabled')
    })

    it('renders edit button and dropdown for each row', () => {
      expect(wrapper.findAll('[data-test="edit-button"]').length).toBe(2)
      expect(wrapper.findAllComponents(FeatherDropdown).length).toBe(2)
    })

    it('renders FeatherChip for status in each row', () => {
      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips.length).toBe(2)
    })

    it.each([
      { count: 1, expectedMinRows: 1 },
      { count: 5, expectedMinRows: 5 },
      { count: 10, expectedMinRows: 10 }
    ])(
      'renders at least $expectedMinRows rows when $count resource types exist',
      async ({ count, expectedMinRows }) => {
        const resourceTypes = Array.from({ length: count }, (_, i) => ({
          ...mockResourceType,
          id: i + 1,
          name: `Resource Type ${i + 1}`
        }))
        store.resourceTypes = resourceTypes
        await wrapper.vm.$nextTick()

        const rows = wrapper.findAll('transition-group-stub tr')
        expect(rows.length).toBeGreaterThanOrEqual(expectedMinRows)
      }
    )
  })

  describe('Search Functionality', () => {
    it('renders search input with correct props', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('hint')).toBe('Search by Name or Label')
      expect(searchInput.props('label')).toBe('Search')
      expect(searchInput.props('type')).toBe('search')
    })

    it('does not call onChangeResourceTypesSearchTerm before debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).not.toHaveBeenCalled()
    })

    it('calls onChangeResourceTypesSearchTerm after debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('search term')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('search term')
    })

    it.each([
      { term: '' },
      { term: 'simple' },
      { term: 'Host Resources' },
      { term: 'special@chars#' },
      { term: 'UPPERCASE' },
      { term: '資源類型' }
    ])('handles search term "$term" correctly', async ({ term }) => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue(term)
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith(term)
    })

    it('debounces rapid input changes - only last value triggers call', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('a')
      await searchInput.setValue('ab')
      await searchInput.setValue('abc')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('abc')
    })

    it('reflects store search term in input', async () => {
      store.resourceTypesSearchTerm = 'test search'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('test search')
    })
  })

  describe('Refresh Button', () => {
    it('calls resetResourceTypesFilters when clicked', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetResourceTypesFilters).toHaveBeenCalledTimes(1)
    })

    it('can be clicked multiple times', async () => {
      const refreshButton = wrapper.get('[data-test="refresh-button"]')
      await refreshButton.trigger('click')
      await refreshButton.trigger('click')
      await refreshButton.trigger('click')

      expect(store.resetResourceTypesFilters).toHaveBeenCalledTimes(3)
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('renders edit button for each row with correct title', () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.exists()).toBe(true)
      expect(editButton.attributes('title')).toContain('Edit')
    })

    it('calls openResourceTypeCreationDrawer with Edit mode when clicked', async () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      await editButton.trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType, CreateEditMode.Edit)
    })

    it('renders multiple edit buttons and calls with correct resource type', async () => {
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons.length).toBe(2)

      await editButtons[1].trigger('click')
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType2, CreateEditMode.Edit)
    })
  })

  describe('Expand/Collapse Functionality', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('initializes with no expanded rows', () => {
      expect(wrapper.vm.expandedRows).toEqual([])
    })

    it('toggles row expansion when toggle function is called', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockResourceType.id)
    })

    it('can expand multiple rows', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
      expect(wrapper.vm.expandedRows).toContain(mockResourceType2.id)
      expect(wrapper.findAll('.expanded-content').length).toBe(2)
    })

    it('shows expanded content when row is expanded', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)
    })

    it('hides expanded content when row is collapsed', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(false)
    })

    it('displays storage and persistence strategies in expanded content', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Storage Strategy')
      expect(expandedContent.text()).toContain('Persistence Selector Strategy')
      expect(expandedContent.text()).toContain(mockResourceType.storageStrategy)
      expect(expandedContent.text()).toContain(mockResourceType.persistenceSelectorStrategy)
    })

    it('handles rapid toggle clicks correctly', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      // After 5 toggles, should be expanded (odd number)
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
    })

    it('handles toggling non-existent id', async () => {
      wrapper.vm.toggleExpand(999)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(999)
    })
  })

  describe('Expanded Content Details', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
    })

    it('displays proper headers and structure', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Storage Strategy')
      expect(expandedContent.text()).toContain('Persistence Selector Strategy')

      const td = expandedContent.find('td')
      expect(td.attributes('colspan')).toBe('5')
    })

    it('displays strategy values with description class', async () => {
      const descriptions = wrapper.findAll('.expanded-content .description')
      expect(descriptions.length).toBe(2)
      expect(descriptions[0].text()).toBe(mockResourceType.storageStrategy)
      expect(descriptions[1].text()).toBe(mockResourceType.persistenceSelectorStrategy)
    })

    it('displays h6 headers for section titles', async () => {
      const h6s = wrapper.findAll('.expanded-content h6')
      expect(h6s.length).toBe(2)
      expect(h6s[0].text()).toBe('Storage Strategy:')
      expect(h6s[1].text()).toBe('Persistence Selector Strategy:')
    })
  })

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    it('renders 4 sort headers for columns', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBe(4)
    })

    it('initializes sort state with NONE for all columns', () => {
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.label).toBe(SORT.NONE)
      expect(wrapper.vm.sort.resourceLabel).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('handles sort reset to default when value is none', () => {
      wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })
      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it.each([
      { property: 'name', sortOrder: 'asc' },
      { property: 'name', sortOrder: 'desc' },
      { property: 'label', sortOrder: 'asc' },
      { property: 'label', sortOrder: 'desc' },
      { property: 'resourceLabel', sortOrder: 'asc' },
      { property: 'resourceLabel', sortOrder: 'desc' },
      { property: 'enabled', sortOrder: 'asc' },
      { property: 'enabled', sortOrder: 'desc' }
    ])('handles sorting by $property with $sortOrder order', async ({ property, sortOrder }) => {
      wrapper.vm.sortChanged({ property, value: sortOrder })
      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith(property, sortOrder)
      expect(wrapper.vm.sort[property]).toBe(sortOrder)
    })

    it('resets all sorts when changing sort column', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')

      wrapper.vm.sortChanged({ property: 'label', value: 'desc' })
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.label).toBe('desc')
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 50 }
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
        expect(store.onResourceTypesPageChange).toHaveBeenCalledWith(page)
      }
    )

    it.each([{ pageSize: 10 }, { pageSize: 20 }, { pageSize: 30 }])(
      'handles page size change to $pageSize',
      async ({ pageSize }) => {
        const pagination = wrapper.getComponent(FeatherPagination)
        await pagination.vm.$emit('update:pageSize', pageSize)
        expect(store.onResourceTypesPageSizeChange).toHaveBeenCalledWith(pageSize)
      }
    )

    it('reflects store pagination in component', async () => {
      store.resourceTypesPagination = { page: 3, pageSize: 20, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(3)
      expect(pagination.props('pageSize')).toBe(20)
      expect(pagination.props('total')).toBe(100)
    })
  })

  describe('Dropdown Actions', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown and action container for each row', async () => {
      expect(wrapper.find('.action-container').exists()).toBe(true)
      expect(wrapper.findComponent(FeatherDropdown).exists()).toBe(true)
    })

    it('renders multiple dropdowns for multiple rows', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      await wrapper.vm.$nextTick()

      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })

    it('renders dropdown with action button in each row', async () => {
      const dropdown = wrapper.findComponent(FeatherDropdown)
      expect(dropdown.exists()).toBe(true)

      const triggerButton = dropdown.find('button')
      expect(triggerButton.exists()).toBe(true)
      expect(triggerButton.attributes('aria-haspopup')).toBe('true')
    })
  })

  describe('Columns Configuration', () => {
    it('has correct columns defined', () => {
      expect(wrapper.vm.columns.length).toBe(4)

      const columnIds = wrapper.vm.columns.map((c: any) => c.id)
      expect(columnIds).toContain('name')
      expect(columnIds).toContain('label')
      expect(columnIds).toContain('resourceLabel')
      expect(columnIds).toContain('enabled')
    })

    it.each([
      { id: 'name', label: 'Name' },
      { id: 'label', label: 'Label' },
      { id: 'resourceLabel', label: 'Resource Label' },
      { id: 'enabled', label: 'Status' }
    ])('has column "$label" with id "$id"', ({ id, label }) => {
      const col = wrapper.vm.columns.find((c: any) => c.id === id)
      expect(col).toBeDefined()
      expect(col.label).toBe(label)
    })

    it('renders Actions column header (non-sortable)', async () => {
      store.resourceTypes = [mockResourceType]
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
    ])(
      'displays "$expectedText" with class "$expectedClass" when enabled=$enabled',
      async ({ enabled, expectedText, expectedClass }) => {
        const resourceType = { ...mockResourceType, enabled }
        store.resourceTypes = [resourceType]
        store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
        await wrapper.vm.$nextTick()

        const statusTag = wrapper.find('[data-test="status-tag"]')
        expect(statusTag.exists()).toBe(true)
        expect(statusTag.text()).toBe(expectedText)
        expect(statusTag.classes()).toContain(expectedClass)
      }
    )

    it('renders mixed enabled/disabled states correctly', async () => {
      store.resourceTypes = [mockResourceType, disabledResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips.length).toBe(2)
      expect(chips[0].classes()).toContain('enabled-tag')
      expect(chips[1].classes()).toContain('disabled-tag')
    })
  })

  describe('Edge Cases', () => {
    it('handles resource type with empty string fields', async () => {
      const emptyResourceType: SnmpCollectionResourceType = {
        id: 1,
        name: '',
        label: '',
        resourceLabel: '',
        persistenceSelectorStrategy: '',
        persistenceSelectorParams: '',
        storageStrategy: '',
        storageStrategyParams: '',
        enabled: true,
        collectionSourceId: 1,
        collectionSourceName: ''
      }

      store.resourceTypes = [emptyResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('transition-group-stub tr').length).toBeGreaterThanOrEqual(1)
    })

    it('handles special characters in fields', async () => {
      const specialResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        name: 'test-resource_type.v2<>&"',
        label: 'Test <Resource> Type & More'
      }

      store.resourceTypes = [specialResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const row = wrapper.findAll('transition-group-stub tr')[0]
      expect(row.text()).toContain('test-resource_type.v2<>&"')
    })

    it('handles unicode characters in fields', async () => {
      const unicodeResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        name: '資源類型-テスト',
        label: 'Étiquette accentuée 日本語',
        resourceLabel: '${リソース_äöü}'
      }

      store.resourceTypes = [unicodeResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const row = wrapper.findAll('transition-group-stub tr')[0]
      expect(row.text()).toContain('資源類型-テスト')
      expect(row.text()).toContain('Étiquette accentuée 日本語')
    })

    it('handles pagination with zero total', async () => {
      store.resourceTypes = []
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 0 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('handles large pagination total counts', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 100000 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(100000)
    })

    it('preserves expanded state when data updates', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)

      const newResourceType: SnmpCollectionResourceType = { ...mockResourceType, id: 10, name: 'newItem' }
      store.resourceTypes = [mockResourceType, mockResourceType2, newResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 3 }
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
    })

    it('hides expanded content when data is cleared', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      store.resourceTypes = []
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })

    it('displays long strategy strings in expanded content', async () => {
      const longStrategyType: SnmpCollectionResourceType = {
        ...mockResourceType,
        storageStrategy:
          'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy.VeryLongClassName.WithMultiple.Packages',
        persistenceSelectorStrategy:
          'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy.AnotherVeryLongClassName'
      }

      store.resourceTypes = [longStrategyType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(longStrategyType.id)
      await wrapper.vm.$nextTick()

      const expanded = wrapper.find('.expanded-content')
      expect(expanded.text()).toContain(longStrategyType.storageStrategy)
      expect(expanded.text()).toContain(longStrategyType.persistenceSelectorStrategy)
    })
  })

  describe('Accessibility', () => {
    it('table has aria-label attribute', async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()

      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('sort headers have col scope', async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBeGreaterThan(0)
      sortHeaders.forEach((header) => {
        expect(header.attributes('scope')).toBe('col')
      })
    })

    it('edit button has title attribute', async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()

      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toContain('Edit')
    })
  })

  describe('Delete Resource Type Dialog', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('initializes with delete dialog hidden', () => {
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('opens delete dialog and sets selectedResourceType correctly', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType.name)
    })

    it('closes delete dialog and clears selection', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('passes correct props to DeleteConfirmationDialog', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')?.id).toBe(mockResourceType.id)
      expect(dialog.props('selected')?.name).toBe(mockResourceType.name)
      expect(dialog.props('type')).toBe('resource-type')
    })

    it('handles close event from DeleteConfirmationDialog', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('close')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })
  })

  describe('Delete Resource Type Action', () => {
    let deleteResourceTypesSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')

      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('calls deleteResourceTypes service on successful delete', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })

    it('closes dialog and fetches data after successful deletion', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const fetchSpy = vi.spyOn(store, 'fetchResourceTypes')

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
      expect(fetchSpy).toHaveBeenCalled()
    })

    it.each([
      { desc: 'type does not match', params: { id: 1, name: 'interfaceSnmp' }, type: 'wrong-type' },
      { desc: 'selected id does not match', params: { id: 999, name: 'interfaceSnmp' }, type: 'resource-type' },
      { desc: 'selected name does not match', params: { id: 1, name: 'wrong-name' }, type: 'resource-type' }
    ])('does not call deleteResourceTypes when $desc', async ({ params, type }) => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType(params, type)
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when selectedCollectionSource is missing', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when selected is null or missing id', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType(null, 'resource-type')
      await flushPromises()
      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()

      await wrapper.vm.deleteResourceType({ name: mockResourceType.name } as any, 'resource-type')
      await flushPromises()
      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('handles confirm event from DeleteConfirmationDialog', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })
  })

  describe('Delete Resource Type Error Handling', () => {
    let deleteResourceTypesSpy: any
    let showSnackBarSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')

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

      const pinia = createTestingPinia({
        createSpy: vi.fn,
        stubActions: false
      })
      store = useSnmpDataCollectionDetailStore(pinia)
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      store.fetchResourceTypes = vi.fn().mockResolvedValue(undefined)

      wrapper = mount(ResourceTypesTable, {
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
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Resource Type '${mockResourceType.name}' deleted successfully.`
      })
    })

    it('shows error snackbar when deletion fails', async () => {
      deleteResourceTypesSpy.mockResolvedValue(false)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete Resource Type '${mockResourceType.name}'.`,
        error: true
      })
    })

    it('shows error snackbar when validation fails', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: 999, name: 'wrong-name' }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete Resource Type \'wrong-name\'.',
        error: true
      })
    })

    it('keeps delete dialog open when deletion fails', async () => {
      deleteResourceTypesSpy.mockResolvedValue(false)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType).not.toBeNull()
    })
  })

  describe('Delete with Edge Cases', () => {
    beforeEach(async () => {
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
    })

    it('handles delete for disabled resource type', async () => {
      store.resourceTypes = [disabledResourceType]
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(disabledResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(disabledResourceType.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(disabledResourceType.name)
    })

    it('handles delete for resource type with special characters in name', async () => {
      const specialResourceType = {
        ...mockResourceType,
        id: 10,
        name: 'Resource<>Type&"Special\'Chars'
      }
      store.resourceTypes = [specialResourceType]
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(specialResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.name).toBe('Resource<>Type&"Special\'Chars')
    })

    it('handles delete for resource type with very long name', async () => {
      const longName = 'A'.repeat(200)
      const longNameResourceType = { ...mockResourceType, id: 11, name: longName }
      store.resourceTypes = [longNameResourceType]
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(longNameResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.name).toBe(longName)
    })

    it('handles rapid open/close of delete dialog', async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      wrapper.vm.closeDeleteResourceTypeDialog()
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      wrapper.vm.closeDeleteResourceTypeDialog()
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType.id)
    })

    it('handles switching selected resource type without closing dialog', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType2.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType2.name)
    })
  })

  describe('Change Status Dialog - State Management', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()
    })

    it('change status dialog is initially hidden', () => {
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
    })

    it('selected resource type is initially null', () => {
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('opens change status dialog and sets selected resource type', () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType).toEqual(mockResourceType)
    })

    it('closes change status dialog and clears selection', () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      wrapper.vm.closeChangeStatusDialog()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('opens and closes change status dialog multiple times', () => {
      for (let i = 0; i < 3; i++) {
        wrapper.vm.openChangeStatusDialog(mockResourceType)
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
        wrapper.vm.closeChangeStatusDialog()
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      }
    })

    it('opens change status dialog with null', () => {
      wrapper.vm.openChangeStatusDialog(null)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('renders SnmpDataCollectionChangeStatusDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('passes correct props to SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('type')).toBe('resource-type')
      expect(dialog.props('selected')).toEqual(mockResourceType)
    })

    it('passes correct status prop based on enabled state - enabled resource type shows Disable', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockResourceType, enabled: true })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')
    })

    it('passes correct status prop based on enabled state - disabled resource type shows Enable', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockResourceType, enabled: false })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Enable')
    })

    it('passes visible=false when change status dialog is closed', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(false)
    })

    it('handles close event from SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('close')

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })
  })

  describe('Change Resource Type Status - Successful Status Change', () => {
    let enableDisableSnmpResourceTypesSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpResourceTypes')

      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('calls enableDisableSnmpResourceTypes service when disabling', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockResourceType, enabled: true })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).toHaveBeenCalledWith(1, false, [mockResourceType.id])
    })

    it('calls enableDisableSnmpResourceTypes service when enabling', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockResourceType, enabled: false })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).toHaveBeenCalledWith(1, true, [mockResourceType.id])
    })

    it('closes dialog and fetches data after successful status change', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
      expect(store.fetchResourceTypes).toHaveBeenCalled()
    })

    it('uses correct collection source id for status change service call', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)
      store.selectedCollectionSource = { id: 99, name: 'Custom Source' } as any

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).toHaveBeenCalledWith(99, false, [mockResourceType.id])
    })

    it('handles confirm event from SnmpDataCollectionChangeStatusDialog', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('confirm', { id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).toHaveBeenCalledWith(1, false, [mockResourceType.id])
    })
  })

  describe('Change Resource Type Status - Failed Status Change', () => {
    let enableDisableSnmpResourceTypesSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpResourceTypes')

      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('keeps dialog open when status change fails', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(false)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType).not.toBeNull()
    })

    it('does not fetch data when status change fails', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(false)
      store.fetchResourceTypes = vi.fn()

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      const initialCallCount = (store.fetchResourceTypes as any).mock.calls.length

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect((store.fetchResourceTypes as any).mock.calls.length).toBe(initialCallCount)
    })
  })

  describe('Change Resource Type Status - Validation Failures', () => {
    let enableDisableSnmpResourceTypesSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpResourceTypes')

      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('does not call service when type does not match', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus({ id: mockResourceType.id, name: mockResourceType.name }, 'wrong-type')
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected id does not match', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus({ id: 999, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected name does not match', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus({ id: mockResourceType.id, name: 'wrong-name' }, 'resource-type')
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected is null', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(null, 'resource-type')
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selectedCollectionSource is missing', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected has no id', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus({ name: mockResourceType.name } as any, 'resource-type')
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selectedResourceType id is missing', async () => {
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ name: mockResourceType.name } as any)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).not.toHaveBeenCalled()
    })
  })

  describe('Change Status Edge Cases', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2, disabledResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('handles opening change status dialog for disabled resource type', () => {
      wrapper.vm.openChangeStatusDialog(disabledResourceType)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.enabled).toBe(false)
    })

    it('handles opening change status dialog for enabled resource type', () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.enabled).toBe(true)
    })

    it('handles rapid open/close of change status dialog', () => {
      for (let i = 0; i < 5; i++) {
        wrapper.vm.openChangeStatusDialog(mockResourceType)
        wrapper.vm.closeChangeStatusDialog()
      }
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType.id)
    })

    it('handles switching selected resource type in change status dialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await wrapper.vm.$nextTick()

      wrapper.vm.openChangeStatusDialog(mockResourceType2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType2.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType2.name)
    })

    it('handles resource type with special characters in name for status change', async () => {
      const specialResourceType = {
        ...mockResourceType,
        id: 20,
        name: 'Resource<>Type&"Special\'Chars'
      }

      wrapper.vm.openChangeStatusDialog(specialResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.name).toBe('Resource<>Type&"Special\'Chars')
    })

    it('handles resource type with very long name for status change', async () => {
      const longName = 'A'.repeat(200)
      const longNameResourceType = { ...mockResourceType, id: 21, name: longName }

      wrapper.vm.openChangeStatusDialog(longNameResourceType)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.name).toBe(longName)
    })

    it('handles mixed enabled/disabled resource types', async () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      expect(wrapper.vm.selectedResourceType?.enabled).toBe(true)
      wrapper.vm.closeChangeStatusDialog()

      wrapper.vm.openChangeStatusDialog(disabledResourceType)
      expect(wrapper.vm.selectedResourceType?.enabled).toBe(false)
    })

    it('correctly toggles status from enabled to disabled', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const enableDisableSnmpResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpResourceTypes')
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockResourceType, enabled: true })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).toHaveBeenCalledWith(1, false, [mockResourceType.id])
    })

    it('correctly toggles status from disabled to enabled', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const enableDisableSnmpResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpResourceTypes')
      enableDisableSnmpResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockResourceType, enabled: false })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeResourceTypeStatus(
        { id: mockResourceType.id, name: mockResourceType.name },
        'resource-type'
      )
      await flushPromises()

      expect(enableDisableSnmpResourceTypesSpy).toHaveBeenCalledWith(1, true, [mockResourceType.id])
    })
  })

  describe('Integration Tests', () => {
    it('complete workflow: search, sort, paginate', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      // Search
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('interface')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()
      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('interface')

      // Sort
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('name', 'asc')

      // Paginate
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      expect(store.onResourceTypesPageChange).toHaveBeenCalledWith(2)
    })

    it('expand and collapse multiple groups', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)

      wrapper.vm.toggleExpand(mockResourceType2.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockResourceType2.id)

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockResourceType.id)
      expect(wrapper.vm.expandedRows).toContain(mockResourceType2.id)
    })

    it('refresh clears filters and fetches data', async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetResourceTypesFilters).toHaveBeenCalled()
    })

    it('expand, edit, and collapse flow', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(1)

      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType, CreateEditMode.Edit)

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })
  })
})

