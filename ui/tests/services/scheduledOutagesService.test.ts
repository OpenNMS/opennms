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
  getOutageApplicability,
  getScheduledOutage,
  getScheduledOutages,
  searchOutageNodes,
  setNotificationMembership,
  setPackageMembership
} from '@/services/scheduledOutagesService'
import { rest, v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
  v2: { get: vi.fn() }
}))

describe('scheduledOutagesService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('unwraps the "outage" array from the list response', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: { outage: [{ name: 'a' }, { name: 'b' }] } })
    const result = await getScheduledOutages()
    expect(result?.map(o => o.name)).toEqual(['a', 'b'])
  })

  it('normalizes a single-element outage list to an array', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: { outage: { name: 'solo' } } })
    const result = await getScheduledOutages()
    expect(result?.map(o => o.name)).toEqual(['solo'])
  })

  it('normalizes collapsed single time/node/interface objects on a single outage', async () => {
    vi.mocked(rest.get).mockResolvedValue({
      data: { name: 'x', type: 'weekly', time: { begins: '00:00:00', ends: '23:59:59', day: 'monday' }, node: { id: 3 } }
    })
    const outage = await getScheduledOutage('x')
    expect(outage?.time).toEqual([{ begins: '00:00:00', ends: '23:59:59', day: 'monday' }])
    expect(outage?.node).toEqual([{ id: 3 }])
    expect(outage?.interface).toEqual([])
  })

  it('reads applies-to without a name for a new outage', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: { notifications: false, pollers: [{ name: 'default', applied: false }] } })
    const appl = await getOutageApplicability()
    expect(rest.get).toHaveBeenCalledWith('/sched-outages/applies-to')
    expect(appl?.pollers).toEqual([{ name: 'default', applied: false }])
    expect(appl?.collectors).toEqual([])
  })

  it('reads applies-to for a named outage', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: { notifications: true } })
    const appl = await getOutageApplicability('Weekend Maint')
    expect(rest.get).toHaveBeenCalledWith('/sched-outages/Weekend%20Maint/applies-to')
    expect(appl?.notifications).toBe(true)
  })

  it('PUTs a package membership when applied, DELETEs when not', async () => {
    await setPackageMembership('pollerd', 'My Outage', 'example1', true)
    expect(rest.put).toHaveBeenCalledWith('/sched-outages/My%20Outage/pollerd/example1')

    await setPackageMembership('threshd', 'My Outage', 'mib2', false)
    expect(rest.delete).toHaveBeenCalledWith('/sched-outages/My%20Outage/threshd/mib2')
  })

  it('routes notification membership to the notifd sub-resource', async () => {
    await setNotificationMembership('nightly', true)
    expect(rest.put).toHaveBeenCalledWith('/sched-outages/nightly/notifd')
    await setNotificationMembership('nightly', false)
    expect(rest.delete).toHaveBeenCalledWith('/sched-outages/nightly/notifd')
  })

  it('builds the node autocomplete query and maps id/label', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { node: [{ id: '7', label: 'core-sw' }] } })
    const nodes = await searchOutageNodes('core')
    expect(v2.get).toHaveBeenCalledWith('/nodes?limit=200&_s=node.label==*core*')
    expect(nodes).toEqual([{ id: 7, label: 'core-sw' }])
  })

  it('strips FIQL/URL metacharacters from the autocomplete query', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { node: [] } })
    await searchOutageNodes('a*b);node.id==1&x=2')
    // metacharacters that could break out of the FIQL term or inject params are removed
    expect(v2.get).toHaveBeenCalledWith('/nodes?limit=200&_s=node.label==*abnode.id1x2*')
  })
})
