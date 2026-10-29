import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import GroupsTable from '@/components/ManageGroups/GroupsTable.vue'
import GroupEditorPanel from '@/components/ManageGroups/GroupEditorPanel.vue'
import GroupRenameDialog from '@/components/ManageGroups/GroupRenameDialog.vue'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { STALE_GROUP_MESSAGE } from '@/types/groupAdmin'

const store = vi.hoisted(() => ({
  updateGroup: vi.fn(),
  openGroupEditor: vi.fn(),
  createGroup: vi.fn(),
  renameGroup: vi.fn(),
  deleteGroup: vi.fn(),
  reloadError: '',
  groups: [] as unknown[],
  memberCandidates: [] as string[]
}))

// reactive, so that a table re-renders its rows when a test replaces store.groups through useGroupAdminStore()
vi.mock('@/stores/groupAdminStore', async () => {
  const { reactive } = await import('vue')
  const live = reactive(store)
  return { useGroupAdminStore: () => live }
})

// the components give the feedback (the store and service give none)
const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
const spinner = vi.hoisted(() => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }))
vi.mock('@/composables/useSpinner', () => ({ default: () => spinner }))

// the dialogs are teleported to the body; unmount each one, or a later test can find an earlier test's dialog
enableAutoUnmount(afterEach)

const group = { name: 'Ops', users: [], version: 3 }

type DialogVm = { save: () => Promise<void>, newName: string }

// each case: a mounted dialog, the store call its Save makes, the data-test of the dialog,
// how to fill it in and its success message
const cases = [
  ['GroupRenameDialog', () => mount(GroupRenameDialog, { props: { visible: true, groupName: 'Ops', version: 3 }, global: { plugins: [PrimeVue] }}), store.renameGroup, 'group-rename-dialog',
    (vm: DialogVm) => {
      vm.newName = 'Dev'
    }, 'Group \'Ops\' renamed to \'Dev\'.']
] as const

describe.each(cases)('%s', (_name, mountDialog, storeCall, dialogTest, fill, successMsg) => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.reloadError = ''
  })

  it('closes when the save is rejected as stale, and shows the stale text once', async () => {
    storeCall.mockResolvedValue({ message: STALE_GROUP_MESSAGE, stale: true })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as DialogVm).save()

    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(showSnackBar).toHaveBeenCalledTimes(1)
    expect(showSnackBar).toHaveBeenCalledWith({ msg: STALE_GROUP_MESSAGE, error: true })
  })

  it('stays open and shows other errors in the dialog only', async () => {
    storeCall.mockResolvedValue({ message: 'Something else went wrong', stale: false })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as DialogVm).save()
    await nextTick()

    expect(wrapper.emitted('update:visible')).toBeUndefined()
    // only the error of this dialog: the dialogs are teleported to the body
    const errors = document.body.querySelectorAll(`[data-test="${dialogTest}"] [data-test="dialog-error"]`)
    expect(errors).toHaveLength(1)
    expect(errors[0].textContent).toBe('Something else went wrong')
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('does not close on the stale wording without the flag', async () => {
    storeCall.mockResolvedValue({ message: STALE_GROUP_MESSAGE, stale: false })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as DialogVm).save()

    expect(wrapper.emitted('update:visible')).toBeUndefined()
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('closes on success and says so, with the spinner around the save', async () => {
    storeCall.mockResolvedValue(null)
    const wrapper = mountDialog()
    fill(wrapper.vm as unknown as DialogVm)

    await (wrapper.vm as unknown as DialogVm).save()

    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(showSnackBar.mock.calls).toEqual([[{ msg: successMsg }]])
    expect(spinner.startSpinner).toHaveBeenCalledTimes(1)
    expect(spinner.stopSpinner).toHaveBeenCalledTimes(1)
  })

  it('on success whose reload failed, says both', async () => {
    storeCall.mockImplementation(async () => {
      store.reloadError = 'Failed to load groups.'
      return null
    })
    const wrapper = mountDialog()
    fill(wrapper.vm as unknown as DialogVm)

    await (wrapper.vm as unknown as DialogVm).save()

    expect(showSnackBar.mock.calls).toEqual([[{ msg: successMsg }], [{ msg: 'Failed to load groups.', error: true }]])
  })
})

