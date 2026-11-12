import HttpCollectionDetail from '@/containers/HttpCollectionDetail.vue'
import { getDefaultUri, useHttpCollectionStore } from '@/stores/httpCollectionStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'HTTP Collection Detail', params: { name: 'doc-count' } as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('HttpCollectionDetail.vue', () => {
  let store: ReturnType<typeof useHttpCollectionStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(HttpCollectionDetail, {
      global: { plugins: [PrimeVue], stubs: { 'router-link': true, BreadCrumbs: true }}
    })
    await flushPromises()
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useHttpCollectionStore()
    store.fetchCollection = vi.fn().mockImplementation(async () => {
      const uri = getDefaultUri()
      uri.name = 'status'
      uri.url.path = '/status'
      uri.url.port = 8080
      uri.attributes = [{ alias: 'count', matchGroup: 1, type: 'gauge' }]
      store.currentCollection = { name: 'doc-count', rrd: { step: 300, rras: ['RRA:AVERAGE:0.5:1:10'] }, uris: [uri], version: 'v1' }
      return ok
    })
    store.deleteUri = vi.fn().mockResolvedValue(ok)
    store.deleteCollection = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('lists the uris with their request', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="http-collection-name"]').text()).toBe('doc-count')
    expect(wrapper.find('[data-test="http-uri-request"]').text()).toBe('GET http://${ipaddr}:8080/status')
    expect(wrapper.find('[data-test="http-uri-table"]').text()).toContain('count')
  })

  it('opens the uri editor at the row index', async () => {
    await mountComponent()
    await wrapper.find('[data-test="http-uri-edit"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/http-collection-config/collection/doc-count/uri/0/edit')
  })

  it('deletes a uri after confirmation', async () => {
    await mountComponent()
    await wrapper.find('[data-test="http-uri-delete"]').trigger('click')
    await flushPromises()

    const dialog = wrapper.findComponent({ name: 'OnmsConfirmationDialog' })
    dialog.vm.$emit('ok')
    await flushPromises()

    expect(store.deleteUri).toHaveBeenCalledWith(0)
  })

  it('deletes the collection with its version and goes back to the list', async () => {
    await mountComponent()
    await wrapper.find('[data-test="http-collection-delete"]').trigger('click')
    await flushPromises()

    wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
    await flushPromises()

    expect(store.deleteCollection).toHaveBeenCalledWith('doc-count', 'v1')
    expect(pushMock).toHaveBeenCalledWith('/http-collection-config?tab=collections')
  })

  it('says so when the collection does not exist', async () => {
    store.fetchCollection = vi.fn().mockImplementation(async () => {
      store.currentCollection = null
      return { success: false, message: 'gone' }
    })
    await mountComponent()

    expect(wrapper.find('[data-test="http-collection-not-found"]').exists()).toBe(true)
  })
})
