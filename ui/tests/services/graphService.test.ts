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
import { getGraphDefinitionsByResourceId } from '@/services/graphService'
import { rest } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn() },
  v2: { get: vi.fn(), post: vi.fn() }
}))

describe('graphService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('encodes the resource id in the graph definitions path', async () => {
    vi.mocked(rest.get).mockResolvedValueOnce({ data: { name: ['ns-dskPercent'] }})
    // a string-indexed resource can carry characters that end or break a URL
    const id = 'node[1].dskIndex[C:\\ #1 100%]'
    const result = await getGraphDefinitionsByResourceId(id)
    expect(rest.get).toHaveBeenCalledWith(`/graphs/for/${encodeURIComponent(id)}`)
    expect(result.name).toEqual(['ns-dskPercent'])
  })

  it('answers an empty list when the lookup fails', async () => {
    vi.mocked(rest.get).mockRejectedValueOnce(new Error('boom'))
    expect((await getGraphDefinitionsByResourceId('node[1].nodeSnmp[]')).name).toEqual([])
  })
})
