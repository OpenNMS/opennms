import ResourceTypesConfigTable from '@/components/ResourceTypesConfiguration/ResourceTypesConfigTable.vue'
import { useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock } = vi.hoisted(() => ({ showSnackBarMock: vi.fn(), pushMock: vi.fn() }))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

describe('ResourceTypesConfigTable.vue', () => {
  let store: ReturnType<typeof useResourceTypesConfigStore>
  let wrapper: VueWrapper<any>

  const mountComponent = () => {
    wrapper = mount(ResourceTypesConfigTable, {
      global: { plugins: [PrimeVue], stubs: { 'router-link': { template: '<a><slot /></a>' }}}
    })
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useResourceTypesConfigStore()
    store.configs = [
      { id: 'jmx-resource', resourceTypeNames: ['kafkaLag'] },
      { id: 'postgresql-resource', resourceTypeNames: ['pgTableSpace', 'pgDatabase'] }
    ]
    store.deleteConfig = vi.fn().mockResolvedValue({ success: true, message: '' })
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('lists the configurations with their resource types', () => {
    mountComponent()

    const links = wrapper.findAll('[data-test="resource-types-config-link"]').map(link => link.text())
    expect(links).toEqual(['jmx-resource', 'postgresql-resource'])
    expect(wrapper.text()).toContain('pgTableSpace, pgDatabase')
  })

  it('finds a configuration by the name of one of its types', async () => {
    mountComponent()

    await wrapper.find('input[data-test="resource-types-config-search"]').setValue('pgdata')

    const links = wrapper.findAll('[data-test="resource-types-config-link"]').map(link => link.text())
    expect(links).toEqual(['postgresql-resource'])
  })

  it('opens the create page', async () => {
    mountComponent()

    await wrapper.find('[data-test="resource-types-config-create"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/resource-types/create-config')
  })

  it('deletes only after confirming, warning about the users of its types', async () => {
    mountComponent()

    await wrapper.findAll('[data-test="resource-types-config-delete"]')[1].trigger('click')
    expect(store.deleteConfig).not.toHaveBeenCalled()

    const dialog = document.body.textContent ?? ''
    expect(dialog).toContain('pgTableSpace, pgDatabase')
    expect(dialog).toContain('stop working')

    wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
    await flushPromises()

    expect(store.deleteConfig).toHaveBeenCalledWith('postgresql-resource')
  })
})
