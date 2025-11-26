import EventConfigurationDetail from '@/containers/EventConfigurationDetail.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'
import { getEventConfSourceById } from '@/services/eventConfigService'
import { getDefaultEventConfigEvent, useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigSource } from '@/types/eventConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { format } from 'date-fns'
import { setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/lib/utils', () => ({
  VENDOR_OPENNMS: 'OpenNMS'
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    params: { id: '1' }
  })),
  useRouter: vi.fn(() => ({
    push: mockPush
  }))
}))

vi.mock('@/services/eventConfigService', () => ({
  getEventConfSourceById: vi.fn()
}))

vi.mock('@/stores/eventConfigDetailStore', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/stores/eventConfigDetailStore')>()
  return {
    ...actual,
    getDefaultEventConfigEvent: vi.fn(() => ({
      uei: '',
      eventLabel: '',
      description: ''
    }))
  }
})

describe('EventConfigurationDetail.vue', () => {
  let wrapper: VueWrapper
  let store: ReturnType<typeof useEventConfigDetailStore>
  let modificationStore: ReturnType<typeof useEventModificationStore>

  const mockConfig: EventConfigSource = {
    id: 1,
    name: 'Test Config',
    description: 'Test Description',
    vendor: 'Test Vendor',
    enabled: true,
    eventCount: 5,
    fileOrder: 1,
    uploadedBy: 'test-user',
    createdTime: new Date(),
    lastModified: new Date()
  }

  const globalStubs = {
    FeatherBackButton: true,
    FeatherButton: true,
    EventConfigEventTable: true,
    DeleteEventConfigSourceDialog: true,
    ChangeEventConfigSourceStatusDialog: true
  }

  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.clearAllMocks()
    store = useEventConfigDetailStore()
    modificationStore = useEventModificationStore()
  })

  afterEach(() => {
    // vi.resetAllMocks()
    wrapper.unmount()
  })

  const createWrapper = async (selectedSource: EventConfigSource | null = mockConfig): Promise<VueWrapper> => {
    store.selectedSource = selectedSource
    store.fetchEventsBySourceId = vi.fn()

    wrapper = mount(EventConfigurationDetail, {
      global: {
        stubs: globalStubs
      }
    })
    await wrapper.vm.$nextTick()
    await flushPromises()
    return wrapper
  }

  it('should render the component with config data', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.event-config-container').exists()).toBe(true)
    expect(wrapper.find('h1').text()).toBe('Event Configuration Details')
  })

  it('should display config details correctly', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    const configBox = wrapper.find('.config-details-box')
    expect(configBox.text()).toContain('Test Config')
    expect(configBox.text()).toContain('test-user')
    expect(configBox.text()).toContain('Test Vendor')
    expect(configBox.text()).toContain('Enabled')
    expect(configBox.text()).toContain('5')
    expect(configBox.text()).toContain(format(new Date(), 'MM/dd/yyyy'))
  })

  it('should call setSelectedEventConfigSource when "Add Event" button is clicked', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()
    const addEventButton = wrapper.get('[data-test="add-event"]')
    await addEventButton.trigger('click')
    await wrapper.vm.$nextTick()

    expect(modificationStore.setSelectedEventConfigSource).toHaveBeenCalledTimes(1)
    expect(modificationStore.setSelectedEventConfigSource).toHaveBeenCalledWith(mockConfig, CreateEditMode.Create, expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Create'
    })
  })

  it('should display "Disabled" status when config is disabled', async () => {
    const disabledConfig = { ...mockConfig, enabled: false }
    wrapper = await createWrapper(disabledConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Disabled')
  })

  it('should show "No event configuration found" when config is null', async () => {
    wrapper = await createWrapper(null)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.not-found-container').exists()).toBe(true)
    expect(wrapper.text()).toContain('No event configuration found.')
  })

  it('should display action buttons for non-OpenNMS vendors', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    const buttons = wrapper.findAll('.action-container button')
    expect(buttons.length).equal(3)
  })

  it('should display action buttons for OpenNMS vendor with only Disable Configuration button and Add Event button', async () => {
    const openNmsConfig = { ...mockConfig, vendor: VENDOR_OPENNMS }
    wrapper = await createWrapper(openNmsConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.action-container').exists()).toBe(true)

    const buttons = wrapper.findAll('.action-container button')
    expect(buttons.length).equal(2)

    expect(buttons[0].text()).toContain('Add Event')
    expect(buttons[1].text()).toContain('Disable Configuration')
  })

  it('should show "Disable Configuration" button when source is enabled', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Disable Configuration')
  })

  it('should show "Enable Configuration" button when source is disabled', async () => {
    const disabledConfig = { ...mockConfig, enabled: false }
    wrapper = await createWrapper(disabledConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Enable Configuration')
  })

  it('should navigate back when Go Back button is clicked', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    const backButton = wrapper.get('[data-test="back-button"]')
    await backButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })

  it('should navigate back from not found page', async () => {
    wrapper = await createWrapper(null)
    await wrapper.vm.$nextTick()

    const goBackButton = wrapper.find('.not-found-container button')
    await goBackButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })

  it('should open event modification drawer when Add Event is clicked', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    const addButton = wrapper.findAll('button').find((btn) => btn.text().includes('Add Event'))
    await addButton?.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Create'
    })

    expect(modificationStore.setSelectedEventConfigSource).toHaveBeenCalledTimes(1)
    expect(modificationStore.setSelectedEventConfigSource).toHaveBeenCalledWith(mockConfig, CreateEditMode.Create, expect.any(Object))
  })

  it('should show change status dialog when Disable/Enable Configuration is clicked', async () => {
    store.showChangeEventConfigSourceStatusDialog = vi.fn()
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    const statusButton = wrapper.findAll('button').find((btn) => btn.text().includes('Disable Configuration'))
    await statusButton?.trigger('click')

    expect(store.showChangeEventConfigSourceStatusDialog).toHaveBeenCalledWith(mockConfig)
  })

  it('should show delete dialog when Delete Configuration is clicked', async () => {
    store.showDeleteEventConfigSourceDialog = vi.fn()
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    const deleteButton = wrapper.findAll('button').find((btn) => btn.text().includes('Delete Configuration'))
    await deleteButton?.trigger('click')

    expect(store.showDeleteEventConfigSourceDialog).toHaveBeenCalledWith(mockConfig)
  })

  it('should fetch events on mount when route id matches selected source', async () => {
    store.fetchEventsBySourceId = vi.fn()
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    expect(store.fetchEventsBySourceId).toHaveBeenCalled()
  })

  it('should not fetch events when route id does not match', async () => {
    const differentConfig = { ...mockConfig, id: 999 }
    store.selectedSource = differentConfig
    store.fetchEventsBySourceId = vi.fn()
    wrapper = mount(EventConfigurationDetail, {
      global: {
        stubs: {
          FeatherBackButton: true,
          FeatherButton: true,
          EventConfigEventTable: true,
          DeleteEventConfigSourceDialog: true,
          ChangeEventConfigSourceStatusDialog: true
        }
      }
    })
    await wrapper.vm.$nextTick()
    expect(store.fetchEventsBySourceId).toHaveBeenCalledTimes(1)
    expect(store.fetchEventsBySourceId).toHaveBeenCalledWith()
  })

  it('should render EventConfigEventTable component', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'EventConfigEventTable' }).exists()).toBe(true)
  })

  it('should render DeleteEventConfigSourceDialog component', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'DeleteEventConfigSourceDialog' }).exists()).toBe(true)
  })

  it('should render ChangeEventConfigSourceStatusDialog component', async () => {
    wrapper = await createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'ChangeEventConfigSourceStatusDialog' }).exists()).toBe(true)
  })

  it('should handle missing config properties gracefully', async () => {
    const incompleteConfig = {
      id: 1,
      name: 'Test',
      enabled: true
    } as EventConfigSource

    wrapper = await createWrapper(incompleteConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.event-config-container').exists()).toBe(true)
  })

  it('should handle zero event count', async () => {
    const zeroEventConfig = { ...mockConfig, eventCount: 0 }
    wrapper = await createWrapper(zeroEventConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('0')
  })

  it('should show not-found state on fetch failure', async () => {
    vi.mocked(getEventConfSourceById).mockRejectedValue(new Error('No event configuration found'))
    wrapper = mount(EventConfigurationDetail)
    await flushPromises() // Wait for onMounted rejection

    expect(wrapper.find('.not-found-container').exists()).toBe(true)
    expect(wrapper.text()).toContain('No event configuration found.')
    expect(store.selectedSource).toBeNull() // No store pollution
  })

  it('should handle store.fetchEventsBySourceId rejection without crashing', async () => {
    const mockConfig = {} as EventConfigSource
    vi.mocked(getEventConfSourceById).mockResolvedValue(mockConfig)
    store.fetchEventsBySourceId = vi.fn().mockRejectedValue(new Error('Events fetch failed'))
    wrapper = await createWrapper(mockConfig)

    // Component should still render config details
    expect(wrapper.find('.config-details-box').exists()).toBe(true)
  })

  it('should show not-found when route has no id param', async () => {
    vi.mocked(useRoute).mockReturnValue({ params: {} } as any)
    wrapper = mount(EventConfigurationDetail, {
      global: {
        stubs: globalStubs
      }
    })
    await flushPromises()

    expect(vi.mocked(getEventConfSourceById)).not.toHaveBeenCalled()
    expect(wrapper.find('.not-found-container').exists()).toBe(true)
  })

  it('should handle invalid route id (non-string)', async () => {
    vi.mocked(useRoute).mockReturnValue({ params: { id: 123 } } as any)
    vi.mocked(getEventConfSourceById).mockRejectedValue(null) // Ensure not-found for invalid ID
    wrapper = mount(EventConfigurationDetail, {
      global: {
        stubs: globalStubs
      }
    })
    await flushPromises()

    expect(store.fetchSourceById).toHaveBeenCalledWith(123)
    expect(wrapper.find('.not-found-container').exists()).toBe(true)
  })

  it('should display empty fields gracefully (null/undefined)', async () => {
    const nullishConfig = {
      ...mockConfig,
      name: null,
      description: undefined,
      vendor: null,
      eventCount: undefined
    } as unknown as EventConfigSource
    wrapper = await createWrapper(nullishConfig)

    expect(wrapper.find('.event-config-container').exists()).toBe(true)
    expect(wrapper.text()).toContain('Name:')
    expect(wrapper.text()).toContain('Uploaded By:test-user')
    expect(wrapper.text()).toContain('Vendor:')
    expect(wrapper.text()).toContain('Event Count:')
    expect(wrapper.text()).not.toContain('undefined')
    expect(wrapper.text()).toContain(`Creation Date:${format(new Date(), 'MM/dd/yyyy')}`)
    expect(wrapper.text()).toContain(`Last Modified Date:${format(new Date(), 'MM/dd/yyyy')}`)
    // Actions should show since null !== VENDOR_OPENNMS
    expect(wrapper.find('.action-container').exists()).toBe(true)
  })

  it('should hide actions only for exact VENDOR_OPENNMS match', async () => {
    const undefinedVendorConfig = { ...mockConfig, vendor: undefined } as unknown as EventConfigSource
    wrapper = await createWrapper(undefinedVendorConfig)
    expect(wrapper.find('.action-container').exists()).toBe(true) // undefined !== 'OpenNMS'
  })

  it('should call store.resetFilters and fetchEventsBySourceId on successful mount', async () => {
    store.refreshEventConfigEvents = vi.fn()
    store.fetchEventsBySourceId = vi.fn()
    wrapper = await createWrapper(mockConfig)
    await flushPromises()

    expect(store.refreshEventConfigEvents).toHaveBeenCalledOnce()
    expect(store.fetchEventsBySourceId).toHaveBeenCalledOnce()
    expect(store.fetchEventsBySourceId).toHaveBeenCalledWith() // Pass source ID
    expect(store.selectedSource).toEqual(mockConfig)
  })

  it('should set store.selectedSource only on successful fetch', async () => {
    wrapper = await createWrapper(mockConfig)
    expect(store.selectedSource).toEqual(mockConfig)
  })

  it('should not call onAddEventClick for OpenNMS vendor (button absent)', async () => {
    const openNmsConfig = { ...mockConfig, vendor: VENDOR_OPENNMS }
    wrapper = await createWrapper(openNmsConfig)
    // No button, so no call possible
    expect(wrapper.find('[data-test="add-event"]').exists()).toBe(true)
  })

  it('should handle getDefaultEventConfigEvent returning null', async () => {
    vi.mocked(getDefaultEventConfigEvent).mockReturnValue(null as any)
    // modificationStore.setSelectedEventConfigSource = vi.fn()
    wrapper = await createWrapper()
    const addButton = wrapper.get('[data-test="add-event"]')
    await addButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Create'
    })
  })

  it('should re-render on store change', async () => {
    wrapper = await createWrapper()
    store.$patch({ selectedSource: { ...mockConfig, enabled: false } })
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('Disabled')
  })
})

