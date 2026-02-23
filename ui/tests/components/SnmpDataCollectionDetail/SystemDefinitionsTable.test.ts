import SystemDefinitionsTable from '@/components/SnmpDataCollectionDetail/SystemDefinitionsTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionSystemDef } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'

vi.mock('./Drawer/SystemDefinitionCreationDrawer.vue', () => ({
  default: {
    template: '<div data-test="system-definition-creation-drawer"></div>'
  }
}))

describe('SystemDefinitionsTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>
  let mockSystemDef: SnmpCollectionSystemDef
  let mockSystemDef2: SnmpCollectionSystemDef
  let disabledSystemDef: SnmpCollectionSystemDef

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionDetailStore(pinia)

    store.systemDefinitions = []
    store.systemDefsSearchTerm = ''
    store.systemDefsPagination = { page: 1, pageSize: 10, total: 0 }
    store.systemDefsSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchSystemDefinitions = vi.fn().mockResolvedValue(undefined)
    store.resetSystemDefinitionsFilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeSystemDefsSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onSystemDefsPageChange = vi.fn().mockResolvedValue(undefined)
    store.onSystemDefsPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onSystemDefsSortChange = vi.fn().mockResolvedValue(undefined)
    store.openSystemDefCreationDrawer = vi.fn()

    mockSystemDef = {
      id: 1,
      name: 'Net-SNMP',
      sysoid: '.1.3.6.1.4.1.8072.3.2.10',
      sysoidMask: '.1.3.6.1.4.1.8072',
      ipAddresses: '[]',
      ipAddressMasks: '[]',
      mibGroupNames: ['mib2-interfaces', 'mib2-host-resources-storage'],
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    mockSystemDef2 = {
      id: 2,
      name: 'Cisco-Router',
      sysoid: '.1.3.6.1.4.1.9.1.1',
      sysoidMask: '.1.3.6.1.4.1.9',
      ipAddresses: '["192.168.1.1"]',
      ipAddressMasks: '["255.255.255.0"]',
      mibGroupNames: ['cisco-memory-pool', 'cisco-cpu'],
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    disabledSystemDef = {
      id: 3,
      name: 'Disabled-Device',
      sysoid: '.1.3.6.1.4.1.12345',
      sysoidMask: '.1.3.6.1.4.1.12345',
      ipAddresses: '[]',
      ipAddressMasks: '[]',
      mibGroupNames: ['disabled-mib'],
      enabled: false,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    wrapper = mount(SystemDefinitionsTable, {
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

    it('should display the title "System Definitions"', () => {
      const title = wrapper.find('.title')
      expect(title.exists()).toBe(true)
      expect(title.text()).toBe('System Definitions')
    })

    it('should render the search input', () => {
      const searchInput = wrapper.find('[data-test="search-input"]')
      expect(searchInput.exists()).toBe(true)
    })

    it('should render the refresh button', () => {
      const refreshButton = wrapper.find('[data-test="refresh-button"]')
      expect(refreshButton.exists()).toBe(true)
    })

    it('should call fetchSystemDefinitions on mount', () => {
      expect(store.fetchSystemDefinitions).toHaveBeenCalledTimes(1)
    })

    it('should render within a TableCard container', () => {
      expect(wrapper.find('.system-definitions-table-container').exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('should not render the table when systemDefinitions is empty', async () => {
      store.systemDefinitions = []
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(false)
    })

    it('should still show header with search and refresh when empty', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('should display EmptyList component with correct message when no data', async () => {
      store.systemDefinitions = []
      await wrapper.vm.$nextTick()

      // EmptyList is rendered when there's no data
      const emptyMessage = wrapper.text()
      expect(emptyMessage).toContain('No System Definitions found.')
    })

    it('should not show pagination when systemDefinitions is empty', async () => {
      store.systemDefinitions = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })
  })

  describe('Add System Definition Button', () => {
    it('should render add button and have correct attributes', () => {
      const addButton = wrapper.find('[data-test="add-system-definition-button"]')
      expect(addButton.exists()).toBe(true)
      expect(addButton.text()).toBe('Add System Definition')
    })

    it('should call openSystemDefCreationDrawer with Create mode when clicked', async () => {
      const spy = vi.spyOn(store, 'openSystemDefCreationDrawer')
      const addButton = wrapper.find('[data-test="add-system-definition-button"]')
      await addButton.trigger('click')
      await wrapper.vm.$nextTick()

      expect(spy).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render the table when systemDefinitions has data', () => {
      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('should render correct number of data rows', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('should display system definition name correctly', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockSystemDef.name)
    })

    it('should display system definition sysoid correctly', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockSystemDef.sysoid)
    })

    it('should display system definition sysoidMask correctly', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockSystemDef.sysoidMask)
    })

    it('should display "Enabled" for enabled system definitions', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Enabled')
    })

    it('should display "Disabled" for disabled system definitions', async () => {
      store.systemDefinitions = [disabledSystemDef]
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
        { id: 'sysoid', label: 'SysOID' },
        { id: 'sysoidMask', label: 'SysOID Mask' },
        { id: 'enabled', label: 'Status' }
      ]

      it.each(expectedColumns)('should render column header with correct property for $label', ({ id, label }) => {
        const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
        const header = sortHeaders.find((h) => h.props('property') === id)
        expect(header).toBeDefined()
        expect(header?.text()).toContain(label)
      })
    })
  })

  describe('Expand/Collapse Functionality', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
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
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)
    })

    it('should show expanded content when row is expanded', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.expanded-content').exists()).toBe(true)
    })

    it('should show Mib Group Names header in expanded content', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Mib Group Names:')
    })

    it('should display mib group names in expanded content', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('mib2-interfaces')
      expect(expandedContent.text()).toContain('mib2-host-resources-storage')
    })

    it('should collapse row when toggleExpand is called again', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockSystemDef.id)
    })

    it('should hide expanded content when row is collapsed', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(false)
    })

    it('should allow multiple rows to be expanded simultaneously', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      wrapper.vm.toggleExpand(mockSystemDef2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef2.id)
    })

    it('should have colspan of 5 on expanded content cell', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content td')
      expect(expandedContent.attributes('colspan')).toBe('5')
    })

    it('should display different mib group names for different system definitions', async () => {
      wrapper.vm.toggleExpand(mockSystemDef2.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('cisco-memory-pool')
      expect(expandedContent.text()).toContain('cisco-cpu')
    })
  })

  describe('Search Functionality', () => {
    it('should have search input bound to store.systemDefsSearchTerm', async () => {
      store.systemDefsSearchTerm = 'Net-SNMP'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('Net-SNMP')
    })

    it('should debounce search input', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith('test')
    })

    it('should not trigger search before debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).not.toHaveBeenCalled()
    })

    it('should call onChangeSystemDefsSearchTerm after debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('search term')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith('search term')
    })

    it('should handle empty search term', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith('')
    })

    describe('Search Term Variations', () => {
      const searchTerms = [
        { term: 'Net-SNMP', description: 'with hyphen' },
        { term: 'cisco', description: 'lowercase' },
        { term: 'ROUTER', description: 'uppercase' },
        { term: 'special@chars#', description: 'special characters' },
        { term: '123numeric456', description: 'numeric' },
        { term: '.1.3.6.1.4', description: 'OID format' }
      ]

      it.each(searchTerms)('should handle search with $description: "$term"', async ({ term }) => {
        const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
        await searchInput.setValue(term)
        vi.advanceTimersByTime(500)
        await wrapper.vm.$nextTick()

        expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith(term)
      })
    })
  })

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    describe('Sort Direction Changes', () => {
      const sortDirections = [
        { value: 'asc', expected: { property: 'name', order: 'asc' } },
        { value: 'desc', expected: { property: 'name', order: 'desc' } }
      ]

      it.each(sortDirections)(
        'should call onSystemDefsSortChange with $value direction',
        async ({ value, expected }) => {
          const sortHeader = wrapper.findComponent(FeatherSortHeader)
          await sortHeader.vm.$emit('sort-changed', { property: 'name', value })
          await wrapper.vm.$nextTick()

          expect(store.onSystemDefsSortChange).toHaveBeenCalledWith(expected.property, expected.order)
        }
      )
    })

    describe('Sort Column Changes', () => {
      const sortColumns = [
        { property: 'name', label: 'Name' },
        { property: 'sysoid', label: 'SysOID' },
        { property: 'sysoidMask', label: 'SysOID Mask' },
        { property: 'enabled', label: 'Status' }
      ]

      it.each(sortColumns)('should sort by $property column', async ({ property }) => {
        const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
        const targetHeader = sortHeaders.find((h) => h.props('property') === property)

        await targetHeader?.vm.$emit('sort-changed', { property, value: 'asc' })
        await wrapper.vm.$nextTick()

        expect(store.onSystemDefsSortChange).toHaveBeenCalledWith(property, 'asc')
      })
    })

    it('should reset to default sort when sort value is NONE', async () => {
      const sortHeader = wrapper.findComponent(FeatherSortHeader)
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: SORT.NONE })
      await wrapper.vm.$nextTick()

      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('should reset all sort states when a new column is sorted', async () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)

      // Sort by name first
      await sortHeaders[0].vm.$emit('sort-changed', { property: 'name', value: 'asc' })
      await wrapper.vm.$nextTick()

      // Then sort by sysoid
      await sortHeaders[1].vm.$emit('sort-changed', { property: 'sysoid', value: 'desc' })
      await wrapper.vm.$nextTick()

      expect(store.onSystemDefsSortChange).toHaveBeenLastCalledWith('sysoid', 'desc')
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
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 50 }
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

    it('should call onSystemDefsPageChange when page changes', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      await wrapper.vm.$nextTick()

      expect(store.onSystemDefsPageChange).toHaveBeenCalledWith(2)
    })

    it('should call onSystemDefsPageSizeChange when page size changes', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', 20)
      await wrapper.vm.$nextTick()

      expect(store.onSystemDefsPageSizeChange).toHaveBeenCalledWith(20)
    })

    describe('Page Size Changes', () => {
      const pageSizes = [10, 20, 30]

      it.each(pageSizes)('should handle page size change to %i', async (pageSize) => {
        const pagination = wrapper.findComponent(FeatherPagination)
        await pagination.vm.$emit('update:pageSize', pageSize)
        await wrapper.vm.$nextTick()

        expect(store.onSystemDefsPageSizeChange).toHaveBeenCalledWith(pageSize)
      })
    })

    describe('Page Navigation', () => {
      const pages = [1, 2, 3, 4, 5]

      it.each(pages)('should handle navigation to page %i', async (page) => {
        const pagination = wrapper.findComponent(FeatherPagination)
        await pagination.vm.$emit('update:modelValue', page)
        await wrapper.vm.$nextTick()

        expect(store.onSystemDefsPageChange).toHaveBeenCalledWith(page)
      })
    })
  })

  describe('Refresh Functionality', () => {
    it('should call resetSystemDefinitionsFilters when refresh button is clicked', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetSystemDefinitionsFilters).toHaveBeenCalledTimes(1)
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render edit button for each row', () => {
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons.length).toBe(2)
    })

    it('should call openSystemDefCreationDrawer when edit button is clicked', async () => {
      const spy = vi.spyOn(store, 'openSystemDefCreationDrawer')
      const editButton = wrapper.find('[data-test="edit-button"]')
      await editButton.trigger('click')
      await wrapper.vm.$nextTick()

      expect(spy).toHaveBeenCalled()
      expect(spy).toHaveBeenCalledWith(mockSystemDef, CreateEditMode.Edit)
    })
  })

  describe('Dropdown Menu', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, disabledSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render dropdown component for each row', () => {
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })
  })

  describe('Status Display', () => {
    const statusCases = [
      { enabled: true, expectedText: 'Enabled' },
      { enabled: false, expectedText: 'Disabled' }
    ]

    it.each(statusCases)(
      'should display "$expectedText" when enabled is $enabled',
      async ({ enabled, expectedText }) => {
        const systemDef = { ...mockSystemDef, enabled }
        store.systemDefinitions = [systemDef]
        store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
        await wrapper.vm.$nextTick()

        const rows = wrapper.findAll('transition-group-stub tr')
        expect(rows[0].text()).toContain(expectedText)
      }
    )
  })

  describe('System Definition Data Display', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    describe('Column Data Mapping', () => {
      const columnMappings = [
        { field: 'name', value: 'Net-SNMP' },
        { field: 'sysoid', value: '.1.3.6.1.4.1.8072.3.2.10' },
        { field: 'sysoidMask', value: '.1.3.6.1.4.1.8072' },
        { field: 'enabled', value: 'Enabled' }
      ]

      it.each(columnMappings)('should display $field correctly', ({ value }) => {
        const rows = wrapper.findAll('transition-group-stub tr')
        expect(rows[0].text()).toContain(value)
      })
    })

    describe('Expanded Content Data', () => {
      beforeEach(async () => {
        wrapper.vm.toggleExpand(mockSystemDef.id)
        await wrapper.vm.$nextTick()
      })

      it('should display mib group names correctly', () => {
        const expandedContent = wrapper.find('.expanded-content')
        expect(expandedContent.text()).toContain('mib2-interfaces')
        expect(expandedContent.text()).toContain('mib2-host-resources-storage')
      })

      it('should have Mib Group Names header', () => {
        const expandedContent = wrapper.find('.expanded-content')
        const header = expandedContent.find('h6')
        expect(header.text()).toBe('Mib Group Names:')
      })
    })
  })

  describe('Multiple System Definitions', () => {
    it('should render multiple system definition rows correctly', async () => {
      const count = 5
      const systemDefinitions: SnmpCollectionSystemDef[] = Array.from({ length: count }, (_, i) => ({
        ...mockSystemDef,
        id: i + 1,
        name: `system-def-${i + 1}`,
        sysoid: `.1.3.6.1.4.1.${i + 1}`
      }))

      store.systemDefinitions = systemDefinitions
      store.systemDefsPagination = { page: 1, pageSize: 10, total: count }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(count)
    })
  })

  describe('Edge Cases', () => {
    it('should handle empty mib group names', async () => {
      const emptyMibDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        mibGroupNames: []
      }

      store.systemDefinitions = [emptyMibDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(emptyMibDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.exists()).toBe(true)
    })

    it('should handle system definition with all empty fields', async () => {
      const emptyFieldsDef: SnmpCollectionSystemDef = {
        id: 99,
        name: '',
        sysoid: '',
        sysoidMask: '',
        ipAddresses: '[]',
        ipAddressMasks: '[]',
        mibGroupNames: [],
        enabled: false,
        collectionSourceId: 1,
        collectionSourceName: 'Test Source'
      }

      store.systemDefinitions = [emptyFieldsDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
    })

    it('should handle special characters in system definition name', async () => {
      const specialCharsDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: 100,
        name: 'Test <Device> & "Special" Characters'
      }

      store.systemDefinitions = [specialCharsDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Test <Device> & "Special" Characters')
    })

    it('should handle unicode characters in system definition fields', async () => {
      const unicodeDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: 101,
        name: '测试设备 日本語 العربية',
        mibGroupNames: ['国际化-mib', 'юникод-группа']
      }

      store.systemDefinitions = [unicodeDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('测试设备 日本語 العربية')
    })

    it('should handle very large number of mib group names', async () => {
      const manyMibsDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: 102,
        mibGroupNames: Array.from({ length: 50 }, (_, i) => `mib-group-${i + 1}`)
      }

      store.systemDefinitions = [manyMibsDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(manyMibsDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('mib-group-1')
      expect(expandedContent.text()).toContain('mib-group-50')
    })

    it('should handle very long OID strings', async () => {
      const longOidDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: 103,
        sysoid: '.1.3.6.1.4.1.9.9.166.1.1.1.1.4.1.2.3.4.5.6.7.8.9.10',
        sysoidMask: '.1.3.6.1.4.1.9.9.166.1.1.1.1.4'
      }

      store.systemDefinitions = [longOidDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('.1.3.6.1.4.1.9.9.166.1.1.1.1.4.1.2.3.4.5.6.7.8.9.10')
    })

    it('should handle rapid expand/collapse toggles', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Rapidly toggle expand multiple times
      for (let i = 0; i < 5; i++) {
        wrapper.vm.toggleExpand(mockSystemDef.id)
      }
      await wrapper.vm.$nextTick()

      // After odd number of toggles, should be expanded
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)
    })

    it('should preserve expanded state when data updates', async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      // Expand first row
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)

      // Update store data (e.g., from API refresh)
      store.systemDefinitions = [{ ...mockSystemDef, name: 'Updated Name' }, mockSystemDef2]
      await wrapper.vm.$nextTick()

      // Expanded state should be preserved
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)
    })

    it('should handle zero id value', async () => {
      const zeroIdDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: 0,
        name: 'Zero ID Device'
      }

      store.systemDefinitions = [zeroIdDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Zero ID Device')
    })

    it('should handle negative id value', async () => {
      const negativeIdDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: -1,
        name: 'Negative ID Device'
      }

      store.systemDefinitions = [negativeIdDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })
  })

  describe('Accessibility', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    it('should have aria-label on table', () => {
      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBe('Events Table')
    })

    it('should have title attribute on edit button', () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toContain('Edit')
    })
  })

  describe('TransitionGroup', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('should render TransitionGroup', () => {
      const transitionGroup = wrapper.find('transition-group-stub')
      expect(transitionGroup.exists()).toBe(true)
    })

    it('should have unique keys for each system definition row', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('should handle system definition removal', async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
    })

    it('should handle system definition addition', async () => {
      const newSystemDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: 3,
        name: 'newSystemDef'
      }
      store.systemDefinitions = [mockSystemDef, mockSystemDef2, newSystemDef]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(3)
    })
  })

  describe('Reactivity', () => {
    it('should update table when store.systemDefinitions changes', async () => {
      expect(wrapper.find('.data-table').exists()).toBe(false)

      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('should update pagination when store.systemDefsPagination changes', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 2, pageSize: 20, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(2)
      expect(pagination.props('pageSize')).toBe(20)
      expect(pagination.props('total')).toBe(100)
    })

    it('should update search input when store.systemDefsSearchTerm changes', async () => {
      store.systemDefsSearchTerm = 'newSearch'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('newSearch')
    })
  })

  describe('Integration', () => {
    it('should handle complete user flow: search, sort, paginate', async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      // Search
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('Net-SNMP')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()
      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith('Net-SNMP')

      // Sort
      const sortHeader = wrapper.findComponent(FeatherSortHeader)
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: 'asc' })
      await wrapper.vm.$nextTick()
      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith('name', 'asc')

      // Paginate
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      await wrapper.vm.$nextTick()
      expect(store.onSystemDefsPageChange).toHaveBeenCalledWith(2)
    })

    it('should handle expand and collapse flow', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Expand
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(1)

      // Collapse
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })

    it('should handle refresh and maintain state', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      // Expand a row
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      // Click refresh
      await wrapper.get('[data-test="refresh-button"]').trigger('click')

      expect(store.resetSystemDefinitionsFilters).toHaveBeenCalled()
    })
  })

  describe('Mib Group Names Parsing', () => {
    it('should parse and display single mib group name', async () => {
      const singleGroupDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        mibGroupNames: ['single-group']
      }

      store.systemDefinitions = [singleGroupDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(singleGroupDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('single-group')
    })

    it('should parse and display multiple mib group names separated by commas', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      // mibGroupNames: ['mib2-interfaces', 'mib2-host-resources-storage']
      expect(expandedContent.text()).toContain('mib2-interfaces')
      expect(expandedContent.text()).toContain('mib2-host-resources-storage')
    })

    it('should handle empty mib group names array', async () => {
      const emptyMibGroupsDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        mibGroupNames: []
      }

      store.systemDefinitions = [emptyMibGroupsDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(emptyMibGroupsDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.exists()).toBe(true)
    })
  })

  describe('Delete System Definition Dialog', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('initializes with delete dialog hidden', () => {
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('has openDeleteSystemDefDialog method available', async () => {
      expect(typeof wrapper.vm.openDeleteSystemDefDialog).toBe('function')
    })

    it('opens delete dialog via openDeleteSystemDefDialog method', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef.name)
    })

    it('sets selectedSystemDef correctly when opening dialog', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef.name)
    })

    it('sets selectedSystemDef correctly for different system definitions', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef2.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef2.name)
    })

    it('closes delete dialog and clears selection', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteSystemDefDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('renders DeleteConfirmationDialog component', async () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('passes correct props to DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')?.id).toBe(mockSystemDef.id)
      expect(dialog.props('selected')?.name).toBe(mockSystemDef.name)
      expect(dialog.props('type')).toBe('system-def')
    })

    it('handles opening dialog with null', async () => {
      wrapper.vm.openDeleteSystemDefDialog(null)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })
  })

  describe('Delete System Definition Action', () => {
    let deleteSystemDefinitionsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      deleteSystemDefinitionsSpy = vi.spyOn(snmpDataCollectionService, 'deleteSystemDefinitions')

      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('calls deleteSystemDefinitions service on successful delete', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).toHaveBeenCalledWith(1, [mockSystemDef.id])
    })

    it('closes dialog after successful deletion', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('fetches system definitions after successful deletion', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const fetchSpy = vi.spyOn(store, 'fetchSystemDefinitions')

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(fetchSpy).toHaveBeenCalled()
    })

    it('does not call deleteSystemDefinitions when type does not match', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'wrong-type')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteSystemDefinitions when selected id does not match', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: 999, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteSystemDefinitions when selected name does not match', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: 'wrong-name' }, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteSystemDefinitions when selectedCollectionSource is missing', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteSystemDefinitions when selected is null', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef(null, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })

    it('does not call deleteSystemDefinitions when selected id is missing', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ name: mockSystemDef.name } as any, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })
  })

  describe('Delete System Definition Error Handling', () => {
    let deleteSystemDefinitionsSpy: any
    let showSnackBarSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      deleteSystemDefinitionsSpy = vi.spyOn(snmpDataCollectionService, 'deleteSystemDefinitions')

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
      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      store.fetchSystemDefinitions = vi.fn().mockResolvedValue(undefined)

      wrapper = mount(SystemDefinitionsTable, {
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
      deleteSystemDefinitionsSpy.mockResolvedValue(false)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete System Definition '${mockSystemDef.name}'.`,
        error: true
      })
    })

    it('shows error snackbar when validation fails', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: 999, name: 'wrong-name' }, 'system-def')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'Failed to delete System Definition \'wrong-name\'.',
        error: true
      })
    })

    it('shows success snackbar when deletion succeeds', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `System Definition '${mockSystemDef.name}' deleted successfully.`
      })
    })

    it('shows error when type mismatch occurs', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'wrong-type')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete System Definition '${mockSystemDef.name}'.`,
        error: true
      })
    })

    it('shows error when selected collection source is missing', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: `Failed to delete System Definition '${mockSystemDef.name}'.`,
        error: true
      })
    })
  })

  describe('Delete Button in Dropdown', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown with delete option for each row', async () => {
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBe(2)
    })

    it('has closeDeleteSystemDefDialog method available', async () => {
      expect(typeof wrapper.vm.closeDeleteSystemDefDialog).toBe('function')
    })

    it('calls openDeleteSystemDefDialog for first system definition', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef.name)
    })

    it('calls openDeleteSystemDefDialog for second system definition', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef2.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef2.name)
    })

    it('renders delete button in dropdown', async () => {
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns.length).toBeGreaterThan(0)
    })
  })

  describe('Delete Confirmation Dialog Events', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('handles close event from DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('close')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('handles confirm event from DeleteConfirmationDialog', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteSystemDefinitionsSpy = vi.spyOn(snmpDataCollectionService, 'deleteSystemDefinitions')
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).toHaveBeenCalledWith(1, [mockSystemDef.id])
    })
  })

  describe('Delete with Edge Cases', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('handles opening dialog with object having zero id', async () => {
      wrapper.vm.openDeleteSystemDefDialog({ id: 0, name: 'zero-def' })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.id).toBe(0)
    })

    it('handles opening dialog with object having negative id', async () => {
      wrapper.vm.openDeleteSystemDefDialog({ id: -1, name: 'negative-def' })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.id).toBe(-1)
    })

    it('handles opening dialog with empty string name', async () => {
      wrapper.vm.openDeleteSystemDefDialog({ id: 1, name: '' })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.name).toBe('')
    })

    it('handles opening dialog with special characters in name', async () => {
      wrapper.vm.openDeleteSystemDefDialog({ id: 1, name: 'test-def_v2<>&"' })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.name).toBe('test-def_v2<>&"')
    })

    it('handles opening dialog with unicode name', async () => {
      wrapper.vm.openDeleteSystemDefDialog({ id: 1, name: 'システム定義テスト' })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.name).toBe('システム定義テスト')
    })

    it('handles multiple open and close cycles', async () => {
      // First cycle
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteSystemDefDialog()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)

      // Second cycle
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteSystemDefDialog()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
    })

    it('handles rapid open/close without error', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      wrapper.vm.closeDeleteSystemDefDialog()
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      wrapper.vm.closeDeleteSystemDefDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('handles delete with selectedCollectionSource.id as 0', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteSystemDefinitionsSpy = vi.spyOn(snmpDataCollectionService, 'deleteSystemDefinitions')
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      store.selectedCollectionSource = { id: 0, name: 'Test Source' } as any

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      // id 0 is falsy, should not call service
      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })

    it('handles switching selected system definition before delete', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteSystemDefinitionsSpy = vi.spyOn(snmpDataCollectionService, 'deleteSystemDefinitions')
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      // Open dialog for first system definition
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      // Switch to second system definition
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef2)
      await wrapper.vm.$nextTick()

      // Try deleting with first system definition's params (should fail validation)
      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })
  })

  describe('Delete Integration Flow', () => {
    it('handles complete delete flow: open dialog, confirm, close', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteSystemDefinitionsSpy = vi.spyOn(snmpDataCollectionService, 'deleteSystemDefinitions')
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      // Open dialog
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      // Confirm delete
      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      // After successful delete, dialog should be closed
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
      expect(deleteSystemDefinitionsSpy).toHaveBeenCalledWith(1, [mockSystemDef.id])
    })

    it('handles delete flow when service fails', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const deleteSystemDefinitionsSpy = vi.spyOn(snmpDataCollectionService, 'deleteSystemDefinitions')
      deleteSystemDefinitionsSpy.mockResolvedValue(false)

      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      // Open dialog
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      // Try to delete (will fail)
      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      // Dialog should still be visible (not closed on failure)
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })

    it('handles cancel flow: open dialog, cancel', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()

      // Open dialog
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      // Cancel (close) dialog
      wrapper.vm.closeDeleteSystemDefDialog()
      await wrapper.vm.$nextTick()

      // Dialog should be closed
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })
  })
})

