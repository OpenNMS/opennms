// ui/tests/components/Nodes/NodeCategoriesPanel.test.ts
import NodeCategoriesPanel from '@/components/Nodes/NodeCategoriesPanel.vue'
import { useAuthStore } from '@/stores/authStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'

// One pinia for the whole file, with roles set BEFORE mounting. useRole caches the auth store
// the first time it reads a role (module-level `computed(() => useAuthStore())`), so a fresh
// createTestingPinia per mount would leave later tests reading the first test's roles.
const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })

const mountPanel = (node: any = { id: '1', categories: [] }, roles: string[] = ['ROLE_ADMIN']) => {
  const authStore = useAuthStore(pinia)
  authStore.whoAmI = { id: 'admin', fullName: 'Administrator', internal: true, roles }

  const wrapper = mount(NodeCategoriesPanel, {
    props: { node, baseHref: '/opennms/' },
    global: {
      plugins: [pinia, PrimeVue]
    }
  })

  return { wrapper, authStore }
}

describe('NodeCategoriesPanel.vue', () => {
  it('navigates to the category edit page for the node', async () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)

    const { wrapper } = mountPanel()
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="edit-button"]').trigger('click')

    expect(assign).toHaveBeenCalledWith('/opennms/admin/categories.htm?edit&node=1')
    vi.unstubAllGlobals()
  })
  it('puts the title-row button in a right-aligned container', async () => {
    const { wrapper } = mountPanel()
    await wrapper.vm.$nextTick()

    const buttons = wrapper.find('.title-row .action-buttons-container')
    expect(buttons.exists()).toBe(true)
    expect(buttons.find('[data-test="edit-button"]').exists()).toBe(true)
  })

  // Without the edit button there is nothing to align, so the container goes too.
  it('renders no actions container for a user without the admin role', async () => {
    const { wrapper } = mountPanel({ id: '1', categories: [] }, ['ROLE_USER'])
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-test="edit-button"]').exists()).toBe(false)
    expect(wrapper.find('.action-buttons-container').exists()).toBe(false)
  })
})
