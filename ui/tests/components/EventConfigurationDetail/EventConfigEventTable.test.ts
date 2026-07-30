import EventConfigEventTable from '@/components/EventConfigurationDetail/EventConfigEventTable.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent, EventConfigSource } from '@/types/eventConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

const stubs = {
  DeleteEventConfigEventDialog: { name: 'DeleteEventConfigEventDialog', template: '<div class="delete-event-dialog-stub"></div>' },
  ChangeEventConfigEventStatusDialog: { name: 'ChangeEventConfigEventStatusDialog', template: '<div class="change-status-dialog-stub"></div>' }
}

describe('EventConfigEventTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>
  let modificationStore: ReturnType<typeof useEventModificationStore>
  let mockEvent: EventConfigEvent
  let disabledEvent: EventConfigEvent
  let mockSource: EventConfigSource

  const mountTable = () => mount(EventConfigEventTable, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    mockEvent = {
      id: 1,
      uei: 'uei.opennms.org/test',
      eventLabel: 'Test Event',
      severity: 'Major',
      enabled: true,
      description: '<p>An event description</p>'
    } as any
    disabledEvent = { ...mockEvent, id: 2, eventLabel: 'Disabled Event', enabled: false } as any
    mockSource = { id: 1, name: 'Src', vendor: 'Cisco', enabled: true } as any

    wrapper = mountTable()
    store = useEventConfigDetailStore()
    store.events = []
    store.eventsSearchTerm = ''
    store.eventsPagination = { page: 1, pageSize: 10, total: 0 }
    store.eventsSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.selectedSource = mockSource
    store.refreshEventConfigEvents = vi.fn().mockResolvedValue(undefined)
    store.onChangeEventsSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onEventsPageChange = vi.fn().mockResolvedValue(undefined)
    store.onEventsPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onEventsSortChange = vi.fn().mockResolvedValue(undefined)
    store.showChangeEventConfigEventStatusDialog = vi.fn()
    store.showDeleteEventConfigEventDialog = vi.fn()

    modificationStore = useEventModificationStore()
    modificationStore.setSelectedEventConfigSource = vi.fn()

    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Initial Rendering', () => {
    it('renders the container and header controls', () => {
      expect(wrapper.find('.event-config-event-table').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
    })

    it('renders the child dialogs', () => {
      expect(wrapper.findComponent({ name: 'DeleteEventConfigEventDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'ChangeEventConfigEventStatusDialog' }).exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('shows EmptyList and no table when there are no events', async () => {
      store.events = []
      await nextTick()
      expect(wrapper.find('[data-test="event-config-event-table"]').exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.events = [mockEvent, disabledEvent]
      store.eventsPagination = { page: 1, pageSize: 10, total: 2 }
      await nextTick()
    })

    it('renders the DataTable with a row per event', () => {
      expect(wrapper.find('[data-test="event-config-event-table"]').exists()).toBe(true)
      expect(wrapper.findAll('tbody tr').length).toBe(2)
    })

    it('renders uei, label and severity', () => {
      expect(wrapper.text()).toContain('uei.opennms.org/test')
      expect(wrapper.text()).toContain('Test Event')
      expect(wrapper.text()).toContain('Major')
    })

    it('applies the severity color class to the severity tag', () => {
      expect(wrapper.find('.major-color').exists()).toBe(true)
    })

    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('renders a $expectedText status tag', async ({ enabled, expectedText, expectedClass }) => {
      store.events = [{ ...mockEvent, enabled }]
      await nextTick()
      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.text()).toBe(expectedText)
      expect(tag.classes()).toContain(expectedClass)
    })
  })

  describe('Row expansion', () => {
    beforeEach(async () => {
      store.events = [mockEvent]
      store.eventsPagination = { page: 1, pageSize: 10, total: 1 }
      await nextTick()
    })

    it('renders the description in the expansion slot', async () => {
      wrapper.vm.expandedRows = { [mockEvent.id]: true }
      await nextTick()
      const expanded = wrapper.find('.expanded-content')
      expect(expanded.exists()).toBe(true)
      expect(expanded.html()).toContain('An event description')
    })
  })

  describe('Search', () => {
    it('updates the store term immediately and debounces the fetch', () => {
      wrapper.vm.onChangeSearchTerm('  uei  ')
      expect(store.eventsSearchTerm).toBe('  uei  ')
      expect(store.onChangeEventsSearchTerm).not.toHaveBeenCalled()

      vi.advanceTimersByTime(500)
      expect(store.onChangeEventsSearchTerm).toHaveBeenCalledWith('uei')
    })

    it('debounces rapid changes to a single trailing call', () => {
      wrapper.vm.onChangeSearchTerm('a')
      wrapper.vm.onChangeSearchTerm('ab')
      vi.advanceTimersByTime(500)
      expect(store.onChangeEventsSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeEventsSearchTerm).toHaveBeenCalledWith('ab')
    })
  })

  describe('Refresh / edit', () => {
    it('refreshes events on the refresh button', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.refreshEventConfigEvents).toHaveBeenCalledTimes(1)
    })

    it('navigates to the create flow in edit mode', () => {
      wrapper.vm.onEditEvent(mockEvent)
      expect(modificationStore.setSelectedEventConfigSource).toHaveBeenCalledWith(mockSource, CreateEditMode.Edit, mockEvent)
      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration Create' })
    })
  })

  describe('Lazy sort and pagination', () => {
    it('maps a sort event to onEventsSortChange', () => {
      wrapper.vm.onSort({ sortField: 'uei', sortOrder: 1 })
      expect(store.onEventsSortChange).toHaveBeenCalledWith('uei', 'asc')

      wrapper.vm.onSort({ sortField: 'severity', sortOrder: -1 })
      expect(store.onEventsSortChange).toHaveBeenCalledWith('severity', 'desc')
    })

    it('falls back to default sort when no field is present', () => {
      wrapper.vm.onSort({ sortField: null, sortOrder: 1 })
      expect(store.onEventsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('maps a page event to onEventsPageChange (1-based)', () => {
      store.eventsPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 2, rows: 10, first: 20, pageCount: 5 })
      expect(store.onEventsPageChange).toHaveBeenCalledWith(3)
    })

    it('maps a page-size change to onEventsPageSizeChange', () => {
      store.eventsPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 0, rows: 20, first: 0, pageCount: 3 })
      expect(store.onEventsPageSizeChange).toHaveBeenCalledWith(20)
    })
  })

  describe('Row action menu', () => {
    it('builds enable/disable + delete items for a non-OpenNMS source', () => {
      store.selectedSource = { ...mockSource, vendor: 'Cisco' }
      wrapper.vm.rowMenuTarget = mockEvent
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toEqual(['Disable Event', 'Delete Event'])
    })

    it('shows Enable label when the event is disabled', () => {
      wrapper.vm.rowMenuTarget = disabledEvent
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toContain('Enable Event')
    })

    it('omits Delete when the source is OpenNMS-vendor', () => {
      store.selectedSource = { ...mockSource, vendor: VENDOR_OPENNMS }
      wrapper.vm.rowMenuTarget = mockEvent
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).not.toContain('Delete Event')
    })

    it('change-status command opens the change-status dialog', () => {
      wrapper.vm.rowMenuTarget = mockEvent
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Disable Event').command()
      expect(store.showChangeEventConfigEventStatusDialog).toHaveBeenCalledWith(mockEvent)
    })

    it('delete command opens the delete dialog', () => {
      store.selectedSource = { ...mockSource, vendor: 'Cisco' }
      wrapper.vm.rowMenuTarget = mockEvent
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Delete Event').command()
      expect(store.showDeleteEventConfigEventDialog).toHaveBeenCalledWith(mockEvent)
    })
  })
})
