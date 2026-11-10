import HttpCollectionEdit from '@/containers/HttpCollectionEdit.vue'
import { useHttpCollectionStore } from '@/stores/httpCollectionStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'HTTP Collection Create', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('HttpCollectionEdit.vue', () => {
  let store: ReturnType<typeof useHttpCollectionStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(HttpCollectionEdit, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const save = () => wrapper.find('[data-test="threshold-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'HTTP Collection Create'
    route.params = {}
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useHttpCollectionStore()
    store.collections = [{ name: 'doc-count' }]
    store.fetchCollections = vi.fn().mockResolvedValue(ok)
    store.fetchMetadata = vi.fn().mockResolvedValue(ok)
    store.fetchCollection = vi.fn().mockImplementation(async () => {
      store.currentCollection = {
        name: 'doc-count',
        rrd: { step: 300, rras: ['RRA:AVERAGE:0.5:1:10'] },
        uris: [{ name: 'first', url: { path: '/' }}],
        version: 'v1'
      }
      return ok
    })
    store.createCollection = vi.fn().mockResolvedValue(ok)
    store.saveCollection = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('creates a collection with the default rrd and opens it', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Create New HTTP Collection')
    expect(save().attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="http-collection-edit-name"]').setValue('new one')
    await save().trigger('click')
    await flushPromises()

    expect(store.createCollection).toHaveBeenCalledWith(expect.objectContaining({
      name: 'new one',
      rrd: expect.objectContaining({ step: 300, rras: expect.arrayContaining(['RRA:AVERAGE:0.5:1:2016']) })
    }))
    expect(pushMock).toHaveBeenCalledWith('/http-collection-config/collection/new%20one')
  })

  it('refuses a name that differs from an existing one only in case', async () => {
    await mountComponent()
    await wrapper.find('[data-test="http-collection-edit-name"]').setValue('DOC-COUNT')

    expect(wrapper.text()).toContain('A collection with this name already exists.')
    expect(save().attributes('disabled')).toBeDefined()
  })

  it('renames a collection and keeps its uris and version', async () => {
    route.name = 'HTTP Collection Edit'
    route.params = { name: 'doc-count' }
    await mountComponent()

    await wrapper.find('[data-test="http-collection-edit-name"]').setValue('renamed')
    await save().trigger('click')
    await flushPromises()

    expect(store.saveCollection).toHaveBeenCalledWith('doc-count', expect.objectContaining({
      name: 'renamed',
      version: 'v1',
      uris: [expect.objectContaining({ name: 'first' })]
    }))
    expect(pushMock).toHaveBeenCalledWith('/http-collection-config/collection/renamed')
  })

  it('stays on the page when the save fails', async () => {
    route.name = 'HTTP Collection Edit'
    route.params = { name: 'doc-count' }
    store.saveCollection = vi.fn().mockResolvedValue({ success: false, message: 'in use' })
    await mountComponent()

    await save().trigger('click')
    await flushPromises()

    expect(pushMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith(expect.objectContaining({ msg: 'in use', error: true }))
  })

  it('says so when the collection does not exist', async () => {
    route.name = 'HTTP Collection Edit'
    route.params = { name: 'nope' }
    store.fetchCollection = vi.fn().mockImplementation(async () => {
      store.currentCollection = null
      return { success: false, message: 'gone' }
    })
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').text()).toContain('No HTTP collection named \'nope\' exists.')
  })
})
