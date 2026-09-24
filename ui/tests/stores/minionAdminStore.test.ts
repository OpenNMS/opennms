import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import API from '@/services'
import { Minion } from '@/types/minionAdmin'

vi.mock('@/services', () => ({
  default: { listMinions: vi.fn(), updateMinion: vi.fn(), deleteMinion: vi.fn(), getMinionNodeIds: vi.fn(), getCoreVersion: vi.fn() }
}))

const minion = (id: string, location = 'Default'): Minion => ({ id, label: id, location, type: 'Minion', status: 'UP', version: '1.0', properties: {}})
const listResult = (minions: Minion[], totalCount = minions.length) => ({ minions, totalCount })
const ok = { success: true, message: '' }
const failed = (message: string) => ({ success: false, message })

describe('useMinionAdminStore', () => {
  let store: ReturnType<typeof useMinionAdminStore>
  beforeEach(() => {
    setActivePinia(createPinia())
    store = useMinionAdminStore()
    vi.clearAllMocks()
    vi.mocked(API.getMinionNodeIds).mockResolvedValue({})
  })

  it('starts empty', () => {
    expect(store.minions).toEqual([])
  })

  it('getMinions loads on success and preserves on failure', async () => {
    vi.mocked(API.listMinions).mockResolvedValue(listResult([minion('m1')]))
    expect(await store.getMinions()).toBe(true)
    expect(store.minions).toEqual([minion('m1')])
    vi.mocked(API.listMinions).mockResolvedValue(null)
    expect(await store.getMinions()).toBe(false)
    expect(store.minions).toEqual([minion('m1')])
    expect(store.loadError).toBe(true)
  })

  it('flags truncation when the server had more rows than fetched', async () => {
    vi.mocked(API.listMinions).mockResolvedValue(listResult([minion('m1')], 9))
    await store.getMinions()
    expect(store.truncated).toBe(true)
  })

  it('maps a minion to its node id by id+location for the ID link', async () => {
    vi.mocked(API.listMinions).mockResolvedValue(listResult([minion('m1', 'RemoteA')]))
    vi.mocked(API.getMinionNodeIds).mockResolvedValue({ 'm1\u0000RemoteA': 42 })
    await store.getMinions()
    expect(store.nodeIdFor(minion('m1', 'RemoteA'))).toBe(42)
    expect(store.nodeIdFor(minion('m1', 'Default'))).toBeUndefined()
  })

  it('updateMinion refreshes on success, not on failure', async () => {
    const edit = { id: 'm1', label: 'm1', location: 'Default', properties: {}}
    vi.mocked(API.updateMinion).mockResolvedValue(ok)
    vi.mocked(API.listMinions).mockResolvedValue(listResult([minion('m1')]))
    expect((await store.updateMinion(edit)).success).toBe(true)
    expect(API.listMinions).toHaveBeenCalledTimes(1)
    vi.clearAllMocks()
    vi.mocked(API.updateMinion).mockResolvedValue(failed('boom'))
    expect(await store.updateMinion(edit)).toEqual({ success: false, message: 'boom' })
    expect(API.listMinions).not.toHaveBeenCalled()
  })

  it('deleteMinion refreshes on success', async () => {
    vi.mocked(API.deleteMinion).mockResolvedValue(ok)
    vi.mocked(API.listMinions).mockResolvedValue(listResult([]))
    expect((await store.deleteMinion('m1')).success).toBe(true)
    expect(store.minions).toEqual([])
  })

  it('deleteMinion bubbles the failure result without refreshing', async () => {
    vi.mocked(API.deleteMinion).mockResolvedValue(failed('nope'))
    expect(await store.deleteMinion('m1')).toEqual({ success: false, message: 'nope' })
    expect(API.listMinions).not.toHaveBeenCalled()
  })

  it('getCoreVersion stores the core version, null when it is unavailable', async () => {
    vi.mocked(API.getCoreVersion).mockResolvedValue('34.0.0')
    expect(await store.getCoreVersion()).toBe('34.0.0')
    expect(store.coreVersion).toBe('34.0.0')
    vi.mocked(API.getCoreVersion).mockResolvedValue(null)
    expect(await store.getCoreVersion()).toBeNull()
    expect(store.coreVersion).toBeNull()
  })

  it('byLocation groups the loaded minions by location name', async () => {
    vi.mocked(API.listMinions).mockResolvedValue(listResult([minion('m1'), minion('m2', 'RemoteA'), minion('m3', 'RemoteA'), { ...minion('m4'), location: null }]))
    await store.getMinions()
    expect(Object.keys(store.byLocation).sort()).toEqual(['', 'Default', 'RemoteA'])
    expect(store.byLocation.RemoteA.map(m => m.id)).toEqual(['m2', 'm3'])
    expect(store.byLocation.Default.map(m => m.id)).toEqual(['m1'])
  })
})
