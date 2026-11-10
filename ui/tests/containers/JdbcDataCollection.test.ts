import JdbcDataCollection from '@/containers/JdbcDataCollection.vue'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, replaceMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  replaceMock: vi.fn(),
  route: { name: 'JDBC Data Collection', params: {}, query: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock, replace: replaceMock })
}))

describe('JdbcDataCollection.vue', () => {
  let store: ReturnType<typeof useJdbcDataCollectionStore>
  let wrapper: VueWrapper<any>

  const mountComponent = async () => {
    wrapper = mount(JdbcDataCollection, {
      global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>' }}, directives: { onmsTooltip: {}}}
    })
    await flushPromises()
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    route.query = {}
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useJdbcDataCollectionStore()
    store.fetchSources = vi.fn().mockImplementation(async () => {
      store.sources = [
        { id: 'default', collectionCount: 1, collectionNames: ['default'] },
        { id: 'mysql', collectionCount: 1, collectionNames: ['MySQL-Global-Stats'] }
      ]
      return { success: true, message: '' }
    })
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('lists the sources; the default source cannot be deleted', async () => {
    await mountComponent()

    expect(wrapper.find('h1').text()).toBe('JDBC Data Collection')
    expect(wrapper.findAll('[data-test="jdbc-source-link"]').map(link => link.text())).toEqual(['default', 'mysql'])
    expect(wrapper.findAll('[data-test="jdbc-source-delete"]')).toHaveLength(1)
  })

  it('finds sources by collection name', async () => {
    await mountComponent()
    await wrapper.find('[data-test="jdbc-source-search"]').setValue('global')

    expect(wrapper.findAll('[data-test="jdbc-source-link"]').map(link => link.text())).toEqual(['mysql'])
  })

  it('reports a failed load', async () => {
    store.fetchSources = vi.fn().mockResolvedValue({ success: false, message: 'boom' })
    await mountComponent()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'boom', error: true })
  })

  it('opens the create page', async () => {
    await mountComponent()
    await wrapper.find('[data-test="jdbc-source-create"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/jdbc-data-collection/create-source')
  })
})
