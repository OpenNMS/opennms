// ui/tests/containers/Nodes.test.ts
//
// NOTE: this file only covers the listInterfaces=true -> nodeStructureStore.setShowInterfaces
// wiring added for NMS-20125 (Task 4). A fuller container test suite for Nodes.vue is expected
// to land separately (Task 6); please extend this file rather than creating a second one.
import Nodes from '@/containers/Nodes.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
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
  const mountComponent = () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    setActivePinia(pinia)

    const structure = useNodeStructureStore()
    // Skip the deferred-query path: pretend categories/locations/service-types are already loaded.
    structure.categoriesLoaded = true
    structure.monitoringLocationsLoaded = true
    structure.serviceTypesLoaded = true

    const menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/home' } as any

    const wrapper = mount(Nodes, {
      global: {
        plugins: [PrimeVue, pinia],
        stubs: {
          NodesTable: true,
          BreadCrumbs: true
        }
      }
    })

    return { wrapper, structure }
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
})
