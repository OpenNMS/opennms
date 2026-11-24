import { describe, it, expect, vi, beforeEach } from 'vitest'
import { rest } from '@/services/axiosInstances'
import { getAvailability } from '@/services/availabilityService'

vi.mock('@/services/axiosInstances', () => ({ rest: { get: vi.fn() }}))

describe('getAvailability', () => {
  beforeEach(() => vi.clearAllMocks())

  it('returns null when the fetch fails, so the panel can show "Waiting for availability data"', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('rtc disconnected'))
    expect(await getAvailability()).toBeNull()
  })

  it('returns [] for a genuinely empty result (204 / no section)', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 204, data: {}} as never)
    expect(await getAvailability()).toEqual([])
  })
})
