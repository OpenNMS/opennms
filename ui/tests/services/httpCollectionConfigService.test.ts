import { beforeEach, describe, expect, test, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import {
  createHttpCollection,
  deleteHttpCollection,
  getHttpCollection,
  updateHttpCollection,
  updateHttpCollectionSettings,
  uploadHttpCollectionConfiguration
} from '@/services/httpCollectionConfigService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

const axiosError = (status: number, data: unknown) => {
  const config = { headers: new AxiosHeaders() }
  return new AxiosError('Request failed', String(status), config, {}, { status, statusText: '', headers: {}, config, data })
}

describe('httpCollectionConfigService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => undefined)
  })

  test('encodes the collection name in the path', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { name: 'a b#1' }})

    const result = await getHttpCollection('a b#1')

    expect(v2.get).toHaveBeenCalledWith('/httpcollection/collections/a%20b%231', undefined)
    expect(result.payload?.name).toBe('a b#1')
  })

  test.each(['a/b', 'a\\b', 'CPU 90%', '.', '..', 'item'])('addresses %s, which a path segment cannot carry, by query', async (name) => {
    vi.mocked(v2.get).mockResolvedValue({ data: { name }})
    vi.mocked(v2.put).mockResolvedValue({})
    vi.mocked(v2.delete).mockResolvedValue({})
    const collection = { name, rrd: { step: 300, rras: [] }, uris: [], version: 'abc' }

    await getHttpCollection(name)
    await updateHttpCollection(name, collection)
    await deleteHttpCollection(name, 'abc')

    expect(v2.get).toHaveBeenCalledWith('/httpcollection/collections/item', { params: { name }})
    expect(v2.put).toHaveBeenCalledWith(
      '/httpcollection/collections/item',
      { name, rrd: { step: 300, rras: [] }, uris: [] },
      { params: { name }, headers: { 'If-Match': '"abc"' }}
    )
    expect(v2.delete).toHaveBeenCalledWith('/httpcollection/collections/item', { params: { name }, headers: { 'If-Match': '"abc"' }})
  })

  test('sends the version as If-Match and not in the body', async () => {
    vi.mocked(v2.put).mockResolvedValue({})

    await updateHttpCollection('doc', { name: 'doc', rrd: { step: 300, rras: [] }, uris: [], version: 'abc' })

    expect(v2.put).toHaveBeenCalledWith(
      '/httpcollection/collections/doc',
      { name: 'doc', rrd: { step: 300, rras: [] }, uris: [] },
      { headers: { 'If-Match': '"abc"' }}
    )
  })

  test('sends no If-Match without a version', async () => {
    vi.mocked(v2.delete).mockResolvedValue({})
    vi.mocked(v2.post).mockResolvedValue({})
    vi.mocked(v2.put).mockResolvedValue({})

    await deleteHttpCollection('doc')
    await createHttpCollection({ name: 'doc', rrd: { step: 300, rras: [] }, uris: [], version: 'x' })
    await updateHttpCollectionSettings({ rrdRepository: '/rrd' })

    expect(v2.delete).toHaveBeenCalledWith('/httpcollection/collections/doc', undefined)
    expect(v2.post).toHaveBeenCalledWith('/httpcollection/collections', { name: 'doc', rrd: { step: 300, rras: [] }, uris: [] })
    expect(v2.put).toHaveBeenCalledWith('/httpcollection/config', { rrdRepository: '/rrd' }, undefined)
  })

  test('passes the server message and status of a failure through', async () => {
    vi.mocked(v2.put).mockRejectedValue(axiosError(412, 'HTTP collection \'doc\' has changed since it was read.'))

    const result = await updateHttpCollection('doc', { name: 'doc', rrd: { step: 300, rras: [] }, uris: [] })

    expect(result.success).toBe(false)
    expect(result.status).toBe(412)
    expect(result.message).toBe('HTTP collection \'doc\' has changed since it was read.')
  })

  test('uploads xml to its own endpoint', async () => {
    vi.mocked(v2.post).mockResolvedValue({})

    await uploadHttpCollectionConfiguration(new File(['<x/>'], 'http.xml'), true)

    expect(vi.mocked(v2.post).mock.calls[0][0]).toBe('/httpcollection/upload/xml')
  })
})
