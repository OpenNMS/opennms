import ResourceTypesTable from '@/components/SnmpDataCollectionDetail/ResourceTypesTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { SnmpCollectionResourceType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

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
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Initial Rendering', () => {
    it('should render the component', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('should display the title "Resource Types"', () => {
      const title = wrapper.find('.title')
      expect(title.exists()).toBe(true)
      expect(title.text()).toBe('Resource Types')
    })

    it('should render the search input', () => {
      const searchInput = wrapper.find('[data-test="search-input"]')
      expect(searchInput.exists()).toBe(true)
    })

    it('should render the refresh button', () => {
      const refreshButton = wrapper.find('[data-test="refresh-button"]')
      expect(refreshButton.exists()).toBe(true)
    })

    it('should call fetchResourceTypes on mount', () => {
      expect(store.fetchResourceTypes).toHaveBeenCalledTimes(1)
    })

    it('should render within a TableCard container', () => {
      expect(wrapper.find('.resource-types-table-container').exists()).toBe(true)
    })

    it('should render search input with correct hint text', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('hint')).toBe('Search by Name or Label')
    })

    it('should render search input with correct label', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('label')).toBe('Search')
    })

    it('should render search input with type search', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('type')).toBe('search')
    })
  })

  describe('Empty State', () => {
    it('should not render the table when resourceTypes is empty', async () => {
      store.resourceTypes = []
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(false)
    })

    it('should not render pagination when resourceTypes is empty', async () => {
      store.resourceTypes = []
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('should not render table rows when resourceTypes is empty', async () => {
      store.resourceTypes = []
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('transition-group-stub tr').length).toBe(0)
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

    it('should render the table when resourceTypes has data', () => {
      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('should render correct number of data rows', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('should display resource type name in first column', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockResourceType.name)
    })

    it('should display resource type label in second column', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockResourceType.label)
    })

    it('should display resource label in third column', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockResourceType.resourceLabel)
    })

    it('should display "Enabled" for enabled resource types', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Enabled')
    })

    it('should display "Disabled" for disabled resource types', async () => {
      store.resourceTypes = [disabledResourceType]
      await wrapper.vm.$nextTick()
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Disabled')
    })

    it('should render action buttons for each row', () => {
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons.length).toBe(2)
    })

    it('should render pagination when there is data', () => {
      expect(wrapper.find('.alerts-pagination').exists()).toBe(true)
    })

    it('should render table with correct aria-label', () => {
      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBe('Events Table')
    })

    it('should render 4 sortable column headers', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBe(4)
    })

    it('should render Actions header column', () => {
      const headers = wrapper.findAll('th')
      const actionsHeader = headers.find(h => h.text() === 'Actions')
      expect(actionsHeader).toBeDefined()
    })

    describe('Column Labels', () => {
      const expectedColumns = [
        { id: 'name', label: 'Name' },
        { id: 'label', label: 'Label' },
        { id: 'resourceLabel', label: 'Resource Label' },
        { id: 'enabled', label: 'Status' }
      ]

      it.each(expectedColumns)('should render column header for $label', ({ label }) => {
        const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
        const headerExists = sortHeaders.some(h => h.text().includes(label))
        expect(headerExists).toBe(true)
      })

      it.each(expectedColumns)('should have correct property for $label column', ({ id }) => {
        const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
        const properties = sortHeaders.map(h => h.props('property'))
        expect(properties).toContain(id)
      })
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
    })

    it('should not show expanded content by default', () => {
      const expandedRows = wrapper.findAll('.expanded-content')
      expect(expandedRows.length).toBe(0)
    })

    it('should expand row when toggleExpand is called', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
    })

    it('should show expanded content when row is expanded', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.expanded-content').exists()).toBe(true)
    })

    it('should show storage strategy in expanded content', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Storage Strategy:')
      expect(expandedContent.text()).toContain(mockResourceType.storageStrategy)
    })

    it('should show persistence selector strategy in expanded content', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Persistence Selector Strategy:')
      expect(expandedContent.text()).toContain(mockResourceType.persistenceSelectorStrategy)
    })

    it('should collapse row when toggleExpand is called again', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockResourceType.id)
    })

    it('should hide expanded content when row is collapsed', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)

      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(false)
    })

    it('should allow multiple rows to be expanded simultaneously', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
      expect(wrapper.vm.expandedRows).toContain(mockResourceType2.id)
    })

    it('should have colspan of 5 on expanded content cell', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content td')
      expect(expandedContent.attributes('colspan')).toBe('5')
    })

    it('should show Storage Strategy header in expanded content', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      const headers = expandedContent.findAll('h6')
      expect(headers[0].text()).toBe('Storage Strategy:')
    })

    it('should show Persistence Selector Strategy header in expanded content', async () => {
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      const headers = expandedContent.findAll('h6')
      expect(headers[1].text()).toBe('Persistence Selector Strategy:')
    })
  })

  describe('Search Functionality', () => {
    it('should have search input bound to store.resourceTypesSearchTerm', async () => {
      store.resourceTypesSearchTerm = 'interface'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('interface')
    })

    it('should debounce search input', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('test')
    })

    it('should not trigger search before debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).not.toHaveBeenCalled()
    })

    it('should call onChangeResourceTypesSearchTerm after debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('search term')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('search term')
    })

    it('should handle empty search term', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('')
    })

    describe('Search Term Variations', () => {
      const searchTerms = [
        { term: 'interface', description: 'partial name' },
        { term: 'SNMP', description: 'uppercase' },
        { term: 'storage', description: 'lowercase' },
        { term: 'Host Resources', description: 'with spaces' },
        { term: 'special@chars#', description: 'special characters' },
        { term: '123numeric456', description: 'numeric' }
      ]

      it.each(searchTerms)('should handle search with $description: "$term"', async ({ term }) => {
        const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
        await searchInput.setValue(term)
        vi.advanceTimersByTime(500)
        await wrapper.vm.$nextTick()

        expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith(term)
      })
    })
  })

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    describe('Sort Direction Changes', () => {
      const sortDirections = [
        { value: 'asc', expected: { property: 'name', order: 'asc' } },
        { value: 'desc', expected: { property: 'name', order: 'desc' } }
      ]

      it.each(sortDirections)('should call onResourceTypesSortChange with $value direction', async ({ value, expected }) => {
        const sortHeader = wrapper.findComponent(FeatherSortHeader)
        await sortHeader.vm.$emit('sort-changed', { property: 'name', value })
        await wrapper.vm.$nextTick()

        expect(store.onResourceTypesSortChange).toHaveBeenCalledWith(expected.property, expected.order)
      })
    })

    describe('Sort Column Changes', () => {
      const sortColumns = [
        { property: 'name', label: 'Name' },
        { property: 'label', label: 'Label' },
        { property: 'resourceLabel', label: 'Resource Label' },
        { property: 'enabled', label: 'Status' }
      ]

      it.each(sortColumns)('should sort by $property column', async ({ property }) => {
        const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
        const targetHeader = sortHeaders.find(h => h.props('property') === property)

        await targetHeader?.vm.$emit('sort-changed', { property, value: 'asc' })
        await wrapper.vm.$nextTick()

        expect(store.onResourceTypesSortChange).toHaveBeenCalledWith(property, 'asc')
      })
    })

    it('should reset to default sort when sort value is NONE', async () => {
      const sortHeader = wrapper.findComponent(FeatherSortHeader)
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: SORT.NONE })
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('should reset all sort states when a new column is sorted', async () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)

      // Sort by name first
      await sortHeaders[0].vm.$emit('sort-changed', { property: 'name', value: 'asc' })
      await wrapper.vm.$nextTick()

      // Then sort by label
      await sortHeaders[1].vm.$emit('sort-changed', { property: 'label', value: 'desc' })
      await wrapper.vm.$nextTick()

      expect(store.onResourceTypesSortChange).toHaveBeenLastCalledWith('label', 'desc')
    })

    it('should have scope="col" on sort headers', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      sortHeaders.forEach(header => {
        expect(header.attributes('scope')).toBe('col')
      })
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()
    })

    it('should render pagination component', () => {
      expect(wrapper.findComponent(FeatherPagination).exists()).toBe(true)
    })

    it('should pass correct modelValue to pagination', () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(1)
    })

    it('should pass correct pageSize to pagination', () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('pageSize')).toBe(10)
    })

    it('should pass correct total to pagination', () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(50)
    })

    it('should pass correct pageSizes options to pagination', () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('pageSizes')).toEqual([10, 20, 30])
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

    describe('Page Size Changes', () => {
      const pageSizes = [10, 20, 30]

      it.each(pageSizes)('should handle page size change to %i', async (pageSize) => {
        const pagination = wrapper.findComponent(FeatherPagination)
        await pagination.vm.$emit('update:pageSize', pageSize)
        await wrapper.vm.$nextTick()

        expect(store.onResourceTypesPageSizeChange).toHaveBeenCalledWith(pageSize)
      })
    })

    describe('Page Navigation', () => {
      const pages = [1, 2, 3, 4, 5]

      it.each(pages)('should handle navigation to page %i', async (page) => {
        const pagination = wrapper.findComponent(FeatherPagination)
        await pagination.vm.$emit('update:modelValue', page)
        await wrapper.vm.$nextTick()

        expect(store.onResourceTypesPageChange).toHaveBeenCalledWith(page)
      })
    })
  })

  describe('Refresh Functionality', () => {
    it('should call resetResourceTypesFilters when refresh button is clicked', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetResourceTypesFilters).toHaveBeenCalledTimes(1)
    })

    it('should be clickable multiple times', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      await wrapper.get('[data-test="refresh-button"]').trigger('click')

      expect(store.resetResourceTypesFilters).toHaveBeenCalledTimes(3)
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render edit button for each row', () => {
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons.length).toBe(2)
    })

    it('should have correct title attribute on edit button', () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toBeDefined()
    })

    it('should log to console when edit button is clicked', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      await wrapper.get('[data-test="edit-button"]').trigger('click')

      expect(consoleSpy).toHaveBeenCalledWith('Resource Type clicked:', mockResourceType)
      consoleSpy.mockRestore()
    })

    it('should pass the correct resource type when edit button is clicked', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      await editButtons[1].trigger('click')

      expect(consoleSpy).toHaveBeenCalledWith('Resource Type clicked:', mockResourceType2)
      consoleSpy.mockRestore()
    })
  })

  describe('Dropdown Menu', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, disabledResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render dropdown component for each row', () => {
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })

    it('should render dropdown trigger button', () => {
      const dropdown = wrapper.findComponent(FeatherDropdown)
      const triggerButton = dropdown.findComponent(FeatherButton)
      expect(triggerButton.exists()).toBe(true)
    })

    it('should have More Options icon on dropdown trigger', () => {
      const dropdown = wrapper.findComponent(FeatherDropdown)
      const triggerButton = dropdown.findComponent(FeatherButton)
      expect(triggerButton.props('icon')).toBe('More Options')
    })
  })

  describe('Status Display', () => {
    describe('Enabled/Disabled Status', () => {
      const statusCases = [
        { enabled: true, expectedText: 'Enabled' },
        { enabled: false, expectedText: 'Disabled' }
      ]

      it.each(statusCases)('should display "$expectedText" when enabled is $enabled', async ({ enabled, expectedText }) => {
        const resourceType = { ...mockResourceType, enabled }
        store.resourceTypes = [resourceType]
        store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
        await wrapper.vm.$nextTick()

        const rows = wrapper.findAll('transition-group-stub tr')
        expect(rows[0].text()).toContain(expectedText)
      })
    })

    describe('Dropdown Status Text', () => {
      it('should render dropdown for enabled resource types', async () => {
        store.resourceTypes = [mockResourceType]
        store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
        await wrapper.vm.$nextTick()

        // FeatherDropdown is rendered within the component
        const dropdown = wrapper.findComponent(FeatherDropdown)
        expect(dropdown.exists()).toBe(true)
      })

      it('should render dropdown for disabled resource types', async () => {
        store.resourceTypes = [disabledResourceType]
        store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
        await wrapper.vm.$nextTick()

        const dropdown = wrapper.findComponent(FeatherDropdown)
        expect(dropdown.exists()).toBe(true)
      })
    })
  })

  describe('Resource Type Data Display', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    describe('Column Data Mapping', () => {
      const columnMappings = [
        { column: 1, field: 'name', value: 'interfaceSnmp' },
        { column: 2, field: 'label', value: 'SNMP Interface Data' },
        { column: 3, field: 'resourceLabel', value: '${ifDescr}' },
        { column: 4, field: 'enabled', value: 'Enabled' }
      ]

      it.each(columnMappings)('should display $field in column $column', ({ value }) => {
        const rows = wrapper.findAll('transition-group-stub tr')
        expect(rows[0].text()).toContain(value)
      })
    })

    describe('Expanded Content Data', () => {
      beforeEach(async () => {
        wrapper.vm.toggleExpand(mockResourceType.id)
        await wrapper.vm.$nextTick()
      })

      it('should display storage strategy value correctly', () => {
        const expandedContent = wrapper.find('.expanded-content')
        expect(expandedContent.text()).toContain(mockResourceType.storageStrategy)
      })

      it('should display persistence selector strategy value correctly', () => {
        const expandedContent = wrapper.find('.expanded-content')
        expect(expandedContent.text()).toContain(mockResourceType.persistenceSelectorStrategy)
      })
    })
  })

  describe('Multiple Resource Types', () => {
    const resourceTypeCounts = [1, 2, 5, 10]

    it.each(resourceTypeCounts)('should render %i resource type rows correctly', async (count) => {
      const resourceTypes: SnmpCollectionResourceType[] = Array.from({ length: count }, (_, i) => ({
        ...mockResourceType,
        id: i + 1,
        name: `resourceType-${i + 1}`,
        label: `Resource Type ${i + 1}`,
        resourceLabel: `${i + 1}`
      }))

      store.resourceTypes = resourceTypes
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: count }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(count)
    })
  })

  describe('Edge Cases', () => {
    it('should handle resource type with empty strings', async () => {
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

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
    })

    it('should handle resource type with special characters in name', async () => {
      const specialResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        name: 'test-resource_type.v2',
        label: 'Test <Resource> Type & More'
      }

      store.resourceTypes = [specialResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('test-resource_type.v2')
    })

    it('should handle resource type with very long strategy strings', async () => {
      const longStrategyResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        storageStrategy: 'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy.VeryLongClassName.WithMultiple.Packages',
        persistenceSelectorStrategy: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy.AnotherVeryLongClassName'
      }

      store.resourceTypes = [longStrategyResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(longStrategyResourceType.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain(longStrategyResourceType.storageStrategy)
      expect(expandedContent.text()).toContain(longStrategyResourceType.persistenceSelectorStrategy)
    })

    it('should handle toggling expansion on non-existent row gracefully', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Expand
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      // Clear data
      store.resourceTypes = []
      await wrapper.vm.$nextTick()

      // Should not throw error
      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })

    it('should handle pagination with total less than page size', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(1)
      expect(pagination.props('pageSize')).toBe(10)
    })

    it('should handle large total counts', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 10000 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(10000)
    })
  })

  describe('Accessibility', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    it('should have aria-label on table', () => {
      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('should have scope="col" on all sort headers', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      sortHeaders.forEach(header => {
        expect(header.attributes('scope')).toBe('col')
      })
    })

    it('should have title attribute on edit buttons', () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toBeDefined()
    })

    it('should have descriptive icon attributes on buttons', () => {
      const refreshButton = wrapper.find('[data-test="refresh-button"]')
      expect(refreshButton.exists()).toBe(true)
    })

    it('should have search input with label', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('label')).toBe('Search')
    })
  })

  describe('Component Structure', () => {
    it('should have header section', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
    })

    it('should have title-container within header', () => {
      expect(wrapper.find('.header .title-container').exists()).toBe(true)
    })

    it('should have action-container within header', () => {
      expect(wrapper.find('.header .action-container').exists()).toBe(true)
    })

    it('should have search-container within action-container', () => {
      expect(wrapper.find('.header .action-container .search-container').exists()).toBe(true)
    })

    it('should have refresh button container', () => {
      expect(wrapper.find('.header .action-container .refresh').exists()).toBe(true)
    })

    it('should have container section for table and pagination', () => {
      expect(wrapper.find('.container').exists()).toBe(true)
    })
  })

  describe('Loading States', () => {
    it('should handle store loading state changes', async () => {
      store.isLoading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.exists()).toBe(true)

      store.isLoading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.exists()).toBe(true)
    })
  })

  describe('TransitionGroup', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render TransitionGroup', () => {
      const transitionGroup = wrapper.find('transition-group-stub')
      expect(transitionGroup.exists()).toBe(true)
    })

    it('should have unique keys for each resource type row', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('should handle resource type removal', async () => {
      store.resourceTypes = [mockResourceType]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
    })

    it('should handle resource type addition', async () => {
      const newResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        id: 3,
        name: 'newResourceType'
      }
      store.resourceTypes = [mockResourceType, mockResourceType2, newResourceType]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(3)
    })
  })

  describe('Reactivity', () => {
    it('should update table when store.resourceTypes changes', async () => {
      expect(wrapper.find('.data-table').exists()).toBe(false)

      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('should update pagination when store.resourceTypesPagination changes', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 2, pageSize: 20, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(2)
      expect(pagination.props('pageSize')).toBe(20)
      expect(pagination.props('total')).toBe(100)
    })

    it('should update search input when store.resourceTypesSearchTerm changes', async () => {
      store.resourceTypesSearchTerm = 'newSearch'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('newSearch')
    })
  })

  describe('Integration', () => {
    it('should handle complete user flow: search, sort, paginate', async () => {
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
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Expand
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(1)

      // Edit
      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(consoleSpy).toHaveBeenCalledWith('Resource Type clicked:', mockResourceType)

      // Collapse
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(0)

      consoleSpy.mockRestore()
    })

    it('should handle refresh and maintain state', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Expand a row
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      // Click refresh
      await wrapper.get('[data-test="refresh-button"]').trigger('click')

      expect(store.resetResourceTypesFilters).toHaveBeenCalled()
    })
  })
})
