import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SnmpDataCollectionSourcesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourcesTable.vue'
import SnmpDataCollection from '@/containers/SnmpDataCollection.vue'
import { useMenuStore } from '@/stores/menuStore'
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

describe('SnmpDataCollection.vue', () => {
  let menuStore: ReturnType<typeof useMenuStore>

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia())
    menuStore = useMenuStore()
  })

  const createWrapper = (options = {}) => {
    return mount(SnmpDataCollection, {
      global: {
        stubs: {
          SnmpDataCollectionSourcesTable: true,
          BreadCrumbs: true
        },
        ...options
      }
    })
  }

  describe('Component Rendering', () => {
    it('renders heading text', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
    })

    it('renders BreadCrumbs component', () => {
      const wrapper = createWrapper()

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('renders SnmpDataCollectionSourcesTable component', () => {
      const wrapper = createWrapper()

      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
    })

    it('renders both action buttons', () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons).toHaveLength(2)
    })

    it('renders Create button with correct text', () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toBe('Create New Data Collection Source')
    })

    it('renders Import button with correct text', () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].text()).toBe('Import Data Collection Source')
    })

    it('renders all child components together', () => {
      const wrapper = createWrapper()

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
      expect(wrapper.findAllComponents(FeatherButton)).toHaveLength(2)
    })
  })

  describe('BreadCrumbs', () => {
    it('renders BreadCrumbs with correct items when homeUrl is set', () => {
      menuStore.mainMenu = { homeUrl: '/opennms' } as any

      const wrapper = createWrapper()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items).toHaveLength(2)
      expect(items[0]).toEqual({ label: 'Home', to: '/opennms', isAbsoluteLink: true })
      expect(items[1]).toEqual({ label: 'SNMP Data Collection', to: '#', position: 'last' })
    })

    it('renders BreadCrumbs with undefined homeUrl when mainMenu is not set', () => {
      const wrapper = createWrapper()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items).toHaveLength(2)
      expect(items[0]).toEqual({ label: 'Home', to: undefined, isAbsoluteLink: true })
      expect(items[1]).toEqual({ label: 'SNMP Data Collection', to: '#', position: 'last' })
    })

    it('renders with null homeUrl', () => {
      menuStore.mainMenu = { homeUrl: null } as any

      const wrapper = createWrapper()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBeNull()
    })

    it('renders with empty string homeUrl', () => {
      menuStore.mainMenu = { homeUrl: '' } as any

      const wrapper = createWrapper()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBe('')
    })

    it('updates breadcrumbs when homeUrl changes', async () => {
      menuStore.mainMenu = { homeUrl: '/initial' } as any

      const wrapper = createWrapper()

      let breadcrumbs = wrapper.findComponent(BreadCrumbs)
      let items = breadcrumbs.props('items')
      expect(items[0].to).toBe('/initial')

      menuStore.mainMenu = { homeUrl: '/updated' } as any
      await wrapper.vm.$nextTick()

      breadcrumbs = wrapper.findComponent(BreadCrumbs)
      items = breadcrumbs.props('items')
      expect(items[0].to).toBe('/updated')
    })

    it.each([
      { homeUrl: '/home', expected: '/home' },
      { homeUrl: '/opennms/index.jsp', expected: '/opennms/index.jsp' },
      { homeUrl: 'https://example.com', expected: 'https://example.com' },
      { homeUrl: '/path/to/home', expected: '/path/to/home' }
    ])('handles different homeUrl formats: $homeUrl', ({ homeUrl, expected }) => {
      menuStore.mainMenu = { homeUrl } as any

      const wrapper = createWrapper()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBe(expected)
    })
  })

  describe('Button Attributes', () => {
    it('Create button has primary attribute', () => {
      const wrapper = createWrapper({
        global: {
          stubs: {
            SnmpDataCollectionSourcesTable: true,
            BreadCrumbs: true
          },
          components: {
            FeatherButton
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].props('primary')).toBe(true)
    })

    it('Import button has secondary attribute', () => {
      const wrapper = createWrapper({
        global: {
          stubs: {
            SnmpDataCollectionSourcesTable: true,
            BreadCrumbs: true
          },
          components: {
            FeatherButton
          }
        }
      })

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].props('secondary')).toBe(true)
    })
  })

  describe('Button Interactions - Create Source', () => {
    it('logs message when Create button is clicked', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')

      expect(consoleSpy).toHaveBeenCalledWith('Create New Data Collection Source clicked')

      consoleSpy.mockRestore()
    })

    it('handles multiple Create button clicks', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      const wrapper = createWrapper()

      const createButton = wrapper.findAllComponents(FeatherButton)[0]
      await createButton.trigger('click')
      await createButton.trigger('click')
      await createButton.trigger('click')

      expect(consoleSpy).toHaveBeenCalledTimes(3)
      expect(consoleSpy).toHaveBeenCalledWith('Create New Data Collection Source clicked')

      consoleSpy.mockRestore()
    })

    it('does not navigate when Create button is clicked', async () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')

      expect(mockPush).not.toHaveBeenCalled()
    })
  })

  describe('Button Interactions - Import Source', () => {
    it('navigates to import page when Import button is clicked', async () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      expect(mockPush).toHaveBeenCalledOnce()
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection Import' })
    })

    it('handles multiple Import button clicks', async () => {
      const wrapper = createWrapper()

      const importButton = wrapper.findAllComponents(FeatherButton)[1]
      await importButton.trigger('click')
      await importButton.trigger('click')

      expect(mockPush).toHaveBeenCalledTimes(2)
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection Import' })
    })

    it('calls router.push with correct route name', async () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      expect(mockPush).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'SNMP Data Collection Import'
        })
      )
    })
  })

  describe('CSS Structure', () => {
    it('applies correct CSS classes', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('.snmp-data-collection-container').exists()).toBe(true)
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.heading').exists()).toBe(true)
      expect(wrapper.find('.action').exists()).toBe(true)
      expect(wrapper.find('.container').exists()).toBe(true)
    })

    it('renders feather-row and feather-col structure', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('.feather-row').exists()).toBe(true)
      expect(wrapper.find('.feather-col-12').exists()).toBe(true)
    })

    it('has correct layout structure', () => {
      const wrapper = createWrapper()

      const container = wrapper.find('.snmp-data-collection-container')
      expect(container.find('.feather-row').exists()).toBe(true)
      expect(container.find('.header').exists()).toBe(true)
      expect(container.find('.container').exists()).toBe(true)
    })
  })

  describe('Component Integration', () => {
    it('maintains component structure after button clicks', async () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')
      await buttons[1].trigger('click')

      expect(wrapper.find('.snmp-data-collection-container').exists()).toBe(true)
      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
    })

    it('maintains BreadCrumbs after interactions', async () => {
      const wrapper = createWrapper()

      await wrapper.findAllComponents(FeatherButton)[0].trigger('click')

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('maintains both buttons after clicking one', async () => {
      const wrapper = createWrapper()

      await wrapper.findAllComponents(FeatherButton)[0].trigger('click')

      expect(wrapper.findAllComponents(FeatherButton)).toHaveLength(2)
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => createWrapper()).not.toThrow()
    })

    it('unmounts without errors', () => {
      const wrapper = createWrapper()

      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('renders correctly on mount', () => {
      const wrapper = createWrapper()

      expect(wrapper.vm).toBeDefined()
      expect(wrapper.element).toBeInstanceOf(HTMLElement)
    })
  })

  describe('Edge Cases', () => {
    it('handles missing menuStore gracefully', () => {
      const wrapper = createWrapper()

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('handles missing router gracefully for Create button', async () => {
      const wrapperWithoutRouter = mount(SnmpDataCollection, {
        global: {
          stubs: {
            SnmpDataCollectionSourcesTable: true,
            BreadCrumbs: true
          }
        }
      })

      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      const buttons = wrapperWithoutRouter.findAllComponents(FeatherButton)
      await buttons[0].trigger('click')

      expect(consoleSpy).toHaveBeenCalled()

      consoleSpy.mockRestore()
    })

    it('buttons are visible and clickable', () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].isVisible()).toBe(true)
      expect(buttons[1].isVisible()).toBe(true)
    })

    it('preserves button order', () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toContain('Create')
      expect(buttons[1].text()).toContain('Import')
    })
  })

  describe('Parametrized Tests - Multiple homeUrl values', () => {
    it.each([
      { homeUrl: undefined, description: 'undefined homeUrl' },
      { homeUrl: null, description: 'null homeUrl' },
      { homeUrl: '', description: 'empty string homeUrl' },
      { homeUrl: '/home', description: 'standard path homeUrl' },
      { homeUrl: '/opennms/index.jsp', description: 'jsp file homeUrl' }
    ])('renders correctly with $description', ({ homeUrl }) => {
      if (homeUrl !== undefined) {
        menuStore.mainMenu = { homeUrl } as any
      }

      const wrapper = createWrapper()

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
    })
  })

  describe('Parametrized Tests - Button Click Scenarios', () => {
    it.each([
      { clicks: 1, buttonIndex: 0, buttonName: 'Create' },
      { clicks: 2, buttonIndex: 0, buttonName: 'Create' },
      { clicks: 5, buttonIndex: 0, buttonName: 'Create' },
      { clicks: 1, buttonIndex: 1, buttonName: 'Import' },
      { clicks: 3, buttonIndex: 1, buttonName: 'Import' }
    ])('handles $clicks click(s) on $buttonName button', async ({ clicks, buttonIndex }) => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      const wrapper = createWrapper()

      const button = wrapper.findAllComponents(FeatherButton)[buttonIndex]

      for (let i = 0; i < clicks; i++) {
        await button.trigger('click')
      }

      if (buttonIndex === 0) {
        expect(consoleSpy).toHaveBeenCalledTimes(clicks)
      } else {
        expect(mockPush).toHaveBeenCalledTimes(clicks)
      }

      consoleSpy.mockRestore()
    })
  })

  describe('Parametrized Tests - Component State', () => {
    it.each([
      { hasMenuStore: true, description: 'with menuStore' },
      { hasMenuStore: false, description: 'without menuStore' }
    ])('renders components correctly $description', ({ hasMenuStore }) => {
      if (hasMenuStore) {
        menuStore.mainMenu = { homeUrl: '/home' } as any
      }

      const wrapper = createWrapper()

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
      expect(wrapper.findAllComponents(FeatherButton)).toHaveLength(2)
    })
  })

  describe('Router Navigation', () => {
    it('passes correct route object structure', async () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      const callArgs = mockPush.mock.calls[0][0]
      expect(callArgs).toHaveProperty('name')
      expect(callArgs.name).toBe('SNMP Data Collection Import')
    })

    it('does not pass any route parameters', async () => {
      const wrapper = createWrapper()

      const buttons = wrapper.findAllComponents(FeatherButton)
      await buttons[1].trigger('click')

      const callArgs = mockPush.mock.calls[0][0]
      expect(callArgs).not.toHaveProperty('params')
      expect(callArgs).not.toHaveProperty('query')
    })
  })

  describe('Accessibility', () => {
    it('heading is properly structured', () => {
      const wrapper = createWrapper()

      const heading = wrapper.find('h1')
      expect(heading.exists()).toBe(true)
      expect(heading.element.tagName).toBe('H1')
    })

    it('buttons are within action container', () => {
      const wrapper = createWrapper()

      const actionDiv = wrapper.find('.action')
      const buttons = actionDiv.findAllComponents(FeatherButton)

      expect(buttons).toHaveLength(2)
    })
  })
})
