import XmlCollectionConfiguration from '@/containers/XmlCollectionConfiguration.vue'
import { useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, RouterLinkStub, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  route: { name: 'XML Collection Configuration', params: {}, query: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() })
}))

describe('XmlCollectionConfiguration.vue', () => {
  let store: ReturnType<typeof useXmlCollectionConfigStore>
  let wrapper: VueWrapper<any>

  const mountComponent = async () => {
    wrapper = mount(XmlCollectionConfiguration, { global: { plugins: [PrimeVue], stubs: { RouterLink: RouterLinkStub }}})
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    route.query = {}
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useXmlCollectionConfigStore()
    store.fetchAll = vi.fn().mockImplementation(async () => {
      store.collections = [{ name: 'xml-es', step: 300, sourceCount: 1, groupCount: 0, importGroups: ['es'] }]
      store.groupFiles = [{ id: 'es', groupCount: 3, usedBy: ['xml-es'] }, { id: 'unused', groupCount: 1, usedBy: [] }]
      store.rrdRepository = '/rrd'
      return { success: true, message: '' }
    })
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('lists the collections', async () => {
    await mountComponent()

    expect(wrapper.find('h1').text()).toBe('XML Data Collection Configuration')
    expect(wrapper.find('[data-test="xml-collection-link"]').text()).toBe('xml-es')
    expect(wrapper.find('[data-test="xml-collection-imports"]').text()).toBe('es')
  })

  it('only offers to delete group files nobody imports', async () => {
    route.query = { tab: 'group-files' }
    await mountComponent()

    const deletes = wrapper.findAll('[data-test="xml-group-file-delete"]')
    expect(deletes[0].attributes('disabled')).toBeDefined()
    expect(deletes[1].attributes('disabled')).toBeUndefined()
  })

  it('reports a failed load', async () => {
    store.fetchAll = vi.fn().mockResolvedValue({ success: false, message: 'boom' })
    await mountComponent()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'boom', error: true })
  })
})