// the editor panel replaces the table; it is not teleported, so its error shows inside the wrapper
describe('GroupEditorPanel save results', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.reloadError = ''
  })

  const mountPanel = () => mount(GroupEditorPanel, { props: { group }, global: { plugins: [PrimeVue] }})
  const save = (wrapper: { vm: unknown }) => (wrapper.vm as { save: () => Promise<void> }).save()

  it('closes when the save is rejected as stale, and shows the stale text once', async () => {
    store.updateGroup.mockResolvedValue({ message: STALE_GROUP_MESSAGE, stale: true })
    const wrapper = mountPanel()
    await save(wrapper)

    expect(wrapper.emitted('close')).toHaveLength(1)
    expect(showSnackBar.mock.calls).toEqual([[{ msg: STALE_GROUP_MESSAGE, error: true }]])
  })

  it('stays open and shows other errors in the panel only', async () => {
    store.updateGroup.mockResolvedValue({ message: 'Something else went wrong', stale: false })
    const wrapper = mountPanel()
    await save(wrapper)
    await nextTick()

    expect(wrapper.emitted('close')).toBeUndefined()
    expect(wrapper.find('[data-test="save-error"]').text()).toBe('Something else went wrong')
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('does not close on the stale wording without the flag', async () => {
    store.updateGroup.mockResolvedValue({ message: STALE_GROUP_MESSAGE, stale: false })
    const wrapper = mountPanel()
    await save(wrapper)

    expect(wrapper.emitted('close')).toBeUndefined()
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('closes on an update and says so, with the spinner around the save', async () => {
    store.updateGroup.mockResolvedValue(null)
    const wrapper = mountPanel()
    await save(wrapper)

    expect(wrapper.emitted('close')).toHaveLength(1)
    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'Group \'Ops\' updated.' }]])
    expect(spinner.startSpinner).toHaveBeenCalledTimes(1)
    expect(spinner.stopSpinner).toHaveBeenCalledTimes(1)
  })

  it('closes on a create and says so', async () => {
    store.createGroup.mockResolvedValue(null)
    const wrapper = mount(GroupEditorPanel, { props: { group: null }, global: { plugins: [PrimeVue] }})
    const vm = wrapper.vm as unknown as { name: string, save: () => Promise<void> }
    vm.name = 'Dev'
    await vm.save()

    expect(wrapper.emitted('close')).toHaveLength(1)
    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'Group \'Dev\' created.' }]])
  })

  it('on success whose reload failed, says both', async () => {
    store.updateGroup.mockImplementation(async () => {
      store.reloadError = 'Failed to load groups.'
      return null
    })
    const wrapper = mountPanel()
    await save(wrapper)

    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'Group \'Ops\' updated.' }], [{ msg: 'Failed to load groups.', error: true }]])
  })
})

describe('GroupsTable delete results', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.reloadError = ''
  })

  const deleteFromTable = async () => {
    const wrapper = mount(GroupsTable, { shallow: true, global: { plugins: [PrimeVue], directives: { tooltip: {}}}})
    const vm = wrapper.vm as unknown as { askDelete: (g: typeof group) => void, confirmDelete: () => Promise<void>, showDeleteConfirmation: boolean }
    vm.askDelete(group)
    await vm.confirmDelete()
    return vm
  }

  it('says the group was deleted', async () => {
    store.deleteGroup.mockResolvedValue(null)
    const vm = await deleteFromTable()

    expect(vm.showDeleteConfirmation).toBe(false)
    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'Group \'Ops\' deleted.' }]])
    expect(spinner.startSpinner).toHaveBeenCalledTimes(1)
    expect(spinner.stopSpinner).toHaveBeenCalledTimes(1)
  })

  it('shows a failed delete with the server text', async () => {
    store.deleteGroup.mockResolvedValue({ message: 'Group \'Ops\' cannot be deleted.', stale: false })
    const vm = await deleteFromTable()

    expect(vm.showDeleteConfirmation).toBe(false)
    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'Group \'Ops\' cannot be deleted.', error: true }]])
  })

  it('shows a stale delete with the stale text once', async () => {
    store.deleteGroup.mockResolvedValue({ message: STALE_GROUP_MESSAGE, stale: true })
    await deleteFromTable()

    expect(showSnackBar.mock.calls).toEqual([[{ msg: STALE_GROUP_MESSAGE, error: true }]])
  })

  it('on success whose reload failed, says both', async () => {
    store.deleteGroup.mockImplementation(async () => {
      store.reloadError = 'Failed to load groups.'
      return null
    })
    await deleteFromTable()

    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'Group \'Ops\' deleted.' }], [{ msg: 'Failed to load groups.', error: true }]])
  })
})

