import XmlSourceEdit from '@/containers/XmlSourceEdit.vue'
import { getDefaultCollection, getDefaultGroup, getDefaultObject, useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'XML Source Edit', params: { name: 'c', index: '0' } as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('XmlSourceEdit.vue', () => {
  let store: ReturnType<typeof useXmlCollectionConfigStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(XmlSourceEdit, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const saveButton = () => wrapper.find('[data-test="threshold-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'XML Source Edit'
    route.params = { name: 'c', index: '0' }
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useXmlCollectionConfigStore()
    store.fetchCollection = vi.fn().mockImplementation(async () => {
      store.currentCollection = {
        ...getDefaultCollection(),
        name: 'c',
        sources: [{
          url: 'http://{ipaddr}/stats',
          request: null,
          importGroups: ['shared'],
          groups: [{ ...getDefaultGroup(), name: 'g', objects: [{ ...getDefaultObject(), name: 'v', xpath: 'v' }] }]
        }],
        version: 'v1'
      }
      return ok
    })
    store.fetchMetadata = vi.fn().mockImplementation(async () => {
      store.metadata = { attributeTypes: ['gauge', 'counter', 'string'], resourceTypes: [{ name: 'node', label: 'Node' }], groupFiles: ['shared'] }
      return ok
    })
    store.saveSource = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('loads the source', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Edit Source of \'c\'')
    expect((wrapper.find('[data-test="xml-source-url"]').element as HTMLInputElement).value).toBe('http://{ipaddr}/stats')
    expect(wrapper.find('[data-test="xml-source-group-0"]').exists()).toBe(true)
    expect(saveButton().attributes('disabled')).toBeUndefined()
  })

  it('saves the edited source', async () => {
    await mountComponent()
    await wrapper.find('[data-test="xml-source-url"]').setValue('http://{ipaddr}/other')
    await saveButton().trigger('click')
    await flushPromises()

    expect(store.saveSource).toHaveBeenCalledWith(0, expect.objectContaining({ url: 'http://{ipaddr}/other', importGroups: ['shared'] }))
    expect(pushMock).toHaveBeenCalledWith('/xml-collection-config/collection/c')
  })

  it('keeps save disabled without a URL', async () => {
    await mountComponent()
    await wrapper.find('[data-test="xml-source-url"]').setValue(' ')

    expect(saveButton().attributes('disabled')).toBeDefined()
  })

  it('adds a group that must be completed before saving', async () => {
    await mountComponent()
    await wrapper.find('[data-test="xml-source-group-add"]').trigger('click')

    expect(wrapper.find('[data-test="xml-source-group-1"]').exists()).toBe(true)
    // The new group has no name yet.
    expect(saveButton().attributes('disabled')).toBeDefined()
  })

  it('shows the request fields only for a custom request', async () => {
    await mountComponent()
    expect(wrapper.find('[data-test="xml-source-content"]').exists()).toBe(false)

    await wrapper.find('[data-test="xml-source-custom-request"] input').setValue(true)

    expect(wrapper.find('[data-test="xml-source-content"]').exists()).toBe(true)
  })

  it('adds a new source', async () => {
    route.name = 'XML Source Create'
    route.params = { name: 'c' }
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Add Source to \'c\'')
    await wrapper.find('[data-test="xml-source-url"]').setValue('http://{ipaddr}/new')
    await wrapper.find('[data-test="xml-source-group-add"]').trigger('click')
    await wrapper.find('[data-test="xml-source-group-0-name"]').setValue('g')
    await wrapper.find('[data-test="xml-source-group-0-object-name-0"]').setValue('value')
    await wrapper.find('[data-test="xml-source-group-0-object-xpath-0"]').setValue('value')
    await saveButton().trigger('click')
    await flushPromises()

    expect(store.saveSource).toHaveBeenCalledWith(null, expect.objectContaining({ url: 'http://{ipaddr}/new' }))
  })

  it('reports a missing source', async () => {
    route.params = { name: 'c', index: '4' }
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').text()).toContain('Collection \'c\' has no source 5.')
  })
})
