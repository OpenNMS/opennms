import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { queryTopn } from '@/services/topnService'
import { rest } from '@/services/axiosInstances'
import { TimeframePreset } from '@/types/dashboard'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn() }
}))

const tf = { preset: TimeframePreset.Last24h, from: null, to: null }

// two nodes each carrying the ICMP response-time attribute
const resourceTree = {
  data: {
    resource: [
      { id: 'node[1]', label: 'node-A', children: { resource: [{ id: 'node[1].responseTime[10.0.0.1]', rrdGraphAttributes: { icmp: {} } }] } },
      { id: 'node[2]', label: 'node-B', children: { resource: [{ id: 'node[2].responseTime[10.0.0.2]', rrdGraphAttributes: { icmp: {} } }] } }
    ]
  }
}

describe('queryTopn column-to-source mapping', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(rest.get).mockResolvedValue(resourceTree)
  })
  afterEach(() => vi.restoreAllMocks())

  it('attributes each measurement to the source its label encodes, not its position', async () => {
    // backend returns columns in HASH order: s1 (node-B) first, s0 (node-A) second
    vi.mocked(rest.post).mockResolvedValue({
      data: {
        labels: ['s1', 's0'],
        columns: [
          { values: [200000, 200000] }, // s1 => node-B => 200ms
          { values: [50000, 50000] }     // s0 => node-A => 50ms
        ]
      }
    })

    const rows = await queryTopn('response-time', tf, 5, 'desc')

    const byLabel = Object.fromEntries(rows.map((r) => [r.label, Math.round(r.value)]))
    expect(byLabel['node-A']).toBe(50)
    expect(byLabel['node-B']).toBe(200)
  })

  it('ranks descending and honors n', async () => {
    vi.mocked(rest.post).mockResolvedValue({
      data: { labels: ['s0', 's1'], columns: [{ values: [50000] }, { values: [200000] }] }
    })
    const rows = await queryTopn('response-time', tf, 1, 'desc')
    expect(rows).toHaveLength(1)
    expect(rows[0].label).toBe('node-B')
  })

  it('returns empty when there are no sources', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: { resource: [] } })
    expect(await queryTopn('response-time', tf, 5, 'desc')).toEqual([])
  })
})
