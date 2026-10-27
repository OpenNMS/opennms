import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

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

const group = { name: 'Ops', users: [], version: 3 }

const cases = [
  ['GroupEditorDialog', () => mount(GroupEditorDialog, { props: { visible: true, group }, global: { plugins: [PrimeVue] }}), store.updateGroup],
  ['GroupRenameDialog', () => mount(GroupRenameDialog, { props: { visible: true, groupName: 'Ops', version: 3 }, global: { plugins: [PrimeVue] }}), store.renameGroup]
] as const

describe.each(cases)('%s', (_name, mountDialog, storeCall) => {
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

    expect(wrapper.emitted('update:visible')).toBeUndefined()
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
