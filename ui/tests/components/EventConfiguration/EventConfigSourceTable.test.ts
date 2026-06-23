import EventConfigSourceTable from '@/components/EventConfiguration/EventConfigSourceTable.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigSource } from '@/types/eventConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

const mockDownloadEventConfXmlBySourceId = vi.fn()
vi.mock('@/services/eventConfigService', () => ({
  downloadEventConfXmlBySourceId: (...args: any[]) => mockDownloadEventConfXmlBySourceId(...args),
  // The store's fetchEventConfigs action runs on mount (before the test overrides it),
  // so the service functions it chains through must resolve cleanly.
  filterEventConfigSources: vi.fn().mockResolvedValue({ sources: [], totalRecords: 0 }),
  getAllSourceNames: vi.fn().mockResolvedValue([])
}))

// Stub the child dialogs so this suite focuses on the table's own behaviour.
const stubs = {
  DeleteEventConfigSourceDialog: { name: 'DeleteEventConfigSourceDialog', template: '<div class="delete-dialog-stub"></div>' },
  ChangeEventConfigSourceStatusDialog: { name: 'ChangeEventConfigSourceStatusDialog', template: '<div class="change-status-dialog-stub"></div>' }
}

describe('EventConfigSourceTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>
  let mockSource: EventConfigSource
  let disabledSource: EventConfigSource
  let openNMSMockSource: EventConfigSource

  const mountTable = () => mount(EventConfigSourceTable, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    mockSource = {
      id: 1,
      name: 'TestSource',
      vendor: 'Cisco',
      description: 'Test description',
      enabled: true,
      eventCount: 5,
      fileOrder: 1,
      uploadedBy: 'TestUser',
      createdTime: new Date('2024-01-01'),
      lastModified: new Date('2024-01-02')
    } as any
    disabledSource = { ...mockSource, id: 2, name: 'Disabled', enabled: false } as any
    openNMSMockSource = { ...mockSource, id: 3, name: 'OpenNMS', vendor: VENDOR_OPENNMS } as any

    wrapper = mountTable()
    store = useEventConfigStore()
    store.sources = []
    store.sourcesSearchTerm = ''
    store.sourcesPagination = { page: 1, pageSize: 10, total: 0 }
    store.sourcesSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchEventConfigs = vi.fn().mockResolvedValue(undefined)
    store.refreshSourcesFilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeSourcesSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcesSortChange = vi.fn().mockResolvedValue(undefined)
    store.showDeleteEventConfigSourceModal = vi.fn()
    store.showChangeEventConfigSourceStatusDialog = vi.fn()

    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Initial Rendering', () => {
    it('renders the container and header controls', () => {
      expect(wrapper.find('.event-configuration-table').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('renders the child dialogs', () => {
      expect(wrapper.findComponent({ name: 'DeleteEventConfigSourceDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'ChangeEventConfigSourceStatusDialog' }).exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('shows EmptyList and no table when there are no sources', async () => {
      store.sources = []
      await nextTick()
      expect(wrapper.find('[data-test="event-config-source-table"]').exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.sources = [mockSource, disabledSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 2 }
      await nextTick()
    })

    it('renders the DataTable with a row per source', () => {
      expect(wrapper.find('[data-test="event-config-source-table"]').exists()).toBe(true)
      expect(wrapper.findAll('tbody tr').length).toBe(2)
    })

    it('renders source name, vendor and event count', () => {
      expect(wrapper.text()).toContain('TestSource')
      expect(wrapper.text()).toContain('Cisco')
      expect(wrapper.text()).toContain('5')
    })

    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('renders a $expectedText status tag', async ({ enabled, expectedText, expectedClass }) => {
      store.sources = [{ ...mockSource, enabled }]
      await nextTick()
      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.text()).toBe(expectedText)
      expect(tag.classes()).toContain(expectedClass)
    })
  })

  describe('Search', () => {
    it('updates the store term immediately and debounces the fetch', () => {
      wrapper.vm.onChangeSearchTerm('  cisco  ')
      expect(store.sourcesSearchTerm).toBe('  cisco  ')
      expect(store.onChangeSourcesSearchTerm).not.toHaveBeenCalled()

      vi.advanceTimersByTime(500)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('cisco')
    })

    it('debounces rapid changes to a single trailing call', () => {
      wrapper.vm.onChangeSearchTerm('a')
      wrapper.vm.onChangeSearchTerm('ab')
      wrapper.vm.onChangeSearchTerm('abc')
      vi.advanceTimersByTime(500)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('abc')
    })
  })

  describe('Refresh / navigation', () => {
    it('refreshes filters on the refresh button', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.refreshSourcesFilters).toHaveBeenCalledTimes(1)
    })

    it('navigates to the source detail on view click', () => {
      wrapper.vm.onEventClick(mockSource)
      expect(mockPush).toHaveBeenCalledWith({
        name: 'Event Configuration Detail',
        params: { id: mockSource.id }
      })
    })
  })

  describe('Lazy sort and pagination', () => {
    it('maps a sort event to onSourcesSortChange', () => {
      wrapper.vm.onSort({ sortField: 'name', sortOrder: 1 })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'asc')

      wrapper.vm.onSort({ sortField: 'vendor', sortOrder: -1 })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('vendor', 'desc')
    })

    it('falls back to default sort when no field is present', () => {
      wrapper.vm.onSort({ sortField: null, sortOrder: 1 })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('maps a page event to onSourcePageChange (1-based)', () => {
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 2, rows: 10, first: 20, pageCount: 5 })
      expect(store.onSourcePageChange).toHaveBeenCalledWith(3)
    })

    it('maps a page-size change to onSourcePageSizeChange', () => {
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 0, rows: 20, first: 0, pageCount: 3 })
      expect(store.onSourcePageSizeChange).toHaveBeenCalledWith(20)
    })
  })

  describe('Download', () => {
    it('downloads the source XML by id', async () => {
      store.sources = [mockSource]
      await nextTick()
      await wrapper.get('[data-test="download-button"]').trigger('click')
      expect(mockDownloadEventConfXmlBySourceId).toHaveBeenCalledWith(mockSource.id)
    })
  })

  describe('Row action menu', () => {
    it('builds enable/disable + delete items for a non-OpenNMS source', () => {
      wrapper.vm.rowMenuTarget = mockSource
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toEqual(['Disable Source', 'Delete Source'])
    })

    it('shows Enable label when the source is disabled', () => {
      wrapper.vm.rowMenuTarget = disabledSource
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toContain('Enable Source')
    })

    it('omits Delete for an OpenNMS-vendor source', () => {
      wrapper.vm.rowMenuTarget = openNMSMockSource
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).not.toContain('Delete Source')
    })

    it('change-status command opens the change-status dialog', () => {
      wrapper.vm.rowMenuTarget = mockSource
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Disable Source').command()
      expect(store.showChangeEventConfigSourceStatusDialog).toHaveBeenCalledWith(mockSource)
    })

    it('delete command opens the delete dialog', () => {
      wrapper.vm.rowMenuTarget = mockSource
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Delete Source').command()
      expect(store.showDeleteEventConfigSourceModal).toHaveBeenCalledWith(mockSource)
    })
  })
})
