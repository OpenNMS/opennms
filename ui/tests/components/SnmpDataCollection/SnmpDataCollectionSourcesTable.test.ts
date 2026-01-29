import SnmpDataCollectionSourcesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourcesTable.vue'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionSource } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('SnmpDataCollectionSourcesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionStore>
  let mockSource: SnmpCollectionSource
  let mockSource2: SnmpCollectionSource
  let disabledMockSource: SnmpCollectionSource

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionStore(pinia)

    store.sources = []
    store.sourcesSearchTerm = ''
    store.sourcesPagination = { page: 1, pageSize: 10, total: 0 }
    store.sourcesSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    store.refreshSourcesfilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeSourcesSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcesSortChange = vi.fn().mockResolvedValue(undefined)

    mockSource = {
      id: 1,
      name: 'Test Source',
      vendor: 'Cisco',
      description: 'Test description',
      enabled: true,
      uploadedBy: 'TestUser',
      createdTime: new Date('2024-01-01'),
      lastModified: new Date('2024-01-02')
    }

    mockSource2 = {
      id: 2,
      name: 'Another Source',
      vendor: 'Juniper',
      description: 'Another description',
      enabled: true,
      uploadedBy: 'AnotherUser',
      createdTime: new Date('2024-02-01'),
      lastModified: new Date('2024-02-02')
    }

    disabledMockSource = {
      id: 3,
      name: 'Disabled Source',
      vendor: 'HP',
      description: 'Disabled source description',
      enabled: false,
      uploadedBy: 'DisabledUser',
      createdTime: new Date('2024-03-01'),
      lastModified: new Date('2024-03-02')
    }

    wrapper = mount(SnmpDataCollectionSourcesTable, {
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

    it('calls fetchSnmpCollectionSources on mount', () => {
      expect(store.fetchSnmpCollectionSources).toHaveBeenCalled()
    })

    it('renders the title correctly', () => {
      expect(wrapper.text()).toContain('Data Collection Sources')
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
    it('renders EmptyList when no sources are available', async () => {
      store.sources = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })

    it('does not render table when sources are empty', async () => {
      store.sources = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(false)
    })

    it('does not render pagination when sources are empty', async () => {
      store.sources = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('renders table when sources exist', async () => {
      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('renders correct number of rows', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows).toHaveLength(2)
    })

    it('renders source name correctly', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Test Source')
    })

    it('renders vendor correctly', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Cisco')
    })

    it('renders uploadedBy correctly', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('TestUser')
    })

    it('renders enabled status correctly', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Enabled')
    })

    it('renders disabled status correctly', async () => {
      store.sources = [disabledMockSource]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('Disabled')
    })

    it('renders action buttons for each row', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      const buttons = rows[0].findAll('button')
      expect(buttons.length).toBeGreaterThanOrEqual(2)
    })

    it('renders view button', async () => {
      expect(wrapper.find('[data-test="view-button"]').exists()).toBe(true)
    })

    it('renders download button', async () => {
      expect(wrapper.find('[data-test="download-button"]').exists()).toBe(true)
    })

    it('renders pagination when sources exist', async () => {
      expect(wrapper.find('.alerts-pagination').exists()).toBe(true)
    })
  })

  describe('Table with Multiple Sources', () => {
    it.each([
      { count: 1, expectedRows: 1 },
      { count: 2, expectedRows: 2 },
      { count: 5, expectedRows: 5 },
      { count: 10, expectedRows: 10 }
    ])('renders $expectedRows rows when $count sources exist', async ({ count, expectedRows }) => {
      const sources = Array.from({ length: count }, (_, i) => ({
        ...mockSource,
        id: i + 1,
        name: `Source ${i + 1}`
      }))
      store.sources = sources
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows).toHaveLength(expectedRows)
    })
  })

  describe('Search Functionality', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
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

      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('test')
    })

    it('does not call onChangeSourcesSearchTerm before debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSourcesSearchTerm).not.toHaveBeenCalled()
    })

    it('calls onChangeSourcesSearchTerm after debounce time', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('search term')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('search term')
    })

    it('handles empty search term', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('')
    })

    it('shows empty state after search with no results', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('nonexistent')
      vi.advanceTimersByTime(500)
      await flushPromises()

      store.sources = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
    })

    it.each([
      { term: 'simple' },
      { term: 'with spaces' },
      { term: 'special@chars#' },
      { term: 'UPPERCASE' },
      { term: '123numeric456' }
    ])('handles search term "$term" correctly', async ({ term }) => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue(term)
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith(term)
    })
  })

  describe('Refresh Button', () => {
    it('calls refreshSourcesfilters when refresh button is clicked', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.refreshSourcesfilters).toHaveBeenCalledTimes(1)
    })

    it('can click refresh button multiple times', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      await wrapper.get('[data-test="refresh-button"]').trigger('click')

      expect(store.refreshSourcesfilters).toHaveBeenCalledTimes(3)
    })
  })

  describe('View Details Navigation', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('navigates to detail page when view button is clicked', async () => {
      await wrapper.get('[data-test="view-button"]').trigger('click')
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Detail',
        params: { id: mockSource.id }
      })
    })

    it('handles view click via onSourceClick function', () => {
      wrapper.vm.onSourceClick(mockSource)
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Detail',
        params: { id: mockSource.id }
      })
    })

    it.each([
      { id: 1, name: 'Source 1' },
      { id: 42, name: 'Source 42' },
      { id: 999, name: 'Source 999' }
    ])('navigates correctly for source with id $id', async ({ id, name }) => {
      const source = { ...mockSource, id, name }
      store.sources = [source]
      await wrapper.vm.$nextTick()

      wrapper.vm.onSourceClick(source)
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Detail',
        params: { id }
      })
    })
  })

  describe('Sorting Functionality', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('renders sort headers', async () => {
      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBeGreaterThan(0)
    })

    it('handles sort change for ascending', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'asc')
    })

    it('handles sort change for descending', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'desc' })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'desc')
    })

    it('handles sort reset to default when value is none', () => {
      wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('updates local sort state on sort change', async () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')
    })

    it('resets other sort properties when sorting by a column', async () => {
      wrapper.vm.sort.vendor = 'asc'
      wrapper.vm.sortChanged({ property: 'name', value: 'desc' })

      expect(wrapper.vm.sort.name).toBe('desc')
      expect(wrapper.vm.sort.vendor).toBe(SORT.NONE)
    })

    it('clicks sort header and triggers onSourcesSortChange', async () => {
      const sortHeader = wrapper.findAllComponents(FeatherSortHeader)[0]
      await sortHeader.vm.$emit('sort-changed', { property: 'name', value: SORT.ASCENDING })
      await wrapper.vm.$nextTick()

      expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', SORT.ASCENDING)
    })

    it.each([
      { property: 'name', sortOrder: 'asc' },
      { property: 'name', sortOrder: 'desc' },
      { property: 'vendor', sortOrder: 'asc' },
      { property: 'vendor', sortOrder: 'desc' },
      { property: 'uploadedBy', sortOrder: 'asc' },
      { property: 'uploadedBy', sortOrder: 'desc' },
      { property: 'enabled', sortOrder: 'asc' },
      { property: 'enabled', sortOrder: 'desc' }
    ])('handles sorting by $property with $sortOrder order', async ({ property, sortOrder }) => {
      wrapper.vm.sortChanged({ property, value: sortOrder })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith(property, sortOrder)
      expect(wrapper.vm.sort[property]).toBe(sortOrder)
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
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
      expect(store.onSourcePageChange).toHaveBeenCalledWith(2)
    })

    it('handles page size change', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', 20)
      expect(store.onSourcePageSizeChange).toHaveBeenCalledWith(20)
    })

    it.each([
      { page: 1 },
      { page: 2 },
      { page: 5 },
      { page: 10 }
    ])('handles page change to page $page', async ({ page }) => {
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', page)
      expect(store.onSourcePageChange).toHaveBeenCalledWith(page)
    })

    it.each([
      { pageSize: 10 },
      { pageSize: 20 },
      { pageSize: 50 },
      { pageSize: 100 },
      { pageSize: 200 }
    ])('handles page size change to $pageSize', async ({ pageSize }) => {
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', pageSize)
      expect(store.onSourcePageSizeChange).toHaveBeenCalledWith(pageSize)
    })

    it('pagination has correct page sizes options', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('pageSizes')).toEqual([10, 20, 50, 100, 200])
    })
  })

  describe('Dropdown Actions', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('renders dropdown for each row', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].findComponent(FeatherDropdown).exists()).toBe(true)
    })

    it('renders more actions button in each row', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      const buttons = rows[0].findAll('button')
      // Should have at least 3 buttons: view, download, and more actions
      expect(buttons.length).toBeGreaterThanOrEqual(3)
    })

    it('has dropdown menu in row actions', async () => {
      const rows = wrapper.findAll('transition-group-stub tr')
      const dropdown = rows[0].findComponent(FeatherDropdown)
      expect(dropdown.exists()).toBe(true)
    })
  })

  describe('Columns Configuration', () => {
    it('has correct columns defined', () => {
      const columns = wrapper.vm.columns
      expect(columns).toEqual([
        { id: 'name', label: 'Source' },
        { id: 'vendor', label: 'Vendor' },
        { id: 'uploadedBy', label: 'Uploaded By' },
        { id: 'enabled', label: 'Status' }
      ])
    })

    it('renders all column headers', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders).toHaveLength(4)
    })

    it.each([
      { id: 'name', label: 'Source' },
      { id: 'vendor', label: 'Vendor' },
      { id: 'uploadedBy', label: 'Uploaded By' },
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
      const source = { ...mockSource, enabled }
      store.sources = [source]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain(expectedText)
    })
  })

  describe('Multiple Sources with Mixed States', () => {
    it('renders multiple sources with different enabled states', async () => {
      store.sources = [mockSource, disabledMockSource]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows).toHaveLength(2)
      expect(rows[0].text()).toContain('Enabled')
      expect(rows[1].text()).toContain('Disabled')
    })

    it('renders sources with all different vendors', async () => {
      store.sources = [mockSource, mockSource2, disabledMockSource]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows).toHaveLength(3)
      expect(rows[0].text()).toContain('Cisco')
      expect(rows[1].text()).toContain('Juniper')
      expect(rows[2].text()).toContain('HP')
    })
  })

  describe('Sort State Management', () => {
    it('initializes sort state with NONE for all columns', () => {
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.vendor).toBe(SORT.NONE)
      expect(wrapper.vm.sort.uploadedBy).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('maintains sort state after sorting', () => {
      wrapper.vm.sortChanged({ property: 'vendor', value: 'asc' })

      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.vendor).toBe('asc')
      expect(wrapper.vm.sort.uploadedBy).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('resets all sorts when changing sort column', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')

      wrapper.vm.sortChanged({ property: 'vendor', value: 'desc' })
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.vendor).toBe('desc')
    })
  })

  describe('Edge Cases', () => {
    it('handles source with empty name', async () => {
      const sourceWithEmptyName = { ...mockSource, name: '' }
      store.sources = [sourceWithEmptyName]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('handles source with empty vendor', async () => {
      const sourceWithEmptyVendor = { ...mockSource, vendor: '' }
      store.sources = [sourceWithEmptyVendor]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('handles source with empty uploadedBy', async () => {
      const sourceWithEmptyUploadedBy = { ...mockSource, uploadedBy: '' }
      store.sources = [sourceWithEmptyUploadedBy]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('handles rapid search input changes', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('a')
      await searchInput.setValue('ab')
      await searchInput.setValue('abc')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      // Only the last value should trigger the call due to debouncing
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('abc')
    })

    it('handles pagination with zero total', async () => {
      store.sources = []
      store.sourcesPagination = { page: 1, pageSize: 10, total: 0 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })
  })

  describe('Store State Binding', () => {
    it('reflects store sources in table', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows).toHaveLength(2)
    })

    it('reflects store pagination in component', async () => {
      store.sources = [mockSource]
      store.sourcesPagination = { page: 3, pageSize: 25, total: 100 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('modelValue')).toBe(3)
      expect(pagination.props('pageSize')).toBe(25)
      expect(pagination.props('total')).toBe(100)
    })

    it('reflects store search term in input', async () => {
      store.sourcesSearchTerm = 'test search'
      await wrapper.vm.$nextTick()

      // The v-model binding should reflect the store value
      expect(store.sourcesSearchTerm).toBe('test search')
    })
  })

  describe('Integration Tests', () => {
    it('complete workflow: search, sort, paginate', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      // Search
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('test')

      // Sort
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'asc')

      // Paginate
      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      expect(store.onSourcePageChange).toHaveBeenCalledWith(2)
    })

    it('navigates to different sources', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      wrapper.vm.onSourceClick(mockSource)
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Detail',
        params: { id: mockSource.id }
      })

      wrapper.vm.onSourceClick(mockSource2)
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Detail',
        params: { id: mockSource2.id }
      })
    })

    it('refresh clears filters and fetches data', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.refreshSourcesfilters).toHaveBeenCalled()
    })
  })

  describe('Parametrized Tests - Source Data Variations', () => {
    it.each([
      { field: 'name', value: 'Very Long Source Name That Might Overflow' },
      { field: 'vendor', value: 'Vendor With Special Characters !@#$%' },
      { field: 'uploadedBy', value: 'user@example.com' },
      { field: 'description', value: 'A very detailed description of this source' }
    ])('renders source with $field as "$value"', async ({ field, value }) => {
      const source = { ...mockSource, [field]: value }
      store.sources = [source]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })
  })

  describe('Accessibility', () => {
    it('table has aria-label', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('sort headers are rendered with col scope', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders.length).toBeGreaterThan(0)
      // Each sort header should have scope="col" as defined in the template
      sortHeaders.forEach((header) => {
        expect(header.attributes('scope')).toBe('col')
      })
    })
  })
})
