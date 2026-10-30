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
import { getNodeEnlinkdElements } from '@/services/enlinkdService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn() }
}))

const lldp = { lldpChassisId: '00:11:22:33:44:55', lldpSysName: 'sw1', lldpLastPollTime: '2026-10-05T12:00:00' }

// Answers each element endpoint from a table, keyed by the endpoint's kind.
const answer = (byKind: Record<string, unknown>) => {
  vi.mocked(v2.get).mockImplementation(async (url: string) => {
    const kind = url.split('/')[2]
    const value = byKind[kind]

    if (value instanceof Error) {
      throw value
    }

    return value === undefined ? { status: 204, data: '' } : { status: 200, data: value }
  })
}

describe('enlinkdService.getNodeEnlinkdElements', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('asks the five element endpoints for the node', async () => {
    answer({ bridge_elems: [] })

    await getNodeEnlinkdElements('144')

    expect(vi.mocked(v2.get).mock.calls.map(c => c[0]).sort()).toEqual([
      '/enlinkd/bridge_elems/144',
      '/enlinkd/cdp_elems/144',
      '/enlinkd/isis_elems/144',
      '/enlinkd/lldp_elems/144',
      '/enlinkd/ospf_elems/144'
    ])
  })

  // 204 from an element endpoint is "none found", not a failure.
  it('reports the elements found, and none for each 204', async () => {
    const bridge = { baseBridgeAddress: 'aa', vlan: 1 }
    answer({ lldp_elems: lldp, bridge_elems: [bridge] })

    const result = await getNodeEnlinkdElements('144')

    expect(result.success).toBe(true)
    expect(result.payload).toEqual({ lldp, cdp: undefined, ospf: undefined, isis: undefined, bridges: [bridge] })
  })

  it('reports a node with nothing as a success with no elements', async () => {
    answer({ bridge_elems: [] })

    const result = await getNodeEnlinkdElements('161')

    expect(result.success).toBe(true)
    expect(result.payload).toEqual({ lldp: undefined, cdp: undefined, ospf: undefined, isis: undefined, bridges: [] })
  })

  it('fails if any request fails, keeping what did load', async () => {
    answer({ lldp_elems: lldp, cdp_elems: new Error('boom'), bridge_elems: [] })

    const result = await getNodeEnlinkdElements('144')

    expect(result.success).toBe(false)
    expect(result.payload?.lldp).toEqual(lldp)
  })
})
