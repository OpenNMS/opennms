import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserAdminStore } from '@/stores/userAdminStore'
import API from '@/services'
import { ManagedUser } from '@/types/userAdmin'

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
    { userId: 'admin', fullName: 'Administrator', roles: ['ROLE_ADMIN'] },
    { userId: 'noc', fullName: 'NOC Operator', email: 'noc@example.com', roles: ['ROLE_USER'] }
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
    vi.mocked(API.getManagedUsers).mockResolvedValue(mockUsers)
    vi.mocked(API.getAvailableUserRoles).mockResolvedValue(['ROLE_ADMIN', 'ROLE_USER'])

    await store.populate()

    expect(store.users).toEqual(mockUsers)
    expect(store.availableRoles).toEqual(['ROLE_ADMIN', 'ROLE_USER'])
  })

  it('createUser should refresh on success', async () => {
    vi.mocked(API.createManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(mockUsers)

    const ok = await store.createUser({ userId: 'noc', password: 'secret' })

    expect(ok).toBe(null)
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
  })

  it('createUser should not refresh on failure', async () => {
    vi.mocked(API.createManagedUser).mockResolvedValue('it failed')

    const ok = await store.createUser({ userId: 'noc', password: 'secret' })

    expect(ok).toBe('it failed')
    expect(API.getManagedUsers).not.toHaveBeenCalled()
  })

  it('updateUser should refresh on success', async () => {
    vi.mocked(API.updateManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(mockUsers)

    await store.updateUser(mockUsers[1])

    expect(API.updateManagedUser).toHaveBeenCalledWith(mockUsers[1])
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
  })

  it('renameUser should pass old and new ids and refresh', async () => {
    vi.mocked(API.renameManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue(mockUsers)

    await store.renameUser('noc', 'noc2')

    expect(API.renameManagedUser).toHaveBeenCalledWith('noc', 'noc2')
    expect(API.getManagedUsers).toHaveBeenCalledTimes(1)
  })

  it('deleteUser should refresh on success', async () => {
    vi.mocked(API.deleteManagedUser).mockResolvedValue(null)
    vi.mocked(API.getManagedUsers).mockResolvedValue([mockUsers[0]])

    await store.deleteUser('noc')

    expect(store.users).toEqual([mockUsers[0]])
  })

  it('a failed refresh should keep the previous user list', async () => {
    vi.mocked(API.getManagedUsers).mockResolvedValue(mockUsers)
    await store.getUsers()
    expect(store.users).toEqual(mockUsers)

    vi.mocked(API.getManagedUsers).mockResolvedValue(null)
    await store.getUsers()
    expect(store.users).toEqual(mockUsers)
  })

  it('setPassword should not trigger a reload', async () => {
    vi.mocked(API.setManagedUserPassword).mockResolvedValue(null)

    const ok = await store.setPassword('noc', 'newpw')

    expect(ok).toBe(null)
    expect(API.setManagedUserPassword).toHaveBeenCalledWith('noc', 'newpw')
    expect(API.getManagedUsers).not.toHaveBeenCalled()
  })
})
