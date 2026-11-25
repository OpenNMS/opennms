import ResourceTypesConfigEdit from '@/containers/ResourceTypesConfigEdit.vue'
import { useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'Resource Types Config Create', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('ResourceTypesConfigEdit.vue', () => {
  let store: ReturnType<typeof useResourceTypesConfigStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(ResourceTypesConfigEdit, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const save = () => wrapper.find('[data-test="threshold-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'Resource Types Config Create'
    route.params = {}
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useResourceTypesConfigStore()
    store.configs = [{ id: 'jmx-resource', resourceTypeNames: [] }]
    store.fetchConfigs = vi.fn().mockResolvedValue(ok)
    store.fetchConfig = vi.fn().mockImplementation(async () => {
      store.currentConfig = { id: 'jmx-resource', resourceTypes: [], version: 'v1' }
      return ok
    })
    store.createConfig = vi.fn().mockResolvedValue(ok)
    store.uploadConfig = vi.fn().mockResolvedValue(ok)
    store.renameConfig = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('refuses a taken id', async () => {
    await mountComponent()
    await wrapper.find('[data-test="resource-types-config-edit-id"]').setValue('jmx-resource')

    expect(wrapper.text()).toContain('already exists')
    expect(save().attributes('disabled')).toBeDefined()
  })

  it('creates an empty configuration and opens it', async () => {
    await mountComponent()
    await wrapper.find('[data-test="resource-types-config-edit-id"]').setValue('my-types')
    await save().trigger('click')
    await flushPromises()

    expect(store.createConfig).toHaveBeenCalledWith({ id: 'my-types', resourceTypes: [] })
    expect(pushMock).toHaveBeenCalledWith('/resource-types/config/my-types')
  })

  it('creates the configuration from a file', async () => {
    await mountComponent()
    await wrapper.find('[data-test="resource-types-config-edit-id"]').setValue('my-types')
    const input = wrapper.find('[data-test="resource-types-config-edit-file"]')
    const file = new File(['<resource-types/>'], 'old.xml')
    Object.defineProperty(input.element, 'files', { value: [file] })
    await input.trigger('change')
    await save().trigger('click')
    await flushPromises()

    expect(store.uploadConfig).toHaveBeenCalledWith('my-types', file, true)
    expect(store.createConfig).not.toHaveBeenCalled()
  })

  it('renames, allowing the configuration to keep its own id', async () => {
    route.name = 'Resource Types Config Edit'
    route.params = { id: 'jmx-resource' }
    await mountComponent()

    expect(save().attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="resource-types-config-edit-id"]').setValue('kafka-resource')
    await save().trigger('click')
    await flushPromises()

    expect(store.renameConfig).toHaveBeenCalledWith('jmx-resource', 'kafka-resource')
    expect(pushMock).toHaveBeenCalledWith('/resource-types/config/kafka-resource')
  })
})
