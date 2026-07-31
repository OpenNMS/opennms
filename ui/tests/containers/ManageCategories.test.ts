import ManageCategories from '@/containers/ManageCategories.vue'
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

describe('ManageCategories.vue (container)', () => {
  it('loads categories on mount and renders the page title', async () => {
    const wrapper = mount(ManageCategories, {
      global: {
        plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
        stubs: { CategoriesTable: true, CategoriesHelpPanel: true, BreadCrumbs: true }
      }
    })
    const store = useCategoryAdminStore()
    await flushPromises()
    expect(store.getCategories).toHaveBeenCalled()
    expect(wrapper.find('.page-title').text()).toBe('Surveillance Categories')
  })
})
