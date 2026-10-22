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
import { getNodeAlarmStatus, getNodeAvailabilityPercentage, getNodeCriticalPath } from '@/services/nodeService'
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
