import ThresholdPackageCreate from '@/containers/ThresholdPackageCreate.vue'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
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

describe('ThresholdPackageCreate.vue', () => {
  let store: ReturnType<typeof useThreshdConfigurationStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(ThresholdPackageCreate, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const fill = async (name: string) => {
    await wrapper.find('[data-test="threshd-package-create-name"]').setValue(name)
    await wrapper.find('[data-test="threshd-package-create-filter"]').setValue('IPADDR != \'0.0.0.0\'')
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useThreshdConfigurationStore()
    store.packages = [{ name: 'example1', filter: '', serviceCount: 0 } as any]
    store.fetchPackages = vi.fn().mockResolvedValue(ok)
    store.createPackage = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('reads as a create page', async () => {
    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Create New Threshd Package')
    expect(wrapper.find('[data-test="threshold-edit-save"]').text()).toBe('Create Threshd Package')
  })

  it('refuses a name that is already taken', async () => {
    await mountComponent()
    await fill('example1')

    expect(wrapper.find('[data-test="threshold-edit-save"]').attributes('disabled')).toBeDefined()
  })

  it('creates the package and opens it', async () => {
    await mountComponent()
    await fill('example2')
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
    await flushPromises()

    expect(store.createPackage).toHaveBeenCalledWith(expect.objectContaining({ name: 'example2' }))
    expect(pushMock).toHaveBeenCalledWith('/threshold-config/package/example2')
  })

  it('stays on the page when the save fails', async () => {
    store.createPackage = vi.fn().mockResolvedValue({ success: false, message: 'boom' })

    await mountComponent()
    await fill('example2')
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'boom', error: true })
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('goes back to the package list on cancel', async () => {
    await mountComponent()
    await wrapper.find('[data-test="threshold-edit-cancel"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/threshold-config?tab=packages')
  })
})
