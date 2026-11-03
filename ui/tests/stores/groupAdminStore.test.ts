import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import API from '@/services'
import { ManagedGroup } from '@/types/groupAdmin'

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
    { name: 'Admin', comments: 'The administrators', users: ['admin'] },
    { name: 'NOC', users: ['second', 'first'] }
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

    const group: ManagedGroup = { name: 'NOC', users: ['second', 'first'] }
    const ok = await store.createGroup(group)

    expect(ok).toBe(null)
    expect(API.createManagedGroup).toHaveBeenCalledWith(group)
    expect(vi.mocked(API.createManagedGroup).mock.calls[0][0].users).toEqual(['second', 'first'])
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
  })

  it('createGroup should not refresh on failure', async () => {
    vi.mocked(API.createManagedGroup).mockResolvedValue('it failed')

    const ok = await store.createGroup({ name: 'NOC' })

    expect(ok).toBe('it failed')
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

    await store.renameGroup('NOC', 'NOC2')

    expect(API.renameManagedGroup).toHaveBeenCalledWith('NOC', 'NOC2')
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
  })

  it('deleteGroup should refresh on success', async () => {
    vi.mocked(API.deleteManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue([mockGroups[0]])

    await store.deleteGroup('NOC')

    expect(store.groups).toEqual([mockGroups[0]])
  })
})
