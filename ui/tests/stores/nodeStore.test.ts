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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNodeStore } from '@/stores/nodeStore'
import API from '@/services'
import { SnmpInterface } from '@/types'

vi.mock('@/services', () => ({
  default: {
    getSnmpInterfaces: vi.fn()
  }
}))

const createMockSnmpInterface = (id: number, nodeId: number): SnmpInterface => ({
  collect: true,
  collectFlag: 'C',
  collectionUserSpecified: false,
  hasEgressFlows: false,
  hasFlows: false,
  hasIngressFlows: false,
  id,
  ifAdminStatus: 1,
  ifAlias: null,
  ifDescr: null,
  ifIndex: id,
  ifName: null,
  ifOperStatus: 1,
  ifSpeed: 0,
  ifType: 6,
  lastCapsdPoll: 0,
  lastEgressFlow: null,
  lastIngressFlow: null,
  lastSnmpPoll: 0,
  nodeId,
  physAddr: null,
  poll: true
})

describe('useNodeStore', () => {
  let store: ReturnType<typeof useNodeStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useNodeStore()
    vi.clearAllMocks()
  })

  describe('getSnmpInterfacesForNodes', () => {
    it('groups a mocked response containing interfaces for two nodes into nodeToSnmpInterfaceMap', async () => {
      const node1Snmp1 = createMockSnmpInterface(101, 1)
      const node1Snmp2 = createMockSnmpInterface(102, 1)
      const node2Snmp1 = createMockSnmpInterface(201, 2)

      vi.mocked(API.getSnmpInterfaces).mockResolvedValue({
        snmpInterface: [node1Snmp1, node1Snmp2, node2Snmp1],
        totalCount: 3,
        count: 3,
        offset: 0
      })

      await store.getSnmpInterfacesForNodes(['1', '2'])

      expect(store.nodeToSnmpInterfaceMap.get('1')).toEqual([node1Snmp1, node1Snmp2])
      expect(store.nodeToSnmpInterfaceMap.get('2')).toEqual([node2Snmp1])
    })

    it('leaves an empty map when the service returns false', async () => {
      vi.mocked(API.getSnmpInterfaces).mockResolvedValue(false)

      await store.getSnmpInterfacesForNodes(['1', '2'])

      expect(store.nodeToSnmpInterfaceMap.size).toEqual(0)
    })

    it('leaves an empty map when the service returns an empty interface list', async () => {
      vi.mocked(API.getSnmpInterfaces).mockResolvedValue({
        snmpInterface: [],
        totalCount: 0,
        count: 0,
        offset: 0
      })

      await store.getSnmpInterfacesForNodes(['1', '2'])

      expect(store.nodeToSnmpInterfaceMap.size).toEqual(0)
    })

    it('resets the map to empty and does not call the service when nodeIds is empty', async () => {
      await store.getSnmpInterfacesForNodes([])

      expect(API.getSnmpInterfaces).not.toHaveBeenCalled()
      expect(store.nodeToSnmpInterfaceMap.size).toEqual(0)
    })

    it('ignores a stale response that resolves after a newer request (latest request wins)', async () => {
      const staleSnmp = createMockSnmpInterface(101, 1)
      const freshSnmp = createMockSnmpInterface(201, 2)

      let resolveFirst: (value: unknown) => void = () => {}
      const firstResponse = new Promise(resolve => {
        resolveFirst = resolve
      })
      vi.mocked(API.getSnmpInterfaces)
        .mockReturnValueOnce(firstResponse as ReturnType<typeof API.getSnmpInterfaces>)
        .mockResolvedValueOnce({ snmpInterface: [freshSnmp], totalCount: 1, count: 1, offset: 0 })

      const firstCall = store.getSnmpInterfacesForNodes(['1'])
      await store.getSnmpInterfacesForNodes(['2'])

      // The first request's response arrives on the wire after the second's.
      resolveFirst({ snmpInterface: [staleSnmp], totalCount: 1, count: 1, offset: 0 })
      await firstCall

      expect(store.nodeToSnmpInterfaceMap.get('2')).toEqual([freshSnmp])
      expect(store.nodeToSnmpInterfaceMap.has('1')).toBe(false)
    })

    it('does not let an in-flight response overwrite a subsequent empty-ids reset', async () => {
      const staleSnmp = createMockSnmpInterface(101, 1)

      let resolveFirst: (value: unknown) => void = () => {}
      const firstResponse = new Promise(resolve => {
        resolveFirst = resolve
      })
      vi.mocked(API.getSnmpInterfaces).mockReturnValueOnce(firstResponse as ReturnType<typeof API.getSnmpInterfaces>)

      const firstCall = store.getSnmpInterfacesForNodes(['1'])
      await store.getSnmpInterfacesForNodes([])

      resolveFirst({ snmpInterface: [staleSnmp], totalCount: 1, count: 1, offset: 0 })
      await firstCall

      expect(store.nodeToSnmpInterfaceMap.size).toEqual(0)
    })
  })
})
