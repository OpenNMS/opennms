// ui/tests/containers/Nodes.test.ts
//
// NOTE: originally this file only covered the listInterfaces=true -> nodeStructureStore.setShowInterfaces
// wiring added for NMS-20125 (Task 4). It has since been extended (Task 6) with coverage for the
// legacy `?nodeId=<n>` bookmark redirect. Please extend this file further rather than creating a
// second one.
import Nodes from '@/containers/Nodes.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

let routeQuery: Record<string, unknown> = {}
const routerReplaceMock = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    get query() {
      return routeQuery
    }
  })),
  useRouter: vi.fn(() => ({
    push: vi.fn(),
    replace: routerReplaceMock
  }))
}))

vi.mock('@/services/localStorageService', () => ({
  loadNodePreferences: vi.fn().mockReturnValue(null),
  saveNodeQueryFilter: vi.fn()
}))

describe('Nodes.vue container', () => {
  let replaceSpy: ReturnType<typeof vi.spyOn>

  const mountComponent = (mainMenu: Record<string, unknown> = { homeUrl: '/home' }) => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    setActivePinia(pinia)

    const structure = useNodeStructureStore()
    // Skip the deferred-query path: pretend categories/locations/service-types are already loaded.
    structure.categoriesLoaded = true
    structure.monitoringLocationsLoaded = true
    structure.serviceTypesLoaded = true

    const menuStore = useMenuStore()
    menuStore.mainMenu = mainMenu as any

    const wrapper = mount(Nodes, {
      global: {
        plugins: [PrimeVue, pinia],
        stubs: {
          NodesTable: true,
          BreadCrumbs: true
        }
      }
    })

    return { wrapper, structure, menuStore }
  }

  beforeEach(() => {
    vi.clearAllMocks()
    routeQuery = {}
    replaceSpy = vi.spyOn(window.location, 'replace').mockImplementation(() => { /* no-op: prevent jsdom navigation */ })
  })

  afterEach(() => {
    replaceSpy.mockRestore()
  })

  it('sets showInterfaces when the route query has listInterfaces=true', async () => {
    routeQuery = { listInterfaces: 'true' }

    const { structure } = mountComponent()
    await flushPromises()

    expect(structure.showInterfaces).toBe(true)
  })

  it('is case-insensitive when reading listInterfaces', async () => {
    routeQuery = { listInterfaces: 'True' }

    const { structure } = mountComponent()
    await flushPromises()

    expect(structure.showInterfaces).toBe(true)
  })

  it('does not set showInterfaces when listInterfaces is absent from the route query', async () => {
    routeQuery = {}

    const { structure } = mountComponent()
    await flushPromises()

    expect(structure.showInterfaces).toBe(false)
  })

  it('does not set showInterfaces when listInterfaces is false', async () => {
    routeQuery = { listInterfaces: 'false' }

    const { structure } = mountComponent()
    await flushPromises()

    expect(structure.showInterfaces).toBe(false)
  })

  describe('legacy nodeId redirect', () => {
    const loadedMainMenu = { homeUrl: '/home', baseHref: '/opennms/', baseNodeUrl: 'element/node.jsp?node=' }

    it('redirects to the node detail page when nodeId is a positive integer', async () => {
      routeQuery = { nodeId: '42' }

      mountComponent(loadedMainMenu)
      await flushPromises()

      expect(replaceSpy).toHaveBeenCalledWith('/opennms/element/node.jsp?node=42')
      // The redirect bypasses normal query handling entirely — the URL should not be cleared.
      expect(routerReplaceMock).not.toHaveBeenCalled()
    })

    it('defers the redirect until menuStore.mainMenu finishes loading', async () => {
      routeQuery = { nodeId: '7' }

      // mainMenu has not loaded yet (no baseHref/baseNodeUrl).
      const { menuStore } = mountComponent({})
      await flushPromises()

      expect(replaceSpy).not.toHaveBeenCalled()

      menuStore.mainMenu = loadedMainMenu as any
      await flushPromises()

      expect(replaceSpy).toHaveBeenCalledWith('/opennms/element/node.jsp?node=7')
    })

    it('does not redirect when nodeId is non-numeric, and normal query handling proceeds', async () => {
      routeQuery = { nodeId: 'abc' }

      mountComponent(loadedMainMenu)
      await flushPromises()

      expect(replaceSpy).not.toHaveBeenCalled()
    })

    it('does not redirect when nodeId is zero or negative', async () => {
      routeQuery = { nodeId: '-1' }

      mountComponent(loadedMainMenu)
      await flushPromises()

      expect(replaceSpy).not.toHaveBeenCalled()
    })

    it('does not redirect when nodeId is absent from the route query', async () => {
      routeQuery = {}

      mountComponent(loadedMainMenu)
      await flushPromises()

      expect(replaceSpy).not.toHaveBeenCalled()
    })
  })
})
