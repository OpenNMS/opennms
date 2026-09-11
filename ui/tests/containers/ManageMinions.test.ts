import ManageMinions from '@/containers/ManageMinions.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

describe('ManageMinions.vue (container)', () => {
  it('loads minions on mount and renders the page title', async () => {
    const wrapper = mount(ManageMinions, {
      global: {
        plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
        stubs: { MinionsTable: true, MinionsHelpPanel: true, BreadCrumbs: true }
      }
    })
    const store = useMinionAdminStore()
    await flushPromises()
    expect(store.getMinions).toHaveBeenCalled()
    expect(wrapper.find('.page-title').text()).toBe('Manage Minions')
  })
})
