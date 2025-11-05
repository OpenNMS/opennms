import { mount, VueWrapper } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import EventConfigEventCreate from '@/containers/EventConfigEventCreate.vue'

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

  it('renders BasicInformation when selected source exists', async () => {
    detailStore.selectedSource = mockSource
    modificationStore.selectedSource = mockSource

    await wrapper.vm.$forceUpdate()

    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(true)
    expect(wrapper.text()).not.toContain('No event configuration found.')
  })
})
