import EventConfigEventCreate from '@/containers/EventConfigEventCreate.vue'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent, EventConfigEventJsonStructure } from '@/types/eventConfig'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
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

const mockEvent: EventConfigEvent = {
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
  fileOrder: 1,
  jsonContent: {} as EventConfigEventJsonStructure
}

describe('EventConfigSourceDetail.vue', () => {
  let detailStore: ReturnType<typeof useEventConfigDetailStore>
  let modificationStore: ReturnType<typeof useEventModificationStore>
  let wrapper: VueWrapper<any>

  beforeEach(() => {
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

  it('renders "not found" state when no selected source', () => {
    expect(wrapper.text()).toContain('No event configuration found.')
    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(false)
  })

  it('navigates back when button is clicked', async () => {
    const btn = wrapper.get('button')
    await btn.trigger('click')

    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })

  it('renders BasicInformation when both selected source and event config event exist', async () => {
    modificationStore.selectedSource = mockSource
    modificationStore.eventModificationState.eventConfigEvent = mockEvent

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(true)
    expect(wrapper.text()).not.toContain('No event configuration found.')
  })

  it('renders "not found" when selectedSource exists but eventConfigEvent is null', async () => {
    modificationStore.selectedSource = mockSource
    modificationStore.eventModificationState.eventConfigEvent = null

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(false)
    expect(wrapper.text()).toContain('No event configuration found.')
  })

  it('renders "not found" when eventConfigEvent exists but selectedSource is null', async () => {
    modificationStore.selectedSource = null
    modificationStore.eventModificationState.eventConfigEvent = mockEvent

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(false)
    expect(wrapper.text()).toContain('No event configuration found.')
  })

  it('navigates to Event Configuration Detail when source has id', async () => {
    modificationStore.selectedSource = mockSource

    const component = wrapper.vm
    component.goBack()

    expect(mockPush).toHaveBeenCalledWith({
      name: 'Event Configuration Detail',
      params: { id: mockSource.id }
    })
  })
})
