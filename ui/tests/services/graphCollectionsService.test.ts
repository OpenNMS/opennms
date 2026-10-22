///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  addGraphToCollection,
  deleteGraphCollection,
  getGraphCollection,
  getGraphCollections,
  getResolvedGraphCollection,
  getResolvedSyntheticGraphCollection,
  graphCollectionProblems,
  isStaleWrite,
  updateGraphCollection
} from '@/services/graphCollectionsService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

const collection = { id: 3, title: 'Core', showTimespanButton: true, showGraphtypeButton: false, graphsPerLine: 2, graphs: [] }

describe('graphCollectionsService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('returns the list as a payload, and the failure as a message', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ data: [collection] })
    expect(await getGraphCollections()).toEqual({ success: true, message: '', payload: [collection] })
    expect(v2.get).toHaveBeenLastCalledWith('/graph-collections')

    vi.mocked(v2.get).mockRejectedValueOnce({ response: { status: 500, data: '' }})
    const failed = await getGraphCollections()
    expect(failed.success).toBe(false)
    expect(failed.message).toBe('Failed to load the graph collections.')
    expect(failed.payload).toBeUndefined()
  })

  it('reads one collection together with its ETag', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ data: collection, headers: { etag: '"abc"' }})
    const result = await getGraphCollection(3)
    expect(result.payload).toEqual({ collection, etag: '"abc"' })
    expect(v2.get).toHaveBeenLastCalledWith('/graph-collections/3')
  })

  it('passes only the overrides that are set to the resolved view', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { ...collection, type: 'custom', graphTypes: [] }, headers: {}})
    await getResolvedGraphCollection(3, { timespan: '1_day', graphtype: null })
    expect(v2.get).toHaveBeenLastCalledWith('/graph-collections/3/resolved', { params: { timespan: '1_day' }})

    await getResolvedSyntheticGraphCollection('nodeSource', 'fs:id/with slash', {})
    expect(v2.get).toHaveBeenLastCalledWith('/graph-collections/resolved/nodeSource/fs%3Aid%2Fwith%20slash', { params: {}})
    const result = await getResolvedGraphCollection(3)
    expect(result.payload?.etag).toBeNull()
  })

  it('sends If-Match on writes only when an ETag is given', async () => {
    vi.mocked(v2.put).mockResolvedValue({ data: collection, headers: { etag: '"new"' }})
    const saved = await updateGraphCollection(3, collection, '"old"')
    expect(v2.put).toHaveBeenLastCalledWith('/graph-collections/3', collection, { headers: { 'If-Match': '"old"' }})
    expect(saved.payload?.etag).toBe('"new"')

    await updateGraphCollection(3, collection)
    expect(v2.put).toHaveBeenLastCalledWith('/graph-collections/3', collection, {})

    vi.mocked(v2.delete).mockResolvedValue({})
    await deleteGraphCollection(3, '"old"')
    expect(v2.delete).toHaveBeenLastCalledWith('/graph-collections/3', { headers: { 'If-Match': '"old"' }})
  })

  it('adds a graph at the end or at an index', async () => {
    vi.mocked(v2.post).mockResolvedValue({ data: collection, headers: {}})
    const graph = { title: 'g', resourceId: 'node[1].nodeSnmp[]', graphtype: 'mib2.bits', timespan: '7_day' }
    await addGraphToCollection(3, graph)
    expect(v2.post).toHaveBeenLastCalledWith('/graph-collections/3/graphs', graph, { params: {}})
    await addGraphToCollection(3, graph, { index: 0, etag: '"e"' })
    expect(v2.post).toHaveBeenLastCalledWith('/graph-collections/3/graphs', graph, { headers: { 'If-Match': '"e"' }, params: { index: 0 }})
  })

  it('reports every validation problem of a 400, one per line', async () => {
    // the header the API sends, charset included
    const err = { response: { status: 400, data: 'title is required.\nGraph 2: unknown timespan \'fortnight\'.\n', headers: { 'content-type': 'text/plain;charset=UTF-8' }}}
    vi.mocked(v2.put).mockRejectedValueOnce(err)
    const result = await updateGraphCollection(3, collection)
    expect(result.success).toBe(false)
    expect(result.message).toBe('title is required.\nGraph 2: unknown timespan \'fortnight\'.')
    expect(result.errors).toEqual(['title is required.', 'Graph 2: unknown timespan \'fortnight\'.'])
    expect(graphCollectionProblems(err)).toHaveLength(2)
    expect(graphCollectionProblems({ response: { status: 500, data: 'boom' }})).toEqual([])
  })

  it('uses a plain-text body as the message but not an HTML error page', async () => {
    const html = { response: { status: 502, data: '<html><body><h1>Bad Gateway</h1></body></html>', headers: { 'content-type': 'text/html' }}}
    vi.mocked(v2.put).mockRejectedValueOnce(html)
    expect((await updateGraphCollection(3, collection)).message).toBe('Failed to save graph collection 3.')

    const plain = { response: { status: 404, data: 'Graph collection 3 was not found.', headers: { 'content-type': 'text/plain;charset=UTF-8' }}}
    vi.mocked(v2.put).mockRejectedValueOnce(plain)
    expect((await updateGraphCollection(3, collection)).message).toBe('Graph collection 3 was not found.')

    // a string body under the JSON type, as the API sent before it declared its error bodies
    const typedJson = { response: { status: 404, data: 'Graph collection 3 was not found.', headers: { 'content-type': 'application/json;charset=UTF-8' }}}
    vi.mocked(v2.put).mockRejectedValueOnce(typedJson)
    expect((await updateGraphCollection(3, collection)).message).toBe('Graph collection 3 was not found.')
    expect(graphCollectionProblems({ response: { status: 400, data: 'Graph 1: unknown timespan \'fortnight\'.\nGraph 2: a graphtype is required.', headers: { 'content-type': 'application/json;charset=UTF-8' }}}))
      .toEqual(['Graph 1: unknown timespan \'fortnight\'.', 'Graph 2: a graphtype is required.'])

    // a 400 whose body is a page, not the problem list
    expect(graphCollectionProblems({ response: { status: 400, data: '<!DOCTYPE html><html></html>', headers: {}}})).toEqual([])
  })

  it('words a 403 itself and recognises a stale write', async () => {
    vi.mocked(v2.delete).mockRejectedValueOnce({ response: { status: 403, data: '' }})
    expect((await deleteGraphCollection(3)).message).toBe('You do not have permission to change graph collections.')

    const stale = { response: { status: 412, data: 'Graph collection 3 has changed since it was read; fetch it again before writing.' }}
    vi.mocked(v2.delete).mockRejectedValueOnce(stale)
    const result = await deleteGraphCollection(3, '"old"')
    expect(result.message).toContain('has changed since it was read')
    expect(isStaleWrite(stale)).toBe(true)
    expect(isStaleWrite({ response: { status: 404 }})).toBe(false)
  })
})
