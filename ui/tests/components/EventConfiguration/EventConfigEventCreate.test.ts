import EventConfigEventCreate from '@/containers/EventConfigEventCreate.vue'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

const mockSource = {
  id: 1,
  name: 'Test Source',
  vendor: 'Test Vendor',
  enabled: true
}

describe('EventConfigEventCreate.vue (container)', () => {
  let modificationStore: ReturnType<typeof useEventModificationStore>
  let wrapper: VueWrapper<any>

  const mountContainer = () => mount(EventConfigEventCreate, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      // Stub the heavy form so this suite focuses on the container's branching.
      stubs: { BasicInformation: { name: 'BasicInformation', template: '<div class="basic-information-stub"></div>' }}
    }
  })

  beforeEach(() => {
    vi.clearAllMocks()
    wrapper = mountContainer()
    modificationStore = useEventModificationStore()
    modificationStore.selectedSource = null as any
    modificationStore.eventModificationState = { isEditMode: CreateEditMode.None, eventConfigEvent: null } as any
  })

  it.each([
    [CreateEditMode.Create],
    [CreateEditMode.Edit]
  ])('renders BasicInformation when isEditMode is %s', async (mode) => {
    modificationStore.selectedSource = mockSource as any
    modificationStore.eventModificationState.isEditMode = mode
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(true)
    expect(wrapper.text()).not.toContain('No event configuration found.')
  })

  it('renders the not-found message when isEditMode is None', async () => {
    modificationStore.eventModificationState.isEditMode = CreateEditMode.None
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent({ name: 'BasicInformation' }).exists()).toBe(false)
    expect(wrapper.text()).toContain('No event configuration found.')
    expect(wrapper.find('button').text()).toContain('Go Back')
  })

  it('navigates to the source detail on Go Back when a source id exists', async () => {
    modificationStore.eventModificationState.isEditMode = CreateEditMode.None
    modificationStore.selectedSource = mockSource as any
    await wrapper.vm.$nextTick()
    await wrapper.find('button').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration Detail', params: { id: mockSource.id }})
  })

  it('navigates to the list on Go Back when no source id exists', async () => {
    modificationStore.eventModificationState.isEditMode = CreateEditMode.None
    modificationStore.selectedSource = null as any
    await wrapper.vm.$nextTick()
    await wrapper.find('button').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })
})
