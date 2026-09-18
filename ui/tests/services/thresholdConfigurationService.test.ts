import { beforeEach, describe, expect, test, vi } from 'vitest'
import {
  createThresholdGroup,
  createThreshdPackage,
  deleteThresholdGroup,
  deleteThreshdPackage,
  downloadThresholdingConfiguration,
  getThresholdGroup,
  getThresholdGroups,
  getThresholdingMetadata,
  getThreshdConfiguration,
  getThreshdPackages,
  reloadThresholdingConfiguration,
  updateThresholdGroup,
  updateThreshdPackage,
  uploadThresholdingConfiguration
} from '@/services/thresholdConfigurationService'
import { v2 } from '@/services/axiosInstances'
import type { ThresholdGroup, ThreshdPackage } from '@/types/thresholdConfig'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

const group = (overrides: Partial<ThresholdGroup> = {}): ThresholdGroup => ({
  name: 'mib2',
  rrdRepository: '/rrd',
  thresholds: [],
  expressions: [],
  ...overrides
})

const threshdPackage = (overrides: Partial<ThreshdPackage> = {}): ThreshdPackage => ({
  name: 'example1',
  filter: 'IPADDR != \'0.0.0.0\'',
  specifics: [],
  includeRanges: [],
  excludeRanges: [],
  includeUrls: [],
  services: [],
  outageCalendars: [],
  ...overrides
})

describe('thresholdConfigurationService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('reads', () => {
    test('returns the payload of a successful read', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: [{ name: 'mib2' }] })

      const result = await getThresholdGroups()

      expect(v2.get).toHaveBeenCalledWith('/thresholding/groups')
      expect(result.success).toBe(true)
      expect(result.payload).toEqual([{ name: 'mib2' }])
    })

    test('treats a body-less response as an empty list rather than undefined', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: undefined })

      const result = await getThresholdGroups()

      expect(result.payload).toEqual([])
    })

    test('encodes a name before putting it in the path', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: {}})

      await getThresholdGroup('my group/with slash')

      expect(v2.get).toHaveBeenCalledWith('/thresholding/groups/my%20group%2Fwith%20slash')
    })

    test('reads metadata and the threshd configuration from their own endpoints', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: {}})

      await getThresholdingMetadata()
      expect(v2.get).toHaveBeenCalledWith('/thresholding/metadata')

      await getThreshdConfiguration()
      expect(v2.get).toHaveBeenCalledWith('/threshd/config')

      await getThreshdPackages()
      expect(v2.get).toHaveBeenCalledWith('/threshd/packages')
    })
  })

  describe('writes', () => {
    test('posts a new group without an If-Match header', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 201 })

      const result = await createThresholdGroup(group())

      expect(v2.post).toHaveBeenCalledWith('/thresholding/groups', group())
      expect(result.success).toBe(true)
    })

    test('sends the version back as a quoted If-Match on an update', async () => {
      vi.mocked(v2.put).mockResolvedValue({ status: 204 })

      const payload = group({ version: 'abc123' })
      await updateThresholdGroup('mib2', payload)

      expect(v2.put).toHaveBeenCalledWith('/thresholding/groups/mib2', payload, {
        headers: { 'If-Match': '"abc123"' }
      })
    })

    test('omits If-Match entirely when no version is known', async () => {
      vi.mocked(v2.put).mockResolvedValue({ status: 204 })

      await updateThresholdGroup('mib2', group())

      expect(v2.put).toHaveBeenCalledWith('/thresholding/groups/mib2', group(), undefined)
    })

    test('passes the version through on delete too', async () => {
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      await deleteThresholdGroup('mib2', 'abc123')

      expect(v2.delete).toHaveBeenCalledWith('/thresholding/groups/mib2', {
        headers: { 'If-Match': '"abc123"' }
      })
    })

    test('writes threshd packages to their own endpoints', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 201 })
      vi.mocked(v2.put).mockResolvedValue({ status: 204 })
      vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

      await createThreshdPackage(threshdPackage())
      expect(v2.post).toHaveBeenCalledWith('/threshd/packages', threshdPackage())

      await updateThreshdPackage('example1', threshdPackage())
      expect(v2.put).toHaveBeenCalledWith('/threshd/packages/example1', threshdPackage(), undefined)

      await deleteThreshdPackage('example1')
      expect(v2.delete).toHaveBeenCalledWith('/threshd/packages/example1', undefined)
    })

    test('posts a reload with no body', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 202 })

      const result = await reloadThresholdingConfiguration()

      expect(v2.post).toHaveBeenCalledWith('/thresholding/reload')
      expect(result.success).toBe(true)
    })

    test('uploads to the xml endpoint when the file is xml', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 200 })

      await uploadThresholdingConfiguration(new File(['<x/>'], 'thresholds.xml'), true)
      expect(v2.post).toHaveBeenCalledWith('/thresholding/upload/xml', expect.any(FormData))

      await uploadThresholdingConfiguration(new File(['{}'], 'thresholds.json'), false)
      expect(v2.post).toHaveBeenCalledWith('/thresholding/upload', expect.any(FormData))
    })
  })

  describe('failures', () => {
    test('surfaces the server message verbatim instead of throwing', async () => {
      // The server explains exactly which rule failed; flattening that to a generic message would make the
      // 400 undiagnosable from the UI.
      const axiosError = Object.assign(new Error('Request failed'), {
        isAxiosError: true,
        response: { status: 400, data: 'ds-name \'aVeryLongDatasource\' is longer than 19 characters.' }
      })
      vi.mocked(v2.post).mockRejectedValue(axiosError)

      const result = await createThresholdGroup(group())

      expect(result.success).toBe(false)
      expect(result.message).toContain('longer than 19 characters')
    })

    test('falls back to a descriptive message for a non-axios failure', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error(''))

      const result = await getThresholdGroup('mib2')

      expect(result.success).toBe(false)
      expect(result.message).toContain('Failed to retrieve threshold group \'mib2\'.')
      expect(result.payload).toBeUndefined()
    })

    test('returns false from a failed download rather than rejecting', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))

      await expect(downloadThresholdingConfiguration(true)).resolves.toBe(false)
    })

    test('requests the right format on download', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: new Blob() })

      await downloadThresholdingConfiguration(true)

      expect(v2.get).toHaveBeenCalledWith('/thresholding/download', {
        params: { format: 'xml' },
        responseType: 'blob'
      })
    })
  })
})
