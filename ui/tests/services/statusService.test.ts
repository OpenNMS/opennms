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
import { getBusinessServicesStatus, getNodesByAlarms } from '@/services/statusService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn() }
}))

describe('statusService', () => {
  beforeEach(() => vi.clearAllMocks())

  it('filters and orders problems server-side instead of paging first', async () => {
    // without severityFilter/orderBy=severity the server pages at its default
    // limit of 10 (unordered) and problems past the page silently vanish
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { businessservice: [] }})
    await getBusinessServicesStatus()

    const url = vi.mocked(v2.get).mock.calls[0][0] as string
    for (const severity of ['WARNING', 'MINOR', 'MAJOR', 'CRITICAL']) {
      expect(url).toContain(`severityFilter=${severity}`)
    }
    expect(url).toContain('orderBy=severity')
    expect(url).toContain('order=desc')
    expect(url).toMatch(/limit=\d+/)
  })

  it('returns null on failure so panels can show an error, not all-clear', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('500'))
    expect(await getBusinessServicesStatus()).toBeNull()
    expect(await getNodesByAlarms()).toBeNull()
  })

  it('still parses the c3 column pairs from the summary endpoints', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: [['Critical', 2], ['Minor', 1]] })
    expect(await getNodesByAlarms()).toEqual([
      { label: 'Critical', count: 2 },
      { label: 'Minor', count: 1 }
    ])
  })
})
