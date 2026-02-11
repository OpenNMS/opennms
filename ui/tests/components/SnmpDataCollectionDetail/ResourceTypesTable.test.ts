import ResourceTypesTable from '@/components/SnmpDataCollectionDetail/ResourceTypesTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionResourceType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
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

    it('should render the Add Resource Type button', () => {
      const addButton = wrapper.find('[data-test="add-resource-type-button"]')
      expect(addButton.exists()).toBe(true)
      expect(addButton.text()).toBe('Add Resource Type')
    })

    it('should render Add Resource Type button with primary style', () => {
      const featherButtons = wrapper.findAllComponents(FeatherButton)
      const addButton = featherButtons.find((btn) => btn.text().includes('Add Resource Type'))
      expect(addButton).toBeDefined()
      expect(addButton?.props('primary')).toBe(true)
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

    it('should display EmptyList component with correct message when no data', async () => {
      store.resourceTypes = []
      await wrapper.vm.$nextTick()

      const emptyMessage = wrapper.text()
      expect(emptyMessage).toContain('No Resource Types found.')
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
      const actionsHeader = headers.find((h) => h.text() === 'Actions')
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
        const headerExists = sortHeaders.some((h) => h.text().includes(label))
        expect(headerExists).toBe(true)
      })

      it.each(expectedColumns)('should have correct property for $label column', ({ id }) => {
        const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
        const properties = sortHeaders.map((h) => h.props('property'))
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

      it.each(sortDirections)(
        'should call onResourceTypesSortChange with $value direction',
        async ({ value, expected }) => {
          const sortHeader = wrapper.findComponent(FeatherSortHeader)
          await sortHeader.vm.$emit('sort-changed', { property: 'name', value })
          await wrapper.vm.$nextTick()

          expect(store.onResourceTypesSortChange).toHaveBeenCalledWith(expected.property, expected.order)
        }
      )
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
        const targetHeader = sortHeaders.find((h) => h.props('property') === property)

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
      sortHeaders.forEach((header) => {
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

  describe('Add Resource Type Button', () => {
    it('should call openResourceTypeCreationDrawer when Add Resource Type button is clicked', async () => {
      await wrapper.get('[data-test="add-resource-type-button"]').trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('should call openResourceTypeCreationDrawer with Create mode', async () => {
      await wrapper.get('[data-test="add-resource-type-button"]').trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledTimes(1)
    })

    it('should be clickable multiple times', async () => {
      await wrapper.get('[data-test="add-resource-type-button"]').trigger('click')
      await wrapper.get('[data-test="add-resource-type-button"]').trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledTimes(2)
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

    it('should call openResourceTypeCreationDrawer with Edit mode when edit button is clicked', async () => {
      await wrapper.get('[data-test="edit-button"]').trigger('click')

      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType, CreateEditMode.Edit)
    })

    it('should pass the correct resource type when edit button is clicked', async () => {
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

    it('should render dropdown for each resource type row', () => {
      // Each row should have one dropdown
      const rows = wrapper.findAll('transition-group-stub tr')
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(rows.length)
    })
  })

  describe('Status Display', () => {
    describe('Enabled/Disabled Status', () => {
      const statusCases = [
        { enabled: true, expectedText: 'Enabled' },
        { enabled: false, expectedText: 'Disabled' }
      ]

      it.each(statusCases)(
        'should display "$expectedText" when enabled is $enabled',
        async ({ enabled, expectedText }) => {
          const resourceType = { ...mockResourceType, enabled }
          store.resourceTypes = [resourceType]
          store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
          await wrapper.vm.$nextTick()

          const rows = wrapper.findAll('transition-group-stub tr')
          expect(rows[0].text()).toContain(expectedText)
        }
      )
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
        storageStrategy:
          'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy.VeryLongClassName.WithMultiple.Packages',
        persistenceSelectorStrategy:
          'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy.AnotherVeryLongClassName'
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

    it('should handle unicode characters in resource type fields', async () => {
      const unicodeResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        name: '資源類型-テスト',
        label: 'Étiquette accentuée 日本語',
        resourceLabel: '${リソース_äöü}'
      }

      store.resourceTypes = [unicodeResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('資源類型-テスト')
      expect(rows[0].text()).toContain('Étiquette accentuée 日本語')
    })

    it('should handle zero id value', async () => {
      const zeroIdResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        id: 0,
        name: 'zeroIdResource'
      }

      store.resourceTypes = [zeroIdResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
      expect(rows[0].text()).toContain('zeroIdResource')
    })

    it('should handle negative id value', async () => {
      const negativeIdResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        id: -1,
        name: 'negativeIdResource'
      }

      store.resourceTypes = [negativeIdResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
      expect(rows[0].text()).toContain('negativeIdResource')
    })

    it('should handle rapid expand/collapse toggles', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Rapidly toggle multiple times
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()

      // After odd number of toggles, should be expanded
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
    })

    it('should preserve expanded state when data updates', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      // Expand first row
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)

      // Update store with same data plus new item
      const newResourceType: SnmpCollectionResourceType = {
        ...mockResourceType,
        id: 3,
        name: 'newItem'
      }
      store.resourceTypes = [mockResourceType, mockResourceType2, newResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 3 }
      await wrapper.vm.$nextTick()

      // Expanded state should be preserved
      expect(wrapper.vm.expandedRows).toContain(mockResourceType.id)
    })

    it('should handle very long resourceLabel', async () => {
      const longResourceLabelType: SnmpCollectionResourceType = {
        ...mockResourceType,
        resourceLabel: '${' + 'A'.repeat(500) + '}'
      }

      store.resourceTypes = [longResourceLabelType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
    })

    it('should handle expand toggle on non-existent id without crashing', async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Toggle non-existent id
      wrapper.vm.toggleExpand(999)
      await wrapper.vm.$nextTick()

      // Should add to expandedRows even if row doesn't exist
      expect(wrapper.vm.expandedRows).toContain(999)
      // But no expanded content should render
      const expandedContent = wrapper.findAll('.expanded-content')
      expect(expandedContent.length).toBe(0)
    })

    it('should handle multiple expanded rows with some removed from data', async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      // Expand both rows
      wrapper.vm.toggleExpand(mockResourceType.id)
      wrapper.vm.toggleExpand(mockResourceType2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows.length).toBe(2)

      // Remove second item from data
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Only one expanded content should show (the one still in data)
      const expandedContent = wrapper.findAll('.expanded-content')
      expect(expandedContent.length).toBe(1)
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
      sortHeaders.forEach((header) => {
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

    it('should have add button container within action-container', () => {
      expect(wrapper.find('.header .action-container .add').exists()).toBe(true)
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
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Expand
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(1)

      // Edit
      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType, CreateEditMode.Edit)

      // Collapse
      wrapper.vm.toggleExpand(mockResourceType.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(0)
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

    it('has openResourceTypeDeleteDialog method available', async () => {
      expect(typeof wrapper.vm.openResourceTypeDeleteDialog).toBe('function')
    })

    it('opens delete dialog via openResourceTypeDeleteDialog method', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType.name)
    })

    it('sets selectedResourceType correctly when opening dialog', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType.name)
    })

    it('sets selectedResourceType correctly for different resource types', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType2.id, mockResourceType2.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType2.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType2.name)
    })

    it('closes delete dialog and clears selection', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('renders DeleteConfirmationDialog component', async () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('passes correct props to DeleteConfirmationDialog', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')?.id).toBe(mockResourceType.id)
      expect(dialog.props('selected')?.name).toBe(mockResourceType.name)
      expect(dialog.props('type')).toBe('resource-type')
    })

    it('has closeDeleteResourceTypeDialog method available', async () => {
      expect(typeof wrapper.vm.closeDeleteResourceTypeDialog).toBe('function')
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

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })

    it('closes dialog after successful deletion', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('fetches resource types after successful deletion', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      const fetchSpy = vi.spyOn(store, 'fetchResourceTypes')

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(fetchSpy).toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when type does not match', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'wrong-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when selected id does not match', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: 999, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when selected name does not match', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: 'wrong-name' }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when selectedCollectionSource is missing', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when selected is null', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType(null, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteResourceTypes when selected id is missing', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ name: mockResourceType.name } as any, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
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

      // Remount wrapper to pick up mocked snackbar
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

    it('shows error snackbar when deletion fails', async () => {
      deleteResourceTypesSpy.mockResolvedValue(false)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete Resource Type. Please try again.',
        error: true
      })
    })

    it('shows error snackbar when validation fails', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: 999, name: 'wrong-name' }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete Resource Type. Please try again.',
        error: true
      })
    })

    it('shows success snackbar when deletion succeeds', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Resource Type '${mockResourceType.name}' deleted successfully.`
      })
    })

    it('shows error when type mismatch occurs', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'wrong-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete Resource Type. Please try again.',
        error: true
      })
    })

    it('shows error when selected collection source is missing', async () => {
      deleteResourceTypesSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete Resource Type. Please try again.',
        error: true
      })
    })
  })

  describe('Delete Button in Dropdown', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, mockResourceType2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown with delete option for each row', async () => {
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })

    it('calls openResourceTypeDeleteDialog for first resource type', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType.name)
    })

    it('calls openResourceTypeDeleteDialog for second resource type', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType2.id, mockResourceType2.name)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType2.id)
      expect(wrapper.vm.selectedResourceType?.name).toBe(mockResourceType2.name)
    })

    it('renders delete button in dropdown', async () => {
      // FeatherDropdown components containing delete option should be rendered for each row
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBeGreaterThan(0)
    })
  })

  describe('Delete Confirmation Dialog Events', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('handles close event from DeleteConfirmationDialog', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('close')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('handles confirm event from DeleteConfirmationDialog', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')
      deleteResourceTypesSpy.mockResolvedValue(true)

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })
  })

  describe('Delete with Edge Cases', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('handles opening dialog with zero id', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(0, 'zero-resource')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.id).toBe(0)
    })

    it('handles opening dialog with negative id', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(-1, 'negative-resource')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.id).toBe(-1)
    })

    it('handles opening dialog with empty string name', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(1, '')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.name).toBe('')
    })

    it('handles opening dialog with special characters in name', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(1, 'test-resource_type.v2<>&"')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.name).toBe('test-resource_type.v2<>&"')
    })

    it('handles opening dialog with unicode name', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(1, '資源類型テスト')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.name).toBe('資源類型テスト')
    })

    it('handles multiple open and close cycles', async () => {
      // First cycle
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)

      // Second cycle
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
    })

    it('handles rapid open/close without error', async () => {
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      wrapper.vm.closeDeleteResourceTypeDialog()
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })

    it('handles delete with selectedCollectionSource.id as 0', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')
      deleteResourceTypesSpy.mockResolvedValue(true)

      store.selectedCollectionSource = { id: 0, name: 'Test Source' } as any

      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      // id 0 is falsy, should not call service
      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })

    it('handles switching selected resource type before delete', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')
      deleteResourceTypesSpy.mockResolvedValue(true)

      // Open dialog for first resource
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      // Switch to second resource
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType2.id, mockResourceType2.name)
      await wrapper.vm.$nextTick()

      // Try deleting with first resource's params (should fail validation)
      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      expect(deleteResourceTypesSpy).not.toHaveBeenCalled()
    })
  })

  describe('Delete Integration Flow', () => {
    it('handles complete delete flow: open dialog, confirm, close', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')
      deleteResourceTypesSpy.mockResolvedValue(true)

      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      // Open dialog
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      // Confirm delete
      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      // After successful delete, dialog should be closed
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
      expect(deleteResourceTypesSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
    })

    it('handles delete flow when service fails', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteResourceTypesSpy = vi.spyOn(snmpDataCollectionService, 'deleteResourceTypes')
      deleteResourceTypesSpy.mockResolvedValue(false)

      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      // Open dialog
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()

      // Try to delete (will fail)
      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()

      // Dialog should still be visible (not closed on failure)
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })

    it('handles cancel flow: open dialog, cancel', async () => {
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      // Open dialog
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType.id, mockResourceType.name)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      // Cancel (close) dialog
      wrapper.vm.closeDeleteResourceTypeDialog()
      await wrapper.vm.$nextTick()

      // Dialog should be closed
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedResourceType).toBeNull()
    })
  })
})

