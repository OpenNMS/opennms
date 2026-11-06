///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { AdminEditMode, adminWriteSuccess } from '@/types/adminWrite'
import API from '@/services'
import {
  ManagedOnCallRole,
  ManagedOnCallRoleCreate,
  OnCallScheduleSettings,
  STALE_ONCALL_ROLE_MESSAGE,
  STALE_ONCALL_ROLE_RELOAD_FAILED_MESSAGE
} from '@/types/onCallRoleAdmin'
import { ManagedUser } from '@/types/userAdmin'
import { ManagedGroup } from '@/types/groupAdmin'
import { createResultWithPayload } from '@/types/validation'

const loaded = <T>(payload: T) => createResultWithPayload(true, '', payload)
const failed = (message: string) => createResultWithPayload<never>(false, message)

// feedback belongs to the components: the store must never show any
const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

vi.mock('@/services', () => ({
  default: {
    getManagedOnCallRoles: vi.fn(),
    getOnCallScheduleSettings: vi.fn(),
    getManagedUsers: vi.fn(),
    getManagedGroups: vi.fn(),
    createManagedOnCallRole: vi.fn(),
    updateManagedOnCallRole: vi.fn(),
    renameManagedOnCallRole: vi.fn(),
    deleteManagedOnCallRole: vi.fn()
  }
}))

