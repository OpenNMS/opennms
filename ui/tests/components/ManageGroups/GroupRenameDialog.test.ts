import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'

import GroupRenameDialog from '@/components/ManageGroups/GroupRenameDialog.vue'

vi.mock('@/stores/groupAdminStore', () => ({ useGroupAdminStore: () => ({}) }))

// the dialog is teleported to the body
enableAutoUnmount(afterEach)

describe('GroupRenameDialog reserved names', () => {
  const mountDialog = () => mount(GroupRenameDialog, { props: { visible: true, groupName: 'Ops', version: 1 }, global: { plugins: [PrimeVue] }})
  const find = (selector: string) => document.body.querySelector(selector) as HTMLElement

  it('shows the message and disables Rename for a look-alike', async () => {
    const wrapper = mountDialog()
    ;(wrapper.vm as unknown as { newName: string }).newName = 'admin'
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).toContain('The group name "admin" is reserved.')
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(true)
  })

  it('hints that destination paths do not follow the rename', async () => {
    const wrapper = mountDialog()
    await wrapper.vm.$nextTick()
    expect(find('.field-hint').textContent).toBe('On-call roles referencing this group follow the rename; destination paths that target the group by name do not.')
  })

  it('accepts another name', async () => {
    const wrapper = mountDialog()
    ;(wrapper.vm as unknown as { newName: string }).newName = 'other'
    await wrapper.vm.$nextTick()

    expect(document.body.textContent).not.toContain('is reserved')
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(false)
  })
})
