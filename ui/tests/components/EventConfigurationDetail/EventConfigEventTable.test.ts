import EventConfigEventTable from '@/components/EventConfigurationDetail/EventConfigEventTable.vue'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('EventConfigEventTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigDetailStore>

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useEventConfigDetailStore(pinia)

    store.events = []
    store.eventsSearchTerm = ''
    store.eventsPagination = { page: 1, pageSize: 10, total: 0 }
    store.fetchEventsBySourceId = vi.fn().mockResolvedValue(undefined)
    store.refreshEventConfigEvents = vi.fn().mockResolvedValue(undefined)
    store.onChangeEventsSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onEventsPageChange = vi.fn().mockResolvedValue(undefined)
    store.onEventsPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onEventsSortChange = vi.fn().mockResolvedValue(undefined)
    store.showDeleteEventConfigEventDialog = vi.fn()
    store.showChangeEventConfigEventStatusDialog = vi.fn()

    wrapper = mount(EventConfigEventTable, {
      global: {
        plugins: [pinia],
        components: {
          FeatherButton,
          FeatherChip,
          FeatherDropdown,
          FeatherDropdownItem,
          FeatherIcon,
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

  it('mounts', () => {
    expect(wrapper.exists()).toBe(true)
  })
})
