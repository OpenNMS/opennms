import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import API from '@/services'
import { ManagedGroup, ManagedGroupCreate, STALE_GROUP_MESSAGE, STALE_GROUP_RELOAD_FAILED_MESSAGE } from '@/types/groupAdmin'

const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

vi.mock('@/services', () => ({
  default: {
    getManagedGroups: vi.fn(),
    getGroupMemberCandidates: vi.fn(),
    createManagedGroup: vi.fn(),
    updateManagedGroup: vi.fn(),
    renameManagedGroup: vi.fn(),
    deleteManagedGroup: vi.fn()
  }
}))

describe('useGroupAdminStore', () => {
  let store: ReturnType<typeof useGroupAdminStore>

  const mockGroups: ManagedGroup[] = [
    { name: 'Admin', comments: 'The administrators', users: ['admin'], version: 1 },
    { name: 'NOC', users: ['second', 'first'], version: 2 }
  ]

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useGroupAdminStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should start empty', () => {
    expect(store.groups).toEqual([])
    expect(store.memberCandidates).toEqual([])
  })

  it('populate should load groups and member candidates', async () => {
    vi.mocked(API.getManagedGroups).mockResolvedValue(mockGroups)
    vi.mocked(API.getGroupMemberCandidates).mockResolvedValue(['admin', 'first', 'second'])

    await store.populate()

    expect(store.groups).toEqual(mockGroups)
    expect(store.memberCandidates).toEqual(['admin', 'first', 'second'])
  })

  it('a failed refresh should keep the previous group list', async () => {
    vi.mocked(API.getManagedGroups).mockResolvedValue(mockGroups)
    await store.getGroups()
    expect(store.groups).toEqual(mockGroups)

    vi.mocked(API.getManagedGroups).mockResolvedValue(null)
    await store.getGroups()
    expect(store.groups).toEqual(mockGroups)
  })

  it('createGroup should refresh on success and preserve member order in the payload', async () => {
    vi.mocked(API.createManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(mockGroups)

    const group: ManagedGroupCreate = { name: 'NOC', users: ['second', 'first'] }
    const ok = await store.createGroup(group)

    expect(ok).toBe(null)
    expect(API.createManagedGroup).toHaveBeenCalledWith(group)
    expect(vi.mocked(API.createManagedGroup).mock.calls[0][0].users).toEqual(['second', 'first'])
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
  })

  it('createGroup should not refresh on failure', async () => {
    vi.mocked(API.createManagedGroup).mockResolvedValue({ message: 'it failed', stale: false })

    const ok = await store.createGroup({ name: 'NOC' })

    expect(ok).toEqual({ message: 'it failed', stale: false })
    expect(API.getManagedGroups).not.toHaveBeenCalled()
  })

  it('updateGroup should refresh on success', async () => {
    vi.mocked(API.updateManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(mockGroups)

    await store.updateGroup(mockGroups[1])

    expect(API.updateManagedGroup).toHaveBeenCalledWith(mockGroups[1])
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
  })

  it('renameGroup should pass old and new names and refresh', async () => {
    vi.mocked(API.renameManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(mockGroups)

    await store.renameGroup('NOC', 'NOC2', 1)

    expect(API.renameManagedGroup).toHaveBeenCalledWith('NOC', 'NOC2', 1)
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
  })

  it('deleteGroup should refresh on success', async () => {
    vi.mocked(API.deleteManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue([mockGroups[0]])

    await store.deleteGroup('NOC', 1)

    expect(store.groups).toEqual([mockGroups[0]])
  })


  it('passes the version through rename and delete', async () => {
    vi.mocked(API.renameManagedGroup).mockResolvedValue(null)
    vi.mocked(API.deleteManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(mockGroups)

    await store.renameGroup('NOC', 'NOC2', 4)
    await store.deleteGroup('NOC', 4)

    expect(API.renameManagedGroup).toHaveBeenCalledWith('NOC', 'NOC2', 4)
    expect(API.deleteManagedGroup).toHaveBeenCalledWith('NOC', 4)
  })

  it('on a stale write reloads the list, then reports the stale message', async () => {
    vi.mocked(API.updateManagedGroup).mockResolvedValue({ message: '', stale: true })
    vi.mocked(API.getManagedGroups).mockResolvedValue(mockGroups)

    const error = await store.updateGroup(mockGroups[1])

    expect(error).toEqual({ message: STALE_GROUP_MESSAGE, stale: true })
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
    expect(store.groups).toEqual(mockGroups)
    expect(showSnackBar).toHaveBeenLastCalledWith({ msg: STALE_GROUP_MESSAGE, error: true })

    vi.mocked(API.renameManagedGroup).mockResolvedValue({ message: '', stale: true })
    expect((await store.renameGroup('NOC', 'y', 1))?.stale).toBe(true)
    vi.mocked(API.renameManagedGroup).mockResolvedValue({ message: '', stale: true })
    expect((await store.renameGroup('NOC', 'x', 1))?.stale).toBe(true)
    vi.mocked(API.deleteManagedGroup).mockResolvedValue({ message: '', stale: true })
    expect((await store.deleteGroup('NOC', 1))?.stale).toBe(true)
    expect(API.getManagedGroups).toHaveBeenCalledTimes(4)
  })

  it('on a stale write whose reload fails, says the list could not be reloaded', async () => {
    vi.mocked(API.deleteManagedGroup).mockResolvedValue({ message: '', stale: true })
    vi.mocked(API.getManagedGroups).mockResolvedValue(null)

    const error = await store.deleteGroup('NOC', 1)

    expect(error).toEqual({ message: STALE_GROUP_RELOAD_FAILED_MESSAGE, stale: true })
    expect(showSnackBar).toHaveBeenLastCalledWith({ msg: STALE_GROUP_RELOAD_FAILED_MESSAGE, error: true })
    expect(STALE_GROUP_RELOAD_FAILED_MESSAGE).not.toContain('has been reloaded')
  })

  it('on 409 returns the server message without reloading', async () => {
    vi.mocked(API.renameManagedGroup).mockResolvedValue({ message: 'Group \'x\' already exists.', stale: false })

    expect(await store.renameGroup('NOC', 'x', 1)).toEqual({ message: 'Group \'x\' already exists.', stale: false })
    expect(API.getManagedGroups).not.toHaveBeenCalled()
  })
})
