import { beforeEach, describe, expect, test, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import { v2 } from '@/services/axiosInstances'
import {
  createJdbcCollection,
  deleteJdbcSource,
  downloadJdbcSource,
  getJdbcSource,
  getJdbcSources,
  updateJdbcCollection,
  uploadJdbcSource
} from '@/services/jdbcDataCollectionService'
import type { JdbcCollection } from '@/types/jdbcDataCollection'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

const httpError = (status: number, data: unknown) => {
  const config = { headers: new AxiosHeaders() }
  return new AxiosError('Request failed', String(status), config, {}, { status, statusText: '', data, headers: {}, config })
}

const collection: JdbcCollection = { name: 'My Collection', rrdStep: 300, rras: ['RRA:AVERAGE:0.5:1:2016'], queries: [] }

describe('jdbcDataCollectionService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => undefined)
  })

  test('lists the sources', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: [{ id: 'default' }] })

    const result = await getJdbcSources()

    expect(v2.get).toHaveBeenCalledWith('/jdbc-datacollection/sources')
    expect(result.success).toBe(true)
    expect(result.payload).toEqual([{ id: 'default' }])
  })

  test('encodes the source id', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { id: 'a b' }})

    await getJdbcSource('a b')

    expect(v2.get).toHaveBeenCalledWith('/jdbc-datacollection/sources/a%20b')
  })

  test('passes the server message and status of a failure through', async () => {
    vi.mocked(v2.get).mockRejectedValue(httpError(404, 'JDBC data collection source \'x\' does not exist.'))

    const result = await getJdbcSource('x')

    expect(result.success).toBe(false)
    expect(result.status).toBe(404)
    expect(result.message).toBe('JDBC data collection source \'x\' does not exist.')
  })

  test('sends the version as If-Match on collection writes', async () => {
    vi.mocked(v2.post).mockResolvedValue({})
    vi.mocked(v2.put).mockResolvedValue({})

    await createJdbcCollection('mysql', collection, 'v1')
    await updateJdbcCollection('mysql', 'Old Name', collection, 'v2')

    expect(v2.post).toHaveBeenCalledWith('/jdbc-datacollection/sources/mysql/collections', collection, { headers: { 'If-Match': '"v1"' }})
    expect(v2.put).toHaveBeenCalledWith('/jdbc-datacollection/sources/mysql/collections/Old%20Name', collection, { headers: { 'If-Match': '"v2"' }})
  })

  test('marks a stale write with its status', async () => {
    vi.mocked(v2.delete).mockRejectedValue(httpError(412, 'changed'))

    const result = await deleteJdbcSource('mysql', 'v1')

    expect(v2.delete).toHaveBeenCalledWith('/jdbc-datacollection/sources/mysql', { headers: { 'If-Match': '"v1"' }})
    expect(result.status).toBe(412)
  })

  test('downloads as a blob in the requested format', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: 'xml' })

    await downloadJdbcSource('mysql', true)

    expect(v2.get).toHaveBeenCalledWith('/jdbc-datacollection/sources/mysql/download', { params: { format: 'xml' }, responseType: 'blob' })
  })

  test('returns null when a download fails', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('down'))

    expect(await downloadJdbcSource('mysql', false)).toBeNull()
  })

  test('uploads a file with the id and replace flag', async () => {
    vi.mocked(v2.post).mockResolvedValue({ data: { id: 'mysql' }})
    const file = new File(['<x/>'], 'mysql.xml')

    const result = await uploadJdbcSource(file, ' mysql ', true)

    const [url, body, options] = vi.mocked(v2.post).mock.calls[0]
    expect(url).toBe('/jdbc-datacollection/upload')
    expect((body as FormData).get('upload')).toBeInstanceOf(File)
    expect(options).toEqual({ params: { replace: true, id: 'mysql' }})
    expect(result.payload).toEqual({ id: 'mysql' })
  })

  test('leaves the id to the server when none is given', async () => {
    vi.mocked(v2.post).mockResolvedValue({ data: { id: 'mysql' }})

    await uploadJdbcSource(new File(['<x/>'], 'mysql.xml'), '', false)

    expect(vi.mocked(v2.post).mock.calls[0][2]).toEqual({ params: { replace: false }})
  })
})
