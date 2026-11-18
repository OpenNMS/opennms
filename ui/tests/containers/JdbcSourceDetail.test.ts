import JdbcSourceDetail from '@/containers/JdbcSourceDetail.vue'
import { useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import type { JdbcSource } from '@/types/jdbcDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'JDBC Source Detail', params: { id: 'mysql' } as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

const source = (id: string): JdbcSource => ({
  id,
  rrdRepository: '/own',
  effectiveRrdRepository: '/effective',
  version: 'v1',
  collections: [
    { name: 'MySQL', rrdStep: 300, rras: [], queries: [], usedBy: ['example1/MySQL'], shadowedBy: null },
    { name: 'PostgreSQL', rrdStep: 300, rras: [], queries: [], usedBy: [], shadowedBy: 'postgresql' }
  ]
})

describe('JdbcSourceDetail.vue', () => {
  let store: ReturnType<typeof useJdbcDataCollectionStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(JdbcSourceDetail, {
      global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>' }}, directives: { onmsTooltip: {}}}
    })
    await flushPromises()
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    route.params = { id: 'mysql' }
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useJdbcDataCollectionStore()
    store.fetchSource = vi.fn().mockImplementation(async (id: string) => {
      store.currentSource = source(id)
      return ok
    })
    store.fetchSources = vi.fn().mockResolvedValue(ok)
    store.updateRepository = vi.fn().mockResolvedValue(ok)
    store.deleteSource = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('shows the collections, who uses them and which are shadowed', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="jdbc-source-id"]').text()).toBe('mysql')
    expect(wrapper.findAll('[data-test="jdbc-collection-link"]').map(link => link.text())).toEqual(['MySQL', 'PostgreSQL'])
    expect(wrapper.findAll('[data-test="jdbc-collection-used-by"]')[0].text()).toBe('example1/MySQL')
    expect(wrapper.findAll('[data-test="jdbc-collection-shadowed"]')).toHaveLength(1)
  })

  it('says which repository the collector really uses', async () => {
    await mountComponent()

    expect(wrapper.text()).toContain('The collector currently uses \'/effective\' for all collections.')
  })

  it('saves a changed repository', async () => {
    await mountComponent()
    const saveButton = wrapper.find('[data-test="jdbc-source-repository-save"]')
    expect(saveButton.attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="jdbc-source-repository"]').setValue(' /var/rrd ')
    await saveButton.trigger('click')
    await flushPromises()

    expect(store.updateRepository).toHaveBeenCalledWith('/var/rrd')
    expect(showSnackBarMock).toHaveBeenCalledWith(expect.objectContaining({ msg: 'RRD repository saved.' }))
  })

  it('deletes the source after confirmation', async () => {
    await mountComponent()
    await wrapper.find('[data-test="jdbc-source-delete"]').trigger('click')
    await wrapper.vm.$nextTick()
    // the confirmation dialog is teleported; confirm through the component
    await (wrapper.vm as any).$.setupState.onDelete()
    await flushPromises()

    expect(store.deleteSource).toHaveBeenCalledWith('mysql', 'v1')
    expect(pushMock).toHaveBeenCalledWith('/jdbc-data-collection?tab=sources')
  })

  it('does not offer to delete the default source', async () => {
    route.params = { id: 'default' }
    await mountComponent()

    expect(wrapper.find('[data-test="jdbc-source-delete"]').exists()).toBe(false)
  })

  it('shows an empty state for an unknown source', async () => {
    store.fetchSource = vi.fn().mockImplementation(async () => {
      store.currentSource = null
      return { success: false, message: 'not found', status: 404 }
    })
    await mountComponent()

    expect(wrapper.find('[data-test="jdbc-source-not-found"]').exists()).toBe(true)
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })
})
