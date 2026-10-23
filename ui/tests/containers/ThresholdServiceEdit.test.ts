import ThresholdServiceEdit from '@/containers/ThresholdServiceEdit.vue'
import { getDefaultThreshdPackage, useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import type { ThreshdPackage } from '@/types/thresholdConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route, leaveGuard } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { params: {} as Record<string, string> },
  leaveGuard: { value: undefined as undefined | ((to: unknown) => void) }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock }),
  onBeforeRouteLeave: (guard: (to: unknown) => void) => {
    leaveGuard.value = guard
  }
}))

describe('ThresholdServiceEdit.vue', () => {
  let store: ReturnType<typeof useThreshdConfigurationStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const pkg = (): ThreshdPackage => ({
    ...getDefaultThreshdPackage(),
    name: 'example1',
    filter: 'IPADDR != \'0.0.0.0\'',
    services: [{ name: 'SNMP', interval: 300000, userDefined: false, status: 'on', parameters: [{ key: 'thresholding-group', value: 'mib2' }] }]
  })

  const mountComponent = async () => {
    wrapper = mount(ThresholdServiceEdit, { global: { plugins: [PrimeVue], stubs: { ParameterListEditor: true }}})
    await flushPromises()
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    leaveGuard.value = undefined
    route.params = { name: 'example1', index: '0' }
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useThreshdConfigurationStore()
    store.fetchPackage = vi.fn().mockImplementation(async (name: string) => {
      store.currentPackage = pkg()
      store.loadedPackageName = name
      return ok
    })
    store.saveService = vi.fn().mockResolvedValue(ok)
    const groupStore = useThresholdGroupStore()
    groupStore.groups = [{ name: 'mib2', rrdRepository: '/rrd', thresholdCount: 0, expressionCount: 0 }]
    groupStore.fetchGroups = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('loads the package on a direct visit and edits the service at the index', async () => {
    await mountComponent()

    expect(store.fetchPackage).toHaveBeenCalledWith('example1')
    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Edit Service Details')
    expect((wrapper.find('[data-test="package-service-name"]').element as HTMLInputElement).value).toBe('SNMP')
  })

  it('keeps a package the package page already holds, unsaved edits included', async () => {
    store.currentPackage = { ...pkg(), filter: 'edited but not saved' }
    store.loadedPackageName = 'example1'

    await mountComponent()

    expect(store.fetchPackage).not.toHaveBeenCalled()
    expect(store.currentPackage?.filter).toBe('edited but not saved')
  })

  it('creates a service and returns to the package', async () => {
    route.params = { name: 'example1' }

    await mountComponent()
    expect(wrapper.find('[data-test="threshold-edit-save"]').text()).toBe('Create Service')

    await wrapper.find('[data-test="package-service-name"]').setValue('ICMP')
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
    await flushPromises()

    expect(store.saveService).toHaveBeenCalledWith(null, expect.objectContaining({ name: 'ICMP' }))
    expect(pushMock).toHaveBeenCalledWith('/threshold-config/package/example1')
  })

  it('follows a rename the save wrote along', async () => {
    store.saveService = vi.fn().mockImplementation(async () => {
      store.loadedPackageName = 'renamed'
      return ok
    })

    await mountComponent()
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
    await flushPromises()

    expect(pushMock).toHaveBeenCalledWith('/threshold-config/package/renamed')
  })

  it('stays on the page when the save fails', async () => {
    store.saveService = vi.fn().mockResolvedValue({ success: false, message: 'conflict' })

    await mountComponent()
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'conflict', error: true })
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('shows a not-found state for an index the package does not have', async () => {
    route.params = { name: 'example1', index: '4' }

    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').exists()).toBe(true)
  })

  it('drops unsaved package edits unless it goes back to the package', async () => {
    await mountComponent()

    leaveGuard.value?.({ name: 'Threshold Package Detail', params: { name: 'example1' }})
    expect(store.clearCurrentPackage).not.toHaveBeenCalled()

    leaveGuard.value?.({ name: 'Threshold Configuration', params: {}})
    expect(store.clearCurrentPackage).toHaveBeenCalled()
  })
})
