import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import EventConfiguration from '@/containers/EventConfiguration.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { useMenuStore } from '@/stores/menuStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

describe('EventConfiguration.vue (container)', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>
  let modificationStore: ReturnType<typeof useEventModificationStore>

  const mountContainer = () => mount(EventConfiguration, {
    global: {
      plugins: [PrimeVue],
      stubs: {
        EventConfigTabContainer: true,
        CreateEventConfigurationDialog: true,
        BreadCrumbs: true
      }
    }
  })

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ createSpy: vi.fn, stubActions: false }))
    store = useEventConfigStore()
    store.showCreateEventConfigSourceDialog = vi.fn()
    useMenuStore().mainMenu = { homeUrl: '/opennms' } as any
    modificationStore = useEventModificationStore()
    modificationStore.openCreateWithoutSource = vi.fn()
    wrapper = mountContainer()
  })

  it('renders the heading and tab container', () => {
    expect(wrapper.find('h1').text()).toBe('Manage Event Configurations')
    expect(wrapper.findComponent(EventConfigTabContainer).exists()).toBe(true)
  })

  it('renders the two create buttons', () => {
    const labels = wrapper.findAllComponents(Button).map(b => b.props('label'))
    expect(labels).toContain('Create New Event Source')
    expect(labels).toContain('Create New Event Config')
  })

  it('opens the create-source dialog from the first button', async () => {
    const btn = wrapper.findAllComponents(Button).find(b => b.props('label') === 'Create New Event Source')
    await btn?.trigger('click')
    expect(store.showCreateEventConfigSourceDialog).toHaveBeenCalled()
  })

  it('navigates to the create-event flow from the second button', async () => {
    const btn = wrapper.findAllComponents(Button).find(b => b.props('label') === 'Create New Event Config')
    await btn?.trigger('click')
    expect(modificationStore.openCreateWithoutSource).toHaveBeenCalledWith(CreateEditMode.Create, expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration Create' })
  })

  it('builds breadcrumbs with the home url', () => {
    expect(wrapper.vm.breadcrumbs[0]).toMatchObject({ label: 'Home', to: '/opennms', isAbsoluteLink: true })
    expect(wrapper.vm.breadcrumbs[1]).toMatchObject({ label: 'Manage Event Configurations', position: 'last' })
  })
})
