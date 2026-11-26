import { AxiosError, AxiosHeaders } from 'axios'
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import {
  deleteXmlGroupFile,
  getXmlCollection,
  getXmlCollections,
  updateXmlCollection,
  updateXmlGroupFile,
  uploadXmlCollectionXml
} from '@/services/xmlCollectionConfigService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

const httpError = (status: number, data: string) =>
  new AxiosError('Request failed', 'ERR_BAD_REQUEST', undefined, undefined, {
    status,
    statusText: '',
    data,
    headers: {},
    config: { headers: new AxiosHeaders() }
  })

describe('xmlCollectionConfigService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  test('returns the payload of a read', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: [{ name: 'c' }] })

    const result = await getXmlCollections()

    expect(v2.get).toHaveBeenCalledWith('/xml-collection/collections')
    expect(result.success).toBe(true)
    expect(result.payload).toEqual([{ name: 'c' }])
  })

  test('encodes names in paths', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: {}})

    await getXmlCollection('a/b c')

    expect(v2.get).toHaveBeenCalledWith('/xml-collection/collections/a%2Fb%20c')
  })

  test('sends the version as If-Match', async () => {
    vi.mocked(v2.put).mockResolvedValue({})

    await updateXmlCollection('old', { name: 'new', rrd: { step: 300, rras: [] }, sources: [], version: 'abc' })
    await updateXmlGroupFile({ id: 'f', groups: [], version: 'def' })

    expect(v2.put).toHaveBeenNthCalledWith(1, '/xml-collection/collections/old', expect.objectContaining({ name: 'new' }), { headers: { 'If-Match': '"abc"' }})
    expect(v2.put).toHaveBeenNthCalledWith(2, '/xml-collection/groups/f', expect.anything(), { headers: { 'If-Match': '"def"' }})
  })

  test('passes the server message and status of a failure through', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    vi.mocked(v2.delete).mockRejectedValue(httpError(409, 'Group file \'f\' is imported by collections: c.'))

    const result = await deleteXmlGroupFile('f', 'v1')

    expect(result.success).toBe(false)
    expect(result.message).toBe('Group file \'f\' is imported by collections: c.')
    expect(result.status).toBe(409)
    expect(consoleError).toHaveBeenCalled()
  })

  test('uploads an XML file with an optional id', async () => {
    vi.mocked(v2.post).mockResolvedValue({ data: { type: 'groupFile', id: 'mine' }})
    const file = new File(['<xml-groups/>'], 'x.xml')

    const result = await uploadXmlCollectionXml(file, 'mine')

    const form = vi.mocked(v2.post).mock.calls[0][1] as FormData
    expect(vi.mocked(v2.post).mock.calls[0][0]).toBe('/xml-collection/upload/xml')
    expect(form.get('id')).toBe('mine')
    expect(result.payload).toEqual({ type: 'groupFile', id: 'mine' })
  })
})
