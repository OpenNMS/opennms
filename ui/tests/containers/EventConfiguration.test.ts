import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EventConfiguration from '@/containers/EventConfiguration.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { useMenuStore } from '@/stores/menuStore'
import { CreateEditMode } from '@/types'
import { FeatherButton } from '@featherds/button'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('EventConfig.vue', () => {
  let store: ReturnType<typeof useEventConfigStore>
  let menuStore: ReturnType<typeof useMenuStore>
  let modificationStore: ReturnType<typeof useEventModificationStore>

  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockClear()
    setActivePinia(createTestingPinia())
    store = useEventConfigStore()
    menuStore = useMenuStore()
    modificationStore = useEventModificationStore()
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
    expect(items[1]).toEqual({ label: 'Manage Event Configurations', to: '#', position: 'last' })
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
    expect(items[1]).toEqual({ label: 'Manage Event Configurations', to: '#', position: 'last' })
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
    expect(button.text()).toBe('Create New Event Source')
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

  describe('Create New Event Config Button', () => {
    it('renders "Create New Event Config" button', () => {
      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
      expect(buttons[1].text()).toBe('Create New Event Config')
    })

    it('renders both action buttons', () => {
      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toBe('Create New Event Source')
      expect(buttons[1].text()).toBe('Create New Event Config')
    })

    it('both buttons have primary attribute', () => {
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

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].props('primary')).toBe(true)
      expect(buttons[1].props('primary')).toBe(true)
    })

    it('calls goToCreateEventConfig when Create New Event Config button is clicked', async () => {
      modificationStore.openCreateWithoutSource = vi.fn()

      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      expect(modificationStore.openCreateWithoutSource).toHaveBeenCalledWith(
        CreateEditMode.Create,
        expect.objectContaining({
          uei: '',
          eventLabel: '',
          description: '',
          enabled: true
        })
      )
      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration Create' })
    })

    it('navigates to Event Configuration Create route', async () => {
      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration Create' })
    })

    it('sets up modification store with Create mode', async () => {
      modificationStore.openCreateWithoutSource = vi.fn()

      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      expect(modificationStore.openCreateWithoutSource).toHaveBeenCalledWith(
        CreateEditMode.Create,
        expect.any(Object)
      )
    })

    it('sets up modification store with default event config', async () => {
      modificationStore.openCreateWithoutSource = vi.fn()

      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      const callArg = (modificationStore.openCreateWithoutSource as any).mock.calls[0][1]
      expect(callArg).toHaveProperty('id')
      expect(callArg.id).toBeGreaterThan(0)
      expect(callArg).toHaveProperty('uei', '')
      expect(callArg).toHaveProperty('eventLabel', '')
      expect(callArg).toHaveProperty('enabled', true)
      expect(callArg).toHaveProperty('severity')
      expect(callArg).toHaveProperty('xmlContent', '')
    })

    it('handles multiple clicks on Create New Event Config button', async () => {
      modificationStore.openCreateWithoutSource = vi.fn()

      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')
      await buttons[1].trigger('click')

      expect(modificationStore.openCreateWithoutSource).toHaveBeenCalledTimes(2)
      expect(mockPush).toHaveBeenCalledTimes(2)
    })
  })

  describe('Button Interactions', () => {
    it('Create New Event Source button click does not affect Event Config button', async () => {
      store.showCreateEventConfigSourceDialog = vi.fn()
      modificationStore.openCreateWithoutSource = vi.fn()

      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')

      expect(store.showCreateEventConfigSourceDialog).toHaveBeenCalledOnce()
      expect(modificationStore.openCreateWithoutSource).not.toHaveBeenCalled()
      expect(mockPush).not.toHaveBeenCalled()
    })

    it('Create New Event Config button click does not affect Event Source button', async () => {
      store.showCreateEventConfigSourceDialog = vi.fn()
      modificationStore.openCreateWithoutSource = vi.fn()

      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      expect(modificationStore.openCreateWithoutSource).toHaveBeenCalledOnce()
      expect(mockPush).toHaveBeenCalledOnce()
      expect(store.showCreateEventConfigSourceDialog).not.toHaveBeenCalled()
    })

    it('can click both buttons in sequence', async () => {
      store.showCreateEventConfigSourceDialog = vi.fn()
      modificationStore.openCreateWithoutSource = vi.fn()

      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')
      await buttons[1].trigger('click')

      expect(store.showCreateEventConfigSourceDialog).toHaveBeenCalledOnce()
      expect(modificationStore.openCreateWithoutSource).toHaveBeenCalledOnce()
      expect(mockPush).toHaveBeenCalledOnce()
    })
  })

  describe('Component Layout', () => {
    it('buttons are in action div', () => {
      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const actionDiv = wrapper.find('.action')
      const buttons = actionDiv.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
    })

    it('heading and action are in header', () => {
      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const header = wrapper.find('.header')
      expect(header.find('.heading').exists()).toBe(true)
      expect(header.find('.action').exists()).toBe(true)
    })

    it('maintains proper structure with all sections', () => {
      const wrapper = mount(EventConfiguration, {
        global: {
          stubs: {
            EventConfigTabContainer: true,
            CreateEventConfigurationDialog: true,
            BreadCrumbs: true
          }
        }
      })

      const eventConfig = wrapper.find('.event-config')
      expect(eventConfig.find('.feather-row').exists()).toBe(true)
      expect(eventConfig.find('.header').exists()).toBe(true)
      expect(eventConfig.find('.tabs').exists()).toBe(true)
    })
  })
})