describe('useOnCallRoleAdminStore', () => {
  let store: ReturnType<typeof useOnCallRoleAdminStore>

  const mockRoles: ManagedOnCallRole[] = [
    { name: 'Ops Rota', membershipGroup: 'NOC', supervisor: 'admin', schedules: [], onCallNow: ['admin'], onCallNowIsSupervisor: false, version: 1 },
    { name: 'Night', membershipGroup: 'NOC', supervisor: 'admin', schedules: [], version: 2 }
  ]
  const mockUsers = [{ userId: 'admin', fullName: 'Administrator', version: 1 }, { userId: 'jdoe', version: 1 }] as ManagedUser[]
  const mockGroups = [{ name: 'NOC', users: ['jdoe', 'admin'], version: 1 }, { name: 'Empty', version: 1 }] as ManagedGroup[]
  const settings: OnCallScheduleSettings = { timeZone: 'UTC', shortMonths: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'] }

  const mockAllReads = () => {
    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(loaded(mockRoles))
    vi.mocked(API.getManagedUsers).mockResolvedValue(loaded(mockUsers))
    vi.mocked(API.getManagedGroups).mockResolvedValue(loaded(mockGroups))
    vi.mocked(API.getOnCallScheduleSettings).mockResolvedValue(loaded(settings))
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useOnCallRoleAdminStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should start empty', () => {
    expect(store.roles).toEqual([])
    expect(store.users).toEqual([])
    expect(store.groups).toEqual([])
    expect(store.scheduleSettings).toBeNull()
    expect(store.currentRole).toBeNull()
  })

  it('populate loads roles, users, groups and the schedule settings', async () => {
    mockAllReads()

    expect(await store.populate()).toEqual(expect.objectContaining({ success: true }))

    expect(store.roles).toEqual(mockRoles)
    expect(store.users).toEqual(mockUsers)
    expect(store.groups).toEqual(mockGroups)
    expect(store.scheduleSettings).toEqual(settings)
  })

  it.each([
    ['getManagedOnCallRoles', 'Failed to load on-call roles.'],
    ['getManagedUsers', 'Failed to load users.'],
    ['getManagedGroups', 'Failed to load groups.'],
    ['getOnCallScheduleSettings', 'Failed to load the schedule settings.']
  ] as const)('populate reports a failed %s with its message', async (read, message) => {
    mockAllReads()
    vi.mocked(API[read]).mockResolvedValue(failed(message))

    expect(await store.populate()).toEqual(expect.objectContaining({ success: false, message }))
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('populate reports the first failure and a failed read keeps what was loaded before', async () => {
    mockAllReads()
    await store.populate()

    vi.mocked(API.getManagedUsers).mockResolvedValue(failed('users down'))
    vi.mocked(API.getManagedGroups).mockResolvedValue(failed('groups down'))
    expect(await store.populate()).toEqual(expect.objectContaining({ success: false, message: 'users down' }))
    expect(store.users).toEqual(mockUsers)
    expect(store.groups).toEqual(mockGroups)
  })

  it('a slow earlier read does not replace the list of a later one', async () => {
    let finishFirst: (value: ReturnType<typeof loaded<ManagedOnCallRole[]>>) => void = () => {}
    vi.mocked(API.getManagedOnCallRoles)
      .mockReturnValueOnce(new Promise((resolve) => {
        finishFirst = resolve
      }))
      .mockResolvedValueOnce(loaded(mockRoles))
    const first = store.getRoles()
    await store.getRoles()
    finishFirst(loaded([]))
    await first

    expect(store.roles).toEqual(mockRoles)
  })

  it('a failed refresh keeps the previous role list', async () => {
    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(loaded(mockRoles))
    await store.getRoles()

    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(failed('Failed to load on-call roles.'))
    expect(await store.getRoles()).toEqual(expect.objectContaining({ success: false }))
    expect(store.roles).toEqual(mockRoles)
  })

  it('fullName looks up a user by id', async () => {
    mockAllReads()
    await store.populate()

    expect(store.fullName('admin')).toBe('Administrator')
    expect(store.fullName('jdoe')).toBeUndefined()
    expect(store.fullName('nobody')).toBeUndefined()
  })

  it('groupMembers lists a group\'s users in order, and nothing for an unknown group', async () => {
    mockAllReads()
    await store.populate()

    expect(store.groupMembers('NOC')).toEqual(['jdoe', 'admin'])
    expect(store.groupMembers('Empty')).toEqual([])
    expect(store.groupMembers('Nope')).toEqual([])
  })

  it('createRole refreshes on success and not on failure', async () => {
    vi.mocked(API.createManagedOnCallRole).mockResolvedValue(adminWriteSuccess())
    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(loaded(mockRoles))
    const role: ManagedOnCallRoleCreate = { name: 'Night', membershipGroup: 'NOC', supervisor: 'admin', schedules: [] }

    expect(await store.createRole(role)).toEqual(adminWriteSuccess())
    expect(API.createManagedOnCallRole).toHaveBeenCalledWith(role)
    expect(API.getManagedOnCallRoles).toHaveBeenCalledTimes(1)

    vi.mocked(API.createManagedOnCallRole).mockResolvedValue({ success: false, message: 'it failed', stale: false })
    expect(await store.createRole(role)).toEqual({ success: false, message: 'it failed', stale: false })
    expect(API.getManagedOnCallRoles).toHaveBeenCalledTimes(1)
  })

  it('update, rename and delete pass their arguments and refresh', async () => {
    vi.mocked(API.updateManagedOnCallRole).mockResolvedValue(adminWriteSuccess())
    vi.mocked(API.renameManagedOnCallRole).mockResolvedValue(adminWriteSuccess())
    vi.mocked(API.deleteManagedOnCallRole).mockResolvedValue(adminWriteSuccess())
    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(loaded([mockRoles[0]]))

    await store.updateRole(mockRoles[1])
    await store.renameRole('Night', 'Night2', 4)
    await store.deleteRole('Night', 4)

    expect(API.updateManagedOnCallRole).toHaveBeenCalledWith(mockRoles[1])
    expect(API.renameManagedOnCallRole).toHaveBeenCalledWith('Night', 'Night2', 4)
    expect(API.deleteManagedOnCallRole).toHaveBeenCalledWith('Night', 4)
    expect(API.getManagedOnCallRoles).toHaveBeenCalledTimes(3)
    expect(store.roles).toEqual([mockRoles[0]])
  })

  it('a write whose reload fails still succeeds, and reports the reload in errors', async () => {
    vi.mocked(API.renameManagedOnCallRole).mockResolvedValue(adminWriteSuccess())
    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(failed('Failed to load on-call roles.'))

    expect(await store.renameRole('Night', 'Night2', 2)).toEqual({ ...adminWriteSuccess(), errors: ['Failed to load on-call roles.'] })
  })

  it('on a stale write reloads the list, then reports the stale message', async () => {
    vi.mocked(API.updateManagedOnCallRole).mockResolvedValue({ success: false, message: '', stale: true })
    vi.mocked(API.renameManagedOnCallRole).mockResolvedValue({ success: false, message: '', stale: true })
    vi.mocked(API.deleteManagedOnCallRole).mockResolvedValue({ success: false, message: '', stale: true })
    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(loaded(mockRoles))

    expect(await store.updateRole(mockRoles[1])).toEqual({ success: false, message: STALE_ONCALL_ROLE_MESSAGE, stale: true })
    expect((await store.renameRole('Night', 'x', 1))?.stale).toBe(true)
    expect((await store.deleteRole('Night', 1))?.stale).toBe(true)
    expect(API.getManagedOnCallRoles).toHaveBeenCalledTimes(3)
    expect(store.roles).toEqual(mockRoles)
  })

  it('on a stale write whose reload fails, says the list could not be reloaded', async () => {
    vi.mocked(API.deleteManagedOnCallRole).mockResolvedValue({ success: false, message: '', stale: true })
    vi.mocked(API.getManagedOnCallRoles).mockResolvedValue(failed('Failed to load on-call roles.'))

    expect(await store.deleteRole('Night', 1)).toEqual({ success: false, message: STALE_ONCALL_ROLE_RELOAD_FAILED_MESSAGE, stale: true })
    expect(STALE_ONCALL_ROLE_RELOAD_FAILED_MESSAGE).not.toContain('has been reloaded')
  })

  it('on 409 returns the server message without reloading', async () => {
    vi.mocked(API.renameManagedOnCallRole).mockResolvedValue({ success: false, message: 'Role \'x\' already exists.', stale: false })

    expect(await store.renameRole('Night', 'x', 1)).toEqual({ success: false, message: 'Role \'x\' already exists.', stale: false })
    expect(API.getManagedOnCallRoles).not.toHaveBeenCalled()
  })

  it('opens the editor on a role or a new one, and closes it back to the table', () => {
    expect(store.editMode).toBe(AdminEditMode.Table)

    store.openRoleEditor(mockRoles[0])
    expect(store.editMode).toBe(AdminEditMode.Edit)
    expect(store.currentRole).toEqual(mockRoles[0])

    store.openRoleEditor(null)
    expect(store.editMode).toBe(AdminEditMode.Create)
    expect(store.currentRole).toBeNull()

    store.closeRoleEditor()
    expect(store.editMode).toBe(AdminEditMode.Table)
    expect(store.currentRole).toBeNull()
  })
})
