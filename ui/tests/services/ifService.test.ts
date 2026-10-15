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
import { getNodeServicesByName } from '@/services/ifService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn() },
  v2: { get: vi.fn() }
}))

describe('ifService.getNodeServicesByName', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('asks v2 for the node\'s services of those types, all of them, in one query', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { service: [] }})

    await getNodeServicesByName('152', ['SSH', 'HTTP'])

    expect(v2.get).toHaveBeenCalledWith('/ifservices?limit=0&_s=node.id==152;(serviceType.name==SSH,serviceType.name==HTTP)')
  })

  it('returns each service\'s name and address', async () => {
    vi.mocked(v2.get).mockResolvedValue({
      status: 200,
      data: { service: [{ ipAddress: '10.0.0.1', serviceType: { id: 4, name: 'SSH' }, status: 'A' }] }
    })

    const result = await getNodeServicesByName('152', ['SSH'])

    expect(result).toEqual(expect.objectContaining({ success: true, payload: [{ serviceName: 'SSH', ipAddress: '10.0.0.1' }] }))
  })

  it('answers an empty list for 204', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 204, data: '' })

    expect((await getNodeServicesByName('152', ['SSH'])).payload).toEqual([])
  })

  it('answers a failed result when the request fails', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('boom'))

    expect((await getNodeServicesByName('152', ['SSH'])).success).toBe(false)
  })
})
