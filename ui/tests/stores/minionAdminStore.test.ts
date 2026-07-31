import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import API from '@/services'
import { Minion } from '@/types/minionAdmin'

vi.mock('@/services', () => ({
  default: { listMinions: vi.fn(), updateMinion: vi.fn(), deleteMinion: vi.fn() }
}))

const minion = (id: string): Minion => ({ id, label: id, location: 'Default', type: 'Minion', status: 'UP', version: '1.0', properties: {} })

describe('useMinionAdminStore', () => {
  let store: ReturnType<typeof useMinionAdminStore>
  beforeEach(() => {
    setActivePinia(createPinia())
    store = useMinionAdminStore()
    vi.clearAllMocks()
  })

  it('starts empty', () => {
    expect(store.minions).toEqual([])
  })

  it('getMinions loads on success and preserves on failure', async () => {
    vi.mocked(API.listMinions).mockResolvedValue([minion('m1')])
    await store.getMinions()
    expect(store.minions).toEqual([minion('m1')])
    vi.mocked(API.listMinions).mockResolvedValue(null)
    await store.getMinions()
    expect(store.minions).toEqual([minion('m1')])
    expect(store.loadError).toBe(true)
  })

  it('updateMinion refreshes on success, not on failure', async () => {
    const edit = { id: 'm1', label: 'm1', location: 'Default', properties: {} }
    vi.mocked(API.updateMinion).mockResolvedValue(null)
    vi.mocked(API.listMinions).mockResolvedValue([minion('m1')])
    expect(await store.updateMinion(edit)).toBeNull()
    expect(API.listMinions).toHaveBeenCalledTimes(1)
    vi.clearAllMocks()
    vi.mocked(API.updateMinion).mockResolvedValue('boom')
    expect(await store.updateMinion(edit)).toBe('boom')
    expect(API.listMinions).not.toHaveBeenCalled()
  })

  it('deleteMinion refreshes on success', async () => {
    vi.mocked(API.deleteMinion).mockResolvedValue(null)
    vi.mocked(API.listMinions).mockResolvedValue([])
    await store.deleteMinion('m1')
    expect(store.minions).toEqual([])
  })
})
