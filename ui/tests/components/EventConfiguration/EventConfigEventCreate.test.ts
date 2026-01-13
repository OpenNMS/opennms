import { mount, VueWrapper } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import EventConfigEventCreate from '@/containers/EventConfigEventCreate.vue'
import { CreateEditMode } from '@/types'

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

vi.mock('@/services/eventConfigService', () => ({
  createEventConfigEvent: vi.fn(),
  updateEventConfigEventById: vi.fn(),
  addEventConfigSource: vi.fn()
}))

vi.mock('@/stores/eventConfigStore', () => ({
  useEventConfigStore: vi.fn(() => ({
    uploadedSources: [
      {
        id: 1,
        name: 'Test Source'
      },
      {
        id: 2,
        name: 'Another Source'
      }
    ],
    fetchAllSourcesNames: vi.fn().mockResolvedValue(undefined)
  }))
}))

const mockSource = {
  id: 1,
  name: 'Test Source',
  vendor: 'Test Vendor',
  description: 'Some description',
  enabled: true,
  eventCount: 5,
  fileOrder: 1,
  uploadedBy: 'Tester',
  createdTime: new Date(),
  lastModified: new Date()
}

const mockEvent = {
  id: 1,
  uei: 'test-uei',
  eventLabel: 'Test Event',
  description: 'Test Description',
  severity: 'NORMAL',
  enabled: true,
  xmlContent: '<event>test</event>',
  createdTime: new Date(),
  lastModified: new Date(),
  modifiedBy: 'Tester',
  sourceName: 'Test Source',
  vendor: 'Test Vendor',
  fileOrder: 1
}

describe('EventConfigEventCreate.vue', () => {
  let detailStore: ReturnType<typeof useEventConfigDetailStore>
  let modificationStore: ReturnType<typeof useEventModificationStore>
  let wrapper: VueWrapper<any>

  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockClear()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    detailStore = useEventConfigDetailStore(pinia)
    modificationStore = useEventModificationStore(pinia)
    detailStore.selectedSource = null
    modificationStore.selectedSource = null
    modificationStore.eventModificationState = {
      isEditMode: CreateEditMode.None,
      eventConfigEvent: null
    }

    wrapper = mount(EventConfigEventCreate, {
      global: {
        plugins: [pinia]
      }
    })
  })

  it('renders BasicInformation when isEditMode is Create', async () => {
    modificationStore.selectedSource = mockSource
    modificationStore.eventModificationState.isEditMode = CreateEditMode.Create
    modificationStore.eventModificationState.eventConfigEvent = mockEvent

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(true)
    expect(wrapper.text()).not.toContain('No event configuration found.')
  })

  it('renders BasicInformation when isEditMode is Edit', async () => {
    modificationStore.selectedSource = mockSource
    modificationStore.eventModificationState.isEditMode = CreateEditMode.Edit
    modificationStore.eventModificationState.eventConfigEvent = mockEvent

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(true)
    expect(wrapper.text()).not.toContain('No event configuration found.')
  })

  it('renders "No event configuration found" message when isEditMode is None', async () => {
    modificationStore.eventModificationState.isEditMode = CreateEditMode.None

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(false)
    expect(wrapper.text()).toContain('No event configuration found.')
    expect(wrapper.find('button').exists()).toBe(true)
    expect(wrapper.find('button').text()).toBe('Go Back')
  })

  it('navigates to Event Configuration Detail when Go Back is clicked and selectedSource.id exists', async () => {
    modificationStore.eventModificationState.isEditMode = CreateEditMode.None
    modificationStore.selectedSource = mockSource

    await wrapper.vm.$forceUpdate()

    const goBackButton = wrapper.find('button')
    await goBackButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Detail',
      params: { id: mockSource.id }
    })
  })

  it('navigates to Event Configuration when Go Back is clicked and selectedSource.id does not exist', async () => {
    modificationStore.eventModificationState.isEditMode = CreateEditMode.None
    modificationStore.selectedSource = null

    await wrapper.vm.$forceUpdate()

    const goBackButton = wrapper.find('button')
    await goBackButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration'
    })
  })

  it('navigates to Event Configuration when Go Back is clicked and selectedSource has no id', async () => {
    modificationStore.eventModificationState.isEditMode = CreateEditMode.None
    modificationStore.selectedSource = { ...mockSource, id: undefined } as any

    await wrapper.vm.$forceUpdate()

    const goBackButton = wrapper.find('button')
    await goBackButton.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration'
    })
  })
})
