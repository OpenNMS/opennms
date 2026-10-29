import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import UsersTable from '@/components/ManageUsers/UsersTable.vue'
import UserEditorPanel from '@/components/ManageUsers/UserEditorPanel.vue'
import UserPasswordDialog from '@/components/ManageUsers/UserPasswordDialog.vue'
import UserRenameDialog from '@/components/ManageUsers/UserRenameDialog.vue'
import { useUserAdminStore } from '@/stores/userAdminStore'

const store = vi.hoisted(() => ({
  updateUser: vi.fn(),
  openUserEditor: vi.fn(),
  createUser: vi.fn(),
  renameUser: vi.fn(),
  setPassword: vi.fn(),
  deleteUser: vi.fn(),
  users: [] as unknown[],
  availableRoles: [] as string[]
}))

// reactive, so that a table re-renders its rows when a test replaces store.users through useUserAdminStore()
vi.mock('@/stores/userAdminStore', async () => {
  const { reactive } = await import('vue')
  const live = reactive(store)
  return { useUserAdminStore: () => live }
})

// the dialogs are teleported to the body; unmount each one, or a later test can find an earlier test's dialog
enableAutoUnmount(afterEach)

const user = { userId: 'jdoe', fullName: 'J Doe', version: 3 }

// each case: a mounted dialog, the store call its Save makes and the data-test of the dialog
const cases = [
  ['UserRenameDialog', () => mount(UserRenameDialog, { props: { visible: true, userId: 'jdoe', version: 3 }, global: { plugins: [PrimeVue] }}), store.renameUser, 'user-rename-dialog'],
  ['UserPasswordDialog', () => mount(UserPasswordDialog, { props: { visible: true, userId: 'jdoe', version: 3 }, global: { plugins: [PrimeVue] }}), store.setPassword, 'user-password-dialog']
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
    storeCall.mockResolvedValue({ message: 'This user was changed by someone else. The list has been reloaded; reapply your change.', stale: false })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as { save: () => Promise<void> }).save()

    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })
})

// the editor panel replaces the table; it is not teleported, so its error shows inside the wrapper
describe('UserEditorPanel save results', () => {
  beforeEach(() => vi.clearAllMocks())

  const mountPanel = () => mount(UserEditorPanel, { props: { user }, global: { plugins: [PrimeVue] }})
  const save = (wrapper: { vm: unknown }) => (wrapper.vm as { save: () => Promise<void> }).save()

  it('closes when the save is rejected as stale', async () => {
    // the text is irrelevant: only the stale flag closes the panel
    store.updateUser.mockResolvedValue({ message: 'any wording at all', stale: true })
    const wrapper = mountPanel()
    await save(wrapper)

    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('stays open and shows other errors', async () => {
    store.updateUser.mockResolvedValue({ message: 'Something else went wrong', stale: false })
    const wrapper = mountPanel()
    await save(wrapper)
    await nextTick()

    expect(wrapper.emitted('close')).toBeUndefined()
    expect(wrapper.find('[data-test="save-error"]').text()).toBe('Something else went wrong')
  })

  it('does not close on the stale wording without the flag', async () => {
    store.updateUser.mockResolvedValue({ message: 'This user was changed by someone else. The list has been reloaded; reapply your change.', stale: false })
    const wrapper = mountPanel()
    await save(wrapper)

    expect(wrapper.emitted('close')).toBeUndefined()
  })
})

// the version of the row the editor was opened on must reach the store, or the server cannot detect a stale write
describe('version reaches the store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.updateUser.mockResolvedValue(null)
    store.renameUser.mockResolvedValue(null)
    store.setPassword.mockResolvedValue(null)
    store.deleteUser.mockResolvedValue(null)
  })

  const save = (wrapper: { vm: unknown }) => (wrapper.vm as { save: () => Promise<void> }).save()

  it('UserEditorPanel sends the user with its version', async () => {
    await save(mount(UserEditorPanel, { props: { user }, global: { plugins: [PrimeVue] }}))

    expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({ userId: 'jdoe', version: 3 }))
  })

  it('UserRenameDialog sends the version', async () => {
    const wrapper = mount(UserRenameDialog, { props: { visible: true, userId: 'jdoe', version: 3 }, global: { plugins: [PrimeVue] }})
    ;(wrapper.vm as unknown as { newUserId: string }).newUserId = 'jsmith'
    await save(wrapper)

    expect(store.renameUser).toHaveBeenCalledWith('jdoe', 'jsmith', 3)
  })

  it('UserPasswordDialog sends the version', async () => {
    const wrapper = mount(UserPasswordDialog, { props: { visible: true, userId: 'jdoe', version: 3 }, global: { plugins: [PrimeVue] }})
    const vm = wrapper.vm as unknown as { password: string, confirmation: string }
    vm.password = 'secret-pass-1'
    vm.confirmation = 'secret-pass-1'
    await save(wrapper)

    expect(store.setPassword).toHaveBeenCalledWith('jdoe', 'secret-pass-1', 3)
  })

  it('UsersTable sends the version when deleting', async () => {
    const wrapper = mount(UsersTable, { shallow: true, global: { plugins: [PrimeVue] }})
    const vm = wrapper.vm as unknown as { askDelete: (u: typeof user) => void, confirmDelete: () => Promise<void> }
    vm.askDelete(user)
    await vm.confirmDelete()

    expect(store.deleteUser).toHaveBeenCalledWith('jdoe', 3)
  })
})

