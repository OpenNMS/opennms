import ResourceTypesConfigDetail from '@/containers/ResourceTypesConfigDetail.vue'
import { getDefaultResourceType, useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'Resource Types Config Detail', params: { id: 'jmx-resource' } as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('ResourceTypesConfigDetail.vue', () => {
  let store: ReturnType<typeof useResourceTypesConfigStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(ResourceTypesConfigDetail, {
      global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>' }, BreadCrumbs: true }}
    })
    await flushPromises()
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    route.params = { id: 'jmx-resource' }
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useResourceTypesConfigStore()
    store.fetchConfigs = vi.fn().mockResolvedValue(ok)
    store.fetchConfig = vi.fn().mockImplementation(async () => {
      store.currentConfig = {
        id: 'jmx-resource',
        resourceTypes: [{ ...getDefaultResourceType(), name: 'kafkaLag', label: 'Kafka Lag' }],
        version: 'v1'
      }
      return ok
    })
    store.deleteConfig = vi.fn().mockResolvedValue(ok)
    store.uploadConfig = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('shows the resource types of the configuration', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="resource-types-config-name"]').text()).toBe('jmx-resource')
    expect(wrapper.find('[data-test="resource-type-link"]').text()).toBe('kafkaLag')
    expect(wrapper.find('[data-test="resource-type-storage"]').text()).toBe('IndexStorageStrategy')
  })

  it('deletes with the loaded version and goes back to the list', async () => {
    await mountComponent()
    await wrapper.find('[data-test="resource-types-config-delete"]').trigger('click')

    wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
    await flushPromises()

    expect(store.deleteConfig).toHaveBeenCalledWith('jmx-resource', 'v1')
    expect(pushMock).toHaveBeenCalledWith('/resource-types')
  })

  it('replaces the configuration from a file after confirming', async () => {
    await mountComponent()
    const input = wrapper.find('[data-test="resource-types-config-upload"]')
    const file = new File(['{}'], 'jmx.json')
    Object.defineProperty(input.element, 'files', { value: [file] })
    await input.trigger('change')

    expect(store.uploadConfig).not.toHaveBeenCalled()

    wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
    await flushPromises()

    expect(store.uploadConfig).toHaveBeenCalledWith('jmx-resource', file, false)
  })

  it('says so when the configuration does not exist', async () => {
    store.fetchConfig = vi.fn().mockImplementation(async () => {
      store.currentConfig = null
      return { success: false, message: 'not found' }
    })
    await mountComponent()

    expect(wrapper.find('[data-test="resource-types-config-not-found"]').exists()).toBe(true)
  })
})
