import ThresholdConfiguration from '@/containers/ThresholdConfiguration.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock } = vi.hoisted(() => ({ showSnackBarMock: vi.fn() }))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

describe('ThresholdConfiguration.vue', () => {
  let groupStore: ReturnType<typeof useThresholdGroupStore>
  let threshdStore: ReturnType<typeof useThreshdConfigurationStore>
  let menuStore: ReturnType<typeof useMenuStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = () => {
    wrapper = mount(ThresholdConfiguration, {
      global: {
        plugins: [PrimeVue],
        stubs: {
          BreadCrumbs: true,
          ThresholdConfigTabContainer: true
        }
      }
    })
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    groupStore = useThresholdGroupStore()
    threshdStore = useThreshdConfigurationStore()
    menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/home' } as any

    groupStore.fetchGroups = vi.fn().mockResolvedValue(ok)
    groupStore.fetchMetadata = vi.fn().mockResolvedValue(ok)
    threshdStore.fetchConfiguration = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    // Unmount so PrimeVue TabList's orphaned setTimeout(updateInkBar) cannot fire against a torn-down DOM.
    wrapper?.unmount()
  })

  it('renders the heading the admin page and menu smoke tests look for', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('h1').text()).toBe('Threshold Configuration')
    expect(wrapper.findComponent(BreadCrumbs).exists()).toBe(true)
  })

  it('builds breadcrumbs from the menu store home URL', () => {
    const wrapper = mountComponent()

    const items = wrapper.findComponent(BreadCrumbs).props('items') as any[]
    expect(items).toHaveLength(2)
    expect(items[0]).toMatchObject({ label: 'Home', to: '/home', isAbsoluteLink: true })
    expect(items[1]).toMatchObject({ label: 'Threshold Configuration', position: 'last' })
  })

  it('loads both configurations on mount', async () => {
    mountComponent()
    await new Promise(resolve => setTimeout(resolve))

    expect(groupStore.fetchGroups).toHaveBeenCalledTimes(1)
    expect(threshdStore.fetchConfiguration).toHaveBeenCalledTimes(1)
    expect(groupStore.fetchMetadata).toHaveBeenCalledTimes(1)
  })

  it('reports a failure but still loads the other configuration', async () => {
    // The two documents are independent; one being unreadable should not blank out the whole page.
    groupStore.fetchGroups = vi.fn().mockResolvedValue({ success: false, message: 'No thresholding configuration.' })

    mountComponent()
    await new Promise(resolve => setTimeout(resolve))

    expect(threshdStore.fetchConfiguration).toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith(
      expect.objectContaining({ msg: 'No thresholding configuration.', error: true })
    )
  })

  it('says nothing when both loads succeed', async () => {
    mountComponent()
    await new Promise(resolve => setTimeout(resolve))

    expect(showSnackBarMock).not.toHaveBeenCalled()
  })
})
