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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { getNodeOutages, getOutages, outageServiceName } from '@/services/outageService'
import { v2 } from '@/services/axiosInstances'
import { Outage, SORT } from '@/types'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn() }
}))

describe('outageService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getOutages', () => {
    it('queries v2 outages with the given parameters', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { outage: [{ id: 1 }], totalCount: 9 }})

      const resp = await getOutages({ limit: 5, _s: 'node.id==42' })

      expect(v2.get).toHaveBeenCalledWith('/outages?limit=5&_s=node.id==42')
      expect(resp).toEqual({ outage: [{ id: 1 }], totalCount: 9 })
    })

    it('answers an empty result for 204 No Content', async () => {
      vi.mocked(v2.get).mockResolvedValue({ status: 204, data: '' })

      expect(await getOutages()).toEqual({ outage: [], totalCount: 0, count: 0, offset: 0 })
    })

    it('answers false when the request fails', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))

      expect(await getOutages()).toBe(false)
    })
  })

  describe('getNodeOutages', () => {
    // 2026-10-02T12:00:00Z; a week earlier is 2026-09-25T12:00:00Z.
    const NOW = Date.UTC(2026, 9, 2, 12, 0, 0)
    const RECENT = 'perspective==%00;(ifLostService=gt=2026-09-25T12:00:00.000-0000,'
      + 'outage.ifRegainedService==1970-01-01T00:00:00.000-0000)'

    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(NOW)
      vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { outage: [], totalCount: 0 }})
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    const requestedUrl = () => vi.mocked(v2.get).mock.calls[0][0] as string

    // As v1 outages/forNode did: lost in the last week or still open, no perspective outages,
    // newest first.
    it('asks v2 for the node\'s recent outages, newest first, with the caller\'s paging', async () => {
      await getNodeOutages('42', { limit: 5, offset: 10 })

      expect(requestedUrl()).toBe(`/outages?orderBy=id&order=desc&limit=5&offset=10&_s=node.id==42;(${RECENT})`)
    })

    // A + in the query is read back as a space and the date then fails to parse.
    it('writes the window\'s start with a -0000 offset, never + or Z', async () => {
      await getNodeOutages('42')

      expect(requestedUrl()).toContain('ifLostService=gt=2026-09-25T12:00:00.000-0000')
      expect(requestedUrl()).not.toMatch(/\+|\dZ/)
    })

    it('takes a different window', async () => {
      await getNodeOutages('42', undefined, 24 * 60 * 60 * 1000)

      expect(requestedUrl()).toContain('ifLostService=gt=2026-10-01T12:00:00.000-0000')
    })

    it('lets the caller override the sort', async () => {
      await getNodeOutages('42', { orderBy: 'ifLostService', order: SORT.ASCENDING })

      expect(requestedUrl()).toContain('orderBy=ifLostService&order=asc')
    })

    it('applies the caller\'s FIQL within the node\'s recent outages', async () => {
      await getNodeOutages('42', { _s: 'serviceType.name==ICMP,serviceType.name==SNMP' })

      expect(requestedUrl()).toContain(`_s=node.id==42;(${RECENT};(serviceType.name==ICMP,serviceType.name==SNMP))`)
    })

    it('answers false when the request fails', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))

      expect(await getNodeOutages('42')).toBe(false)
    })
  })

  describe('outageServiceName', () => {
    it('is the nested service type\'s name', () => {
      expect(outageServiceName({ monitoredService: { id: 1, serviceType: { id: 3, name: 'SNMP' }}} as Outage)).toBe('SNMP')
    })

    it('falls back to "service" when the outage carries no service type', () => {
      expect(outageServiceName({} as Outage)).toBe('service')
    })
  })
})
