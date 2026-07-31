import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { buildFilterClauses, filterFiqlClauses, isFilterActive, resolveFilterNodeIds } from '@/components/Dashboard/filter'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({ v2: { get: vi.fn() } }))

const filter = (over: Partial<{ surveillanceCategories: string[]; ipMatch: string | null }> = {}) => ({
  surveillanceCategories: over.surveillanceCategories ?? [],
  ipMatch: over.ipMatch ?? null
})

describe('dashboard filter helper', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('isFilterActive reflects categories or IP', () => {
    expect(isFilterActive(filter())).toBe(false)
    expect(isFilterActive(filter({ ipMatch: '10.0.0.1' }))).toBe(true)
    expect(isFilterActive(filter({ surveillanceCategories: ['Routers'] }))).toBe(true)
  })

  it('emits an IP clause and no node group when only IP is set', () => {
    expect(filterFiqlClauses(filter({ ipMatch: '10.1.*.*' }), null)).toEqual(['ipInterface.ipAddress==10.1.*.*'])
  })

  it('groups the resolved node ids as an OR set', () => {
    expect(filterFiqlClauses(filter(), [1, 2, 3])).toEqual(['(node.id==1,node.id==2,node.id==3)'])
  })

  it('matches nothing when a category resolves to zero nodes', () => {
    expect(filterFiqlClauses(filter(), [])).toEqual(['node.id==-1'])
  })

  it('resolveFilterNodeIds unions members across categories and caches', async () => {
    vi.mocked(v2.get)
      .mockResolvedValueOnce({ data: { node: [{ id: 1 }, { id: 2 }] } })
      .mockResolvedValueOnce({ data: { node: [{ id: 2 }, { id: 3 }] } })

    const ids = await resolveFilterNodeIds(filter({ surveillanceCategories: ['B', 'A'] }))
    expect([...(ids ?? [])].sort()).toEqual([1, 2, 3])

    // second call for the same set is served from cache (no new request)
    const before = vi.mocked(v2.get).mock.calls.length
    await resolveFilterNodeIds(filter({ surveillanceCategories: ['A', 'B'] }))
    expect(vi.mocked(v2.get).mock.calls.length).toBe(before)
  })

  it('resolveFilterNodeIds returns null with no categories', async () => {
    expect(await resolveFilterNodeIds(filter({ ipMatch: '10.0.0.1' }))).toBeNull()
  })

  it('buildFilterClauses combines IP and resolved nodes', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { node: [{ id: 7 }] } })
    const clauses = await buildFilterClauses(filter({ surveillanceCategories: ['X'], ipMatch: '10.0.0.5' }))
    expect(clauses).toContain('ipInterface.ipAddress==10.0.0.5')
    expect(clauses).toContain('(node.id==7)')
  })
})
