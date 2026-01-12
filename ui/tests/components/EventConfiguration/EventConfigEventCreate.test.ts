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

  it('renders BasicInformation when both selected source and event config event exist', async () => {
    modificationStore.selectedSource = mockSource
    modificationStore.eventModificationState.eventConfigEvent = mockEvent

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(true)
    expect(wrapper.text()).not.toContain('No event configuration found.')
  })
})
