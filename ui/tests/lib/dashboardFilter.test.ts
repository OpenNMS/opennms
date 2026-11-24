import { describe, expect, it } from 'vitest'
import { filterFiqlClauses, isFilterActive, MAX_NODE_IDS } from '@/lib/dashboardFilter'

const filter = (over: Partial<{ surveillanceCategories: string[]; ipMatch: string | null }> = {}) => ({
  surveillanceCategories: over.surveillanceCategories ?? [],
  ipMatch: over.ipMatch ?? null
})

describe('dashboard filter helper', () => {
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

  it('uses the root id property when the nodes endpoint itself is queried', () => {
    // /api/v2/nodes has no `node` alias: `node.id` is unmapped there and 500s
    expect(filterFiqlClauses(filter(), [1, 2], 'id')).toEqual(['(id==1,id==2)'])
    expect(filterFiqlClauses(filter(), [], 'id')).toEqual(['id==-1'])
  })

  it('caps the node id group at MAX_NODE_IDS', () => {
    const ids = Array.from({ length: MAX_NODE_IDS + 50 }, (_, n) => n + 1)
    const [clause] = filterFiqlClauses(filter(), ids)
    expect(clause.split(',')).toHaveLength(MAX_NODE_IDS)
  })
})
