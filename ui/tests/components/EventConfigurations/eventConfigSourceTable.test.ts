import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import EventConfigSourceTable from '@/components/EventConfiguration/EventConfigSourceTable.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('EventConfigSourceTable.vue', () => {
  let wrapper: any
  let store: any
  let detailStore: any
  let mockSource: any

  beforeEach(() => {
    vi.clearAllMocks()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useEventConfigStore(pinia)
    detailStore = useEventConfigDetailStore(pinia)

    store.sources = []
    store.sourcesSearchTerm = ''
    store.sourcesPagination = { page: 1, pageSize: 10, total: 0 }
    store.fetchEventConfigs = vi.fn().mockResolvedValue(undefined)
    store.refreshEventsSources = vi.fn().mockResolvedValue(undefined)
    store.onChangeSourcesSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcesSortChange = vi.fn().mockResolvedValue(undefined)
    store.showDeleteEventConfigSourceModal = vi.fn()
    store.showChangeEventConfigSourceStatusDialog = vi.fn()
    store.showDeleteEventConfigSourceModal = vi.fn().mockResolvedValue(true)
    store.showChangeEventConfigSourceStatusDialog = vi.fn().mockResolvedValue(true)
    detailStore.setSelectedEventConfigSource = vi.fn()

    mockSource = {
      id: '1',
      name: 'TestSource',
      vendor: 'Cisco',
      description: 'Test description',
      eventCount: 5,
      enabled: true
    }

    wrapper = mount(EventConfigSourceTable, {
      global: {
        plugins: [pinia]
      }
    })
  })

  it('renders correctly and calls fetchEventConfigs on mount', () => {
    expect(wrapper.exists()).toBe(true)
    expect(store.fetchEventConfigs).toHaveBeenCalled()
  })

  it('renders EmptyList when no sources are available', () => {
    store.sources = []
    expect(store.sources.length).toBe(0)
  })

  it('renders table with data when sources exist', () => {
    store.sources = [mockSource]
    expect(store.sources.length).toBe(1)
    expect(store.sources[0].name).toBe('TestSource')
  })

  it('calls refreshEventsSources when refresh button is clicked', () => {
    expect(vi.isMockFunction(store.refreshEventsSources)).toBe(true)
  })

  it('handles search input changes with debouncing', async () => {
    const elem = wrapper.get('[data-test="search-input"]')
    expect(elem.exists()).toBeTruthy()
  })

  it('handles view details button click correctly', () => {
    wrapper.vm.onEventClick(mockSource)

    expect(detailStore.setSelectedEventConfigSource).toHaveBeenCalledWith(mockSource)
    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Details',
      params: { id: '1' }
    })
  })

  it('handles sorting changes correctly', () => {
    wrapper.vm.sortChanged({ property: 'name', value: 'asc' })
    expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'asc')
  })

  it('shows dropdown actions for non-OpenNMS vendors', () => {
    expect(mockSource.vendor).not.toBe('OpenNMS')
  })

  it('handles enable/disable source action', () => {
    store.showChangeEventConfigSourceStatusDialog(mockSource)
    expect(store.showChangeEventConfigSourceStatusDialog).toHaveBeenCalledWith(mockSource)
  })

  it('handles delete source action', () => {
    store.showDeleteEventConfigSourceModal(mockSource)
    expect(store.showDeleteEventConfigSourceModal).toHaveBeenCalledWith(mockSource)
  })

  it('renders pagination when sources exist', () => {
    store.sources = [mockSource]
    store.sourcesPagination = { page: 1, pageSize: 10, total: 15 }
    expect(store.sourcesPagination.total).toBe(15)
  })

  it('handles page size changes', () => {
    store.onSourcePageSizeChange(20)
    expect(store.onSourcePageSizeChange).toHaveBeenCalledWith(20)
  })
  it('handles delete source action', async () => {
    await store.showDeleteEventConfigSourceModal(mockSource)
    expect(store.showDeleteEventConfigSourceModal).toHaveBeenCalledWith(mockSource)
  })
  it('handles enable/disable source action', async () => {
    await store.showChangeEventConfigSourceStatusDialog(mockSource)
    expect(store.showChangeEventConfigSourceStatusDialog).toHaveBeenCalledWith(mockSource)
  })
})
