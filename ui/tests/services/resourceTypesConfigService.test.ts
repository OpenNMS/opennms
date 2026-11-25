import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import {
  createResourceTypesConfig,
  deleteResourceTypesConfig,
  downloadResourceTypesConfig,
  getResourceTypesConfig,
  getResourceTypesConfigs,
  updateResourceTypesConfig,
  uploadResourceTypesConfig
} from '@/services/resourceTypesConfigService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

const axiosError = (status: number, data: unknown) => Object.assign(new Error('Request failed'), {
  isAxiosError: true,
  response: { status, data, headers: { 'content-type': 'text/plain' }}
})

describe('resourceTypesConfigService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  test('lists the configurations', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: [{ id: 'jmx-resource', resourceTypeNames: [] }] })

    const result = await getResourceTypesConfigs()

    expect(v2.get).toHaveBeenCalledWith('/resource-types/configs')
    expect(result.success).toBe(true)
    expect(result.payload).toEqual([{ id: 'jmx-resource', resourceTypeNames: [] }])
  })

  test('encodes the id of a configuration', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { id: 'a b', resourceTypes: [] }})

    await getResourceTypesConfig('a b')

    expect(v2.get).toHaveBeenCalledWith('/resource-types/configs/a%20b')
  })

  test('sends the version as If-Match on update and delete', async () => {
    vi.mocked(v2.put).mockResolvedValue({ status: 204 })
    vi.mocked(v2.delete).mockResolvedValue({ status: 204 })

    await updateResourceTypesConfig('jmx-resource', { id: 'kafka', resourceTypes: [], version: 'v1' })
    await deleteResourceTypesConfig('jmx-resource', 'v2')

    expect(v2.put).toHaveBeenCalledWith('/resource-types/configs/jmx-resource', { id: 'kafka', resourceTypes: [], version: 'v1' },
      { headers: { 'If-Match': '"v1"' }})
    expect(v2.delete).toHaveBeenCalledWith('/resource-types/configs/jmx-resource', { headers: { 'If-Match': '"v2"' }})
  })

  test('passes the server message and status of a failed write on', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    vi.mocked(v2.post).mockRejectedValue(axiosError(409, 'Resource type \'x\' is already defined in configuration \'y\'.'))

    const result = await createResourceTypesConfig({ id: 'new', resourceTypes: [] })

    expect(result.success).toBe(false)
    expect(result.status).toBe(409)
    expect(result.message).toBe('Resource type \'x\' is already defined in configuration \'y\'.')
    expect(consoleError).toHaveBeenCalled()
  })

  test('downloads and uploads per configuration', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: new Blob() })
    vi.mocked(v2.post).mockResolvedValue({ status: 200 })

    await downloadResourceTypesConfig('jmx-resource', true)
    const result = await uploadResourceTypesConfig('jmx-resource', new File(['<x/>'], 'jmx.xml'), true)

    expect(v2.get).toHaveBeenCalledWith('/resource-types/configs/jmx-resource/download', { params: { format: 'xml' }, responseType: 'blob' })
    expect(v2.post).toHaveBeenCalledWith('/resource-types/configs/jmx-resource/upload/xml', expect.any(FormData))
    expect(result.success).toBe(true)
  })
})
