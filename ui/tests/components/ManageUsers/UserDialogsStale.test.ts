import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import UsersTable from '@/components/ManageUsers/UsersTable.vue'
import UserEditorDialog from '@/components/ManageUsers/UserEditorDialog.vue'
import UserPasswordDialog from '@/components/ManageUsers/UserPasswordDialog.vue'
import UserRenameDialog from '@/components/ManageUsers/UserRenameDialog.vue'

const store = vi.hoisted(() => ({
  updateUser: vi.fn(),
  createUser: vi.fn(),
  renameUser: vi.fn(),
  setPassword: vi.fn(),
  deleteUser: vi.fn(),
  users: [] as unknown[],
  availableRoles: [] as string[]
}))

vi.mock('@/stores/userAdminStore', () => ({ useUserAdminStore: () => store }))

const user = { userId: 'jdoe', fullName: 'J Doe', version: 3 }

// each case: a mounted dialog and the store call its Save makes
const cases = [
  ['UserEditorDialog', () => mount(UserEditorDialog, { props: { visible: true, user }, global: { plugins: [PrimeVue] }}), store.updateUser],
  ['UserRenameDialog', () => mount(UserRenameDialog, { props: { visible: true, userId: 'jdoe', version: 3 }, global: { plugins: [PrimeVue] }}), store.renameUser],
  ['UserPasswordDialog', () => mount(UserPasswordDialog, { props: { visible: true, userId: 'jdoe', version: 3 }, global: { plugins: [PrimeVue] }}), store.setPassword]
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
    storeCall.mockResolvedValue({ message: 'This user was changed by someone else. The list has been reloaded; reapply your change.', stale: false })
    const wrapper = mountDialog()

    await (wrapper.vm as unknown as { save: () => Promise<void> }).save()

    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })
})

// the version of the row the dialog was opened on must reach the store, or the server cannot detect a stale write
describe('version reaches the store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.updateUser.mockResolvedValue(null)
    store.renameUser.mockResolvedValue(null)
    store.setPassword.mockResolvedValue(null)
    store.deleteUser.mockResolvedValue(null)
  })

  const save = (wrapper: { vm: unknown }) => (wrapper.vm as { save: () => Promise<void> }).save()

  it('UserEditorDialog sends the user with its version', async () => {
    await save(mount(UserEditorDialog, { props: { visible: true, user }, global: { plugins: [PrimeVue] }}))

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
