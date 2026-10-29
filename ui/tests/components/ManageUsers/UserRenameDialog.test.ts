import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'

import UserRenameDialog from '@/components/ManageUsers/UserRenameDialog.vue'

vi.mock('@/stores/userAdminStore', () => ({ useUserAdminStore: () => ({}) }))

// the dialog is teleported to the body
enableAutoUnmount(afterEach)

describe('UserRenameDialog reserved names', () => {
  const mountDialog = () => mount(UserRenameDialog, { props: { visible: true, userId: 'jdoe', version: 1 }, global: { plugins: [PrimeVue] }})
  const find = (selector: string) => document.body.querySelector(selector) as HTMLElement

  it('shows the message and disables Rename for a look-alike', async () => {
    const wrapper = mountDialog()
    ;(wrapper.vm as unknown as { newUserId: string }).newUserId = 'Admin'
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).toContain('The user-id "Admin" is reserved.')
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(true)
  })

  it('accepts another name', async () => {
    const wrapper = mountDialog()
    ;(wrapper.vm as unknown as { newUserId: string }).newUserId = 'other'
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).not.toContain('is reserved')
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(false)
  })
})
