import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import {
  createMonitoringLocation, deleteMonitoringLocation, getNodeCountByLocation, listMonitoringLocations, updateMonitoringLocation
} from '@/services/monitoringLocationAdminService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({ v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }}))

const http = (status: number, data: any = '') => {
  const e = new AxiosError('x')
  e.response = { status, data, statusText: '', headers: {}, config: { headers: new AxiosHeaders() }}
  return e
}

describe('monitoringLocationAdminService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })
  afterEach(() => vi.restoreAllMocks())

  describe('listMonitoringLocations', () => {
    it('fetches a bounded page and reports the total', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: { location: [{ 'location-name': 'Default' }], totalCount: 42 }})
      const result = await listMonitoringLocations()
      expect(result).toEqual({ locations: [{ 'location-name': 'Default' }], totalCount: 42 })
      // bounded, not limit=0
      expect(vi.mocked(v2.get).mock.calls[0][0]).toMatch(/limit=\d+/)
      expect(vi.mocked(v2.get).mock.calls[0][0]).not.toContain('limit=0')
    })

    it('normalizes a single object to an array and defaults the total', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: { location: { 'location-name': 'Default' }}})
      expect(await listMonitoringLocations()).toEqual({ locations: [{ 'location-name': 'Default' }], totalCount: 1 })
    })

    it('returns null (not []) on failure so the store can flag a load error', async () => {
      vi.mocked(v2.get).mockRejectedValue(http(500))
      expect(await listMonitoringLocations()).toBeNull()
    })
  })

  describe('createMonitoringLocation', () => {
    it('returns a success result and keeps a short server message on failure', async () => {
      vi.mocked(v2.post).mockResolvedValueOnce({})
      expect(await createMonitoringLocation({ 'location-name': 'Default' } as any)).toEqual({ success: true, message: '' })
      vi.mocked(v2.post).mockRejectedValueOnce(http(400, 'A location named Default already exists'))
      expect(await createMonitoringLocation({ 'location-name': 'Default' } as any))
        .toEqual({ success: false, message: 'A location named Default already exists' })
    })

    it('falls back to a generic message when the server body is not a short plain string', async () => {
      vi.mocked(v2.post).mockRejectedValueOnce(http(500, '<html>Internal Server Error</html>'))
      const result = await createMonitoringLocation({ 'location-name': 'Raleigh' } as any)
      expect(result.success).toBe(false)
      expect(result.message).toBe('Failed to create monitoring location \'Raleigh\'.')
    })
  })

  describe('updateMonitoringLocation', () => {
    it('reads the current row and patches only the name and description', async () => {
      // the fresh server row carries fields this page never edits; they must survive
      vi.mocked(v2.get).mockResolvedValue({ data: { 'location-name': 'Raleigh', 'monitoring-area': 'old', priority: 50, latitude: 1, longitude: 2, tags: ['keep-me'] }})
      vi.mocked(v2.put).mockResolvedValue({})

      const result = await updateMonitoringLocation({ 'location-name': 'Raleigh', 'monitoring-area': 'new', priority: 100, latitude: 35, longitude: -78 } as any)

      expect(result.success).toBe(true)
      const [path, body] = vi.mocked(v2.put).mock.calls[0]
      expect(path).toBe('/monitoringLocations/Raleigh')
      expect(body).toMatchObject({
        'location-name': 'Raleigh', 'monitoring-area': 'new',
        priority: 50, latitude: 1, longitude: 2, tags: ['keep-me'] // hidden fields round-trip from the fresh read
      })
    })

    it('keeps the server geolocation since the editor no longer exposes it', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: { 'location-name': 'Raleigh', 'monitoring-area': 'a', geolocation: 'server address', priority: 100, latitude: 1, longitude: 2 }})
      vi.mocked(v2.put).mockResolvedValue({})

      await updateMonitoringLocation({ 'location-name': 'Raleigh', 'monitoring-area': 'a', geolocation: 'stale client address', priority: 100, latitude: 1, longitude: 2 } as any)

      const [, body] = vi.mocked(v2.put).mock.calls[0]
      expect((body as any).geolocation).toBe('server address')
    })

    it('returns the scrubbed server message on failure', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: { 'location-name': 'Raleigh' }})
      vi.mocked(v2.put).mockRejectedValue(http(400, 'The ID of the object doesn\'t match the ID of the path'))
      const result = await updateMonitoringLocation({ 'location-name': 'Raleigh' } as any)
      expect(result).toEqual({ success: false, message: 'The ID of the object doesn\'t match the ID of the path' })
    })
  })

  describe('deleteMonitoringLocation', () => {
    it('returns success on 204', async () => {
      vi.mocked(v2.delete).mockResolvedValue({})
      expect(await deleteMonitoringLocation('Raleigh')).toEqual({ success: true, message: '' })
      expect(vi.mocked(v2.delete).mock.calls[0][0]).toBe('/monitoringLocations/Raleigh')
    })

    it('reports a missing location as a failure (the server answers 404)', async () => {
      vi.mocked(v2.delete).mockRejectedValue(http(404))
      expect((await deleteMonitoringLocation('gone')).success).toBe(false)
    })

    it('hides a raw persistence error from a 5xx behind the node-assignment hint', async () => {
      vi.mocked(v2.delete).mockRejectedValue(http(500, 'could not execute statement; constraint [fk_node_location]; nested exception is org.hibernate.exception.ConstraintViolationException'))
      const result = await deleteMonitoringLocation('Branch')
      expect(result.success).toBe(false)
      expect(result.message).not.toContain('hibernate')
      expect(result.message).toContain('no nodes are assigned')
    })

    it('hides an HTML error page and explains the nodes prerequisite instead', async () => {
      vi.mocked(v2.delete).mockRejectedValue(http(500, '<html><body>Internal Server Error</body></html>'))
      const result = await deleteMonitoringLocation('Raleigh')
      expect(result.success).toBe(false)
      expect(result.message).not.toContain('<html>')
      expect(result.message).toBe('Monitoring location \'Raleigh\' could not be deleted. Make sure no nodes are assigned to it.')
    })
  })

  describe('getNodeCountByLocation', () => {
    it('asks for a one-row page filtered by location and reads totalCount', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { totalCount: 17, node: [{ id: 1 }] }})
      expect(await getNodeCountByLocation('Data Center east')).toBe(17)
      const url = vi.mocked(v2.get).mock.calls[0][0] as string
      expect(url).toContain('limit=1')
      expect(decodeURIComponent(url)).toContain('_s=location.locationName==Data Center east')
    })

    it('maps a 204 (no nodes) to zero', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 204 })
      expect(await getNodeCountByLocation('Empty')).toBe(0)
    })

    it('is null on failure, a missing total, or a name that would alter the FIQL query', async () => {
      vi.mocked(v2.get).mockRejectedValueOnce(http(500))
      expect(await getNodeCountByLocation('Raleigh')).toBeNull()
      vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: {}})
      expect(await getNodeCountByLocation('Raleigh')).toBeNull()
      expect(await getNodeCountByLocation('a,b')).toBeNull()
      expect(await getNodeCountByLocation('east (1)')).toBeNull()
      expect(v2.get).toHaveBeenCalledTimes(2)
    })
  })
})
