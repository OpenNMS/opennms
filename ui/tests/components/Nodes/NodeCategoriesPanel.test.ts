// ui/tests/components/Nodes/NodeCategoriesPanel.test.ts
import NodeCategoriesPanel from '@/components/Nodes/NodeCategoriesPanel.vue'
import { useAuthStore } from '@/stores/authStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'

const mountPanel = (node: any = { id: '1', categories: [] }) => {
  const wrapper = mount(NodeCategoriesPanel, {
    props: { node, baseHref: '/opennms/' },
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue]
    }
  })

  const authStore = useAuthStore()
  authStore.whoAmI = { id: 'admin', fullName: 'Administrator', internal: true, roles: ['ROLE_ADMIN'] }

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
})
