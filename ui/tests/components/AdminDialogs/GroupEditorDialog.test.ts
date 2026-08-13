import GroupEditorDialog from '@/components/ManageGroups/GroupEditorDialog.vue'
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

describe('GroupEditorDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (group: any = null) => {
    wrapper = mount(GroupEditorDialog, {
      props: { visible: false, group },
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
      memberCandidates: ['admin', 'jose'],
      createGroup: vi.fn().mockResolvedValue(null),
      updateGroup: vi.fn().mockResolvedValue(null)
    }
    vi.mocked(useGroupAdminStore).mockReturnValue(store)
  })

  it('flags a group name with whitespace and disables saving', async () => {
    await mountDialog()
    await wrapper.find('[data-test="group-name-input"]').setValue('Test Group')

    expect(wrapper.find('#group-editor-name-error').text()).toContain('must not contain')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    expect(store.createGroup).not.toHaveBeenCalled()
  })

  it('flags markup in the comments and disables saving', async () => {
    await mountDialog()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="group-comments-input"]').setValue('<b>styled</b>')

    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('keeps a group with a pre-existing markup comment editable', async () => {
    await mountDialog({ name: 'Legacy', comments: 'Bob\'s R&D team', user: [] })

    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(false)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="group-comments-input"]').setValue('<b>changed</b>')
    expect(wrapper.find('#group-editor-comments-error').exists()).toBe(true)
  })

  it('submits a valid group and closes', async () => {
    await mountDialog()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="group-comments-input"]').setValue('Test Group')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.createGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'TestGroup', comments: 'Test Group' }))
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('shows a server rejection inside the dialog and stays open', async () => {
    store.createGroup.mockResolvedValue('Group TestGroup already exists.')
    await mountDialog()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('already exists')
    expect(wrapper.emitted('update:visible') ?? []).toEqual([])
  })

  it('clears a previous error when reopened', async () => {
    store.createGroup.mockResolvedValue('rejected')
    await mountDialog()
    await wrapper.find('[data-test="group-name-input"]').setValue('TestGroup')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').exists()).toBe(true)

    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true })
    expect(wrapper.find('[data-test="dialog-error"]').exists()).toBe(false)
  })
})
