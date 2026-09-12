import { describe, it, expect, vi, beforeEach } from 'vitest'
import { rest } from '@/services/axiosInstances'
import { getAvailability, staleness } from '@/services/availabilityService'

vi.mock('@/services/axiosInstances', () => ({ rest: { get: vi.fn() }}))

describe('getAvailability', () => {
  beforeEach(() => vi.clearAllMocks())

  it('returns null when the fetch fails, so the panel can show "Waiting for availability data"', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('fetch failed'))
    expect(await getAvailability()).toBeNull()
  })

  it('returns [] for a genuinely empty result (204 / no section)', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 204, data: {}} as never)
    expect(await getAvailability()).toEqual([])
  })

  it('parses the stale flag and last-updated of each category', async () => {
    vi.mocked(rest.get).mockResolvedValue({
      status: 200,
      data: { section: { name: 'Total', categories: { category: [
        { name: 'Fresh', 'last-updated': 1700000000000, stale: false },
        { name: 'Old', 'last-updated': '1600000000000', stale: 'true' },
        { name: 'Never' }
      ] }}}
    } as never)
    const [section] = (await getAvailability())!
    expect(section.categories.map(c => [c.name, c.lastUpdated, c.stale])).toEqual([
      ['Fresh', 1700000000000, false],
      ['Old', 1600000000000, true],
      ['Never', null, false]
    ])
  })
})

describe('staleness', () => {
  const cat = (name: string, stale: boolean, lastUpdated: number | null) => ({
    name, outageText: '', availabilityText: '', availability: 100, availabilityClass: 'Normal', outageClass: 'Normal', stale, lastUpdated
  })

  it('is not stale when no category is', () => {
    expect(staleness([{ name: 'S', categories: [cat('A', false, 1), cat('B', false, null)] }])).toEqual({ stale: false, oldest: null })
  })

  it('reports the oldest snapshot among the stale categories only', () => {
    const sections = [
      { name: 'S1', categories: [cat('A', false, 5), cat('B', true, 300)] },
      { name: 'S2', categories: [cat('C', true, 200), cat('D', true, null)] }
    ]
    expect(staleness(sections)).toEqual({ stale: true, oldest: 200 })
  })

  it('is stale even when no stale category carries a timestamp', () => {
    expect(staleness([{ name: 'S', categories: [cat('A', true, null)] }])).toEqual({ stale: true, oldest: null })
  })
})
