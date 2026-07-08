import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import TrapdConfiguration from '@/containers/TrapdConfiguration.vue'
import { validateTrapdXml } from '@/lib/trapdValidator'
import { useMenuStore } from '@/stores/menuStore'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: showSnackBarMock
  })
}))

vi.mock('@/lib/trapdValidator', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/lib/trapdValidator')>()
  return {
    ...actual,
    validateTrapdXml: vi.fn()
  }
})

describe('TrapdConfiguration.vue', () => {
  let trapStore: ReturnType<typeof useTrapdConfigStore>
  let menuStore: ReturnType<typeof useMenuStore>

  const validateTrapdXmlMock = vi.mocked(validateTrapdXml)
  const mountComponent = () => {
    return mount(TrapdConfiguration, {
      global: {
        plugins: [PrimeVue],
        stubs: {
          GeneralConfiguration: true,
          SnmpV3UserManagement: true,
          CreateSnmpV3User: true,
          TrapdAdvancedConfiguration: true,
          BreadCrumbs: true
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    trapStore = useTrapdConfigStore()
    menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/home' } as any
    trapStore.fetchTrapConfig = vi.fn().mockResolvedValue(undefined)
    validateTrapdXmlMock.mockReturnValue({ valid: true, errors: [] })
  })

  it('renders heading and child sections', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('h2').text()).toBe('Trap Configuration')
    expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
  })

  it('renders breadcrumbs with home and trap configuration entries', () => {
    const wrapper = mountComponent()
    const breadcrumbs = wrapper.findComponent(BreadCrumbs)
    const items = breadcrumbs.props('items')

    expect(items).toHaveLength(2)
    expect(items[0]).toEqual({ label: 'Home', to: '/home', isAbsoluteLink: true })
    expect(items[1]).toEqual({ label: 'Trap Configuration', to: '#', position: 'last' })
  })

  it('calls fetchTrapConfig on mount', () => {
    mountComponent()
    expect(trapStore.fetchTrapConfig).toHaveBeenCalledTimes(1)
  })

  it('shows snackbar when initial fetch fails with Error', async () => {
    trapStore.fetchTrapConfig = vi.fn().mockRejectedValue(new Error('initial fetch failed'))

    mountComponent()
    await Promise.resolve()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'initial fetch failed', error: true })
  })

  it('shows snackbar when initial fetch fails with non-Error', async () => {
    trapStore.fetchTrapConfig = vi.fn().mockRejectedValue('boom')

    mountComponent()
    await Promise.resolve()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to retrieve trapd configuration.', error: true })
  })
})
