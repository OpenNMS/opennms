import GroupRenameDialog from '@/components/ManageGroups/GroupRenameDialog.vue'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/groupAdminStore')

// PrimeVue's Dialog teleports its content; a passthrough stub keeps the form
// and footer in the wrapper so the dialog's own behavior can be asserted.
const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

describe('GroupRenameDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (groupName = 'Ops') => {
    wrapper = mount(GroupRenameDialog, {
      props: { visible: false, groupName },
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
    store = { renameGroup: vi.fn().mockResolvedValue(null) }
    vi.mocked(useGroupAdminStore).mockReturnValue(store)
  })

  it('pre-populates the current name and keeps Rename disabled until it changes', async () => {
    await mountDialog('Ops')

    expect((wrapper.find('[data-test="new-name-input"]').element as HTMLInputElement).value).toBe('Ops')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="new-name-input"]').setValue('Ops2')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
  })

  it('renames and closes', async () => {
    await mountDialog('Ops')
    await wrapper.find('[data-test="new-name-input"]').setValue('Operations')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.renameGroup).toHaveBeenCalledWith('Ops', 'Operations')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('shows a server rejection and stays open', async () => {
    store.renameGroup.mockResolvedValue('Group Operations already exists.')
    await mountDialog('Ops')
    await wrapper.find('[data-test="new-name-input"]').setValue('Operations')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('already exists')
    expect(wrapper.emitted('update:visible') ?? []).toEqual([])
  })
})
