import CategoriesTable from '@/components/ManageCategories/CategoriesTable.vue'
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mountTable = () => {
  const wrapper = mount(CategoriesTable, {
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: {
        CategoryEditorDialog: true, CategoryNodesDialog: true, OnmsConfirmationDialog: true,
        TableCard: { template: '<div><slot /></div>' }
      }
    }
  })
  return { wrapper, store: useCategoryAdminStore() }
}

describe('CategoriesTable.vue', () => {
  let ctx: ReturnType<typeof mountTable>

  beforeEach(() => {
    ctx = mountTable()
  })

  it('renders column headers even when there are no categories', async () => {
    ctx.store.categories = []
    await ctx.wrapper.vm.$nextTick()
    const headers = ctx.wrapper.findAll('th').map((th) => th.text().trim()).filter(Boolean)
    expect(headers).toContain('Name')
    expect(headers).toContain('Description')
    expect(ctx.wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
  })

  it('renders a row per category with the action buttons', async () => {
    ctx.store.categories = [{ name: 'Routers', description: 'core' }] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="manage-nodes-button"]').exists()).toBe(true)
    expect(ctx.wrapper.find('[data-test="edit-category-button"]').exists()).toBe(true)
    expect(ctx.wrapper.find('[data-test="delete-category-button"]').exists()).toBe(true)
  })
})
