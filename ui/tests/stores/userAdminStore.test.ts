import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { AdminEditMode } from '@/types/adminWrite'
import API from '@/services'
import { ManagedUser, STALE_USER_MESSAGE, STALE_USER_RELOAD_FAILED_MESSAGE } from '@/types/userAdmin'
import { createResultWithPayload } from '@/types/validation'

const loaded = <T>(payload: T) => createResultWithPayload(true, '', payload)
const failed = (message: string) => createResultWithPayload<never>(false, message)
const UNAVAILABLE = 'User management through this API needs org.opennms.usermgmt.store=db.'

// feedback belongs to the components: the store must never show any
const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

vi.mock('@/services', () => ({
  default: {
    getManagedUsers: vi.fn(),
    getAvailableUserRoles: vi.fn(),
    createManagedUser: vi.fn(),
    updateManagedUser: vi.fn(),
    setManagedUserPassword: vi.fn(),
    renameManagedUser: vi.fn(),
    deleteManagedUser: vi.fn()
  }
}))

describe('useUserAdminStore', () => {
  let store: ReturnType<typeof useUserAdminStore>

  const mockUsers: ManagedUser[] = [
    { userId: 'admin', fullName: 'Administrator', roles: ['ROLE_ADMIN'], version: 1 },
    { userId: 'noc', fullName: 'NOC Operator', email: 'noc@example.com', roles: ['ROLE_USER'], version: 2 }
  ]

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useUserAdminStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should start empty', () => {
    expect(store.users).toEqual([])
    expect(store.availableRoles).toEqual([])
  })

  it('populate should load users and roles', async () => {
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))
    vi.mocked(API.getAvailableUserRoles).mockResolvedValue(loaded(['ROLE_ADMIN', 'ROLE_USER']))

    expect(await store.populate()).toEqual(expect.objectContaining({ success: true }))

    expect(store.users).toEqual(mockUsers)
    expect(store.availableRoles).toEqual(['ROLE_ADMIN', 'ROLE_USER'])
  })

  it('populate reports a failed read with its message', async () => {
    vi.mocked(API.getManagedUsers).mockResolvedValue(failed(UNAVAILABLE))
    vi.mocked(API.getAvailableUserRoles).mockResolvedValue(loaded(['ROLE_ADMIN']))
    expect(await store.populate()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))

    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))
    vi.mocked(API.getAvailableUserRoles).mockResolvedValue(failed('Failed to load available roles.'))
    expect(await store.populate()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load available roles.' }))
    expect(store.users).toEqual(mockUsers)
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('getUsers returns the read result', async () => {
    vi.mocked(API.getManagedUsers).mockResolvedValue(failed(UNAVAILABLE))

    expect(await store.getUsers()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))
  })

  it('createUser should refresh on success', async () => {
    vi.mocked(API.createManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))

    const ok = await store.createUser({ userId: 'noc', password: 'secret' })

    expect(ok).toBe(null)
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
  })

  it('createUser should not refresh on failure', async () => {
    vi.mocked(API.createManagedUser).mockResolvedValue({ message: 'it failed', stale: false })

    const ok = await store.createUser({ userId: 'noc', password: 'secret' })

    expect(ok).toEqual({ message: 'it failed', stale: false })
    expect(API.getManagedUsers).not.toHaveBeenCalled()
  })

  it('updateUser should refresh on success', async () => {
    vi.mocked(API.updateManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))

    await store.updateUser(mockUsers[1])

    expect(API.updateManagedUser).toHaveBeenCalledWith(mockUsers[1])
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
  })

  it('renameUser should pass old and new ids and refresh', async () => {
    vi.mocked(API.renameManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))

    await store.renameUser('noc', 'noc2', 2)

    expect(API.renameManagedUser).toHaveBeenCalledWith('noc', 'noc2', 2)
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
  })

  it('deleteUser should refresh on success', async () => {
    vi.mocked(API.deleteManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded([mockUsers[0]]))

    await store.deleteUser('noc', 2)

    expect(store.users).toEqual([mockUsers[0]])
  })

  it('a failed refresh should keep the previous user list', async () => {
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))
    await store.getUsers()
    expect(store.users).toEqual(mockUsers)

    vi.mocked(API.getManagedUsers).mockResolvedValue(failed('Failed to load users.'))
    await store.getUsers()
    expect(store.users).toEqual(mockUsers)
  })

  it('setPassword should reload the list on success', async () => {
    vi.mocked(API.setManagedUserPassword).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))

    const ok = await store.setPassword('noc', 'newpw', 2)

    expect(ok).toBe(null)
    expect(API.setManagedUserPassword).toHaveBeenCalledWith('noc', 'newpw', 2)
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
  })

  it('an update after a password change sends the bumped version', async () => {
    vi.mocked(API.setManagedUserPassword).mockResolvedValue(null)
    vi.mocked(API.updateManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded([{ userId: 'noc', version: 5 }]))
    store.users = [{ userId: 'noc', version: 4 }]

    await store.setPassword('noc', 'pw', store.users[0].version)
    expect(store.users[0].version).toBe(5)
    await store.updateUser(store.users[0])

    expect(API.updateManagedUser).toHaveBeenCalledWith({ userId: 'noc', version: 5 })
  })


  it('passes the version through rename, delete and password', async () => {
    vi.mocked(API.renameManagedUser).mockResolvedValue(null)
    vi.mocked(API.deleteManagedUser).mockResolvedValue(null)
    vi.mocked(API.setManagedUserPassword).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))

    await store.renameUser('noc', 'noc2', 4)
    await store.deleteUser('noc', 4)
    await store.setPassword('noc', 'pw', 4)

    expect(API.renameManagedUser).toHaveBeenCalledWith('noc', 'noc2', 4)
    expect(API.deleteManagedUser).toHaveBeenCalledWith('noc', 4)
    expect(API.setManagedUserPassword).toHaveBeenCalledWith('noc', 'pw', 4)
  })

  it('a write that succeeds keeps reloadError empty', async () => {
    vi.mocked(API.updateManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))

    expect(await store.updateUser(mockUsers[1])).toBe(null)
    expect(store.reloadError).toBe('')
  })

  it('a write whose reload fails still succeeds, and reports the reload in reloadError', async () => {
    vi.mocked(API.renameManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(failed(UNAVAILABLE))

    expect(await store.renameUser('noc', 'noc2', 2)).toBe(null)
    expect(store.reloadError).toBe(UNAVAILABLE)

    // the next write starts clean
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))
    vi.mocked(API.createManagedUser).mockResolvedValue(null)
    await store.createUser({ userId: 'x', password: 'pw' })
    expect(store.reloadError).toBe('')

    vi.mocked(API.getManagedUsers).mockResolvedValue(failed(UNAVAILABLE))
    await store.createUser({ userId: 'x', password: 'pw' })
    expect(store.reloadError).toBe(UNAVAILABLE)
    vi.mocked(API.deleteManagedUser).mockResolvedValue({ message: 'Failed to delete user \'x\'.', stale: false })
    await store.deleteUser('x', 1)
    expect(store.reloadError).toBe('')
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('on a stale write reloads the list, then reports the stale message', async () => {
    vi.mocked(API.updateManagedUser).mockResolvedValue({ message: '', stale: true })
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))

    const error = await store.updateUser(mockUsers[1])

    expect(error).toEqual({ message: STALE_USER_MESSAGE, stale: true })
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
    expect(store.users).toEqual(mockUsers)

    vi.mocked(API.setManagedUserPassword).mockResolvedValue({ message: '', stale: true })
    expect((await store.setPassword('noc', 'pw', 1))?.stale).toBe(true)
    vi.mocked(API.renameManagedUser).mockResolvedValue({ message: '', stale: true })
    expect((await store.renameUser('noc', 'x', 1))?.stale).toBe(true)
    vi.mocked(API.deleteManagedUser).mockResolvedValue({ message: '', stale: true })
    expect((await store.deleteUser('noc', 1))?.stale).toBe(true)
    expect(API.getManagedUsers).toHaveBeenCalledTimes(4)
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('on a stale write whose reload fails, says the list could not be reloaded', async () => {
    vi.mocked(API.deleteManagedUser).mockResolvedValue({ message: '', stale: true })
    vi.mocked(API.getManagedUsers).mockResolvedValue(failed('Failed to load users.'))

    const error = await store.deleteUser('noc', 1)

    expect(error).toEqual({ message: STALE_USER_RELOAD_FAILED_MESSAGE, stale: true })
    expect(showSnackBar).not.toHaveBeenCalled()
    expect(STALE_USER_RELOAD_FAILED_MESSAGE).not.toContain('has been reloaded')
  })

  it('on 409 returns the server message without reloading', async () => {
    vi.mocked(API.renameManagedUser).mockResolvedValue({ message: 'User \'x\' already exists.', stale: false })

    expect(await store.renameUser('noc', 'x', 1)).toEqual({ message: 'User \'x\' already exists.', stale: false })
    expect(API.getManagedUsers).not.toHaveBeenCalled()
  })

  it('opens the editor on a user or a new one, and closes it back to the table', () => {
    expect(store.editMode).toBe(AdminEditMode.Table)

    store.openUserEditor(mockUsers[1])
    expect(store.editMode).toBe(AdminEditMode.Edit)
    expect(store.currentUser).toEqual(mockUsers[1])

    store.openUserEditor(null)
    expect(store.editMode).toBe(AdminEditMode.Create)
    expect(store.currentUser).toBeNull()

    store.closeUserEditor()
    expect(store.editMode).toBe(AdminEditMode.Table)
    expect(store.currentUser).toBeNull()
  })
})
