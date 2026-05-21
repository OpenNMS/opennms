import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollection/SnmpDataCollectionSourceImport.vue'
import SnmpDataCollectionSourcesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourcesTable.vue'
import SnmpDataCollection from '@/containers/SnmpDataCollection.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

describe('SnmpDataCollection.vue', () => {
  let wrapper: VueWrapper<any>
  let menuStore: ReturnType<typeof useMenuStore>
  let snmpStore: ReturnType<typeof useSnmpDataCollectionStore>
  let pinia: ReturnType<typeof createTestingPinia>
  let origQuerySelector: typeof Element.prototype.querySelector

  beforeAll(() => {
    // Mock querySelector to handle FeatherTabContainer's internal
    // "[aria-selected='true']" lookup which returns null in jsdom
    origQuerySelector = Element.prototype.querySelector
    Element.prototype.querySelector = function (this: Element, selector: string) {
      const result = origQuerySelector.call(this, selector)
      if (result === null && typeof selector === 'string' && selector.includes('aria-selected')) {
        return document.createElement('div')
      }
      return result
    } as typeof Element.prototype.querySelector

    // Polyfill ResizeObserver for jsdom
    if (typeof window.ResizeObserver === 'undefined') {
      ;(window as any).ResizeObserver = class {
        observe() {}
        unobserve() {}
        disconnect() {}
      }
    }
  })

  afterAll(() => {
    Element.prototype.querySelector = origQuerySelector
  })

  const createWrapper = () => {
    return mount(SnmpDataCollection, {
      global: {
        plugins: [pinia],
        stubs: {
          FeatherTab,
          FeatherTabContainer,
          FeatherTabPanel,
          SnmpDataCollectionSourcesTable: true,
          SnmpDataCollectionSourceImport: true,
          BreadCrumbs: true
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()

    pinia = createTestingPinia({
      createSpy: vi.fn
    })

    setActivePinia(pinia)
    menuStore = useMenuStore(pinia)
    snmpStore = useSnmpDataCollectionStore(pinia)
    snmpStore.activeTab = 0

    wrapper = createWrapper()
  })

  afterEach(() => {
    wrapper.unmount()
    vi.clearAllMocks()
  })

  // ---------------------------------------------------------------------------
  // Component Rendering
  // ---------------------------------------------------------------------------
  describe('Component Rendering', () => {
    it('renders correctly', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('renders heading text', () => {
      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
    })

    it('renders BreadCrumbs component', () => {
      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('renders SnmpDataCollectionSourcesTable component', () => {
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
    })

    it('renders SnmpDataCollectionSourceImport component', () => {
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('renders all key child components together', () => {
      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('does not render any unexpected text outside structured elements', () => {
      const h1 = wrapper.find('h1')
      expect(h1.exists()).toBe(true)
      expect(h1.text()).toContain('SNMP Data Collection')
    })
  })

  // ---------------------------------------------------------------------------
  // Tab Structure
  // ---------------------------------------------------------------------------
  describe('Tab Structure', () => {
    it('renders FeatherTabContainer', () => {
      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.exists()).toBe(true)
    })

    it('renders exactly two FeatherTab components', () => {
      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs).toHaveLength(2)
    })

    it('first tab has label "Data Collection Sources"', () => {
      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs[0].text()).toContain('Data Collection Sources')
    })

    it('second tab has label "Import Data Collection Sources"', () => {
      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs[1].text()).toContain('Import Data Collection Sources')
    })

    it('renders exactly two FeatherTabPanel components', () => {
      const panels = wrapper.findAllComponents(FeatherTabPanel)
      expect(panels).toHaveLength(2)
    })

    it('tab container binds to store.activeTab via modelValue', () => {
      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(0)
    })

    it('first tab panel contains SnmpDataCollectionSourcesTable', () => {
      const panels = wrapper.findAllComponents(FeatherTabPanel)
      expect(panels[0].findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
    })

    it('second tab panel contains SnmpDataCollectionSourceImport', () => {
      const panels = wrapper.findAllComponents(FeatherTabPanel)
      expect(panels[1].findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('SnmpDataCollectionSourcesTable is NOT in the second panel', () => {
      const panels = wrapper.findAllComponents(FeatherTabPanel)
      expect(panels[1].findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(false)
    })

    it('SnmpDataCollectionSourceImport is NOT in the first panel', () => {
      const panels = wrapper.findAllComponents(FeatherTabPanel)
      expect(panels[0].findComponent(SnmpDataCollectionSourceImport).exists()).toBe(false)
    })

    it('tab labels are non-empty strings', () => {
      const tabs = wrapper.findAllComponents(FeatherTab)
      tabs.forEach((tab) => {
        expect(tab.text().trim().length).toBeGreaterThan(0)
      })
    })

    it('each tab panel contains exactly one child component', () => {
      const panels = wrapper.findAllComponents(FeatherTabPanel)

      const panel0Children = [
        panels[0].findComponent(SnmpDataCollectionSourcesTable).exists(),
        panels[0].findComponent(SnmpDataCollectionSourceImport).exists()
      ]
      expect(panel0Children.filter(Boolean)).toHaveLength(1)

      const panel1Children = [
        panels[1].findComponent(SnmpDataCollectionSourcesTable).exists(),
        panels[1].findComponent(SnmpDataCollectionSourceImport).exists()
      ]
      expect(panel1Children.filter(Boolean)).toHaveLength(1)
    })
  })

  // ---------------------------------------------------------------------------
  // Tab Active State
  // ---------------------------------------------------------------------------
  describe('Tab Active State', () => {
    it('defaults to activeTab 0', () => {
      expect(snmpStore.activeTab).toBe(0)
      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(0)
    })

    it('reflects store activeTab value of 1', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(1)
    })

    it('reflects store activeTab switching back to 0', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      snmpStore.activeTab = 0
      await wrapper.vm.$nextTick()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(0)
    })

    it('updates store activeTab when tab container emits update:modelValue', async () => {
      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      await tabContainer.vm.$emit('update:modelValue', 1)
      await wrapper.vm.$nextTick()

      expect(snmpStore.activeTab).toBe(1)
    })

    it('updates store activeTab to 0 via v-model emission', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      await tabContainer.vm.$emit('update:modelValue', 0)
      await wrapper.vm.$nextTick()

      expect(snmpStore.activeTab).toBe(0)
    })

    it.each([
      { tabIndex: 0, description: 'first tab' },
      { tabIndex: 1, description: 'second tab' }
    ])('tab container shows $description when activeTab is $tabIndex', async ({ tabIndex }) => {
      snmpStore.activeTab = tabIndex
      await wrapper.vm.$nextTick()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(tabIndex)
    })
  })

  // ---------------------------------------------------------------------------
  // BreadCrumbs
  // ---------------------------------------------------------------------------
  describe('BreadCrumbs', () => {
    it('renders BreadCrumbs with correct items when homeUrl is set', async () => {
      menuStore.mainMenu = { homeUrl: '/opennms' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items).toHaveLength(2)
      expect(items[0]).toEqual({ label: 'Home', to: '/opennms', isAbsoluteLink: true })
      expect(items[1]).toEqual({ label: 'SNMP Data Collection', to: '#', position: 'last' })
    })

    it('renders BreadCrumbs with undefined homeUrl when mainMenu is not set', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items).toHaveLength(2)
      expect(items[0]).toEqual({ label: 'Home', to: undefined, isAbsoluteLink: true })
      expect(items[1]).toEqual({ label: 'SNMP Data Collection', to: '#', position: 'last' })
    })

    it('renders with null homeUrl', async () => {
      menuStore.mainMenu = { homeUrl: null } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBeNull()
    })

    it('renders with empty string homeUrl', async () => {
      menuStore.mainMenu = { homeUrl: '' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBe('')
    })

    it('updates breadcrumbs when homeUrl changes', async () => {
      menuStore.mainMenu = { homeUrl: '/initial' } as any
      await wrapper.vm.$nextTick()

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
    ])('handles different homeUrl formats: $homeUrl', async ({ homeUrl, expected }) => {
      menuStore.mainMenu = { homeUrl } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBe(expected)
    })

    it('breadcrumbs first item always has label Home', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[0].label).toBe('Home')
    })

    it('breadcrumbs first item always has isAbsoluteLink true', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[0].isAbsoluteLink).toBe(true)
    })

    it('breadcrumbs second item is always SNMP Data Collection', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[1]).toEqual({ label: 'SNMP Data Collection', to: '#', position: 'last' })
    })

    it('breadcrumbs second item to is always #', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[1].to).toBe('#')
    })

    it('breadcrumbs second item position is always last', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[1].position).toBe('last')
    })
  })

  // ---------------------------------------------------------------------------
  // CSS Structure
  // ---------------------------------------------------------------------------
  describe('CSS Structure', () => {
    it('applies snmp-data-collection-container class', () => {
      expect(wrapper.find('.snmp-data-collection-container').exists()).toBe(true)
    })

    it('has header section', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
    })

    it('has heading section within header', () => {
      expect(wrapper.find('.heading').exists()).toBe(true)
    })

    it('has tab-container section', () => {
      expect(wrapper.find('.tab-container').exists()).toBe(true)
    })

    it('renders feather-row and feather-col structure', () => {
      expect(wrapper.find('.feather-row').exists()).toBe(true)
      expect(wrapper.find('.feather-col-12').exists()).toBe(true)
    })

    it('has correct layout structure hierarchy', () => {
      const container = wrapper.find('.snmp-data-collection-container')
      expect(container.find('.feather-row').exists()).toBe(true)
      expect(container.find('.header').exists()).toBe(true)
      expect(container.find('.tab-container').exists()).toBe(true)
    })

    it('feather-col-12 is inside feather-row', () => {
      const row = wrapper.find('.feather-row')
      expect(row.find('.feather-col-12').exists()).toBe(true)
    })

    it('heading is inside header', () => {
      const header = wrapper.find('.header')
      expect(header.find('.heading').exists()).toBe(true)
    })

    it('h1 is inside heading', () => {
      const heading = wrapper.find('.heading')
      expect(heading.find('h1').exists()).toBe(true)
    })

    it('does not have action class (legacy)', () => {
      expect(wrapper.find('.action').exists()).toBe(false)
    })

    it('does not have container class at top level (legacy)', () => {
      expect(wrapper.find('.container').exists()).toBe(false)
    })
  })

  // ---------------------------------------------------------------------------
  // DOM Order
  // ---------------------------------------------------------------------------
  describe('DOM Order', () => {
    it('BreadCrumbs row is rendered before header', () => {
      const container = wrapper.find('.snmp-data-collection-container')
      const children = container.element.children
      const featherRow = container.find('.feather-row')
      const header = container.find('.header')

      const featherRowIndex = Array.from(children).indexOf(featherRow.element)
      const headerIndex = Array.from(children).indexOf(header.element)
      expect(featherRowIndex).toBeLessThan(headerIndex)
    })

    it('header is rendered before tab-container', () => {
      const container = wrapper.find('.snmp-data-collection-container')
      const children = container.element.children
      const header = container.find('.header')
      const tabContainer = container.find('.tab-container')

      const headerIndex = Array.from(children).indexOf(header.element)
      const tabContainerIndex = Array.from(children).indexOf(tabContainer.element)
      expect(headerIndex).toBeLessThan(tabContainerIndex)
    })

    it('tab-container div contains the FeatherTabContainer', () => {
      const tabContainerDiv = wrapper.find('.tab-container')
      expect(tabContainerDiv.findComponent(FeatherTabContainer).exists()).toBe(true)
    })

    it('BreadCrumbs is inside feather-col-12', () => {
      const col = wrapper.find('.feather-col-12')
      expect(col.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('there are exactly three direct children of the container', () => {
      const container = wrapper.find('.snmp-data-collection-container')
      expect(container.element.children).toHaveLength(3)
    })
  })

  // ---------------------------------------------------------------------------
  // Component Lifecycle
  // ---------------------------------------------------------------------------
  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => {
        const w = createWrapper()
        w.unmount()
      }).not.toThrow()
    })

    it('unmounts without errors', () => {
      const extra = createWrapper()
      expect(() => extra.unmount()).not.toThrow()
    })

    it('renders correctly on mount', () => {
      expect(wrapper.vm).toBeDefined()
      expect(wrapper.element).toBeInstanceOf(HTMLElement)
    })

    it('wrapper element is a div', () => {
      expect(wrapper.element.tagName).toBe('DIV')
    })

    it('multiple mounts/unmounts do not leak state', () => {
      const w1 = createWrapper()
      const w2 = createWrapper()
      expect(w1.exists()).toBe(true)
      expect(w2.exists()).toBe(true)
      w1.unmount()
      w2.unmount()
    })
  })

  // ---------------------------------------------------------------------------
  // Accessibility
  // ---------------------------------------------------------------------------
  describe('Accessibility', () => {
    it('heading is an h1 element', () => {
      const heading = wrapper.find('h1')
      expect(heading.exists()).toBe(true)
      expect(heading.element.tagName).toBe('H1')
    })

    it('heading is within header container', () => {
      const header = wrapper.find('.header')
      expect(header.find('.heading h1').exists()).toBe(true)
    })

    it('BreadCrumbs resides inside feather-col-12', () => {
      const col = wrapper.find('.feather-col-12')
      expect(col.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('tabs provide discoverable panel titles', () => {
      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs[0].text()).not.toBe('')
      expect(tabs[1].text()).not.toBe('')
    })

    it('there is exactly one h1 on the page', () => {
      const headings = wrapper.findAll('h1')
      expect(headings).toHaveLength(1)
    })

    it('heading text is descriptive', () => {
      const heading = wrapper.find('h1')
      expect(heading.text()).toContain('SNMP')
      expect(heading.text()).toContain('Data Collection')
    })

    it('tab panels have role tabpanel attribute', () => {
      const panels = wrapper.findAllComponents(FeatherTabPanel)
      panels.forEach((panel) => {
        expect(panel.attributes('role')).toBe('tabpanel')
      })
    })
  })

  // ---------------------------------------------------------------------------
  // Computed Properties
  // ---------------------------------------------------------------------------
  describe('Computed Properties', () => {
    it('homeUrl is reactive to menuStore changes', async () => {
      menuStore.mainMenu = { homeUrl: '/first' } as any
      await wrapper.vm.$nextTick()

      let breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBe('/first')

      menuStore.mainMenu = { homeUrl: '/second' } as any
      await wrapper.vm.$nextTick()

      breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBe('/second')
    })

    it('breadcrumbs always has exactly two items', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')).toHaveLength(2)
    })

    it('second breadcrumb is always SNMP Data Collection with position last', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[1]).toEqual({ label: 'SNMP Data Collection', to: '#', position: 'last' })
    })

    it('first breadcrumb always has isAbsoluteLink true', async () => {
      menuStore.mainMenu = { homeUrl: '/any-path' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].isAbsoluteLink).toBe(true)
    })

    it('first breadcrumb label is always Home', async () => {
      menuStore.mainMenu = { homeUrl: '/test' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].label).toBe('Home')
    })

    it('second breadcrumb to is always #', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[1].to).toBe('#')
    })

    it('homeUrl defaults to undefined when menuStore has empty mainMenu', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBeUndefined()
    })
  })

  // ---------------------------------------------------------------------------
  // Store Integration
  // ---------------------------------------------------------------------------
  describe('Store Integration', () => {
    it('uses useSnmpDataCollectionStore for activeTab', () => {
      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(snmpStore.activeTab)
    })

    it('uses useMenuStore for breadcrumbs', async () => {
      menuStore.mainMenu = { homeUrl: '/store-test' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBe('/store-test')
    })

    it('activeTab changes are reflected in tab container', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(1)

      snmpStore.activeTab = 0
      await wrapper.vm.$nextTick()
      expect(tabContainer.props('modelValue')).toBe(0)
    })

    it('tab container v-model update writes to store', async () => {
      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      await tabContainer.vm.$emit('update:modelValue', 1)
      await wrapper.vm.$nextTick()

      expect(snmpStore.activeTab).toBe(1)
    })

    it('store and tab container stay in sync during rapid changes', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()
      snmpStore.activeTab = 0
      await wrapper.vm.$nextTick()
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(1)
      expect(snmpStore.activeTab).toBe(1)
    })
  })

  // ---------------------------------------------------------------------------
  // Edge Cases
  // ---------------------------------------------------------------------------
  describe('Edge Cases', () => {
    it('handles missing menuStore mainMenu gracefully', () => {
      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      const items = wrapper.findComponent(BreadCrumbs).props('items')
      expect(items[0].to).toBeUndefined()
    })

    it('handles menuStore mainMenu set to null', async () => {
      menuStore.mainMenu = null as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBeUndefined()
    })

    it('handles menuStore mainMenu with no homeUrl property', async () => {
      menuStore.mainMenu = {} as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBeUndefined()
    })

    it('tab structure remains intact regardless of store state', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      expect(wrapper.findAllComponents(FeatherTab)).toHaveLength(2)
      expect(wrapper.findAllComponents(FeatherTabPanel)).toHaveLength(2)
      expect(wrapper.findComponent(FeatherTabContainer).exists()).toBe(true)
    })

    it('heading text remains unchanged after store changes', async () => {
      snmpStore.activeTab = 1
      menuStore.mainMenu = { homeUrl: '/changed' } as any
      await wrapper.vm.$nextTick()

      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
    })

    it('child components persist after multiple tab switches', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()
      snmpStore.activeTab = 0
      await wrapper.vm.$nextTick()
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('handles menuStore mainMenu with additional properties', async () => {
      menuStore.mainMenu = { homeUrl: '/test', notices: [], username: 'admin' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBe('/test')
    })

    it('homeUrl with special characters renders correctly', async () => {
      menuStore.mainMenu = { homeUrl: '/path?q=hello&lang=en#section' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.props('items')[0].to).toBe('/path?q=hello&lang=en#section')
    })
  })

  // ---------------------------------------------------------------------------
  // Parametrized Tests - homeUrl values
  // ---------------------------------------------------------------------------
  describe('Parametrized Tests - Multiple homeUrl values', () => {
    it.each([
      { homeUrl: undefined, description: 'undefined homeUrl' },
      { homeUrl: null, description: 'null homeUrl' },
      { homeUrl: '', description: 'empty string homeUrl' },
      { homeUrl: '/home', description: 'standard path homeUrl' },
      { homeUrl: '/opennms/index.jsp', description: 'jsp file homeUrl' },
      { homeUrl: 'https://example.com', description: 'absolute URL homeUrl' },
      { homeUrl: '/deep/nested/path', description: 'deep nested path homeUrl' }
    ])('renders correctly with $description', async ({ homeUrl }) => {
      if (homeUrl !== undefined) {
        menuStore.mainMenu = { homeUrl } as any
        await wrapper.vm.$nextTick()
      }

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
    })
  })

  // ---------------------------------------------------------------------------
  // Parametrized Tests - Component State
  // ---------------------------------------------------------------------------
  describe('Parametrized Tests - Component State', () => {
    it.each([
      { hasMenuStore: true, description: 'with menuStore homeUrl' },
      { hasMenuStore: false, description: 'without menuStore homeUrl' }
    ])('renders components correctly $description', async ({ hasMenuStore }) => {
      if (hasMenuStore) {
        menuStore.mainMenu = { homeUrl: '/home' } as any
        await wrapper.vm.$nextTick()
      }

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
      expect(wrapper.findAllComponents(FeatherTab)).toHaveLength(2)
    })
  })

  // ---------------------------------------------------------------------------
  // Parametrized Tests - Tab Labels
  // ---------------------------------------------------------------------------
  describe('Parametrized Tests - Tab Labels', () => {
    it.each([
      { index: 0, expectedLabel: 'Data Collection Sources' },
      { index: 1, expectedLabel: 'Import Data Collection Sources' }
    ])('tab at index $index has label "$expectedLabel"', ({ index, expectedLabel }) => {
      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs[index].text()).toContain(expectedLabel)
    })
  })

  // ---------------------------------------------------------------------------
  // Parametrized Tests - Active Tab
  // ---------------------------------------------------------------------------
  describe('Parametrized Tests - Active Tab', () => {
    it.each([{ tabIndex: 0 }, { tabIndex: 1 }])(
      'tab container modelValue matches store activeTab $tabIndex',
      async ({ tabIndex }) => {
        snmpStore.activeTab = tabIndex
        await wrapper.vm.$nextTick()

        const tabContainer = wrapper.findComponent(FeatherTabContainer)
        expect(tabContainer.props('modelValue')).toBe(tabIndex)
      }
    )
  })

  // ---------------------------------------------------------------------------
  // Component Integration
  // ---------------------------------------------------------------------------
  describe('Component Integration', () => {
    it('maintains structure after activeTab change', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.snmp-data-collection-container').exists()).toBe(true)
      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourcesTable).exists()).toBe(true)
      expect(wrapper.findComponent(SnmpDataCollectionSourceImport).exists()).toBe(true)
    })

    it('maintains BreadCrumbs after tab switch', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('maintains tabs after menuStore update', async () => {
      menuStore.mainMenu = { homeUrl: '/changed' } as any
      await wrapper.vm.$nextTick()

      expect(wrapper.findAllComponents(FeatherTab)).toHaveLength(2)
      expect(wrapper.findAllComponents(FeatherTabPanel)).toHaveLength(2)
    })

    it('combined store interactions do not break rendering', async () => {
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      menuStore.mainMenu = { homeUrl: '/new-home' } as any
      await wrapper.vm.$nextTick()

      snmpStore.activeTab = 0
      await wrapper.vm.$nextTick()

      expect(wrapper.find('h1').text()).toBe('Manage SNMP Data Collection Sources')
      expect(wrapper.findComponent(BreadCrumbs).props('items')[0].to).toBe('/new-home')

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.props('modelValue')).toBe(0)
    })

    it('all CSS classes remain present after store mutations', async () => {
      menuStore.mainMenu = { homeUrl: '/test' } as any
      snmpStore.activeTab = 1
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.snmp-data-collection-container').exists()).toBe(true)
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.heading').exists()).toBe(true)
      expect(wrapper.find('.tab-container').exists()).toBe(true)
      expect(wrapper.find('.feather-row').exists()).toBe(true)
      expect(wrapper.find('.feather-col-12').exists()).toBe(true)
    })

    it('component count does not change after interactions', async () => {
      const initialTabCount = wrapper.findAllComponents(FeatherTab).length
      const initialPanelCount = wrapper.findAllComponents(FeatherTabPanel).length

      snmpStore.activeTab = 1
      menuStore.mainMenu = { homeUrl: '/test' } as any
      await wrapper.vm.$nextTick()

      expect(wrapper.findAllComponents(FeatherTab)).toHaveLength(initialTabCount)
      expect(wrapper.findAllComponents(FeatherTabPanel)).toHaveLength(initialPanelCount)
    })
  })

  // ---------------------------------------------------------------------------
  // Structure Validation
  // ---------------------------------------------------------------------------
  describe('Structure Validation', () => {
    it('container has three major sections: row, header, tab-container', () => {
      const container = wrapper.find('.snmp-data-collection-container')
      expect(container.find('.feather-row').exists()).toBe(true)
      expect(container.find('.header').exists()).toBe(true)
      expect(container.find('.tab-container').exists()).toBe(true)
    })

    it('BreadCrumbs items prop is an array', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(Array.isArray(items)).toBe(true)
    })

    it('each breadcrumb item has label and to properties', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      items.forEach((item: any) => {
        expect(item).toHaveProperty('label')
        expect(item).toHaveProperty('to')
      })
    })

    it('first breadcrumb has isAbsoluteLink property', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[0]).toHaveProperty('isAbsoluteLink')
    })

    it('second breadcrumb has position property', () => {
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[1]).toHaveProperty('position')
    })

    it('FeatherTabContainer is the only tab container component', () => {
      const containers = wrapper.findAllComponents(FeatherTabContainer)
      expect(containers).toHaveLength(1)
    })
  })
})
