import RoleEditorDialog from '@/components/ManageOnCallRoles/RoleEditorDialog.vue'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/onCallRoleAdminStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

describe('RoleEditorDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (role: any = null) => {
    wrapper = mount(RoleEditorDialog, {
      props: { visible: false, role },
      global: {
        plugins: [PrimeVue],
        stubs: { Dialog: DialogStub }
      }
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = {
      supervisorCandidates: ['admin', 'jose'],
      groupMembers: { NOC: ['jose'] },
      createRole: vi.fn().mockResolvedValue(null),
      updateRole: vi.fn().mockResolvedValue(null)
    }
    vi.mocked(useOnCallRoleAdminStore).mockReturnValue(store)
  })

  it('flags a role name with whitespace and disables saving', async () => {
    await mountDialog()
    await wrapper.find('[data-test="role-name-input"]').setValue('NOC Duty')

    expect(wrapper.find('[data-test="name-error"]').text()).toContain('must not contain')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('requires a membership group and supervisor before saving', async () => {
    await mountDialog()
    await wrapper.find('[data-test="role-name-input"]').setValue('NOC-Duty')

    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('shows a server rejection inside the dialog and stays open', async () => {
    store.updateRole.mockResolvedValue('A supervisor is required and must be an existing user.')
    await mountDialog({ name: 'NOC-Duty', 'membership-group': 'NOC', supervisor: 'admin' })
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('supervisor is required')
    expect(wrapper.emitted('update:visible') ?? []).toEqual([])
  })

  it('saves an edit and closes', async () => {
    await mountDialog({ name: 'NOC-Duty', 'membership-group': 'NOC', supervisor: 'admin', description: 'x' })
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.updateRole).toHaveBeenCalledWith(expect.objectContaining({ name: 'NOC-Duty' }))
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })
})
