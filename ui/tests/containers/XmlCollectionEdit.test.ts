import XmlCollectionEdit from '@/containers/XmlCollectionEdit.vue'
import { DEFAULT_RRAS, useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'XML Collection Create', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('XmlCollectionEdit.vue', () => {
  let store: ReturnType<typeof useXmlCollectionConfigStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(XmlCollectionEdit, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const saveButton = () => wrapper.find('[data-test="threshold-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'XML Collection Create'
    route.params = {}
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useXmlCollectionConfigStore()
    store.fetchCollections = vi.fn().mockImplementation(async () => {
      store.collections = [{ name: 'existing', step: 300, sourceCount: 1, groupCount: 0, importGroups: [] }]
      return ok
    })
    store.fetchCollection = vi.fn().mockImplementation(async () => {
      store.currentCollection = {
        name: 'existing',
        rrd: { step: 300, rras: ['RRA:AVERAGE:0.5:1:2016'] },
        sources: [{ url: 'http://a', request: null, importGroups: ['f'], groups: [] }],
        version: 'v1'
      }
      return ok
    })
    store.createCollection = vi.fn().mockResolvedValue(ok)
    store.updateCollection = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('creates a collection with the default archives', async () => {
    await mountComponent()
    await wrapper.find('[data-test="xml-collection-edit-name"]').setValue('existing')
    expect(saveButton().attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="xml-collection-edit-name"]').setValue(' new ')
    await saveButton().trigger('click')
    await flushPromises()

    expect(store.createCollection).toHaveBeenCalledWith({ name: 'new', rrd: { step: 300, rras: DEFAULT_RRAS }, sources: [] })
    expect(pushMock).toHaveBeenCalledWith('/xml-collection-config/collection/new')
  })

  it('refuses an invalid RRA', async () => {
    await mountComponent()
    await wrapper.find('[data-test="xml-collection-edit-name"]').setValue('new')
    await wrapper.find('[data-test="xml-collection-rra-value-0"]').setValue('RRA:SUM:0.5:1:1')

    expect(saveButton().attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('\'RRA:SUM:0.5:1:1\' is invalid')
  })

  it('renames a collection, keeping its sources and version, and warns about collectd', async () => {
    route.name = 'XML Collection Edit'
    route.params = { name: 'existing' }
    await mountComponent()

    expect(wrapper.find('[data-test="xml-collection-rename-warning"]').exists()).toBe(false)
    await wrapper.find('[data-test="xml-collection-edit-name"]').setValue('renamed')
    expect(wrapper.find('[data-test="xml-collection-rename-warning"]').exists()).toBe(true)

    await saveButton().trigger('click')
    await flushPromises()

    expect(store.updateCollection).toHaveBeenCalledWith('existing', expect.objectContaining({
      name: 'renamed',
      version: 'v1',
      sources: [expect.objectContaining({ url: 'http://a' })]
    }))
  })
})
