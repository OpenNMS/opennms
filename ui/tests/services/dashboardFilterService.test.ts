import { beforeEach, describe, expect, it, vi } from 'vitest'
import { buildFilterClauses, resolveFilterNodeIds, resolveFilterToNodeIdSet } from '@/services/dashboardFilterService'
import { getNodeIdsByIpAddress, getNodeIdsInCategory } from '@/services/nodeService'

vi.mock('@/services/nodeService', () => ({
  getNodeIdsInCategory: vi.fn(),
  getNodeIdsByIpAddress: vi.fn()
}))

const filter = (over: Partial<{ surveillanceCategories: string[]; ipMatch: string | null }> = {}) => ({
  surveillanceCategories: over.surveillanceCategories ?? [],
  ipMatch: over.ipMatch ?? null
})

// the per-session cache is module state, so each test uses its own category names
describe('dashboardFilterService', () => {
  beforeEach(() => vi.resetAllMocks())

  it('resolveFilterNodeIds unions members across categories and caches', async () => {
    vi.mocked(getNodeIdsInCategory).mockImplementation(async (c: string) => (c === 'A' ? [1, 2] : [2, 3]))

    const ids = await resolveFilterNodeIds(filter({ surveillanceCategories: ['B', 'A'] }))
    expect([...(ids ?? [])].sort()).toEqual([1, 2, 3])
    expect(getNodeIdsInCategory).toHaveBeenCalledWith('A')
    expect(getNodeIdsInCategory).toHaveBeenCalledWith('B')

    // second call for the same set is served from cache (no new lookups)
    await resolveFilterNodeIds(filter({ surveillanceCategories: ['A', 'B'] }))
    expect(getNodeIdsInCategory).toHaveBeenCalledTimes(2)
  })

  it('resolveFilterNodeIds returns null with no categories', async () => {
    expect(await resolveFilterNodeIds(filter({ ipMatch: '10.0.0.1' }))).toBeNull()
  })

  it('evicts the cache after a failed category lookup so the next call retries', async () => {
    vi.mocked(getNodeIdsInCategory).mockRejectedValueOnce(new Error('boom'))
    expect(await resolveFilterNodeIds(filter({ surveillanceCategories: ['Flaky'] }))).toEqual([])

    vi.mocked(getNodeIdsInCategory).mockResolvedValueOnce([9])
    expect(await resolveFilterNodeIds(filter({ surveillanceCategories: ['Flaky'] }))).toEqual([9])
    expect(getNodeIdsInCategory).toHaveBeenCalledTimes(2)
  })

  it('buildFilterClauses combines IP and resolved nodes', async () => {
    vi.mocked(getNodeIdsInCategory).mockResolvedValue([7])
    const clauses = await buildFilterClauses(filter({ surveillanceCategories: ['X'], ipMatch: '10.0.0.5' }))
    expect(clauses).toContain('ipInterface.ipAddress==10.0.0.5')
    expect(clauses).toContain('(node.id==7)')
  })

  it('resolveFilterToNodeIdSet is null with no active filter', async () => {
    expect(await resolveFilterToNodeIdSet(filter())).toBeNull()
  })

  it('resolveFilterToNodeIdSet intersects categories with the IP match', async () => {
    vi.mocked(getNodeIdsInCategory).mockResolvedValue([1, 2, 3])
    vi.mocked(getNodeIdsByIpAddress).mockResolvedValue([2, 3, 4])
    const set = await resolveFilterToNodeIdSet(filter({ surveillanceCategories: ['Both'], ipMatch: ' 10.0.0.1 ' }))
    expect([...(set ?? [])].sort()).toEqual([2, 3])
    expect(getNodeIdsByIpAddress).toHaveBeenCalledWith('10.0.0.1')
  })

  it('resolveFilterToNodeIdSet fails closed when the IP lookup fails', async () => {
    vi.mocked(getNodeIdsByIpAddress).mockRejectedValue(new Error('boom'))
    expect(await resolveFilterToNodeIdSet(filter({ ipMatch: '10.0.0.1' }))).toEqual(new Set())
  })
})
