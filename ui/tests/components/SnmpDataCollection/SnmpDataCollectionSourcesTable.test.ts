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

const mockDownloadSnmpDataCollectionById = vi.fn()
const mockDeleteSnmpCollectionSources = vi.fn()
const mockEnableDisableSnmpDataCollectionSources = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  downloadSnmpDataCollectionById: (...args: any[]) => mockDownloadSnmpDataCollectionById(...args),
  deleteSnmpCollectionSources: (...args: any[]) => mockDeleteSnmpCollectionSources(...args),
  enableDisableSnmpDataCollectionSources: (...args: any[]) => mockEnableDisableSnmpDataCollectionSources(...args),
  getAllSnmpCollectionProfiles: vi.fn().mockResolvedValue([])
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
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
    store.fetchAllSourcesNames = vi.fn().mockResolvedValue(undefined)
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

    it('renders the Create New Data Collection Source button', () => {
      expect(wrapper.text()).toContain('Create New Data Collection Source')
    })

    it('renders search input', () => {
      const searchInput = wrapper.find('[data-test="search-input"]')
      expect(searchInput.exists()).toBe(true)
    })

    it('renders within snmp-data-collection-source-table container', () => {
      expect(wrapper.find('.snmp-data-collection-source-table').exists()).toBe(true)
    })

    it('renders header with section-left and section-right', () => {
      expect(wrapper.find('.header .section-left').exists()).toBe(true)
      expect(wrapper.find('.header .section-right').exists()).toBe(true)
    })

    it('renders search-container within section-left', () => {
      expect(wrapper.find('.section-left .search-container').exists()).toBe(true)
    })

    it('renders DeleteConfirmationDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('renders .container wrapper for table area', () => {
      expect(wrapper.find('.container').exists()).toBe(true)
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

    it('should still show header with search when empty', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.text()).toContain('Create New Data Collection Source')
    })

    it('shows table then hides when data is cleared', async () => {
      store.sources = [mockSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(true)

      store.sources = []
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.data-table').exists()).toBe(false)
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

    it('renders .action-container in each row', async () => {
      const actionContainers = wrapper.findAll('.action-container')
      expect(actionContainers).toHaveLength(1)
    })

    it('renders action-container with view, dropdown and nothing else', async () => {
      const viewBtn = wrapper.find('[data-test="view-button"]')
      const editBtn = wrapper.find('[data-test="edit-button"]')
      const dropdown = wrapper.findComponent(FeatherDropdown)
      expect(viewBtn.exists()).toBe(true)
      expect(editBtn.exists()).toBe(false)
      expect(dropdown.exists()).toBe(true)
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

  describe('View Details Navigation', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('navigates to detail page when view button is clicked', async () => {
      await wrapper.get('[data-test="view-button"]').trigger('click')
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Source Detail',
        params: { id: mockSource.id }
      })
    })

    it('handles view click via onSourceClick function', () => {
      wrapper.vm.onSourceClick(mockSource)
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Source Detail',
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
        name: 'SNMP Data Collection Source Detail',
        params: { id }
      })
    })

    it('navigates to correct source when clicking second row view button', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      const viewButtons = wrapper.findAll('[data-test="view-button"]')
      await viewButtons[1].trigger('click')

      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Source Detail',
        params: { id: mockSource2.id }
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

    it('handles sort reset to default when value is none', () => {
      wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('resets other sort properties when sorting by a column', async () => {
      wrapper.vm.sort.enabled = 'asc'
      wrapper.vm.sortChanged({ property: 'name', value: 'desc' })

      expect(wrapper.vm.sort.name).toBe('desc')
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
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

    it.each([{ page: 1 }, { page: 2 }, { page: 5 }, { page: 10 }])(
      'handles page change to page $page',
      async ({ page }) => {
        const pagination = wrapper.getComponent(FeatherPagination)
        await pagination.vm.$emit('update:modelValue', page)
        expect(store.onSourcePageChange).toHaveBeenCalledWith(page)
      }
    )

    it.each([{ pageSize: 10 }, { pageSize: 20 }, { pageSize: 50 }, { pageSize: 100 }, { pageSize: 200 }])(
      'handles page size change to $pageSize',
      async ({ pageSize }) => {
        const pagination = wrapper.getComponent(FeatherPagination)
        await pagination.vm.$emit('update:pageSize', pageSize)
        expect(store.onSourcePageSizeChange).toHaveBeenCalledWith(pageSize)
      }
    )

    it('pagination has correct page sizes options', async () => {
      const pagination = wrapper.getComponent(FeatherPagination)
      expect(pagination.props('pageSizes')).toEqual([10, 20, 50, 100, 200])
    })
  })

  describe('Columns Configuration', () => {
    it('renders a Profiles header in addition to the existing ones', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const ths = wrapper.findAll('thead th')
      // Source (sortable), Profiles (plain), Status (sortable), Actions (plain) = 4 headers.
      expect(ths.length).toBe(4)
      const labels = ths.map((th) => th.text().trim())
      expect(labels).toContain('Profiles')
      expect(labels).toContain('Actions')
    })

    it('keeps Source and Status as sortable headers', async () => {
      // The table (and thus sort headers) only renders when sources exist.
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      const properties = sortHeaders.map((h) => h.props('property'))
      expect(properties).toContain('name')
      expect(properties).toContain('enabled')
    })

    it('each sort header has correct property prop', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders[0].props('property')).toBe('name')
      expect(sortHeaders[1].props('property')).toBe('enabled')
    })

    it('sort headers display correct label text', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders[0].text()).toContain('Source')
      expect(sortHeaders[1].text()).toContain('Status')
    })
  })

  describe('Status Display', () => {
    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('displays "$expectedText" with class "$expectedClass" when enabled=$enabled', async ({ enabled, expectedText, expectedClass }) => {
      const source = { ...mockSource, enabled }
      store.sources = [source]
      await wrapper.vm.$nextTick()

      const statusTag = wrapper.find('[data-test="status-tag"]')
      expect(statusTag.exists()).toBe(true)
      expect(statusTag.text()).toBe(expectedText)
      expect(statusTag.classes()).toContain(expectedClass)
    })

    it('renders FeatherChip for status in each row', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips).toHaveLength(2)
    })

    it('renders mixed enabled/disabled status chips', async () => {
      store.sources = [mockSource, disabledMockSource]
      await wrapper.vm.$nextTick()

      const chips = wrapper.findAll('[data-test="status-tag"]')
      expect(chips).toHaveLength(2)
      expect(chips[0].text()).toBe('Enabled')
      expect(chips[0].classes()).toContain('enabled-tag')
      expect(chips[1].text()).toBe('Disabled')
      expect(chips[1].classes()).toContain('disabled-tag')
    })

    it('renders .tag container around status chip', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const tag = wrapper.find('.tag')
      expect(tag.exists()).toBe(true)
      expect(tag.find('[data-test="status-tag"]').exists()).toBe(true)
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

    it('renders sources with all different names', async () => {
      store.sources = [mockSource, mockSource2, disabledMockSource]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows).toHaveLength(3)
      expect(rows[0].text()).toContain('Test Source')
      expect(rows[1].text()).toContain('Another Source')
      expect(rows[2].text()).toContain('Disabled Source')
    })
  })

  describe('Sort State Management', () => {
    it('initializes sort state with NONE for all columns', () => {
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe(SORT.NONE)
    })

    it('maintains sort state after sorting', () => {
      wrapper.vm.sortChanged({ property: 'enabled', value: 'asc' })

      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe('asc')
    })

    it('resets all sorts when changing sort column', () => {
      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(wrapper.vm.sort.name).toBe('asc')

      wrapper.vm.sortChanged({ property: 'enabled', value: 'desc' })
      expect(wrapper.vm.sort.name).toBe(SORT.NONE)
      expect(wrapper.vm.sort.enabled).toBe('desc')
    })
  })

  describe('Edge Cases', () => {
    it('handles source with empty name', async () => {
      const sourceWithEmptyName = { ...mockSource, name: '' }
      store.sources = [sourceWithEmptyName]
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

    it('handles unicode characters in source fields', async () => {
      const unicodeSource: SnmpCollectionSource = {
        ...mockSource,
        name: 'ソース-テスト'
      }
      store.sources = [unicodeSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows[0].text()).toContain('ソース-テスト')
    })

    it('handles large pagination total counts', async () => {
      store.sources = [mockSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 100000 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.props('total')).toBe(100000)
    })

    it('does not call search before debounce completes when input cleared', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
      await searchInput.setValue('test')
      vi.advanceTimersByTime(300)
      await searchInput.setValue('')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('')
    })

    it('handles source with very long name', async () => {
      const longName = 'A'.repeat(500)
      const longNameSource = { ...mockSource, name: longName }
      store.sources = [longNameSource]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
      expect(wrapper.text()).toContain(longName)
    })

    it('handles negative id value', async () => {
      const negativeIdSource = { ...mockSource, id: -1, name: 'negativeIdSource' }
      store.sources = [negativeIdSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 1 }
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      expect(rows.length).toBeGreaterThanOrEqual(1)
      expect(rows[0].text()).toContain('negativeIdSource')
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
  })

  describe('Parametrized Tests - Source Data Variations', () => {
    it.each([
      { field: 'name', value: 'Very Long Source Name That Might Overflow' },
      { field: 'description', value: 'A very detailed description of this source' }
    ])('renders source with $field as "$value"', async ({ field, value }) => {
      const source = { ...mockSource, [field]: value }
      store.sources = [source]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)
    })
  })

  describe('Accessibility', () => {
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

  describe('Download Functionality', () => {
    let mockCreateObjectURL: ReturnType<typeof vi.fn>
    let mockRevokeObjectURL: ReturnType<typeof vi.fn>
    let mockClick: ReturnType<typeof vi.fn>
    let mockLink: { href: string; download: string; click: ReturnType<typeof vi.fn> }

    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      mockClick = vi.fn()
      mockLink = { href: '', download: '', click: mockClick }
      vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any)

      mockCreateObjectURL = vi.fn().mockReturnValue('blob:http://localhost/test-blob-url')
      mockRevokeObjectURL = vi.fn()
      window.URL.createObjectURL = mockCreateObjectURL as unknown as (obj: Blob | MediaSource) => string
      window.URL.revokeObjectURL = mockRevokeObjectURL as unknown as (url: string) => void
    })

    it('downloads XML file successfully', async () => {
      const mockBlob = new Blob(['<xml>test</xml>'], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(mockDownloadSnmpDataCollectionById).toHaveBeenCalledWith(mockSource.id, 'xml')
      expect(mockCreateObjectURL).toHaveBeenCalled()
      expect(mockLink.download).toBe('Test Source.xml')
      expect(mockClick).toHaveBeenCalled()
      expect(mockRevokeObjectURL).toHaveBeenCalled()
    })

    it('downloads JSON file successfully', async () => {
      const mockBlob = new Blob(['{"test": true}'], { type: 'application/json' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, 'json')

      expect(mockDownloadSnmpDataCollectionById).toHaveBeenCalledWith(mockSource.id, 'json')
      expect(mockLink.download).toBe('Test Source.json')
      expect(mockClick).toHaveBeenCalled()
    })

    it('passes blob to createObjectURL', async () => {
      const mockBlob = new Blob(['<xml>data</xml>'], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      const blobArg = mockCreateObjectURL.mock.calls[0][0]
      expect(blobArg).toBeInstanceOf(Blob)
    })

    it('revokes object URL after download', async () => {
      const mockBlob = new Blob(['<xml>test</xml>'], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(mockRevokeObjectURL).toHaveBeenCalledWith('blob:http://localhost/test-blob-url')
    })

    it('shows error snackbar when download response is falsy', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue(null)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to download Collection Source \'Test Source\'.',
        error: true
      })
    })

    it('shows error snackbar when download response is undefined', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue(undefined)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to download Collection Source \'Test Source\'.',
        error: true
      })
    })

    it('calls service with correct source id and format for XML', async () => {
      const mockBlob = new Blob([''], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(mockDownloadSnmpDataCollectionById).toHaveBeenCalledWith(1, 'xml')
    })

    it('calls service with correct source id and format for JSON', async () => {
      const mockBlob = new Blob([''], { type: 'application/json' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, 'json')

      expect(mockDownloadSnmpDataCollectionById).toHaveBeenCalledWith(1, 'json')
    })

    it('sets download filename using source name and format', async () => {
      const customSource = { ...mockSource, name: 'My Custom Source' }
      store.sources = [customSource]
      await wrapper.vm.$nextTick()

      const mockBlob = new Blob(['<xml/>'], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(customSource, 'xml')

      expect(mockLink.download).toBe('My Custom Source.xml')
    })

    it.each([
      { format: 'xml', expectedFilename: 'Test Source.xml' },
      { format: 'json', expectedFilename: 'Test Source.json' }
    ])('generates correct filename for $format format', async ({ format, expectedFilename }) => {
      const mockBlob = new Blob(['content'], { type: `application/${format}` })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, format)

      expect(mockLink.download).toBe(expectedFilename)
    })

    it('handles download for source with special characters in name', async () => {
      const specialSource = { ...mockSource, name: 'Source (v2) [test]' }
      const mockBlob = new Blob(['<xml/>'], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(specialSource, 'xml')

      expect(mockLink.download).toBe('Source (v2) [test].xml')
    })

    it('creates anchor element for download link', async () => {
      const createElementSpy = vi.spyOn(document, 'createElement')
      const mockBlob = new Blob(['<xml/>'], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(createElementSpy).toHaveBeenCalledWith('a')
    })

    it('downloads file for different source with correct id', async () => {
      const mockBlob = new Blob(['<xml>test</xml>'], { type: 'application/xml' })
      mockDownloadSnmpDataCollectionById.mockResolvedValue(mockBlob)

      await wrapper.vm.downloadCollectionSource(mockSource2, 'xml')

      expect(mockDownloadSnmpDataCollectionById).toHaveBeenCalledWith(mockSource2.id, 'xml')
      expect(mockLink.download).toBe('Another Source.xml')
    })
  })

  describe('Delete Dialog', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('delete dialog is initially hidden', () => {
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
    })

    it('selected collection source is initially null', () => {
      expect(wrapper.vm.selectedCollectionSource).toBeNull()
    })

    it('opens delete dialog and sets selected source', () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource)
    })

    it('closes delete dialog and clears selected source', () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      wrapper.vm.closeDeleteCollectionSourceDialog()

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedCollectionSource).toBeNull()
    })

    it('opens and closes dialog multiple times', () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)

      wrapper.vm.closeDeleteCollectionSourceDialog()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)

      wrapper.vm.openDeleteCollectionSourceDialog(mockSource2)
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource2)
    })

    it('opens dialog with null source', () => {
      wrapper.vm.openDeleteCollectionSourceDialog(null)

      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource).toBeNull()
    })

    it('passes correct props to DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')).toEqual(mockSource)
      expect(dialog.props('type')).toBe('source')
    })

    it('passes visible=false when dialog is closed', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(false)
    })
  })

  describe('Delete Functionality', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
    })

    it('successfully deletes a collection source', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)

      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([mockSource.id])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Collection Source \'Test Source\' deleted successfully.'
      })
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })

    it('shows error snackbar when delete service returns false', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(false)

      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'Test Source\'.',
        error: true
      })
    })

    it('does not navigate after failed delete', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(false)

      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockPush).not.toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })

    it('keeps delete dialog state unchanged on service failure', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(false)

      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')

      // The component uses selectedCollectionSource.value?.name in the error message,
      // but should not clear the dialog state on failure
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource).not.toBeNull()
    })

    it('resets dialog state and navigates on successful delete', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)

      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')

      // Component resets dialog state before navigating
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedCollectionSource).toBe(null)
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })

    it('shows error when type is not source', async () => {
      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'mib-group')

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'Test Source\'.',
        error: true
      })
    })

    it('shows error when selected id does not match', async () => {
      await wrapper.vm.deleteCollectionSource({ id: 999, name: mockSource.name }, 'source')

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'Test Source\'.',
        error: true
      })
    })

    it('shows error when selected name does not match', async () => {
      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: 'Wrong Name' }, 'source')

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'Wrong Name\'.',
        error: true
      })
    })

    it('shows error when selected is null', async () => {
      await wrapper.vm.deleteCollectionSource(null, 'source')

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'undefined\'.',
        error: true
      })
    })

    it.each([
      { type: 'mib-group' },
      { type: 'system-def' },
      { type: 'resource-type' },
      { type: 'unknown' },
      { type: '' }
    ])('rejects delete when type is "$type" instead of "source"', async ({ type }) => {
      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, type)

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })
  })

  describe('Delete Dialog Events', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('handles close event from DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('close')

      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedCollectionSource).toBeNull()
    })

    it('handles confirm event from DeleteConfirmationDialog', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      await dialog.vm.$emit('confirm', { id: mockSource.id, name: mockSource.name }, 'source')
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([mockSource.id])
    })
  })

  describe('Download Edge Cases', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      vi.spyOn(document, 'createElement').mockReturnValue({ href: '', download: '', click: vi.fn() } as any)
      window.URL.createObjectURL = vi.fn().mockReturnValue('blob:test') as unknown as (
        obj: Blob | MediaSource
      ) => string
      window.URL.revokeObjectURL = vi.fn() as unknown as (url: string) => void
    })

    it('handles download when service throws an error', async () => {
      mockDownloadSnmpDataCollectionById.mockRejectedValue(new Error('Network error'))

      await expect(wrapper.vm.downloadCollectionSource(mockSource, 'xml')).rejects.toThrow('Network error')
    })

    it('does not create object URL when response is null', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue(null)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(window.URL.createObjectURL).not.toHaveBeenCalled()
    })

    it('does not call click when response is null', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue(null)
      const clickSpy = vi.fn()
      vi.spyOn(document, 'createElement').mockReturnValue({ href: '', download: '', click: clickSpy } as any)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(clickSpy).not.toHaveBeenCalled()
    })

    it('handles download for source with empty name', async () => {
      const emptyNameSource = { ...mockSource, name: '' }
      mockDownloadSnmpDataCollectionById.mockResolvedValue({
        data: '<xml/>',
        headers: { 'content-type': 'application/xml' }
      })
      const mockLink = { href: '', download: '', click: vi.fn() }
      vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any)

      await wrapper.vm.downloadCollectionSource(emptyNameSource, 'xml')

      expect(mockLink.download).toBe('.xml')
    })

    it('handles download with empty data response', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue({
        data: '',
        headers: { 'content-type': 'application/xml' }
      })
      const mockLink = { href: '', download: '', click: vi.fn() }
      vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(mockLink.click).toHaveBeenCalled()
    })

    it('handles download with missing content-type header', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue({
        data: '<xml/>',
        headers: {}
      })
      const mockLink = { href: '', download: '', click: vi.fn() }
      vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any)

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')

      expect(mockLink.click).toHaveBeenCalled()
      expect(mockLink.download).toBe('Test Source.xml')
    })

    it.each([{ format: 'xml' }, { format: 'json' }])(
      'does not show error snackbar on successful $format download',
      async ({ format }) => {
        mockDownloadSnmpDataCollectionById.mockResolvedValue({
          data: 'data',
          headers: { 'content-type': `application/${format}` }
        })
        const mockLink = { href: '', download: '', click: vi.fn() }
        vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any)

        await wrapper.vm.downloadCollectionSource(mockSource, format)

        expect(mockShowSnackBar).not.toHaveBeenCalled()
      }
    )
  })

  describe('Delete Edge Cases', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('handles delete when selected source id is 0 (falsy)', async () => {
      const zeroIdSource = { ...mockSource, id: 0 }
      wrapper.vm.openDeleteCollectionSourceDialog(zeroIdSource)

      await wrapper.vm.deleteCollectionSource({ id: 0, name: mockSource.name }, 'source')

      // id 0 is falsy, so it should fall into the else branch
      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })

    it('handles delete when selectedCollectionSource is null (dialog not opened)', async () => {
      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })

    it('opens delete dialog for correct source when multiple sources exist', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteCollectionSourceDialog(mockSource2)

      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource2)
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })

    it('delete dialog does not affect other sources', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)

      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')

      // mockSource2 should still exist in store
      expect(store.sources).toContainEqual(mockSource2)
    })

    it('handles delete service throwing an exception', async () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      mockDeleteSnmpCollectionSources.mockRejectedValue(new Error('Network error'))

      await expect(
        wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')
      ).rejects.toThrow('Network error')
    })

    it('switching selected source in dialog updates correctly', async () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource)

      wrapper.vm.openDeleteCollectionSourceDialog(mockSource2)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource2)
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })

    it('close then reopen dialog resets selected source correctly', async () => {
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      wrapper.vm.closeDeleteCollectionSourceDialog()
      expect(wrapper.vm.selectedCollectionSource).toBeNull()

      wrapper.vm.openDeleteCollectionSourceDialog(mockSource2)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource2)
    })

    it('handles sequential delete operations correctly', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      // First delete
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)
      await wrapper.vm.deleteCollectionSource({ id: mockSource.id, name: mockSource.name }, 'source')
      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([mockSource.id])

      // Reset dialog
      wrapper.vm.closeDeleteCollectionSourceDialog()
      expect(wrapper.vm.selectedCollectionSource).toBeNull()

      // Second delete
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource2)
      await wrapper.vm.deleteCollectionSource({ id: mockSource2.id, name: mockSource2.name }, 'source')
      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([mockSource2.id])
    })
  })

  describe('Create Source Button', () => {
    it('renders the create button', () => {
      const createButton = wrapper
        .findAllComponents(FeatherButton)
        .find((btn: any) => btn.text().includes('Create New Data Collection Source'))
      expect(createButton).toBeDefined()
    })

    it('calls goToCreateSource function to navigate', () => {
      wrapper.vm.goToCreateSource()
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection Source Detail', params: { id: 'create' } })
    })

    it('goToCreateSource can be called multiple times', () => {
      wrapper.vm.goToCreateSource()
      wrapper.vm.goToCreateSource()
      expect(mockPush).toHaveBeenCalledTimes(2)
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection Source Detail', params: { id: 'create' } })
    })
  })

  describe('Edit Button', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('does not render edit button', async () => {
      expect(wrapper.find('[data-test="edit-button"]').exists()).toBe(false)
    })

    it('does not render edit buttons for multiple rows', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      expect(editButtons).toHaveLength(0)
    })
  })

  describe('Dropdown Item DOM Interactions', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('renders a FeatherDropdown in the action column', () => {
      const dropdown = wrapper.findComponent(FeatherDropdown)
      expect(dropdown.exists()).toBe(true)
    })

    it('dropdown trigger has correct aria label with source name', () => {
      const dropdown = wrapper.findComponent(FeatherDropdown)
      const triggerButton = dropdown.find('button')
      expect(triggerButton.attributes('aria-label')).toBe(`More actions for ${mockSource.name}`)
    })

    it('renders one dropdown per source row', () => {
      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns).toHaveLength(1)
    })

    it('renders two dropdowns with 2 source rows', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns).toHaveLength(2)
    })

    it('renders three dropdowns with 3 source rows', async () => {
      store.sources = [mockSource, mockSource2, disabledMockSource]
      await wrapper.vm.$nextTick()

      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns).toHaveLength(3)
    })

    it('each dropdown trigger has aria label with correct source name', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      const dropdowns = wrapper.findAllComponents(FeatherDropdown)
      expect(dropdowns).toHaveLength(2)

      const firstTrigger = dropdowns[0].find('button')
      expect(firstTrigger.attributes('aria-label')).toBe(`More actions for ${mockSource.name}`)

      const secondTrigger = dropdowns[1].find('button')
      expect(secondTrigger.attributes('aria-label')).toBe(`More actions for ${mockSource2.name}`)
    })
  })

  describe('Search Input Details', () => {
    it('search input has label "Search"', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('label')).toBe('Search')
    })

    it('search input has type "search"', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('type')).toBe('search')
    })

    it('search input has correct hint text', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('hint')).toBe('Search by Source, Vendor or Description')
    })

    it('search input is bound to store sourcesSearchTerm', async () => {
      store.sourcesSearchTerm = 'my search'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('my search')
    })
  })

  describe('EmptyList Component', () => {
    it('renders EmptyList with correct content prop', async () => {
      store.sources = []
      await wrapper.vm.$nextTick()

      const emptyList = wrapper.findComponent({ name: 'EmptyList' })
      expect(emptyList.exists()).toBe(true)
      expect(emptyList.props('content')).toEqual({ msg: 'No results found.' })
    })

    it('does not render EmptyList when sources exist', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)
    })
  })

  describe('Actions Column Header', () => {
    it('renders non-sortable Actions header', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const thElements = wrapper.findAll('thead th')
      const actionsHeader = thElements.find((th: any) => th.text() === 'Actions')
      expect(actionsHeader).toBeDefined()
    })

    it('renders 2 sort headers plus 1 Actions header', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)
      expect(sortHeaders).toHaveLength(2)

      // "Actions" is a plain <th>, not a FeatherSortHeader
      const allTh = wrapper.findAll('thead th')
      expect(allTh.length).toBeGreaterThanOrEqual(1)
    })
  })

  describe('Table Aria Label', () => {
    it('table has aria-label set to "Events Table"', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const table = wrapper.find('.data-table')
      expect(table.attributes('aria-label')).toBe('Events Table')
    })
  })

  describe('Pagination Data Test Attribute', () => {
    it('pagination has data-test="FeatherPagination"', async () => {
      store.sources = [mockSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="FeatherPagination"]').exists()).toBe(true)
    })
  })

  describe('State Transitions', () => {
    it('transitions from empty to populated state', async () => {
      store.sources = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
      expect(wrapper.find('.data-table').exists()).toBe(false)

      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)
      expect(wrapper.find('.data-table').exists()).toBe(true)
    })

    it('transitions from populated to empty state', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(true)

      store.sources = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.data-table').exists()).toBe(false)
      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
    })

    it('pagination appears and disappears with sources', async () => {
      store.sources = [mockSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(true)

      store.sources = []
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.alerts-pagination').exists()).toBe(false)
    })
  })

  describe('DeleteConfirmationDialog Props', () => {
    it('always passes type="source" to DeleteConfirmationDialog', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('type')).toBe('source')
    })

    it('passes null as selected when dialog is not opened', () => {
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('selected')).toBeNull()
    })

    it('updates selected prop when dialog is opened with different sources', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      await wrapper.vm.$nextTick()

      let dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('selected')).toEqual(mockSource)

      wrapper.vm.openDeleteCollectionSourceDialog(mockSource2)
      await wrapper.vm.$nextTick()

      dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('selected')).toEqual(mockSource2)
    })
  })

  describe('Debounce Behavior', () => {
    it('debounce resets timer on subsequent inputs', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('a')
      vi.advanceTimersByTime(400)
      expect(store.onChangeSourcesSearchTerm).not.toHaveBeenCalled()

      await searchInput.setValue('ab')
      vi.advanceTimersByTime(400)
      // Still not called because the 400ms is from the second setValue, not 500ms total
      expect(store.onChangeSourcesSearchTerm).not.toHaveBeenCalled()

      vi.advanceTimersByTime(100)
      await wrapper.vm.$nextTick()
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('ab')
    })

    it('does not call store when search changes within debounce window', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('first')
      vi.advanceTimersByTime(100)
      await searchInput.setValue('second')
      vi.advanceTimersByTime(100)
      await searchInput.setValue('third')
      vi.advanceTimersByTime(100)

      expect(store.onChangeSourcesSearchTerm).not.toHaveBeenCalled()

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('third')
    })

    it('calls store multiple times for searches separated by full debounce period', async () => {
      const searchInput = wrapper.get('[data-test="search-input"] .feather-input')

      await searchInput.setValue('first')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      await searchInput.setValue('second')
      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledTimes(2)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenNthCalledWith(1, 'first')
      expect(store.onChangeSourcesSearchTerm).toHaveBeenNthCalledWith(2, 'second')
    })
  })

  describe('Row Data Correctness', () => {
    it('renders all 3 data columns per row', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      const tds = rows[0].findAll('td')
      // 3 data columns (Source / Profiles / Status) + 1 actions column
      expect(tds).toHaveLength(4)
    })

    it('renders data in correct column order', async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      const tds = rows[0].findAll('td')

      expect(tds[0].text()).toBe('Test Source')
      // tds[1] is the Profiles cell. With no profiles loaded it shows '—'.
      expect(tds[2].text()).toBe('Enabled')
    })

    it('renders disabled source data in correct column order', async () => {
      store.sources = [disabledMockSource]
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('transition-group-stub tr')
      const tds = rows[0].findAll('td')

      expect(tds[0].text()).toBe('Disabled Source')
      expect(tds[2].text()).toBe('Disabled')
    })
  })

  describe('Header Layout', () => {
    it('renders section-left with search', () => {
      expect(wrapper.find('.section-left').exists()).toBe(true)
      expect(wrapper.find('.search-container').exists()).toBe(true)
    })

    it('renders section-right', () => {
      expect(wrapper.find('.section-right').exists()).toBe(true)
      expect(wrapper.find('.add').exists()).toBe(true)
    })
  })

  describe('Concurrent Operations', () => {
    it('handles sort then page change', async () => {
      store.sources = [mockSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'asc')

      const pagination = wrapper.getComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 3)
      expect(store.onSourcePageChange).toHaveBeenCalledWith(3)
    })
  })

  describe('View Button Per Row', () => {
    it('renders correct number of view buttons for multiple sources', async () => {
      store.sources = [mockSource, mockSource2, disabledMockSource]
      await wrapper.vm.$nextTick()

      const viewButtons = wrapper.findAll('[data-test="view-button"]')
      expect(viewButtons).toHaveLength(3)
    })
  })

  describe('Change Status Dialog - State Management', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('change status dialog is initially hidden', () => {
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
    })

    it('selected collection source is initially null', () => {
      expect(wrapper.vm.selectedCollectionSource).toBe(null)
    })

    it('opens change status dialog and sets selected source', () => {
      wrapper.vm.openChangeStatusDialog(mockSource)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource)
    })

    it('closes change status dialog and clears selected source', () => {
      wrapper.vm.openChangeStatusDialog(mockSource)
      wrapper.vm.closeChangeStatusDialog()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedCollectionSource).toBe(null)
    })

    it('opens and closes change status dialog multiple times', () => {
      for (let i = 0; i < 3; i++) {
        wrapper.vm.openChangeStatusDialog(mockSource)
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
        wrapper.vm.closeChangeStatusDialog()
        expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      }
    })

    it('opens change status dialog with null source', () => {
      wrapper.vm.openChangeStatusDialog(null)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource).toBe(null)
    })

    it('renders SnmpDataCollectionChangeStatusDialog component', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('passes correct props to SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockSource)
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('type')).toBe('source')
    })

    it('passes correct status prop based on enabled state - enabled source', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: true })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')
    })

    it('passes correct status prop based on enabled state - disabled source', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: false })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Enable')
    })

    it('passes visible=false when change status dialog is closed', () => {
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('visible')).toBe(false)
    })
  })

  describe('Change Status Dialog - Dropdown Item', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('openChangeStatusDialog sets correct source for enabled source', () => {
      const enabledSource = { ...mockSource, enabled: true }
      wrapper.vm.openChangeStatusDialog(enabledSource)
      
      expect(wrapper.vm.selectedCollectionSource).toEqual(enabledSource)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
    })

    it('openChangeStatusDialog sets correct source for disabled source', () => {
      const disabledSource = { ...mockSource, enabled: false }
      wrapper.vm.openChangeStatusDialog(disabledSource)
      
      expect(wrapper.vm.selectedCollectionSource).toEqual(disabledSource)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
    })

    it('dialog shows Disable status for enabled source', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: true })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')
    })

    it('dialog shows Enable status for disabled source', async () => {
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: false })
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Enable')
    })
  })

  describe('Change Status Functionality - Successful Status Change', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it('successfully disables a collection source', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: true })

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [mockSource.id])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Collection Source '${mockSource.name}' disabled successfully.`
      })
    })

    it('successfully enables a collection source', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: false })

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(true, [mockSource.id])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Collection Source '${mockSource.name}' enabled successfully.`
      })
    })

    it('refreshes sources after successful status change', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(store.fetchSnmpCollectionSources).toHaveBeenCalled()
    })

    it('closes dialog and resets selected source on successful status change', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedCollectionSource).toBe(null)
    })

    it('uses correct source id for status change service call', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      const customSource = { ...mockSource, id: 99 }
      wrapper.vm.openChangeStatusDialog(customSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: 99, name: customSource.name }, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [99])
    })
  })

  describe('Change Status Functionality - Failed Status Change', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it('shows error snackbar when service returns false', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: true })

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Failed to disable Collection Source '${mockSource.name}'.`,
        error: true
      })
    })

    it('shows correct error message for enable failure', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: false })

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Failed to enable Collection Source '${mockSource.name}'.`,
        error: true
      })
    })

    it('does not refresh sources when status change fails', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      store.fetchSnmpCollectionSources = vi.fn()
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(store.fetchSnmpCollectionSources).not.toHaveBeenCalled()
    })

    it('keeps dialog open when status change fails', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource)
    })
  })

  describe('Change Status Functionality - Validation Failures', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it('shows error when type is not source', async () => {
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'mib-group')

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Failed to change status for Collection Source '${mockSource.name}'.`,
        error: true
      })
    })

    it('shows error when selected id does not match', async () => {
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: 999, name: mockSource.name }, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Failed to change status for Collection Source '${mockSource.name}'.`,
        error: true
      })
    })

    it('shows error when selected name does not match', async () => {
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: 'Wrong Name' }, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to change status for Collection Source \'Wrong Name\'.',
        error: true
      })
    })

    it('shows error when selected is null', async () => {
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus(null, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to change status for Collection Source \'undefined\'.',
        error: true
      })
    })

    it('shows error when selectedCollectionSource is null (dialog not opened)', async () => {
      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
    })

    it.each([
      { type: 'mib-group' },
      { type: 'system-def' },
      { type: 'resource-type' },
      { type: 'unknown' },
      { type: '' }
    ])('rejects status change when type is "$type" instead of "source"', async ({ type }) => {
      wrapper.vm.openChangeStatusDialog(mockSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, type)

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
    })
  })

  describe('Change Status Dialog Events', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
    })

    it('handles close event from SnmpDataCollectionChangeStatusDialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockSource)
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('close')

      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
      expect(wrapper.vm.selectedCollectionSource).toBe(null)
    })

    it('handles confirm event from SnmpDataCollectionChangeStatusDialog', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog(mockSource)

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      await dialog.vm.$emit('confirm', { id: mockSource.id, name: mockSource.name }, 'source')
      await flushPromises()

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalled()
    })
  })

  describe('Change Status Edge Cases', () => {
    beforeEach(async () => {
      store.sources = [mockSource]
      await wrapper.vm.$nextTick()
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it('handles status change when selected source id is 0 (falsy)', async () => {
      const sourceWithZeroId = { ...mockSource, id: 0 }
      wrapper.vm.openChangeStatusDialog(sourceWithZeroId)

      await wrapper.vm.changeCollectionSourceStatus({ id: 0, name: mockSource.name }, 'source')

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({ error: true })
      )
    })

    it('switches selected source in dialog updates correctly', () => {
      wrapper.vm.openChangeStatusDialog(mockSource)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource)

      wrapper.vm.openChangeStatusDialog(mockSource2)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource2)
    })

    it('close then reopen dialog resets selected source correctly', () => {
      wrapper.vm.openChangeStatusDialog(mockSource)
      wrapper.vm.closeChangeStatusDialog()
      wrapper.vm.openChangeStatusDialog(mockSource2)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource2)
    })

    it('handles sequential status change operations correctly', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)

      // First operation
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: true })
      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [mockSource.id])

      mockEnableDisableSnmpDataCollectionSources.mockClear()

      // Second operation
      wrapper.vm.openChangeStatusDialog({ ...mockSource2, enabled: false })
      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource2.id, name: mockSource2.name }, 'source')
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(true, [mockSource2.id])
    })

    it('opens change status dialog for correct source when multiple sources exist', async () => {
      store.sources = [mockSource, mockSource2]
      await wrapper.vm.$nextTick()

      wrapper.vm.openChangeStatusDialog(mockSource2)
      expect(wrapper.vm.selectedCollectionSource).toEqual(mockSource2)
    })

    it('handles source with special characters in name for status change', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      const specialSource = { ...mockSource, name: 'Test <Source> & "Quotes"' }
      wrapper.vm.openChangeStatusDialog(specialSource)

      await wrapper.vm.changeCollectionSourceStatus({ id: specialSource.id, name: specialSource.name }, 'source')

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Collection Source \'Test <Source> & "Quotes"\' disabled successfully.'
      })
    })
  })

  describe('Multiple Sources with Mixed States - Change Status', () => {
    it('passes correct status prop for each source based on enabled state', async () => {
      store.sources = [
        { ...mockSource, enabled: true },
        { ...mockSource2, enabled: false },
        { ...disabledMockSource, enabled: false }
      ]
      await wrapper.vm.$nextTick()

      // Test enabled source shows Disable
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: true })
      await wrapper.vm.$nextTick()
      let dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')

      // Test disabled source shows Enable
      wrapper.vm.openChangeStatusDialog({ ...mockSource2, enabled: false })
      await wrapper.vm.$nextTick()
      dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Enable')
    })

    it('handles mixed sources status changes correctly', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.sources = [
        { ...mockSource, enabled: true },
        { ...mockSource2, enabled: false }
      ]
      await wrapper.vm.$nextTick()

      // Disable an enabled source
      wrapper.vm.openChangeStatusDialog({ ...mockSource, enabled: true })
      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource.id, name: mockSource.name }, 'source')
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [mockSource.id])

      mockEnableDisableSnmpDataCollectionSources.mockClear()

      // Enable a disabled source
      wrapper.vm.openChangeStatusDialog({ ...mockSource2, enabled: false })
      await wrapper.vm.changeCollectionSourceStatus({ id: mockSource2.id, name: mockSource2.name }, 'source')
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(true, [mockSource2.id])
    })
  })
})

