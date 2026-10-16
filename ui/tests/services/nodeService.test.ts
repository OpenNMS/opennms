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
import { getNodeAlarmStatus, getNodeAvailabilityPercentage, getNodeCriticalPath, lookupNodeById, resolveNodeId } from '@/services/nodeService'
import { rest, v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn() },
  v2: { get: vi.fn() }
}))

describe('nodeService.getNodeCriticalPath', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('asks v2 for the node\'s critical path', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { criticalPathIp: '10.0.0.3', criticalPathServiceName: 'ICMP' }})

    const result = await getNodeCriticalPath('144')

    expect(v2.get).toHaveBeenCalledWith('/nodes/144/criticalPath')
    expect(result).toEqual(expect.objectContaining({
      success: true,
      payload: { criticalPathIp: '10.0.0.3', criticalPathServiceName: 'ICMP' }
    }))
  })

  // 204: the node has no critical path of its own, which is not a failure.
  it('answers a successful null for a node with none', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 204, data: '' })

    expect(await getNodeCriticalPath('161')).toEqual(expect.objectContaining({ success: true, payload: null }))
  })

  it('answers a failed result when the request fails', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('boom'))

    expect((await getNodeCriticalPath('161')).success).toBe(false)
  })
})

describe('nodeService.getNodeAlarmStatus', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('asks v2 for the node\'s alarm summary', async () => {
    const status = { severity: 'MAJOR', nodeDown: false, interfacesDown: 0, servicesDown: 1, acknowledgedCount: 0, unacknowledgedCount: 1 }
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: status })

    const result = await getNodeAlarmStatus('144')

    expect(v2.get).toHaveBeenCalledWith('/nodes/144/alarmStatus')
    expect(result).toEqual(expect.objectContaining({ success: true, payload: status }))
  })

  it('answers a failed result when the request fails', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('boom'))

    expect((await getNodeAlarmStatus('144')).success).toBe(false)
  })
})

describe('nodeService.getNodeAvailabilityPercentage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(rest.get).mockResolvedValue({ data: { ipinterfaces: [] }})
  })

  it('sends no parameters for the plain rolling roster', async () => {
    await getNodeAvailabilityPercentage('144')

    expect(rest.get).toHaveBeenCalledWith('/availability/nodes/144', undefined)
  })

  it('sends the window and the page', async () => {
    await getNodeAvailabilityPercentage('144', 1000, 2000, { limit: 10, offset: 20 })

    expect(rest.get).toHaveBeenCalledWith('/availability/nodes/144', { params: { start: 1000, end: 2000, limit: 10, offset: 20 }})
  })

  it('asks for monitored interfaces only when told to', async () => {
    await getNodeAvailabilityPercentage('144', 1000, 2000, { limit: 10, offset: 0, withServices: true })

    expect(rest.get).toHaveBeenCalledWith('/availability/nodes/144', {
      params: { start: 1000, end: 2000, limit: 10, offset: 0, withServices: true }
    })
  })

  it('answers false when the request fails', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('boom'))

    expect(await getNodeAvailabilityPercentage('144')).toBe(false)
  })
})

describe('nodeService.resolveNodeId', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('asks v2 for a numeric id', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { id: 42, label: 'n' }})

    const result = await resolveNodeId('42')

    expect(v2.get).toHaveBeenCalledWith('/nodes/42')
    expect(rest.get).not.toHaveBeenCalled()
    expect(result.success).toBe(true)
    expect(result.payload).toBe('42')
  })

  it('answers null for a numeric id that does not exist', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 404 }})

    const result = await resolveNodeId('999')

    expect(result.success).toBe(true)
    expect(result.payload).toBeNull()
  })

  it('fails when the numeric lookup fails', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 500 }})

    const result = await resolveNodeId('42')

    expect(result.success).toBe(false)
  })

  it('resolves a foreign id with a slash through query parameters', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: { node: [{ id: '163' }], totalCount: 1 }})

    const result = await resolveNodeId('slashtest:a/b')

    expect(rest.get).toHaveBeenCalledWith('/nodes', { params: { foreignSource: 'slashtest', foreignId: 'a/b', limit: 1 }})
    expect(v2.get).not.toHaveBeenCalled()
    expect(result.success).toBe(true)
    expect(result.payload).toBe('163')
  })

  it('splits at the first colon only', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: { node: [{ id: 7 }] }})

    const result = await resolveNodeId('FS:a:b')

    expect(rest.get).toHaveBeenCalledWith('/nodes', { params: { foreignSource: 'FS', foreignId: 'a:b', limit: 1 }})
    expect(result.payload).toBe('7')
  })

  it('handles a single node object', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: { node: { id: '12' }}})

    const result = await resolveNodeId('FS:x')

    expect(result.success).toBe(true)
    expect(result.payload).toBe('12')
  })

  it.each([
    ['an empty node array', { status: 200, data: { node: [], totalCount: 0 }}],
    ['a missing node', { status: 200, data: { totalCount: 0 }}],
    ['a 204', { status: 204, data: '' }],
    ['an empty body', { status: 200, data: '' }]
  ])('answers null for %s', async (_name, resp) => {
    vi.mocked(rest.get).mockResolvedValue(resp)

    const result = await resolveNodeId('FS:none')

    expect(result.success).toBe(true)
    expect(result.payload).toBeNull()
  })

  it('fails when the foreign id request fails', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('boom'))

    const result = await resolveNodeId('FS:1')

    expect(result.success).toBe(false)
    expect(result.message).toBe('Failed to look up the node.')
  })
})

describe('nodeService.lookupNodeById', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('answers the node, encoding the id as one path segment', async () => {
    const node = { id: '153', label: 'something-226' }
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: node })

    const result = await lookupNodeById('My Source:a b#c')

    expect(v2.get).toHaveBeenCalledWith('/nodes/My%20Source%3Aa%20b%23c')
    expect(result.success).toBe(true)
    expect(result.payload).toEqual(node)
  })

  it('answers a successful null on a 404', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 404 }})

    const result = await lookupNodeById('42')

    expect(result.success).toBe(true)
    expect(result.payload).toBeNull()
  })

  it('fails on any other error', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 500 }})

    const result = await lookupNodeById('42')

    expect(result.success).toBe(false)
  })
})
