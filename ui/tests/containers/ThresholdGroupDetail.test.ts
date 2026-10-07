import ThresholdGroupDetail from '@/containers/ThresholdGroupDetail.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import ThresholdDefinitionTable from '@/components/ThresholdConfiguration/Group/ThresholdDefinitionTable.vue'
import { ThresholdDefinitionKind } from '@/lib/thresholdValidator'
import { useMenuStore } from '@/stores/menuStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, replaceMock, routeParams } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  replaceMock: vi.fn(),
  routeParams: { value: { name: 'mib2' } as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: routeParams.value }),
  useRouter: () => ({ push: pushMock, replace: replaceMock })
}))

describe('ThresholdGroupDetail.vue', () => {
  let store: ReturnType<typeof useThresholdGroupStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = () => {
    wrapper = mount(ThresholdGroupDetail, {
      global: {
        plugins: [PrimeVue],
        stubs: {
          BreadCrumbs: true,
          ThresholdDefinitionDrawer: true,
          ThresholdGroupDrawer: true,
          ThresholdGroupHeader: true,
          ThresholdDefinitionTable: true
        }
      }
    })
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    routeParams.value = { name: 'mib2' }
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useThresholdGroupStore()
    store.currentGroup = { name: 'mib2', rrdRepository: '/rrd', thresholds: [], expressions: [] }
    store.fetchGroup = vi.fn().mockResolvedValue(ok)
    store.fetchGroups = vi.fn().mockResolvedValue(ok)
    store.fetchMetadata = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('loads the group named in the route', async () => {
    mountComponent()
    await new Promise(resolve => setTimeout(resolve))

    expect(store.fetchGroup).toHaveBeenCalledWith('mib2')
  })

  it('decodes a name that had to be escaped in the URL', async () => {
    // Group names are free-form, so the route carries them percent-encoded.
    routeParams.value = { name: 'my%20group' }

    mountComponent()
    await new Promise(resolve => setTimeout(resolve))

    expect(store.fetchGroup).toHaveBeenCalledWith('my group')
  })

  it('renders one table per definition kind', () => {
    const wrapper = mountComponent()

    const tables = wrapper.findAllComponents(ThresholdDefinitionTable)
    expect(tables).toHaveLength(2)
    expect(tables[0].props('kind')).toBe(ThresholdDefinitionKind.Threshold)
    expect(tables[1].props('kind')).toBe(ThresholdDefinitionKind.Expression)
  })

  it('marks both tables read only for an extension-provided group', () => {
    store.currentGroup!.readOnly = true

    const wrapper = mountComponent()

    expect(wrapper.findAllComponents(ThresholdDefinitionTable).every(table => table.props('readOnly'))).toBe(true)
  })

  it('shows a not-found state instead of an empty page for an unknown group', () => {
    store.currentGroup = null
    store.isLoading = false

    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="threshold-group-not-found"]').exists()).toBe(true)
  })

  it('shows nothing rather than a not-found state while still loading', () => {
    store.currentGroup = null
    store.isLoading = true

    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="threshold-group-not-found"]').exists()).toBe(false)
  })

  it('puts the group name last in the breadcrumbs', () => {
    useMenuStore().mainMenu = { homeUrl: '/home' } as any

    const wrapper = mountComponent()

    const items = wrapper.findComponent(BreadCrumbs).props('items') as any[]
    expect(items).toHaveLength(3)
    expect(items[1]).toMatchObject({ label: 'Threshold Configuration', to: '/threshold-config' })
    expect(items[2]).toMatchObject({ label: 'mib2', position: 'last' })
  })
})
