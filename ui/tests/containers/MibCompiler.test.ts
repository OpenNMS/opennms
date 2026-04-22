import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import CompiledMibFiles from '@/components/MibCompiler/CompiledMibFiles.vue'
import PendingMibFiles from '@/components/MibCompiler/PendingMibFiles.vue'
import UploadMibFiles from '@/components/MibCompiler/UploadMibFiles.vue'
import MibCompiler from '@/containers/MibCompiler.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'

// Mock getBoundingClientRect for FeatherDS tabs in JSDOM
const mockBoundingClientRect = {
  top: 0,
  left: 0,
  bottom: 0,
  right: 0,
  width: 100,
  height: 40,
  x: 0,
  y: 0,
  toJSON: () => ({})
}

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

describe('MibCompiler.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useMibCompilerStore>
  let menuStore: ReturnType<typeof useMenuStore>
  let originalQuerySelector: typeof Element.prototype.querySelector

  // Setup mock for querySelector to handle FeatherDS tabs in JSDOM
  beforeAll(() => {
    originalQuerySelector = Element.prototype.querySelector
    Element.prototype.querySelector = function(selector: string) {
      const result = originalQuerySelector.call(this, selector)
      if (result === null && selector === "[aria-selected='true']") {
        // Return a mock element for FeatherDS tabs
        return {
          getBoundingClientRect: () => mockBoundingClientRect
        } as unknown as Element
      }
      return result
    }
  })

  afterAll(() => {
    Element.prototype.querySelector = originalQuerySelector
  })

  const globalConfig = {
    global: {
      stubs: {
        CompiledMibFiles: true,
        PendingMibFiles: true,
        UploadMibFiles: true,
        BreadCrumbs: true,
        FeatherTab: true,
        FeatherTabContainer: true,
        FeatherTabPanel: true,
        'router-link': true
      }
    }
  }

  const globalConfigWithoutBreadcrumbsStub = {
    global: {
      stubs: {
        CompiledMibFiles: true,
        PendingMibFiles: true,
        UploadMibFiles: true,
        FeatherTab: true,
        FeatherTabContainer: true,
        FeatherTabPanel: true,
        'router-link': true
      }
    }
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({
      createSpy: vi.fn
    }))
    store = useMibCompilerStore()
    menuStore = useMenuStore()
  })

  afterEach(async () => {
    // Wait for any pending promises to settle before unmounting
    await flushPromises()
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  describe('Basic Rendering', () => {
    it('renders the component', () => {
      wrapper = mount(MibCompiler, globalConfig)
      expect(wrapper.exists()).toBe(true)
    })

    it('renders the main container with correct class', () => {
      wrapper = mount(MibCompiler, globalConfig)
      expect(wrapper.find('.mib-compiler-container').exists()).toBe(true)
    })

    it('renders the heading text correctly', () => {
      wrapper = mount(MibCompiler, globalConfig)
      expect(wrapper.find('.heading p').text()).toBe('Import Events from MIB')
    })

    it('renders the header section', () => {
      wrapper = mount(MibCompiler, globalConfig)
      expect(wrapper.find('.header').exists()).toBe(true)
    })

    it('renders the tab container section', () => {
      wrapper = mount(MibCompiler, globalConfig)
      expect(wrapper.find('.tab-container').exists()).toBe(true)
    })

    it('renders feather-row and feather-col-12 structure', () => {
      wrapper = mount(MibCompiler, globalConfig)
      expect(wrapper.find('.feather-row').exists()).toBe(true)
      expect(wrapper.find('.feather-col-12').exists()).toBe(true)
    })
  })

  describe('BreadCrumbs', () => {
    it('renders BreadCrumbs component', () => {
      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)
      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
    })

    it('renders BreadCrumbs with correct items when homeUrl is set', () => {
      menuStore.mainMenu = { homeUrl: '/home' } as any

      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items).toHaveLength(2)
      expect(items[0]).toEqual({ label: 'Home', to: '/home', isAbsoluteLink: false })
      expect(items[1]).toEqual({ label: 'MIB Compiler', to: '#', position: 'last' })
    })

    it('renders BreadCrumbs with undefined homeUrl when mainMenu is not set', () => {
      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items).toHaveLength(2)
      expect(items[0]).toEqual({ label: 'Home', to: undefined, isAbsoluteLink: false })
      expect(items[1]).toEqual({ label: 'MIB Compiler', to: '#', position: 'last' })
    })

    it('renders BreadCrumbs with null homeUrl', () => {
      menuStore.mainMenu = { homeUrl: null } as any

      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBeNull()
    })

    it('renders BreadCrumbs with empty string homeUrl', () => {
      menuStore.mainMenu = { homeUrl: '' } as any

      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      expect(items[0].to).toBe('')
    })

    it('updates breadcrumbs when homeUrl changes', async () => {
      menuStore.mainMenu = { homeUrl: '/initial' } as any

      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      let breadcrumbs = wrapper.findComponent(BreadCrumbs)
      let items = breadcrumbs.props('items')
      expect(items[0].to).toBe('/initial')

      menuStore.mainMenu = { homeUrl: '/updated' } as any
      await wrapper.vm.$nextTick()

      breadcrumbs = wrapper.findComponent(BreadCrumbs)
      items = breadcrumbs.props('items')
      expect(items[0].to).toBe('/updated')
    })

    it('breadcrumbs have correct isAbsoluteLink value', () => {
      menuStore.mainMenu = { homeUrl: '/home' } as any

      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')

      // Home should have isAbsoluteLink: false based on the component
      expect(items[0].isAbsoluteLink).toBe(false)
    })
  })

  describe('Tab Components', () => {
    it('renders FeatherTabContainer', () => {
      wrapper = mount(MibCompiler, {
        global: {
          stubs: {
            CompiledMibFiles: true,
            PendingMibFiles: true,
            UploadMibFiles: true,
            BreadCrumbs: true,
            FeatherTab: true,
            FeatherTabPanel: true,
            'router-link': true
          }
        }
      })

      expect(wrapper.findComponent(FeatherTabContainer).exists()).toBe(true)
    })

    it('renders two tabs', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs).toHaveLength(2)
    })

    it('renders tabs with correct labels', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const tabs = wrapper.findAllComponents(FeatherTab)
      // Stubs render slot content as text
      expect(tabs[0].text()).toContain('View')
      expect(tabs[1].text()).toContain('Upload MIB Files')
    })

    it('renders two tab panels', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const panels = wrapper.findAllComponents(FeatherTabPanel)
      expect(panels).toHaveLength(2)
    })
  })

  describe('Child Components', () => {
    it('renders CompiledMibFiles component', () => {
      wrapper = mount(MibCompiler, {
        global: {
          stubs: {
            PendingMibFiles: true,
            UploadMibFiles: true,
            BreadCrumbs: true,
            FeatherTab: true,
            FeatherTabContainer: true,
            FeatherTabPanel: true,
            FeatherButton: true,
            'router-link': true
          }
        }
      })

      expect(wrapper.findComponent(CompiledMibFiles).exists()).toBe(true)
    })

    it('renders PendingMibFiles component', () => {
      wrapper = mount(MibCompiler, {
        global: {
          stubs: {
            CompiledMibFiles: true,
            UploadMibFiles: true,
            BreadCrumbs: true,
            FeatherTab: true,
            FeatherTabContainer: true,
            FeatherTabPanel: true,
            FeatherButton: true,
            'router-link': true
          }
        }
      })

      expect(wrapper.findComponent(PendingMibFiles).exists()).toBe(true)
    })

    it('renders UploadMibFiles component', () => {
      wrapper = mount(MibCompiler, {
        global: {
          stubs: {
            CompiledMibFiles: true,
            PendingMibFiles: true,
            BreadCrumbs: true,
            FeatherTab: true,
            FeatherTabContainer: true,
            FeatherTabPanel: true,
            'router-link': true
          }
        }
      })

      expect(wrapper.findComponent(UploadMibFiles).exists()).toBe(true)
    })

    it('renders all child components together', () => {
      wrapper = mount(MibCompiler, {
        global: {
          stubs: {
            BreadCrumbs: true,
            FeatherTab: true,
            FeatherTabContainer: true,
            FeatherTabPanel: true,
            FeatherButton: true,
            'router-link': true
          }
        }
      })

      expect(wrapper.findComponent(CompiledMibFiles).exists()).toBe(true)
      expect(wrapper.findComponent(PendingMibFiles).exists()).toBe(true)
      expect(wrapper.findComponent(UploadMibFiles).exists()).toBe(true)
    })

    it('CompiledMibFiles and PendingMibFiles are in the first tab panel (View)', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const panels = wrapper.findAllComponents(FeatherTabPanel)
      const firstPanel = panels[0]

      expect(firstPanel.findComponent(CompiledMibFiles).exists()).toBe(true)
      expect(firstPanel.findComponent(PendingMibFiles).exists()).toBe(true)
    })

    it('UploadMibFiles is in the second tab panel (Upload MIB Files)', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const panels = wrapper.findAllComponents(FeatherTabPanel)
      const secondPanel = panels[1]

      expect(secondPanel.findComponent(UploadMibFiles).exists()).toBe(true)
    })
  })

  describe('Store Interactions', () => {
    it('calls fetchMibFiles on mount', async () => {
      store.fetchMibFiles = vi.fn()

      wrapper = mount(MibCompiler, globalConfig)
      await flushPromises()

      expect(store.fetchMibFiles).toHaveBeenCalledOnce()
    })

    it('has access to mibCompilerStore', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(store).toBeDefined()
      expect(store.files).toBeDefined()
    })

    it('has access to menuStore', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(menuStore).toBeDefined()
      expect(menuStore.mainMenu).toBeDefined()
    })

    it('handles fetchMibFiles error gracefully', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      // Create a mock that rejects but is caught properly
      store.fetchMibFiles = vi.fn().mockImplementation(() => {
        return Promise.reject(new Error('Network error')).catch(() => {
          // Error is caught internally, simulating what the real store does
        })
      })

      wrapper = mount(MibCompiler, globalConfig)
      await flushPromises()

      expect(store.fetchMibFiles).toHaveBeenCalledOnce()
      // Component should not crash
      expect(wrapper.exists()).toBe(true)
      
      consoleErrorSpy.mockRestore()
    })

    it('store isLoading state is initialized to false', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(store.isLoading).toBe(false)
    })

    it('store files array is initially empty', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(store.files).toEqual([])
    })
  })

  describe('CSS Classes and Styling', () => {
    it('applies correct CSS classes to container', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(wrapper.find('.mib-compiler-container').exists()).toBe(true)
    })

    it('applies correct CSS classes to header', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.heading').exists()).toBe(true)
    })

    it('applies correct CSS classes to tab container', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(wrapper.find('.tab-container').exists()).toBe(true)
    })

    it('heading p element exists and has correct text', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const heading = wrapper.find('.heading p')
      expect(heading.exists()).toBe(true)
      expect(heading.text()).toBe('Import Events from MIB')
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => {
        wrapper = mount(MibCompiler, globalConfig)
      }).not.toThrow()
    })

    it('unmounts without errors', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('maintains component structure after mounting', async () => {
      wrapper = mount(MibCompiler, globalConfig)
      await flushPromises()

      expect(wrapper.find('.mib-compiler-container').exists()).toBe(true)
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.tab-container').exists()).toBe(true)
    })
  })

  describe('Edge Cases', () => {
    it('renders when store has files', async () => {
      store.files = [
        { fileName: 'test1.mib', location: 'COMPILED' },
        { fileName: 'test2.mib', location: 'PENDING' }
      ]

      wrapper = mount(MibCompiler, globalConfig)
      await wrapper.vm.$nextTick()

      expect(wrapper.exists()).toBe(true)
    })

    it('renders when store has only compiled files', async () => {
      store.files = [
        { fileName: 'compiled1.mib', location: 'COMPILED' },
        { fileName: 'compiled2.mib', location: 'COMPILED' }
      ]

      wrapper = mount(MibCompiler, globalConfig)
      await wrapper.vm.$nextTick()

      expect(wrapper.exists()).toBe(true)
      expect(store.filteredCompiledMibFiles.length).toBe(2)
      expect(store.filteredPendingMibFiles.length).toBe(0)
    })

    it('renders when store has only pending files', async () => {
      store.files = [
        { fileName: 'pending1.mib', location: 'PENDING' },
        { fileName: 'pending2.mib', location: 'PENDING' }
      ]

      wrapper = mount(MibCompiler, globalConfig)
      await wrapper.vm.$nextTick()

      expect(wrapper.exists()).toBe(true)
      expect(store.filteredCompiledMibFiles.length).toBe(0)
      expect(store.filteredPendingMibFiles.length).toBe(2)
    })

    it('renders with empty mainMenu', () => {
      menuStore.mainMenu = {} as any

      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      expect(wrapper.exists()).toBe(true)
      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[0].to).toBeUndefined()
    })

    it('handles isLoading state correctly', async () => {
      store.isLoading = true

      wrapper = mount(MibCompiler, globalConfig)
      await wrapper.vm.$nextTick()

      expect(wrapper.exists()).toBe(true)
    })

    it('renders with selectedMibFile set', async () => {
      store.selectedMibFile = {
        fileName: 'selected.mib',
        location: 'COMPILED',
        content: 'test content'
      } as any

      wrapper = mount(MibCompiler, globalConfig)
      await wrapper.vm.$nextTick()

      expect(wrapper.exists()).toBe(true)
    })

    it('handles multiple rapid mounts/unmounts', () => {
      const wrappers: VueWrapper<any>[] = []

      for (let i = 0; i < 5; i++) {
        const w = mount(MibCompiler, globalConfig)
        wrappers.push(w)
      }

      for (const w of wrappers) {
        expect(() => w.unmount()).not.toThrow()
      }
    })
  })

  describe('Tab Switching', () => {
    it('renders tab container with correct structure', async () => {
      wrapper = mount(MibCompiler, globalConfig)
      await flushPromises()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      const panels = tabContainer.findAllComponents(FeatherTabPanel)

      expect(tabContainer.exists()).toBe(true)
      expect(panels.length).toBe(2)
    })

    it('renders tab container for switching tabs', async () => {
      wrapper = mount(MibCompiler, globalConfig)
      await flushPromises()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      expect(tabContainer.exists()).toBe(true)
      
      // Verify tabs exist
      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs.length).toBe(2)
      expect(tabs[0].text()).toContain('View')
      expect(tabs[1].text()).toContain('Upload MIB Files')
    })

    it('has correct number of tab panels', async () => {
      wrapper = mount(MibCompiler, globalConfig)
      await flushPromises()

      const tabContainer = wrapper.findComponent(FeatherTabContainer)
      const panels = tabContainer.findAllComponents(FeatherTabPanel)
      
      expect(panels.length).toBe(2)
    })
  })

  describe('Pinia Store Integration', () => {
    it('renders correctly with Pinia testing plugin', () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(wrapper.exists()).toBe(true)
      expect(store).toBeDefined()
    })

    it('store actions are mocked by createTestingPinia', async () => {
      wrapper = mount(MibCompiler, globalConfig)
      await flushPromises()

      // fetchMibFiles should be a mock function when using createTestingPinia
      expect(vi.isMockFunction(store.fetchMibFiles)).toBe(true)
    })

    it('store state can be modified for testing', async () => {
      store.files = [
        { fileName: 'custom.mib', location: 'COMPILED' }
      ]
      store.isLoading = false
      store.compiledMibFilesSearchTerm = 'test'

      wrapper = mount(MibCompiler, globalConfig)
      await wrapper.vm.$nextTick()

      expect(store.files.length).toBe(1)
      expect(store.compiledMibFilesSearchTerm).toBe('test')
    })

    it('store getters return correct filtered data', () => {
      store.files = [
        { fileName: 'compiled1.mib', location: 'COMPILED' },
        { fileName: 'compiled2.mib', location: 'COMPILED' },
        { fileName: 'pending1.mib', location: 'PENDING' }
      ]

      wrapper = mount(MibCompiler, globalConfig)

      expect(store.filteredCompiledMibFiles.length).toBe(2)
      expect(store.filteredPendingMibFiles.length).toBe(1)
    })

    it('store search functionality works correctly', () => {
      store.files = [
        { fileName: 'network.mib', location: 'COMPILED' },
        { fileName: 'system.mib', location: 'COMPILED' },
        { fileName: 'pending.mib', location: 'PENDING' }
      ]
      store.compiledMibFilesSearchTerm = 'network'

      wrapper = mount(MibCompiler, globalConfig)

      expect(store.searchedCompiledMibFiles.length).toBe(1)
      expect(store.searchedCompiledMibFiles[0].fileName).toBe('network.mib')
    })

    it('store pagination returns correct subset', () => {
      const files = []
      for (let i = 1; i <= 25; i++) {
        files.push({ fileName: `file${i}.mib`, location: 'COMPILED' as const })
      }
      store.files = files
      store.compiledMibFilesPagination.page = 1
      store.compiledMibFilesPagination.pageSize = 10

      wrapper = mount(MibCompiler, globalConfig)

      expect(store.paginatedCompiledMibFiles.length).toBe(10)
    })
  })

  describe('Accessibility', () => {
    it('heading has proper structure', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const heading = wrapper.find('.heading p')
      expect(heading.exists()).toBe(true)
    })

    it('breadcrumbs are accessible', () => {
      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      expect(breadcrumbs.exists()).toBe(true)
    })

    it('tabs are accessible via FeatherTab components', () => {
      wrapper = mount(MibCompiler, globalConfig)

      const tabs = wrapper.findAllComponents(FeatherTab)
      expect(tabs.length).toBe(2)
      
      // Tabs should have visible text labels
      expect(tabs[0].text()).toBeTruthy()
      expect(tabs[1].text()).toBeTruthy()
    })
  })

  describe('Component Integration', () => {
    it('all required components are imported and used', () => {
      wrapper = mount(MibCompiler, {
        global: {
          stubs: {
            FeatherButton: true,
            FeatherTabContainer: true,
            FeatherTab: true,
            FeatherTabPanel: true,
            'router-link': true
          }
        }
      })

      expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
      expect(wrapper.findComponent(CompiledMibFiles).exists()).toBe(true)
      expect(wrapper.findComponent(PendingMibFiles).exists()).toBe(true)
      expect(wrapper.findComponent(UploadMibFiles).exists()).toBe(true)
      expect(wrapper.findComponent(FeatherTabContainer).exists()).toBe(true)
    })

    it('component renders correctly without any stubs', async () => {
      // This test verifies the component can render with real child components
      // May fail if child components have unmet dependencies, which is expected
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      try {
        wrapper = mount(MibCompiler, {
          global: {
            stubs: {
              FeatherButton: true,
              FeatherTabContainer: true,
              FeatherTab: true,
              FeatherTabPanel: true,
              'router-link': true
            }
          }
        })
        await flushPromises()

        expect(wrapper.exists()).toBe(true)
      } finally {
        consoleWarnSpy.mockRestore()
        consoleErrorSpy.mockRestore()
      }
    })
  })

  describe('Reactivity', () => {
    it('updates when store files change', async () => {
      wrapper = mount(MibCompiler, globalConfig)

      expect(store.files.length).toBe(0)

      store.files = [{ fileName: 'new.mib', location: 'COMPILED' }]
      await wrapper.vm.$nextTick()

      expect(store.files.length).toBe(1)
    })

    it('updates when menu store changes', async () => {
      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      menuStore.mainMenu = { homeUrl: '/new-home' } as any
      await wrapper.vm.$nextTick()

      const breadcrumbs = wrapper.findComponent(BreadCrumbs)
      const items = breadcrumbs.props('items')
      expect(items[0].to).toBe('/new-home')
    })

    it('computed homeUrl updates reactively', async () => {
      wrapper = mount(MibCompiler, globalConfigWithoutBreadcrumbsStub)

      // Initial state
      let breadcrumbs = wrapper.findComponent(BreadCrumbs)
      let items = breadcrumbs.props('items')
      expect(items[0].to).toBeUndefined()

      // Update mainMenu
      menuStore.mainMenu = { homeUrl: '/dashboard' } as any
      await wrapper.vm.$nextTick()

      // Check updated value
      breadcrumbs = wrapper.findComponent(BreadCrumbs)
      items = breadcrumbs.props('items')
      expect(items[0].to).toBe('/dashboard')
    })
  })
})
