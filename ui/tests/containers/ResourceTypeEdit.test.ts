import ResourceTypeEdit from '@/containers/ResourceTypeEdit.vue'
import { getDefaultResourceType, useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'Resource Type Create', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('ResourceTypeEdit.vue', () => {
  let store: ReturnType<typeof useResourceTypesConfigStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(ResourceTypeEdit, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const type = async (dataTest: string, value: string) => {
    await wrapper.find(`[data-test="${dataTest}"]`).setValue(value)
  }

  const save = () => wrapper.find('[data-test="threshold-edit-save"]')

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'Resource Type Create'
    route.params = { id: 'jmx-resource' }
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useResourceTypesConfigStore()
    store.fetchConfigs = vi.fn().mockImplementation(async () => {
      store.configs = [
        { id: 'jmx-resource', resourceTypeNames: ['kafkaLag'] },
        { id: 'postgresql-resource', resourceTypeNames: ['pgTableSpace'] }
      ]
      return ok
    })
    store.fetchConfig = vi.fn().mockImplementation(async () => {
      store.currentConfig = {
        id: 'jmx-resource',
        resourceTypes: [{ ...getDefaultResourceType(), name: 'kafkaLag', label: 'Kafka Lag', resourceLabel: '${index}' }],
        version: 'v1'
      }
      return ok
    })
    store.fetchMetadata = vi.fn().mockImplementation(async () => {
      store.metadata = {
        storageStrategies: ['org.opennms.netmgt.collection.support.IndexStorageStrategy'],
        persistenceSelectorStrategies: ['org.opennms.netmgt.collection.support.PersistAllSelectorStrategy'],
        dataCollectionTypeNames: ['hrStorageIndex'],
        extensionTypeNames: []
      }
      return ok
    })
    store.saveResourceType = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('creates a resource type with the default strategies', async () => {
    await mountComponent()
    await type('resource-type-edit-name', ' newType ')
    await type('resource-type-edit-label', 'New Type')
    await save().trigger('click')
    await flushPromises()

    expect(store.saveResourceType).toHaveBeenCalledWith(null, expect.objectContaining({
      name: 'newType',
      label: 'New Type',
      resourceLabel: undefined,
      storageStrategy: { className: 'org.opennms.netmgt.collection.support.IndexStorageStrategy', parameters: [] }
    }))
    expect(pushMock).toHaveBeenCalledWith('/resource-types/config/jmx-resource')
  })

  it('refuses a name another configuration defines', async () => {
    await mountComponent()
    await type('resource-type-edit-name', 'pgTableSpace')
    await type('resource-type-edit-label', 'Clash')

    expect(wrapper.text()).toContain('A resource type named \'pgTableSpace\' already exists.')
    expect(save().attributes('disabled')).toBeDefined()
  })

  it('warns when the name overrides a data collection type', async () => {
    await mountComponent()
    await type('resource-type-edit-name', 'hrStorageIndex')

    expect(wrapper.text()).toContain('This definition replaces it.')
  })

  it('edits a type by index, keeping its own name', async () => {
    route.name = 'Resource Type Edit'
    route.params = { id: 'jmx-resource', index: '0' }
    await mountComponent()

    expect((wrapper.find('[data-test="resource-type-edit-name"]').element as HTMLInputElement).value).toBe('kafkaLag')
    expect(save().attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="resource-type-storage-parameter-add"]').trigger('click')
    await type('resource-type-storage-parameter-key-0', 'sibling-column-name')
    await type('resource-type-storage-parameter-value-0', 'name')
    await save().trigger('click')
    await flushPromises()

    expect(store.saveResourceType).toHaveBeenCalledWith(0, expect.objectContaining({
      name: 'kafkaLag',
      resourceLabel: '${index}',
      storageStrategy: expect.objectContaining({ parameters: [{ key: 'sibling-column-name', value: 'name' }] })
    }))
  })

  it('says so when there is no type at the index', async () => {
    route.name = 'Resource Type Edit'
    route.params = { id: 'jmx-resource', index: '5' }
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').text()).toContain('No such resource type in \'jmx-resource\'.')
  })
})
