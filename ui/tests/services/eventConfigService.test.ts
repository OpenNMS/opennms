import {
  filterEventConfigEvents,
  filterEventConfigSources
} from '@/services/eventConfigService'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mockGet = vi.hoisted(() => vi.fn())
vi.mock('@/services/axiosInstances', () => ({
  v2: { get: mockGet, post: vi.fn(), put: vi.fn(), patch: vi.fn(), delete: vi.fn() },
  rest: { get: vi.fn(), post: vi.fn() },
  restFile: { post: vi.fn() }
}))

describe('eventConfigService filters', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('maps a 200 sources page', async () => {
    mockGet.mockResolvedValue({
      status: 200,
      data: { totalRecords: 1, eventConfSourceList: [{ id: 7, name: 'Cisco.events' }] }
    })
    const result = await filterEventConfigSources(0, 10, '', 'evaluationOrder', 'asc')
    expect(result.totalRecords).toBe(1)
    expect(result.sources).toHaveLength(1)
    expect(result.sources[0].name).toBe('Cisco.events')
  })

  // the server answers 204 when nothing matches; the previous rows must not survive as "results"
  it('treats a 204 sources response as an empty page, not an error', async () => {
    mockGet.mockResolvedValue({ status: 204, data: '' })
    const result = await filterEventConfigSources(0, 10, 'nothing-matches', 'name', 'asc')
    expect(result).toEqual({ sources: [], totalRecords: 0 })
  })

  it('treats a 204 events response as an empty page, not an error', async () => {
    mockGet.mockResolvedValue({ status: 204, data: '' })
    const result = await filterEventConfigEvents(7, 0, 10, 'nothing-matches', 'eventOrder', 'asc')
    expect(result).toEqual({ events: [], totalRecords: 0 })
  })

  it('still rejects an unexpected status', async () => {
    mockGet.mockResolvedValue({ status: 500, data: '' })
    await expect(filterEventConfigSources(0, 10, '', 'name', 'asc')).rejects.toThrow('Unexpected response status: 500')
  })
})
