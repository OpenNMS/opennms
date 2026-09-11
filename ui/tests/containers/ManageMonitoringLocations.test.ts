import ManageMonitoringLocations from '@/containers/ManageMonitoringLocations.vue'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

describe('ManageMonitoringLocations.vue (container)', () => {
  it('loads locations on mount and renders the page title', async () => {
    const wrapper = mount(ManageMonitoringLocations, {
      global: {
        plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
        stubs: { LocationsTable: true, LocationsHelpPanel: true, BreadCrumbs: true }
      }
    })
    const store = useMonitoringLocationAdminStore()
    await flushPromises()
    expect(store.getLocations).toHaveBeenCalled()
    expect(wrapper.find('.page-title').text()).toBe('Manage Monitoring Locations')
  })
})