// the version of the row the editor was opened on must reach the store, or the server cannot detect a stale write
describe('version reaches the store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.updateGroup.mockResolvedValue(null)
    store.renameGroup.mockResolvedValue(null)
    store.deleteGroup.mockResolvedValue(null)
  })

  it('GroupEditorPanel sends the group with its version', async () => {
    const wrapper = mount(GroupEditorPanel, { props: { group }, global: { plugins: [PrimeVue] }})
    await (wrapper.vm as unknown as { save: () => Promise<void> }).save()

    expect(store.updateGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'Ops', version: 3 }))
  })

  it('GroupRenameDialog sends the version', async () => {
    const wrapper = mount(GroupRenameDialog, { props: { visible: true, groupName: 'Ops', version: 3 }, global: { plugins: [PrimeVue] }})
    ;(wrapper.vm as unknown as { newName: string }).newName = 'Dev'
    await (wrapper.vm as unknown as { save: () => Promise<void> }).save()

    expect(store.renameGroup).toHaveBeenCalledWith('Ops', 'Dev', 3)
  })

  it('GroupsTable sends the version when deleting', async () => {
    const wrapper = mount(GroupsTable, { shallow: true, global: { plugins: [PrimeVue] }})
    const vm = wrapper.vm as unknown as { askDelete: (g: typeof group) => void, confirmDelete: () => Promise<void> }
    vm.askDelete(group)
    await vm.confirmDelete()

    expect(store.deleteGroup).toHaveBeenCalledWith('Ops', 3)
  })
})

// the rename reloads the list; an edit opened from the table must then send the renamed name and the new version,
// not the row the table showed before the rename
describe('a rename followed by an edit', () => {
  beforeEach(() => vi.clearAllMocks())

  it('sends the renamed group name and the new version', async () => {
    const live = useGroupAdminStore()
    live.groups = [{ ...group }]
    store.renameGroup.mockImplementation(async () => {
      // what the real store does on success: reload the list
      live.groups = [{ name: 'Dev', users: [], version: 4 }]
      return null
    })
    store.updateGroup.mockResolvedValue(null)
    const table = mount(GroupsTable, { global: { plugins: [PrimeVue], directives: { tooltip: {}}}})

    // rename through the row's menu
    await table.find('[data-test="group-actions-menu-button"]').trigger('click')
    ;(table.vm as unknown as { rowMenuItems: { label: string, command: () => void }[] }).rowMenuItems
      .find(item => item.label === 'Rename')!.command()
    await nextTick()
    const rename = table.findComponent(GroupRenameDialog)
    expect(rename.props()).toMatchObject({ visible: true, groupName: 'Ops', version: 3 })
    ;(rename.vm as unknown as { newName: string }).newName = 'Dev'
    await (rename.vm as unknown as { save: () => Promise<void> }).save()
    await nextTick()
    expect(rename.props('visible')).toBe(false)

    // edit the reloaded row through its Edit button: the table opens the editor on that row
    const editButtons = table.findAll('[data-test="edit-group-button"]')
    expect(editButtons).toHaveLength(1)
    await editButtons[0].trigger('click')
    expect(store.openGroupEditor).toHaveBeenCalledTimes(1)
    const opened = store.openGroupEditor.mock.calls[0][0]
    expect(opened).toMatchObject({ name: 'Dev', version: 4 })

    // and the editor saves what it was opened on
    const editor = mount(GroupEditorPanel, { props: { group: opened }, global: { plugins: [PrimeVue] }})
    await (editor.vm as unknown as { save: () => Promise<void> }).save()

    expect(store.updateGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'Dev', version: 4 }))
  })
})

describe('GroupEditorPanel comment limit', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.updateGroup.mockResolvedValue(null)
  })

  const mountEditor = (comments?: string) =>
    mount(GroupEditorPanel, { props: { group: { ...group, comments }}, global: { plugins: [PrimeVue] }})
  const vmOf = (wrapper: { vm: unknown }) => wrapper.vm as unknown as { comments: string, commentsProblem: string | null, isValid: boolean, save: () => Promise<void> }

  it('flags a new comment over 4096 characters and blocks Save', async () => {
    const wrapper = mountEditor('short')
    vmOf(wrapper).comments = 'a'.repeat(4097)
    await nextTick()

    expect(vmOf(wrapper).commentsProblem).toContain('4096')
    // the Save button is disabled by isValid
    expect(vmOf(wrapper).isValid).toBe(false)
  })

  it('lets a stored comment over the limit be saved unchanged', async () => {
    const wrapper = mountEditor('a'.repeat(5000))
    await nextTick()
    // the Save button is enabled by isValid
    expect(vmOf(wrapper).isValid).toBe(true)
    await vmOf(wrapper).save()

    expect(vmOf(wrapper).commentsProblem).toBeNull()
    expect(store.updateGroup).toHaveBeenCalled()
  })
})
