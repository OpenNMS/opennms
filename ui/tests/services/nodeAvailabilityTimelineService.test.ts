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

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn() }
}))

import { v2 } from '@/services/axiosInstances'
import { getNodeOutageTimeline } from '@/services/nodeAvailabilityTimelineService'

const get = vi.mocked(v2.get)

const doc = {
  nodeId: 1,
  start: 1000,
  end: 2000,
  nodeCreateTime: 500,
  count: 1,
  outage: [{
    id: 9, ifServiceId: 3, ipInterfaceId: 1, ipAddress: '10.0.0.1',
    serviceId: 2, serviceName: 'ICMP', ifLostService: 1200, ifRegainedService: null
  }]
}

describe('getNodeOutageTimeline', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('requests the window as epoch millisecond query parameters', async () => {
    get.mockResolvedValue({ status: 200, data: doc } as any)

    await getNodeOutageTimeline(1, 1000, 2000)

    expect(get).toHaveBeenCalledWith('/outages/timeline/1', { params: { start: 1000, end: 2000 }})
  })

  it('returns the document as sent', async () => {
    get.mockResolvedValue({ status: 200, data: doc } as any)

    const result = await getNodeOutageTimeline(1, 1000, 2000)

    expect(result?.outage).toHaveLength(1)
    expect(result?.outage[0].ifRegainedService).toBeNull()
    expect(result?.nodeCreateTime).toBe(500)
  })

  it('substitutes an empty list when the document omits one', async () => {
    get.mockResolvedValue({ status: 200, data: { ...doc, outage: undefined }} as any)

    expect((await getNodeOutageTimeline(1, 1000, 2000))?.outage).toEqual([])
  })

  // null rather than an empty document: an all-green node legitimately has no outages, so the
  // panel has to be able to tell "nothing was down" from "the request failed".
  it('returns null when the request fails', async () => {
    get.mockRejectedValue(new Error('boom'))

    expect(await getNodeOutageTimeline(1, 1000, 2000)).toBeNull()
  })

  it('returns null when the response has no body', async () => {
    get.mockResolvedValue({ status: 204, data: undefined } as any)

    expect(await getNodeOutageTimeline(1, 1000, 2000)).toBeNull()
  })
})
