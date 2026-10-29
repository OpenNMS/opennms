import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import GroupsTable from '@/components/ManageGroups/GroupsTable.vue'
import GroupEditorDialog from '@/components/ManageGroups/GroupEditorDialog.vue'
import GroupRenameDialog from '@/components/ManageGroups/GroupRenameDialog.vue'

const store = vi.hoisted(() => ({
  updateGroup: vi.fn(),
  createGroup: vi.fn(),
  renameGroup: vi.fn(),
  deleteGroup: vi.fn(),
  groups: [] as unknown[],
  memberCandidates: [] as string[]
}))

vi.mock('@/stores/groupAdminStore', () => ({ useGroupAdminStore: () => store }))

// the dialogs are teleported to the body; unmount each one, or a later test can find an earlier test's dialog
enableAutoUnmount(afterEach)

const group = { name: 'Ops', users: [], version: 3 }

// each case: a mounted dialog, the store call its Save makes and the data-test of the dialog
const cases = [
  ['GroupEditorDialog', () => mount(GroupEditorDialog, { props: { visible: true, group }, global: { plugins: [PrimeVue] }}), store.updateGroup, 'group-editor-dialog'],
  ['GroupRenameDialog', () => mount(GroupRenameDialog, { props: { visible: true, groupName: 'Ops', version: 3 }, global: { plugins: [PrimeVue] }}), store.renameGroup, 'group-rename-dialog']
] as const

describe.each(cases)('%s', (_name, mountDialog, storeCall, dialogTest) => {
  beforeEach(() => vi.clearAllMocks())

  it('closes when the save is rejected as stale', async () => {
    // the text is irrelevant: only the stale flag closes the dialog
    storeCall.mockResolvedValue({ message: 'any wording at all', stale: true })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as { save: () => Promise<void> }).save()

    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('stays open and shows other errors', async () => {
    storeCall.mockResolvedValue({ message: 'Something else went wrong', stale: false })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as { save: () => Promise<void> }).save()
    await nextTick()

    expect(wrapper.emitted('update:visible')).toBeUndefined()
    // only the error of this dialog: the dialogs are teleported to the body
    const errors = document.body.querySelectorAll(`[data-test="${dialogTest}"] [data-test="dialog-error"]`)
    expect(errors).toHaveLength(1)
    expect(errors[0].textContent).toBe('Something else went wrong')
  })

  it('does not close on the stale wording without the flag', async () => {
    storeCall.mockResolvedValue({ message: 'This group was changed by someone else. The list has been reloaded; reapply your change.', stale: false })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as { save: () => Promise<void> }).save()

    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })
})

// the version of the row the dialog was opened on must reach the store, or the server cannot detect a stale write
describe('version reaches the store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.updateGroup.mockResolvedValue(null)
    store.renameGroup.mockResolvedValue(null)
    store.deleteGroup.mockResolvedValue(null)
  })

  it('GroupEditorDialog sends the group with its version', async () => {
    const wrapper = mount(GroupEditorDialog, { props: { visible: true, group }, global: { plugins: [PrimeVue] }})
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

// the rename reloads the list; the next edit must send the renamed name and the new version, not the old row
describe('a rename followed by an edit', () => {
  beforeEach(() => vi.clearAllMocks())

  it('sends the renamed group name and the new version', async () => {
    store.groups = [{ ...group }]
    store.renameGroup.mockImplementation(async () => {
      // what the real store does on success: reload the list
      store.groups = [{ name: 'Dev', users: [], version: 4 }]
      return null
    })
    store.updateGroup.mockResolvedValue(null)

    const rename = mount(GroupRenameDialog, { props: { visible: true, groupName: 'Ops', version: 3 }, global: { plugins: [PrimeVue] }})
    ;(rename.vm as unknown as { newName: string }).newName = 'Dev'
    await (rename.vm as unknown as { save: () => Promise<void> }).save()
    expect(rename.emitted('update:visible')?.at(-1)).toEqual([false])

    const editor = mount(GroupEditorDialog, { props: { visible: true, group: store.groups[0] as typeof group }, global: { plugins: [PrimeVue] }})
    await (editor.vm as unknown as { save: () => Promise<void> }).save()

    expect(store.updateGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'Dev', version: 4 }))
  })
})

describe('GroupEditorDialog comment limit', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.updateGroup.mockResolvedValue(null)
  })

  const mountEditor = (comments?: string) =>
    mount(GroupEditorDialog, { props: { visible: true, group: { ...group, comments }}, global: { plugins: [PrimeVue] }})
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
