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
import { nodeExistsInRequisition } from '@/services/requisitionService'
import { rest } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn() }
}))

describe('requisitionService.nodeExistsInRequisition', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('asks v1 for the node in its requisition, encoding both parts', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: {}})

    expect(await nodeExistsInRequisition('Demo Stores', 'a/b#1')).toBe(true)
    expect(rest.get).toHaveBeenCalledWith('/requisitions/Demo%20Stores/nodes/a%2Fb%231')
  })

  it('answers false for a 404', async () => {
    vi.mocked(rest.get).mockRejectedValue({ response: { status: 404 }})

    expect(await nodeExistsInRequisition('fs', 'fid')).toBe(false)
  })

  // Not "not in a requisition": the check itself failed.
  it('answers null for any other failure', async () => {
    vi.mocked(rest.get).mockRejectedValue({ response: { status: 500 }})
    expect(await nodeExistsInRequisition('fs', 'fid')).toBeNull()

    vi.mocked(rest.get).mockRejectedValue(new Error('network'))
    expect(await nodeExistsInRequisition('fs', 'fid')).toBeNull()
  })
})
