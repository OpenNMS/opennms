import { mount, VueWrapper } from '@vue/test-utils'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { EventConfigSource } from '@/types/eventConfig'
import { CreateEditMode } from '@/types'
import EventConfigurationDetail from '@/containers/EventConfigurationDetail.vue'
// Mock the router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    params: { id: '1' }
  })),
  useRouter: vi.fn(() => ({
    push: mockPush
  }))
}))

describe('EventConfigurationDetail.vue', () => {
  let wrapper: VueWrapper
  let store: ReturnType<typeof useEventConfigDetailStore>

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

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useEventConfigDetailStore()
    vi.clearAllMocks()
  })

  const createWrapper = (selectedSource: EventConfigSource | null = mockConfig) => {
    store.selectedSource = selectedSource
    store.fetchEventsBySourceId = vi.fn()

    return mount(EventConfigurationDetail, {
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
  }

  it('should render the component with config data', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.event-config-container').exists()).toBe(true)
    expect(wrapper.find('h1').text()).toBe('Event Configuration Details')
  })

  it('should display config details correctly', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    const configBox = wrapper.get('[data-test="config-box"]')
    expect(configBox.text()).toContain('Test Config')
    expect(configBox.text()).toContain('Test Description')
    expect(configBox.text()).toContain('Test Vendor')
    expect(configBox.text()).toContain('Enabled')
    expect(configBox.text()).toContain('5')
  })

  it('should display "Disabled" status when config is disabled', async () => {
    const disabledConfig = { ...mockConfig, enabled: false }
    wrapper = createWrapper(disabledConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Disabled')
  })

  it('should show "No event configuration found" when config is null', async () => {
    wrapper = createWrapper(null)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.not-found-container').exists()).toBe(true)
    expect(wrapper.text()).toContain('No event configuration found.')
  })

  it('should display action buttons for non-OpenNMS vendors', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    const buttons = wrapper.findAll('.action-container button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('should show "Disable Source" button when source is enabled', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Disable Source')
  })

  it('should show "Enable Source" button when source is disabled', async () => {
    const disabledConfig = { ...mockConfig, enabled: false }
    wrapper = createWrapper(disabledConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Enable Source')
  })

  it('should navigate back when Go Back button is clicked', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    const backButton = wrapper.get('[data-test="back-button"]')
    await backButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })

  it('should navigate back from not found page', async () => {
    wrapper = createWrapper(null)
    await wrapper.vm.$nextTick()

    const goBackButton = wrapper.find('.not-found-container button')
    await goBackButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })

  it('should open event modification drawer when Add Event is clicked', async () => {
    store.openEventModificationDrawer = vi.fn()
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    const addButton = wrapper.findAll('button').find((btn) => btn.text().includes('Add Event'))
    await addButton?.trigger('click')

    expect(store.openEventModificationDrawer).toHaveBeenCalledWith(
      CreateEditMode.Create,
      expect.objectContaining({
        uei: '',
        eventLabel: '',
        description: ''
      })
    )
  })

  it('should show change status dialog when Disable/Enable Source is clicked', async () => {
    store.showChangeEventConfigSourceStatusDialog = vi.fn()
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    const statusButton = wrapper.findAll('button').find((btn) => btn.text().includes('Disable Source'))
    await statusButton?.trigger('click')

    expect(store.showChangeEventConfigSourceStatusDialog).toHaveBeenCalledWith(mockConfig)
  })

  it('should show delete dialog when Delete Source is clicked', async () => {
    store.showDeleteEventConfigSourceDialog = vi.fn()
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    const deleteButton = wrapper.findAll('button').find((btn) => btn.text().includes('Delete Source'))
    await deleteButton?.trigger('click')

    expect(store.showDeleteEventConfigSourceDialog).toHaveBeenCalledWith(mockConfig)
  })

  it('should fetch events on mount when route id matches selected source', async () => {
    store.fetchEventsBySourceId = vi.fn()
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    expect(store.fetchEventsBySourceId).toHaveBeenCalled()
  })

  it('should not fetch events when route id does not match', async () => {
    const differentConfig = { ...mockConfig, id: 999 }
    store.selectedSource = differentConfig
    store.fetchEventsBySourceId = vi.fn()
    await wrapper.vm.$nextTick()

    expect(store.fetchEventsBySourceId).not.toHaveBeenCalled()
  })

  it('should render EventConfigEventTable component', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'EventConfigEventTable' }).exists()).toBe(true)
  })

  it('should render DeleteEventConfigSourceDialog component', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'DeleteEventConfigSourceDialog' }).exists()).toBe(true)
  })

  it('should render ChangeEventConfigSourceStatusDialog component', async () => {
    wrapper = createWrapper()
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'ChangeEventConfigSourceStatusDialog' }).exists()).toBe(true)
  })

  it('should handle zero event count', async () => {
    const zeroEventConfig = { ...mockConfig, eventCount: 0 }
    wrapper = createWrapper(zeroEventConfig)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('0')
  })
})

