import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import API from '@/services'
import { OnCallRole } from '@/types/onCallRoleAdmin'
import { formatScheduleTimestamp } from '@/types/onCallRoleAdmin'

vi.mock('@/services', () => ({
  default: {
    listOnCallRoles: vi.fn(),
    getOnCallRole: vi.fn(),
    getOnCallCalendar: vi.fn(),
    getOnCallSupervisorCandidates: vi.fn(),
    getOnCallGroupCandidates: vi.fn(),
    createOnCallRole: vi.fn(),
    updateOnCallRole: vi.fn(),
    renameOnCallRole: vi.fn(),
    deleteOnCallRole: vi.fn()
  }
}))

describe('useOnCallRoleAdminStore', () => {
  let store: ReturnType<typeof useOnCallRoleAdminStore>

  const mockRoles: OnCallRole[] = [
    { name: 'NOC-Duty', 'membership-group': 'NOC', supervisor: 'admin', 'currently-on-call': ['first'] }
  ]

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
    expect(store.supervisorCandidates).toEqual([])
    expect(store.groupMembers).toEqual({})
  })

  it('populate should load roles and picker data', async () => {
    vi.mocked(API.listOnCallRoles).mockResolvedValue(mockRoles)
    vi.mocked(API.getOnCallSupervisorCandidates).mockResolvedValue(['admin', 'first'])
    vi.mocked(API.getOnCallGroupCandidates).mockResolvedValue({ NOC: ['first'] })

    await store.populate()

    expect(store.roles).toEqual(mockRoles)
    expect(store.supervisorCandidates).toEqual(['admin', 'first'])
    expect(store.groupMembers).toEqual({ NOC: ['first'] })
  })

  it('a failed refresh should keep the previous role list', async () => {
    vi.mocked(API.listOnCallRoles).mockResolvedValue(mockRoles)
    await store.getRoles()
    expect(store.roles).toEqual(mockRoles)

    vi.mocked(API.listOnCallRoles).mockResolvedValue(null)
    await store.getRoles()
    expect(store.roles).toEqual(mockRoles)
  })

  it('createRole should refresh on success and not on failure', async () => {
    vi.mocked(API.createOnCallRole).mockResolvedValue(null)
    vi.mocked(API.listOnCallRoles).mockResolvedValue(mockRoles)
    expect(await store.createRole({ name: 'NOC-Duty' })).toBe(null)
    expect(API.listOnCallRoles).toHaveBeenCalledTimes(1)

    vi.clearAllMocks()
    vi.mocked(API.createOnCallRole).mockResolvedValue('it failed')
    expect(await store.createRole({ name: 'NOC-Duty' })).toBe('it failed')
    expect(API.listOnCallRoles).not.toHaveBeenCalled()
  })

  it('updateRole should pass the payload through and refresh', async () => {
    vi.mocked(API.updateOnCallRole).mockResolvedValue(null)
    vi.mocked(API.listOnCallRoles).mockResolvedValue(mockRoles)

    const payload: OnCallRole = { name: 'NOC-Duty', schedule: [{ user: 'first', type: 'specific', time: [{ begins: 'a', ends: 'b' }] }] }
    await store.updateRole(payload)

    expect(API.updateOnCallRole).toHaveBeenCalledWith(payload)
    expect(API.listOnCallRoles).toHaveBeenCalledTimes(1)
  })

  it('renameRole should pass old and new names and refresh', async () => {
    vi.mocked(API.renameOnCallRole).mockResolvedValue(null)
    vi.mocked(API.listOnCallRoles).mockResolvedValue(mockRoles)

    await store.renameRole('NOC-Duty', 'NOC-Rota')

    expect(API.renameOnCallRole).toHaveBeenCalledWith('NOC-Duty', 'NOC-Rota')
  })

  it('deleteRole should refresh on success', async () => {
    vi.mocked(API.deleteOnCallRole).mockResolvedValue(null)
    vi.mocked(API.listOnCallRoles).mockResolvedValue([])

    await store.deleteRole('NOC-Duty')

    expect(store.roles).toEqual([])
  })

  it('getCalendar should pass parameters through', async () => {
    const calendar = { role: 'NOC-Duty', year: 2093, month: 6, day: [] }
    vi.mocked(API.getOnCallCalendar).mockResolvedValue(calendar)

    expect(await store.getCalendar('NOC-Duty', 2093, 6)).toEqual(calendar)
    expect(API.getOnCallCalendar).toHaveBeenCalledWith('NOC-Duty', 2093, 6)
  })
})

describe('formatScheduleTimestamp', () => {
  it('formats with English month abbreviations regardless of locale', () => {
    expect(formatScheduleTimestamp(new Date(2093, 5, 15, 8, 0, 0))).toBe('15-Jun-2093 08:00:00')
    expect(formatScheduleTimestamp(new Date(2093, 11, 1, 17, 30, 5))).toBe('01-Dec-2093 17:30:05')
  })

  it('expresses the instant on the server zone wall clock when a zone is given', () => {
    // 07:00Z is 09:00 in Berlin (summer) and 16:00 in Tokyo; the day rolls over
    // for zones ahead of UTC near midnight
    const instant = new Date(Date.UTC(2093, 5, 15, 7, 0, 0))
    expect(formatScheduleTimestamp(instant, 'UTC')).toBe('15-Jun-2093 07:00:00')
    expect(formatScheduleTimestamp(instant, 'Europe/Berlin')).toBe('15-Jun-2093 09:00:00')
    expect(formatScheduleTimestamp(instant, 'Asia/Tokyo')).toBe('15-Jun-2093 16:00:00')
    expect(formatScheduleTimestamp(new Date(Date.UTC(2093, 11, 31, 23, 30, 0)), 'Asia/Tokyo')).toBe('01-Jan-2094 08:30:00')
  })

  it('falls back to the browser wall clock for an unknown zone id', () => {
    const local = new Date(2093, 5, 15, 8, 0, 0)
    expect(formatScheduleTimestamp(local, 'Not/AZone')).toBe('15-Jun-2093 08:00:00')
  })
})
