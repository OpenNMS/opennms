import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { AdminEditMode } from '@/types/adminWrite'
import API from '@/services'
import { ManagedGroup, ManagedGroupCreate, STALE_GROUP_MESSAGE, STALE_GROUP_RELOAD_FAILED_MESSAGE } from '@/types/groupAdmin'
import { createResultWithPayload } from '@/types/validation'

const loaded = <T>(payload: T) => createResultWithPayload(true, '', payload)
const failed = (message: string) => createResultWithPayload<never>(false, message)
const UNAVAILABLE = 'User management through this API needs org.opennms.usermgmt.store=db.'

// feedback belongs to the components: the store must never show any
const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

vi.mock('@/services', () => ({
  default: {
    getManagedGroups: vi.fn(),
    getGroupMemberCandidates: vi.fn(),
    getAvailableGroupCategories: vi.fn(),
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
    vi.mocked(API.getAvailableGroupCategories).mockResolvedValue(loaded([]))
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should start empty', () => {
    expect(store.groups).toEqual([])
    expect(store.memberCandidates).toEqual([])
    expect(store.availableCategories).toEqual([])
  })

  it('populate should load groups, member candidates and available categories', async () => {
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))
    vi.mocked(API.getGroupMemberCandidates).mockResolvedValue(loaded(['admin', 'first', 'second']))
    vi.mocked(API.getAvailableGroupCategories).mockResolvedValue(loaded(['Routers', 'Servers']))

    expect(await store.populate()).toEqual(expect.objectContaining({ success: true }))

    expect(store.groups).toEqual(mockGroups)
    expect(store.memberCandidates).toEqual(['admin', 'first', 'second'])
    expect(store.availableCategories).toEqual(['Routers', 'Servers'])
  })

  it('populate reports a failed category read and keeps the earlier categories', async () => {
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))
    vi.mocked(API.getGroupMemberCandidates).mockResolvedValue(loaded(['admin']))
    vi.mocked(API.getAvailableGroupCategories).mockResolvedValue(loaded(['Routers']))
    await store.populate()

    vi.mocked(API.getAvailableGroupCategories).mockResolvedValue(failed('Failed to load available categories.'))
    expect(await store.populate()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load available categories.' }))
    expect(store.availableCategories).toEqual(['Routers'])
  })

  it('populate reports a failed read with its message', async () => {
    vi.mocked(API.getManagedGroups).mockResolvedValue(failed(UNAVAILABLE))
    vi.mocked(API.getGroupMemberCandidates).mockResolvedValue(loaded(['admin']))
    expect(await store.populate()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))
    expect(store.memberCandidates).toEqual(['admin'])

    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))
    vi.mocked(API.getGroupMemberCandidates).mockResolvedValue(failed('Failed to load users.'))
    expect(await store.populate()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load users.' }))
    expect(store.groups).toEqual(mockGroups)
    // a failed read keeps what was loaded before
    expect(store.memberCandidates).toEqual(['admin'])
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('getGroups returns the read result', async () => {
    vi.mocked(API.getManagedGroups).mockResolvedValue(failed(UNAVAILABLE))

    expect(await store.getGroups()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))
  })

  it('a failed refresh should keep the previous group list', async () => {
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))
    await store.getGroups()
    expect(store.groups).toEqual(mockGroups)

    vi.mocked(API.getManagedGroups).mockResolvedValue(failed('Failed to load groups.'))
    await store.getGroups()
    expect(store.groups).toEqual(mockGroups)
  })

  it('createGroup should refresh on success and preserve member order in the payload', async () => {
    vi.mocked(API.createManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))

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
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))

    await store.updateGroup(mockGroups[1])

    expect(API.updateManagedGroup).toHaveBeenCalledWith(mockGroups[1])
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
  })

  it('renameGroup should pass old and new names and refresh', async () => {
    vi.mocked(API.renameManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))

    await store.renameGroup('NOC', 'NOC2', 1)

    expect(API.renameManagedGroup).toHaveBeenCalledWith('NOC', 'NOC2', 1)
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
  })

  it('deleteGroup should refresh on success', async () => {
    vi.mocked(API.deleteManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded([mockGroups[0]]))

    await store.deleteGroup('NOC', 1)

    expect(store.groups).toEqual([mockGroups[0]])
  })


  it('passes the version through rename and delete', async () => {
    vi.mocked(API.renameManagedGroup).mockResolvedValue(null)
    vi.mocked(API.deleteManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))

    await store.renameGroup('NOC', 'NOC2', 4)
    await store.deleteGroup('NOC', 4)

    expect(API.renameManagedGroup).toHaveBeenCalledWith('NOC', 'NOC2', 4)
    expect(API.deleteManagedGroup).toHaveBeenCalledWith('NOC', 4)
  })

  it('a write whose reload fails still succeeds, and reports the reload in reloadError', async () => {
    vi.mocked(API.updateManagedGroup).mockResolvedValue(null)
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))
    expect(await store.updateGroup(mockGroups[1])).toBe(null)
    expect(store.reloadError).toBe('')

    vi.mocked(API.getManagedGroups).mockResolvedValue(failed(UNAVAILABLE))
    vi.mocked(API.renameManagedGroup).mockResolvedValue(null)
    expect(await store.renameGroup('NOC', 'NOC2', 2)).toBe(null)
    expect(store.reloadError).toBe(UNAVAILABLE)

    vi.mocked(API.deleteManagedGroup).mockResolvedValue({ message: 'Failed to delete group \'x\'.', stale: false })
    await store.deleteGroup('x', 1)
    expect(store.reloadError).toBe('')
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('on a stale write reloads the list, then reports the stale message', async () => {
    vi.mocked(API.updateManagedGroup).mockResolvedValue({ message: '', stale: true })
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))

    const error = await store.updateGroup(mockGroups[1])

    expect(error).toEqual({ message: STALE_GROUP_MESSAGE, stale: true })
    expect(API.getManagedGroups).toHaveBeenCalledTimes(1)
    expect(store.groups).toEqual(mockGroups)

    vi.mocked(API.renameManagedGroup).mockResolvedValue({ message: '', stale: true })
    expect((await store.renameGroup('NOC', 'y', 1))?.stale).toBe(true)
    vi.mocked(API.renameManagedGroup).mockResolvedValue({ message: '', stale: true })
    expect((await store.renameGroup('NOC', 'x', 1))?.stale).toBe(true)
    vi.mocked(API.deleteManagedGroup).mockResolvedValue({ message: '', stale: true })
    expect((await store.deleteGroup('NOC', 1))?.stale).toBe(true)
    expect(API.getManagedGroups).toHaveBeenCalledTimes(4)
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('on a stale write whose reload fails, says the list could not be reloaded', async () => {
    vi.mocked(API.deleteManagedGroup).mockResolvedValue({ message: '', stale: true })
    vi.mocked(API.getManagedGroups).mockResolvedValue(failed('Failed to load groups.'))

    const error = await store.deleteGroup('NOC', 1)

    expect(error).toEqual({ message: STALE_GROUP_RELOAD_FAILED_MESSAGE, stale: true })
    expect(showSnackBar).not.toHaveBeenCalled()
    expect(STALE_GROUP_RELOAD_FAILED_MESSAGE).not.toContain('has been reloaded')
  })

  it('on 409 returns the server message without reloading', async () => {
    vi.mocked(API.renameManagedGroup).mockResolvedValue({ message: 'Group \'x\' already exists.', stale: false })

    expect(await store.renameGroup('NOC', 'x', 1)).toEqual({ message: 'Group \'x\' already exists.', stale: false })
    expect(API.getManagedGroups).not.toHaveBeenCalled()
  })

  it('opens the editor on a group or a new one, and closes it back to the table', () => {
    expect(store.editMode).toBe(AdminEditMode.Table)

    store.openGroupEditor(mockGroups[0])
    expect(store.editMode).toBe(AdminEditMode.Edit)
    expect(store.currentGroup).toEqual(mockGroups[0])

    store.openGroupEditor(null)
    expect(store.editMode).toBe(AdminEditMode.Create)
    expect(store.currentGroup).toBeNull()

    store.closeGroupEditor()
    expect(store.editMode).toBe(AdminEditMode.Table)
    expect(store.currentGroup).toBeNull()
  })
})
