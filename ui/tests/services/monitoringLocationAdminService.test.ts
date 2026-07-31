import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import {
  createMonitoringLocation, deleteMonitoringLocation, listMonitoringLocations, updateMonitoringLocation
} from '@/services/monitoringLocationAdminService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({ v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() } }))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar: vi.fn() }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

const http = (status: number, data: any = '') => {
  const e = new AxiosError('x')
  e.response = { status, data, statusText: '', headers: {}, config: { headers: new AxiosHeaders() } }
  return e
}

describe('monitoringLocationAdminService', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('listMonitoringLocations fetches a bounded page and reports the total', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { location: [{ 'location-name': 'Default' }], totalCount: 42 } })
    const result = await listMonitoringLocations()
    expect(result).toEqual({ locations: [{ 'location-name': 'Default' }], totalCount: 42 })
    // bounded, not limit=0
    expect(vi.mocked(v2.get).mock.calls[0][0]).toMatch(/limit=\d+/)
    expect(vi.mocked(v2.get).mock.calls[0][0]).not.toContain('limit=0')
  })

  it('listMonitoringLocations returns null on failure so the store can flag an error', async () => {
    vi.mocked(v2.get).mockRejectedValue(http(500))
    expect(await listMonitoringLocations()).toBeNull()
  })

  it('createMonitoringLocation returns the scrubbed error message on failure', async () => {
    vi.mocked(v2.post).mockRejectedValue(http(400, 'A location named Default already exists'))
    const msg = await createMonitoringLocation({ 'location-name': 'Default' } as any)
    expect(msg).toContain('already exists')
  })

  it('updateMonitoringLocation reads the current row and patches only the editable fields', async () => {
    // the fresh server row carries a tag this page never edits; it must survive
    vi.mocked(v2.get).mockResolvedValue({ data: { 'location-name': 'Raleigh', 'monitoring-area': 'old', priority: 50, latitude: 1, longitude: 2, tags: ['keep-me'] } })
    vi.mocked(v2.put).mockResolvedValue({})

    await updateMonitoringLocation({ 'location-name': 'Raleigh', 'monitoring-area': 'new', priority: 100, latitude: 35, longitude: -78 } as any)

    const [, body] = vi.mocked(v2.put).mock.calls[0]
    expect(body).toMatchObject({
      'location-name': 'Raleigh', 'monitoring-area': 'new', priority: 100, latitude: 35, longitude: -78,
      tags: ['keep-me'] // untouched field round-trips from the fresh read
    })
  })

  it('updateMonitoringLocation applies an edited geolocation (regression: it was dropped)', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { 'location-name': 'Raleigh', 'monitoring-area': 'a', geolocation: 'old address', priority: 100, latitude: 1, longitude: 2 } })
    vi.mocked(v2.put).mockResolvedValue({})

    await updateMonitoringLocation({ 'location-name': 'Raleigh', 'monitoring-area': 'a', geolocation: '123 New St', priority: 100, latitude: 1, longitude: 2 } as any)

    const [, body] = vi.mocked(v2.put).mock.calls[0]
    expect((body as any).geolocation).toBe('123 New St')
  })

  it('deleteMonitoringLocation treats a 404 (already deleted) as success', async () => {
    vi.mocked(v2.delete).mockRejectedValue(http(404))
    expect(await deleteMonitoringLocation('gone')).toBeNull()
  })

  it('does not surface an HTML error page verbatim', async () => {
    vi.mocked(v2.delete).mockRejectedValue(http(500, '<html><body>Internal Server Error</body></html>'))
    const msg = await deleteMonitoringLocation('Raleigh')
    expect(msg).not.toContain('<html>')
    expect(msg).toContain('Raleigh')
  })
})
