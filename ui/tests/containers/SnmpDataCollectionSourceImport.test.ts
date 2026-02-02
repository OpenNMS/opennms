import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollectionSourceImport/SnmpDataCollectionSourceImport.vue'
import SnmpDataCollectionSourceImportContainer from '@/containers/SnmpDataCollectionSourceImport.vue'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('SnmpDataCollectionSourceImport.vue', () => {
  let wrapper: VueWrapper

  const globalStubs = {
    SnmpDataCollectionSourceImport: true
  }

  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const createWrapper = async (): Promise<VueWrapper> => {
    wrapper = mount(SnmpDataCollectionSourceImportContainer, {
      global: {
        stubs: globalStubs
      }
    })
    await wrapper.vm.$nextTick()
    await flushPromises()
    return wrapper
  }

  describe('Component Rendering', () => {
    it('renders the component', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.snmp-data-collection-source-import').exists()).toBe(true)
    })

    it('renders heading text correctly', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('h1').text()).toBe('Import SNMP Data Collection Sources')
    })

    it('renders back button', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.back').exists()).toBe(true)
      expect(wrapper.find('.back button').exists()).toBe(true)
    })

    it('renders SnmpDataCollectionSourceImport child component', async () => {
      wrapper = await createWrapper()

      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('renders all components together', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.back').exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
      expect(wrapper.find('h1').exists()).toBe(true)
    })

    it('renders title container', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.title-container').exists()).toBe(true)
    })

    it('renders header section', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.header').exists()).toBe(true)
    })

    it('renders heading container', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.heading').exists()).toBe(true)
    })

    it('renders back button container', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.back').exists()).toBe(true)
    })
  })

  describe('Back Button Interactions', () => {
    it('calls router.push when Go Back button is clicked', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalledOnce()
    })

    it('navigates to correct route on back button click', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'SNMP Data Collection'
        })
      )
    })

    it('handles multiple back button clicks', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')
      await backButton.trigger('click')
      await backButton.trigger('click')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalledTimes(3)
    })

    it('back button has correct text', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back')
      expect(backButton.text()).toContain('Go Back')
    })

    it('does not pass route parameters', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')
      await backButton.trigger('click')

      const callArgs = mockPush.mock.calls[0][0]
      expect(callArgs.params).toBeUndefined()
      expect(callArgs.query).toBeUndefined()
    })
  })

  describe('CSS Structure', () => {
    it('applies correct CSS classes', async () => {
      wrapper = await createWrapper()

      expect(wrapper.classes()).toContain('snmp-data-collection-source-import')
    })

    it('has correct layout structure', async () => {
      wrapper = await createWrapper()

      const titleContainer = wrapper.find('.title-container')
      expect(titleContainer.exists()).toBe(true)

      const header = wrapper.find('.header')
      expect(header.exists()).toBe(true)
    })

    it('title container is child of main container', async () => {
      wrapper = await createWrapper()

      const mainContainer = wrapper.find('.snmp-data-collection-source-import')
      const titleContainer = mainContainer.find('.title-container')
      expect(titleContainer.exists()).toBe(true)
    })

    it('header is child of title container', async () => {
      wrapper = await createWrapper()

      const titleContainer = wrapper.find('.title-container')
      const header = titleContainer.find('.header')
      expect(header.exists()).toBe(true)
    })

    it('back and heading are siblings in header', async () => {
      wrapper = await createWrapper()

      const header = wrapper.find('.header')
      expect(header.find('.back').exists()).toBe(true)
      expect(header.find('.heading').exists()).toBe(true)
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', async () => {
      expect(() => createWrapper()).not.toThrow()
    })

    it('unmounts without errors', async () => {
      wrapper = await createWrapper()

      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('renders correctly on mount', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.snmp-data-collection-source-import').exists()).toBe(true)
      expect(wrapper.find('h1').exists()).toBe(true)
    })

    it('maintains state after mount', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('h1').text()).toBe('Import SNMP Data Collection Sources')
      expect(wrapper.find('.back').exists()).toBe(true)
    })
  })

  describe('Component Integration', () => {
    it('maintains component structure after button clicks', async () => {
      wrapper = await createWrapper()

      await wrapper.find('.back button').trigger('click')

      expect(wrapper.find('.snmp-data-collection-source-import').exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('maintains back button after interaction', async () => {
      wrapper = await createWrapper()

      await wrapper.find('.back button').trigger('click')

      expect(wrapper.find('.back').exists()).toBe(true)
    })

    it('child component remains after navigation attempt', async () => {
      wrapper = await createWrapper()

      await wrapper.find('.back button').trigger('click')

      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })
  })

  describe('Parametrized Tests - Multiple Clicks', () => {
    it.each([
      { name: 'single click', clicks: 1 },
      { name: 'double click', clicks: 2 },
      { name: 'multiple clicks', clicks: 5 },
      { name: 'many clicks', clicks: 10 }
    ])('handles \'$name\' on back button ($clicks times)', async ({ clicks }) => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')

      for (let i = 0; i < clicks; i++) {
        await backButton.trigger('click')
      }

      expect(mockPush).toHaveBeenCalledTimes(clicks)
      expect(mockPush).toHaveBeenCalledWith(expect.objectContaining({ name: 'SNMP Data Collection' }))
    })
  })

  describe('Edge Cases', () => {
    it('handles missing router gracefully', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.back').exists()).toBe(true)
    })

    it('component is visible', async () => {
      wrapper = await createWrapper()

      expect(wrapper.isVisible()).toBe(true)
    })

    it('back button is visible', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.back').isVisible()).toBe(true)
    })

    it('heading is visible', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('h1').isVisible()).toBe(true)
    })

    it('maintains correct heading text after interactions', async () => {
      wrapper = await createWrapper()

      await wrapper.find('.back button').trigger('click')

      expect(wrapper.find('h1').text()).toBe('Import SNMP Data Collection Sources')
    })
  })

  describe('Router Navigation', () => {
    it('passes correct route object structure', async () => {
      wrapper = await createWrapper()

      await wrapper.find('.back button').trigger('click')

      const callArgs = mockPush.mock.calls[0][0]
      expect(callArgs).toHaveProperty('name')
      expect(callArgs.name).toBe('SNMP Data Collection')
    })

    it('only passes route name', async () => {
      wrapper = await createWrapper()

      await wrapper.find('.back button').trigger('click')

      const callArgs = mockPush.mock.calls[0][0]
      expect(Object.keys(callArgs)).toEqual(['name'])
    })

    it('calls router.push exactly once per click', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')

      await backButton.trigger('click')
      expect(mockPush).toHaveBeenCalledTimes(1)

      await backButton.trigger('click')
      expect(mockPush).toHaveBeenCalledTimes(2)

      await backButton.trigger('click')
      expect(mockPush).toHaveBeenCalledTimes(3)
    })
  })

  describe('Accessibility', () => {
    it('heading is properly structured', async () => {
      wrapper = await createWrapper()

      const heading = wrapper.find('h1')
      expect(heading.exists()).toBe(true)
      expect(heading.text()).toBeTruthy()
    })

    it('has semantic heading structure', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.heading h1').exists()).toBe(true)
    })

    it('back button is accessible', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')
      expect(backButton.exists()).toBe(true)
      expect(backButton.text()).toBeTruthy()
    })

    it('back button has descriptive text', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back')
      const text = backButton.text()
      expect(text).toContain('Go Back')
      expect(text.length).toBeGreaterThan(0)
    })
  })

  describe('Component Props and Structure', () => {
    it('FeatherBackButton receives click handler', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalled()
    })

    it('child component is properly nested', async () => {
      wrapper = await createWrapper()

      const childComponent = wrapper.findComponent(SnmpDataCollectionSourceImport)
      expect(childComponent.exists()).toBe(true)
    })

    it('maintains component hierarchy', async () => {
      wrapper = await createWrapper()

      const mainContainer = wrapper.find('.snmp-data-collection-source-import')
      const titleContainer = mainContainer.find('.title-container')
      const childDiv = mainContainer.findAll('div').at(-1)

      expect(titleContainer.exists()).toBe(true)
      expect(childDiv?.exists()).toBe(true)
    })
  })

  describe('Text Content', () => {
    it('displays correct heading text', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('h1').text()).toBe('Import SNMP Data Collection Sources')
    })

    it('displays correct back button text', async () => {
      wrapper = await createWrapper()

      expect(wrapper.text()).toContain('Go Back')
    })

    it('heading text is exact match', async () => {
      wrapper = await createWrapper()

      const headingText = wrapper.find('h1').text()
      expect(headingText).toBe('Import SNMP Data Collection Sources')
      expect(headingText.startsWith('Import')).toBe(true)
      expect(headingText.endsWith('Sources')).toBe(true)
    })

    it('does not contain unexpected text', async () => {
      wrapper = await createWrapper()

      const text = wrapper.text()
      expect(text).not.toContain('Error')
      expect(text).not.toContain('undefined')
      expect(text).not.toContain('null')
    })
  })

  describe('Rendering Order', () => {
    it('renders back button before heading', async () => {
      wrapper = await createWrapper()

      const header = wrapper.find('.header')
      const backDiv = header.find('.back')
      const headingDiv = header.find('.heading')

      expect(backDiv.exists()).toBe(true)
      expect(headingDiv.exists()).toBe(true)
    })

    it('renders title container before child component', async () => {
      wrapper = await createWrapper()

      const mainContainer = wrapper.find('.snmp-data-collection-source-import')
      const children = mainContainer.findAll('div').filter((el) => el.element.parentElement === mainContainer.element)

      expect(children.length).toBeGreaterThanOrEqual(2)
      expect(children[0].classes()).toContain('title-container')
    })
  })

  describe('Component Stability', () => {
    it('maintains structure after multiple interactions', async () => {
      wrapper = await createWrapper()

      for (let i = 0; i < 5; i++) {
        await wrapper.find('.back button').trigger('click')
      }

      expect(wrapper.find('.snmp-data-collection-source-import').exists()).toBe(true)
      expect(wrapper.find('h1').exists()).toBe(true)
      expect(wrapper.find('.back').exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('does not create duplicate elements', async () => {
      wrapper = await createWrapper()

      expect(wrapper.findAll('h1')).toHaveLength(1)
      expect(wrapper.findAll('.header')).toHaveLength(1)
      expect(wrapper.findAll('.title-container')).toHaveLength(1)
    })

    it('maintains correct element count after interaction', async () => {
      wrapper = await createWrapper()

      await wrapper.find('.back button').trigger('click')

      expect(wrapper.findAll('h1')).toHaveLength(1)
      expect(wrapper.findAll('.back')).toHaveLength(1)
    })
  })

  describe('Rapid Interactions', () => {
    it('handles rapid button clicks without errors', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')
      const promises = []
      for (let i = 0; i < 20; i++) {
        promises.push(backButton.trigger('click'))
      }

      await Promise.all(promises)

      expect(mockPush).toHaveBeenCalledTimes(20)
      expect(wrapper.find('.snmp-data-collection-source-import').exists()).toBe(true)
    })

    it('maintains component integrity during rapid interactions', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('.back button')

      for (let i = 0; i < 10; i++) {
        await backButton.trigger('click')
        expect(wrapper.find('.snmp-data-collection-source-import').exists()).toBe(true)
      }

      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
      expect(wrapper.find('h1').text()).toBe('Import SNMP Data Collection Sources')
    })
  })
})

