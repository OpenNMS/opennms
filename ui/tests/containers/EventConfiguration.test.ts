import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EventConfiguration from '@/containers/EventConfiguration.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useMenuStore } from '@/stores/menuStore'
import { FeatherButton } from '@featherds/button'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

describe('EventConfig.vue', () => {
  let store: ReturnType<typeof useEventConfigStore>
  let menuStore: ReturnType<typeof useMenuStore>

  beforeEach(() => {
    setActivePinia(createTestingPinia())
    store = useEventConfigStore()
    menuStore = useMenuStore()
  })

  it('renders heading text', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    expect(store).toBeDefined()
    expect(wrapper.find('h1').text()).toBe('Manage Event Configurations')
    expect(wrapper.findComponent(EventConfigTabContainer).exists()).toBe(true)
  })

  it('renders BreadCrumbs component', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true
        }
      }
    })

    expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
  })

  it('renders BreadCrumbs with correct items', () => {
    menuStore.mainMenu = { homeUrl: '/home' } as any

    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true
        }
      }
    })

    const breadcrumbs = wrapper.findComponent(BreadCrumbs)
    const items = breadcrumbs.props('items')

    expect(items).toHaveLength(2)
    expect(items[0]).toEqual({ label: 'Home', to: '/home', isAbsoluteLink: true })
    expect(items[1]).toEqual({ label: 'Event Configuration', to: '#', position: 'last' })
  })

  it('renders BreadCrumbs with undefined homeUrl when mainMenu is not set', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true
        }
      }
    })

    const breadcrumbs = wrapper.findComponent(BreadCrumbs)
    const items = breadcrumbs.props('items')

    expect(items).toHaveLength(2)
    expect(items[0]).toEqual({ label: 'Home', to: undefined, isAbsoluteLink: true })
    expect(items[1]).toEqual({ label: 'Event Configuration', to: '#', position: 'last' })
  })

  it('renders CreateEventConfigurationDialog component', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          BreadCrumbs: true
        }
      }
    })

    expect(wrapper.findComponent(CreateEventConfigurationDialog).exists()).toBe(true)
  })

  it('renders Create New Event Configuration button', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    const button = wrapper.find('button')
    expect(button.exists()).toBe(true)
    expect(button.text()).toBe('Create New Event Configuration')
  })

  it('calls store method when Create button is clicked', async () => {
    store.showCreateEventConfigSourceDialog = vi.fn()

    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    const button = wrapper.find('button')
    await button.trigger('click')

    expect(store.showCreateEventConfigSourceDialog).toHaveBeenCalledOnce()
  })

  it('applies correct CSS classes', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    expect(wrapper.find('.event-config').exists()).toBe(true)
    expect(wrapper.find('.header').exists()).toBe(true)
    expect(wrapper.find('.heading').exists()).toBe(true)
    expect(wrapper.find('.action').exists()).toBe(true)
    expect(wrapper.find('.tabs').exists()).toBe(true)
  })

  it('renders all child components together', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    expect(wrapper.findComponent(EventConfigTabContainer).exists()).toBe(true)
    expect(wrapper.findComponent(CreateEventConfigurationDialog).exists()).toBe(true)
  })

  it('button has primary attribute', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        },
        components: {
          FeatherButton
        }
      }
    })

    const button = wrapper.findComponent(FeatherButton)
    expect(button.exists()).toBe(true)
    expect(button.props('primary')).toBe(true)
  })

  it('updates breadcrumbs when homeUrl changes', async () => {
    menuStore.mainMenu = { homeUrl: '/initial' } as any

    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true
        }
      }
    })

    let breadcrumbs = wrapper.findComponent(BreadCrumbs)
    let items = breadcrumbs.props('items')
    expect(items[0].to).toBe('/initial')

    menuStore.mainMenu = { homeUrl: '/updated' } as any
    await wrapper.vm.$nextTick()

    breadcrumbs = wrapper.findComponent(BreadCrumbs)
    items = breadcrumbs.props('items')
    expect(items[0].to).toBe('/updated')
  })

  it('maintains component structure after interactions', async () => {
    store.showCreateEventConfigSourceDialog = vi.fn()
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    await wrapper.find('button').trigger('click')

    expect(wrapper.find('.event-config').exists()).toBe(true)
    expect(wrapper.find('h1').text()).toBe('Manage Event Configurations')
  })

  it('renders feather-row and feather-col structure', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    expect(wrapper.find('.feather-row').exists()).toBe(true)
    expect(wrapper.find('.feather-col-12').exists()).toBe(true)
  })

  it('handles multiple button clicks', async () => {
    store.showCreateEventConfigSourceDialog = vi.fn()

    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    const button = wrapper.find('button')
    await button.trigger('click')
    await button.trigger('click')
    await button.trigger('click')

    expect(store.showCreateEventConfigSourceDialog).toHaveBeenCalledTimes(3)
  })

  it('renders with null homeUrl', () => {
    menuStore.mainMenu = { homeUrl: null } as any

    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true
        }
      }
    })

    const breadcrumbs = wrapper.findComponent(BreadCrumbs)
    const items = breadcrumbs.props('items')

    expect(items[0].to).toBeNull()
  })

  it('renders with empty string homeUrl', () => {
    menuStore.mainMenu = { homeUrl: '' } as any

    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true
        }
      }
    })

    const breadcrumbs = wrapper.findComponent(BreadCrumbs)
    const items = breadcrumbs.props('items')

    expect(items[0].to).toBe('')
  })

  it('unmounts without errors', () => {
    const wrapper = mount(EventConfiguration, {
      global: {
        stubs: {
          EventConfigTabContainer: true,
          CreateEventConfigurationDialog: true,
          BreadCrumbs: true
        }
      }
    })

    expect(() => wrapper.unmount()).not.toThrow()
  })
})
