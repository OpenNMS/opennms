import HttpUriEdit from '@/containers/HttpUriEdit.vue'
import { getDefaultUri, useHttpCollectionStore } from '@/stores/httpCollectionStore'
import type { HttpUri } from '@/types/httpCollectionConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'HTTP Collection URI Edit', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

const storedUri = (): HttpUri => {
  const uri = getDefaultUri()
  uri.name = 'document-counts'
  uri.url.path = '/test.html'
  uri.url.matches = 'Count: ([0-9]+)'
  uri.url.headers = [{ name: 'Accept', value: 'text/html' }]
  uri.attributes = [{ alias: 'documentCount', matchGroup: 1, type: 'counter' }]
  return uri
}

describe('HttpUriEdit.vue', () => {
  let store: ReturnType<typeof useHttpCollectionStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(HttpUriEdit, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const save = () => wrapper.find('[data-test="threshold-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'HTTP Collection URI Edit'
    route.params = { name: 'doc-count', index: '0' }
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useHttpCollectionStore()
    store.fetchMetadata = vi.fn().mockResolvedValue(ok)
    store.fetchCollection = vi.fn().mockImplementation(async () => {
      store.currentCollection = { name: 'doc-count', rrd: { step: 300, rras: ['RRA:AVERAGE:0.5:1:10'] }, uris: [storedUri()] }
      return ok
    })
    store.uriAt = vi.fn().mockImplementation((index: number | null) => (index === null ? getDefaultUri() : index === 0 ? storedUri() : null))
    store.saveUri = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('shows the stored uri', async () => {
    await mountComponent()

    expect((wrapper.find('[data-test="http-uri-name"]').element as HTMLInputElement).value).toBe('document-counts')
    expect((wrapper.find('[data-test="http-uri-path"]').element as HTMLInputElement).value).toBe('/test.html')
    expect((wrapper.find('[data-test="http-uri-header-key-0"]').element as HTMLInputElement).value).toBe('Accept')
    expect((wrapper.find('[data-test="http-attrib-alias-0"]').element as HTMLInputElement).value).toBe('documentCount')
  })

  it('saves the changed uri at its index and goes back to the collection', async () => {
    await mountComponent()
    await wrapper.find('[data-test="http-uri-path"]').setValue('/other.html ')
    await wrapper.find('[data-test="http-uri-query"]').setValue('')
    await save().trigger('click')
    await flushPromises()

    expect(store.saveUri).toHaveBeenCalledWith(0, expect.objectContaining({
      name: 'document-counts',
      url: expect.objectContaining({ path: '/other.html', query: null })
    }))
    expect(pushMock).toHaveBeenCalledWith('/http-collection-config/collection/doc-count')
  })

  it('blocks the save while an attribute refers to a missing group', async () => {
    await mountComponent()
    await wrapper.find('[data-test="http-uri-matches"]').setValue('no groups here')

    expect(wrapper.text()).toContain('does not exist; the expression has 0 groups')
    expect(save().attributes('disabled')).toBeDefined()
  })

  it('adds a header row', async () => {
    await mountComponent()
    await wrapper.find('[data-test="http-uri-header-add"]').trigger('click')

    expect(wrapper.find('[data-test="http-uri-header-key-1"]').exists()).toBe(true)
    // An empty header blocks the save until it is filled in or removed.
    expect(save().attributes('disabled')).toBeDefined()
  })

  it('creates a uri from the defaults', async () => {
    route.name = 'HTTP Collection URI Create'
    route.params = { name: 'doc-count' }
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Create New URI')
    await wrapper.find('[data-test="http-uri-name"]').setValue('status')
    await save().trigger('click')
    await flushPromises()

    expect(store.saveUri).toHaveBeenCalledWith(null, expect.objectContaining({ name: 'status' }))
  })

  it('says so when there is no uri at the index', async () => {
    route.params = { name: 'doc-count', index: '7' }
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').text()).toContain('has no URI at position 7')
  })
})
