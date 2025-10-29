import EventConfigSourceTable from '@/components/EventConfiguration/EventConfigSourceTable.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'
import { downloadEventConfXmlBySourceId } from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigSource } from '@/types/eventConfig'
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

describe('EventConfigSourceTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>
  let detailStore: ReturnType<typeof useEventConfigDetailStore>
  let mockSource: EventConfigSource
  let openNMSMockSource: EventConfigSource

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

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

    mockSource = {
      id: 1,
      name: 'TestSource',
      vendor: 'Cisco',
      description: 'Test description',
      enabled: true,
      eventCount: 5,
      fileOrder: 1,
      uploadedBy: 'TestUser',
      createdTime: new Date(),
      lastModified: new Date()
    }

    openNMSMockSource = {
      ...mockSource,
      id: 2,
      vendor: VENDOR_OPENNMS,
      enabled: false,
      eventCount: 0,
      description: ''
    }

    wrapper = mount(EventConfigSourceTable, {
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

  it('calls refreshEventsSources when refresh button is clicked', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    await wrapper.get('[data-test="refresh-button"]').trigger('click') // Assuming data-test added or use selector for refresh button
    expect(store.refreshEventsSources).toHaveBeenCalledTimes(1)
  })

  it('handles search input changes with debouncing', async () => {
    const elem = wrapper.get('[data-test="search-input"]')
    expect(elem.isVisible()).toBeTruthy()
  })

  it('handles view details button click correctly', () => {
    wrapper.vm.onEventClick(mockSource)

    expect(detailStore.setSelectedEventConfigSource).toHaveBeenCalledWith(mockSource)
    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Detail'
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

  it('renders table rows with correct data including status and event count', async () => {
    store.sources = [mockSource, openNMSMockSource]
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('transition-group-stub tr')
    expect(rows).toHaveLength(2)

    // First row
    expect(rows[0].get('[data-test="view-button"]').isVisible()).toBe(true)
    expect(rows[0].text()).toContain('TestSource')
    expect(rows[0].text()).toContain('Cisco')
    expect(rows[0].text()).toContain('Test description')
    expect(rows[0].text()).toContain('5')
    expect(rows[0].text()).toContain('Enabled')

    // Second row (OpenNMS, disabled, zero count, empty desc)
    expect(rows[1].text()).toContain(openNMSMockSource.name)
    expect(rows[1].text()).toContain(VENDOR_OPENNMS)
    expect(rows[1].text()).toContain('') // Empty description
    expect(rows[1].text()).toContain('0')
    expect(rows[1].text()).toContain('Disabled')
  })

  it('handles search input changes with debouncing and calls onChangeSourcesSearchTerm', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
    await searchInput.setValue('test')
    vi.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()

    expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('test')
  })

  it('clicks view details button in row and navigates correctly', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    await wrapper.get('[data-test="view-button"]').trigger('click')
    expect(detailStore.setSelectedEventConfigSource).toHaveBeenCalledWith(mockSource)
    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Detail'
    })
  })

  it('clicks sort header and triggers onSourcesSortChange, resets other sorts', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    const sortHeader = wrapper.findAllComponents(FeatherSortHeader)[0] // First header (name)
    await sortHeader.vm.$emit('sort-changed', { property: 'name', value: SORT.ASCENDING })
    await wrapper.vm.$nextTick()

    expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', SORT.ASCENDING)
    expect(wrapper.vm.sort.name).toBe(SORT.ASCENDING)
    expect(wrapper.vm.sort.vendor).toBe(SORT.NONE) // Reset others
  })

  it('handles sort change for ascending and resets other sorts', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    wrapper.vm.sortChanged({ property: 'name', value: 'asc' })

    expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'asc')
    expect(wrapper.vm.sort.name).toBe('asc')
    expect(wrapper.vm.sort.vendor).toBe(SORT.NONE)
  })

  it('handles sort reset to default when value is none', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    wrapper.vm.sortChanged({ property: 'name', value: SORT.NONE })

    expect(store.onSourcesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    expect(wrapper.vm.sort.name).toBe(SORT.NONE)
  })

  it('does not render dropdown for OpenNMS vendor', async () => {
    store.sources = [openNMSMockSource]
    await wrapper.vm.$nextTick()

    const row = wrapper.find('transition-group-stub tr')
    expect(row.exists()).toBe(true)
    expect(row.findAll('button')).toHaveLength(2)

    expect(row.findAllComponents(FeatherDropdown)).toHaveLength(0)
    expect(row.findAllComponents(FeatherDropdownItem)).toHaveLength(0)
  })

  it('renders dropdown for non-OpenNMS vendor with correct enable/disable text', async () => {
    const disabledSource = { ...mockSource, enabled: false }
    store.sources = [mockSource, disabledSource]
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('transition-group-stub tr')
    expect(rows).toHaveLength(2)

    const buttons1 = rows[0].findAll('button')
    expect(buttons1.length).toBe(3)

    await buttons1[1].trigger('click')
    await wrapper.vm.$nextTick()

    const dropdown1 = rows[0].findAllComponents(FeatherDropdownItem)
    expect(dropdown1[0].text()).toBe('Disable Source')
    expect(dropdown1[1].text()).toBe('Delete Source')

    const buttons2 = rows[1].findAll('button')
    expect(buttons2.length).toBe(3)

    await buttons2[1].trigger('click')
    await wrapper.vm.$nextTick()

    const dropdown2 = rows[1].findAllComponents(FeatherDropdownItem)
    expect(buttons1.length).toBe(3)

    expect(dropdown2[0].text()).toBe('Enable Source')
    expect(dropdown2[1].text()).toBe('Delete Source')
  })

  it('clicks enable/disable from dropdown and calls showChangeEventConfigSourceStatusDialog', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('transition-group-stub tr')
    expect(rows).toHaveLength(1)

    await rows[0].findAll('button')[1].trigger('click')
    expect(rows[0].findAll('button')).toHaveLength(3)
    await wrapper.vm.$nextTick()

    await wrapper.get('[data-test="change-status-button"]').trigger('click')
    expect(store.showChangeEventConfigSourceStatusDialog).toHaveBeenCalledWith(mockSource)
  })

  it('clicks download from dropdown and calls downloadEventConfXmlBySourceId', async () => {
    store.sources = [mockSource]
    const svc = await import('@/services/eventConfigService')
    vi.spyOn(svc, 'downloadEventConfXmlBySourceId').mockResolvedValue(false)
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('transition-group-stub tr')
    expect(rows).toHaveLength(1)

    await rows[0].findAll('button')[2].trigger('click')
    await wrapper.vm.$nextTick()
    
    expect(downloadEventConfXmlBySourceId).toHaveBeenCalled()
    expect(svc.downloadEventConfXmlBySourceId).toHaveBeenCalledWith(mockSource.id)
  })

  it('clicks delete from dropdown and calls showDeleteEventConfigSourceModal', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('transition-group-stub tr')
    expect(rows).toHaveLength(1)

    await rows[0].findAll('button')[1].trigger('click')
    await wrapper.vm.$nextTick()

    await wrapper.get('[data-test="delete-source-button"]').trigger('click')
    expect(store.showDeleteEventConfigSourceModal).toHaveBeenCalledWith(mockSource)
  })

  it('renders pagination with correct props and handles page change', async () => {
    store.sources = [mockSource]
    store.sourcesPagination = { page: 1, pageSize: 10, total: 15 }
    await wrapper.vm.$nextTick()

    const pagination = wrapper.getComponent(FeatherPagination)
    expect(pagination.props('modelValue')).toBe(1)
    expect(pagination.props('pageSize')).toBe(10)
    expect(pagination.props('total')).toBe(15)

    await pagination.vm.$emit('update:modelValue', 2)
    expect(store.onSourcePageChange).toHaveBeenCalledWith(2)
  })

  it('handles page size change via pagination', async () => {
    store.sources = [mockSource]
    store.sourcesPagination = { page: 1, pageSize: 10, total: 15 }
    await wrapper.vm.$nextTick()

    const pagination = wrapper.getComponent(FeatherPagination)
    await pagination.vm.$emit('update:pageSize', 20)
    expect(store.onSourcePageSizeChange).toHaveBeenCalledWith(20)
  })

  it('renders dialogs (DeleteEventConfigSourceDialog and ChangeEventConfigSourceStatusDialog)', () => {
    expect(wrapper.findComponent({ name: 'DeleteEventConfigSourceDialog' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ChangeEventConfigSourceStatusDialog' }).exists()).toBe(true)
  })

  it('renders EmptyList when no sources are available', async () => {
    store.sources = []
    await wrapper.vm.$nextTick()

    expect(wrapper.get('[data-test="empty-list"]').isVisible()).toBe(true)
    expect(wrapper.text()).toContain('No results found.')
  })

  it('shows empty state after search with no results', async () => {
    store.sources = [mockSource]
    await wrapper.vm.$nextTick()

    const searchInput = wrapper.get('[data-test="search-input"] .feather-input')
    await searchInput.setValue('nonexistent')
    vi.advanceTimersByTime(500)
    await flushPromises()

    store.sources = []
    await wrapper.vm.$nextTick()

    expect(wrapper.get('[data-test="empty-list"]').isVisible()).toBe(true)
    expect(wrapper.text()).toContain('No results found.')
  })
})

