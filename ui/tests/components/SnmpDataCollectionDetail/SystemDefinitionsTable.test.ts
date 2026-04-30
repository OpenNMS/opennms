import SystemDefinitionsTable from '@/components/SnmpDataCollectionDetail/SystemDefinitionsTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionSystemDef } from '@/types/snmpDataCollection'
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
      ipAddresses: [],
      ipAddressMasks: [],
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
      ipAddresses: ["192.168.1.1"],
      ipAddressMasks: ["255.255.255.0"],
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
      ipAddresses: [],
      ipAddressMasks: [],
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
    it('renders correctly with system-definitions-table-container', () => {
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('.system-definitions-table-container').exists()).toBe(true)
    })

    it('renders header with search, refresh, and add button', () => {
      expect(wrapper.find('.header .section-left').exists()).toBe(true)
      expect(wrapper.find('.header .section-right').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-system-definition-button"]').exists()).toBe(true)
    })

    it('renders add button with correct text', () => {
      const addButton = wrapper.find('[data-test="add-system-definition-button"]')
      expect(addButton.text()).toBe('Add System Definition')
    })

    it('renders search input with correct props', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('label')).toBe('Search')
      expect(searchInput.props('type')).toBe('search')
      expect(searchInput.props('hint')).toBe('Search by Name')
    })

    it('renders DeleteConfirmationDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('renders SystemDefinitionCreationDrawer component', () => {
      const drawer = wrapper.findComponent({ name: 'SystemDefinitionCreationDrawer' })
      expect(drawer.exists()).toBe(true)
    })

    it('has container section for table and pagination', () => {
      expect(wrapper.find('.container').exists()).toBe(true)
    })
  })

  describe('Add System Definition Button', () => {
    it('calls openSystemDefCreationDrawer with Create mode when clicked', async () => {
      const addButton = wrapper.find('[data-test="add-system-definition-button"]')
      await addButton.trigger('click')

      expect(store.openSystemDefCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('can be clicked multiple times', async () => {
      const addButton = wrapper.find('[data-test="add-system-definition-button"]')
      await addButton.trigger('click')
      await addButton.trigger('click')
      await addButton.trigger('click')

      expect(store.openSystemDefCreationDrawer).toHaveBeenCalledTimes(3)
    })
  })

  describe('Empty State', () => {
    it('does not render table or pagination when systemDefinitions are empty', async () => {
      store.systemDefinitions = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(false)
      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('displays EmptyList component with correct message when no data', async () => {
      store.systemDefinitions = []
      await wrapper.vm.$nextTick()

      const emptyList = wrapper.findComponent({ name: 'EmptyList' })
      expect(emptyList.exists()).toBe(true)
      expect(emptyList.props('content')).toEqual({ msg: 'No System Definitions found.' })
    })

    it('still shows header elements when empty', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-system-definition-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('shows table then hides when data is cleared', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(true)

      store.systemDefinitions = []
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(false)
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('renders table with aria-label', () => {
      const table = wrapper.find('.data-table')
      expect(table.exists()).toBe(true)
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('renders correct number of rows', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('renders system definition data in row', () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(mockSystemDef.name)
      expect(rows[0].text()).toContain(mockSystemDef.sysoid)
      expect(rows[0].text()).toContain(mockSystemDef.sysoidMask)
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

    it('renders 4 sortable column headers plus Actions', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBe(4)

      const ths = wrapper.findAll('th')
      const actionsHeader = ths.filter((th) => th.text() === 'Actions')
      expect(actionsHeader.length).toBe(1)
    })

    it.each([
      { count: 1, expectedMinRows: 1 },
      { count: 5, expectedMinRows: 5 },
      { count: 10, expectedMinRows: 10 }
    ])(
      'renders at least $expectedMinRows rows when $count system definitions exist',
      async ({ count, expectedMinRows }) => {
        const systemDefinitions = Array.from({ length: count }, (_, i) => ({
          ...mockSystemDef,
          id: i + 1,
          name: `System Definition ${i + 1}`
        }))
        store.systemDefinitions = systemDefinitions
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
      expect(searchInput.props('hint')).toBe('Search by Name')
      expect(searchInput.props('label')).toBe('Search')
      expect(searchInput.props('type')).toBe('search')
    })

    it('does not call onChangeSystemDefsSearchTerm before debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).not.toHaveBeenCalled()
    })

    it('calls onChangeSystemDefsSearchTerm after debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('search term')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith('search term')
    })

    it.each([
      { term: '' },
      { term: 'Net-SNMP' },
      { term: 'cisco-router' },
      { term: 'special@chars#' },
      { term: '.1.3.6.1.4' },
      { term: '测试设备' }
    ])('handles search term "$term" correctly', async ({ term }) => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue(term)
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith(term)
    })

    it('debounces rapid input changes - only last value triggers call', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('a')
      await searchInput.setValue('ab')
      await searchInput.setValue('abc')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith('abc')
    })

    it('reflects store search term in input', async () => {
      store.systemDefsSearchTerm = 'test search'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('test search')
    })
  })

  describe('Refresh Button', () => {
    it('calls resetSystemDefinitionsFilters when clicked', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetSystemDefinitionsFilters).toHaveBeenCalledTimes(1)
    })

    it('can be clicked multiple times', async () => {
      const refreshButton = wrapper.get('[data-test="refresh-button"]')
      await refreshButton.trigger('click')
      await refreshButton.trigger('click')
      await refreshButton.trigger('click')

      expect(store.resetSystemDefinitionsFilters).toHaveBeenCalledTimes(3)
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('renders edit button for each row with correct title', () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.exists()).toBe(true)
      expect(editButton.attributes('title')).toContain('Edit')
    })

    it('calls openSystemDefCreationDrawer with Edit mode when clicked', async () => {
      const editButton = wrapper.find('[data-test="edit-button"]')
      await editButton.trigger('click')

      expect(store.openSystemDefCreationDrawer).toHaveBeenCalledWith(mockSystemDef, CreateEditMode.Edit)
    })

    it('renders multiple edit buttons and calls with correct system definition', async () => {
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons.length).toBe(2)

      await editButtons[1].trigger('click')
      expect(store.openSystemDefCreationDrawer).toHaveBeenCalledWith(mockSystemDef2, CreateEditMode.Edit)
    })
  })

  describe('Expand/Collapse Functionality', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()
    })

    it('initializes with no expanded rows', () => {
      expect(wrapper.vm.expandedRows).toEqual([])
    })

    it('toggles row expansion when toggle function is called', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockSystemDef.id)
    })

    it('can expand multiple rows', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      wrapper.vm.toggleExpand(mockSystemDef2.id)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef2.id)
      expect(wrapper.findAll('.expanded-content').length).toBe(2)
    })

    it('shows expanded content when row is expanded', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)
    })

    it('hides expanded content when row is collapsed', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(true)

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expanded-content').exists()).toBe(false)
    })

    it('displays mib group names in expanded content', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Mib Group Names')
      expect(expandedContent.text()).toContain('mib2-interfaces')
      expect(expandedContent.text()).toContain('mib2-host-resources-storage')
    })

    it('handles rapid toggle clicks correctly', async () => {
      wrapper.vm.toggleExpand(mockSystemDef.id)
      wrapper.vm.toggleExpand(mockSystemDef.id)
      wrapper.vm.toggleExpand(mockSystemDef.id)
      wrapper.vm.toggleExpand(mockSystemDef.id)
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      // After 5 toggles, should be expanded (odd number)
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)
    })

    it('handles toggling non-existent id', async () => {
      wrapper.vm.toggleExpand(999)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(999)
    })
  })

  describe('Expanded Content Details', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
    })

    it('displays proper headers and structure', async () => {
      const expandedContent = wrapper.find('.expanded-content')
      expect(expandedContent.text()).toContain('Mib Group Names')

      const td = expandedContent.find('td')
      expect(td.attributes('colspan')).toBe('5')
    })

    it('displays mib group names with comma separator', async () => {
      const description = wrapper.find('.expanded-content .description')
      expect(description.exists()).toBe(true)
      expect(description.text()).toContain('mib2-interfaces, mib2-host-resources-storage')
    })

    it('displays h6 header for section title', async () => {
      const h6 = wrapper.find('.expanded-content h6')
      expect(h6.exists()).toBe(true)
      expect(h6.text()).toBe('Mib Group Names:')
    })

    it('handles empty mib group names array', async () => {
      const emptyMibGroupsDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        id: 99,
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

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
    })

    it('renders 4 sort headers for columns', () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBe(4)
    })

    it('initializes sort state with NONE for all columns', () => {
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.sysoid).toBe(SORT.NONE)
      expect(wrapper.vm.sort.sysoidMask).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('handles sort reset to default when value is none', () => {
      wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })
      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it.each([
      { property: 'name', sortOrder: 'asc' },
      { property: 'name', sortOrder: 'desc' },
      { property: 'sysoid', sortOrder: 'asc' },
      { property: 'sysoid', sortOrder: 'desc' },
      { property: 'sysoidMask', sortOrder: 'asc' },
      { property: 'sysoidMask', sortOrder: 'desc' },
      { property: 'enabled', sortOrder: 'asc' },
      { property: 'enabled', sortOrder: 'desc' }
    ])('handles sorting by $property with $sortOrder order', async ({ property, sortOrder }) => {
      wrapper.vm.sortChanged({ property, value: sortOrder })
      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith(property, sortOrder)
      expect(wrapper.vm.sort[property]).toBe(sortOrder)
    })

    it('resets all sorts when changing sort column', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')

      wrapper.vm.sortChanged({ property: 'sysoid', value: 'desc' })
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.sysoid).toBe('desc')
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 50 }
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
        expect(store.onSystemDefsPageChange).toHaveBeenCalledWith(page)
      }
    )

    it.each([{ pageSize: 10 }, { pageSize: 20 }, { pageSize: 30 }])(
      'handles page size change to $pageSize',
      async ({ pageSize }) => {
        const pagination = wrapper.getComponent(FeatherPagination)
        await pagination.vm.$emit('update:pageSize', pageSize)
        expect(store.onSystemDefsPageSizeChange).toHaveBeenCalledWith(pageSize)
      }
    )

    it('reflects store pagination in component', async () => {
      store.systemDefsPagination = { page: 3, pageSize: 20, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(3)
      expect(pagination.props('pageSize')).toBe(20)
      expect(pagination.props('total')).toBe(100)
    })
  })

  describe('Dropdown Actions', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown and action container for each row', async () => {
      expect(wrapper.find('.action-container').exists()).toBe(true)
      expect(wrapper.findComponent(FeatherDropdown).exists()).toBe(true)
    })

    it('renders multiple dropdowns for multiple rows', async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
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
      expect(columnIds).toContain('sysoid')
      expect(columnIds).toContain('sysoidMask')
      expect(columnIds).toContain('enabled')
    })

    it.each([
      { id: 'name', label: 'Name' },
      { id: 'sysoid', label: 'SysOID' },
      { id: 'sysoidMask', label: 'SysOID Mask' },
      { id: 'enabled', label: 'Status' }
    ])('has column "$label" with id "$id"', ({ id, label }) => {
      const col = wrapper.vm.columns.find((c: any) => c.id === id)
      expect(col).toBeDefined()
      expect(col.label).toBe(label)
    })

    it('renders Actions column header (non-sortable)', async () => {
      store.systemDefinitions = [mockSystemDef]
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
        const systemDef = { ...mockSystemDef, enabled }
        store.systemDefinitions = [systemDef]
        store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
        await wrapper.vm.$nextTick()

        const statusTag = wrapper.find('[data-test="status-tag"]')
        expect(statusTag.exists()).toBe(true)
        expect(statusTag.text()).toBe(expectedText)
        expect(statusTag.classes()).toContain(expectedClass)
      }
    )

    it('renders mixed enabled/disabled states correctly', async () => {
      store.systemDefinitions = [mockSystemDef, disabledSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips.length).toBe(2)
      expect(chips[0].classes()).toContain('enabled-tag')
      expect(chips[1].classes()).toContain('disabled-tag')
    })
  })

  describe('Edge Cases', () => {
    it('handles system definition with empty string fields', async () => {
      const emptySystemDef: SnmpCollectionSystemDef = {
        id: 1,
        name: '',
        sysoid: '',
        sysoidMask: '',
        ipAddresses: [],
        ipAddressMasks: [],
        mibGroupNames: [],
        enabled: true,
        collectionSourceId: 1,
        collectionSourceName: ''
      }

      store.systemDefinitions = [emptySystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('transition-group-stub tr').length).toBeGreaterThanOrEqual(1)
    })

    it('handles special characters in fields', async () => {
      const specialSystemDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        name: 'test-system_def.v2<>&"',
        sysoid: '.1.3.6.1.4.1.test<>&'
      }

      store.systemDefinitions = [specialSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const row = wrapper.findAll('transition-group-stub tr')[0]
      expect(row.text()).toContain('test-system_def.v2<>&"')
    })

    it('handles unicode characters in fields', async () => {
      const unicodeSystemDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        name: '测试设备 日本語 العربية',
        mibGroupNames: ['国际化-mib', 'юникод-группа']
      }

      store.systemDefinitions = [unicodeSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const row = wrapper.findAll('transition-group-stub tr')[0]
      expect(row.text()).toContain('测试设备 日本語 العربية')
    })

    it('handles pagination with zero total', async () => {
      store.systemDefinitions = []
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 0 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })

    it('handles large pagination total counts', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 100000 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(100000)
    })

    it('preserves expanded state when data updates', async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)

      const newSystemDef: SnmpCollectionSystemDef = { ...mockSystemDef, id: 10, name: 'newItem' }
      store.systemDefinitions = [mockSystemDef, mockSystemDef2, newSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 3 }
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)
    })

    it('hides expanded content when data is cleared', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()

      store.systemDefinitions = []
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })

    it('displays many mib group names in expanded content', async () => {
      const manyMibsDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        mibGroupNames: Array.from({ length: 50 }, (_, i) => `mib-group-${i + 1}`)
      }

      store.systemDefinitions = [manyMibsDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(manyMibsDef.id)
      await wrapper.vm.$nextTick()

      const expanded = wrapper.find('.expanded-content')
      expect(expanded.text()).toContain('mib-group-1')
      expect(expanded.text()).toContain('mib-group-50')
    })

    it('handles very long OID strings', async () => {
      const longOidDef: SnmpCollectionSystemDef = {
        ...mockSystemDef,
        sysoid: '.1.3.6.1.4.1.9.9.166.1.1.1.1.4.1.2.3.4.5.6.7.8.9.10',
        sysoidMask: '.1.3.6.1.4.1.9.9.166.1.1.1.1.4'
      }

      store.systemDefinitions = [longOidDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const row = wrapper.findAll('transition-group-stub tr')[0]
      expect(row.text()).toContain('.1.3.6.1.4.1.9.9.166.1.1.1.1.4.1.2.3.4.5.6.7.8.9.10')
    })
  })

  describe('Accessibility', () => {
    it('table has aria-label attribute', async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()

      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('sort headers have col scope', async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBeGreaterThan(0)
      sortHeaders.forEach((header) => {
        expect(header.attributes('scope')).toBe('col')
      })
    })

    it('edit button has title attribute', async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()

      const editButton = wrapper.find('[data-test="edit-button"]')
      expect(editButton.attributes('title')).toContain('Edit')
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

    it('opens delete dialog and sets selectedSystemDef correctly', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef.name)
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

    it('passes correct props to DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')?.id).toBe(mockSystemDef.id)
      expect(dialog.props('selected')?.name).toBe(mockSystemDef.name)
      expect(dialog.props('type')).toBe('system-def')
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

    it('closes dialog and fetches data after successful deletion', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const fetchSpy = vi.spyOn(store, 'fetchSystemDefinitions')

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
      expect(fetchSpy).toHaveBeenCalled()
    })

    it.each([
      { desc: 'type does not match', params: { id: 1, name: 'Net-SNMP' }, type: 'wrong-type' },
      { desc: 'selected id does not match', params: { id: 999, name: 'Net-SNMP' }, type: 'system-def' },
      { desc: 'selected name does not match', params: { id: 1, name: 'wrong-name' }, type: 'system-def' }
    ])('does not call deleteSystemDefinitions when $desc', async ({ params, type }) => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef(params, type)
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

    it('does not call deleteSystemDefinitions when selected is null or missing id', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef(null, 'system-def')
      await flushPromises()
      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()

      await wrapper.vm.deleteSystemDef({ name: mockSystemDef.name } as any, 'system-def')
      await flushPromises()
      expect(deleteSystemDefinitionsSpy).not.toHaveBeenCalled()
    })

    it('handles confirm event from DeleteConfirmationDialog', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(true)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(deleteSystemDefinitionsSpy).toHaveBeenCalledWith(1, [mockSystemDef.id])
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

    it('keeps delete dialog open when deletion fails', async () => {
      deleteSystemDefinitionsSpy.mockResolvedValue(false)

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef).not.toBeNull()
    })
  })

  describe('Delete with Edge Cases', () => {
    beforeEach(async () => {
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
    })

    it('handles delete for disabled system definition', async () => {
      store.systemDefinitions = [disabledSystemDef]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteSystemDefDialog(disabledSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.id).toBe(disabledSystemDef.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(disabledSystemDef.name)
    })

    it('handles delete for system definition with special characters in name', async () => {
      const specialSystemDef = {
        ...mockSystemDef,
        id: 10,
        name: 'System<>Def&"Special\'Chars'
      }
      store.systemDefinitions = [specialSystemDef]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteSystemDefDialog(specialSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.name).toBe('System<>Def&"Special\'Chars')
    })

    it('handles delete for system definition with very long name', async () => {
      const longName = 'A'.repeat(200)
      const longNameSystemDef = { ...mockSystemDef, id: 11, name: longName }
      store.systemDefinitions = [longNameSystemDef]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteSystemDefDialog(longNameSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.name).toBe(longName)
    })

    it('handles rapid open/close of delete dialog', async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      wrapper.vm.closeDeleteSystemDefDialog()
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      wrapper.vm.closeDeleteSystemDefDialog()
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef.id)
    })

    it('handles switching selected system definition without closing dialog', async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef2.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef2.name)
    })
  })

  describe('Change Status Dialog - State Management', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()
    })

    it('change status dialog is initially hidden', () => {
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
    })

    it('selected system def is initially null', () => {
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('opens change status dialog and sets selected system def', () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef).toEqual(mockSystemDef)
    })

    it('closes change status dialog and clears selection', () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      wrapper.vm.closeChangeStatusDialog()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('opens and closes change status dialog multiple times', () => {
      for (let i = 0; i < 3; i++) {
        wrapper.vm.openChangeStatusDialog(mockSystemDef)
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
        wrapper.vm.closeChangeStatusDialog()
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      }
    })

    it('opens change status dialog with null', () => {
      wrapper.vm.openChangeStatusDialog(null)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })

    it('renders SnmpDataCollectionChangeStatusDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('passes correct props to SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('type')).toBe('system-def')
      expect(dialog.props('selected')).toEqual(mockSystemDef)
    })

    it('passes correct status prop based on enabled state - enabled system def shows Disable', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockSystemDef, enabled: true })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')
    })

    it('passes correct status prop based on enabled state - disabled system def shows Enable', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockSystemDef, enabled: false })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Enable')
    })

    it('passes visible=false when change status dialog is closed', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(false)
    })

    it('handles close event from SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('close')

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
    })
  })

  describe('Change System Def Status - Successful Status Change', () => {
    let enableDisableSnmpSystemDefsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpSystemDefsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpSystemDefs')

      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('calls enableDisableSnmpSystemDefs service when disabling', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockSystemDef, enabled: true })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).toHaveBeenCalledWith(1, false, [mockSystemDef.id])
    })

    it('calls enableDisableSnmpSystemDefs service when enabling', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockSystemDef, enabled: false })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).toHaveBeenCalledWith(1, true, [mockSystemDef.id])
    })

    it('closes dialog and fetches data after successful status change', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedSystemDef).toBeNull()
      expect(store.fetchSystemDefinitions).toHaveBeenCalled()
    })

    it('uses correct collection source id for status change service call', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = { id: 99, name: 'Custom Source' } as any

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).toHaveBeenCalledWith(99, false, [mockSystemDef.id])
    })

    it('handles confirm event from SnmpDataCollectionChangeStatusDialog', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('confirm', { id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).toHaveBeenCalledWith(1, false, [mockSystemDef.id])
    })
  })

  describe('Change System Def Status - Failed Status Change', () => {
    let enableDisableSnmpSystemDefsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpSystemDefsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpSystemDefs')

      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('keeps dialog open when status change fails', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(false)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef).not.toBeNull()
    })

    it('does not fetch data when status change fails', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(false)
      store.fetchSystemDefinitions = vi.fn()

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      const initialCallCount = (store.fetchSystemDefinitions as any).mock.calls.length

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect((store.fetchSystemDefinitions as any).mock.calls.length).toBe(initialCallCount)
    })
  })

  describe('Change System Def Status - Validation Failures', () => {
    let enableDisableSnmpSystemDefsSpy: any

    beforeEach(async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      enableDisableSnmpSystemDefsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpSystemDefs')

      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('does not call service when type does not match', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'wrong-type')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected id does not match', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: 999, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected name does not match', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: 'wrong-name' }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected is null', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus(null, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selectedCollectionSource is missing', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)
      store.selectedCollectionSource = null as any

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selected has no id', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ name: mockSystemDef.name } as any, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).not.toHaveBeenCalled()
    })

    it('does not call service when selectedSystemDef id is missing', async () => {
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ name: mockSystemDef.name } as any)
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).not.toHaveBeenCalled()
    })
  })

  describe('Change Status Edge Cases', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2, disabledSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await wrapper.vm.$nextTick()
    })

    it('handles opening change status dialog for disabled system def', () => {
      wrapper.vm.openChangeStatusDialog(disabledSystemDef)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.enabled).toBe(false)
    })

    it('handles opening change status dialog for enabled system def', () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.enabled).toBe(true)
    })

    it('handles rapid open/close of change status dialog', () => {
      for (let i = 0; i < 5; i++) {
        wrapper.vm.openChangeStatusDialog(mockSystemDef)
        wrapper.vm.closeChangeStatusDialog()
      }
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef.id)
    })

    it('handles switching selected system def in change status dialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await wrapper.vm.$nextTick()

      wrapper.vm.openChangeStatusDialog(mockSystemDef2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef2.id)
      expect(wrapper.vm.selectedSystemDef?.name).toBe(mockSystemDef2.name)
    })

    it('handles system def with special characters in name for status change', async () => {
      const specialSystemDef = {
        ...mockSystemDef,
        id: 20,
        name: 'System<>Def&"Special\'Chars'
      }

      wrapper.vm.openChangeStatusDialog(specialSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.name).toBe('System<>Def&"Special\'Chars')
    })

    it('handles system def with very long name for status change', async () => {
      const longName = 'A'.repeat(200)
      const longNameSystemDef = { ...mockSystemDef, id: 21, name: longName }

      wrapper.vm.openChangeStatusDialog(longNameSystemDef)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedSystemDef?.name).toBe(longName)
    })

    it('handles mixed enabled/disabled system defs', async () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      expect(wrapper.vm.selectedSystemDef?.enabled).toBe(true)
      wrapper.vm.closeChangeStatusDialog()

      wrapper.vm.openChangeStatusDialog(disabledSystemDef)
      expect(wrapper.vm.selectedSystemDef?.enabled).toBe(false)
    })

    it('correctly toggles status from enabled to disabled', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const enableDisableSnmpSystemDefsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpSystemDefs')
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockSystemDef, enabled: true })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).toHaveBeenCalledWith(1, false, [mockSystemDef.id])
    })

    it('correctly toggles status from disabled to enabled', async () => {
      const snmpDataCollectionService = await import('@/services/snmpDataCollectionService')
      const enableDisableSnmpSystemDefsSpy = vi.spyOn(snmpDataCollectionService, 'enableDisableSnmpSystemDefs')
      enableDisableSnmpSystemDefsSpy.mockResolvedValue(true)

      wrapper.vm.openChangeStatusDialog({ ...mockSystemDef, enabled: false })
      await wrapper.vm.$nextTick()

      await wrapper.vm.changeSystemDefStatus({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()

      expect(enableDisableSnmpSystemDefsSpy).toHaveBeenCalledWith(1, true, [mockSystemDef.id])
    })
  })

  describe('Integration Tests', () => {
    it('complete workflow: search, sort, paginate', async () => {
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
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith('name', 'asc')

      // Paginate
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      expect(store.onSystemDefsPageChange).toHaveBeenCalledWith(2)
    })

    it('expand and collapse multiple groups', async () => {
      store.systemDefinitions = [mockSystemDef, mockSystemDef2]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef.id)

      wrapper.vm.toggleExpand(mockSystemDef2.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef2.id)

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.expandedRows).not.toContain(mockSystemDef.id)
      expect(wrapper.vm.expandedRows).toContain(mockSystemDef2.id)
    })

    it('refresh clears filters and fetches data', async () => {
      store.systemDefinitions = [mockSystemDef]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetSystemDefinitionsFilters).toHaveBeenCalled()
    })

    it('expand, edit, and collapse flow', async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(1)

      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(store.openSystemDefCreationDrawer).toHaveBeenCalledWith(mockSystemDef, CreateEditMode.Edit)

      wrapper.vm.toggleExpand(mockSystemDef.id)
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.expanded-content').length).toBe(0)
    })
  })
})

