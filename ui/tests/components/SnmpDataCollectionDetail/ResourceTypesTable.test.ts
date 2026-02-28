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
import { ref } from 'vue'

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
          FeatherInput
        }
      }
    })

    await flushPromises()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Initial Rendering', () => {
    it('should render the component', () => {
      expect(wrapper.find('.resource-types-table-container').exists()).toBe(true)
    })

    it('should call fetchResourceTypes on mount', () => {
      expect(store.fetchResourceTypes).toHaveBeenCalledTimes(1)
    })

    it('should render search input with correct label, hint, and type', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('label')).toBe('Search')
      expect(searchInput.props('type')).toBe('search')
      expect(searchInput.props('hint')).toBe('Search by Name or Label')
    })

    it('should render refresh button', () => {
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('should render Add Resource Type button with secondary style', () => {
      const addButton = wrapper.find('[data-test="add-resource-type-button"]')
      expect(addButton.exists()).toBe(true)
      expect(addButton.text()).toContain('Add Resource Type')

      const addButtonComponent = wrapper.findAllComponents(FeatherButton).find(
        (b) => b.props('icon') !== 'Refresh' && b.text().includes('Add Resource Type')
      )
      expect(addButtonComponent).toBeDefined()
      expect(addButtonComponent!.props('secondary')).toBe(true)
    })

    it('should render search icon in the search input pre slot', () => {
      const searchContainer = wrapper.find('.search-container')
      expect(searchContainer.find('svg').exists() || searchContainer.find('.feather-icon').exists()).toBe(true)
    })
  })

  describe('Component Structure', () => {
    it('should have header with section-left and section-right layout', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.header .section-left').exists()).toBe(true)
      expect(wrapper.find('.header .section-right').exists()).toBe(true)
    })

    it('should have search-container and refresh inside section-left', () => {
      expect(wrapper.find('.header .section-left .search-container').exists()).toBe(true)
      expect(wrapper.find('.header .section-left .refresh').exists()).toBe(true)
    })

    it('should have add button container inside section-right', () => {
      expect(wrapper.find('.header .section-right .add').exists()).toBe(true)
    })

    it('should have container section for table and pagination', () => {
      expect(wrapper.find('.container').exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('should not render table or pagination when resourceTypes is empty', () => {
      expect(wrapper.find('.data-table').exists()).toBe(false)
      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('should not render .alerts-pagination section when there are no resource types', () => {
      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('should render EmptyList with correct message', () => {
      const emptyList = wrapper.findComponent({ name: 'EmptyList' })
      expect(emptyList.exists()).toBe(true)
      expect(emptyList.props('content')).toEqual({ msg: 'No Resource Types found.' })
    })

    it('should still show header with search and refresh when empty', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render the table with correct aria-label', () => {
      const table = wrapper.find('.data-table')
      expect(table.exists()).toBe(true)
      expect(table.attributes('aria-label')).toBe('Events Table')
    })

    it('should render 4 sortable column headers and an Actions column', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBe(4)

      const headers = wrapper.findAll('th')
      const actionsHeader = headers.find((h) => h.text() === 'Actions')
      expect(actionsHeader).toBeDefined()
    })

    it.each([
      { id: 'name', label: 'Name' },
      { id: 'label', label: 'Label' },
      { id: 'resourceLabel', label: 'Resource Label' },
      { id: 'enabled', label: 'Status' }
    ])('should render sortable column "$label" with property "$id" and scope="col"', ({ id, label }) => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      const header = sortHeaders.find((h) => h.props('property') === id)
      expect(header).toBeDefined()
      expect(header!.text()).toContain(label)
      expect(header!.attributes('scope')).toBe('col')
    })

    it('should render correct number of data rows with expected content', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)

      // First row shows resource type data
      expect(rows[0].text()).toContain(mockResourceType.name)
      expect(rows[0].text()).toContain(mockResourceType.label)
      expect(rows[0].text()).toContain(mockResourceType.resourceLabel)
      expect(rows[0].text()).toContain('Enabled')
    })

    it('should render edit button and dropdown for each row', () => {
      expect(wrapper.findAll('[data-test="edit-button"]').length).toBe(2)
      expect(wrapper.findAllComponents(FeatherDropdown).length).toBe(2)
    })

    it('should render pagination with correct props', () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.exists()).toBe(true)
      expect(pagination.props('modelValue')).toBe(1)
      expect(pagination.props('pageSize')).toBe(10)
      expect(pagination.props('total')).toBe(2)
      expect(pagination.props('pageSizes')).toEqual([10, 20, 30])
    })

    it('should render .alerts-pagination when data exists', () => {
      expect(wrapper.find('.alerts-pagination').exists()).toBe(true)
    })

    it('should render FeatherChip component for status column', () => {
      const chips = wrapper.findAllComponents(FeatherChip)
      expect(chips.length).toBe(2)
    })
  })

  describe('Status Display', () => {
    it.each([
      { enabled: true, chipText: 'Enabled' },
      { enabled: false, chipText: 'Disabled' }
    ])('should display "$chipText" chip when enabled is $enabled', async ({ enabled, chipText }) => {
      store.resourceTypes = [{ ...mockResourceType, enabled }]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(chipText)
    })

    it('should render FeatherChip with enabled-tag class for enabled types', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const chip = wrapper.find('[data-test="status-tag"]')
      expect(chip.classes()).toContain('enabled-tag')
    })

    it('should render FeatherChip with disabled-tag class for disabled types', async () => {
      store.resourceTypes = [disabledResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const chip = wrapper.find('[data-test="status-tag"]')
      expect(chip.classes()).toContain('disabled-tag')
    })
  })

  describe('Expand/Collapse Functionality', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should initialize with no expanded rows', () => {
      expect(wrapper.vm.expandedRows).toEqual([])
      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })

    it('should expand a row and display Storage and Persistence strategies', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)

      const expanded = wrapper.find('.expanded-content')
      expect(expanded.exists()).toBe(true)
      expect(expanded.find('td').attributes('colspan')).toBe('5')

      const headers = expanded.findAll('h6')
      expect(headers[0].text()).toBe('Storage Strategy:')
      expect(headers[1].text()).toBe('Persistence Selector Strategy:')
      expect(expanded.text()).toContain(mockResourceType.storageStrategy)
      expect(expanded.text()).toContain(mockResourceType.persistenceSelectorStrategy)
    })

    it('should collapse a row when toggled again', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockResourceType.id)
      expect(wrapper.find('.expanded-content').exists()).toBe(false)
    })

    it('should allow multiple rows to be expanded simultaneously', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
      expect(wrapper.vm.expandedRows).toContain(mockResourceType2.id)
      expect(wrapper.findAll('.expanded-content').length).toBe(2)
    })

    it('should handle rapid odd-number toggles correctly (ends expanded)', async () => {
      for (let i = 0; i < 5; i++) {
        wrapper.vm.toggleExpand(mockResourceType.id)
      }
      await wrapper.vm.$nextTick()

      // After odd number of toggles, should be expanded
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
    })

    it('should preserve expanded state when store data updates', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const newResourceType = { ...mockResourceType, id: 10, name: 'newItem' }
      store.resourceTypes = [mockResourceType, mockResourceType2, newResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 3 }
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
    })

    it('should hide expanded content when its row is removed from data', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType2.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(2)

      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(1)
    })

    it('should handle expand on non-existent id without rendering content', async () => {
      wrapper.vm.toggleExpand(999)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(999)
      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })

    it('should change expand button icon label from "Expand More" to "Expand Less" when expanded', async () => {
      // Before expand: find the first expand button (primary button with icon "Expand More")
      const expandButtons = wrapper.findAllComponents(FeatherButton).filter(
        (b) => b.props('icon') === 'Expand More' || b.props('icon') === 'Expand Less'
      )
      expect(expandButtons.length).toBeGreaterThan(0)
      expect(expandButtons[0].props('icon')).toBe('Expand More')

      // Expand the first row
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      // After expand: the button for the expanded row should show "Expand Less"
      const updatedButtons = wrapper.findAllComponents(FeatherButton).filter(
        (b) => b.props('icon') === 'Expand More' || b.props('icon') === 'Expand Less'
      )
      const expandedRowButton = updatedButtons.find((b) => b.props('icon') === 'Expand Less')
      expect(expandedRowButton).toBeDefined()
    })

    it('should render description paragraphs with correct class in expanded content', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const expanded = wrapper.find('.expanded-content')
      const descriptions = expanded.findAll('.description')
      expect(descriptions.length).toBe(2)
      expect(descriptions[0].text()).toBe(mockResourceType.storageStrategy)
      expect(descriptions[1].text()).toBe(mockResourceType.persistenceSelectorStrategy)
    })
  })

  describe('Search Functionality', () => {
    it('should bind search input to store.resourceTypesSearchTerm', async () => {
      store.resourceTypesSearchTerm = 'interface'
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent(FeatherInput).props('modelValue')).toBe('interface')
    })

    it('should debounce search — not trigger before 500ms, trigger after', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')

      vi.advanceTimersByTime(300)
      await wrapper.vm.$nextTick()
      expect(store.onChangeResourceTypesSearchTerm).not.toHaveBeenCalled()

      vi.advanceTimersByTime(200)
      await wrapper.vm.$nextTick()
      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('test')
    })

    it('should handle empty search term', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('')
    })

    it.each([
      { term: 'Host Resources', desc: 'with spaces' },
      { term: 'special@chars#', desc: 'with special characters' },
      { term: '資源類型', desc: 'with unicode characters' }
    ])('should handle search $desc', async ({ term }) => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue(term)
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith(term)
    })

    it('should debounce rapid typing — only last value fires', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('a')
      vi.advanceTimersByTime(100)
      await searchInput.setValue('ab')
      vi.advanceTimersByTime(100)
      await searchInput.setValue('abc')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      // Only the last value should have triggered the store call
      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('abc')
    })
  })

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    it.each([
      { property: 'name', label: 'Name' },
      { property: 'label', label: 'Label' },
      { property: 'resourceLabel', label: 'Resource Label' },
      { property: 'enabled', label: 'Status' }
    ])('should sort by $property column', async ({ property }) => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      const target = sortHeaders.find((h) => h.props('property') === property)
      await target?.vm.$emit('sort-changed', { property, value: 'asc' })
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith(property, 'asc')
    })

    it.each(['asc', 'desc'] as const)('should handle sort direction "%s"', async (direction) => {
      const sortHeader = wrapper.findComponent(FeatherSortHeader)
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: direction })
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('name', direction)
    })

    it('should reset to default sort (createdTime/desc) when SORT.NONE', async () => {
      const sortHeader = wrapper.findComponent(FeatherSortHeader)
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: SORT.NONE })
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('should reset all column sort states when a new column is sorted', async () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)

      await sortHeaders[0].vm.$emit('sort-changed', { property: 'name', value: 'asc' })
      await wrapper.vm.$nextTick()

      await sortHeaders[1].vm.$emit('sort-changed', { property: 'label', value: 'desc' })
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesSortChange).toHaveBeenLastCalledWith('label', 'desc')
    })

    it('should update sort reactive state to reflect current sort column and direction', async () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      await sortHeaders[0].vm.$emit('sort-changed', { property: 'name', value: 'asc' })
      await wrapper.vm.$nextTick()

      // After sorting by name asc, the sort state for name should be 'asc' and all others SORT.NONE
      expect(wrapper.vm.sort.name).toBe('asc')
      expect(wrapper.vm.sort.label).toBe(SORT.NONE)
      expect(wrapper.vm.sort.resourceLabel).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('should clear previous column sort state when sorting a new column', async () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)

      await sortHeaders[0].vm.$emit('sort-changed', { property: 'name', value: 'asc' })
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.sort.name).toBe('asc')

      await sortHeaders[1].vm.$emit('sort-changed', { property: 'label', value: 'desc' })
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.label).toBe('desc')
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()
    })

    it('should call onResourceTypesPageChange when page changes', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesPageChange).toHaveBeenCalledWith(2)
    })

    it('should call onResourceTypesPageSizeChange when page size changes', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', 20)
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesPageSizeChange).toHaveBeenCalledWith(20)
    })

    it('should update pagination props when store pagination changes', async () => {
      store.resourceTypesPagination = { page: 3, pageSize: 30, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(3)
      expect(pagination.props('pageSize')).toBe(30)
      expect(pagination.props('total')).toBe(100)
    })
  })

  describe('Refresh Button', () => {
    it('should call resetResourceTypesFilters on click', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetResourceTypesFilters).toHaveBeenCalledTimes(1)
    })
  })

  describe('Add Resource Type Button', () => {
    it('should call openResourceTypeCreationDrawer with null and Create mode', async () => {
      await wrapper.get('[data-test="add-resource-type-button"]').trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledTimes(1)
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should have title attribute on edit button', () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toBeDefined()
    })

    it('should call openResourceTypeCreationDrawer with Edit mode for first resource', async () => {
      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType, CreateEditMode.Edit)
    })

    it('should pass the correct resource type per row on edit click', async () => {
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      await editButtons[1].trigger('click')
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType2, CreateEditMode.Edit)
    })
  })

  describe('Dropdown Menu', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, disabledResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render a dropdown with "More Options" trigger for each row', () => {
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)

      const triggerButton = dropdowns[0].findComponent(FeatherButton)
      expect(triggerButton.props('icon')).toBe('More Options')
    })

    it('should render a dropdown for each resource type row', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(rows.length)
    })
  })

  describe('Reactivity', () => {
    it('should show table when store.resourceTypes is populated', async () => {
      expect(wrapper.find('.data-table').exists()).toBe(false)

      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('should update search input when store.resourceTypesSearchTerm changes', async () => {
      store.resourceTypesSearchTerm = 'newSearch'
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent(FeatherInput).props('modelValue')).toBe('newSearch')
    })

    it('should handle TransitionGroup row additions and removals', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('transition-group-stub tr').length).toBeGreaterThanOrEqual(2)

      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('transition-group-stub tr').length).toBeGreaterThanOrEqual(1)
    })


  })

  describe('Delete Resource Type Dialog', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('should initialize with dialog hidden and no selection', () => {
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('should render DeleteConfirmationDialog with correct initial props', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
      expect(dialog.props('visible')).toBe(false)
      expect(dialog.props('type')).toBe('resource-type')
    })

    it('should open dialog with correct selection and pass props', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType).toEqual({
        id: mockResourceType.id,
        name: mockResourceType.name
      })

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')?.id).toBe(mockResourceType.id)
      expect(dialog.props('selected')?.name).toBe(mockResourceType.name)
    })

    it('should open dialog for different resource types', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType2.id, mockResourceType2.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType2.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType2.name)
    })

    it('should close dialog and clear selection', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('should handle close event from DeleteConfirmationDialog', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
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

    it('should call deleteResourceTypes service with correct params on valid confirm', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })

    it('should close dialog and fetch resource types after successful deletion', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      const fetchSpy = vi.spyOn(store, 'fetchResourceTypes')

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
      expect(fetchSpy).toHaveBeenCalled()
    })

    it('should handle confirm event from DeleteConfirmationDialog', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })

    describe('Validation — should not call deleteResourceTypes when', () => {
      beforeEach(async () => {
        deleteResourceTypesSpy.mockResolvedValue(true)
        wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
        await wrapper.vm.$nextTick()
      })

      it('type does not match', async () => {
        await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'wrong-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })

      it('selected id does not match', async () => {
        await wrapper.vm.deleteResourceType({ id: 999, name: mockResourceType.name }, 'resource-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })

      it('selected name does not match', async () => {
        await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: 'wrong-name' }, 'resource-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })

      it('selectedCollectionSource is missing', async () => {
        store.selectedCollectionSource = null as any
        await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })

      it('selected is null', async () => {
        await wrapper.vm.deleteResourceType(null, 'resource-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })

      it('selected id is missing (undefined)', async () => {
        await wrapper.vm.deleteResourceType({ name: mockResourceType.name } as any, 'resource-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })

      it('selectedCollectionSource.id is 0 (falsy)', async () => {
        store.selectedCollectionSource = { id: 0, name: 'Test Source' } as any
        await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })

      it('selection was switched before confirm', async () => {
        // Switch to a different resource type
        wrapper.vm.openResourceTypeDeleteDialog(mockResourceType2.id, mockResourceType2.name)
        await wrapper.vm.$nextTick()

        // Try to delete with the original resource's params
        await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
        await flushPromises()
        expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
      })
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

      // Remount to pick up mocked snackbar
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

    it('should show success snackbar on successful deletion', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Resource Type '${mockResourceType.name}' deleted successfully.`
      })
    })

    it('should show error snackbar when service returns false', async () => {
      deleteResourceTypesSpy.mockResolvedValue(false)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete Resource Type '${mockResourceType.name}'.`,
        error: true
      })
    })

    it('should keep dialog visible when service returns false', async () => {
      deleteResourceTypesSpy.mockResolvedValue(false)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })

    it('should show error snackbar on validation failure', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: 999, name: 'wrong-name' }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete Resource Type \'wrong-name\'.',
        error: true
      })
    })

    it('should show error snackbar on type mismatch', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'wrong-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete Resource Type '${mockResourceType.name}'.`,
        error: true
      })
    })

    it('should show error snackbar when selectedCollectionSource is missing', async () => {
      store.selectedCollectionSource = null as any

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete Resource Type '${mockResourceType.name}'.`,
        error: true
      })
    })

    it('should handle service throwing an exception gracefully', async () => {
      deleteResourceTypesSpy.mockRejectedValue(new Error('Network error'))

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      // The component does not have try/catch, so the promise rejection propagates
      await expect(
        wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      ).rejects.toThrow('Network error')
    })

    it('should not call fetchResourceTypes after failed deletion', async () => {
      deleteResourceTypesSpy.mockResolvedValue(false)
      const fetchSpy = store.fetchResourceTypes as ReturnType<typeof vi.fn>
      fetchSpy.mockClear()

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(fetchSpy).not.toHaveBeenCalled()
    })
  })

  describe('Delete Dialog Edge Cases', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it.each([
      { id: 0, name: 'zero-id-resource' },
      { id: -1, name: 'negative-id-resource' },
      { id: 1, name: '' },
      { id: 1, name: 'test-resource_type.v2<>&"' },
      { id: 1, name: '資源類型テスト' }
    ])('should handle opening dialog with id=$id and name="$name"', async ({ id, name }) => {
      wrapper.vm.openResourceTypeDeleteDialog(id, name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType).toEqual({ id, name })
    })

    it('should handle multiple open/close cycles', async () => {
      for (let i = 0; i < 3; i++) {
        wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
        await wrapper.vm.$nextTick()
        expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

        wrapper.vm.closeDeleteResourceTypeDialog()
        await wrapper.vm.$nextTick()
        expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      }
    })

    it('should handle rapid open/close without error', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      wrapper.vm.closeDeleteResourceTypeDialog()
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })
  })

  describe('Edge Cases', () => {
    it('should render row with empty string fields', async () => {
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

    it('should handle special characters in fields', async () => {
      const specialResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        name: 'test-resource_type.v2',
        label: 'Test <Resource> Type & More'
      }

      store.resourceTypes = [specialResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const row = wrapper.findAll('transition-group-stub tr')[0]
      expect(row.text()).toContain('test-resource_type.v2')
    })

    it('should handle unicode characters in fields', async () => {
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

    it('should display long strategy strings in expanded content', async () => {
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

    it('should handle pagination with large total', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 10000 }
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent(FeatherPagination).props('total')).toBe(10000)
    })

    it('should handle clearing data while rows are expanded', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      store.resourceTypes = []
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })

    it('should render multiple resource type rows correctly', async () => {
      const resourceTypes: SnmpCollectionResourceType[] = Array.from({ length: 5 }, (_, i) => ({
        ...mockResourceType,
        id: i + 1,
        name: `resourceType-${i + 1}`,
        label: `Resource Type ${i + 1}`,
        resourceLabel: `${i + 1}`
      }))

      store.resourceTypes = resourceTypes
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 5 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(5)
    })

    it('should handle pagination with total=0 boundary', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 0 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(0)
    })

    it('should handle mixed enabled/disabled resource types in same view', async () => {
      store.resourceTypes = [mockResourceType, disabledResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips.length).toBe(2)
      expect(chips[0].classes()).toContain('enabled-tag')
      expect(chips[1].classes()).toContain('disabled-tag')
    })

    it('should handle switching resource types data completely', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      let rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockResourceType.name)

      // Replace with completely different data
      store.resourceTypes = [mockResourceType2]
      await wrapper.vm.$nextTick()

      rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockResourceType2.name)
      expect(rows[0].text()).not.toContain(mockResourceType.name)
    })
  })

  describe('Integration Flows', () => {
    it('should handle complete search, sort, paginate flow', async () => {
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
      const sortHeader = wrapper.findComponent(FeatherSortHeader)
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: 'asc' })
      await wrapper.vm.$nextTick()
      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('name', 'asc')

      // Paginate
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      await wrapper.vm.$nextTick()
      expect(store.onResourceTypesPageChange).toHaveBeenCalledWith(2)
    })

    it('should handle expand, edit, and collapse flow', async () => {
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

    it('should handle complete delete flow: open, confirm, close', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')
      deleteResourceTypesSpy.mockResolvedValue(true)

      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })

    it('should handle cancel delete flow: open, close', async () => {
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('should handle delete flow when service fails', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')
      deleteResourceTypesSpy.mockResolvedValue(false)

      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      // Dialog should remain visible on failure
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })

    it('should handle refresh after expanding rows', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetResourceTypesFilters).toHaveBeenCalled()
    })
  })
})

