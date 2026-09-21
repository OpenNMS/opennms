import ManageMonitoringLocations from '@/containers/ManageMonitoringLocations.vue'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

// the store is created before mount so the load result can be set before onMounted runs
const mountPage = (loaded: boolean) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useMonitoringLocationAdminStore(pinia)
  vi.mocked(store.getLocations).mockResolvedValue(loaded)
  const wrapper = mount(ManageMonitoringLocations, {
    global: { plugins: [PrimeVue, pinia], stubs: { LocationsTable: true, BreadCrumbs: true }}
  })
  return { wrapper, store }
}

describe('ManageMonitoringLocations.vue (container)', () => {
  beforeEach(() => showToast.mockClear())

  it('loads locations on mount and renders the page title without an inline help panel', async () => {
    const { wrapper, store } = mountPage(true)
    await flushPromises()
    expect(store.getLocations).toHaveBeenCalled()
    expect(wrapper.find('.page-title').text()).toBe('Manage Monitoring Locations')
    expect(wrapper.findComponent({ name: 'LocationsHelpPanel' }).exists()).toBe(false)
    expect(showToast).not.toHaveBeenCalled()
  })

  it('toasts an error when the initial load fails', async () => {
    mountPage(false)
    await flushPromises()
    expect(showToast).toHaveBeenCalledWith({ message: 'Failed to load monitoring locations.', severity: 'error' })
  })
})
