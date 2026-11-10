// ui/tests/containers/Nodes.test.ts
//
// NOTE: originally this file only covered the listInterfaces=true -> nodeListStore.setShowInterfaces
// wiring added for NMS-20125 (Task 4). It has since been extended (Task 6) with coverage for the
// legacy `?nodeId=<n>` bookmark redirect. Please extend this file further rather than creating a
// second one.
import Nodes from '@/containers/Nodes.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeListStore } from '@/stores/nodeListStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

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
  const mountComponent = (mainMenu: Record<string, unknown> = { homeUrl: '/home' }) => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    setActivePinia(pinia)

    const structure = useNodeListStore()
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
    it('replaces the route with Node Details when nodeId is a positive integer', async () => {
      routeQuery = { nodeId: '42' }

      mountComponent()
      await flushPromises()

      expect(routerReplaceMock).toHaveBeenCalledWith({ name: 'Node Details', params: { id: '42' }})
    })

    it('does not redirect when nodeId is non-numeric, and normal query handling proceeds', async () => {
      routeQuery = { nodeId: 'abc' }

      mountComponent()
      await flushPromises()

      expect(routerReplaceMock).not.toHaveBeenCalledWith(expect.objectContaining({ name: 'Node Details' }))
    })

    it('does not redirect when nodeId is zero or negative', async () => {
      routeQuery = { nodeId: '-1' }

      mountComponent()
      await flushPromises()

      expect(routerReplaceMock).not.toHaveBeenCalledWith(expect.objectContaining({ name: 'Node Details' }))
    })

    it('does not redirect when nodeId is absent from the route query', async () => {
      routeQuery = {}

      mountComponent()
      await flushPromises()

      expect(routerReplaceMock).not.toHaveBeenCalledWith(expect.objectContaining({ name: 'Node Details' }))
    })
  })
})
