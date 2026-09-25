import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import EventConfiguration from '@/containers/EventConfiguration.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { useMenuStore } from '@/stores/menuStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
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
        ReorderEventConfigSourcesDrawer: true,
        BreadCrumbs: true,
        OnmsMenu: { name: 'OnmsMenu', props: ['items'], template: '<div class="menu-stub"></div>' }
      }
    }
  })

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ createSpy: vi.fn, stubActions: false }))
    store = useEventConfigStore()
    store.showCreateEventConfigSourceDialog = vi.fn()
    store.showReorderSourcesDrawer = vi.fn()
    useMenuStore().mainMenu = { homeUrl: '/opennms' } as any
    modificationStore = useEventModificationStore()
    modificationStore.openCreateWithoutSource = vi.fn()
    wrapper = mountContainer()
  })

  it('renders the heading and tab container', () => {
    expect(wrapper.find('h1').text()).toBe('Manage Event Configurations')
    expect(wrapper.findComponent(EventConfigTabContainer).exists()).toBe(true)
  })

  it('renders the Reorder Sources button and the single Create menu button', () => {
    expect(wrapper.find('[data-test="reorder-sources-button"]').text()).toContain('Reorder Sources')
    expect(wrapper.find('[data-test="create-menu-button"]').text()).toContain('Create')
  })

  it('mounts the reorder drawer', () => {
    expect(wrapper.findComponent({ name: 'ReorderEventConfigSourcesDrawer' }).exists()).toBe(true)
  })

  it('opens the reorder drawer from the header button', async () => {
    await wrapper.find('[data-test="reorder-sources-button"]').trigger('click')
    expect(store.showReorderSourcesDrawer).toHaveBeenCalled()
  })

  it('the Create menu opens the create-source dialog and the create-event flow', () => {
    const items = wrapper.vm.createMenuItems
    expect(items.map((i: any) => i.label)).toEqual(['New Event Source', 'New Event Config'])

    items[0].command()
    expect(store.showCreateEventConfigSourceDialog).toHaveBeenCalled()

    items[1].command()
    expect(modificationStore.openCreateWithoutSource).toHaveBeenCalledWith(CreateEditMode.Create, expect.any(Object))
    expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration Create' })
  })

  it('builds breadcrumbs with the home url', () => {
    expect(wrapper.vm.breadcrumbs[0]).toMatchObject({ label: 'Home', to: '/opennms', isAbsoluteLink: true })
    expect(wrapper.vm.breadcrumbs[1]).toMatchObject({ label: 'Manage Event Configurations', position: 'last' })
  })
})