// the rename reloads the list; an edit opened from the table must then send the renamed id and the new version,
// not the row the table showed before the rename
describe('a rename followed by an edit', () => {
  beforeEach(() => vi.clearAllMocks())

  it('sends the renamed user id and the new version', async () => {
    const live = useUserAdminStore()
    live.users = [{ ...user }]
    store.renameUser.mockImplementation(async () => {
      // what the real store does on success: reload the list
      live.users = [{ userId: 'jsmith', fullName: 'J Doe', version: 4 }]
      return null
    })
    store.updateUser.mockResolvedValue(null)
    const table = mount(UsersTable, { global: { plugins: [PrimeVue], directives: { tooltip: {}}}})

    // rename through the row's menu
    await table.find('[data-test="user-actions-menu-button"]').trigger('click')
    ;(table.vm as unknown as { rowMenuItems: { label: string, command: () => void }[] }).rowMenuItems
      .find(item => item.label === 'Rename')!.command()
    await nextTick()
    const rename = table.findComponent(UserRenameDialog)
    expect(rename.props()).toMatchObject({ visible: true, userId: 'jdoe', version: 3 })
    ;(rename.vm as unknown as { newUserId: string }).newUserId = 'jsmith'
    await (rename.vm as unknown as { save: () => Promise<void> }).save()
    await nextTick()
    expect(rename.props('visible')).toBe(false)

    // edit the reloaded row through its Edit button: the table opens the editor on that row
    const editButtons = table.findAll('[data-test="edit-user-button"]')
    expect(editButtons).toHaveLength(1)
    await editButtons[0].trigger('click')
    expect(store.openUserEditor).toHaveBeenCalledTimes(1)
    const opened = store.openUserEditor.mock.calls[0][0]
    expect(opened).toMatchObject({ userId: 'jsmith', version: 4 })

    // and the editor saves what it was opened on
    const editor = mount(UserEditorPanel, { props: { user: opened }, global: { plugins: [PrimeVue] }})
    await (editor.vm as unknown as { save: () => Promise<void> }).save()

    expect(store.updateUser).toHaveBeenCalledWith(expect.objectContaining({ userId: 'jsmith', version: 4 }))
  })
})

describe('UserEditorPanel comment limit', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.updateUser.mockResolvedValue(null)
  })

  const mountEditor = (userComments?: string) =>
    mount(UserEditorPanel, { props: { user: { ...user, userComments }}, global: { plugins: [PrimeVue] }})
  const vmOf = (wrapper: { vm: unknown }) => wrapper.vm as unknown as { form: { comments: string }, commentsProblem: string | null, isValid: boolean, save: () => Promise<void> }

  it('flags a new comment over 4096 characters and blocks Save', async () => {
    const wrapper = mountEditor('short')
    vmOf(wrapper).form.comments = 'a'.repeat(4097)
    await nextTick()

    expect(vmOf(wrapper).commentsProblem).toContain('4096')
    // the Save button is disabled by isValid
    expect(vmOf(wrapper).isValid).toBe(false)
  })

  it('lets a stored comment over the limit be saved unchanged', async () => {
    const stored = 'a'.repeat(5000)
    const wrapper = mountEditor(stored)
    await nextTick()
    // the Save button is enabled by isValid
    expect(vmOf(wrapper).isValid).toBe(true)
    await vmOf(wrapper).save()

    expect(vmOf(wrapper).commentsProblem).toBeNull()
    expect(store.updateUser).toHaveBeenCalled()
  })
})
