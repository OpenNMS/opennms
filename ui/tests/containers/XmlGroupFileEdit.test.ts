import XmlGroupFileEdit from '@/containers/XmlGroupFileEdit.vue'
import { getDefaultGroup, getDefaultObject, useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, RouterLinkStub, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'XML Group File', params: { id: 'shared' } as Record<string, string>, fullPath: '/x' }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('XmlGroupFileEdit.vue', () => {
  let store: ReturnType<typeof useXmlCollectionConfigStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }
  const group = () => ({ ...getDefaultGroup(), name: 'g', objects: [{ ...getDefaultObject(), name: 'v', xpath: 'v' }] })

  const mountComponent = async () => {
    wrapper = mount(XmlGroupFileEdit, { global: { plugins: [PrimeVue], stubs: { RouterLink: RouterLinkStub }}})
    await flushPromises()
    return wrapper
  }

  const saveButton = () => wrapper.find('[data-test="threshold-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'XML Group File'
    route.params = { id: 'shared' }
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useXmlCollectionConfigStore()
    store.fetchGroupFiles = vi.fn().mockImplementation(async () => {
      store.groupFiles = [{ id: 'shared', groupCount: 1, usedBy: ['c'] }]
      return ok
    })
    store.fetchMetadata = vi.fn().mockResolvedValue(ok)
    store.fetchGroupFile = vi.fn().mockImplementation(async () => {
      store.currentGroupFile = { id: 'shared', groups: [group()], version: 'v1' }
      return ok
    })
    store.createGroupFile = vi.fn().mockResolvedValue(ok)
    store.updateGroupFile = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('shows who imports the group file and saves it with its version', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="xml-group-file-used-by"]').text()).toBe('Imported by c.')
    expect(wrapper.find('[data-test="xml-group-file-id"]').exists()).toBe(false)

    await saveButton().trigger('click')
    await flushPromises()

    expect(store.updateGroupFile).toHaveBeenCalledWith(expect.objectContaining({ id: 'shared', version: 'v1' }))
  })

  it('keeps the edit after a refused save', async () => {
    store.updateGroupFile = vi.fn().mockResolvedValue({ success: false, message: 'invalid', status: 400 })
    await mountComponent()
    await wrapper.find('[data-test="xml-group-file-group-remove-0"]').trigger('click')
    await saveButton().trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'invalid', error: true })
    expect(wrapper.find('[data-test="xml-group-file-group-0"]').exists()).toBe(false)
  })

  it('creates a group file and opens it', async () => {
    route.name = 'XML Group File Create'
    route.params = {}
    await mountComponent()

    await wrapper.find('[data-test="xml-group-file-id"]').setValue('shared')
    expect(saveButton().attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="xml-group-file-id"]').setValue('new-file')
    expect(saveButton().attributes('disabled')).toBeUndefined()

    await saveButton().trigger('click')
    await flushPromises()

    expect(store.createGroupFile).toHaveBeenCalledWith({ id: 'new-file', groups: [] })
    expect(pushMock).toHaveBeenCalledWith('/xml-collection-config/group-file/new-file')
  })
})
