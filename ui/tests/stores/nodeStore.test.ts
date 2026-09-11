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
import { IpInterface, Node, SnmpInterface } from '@/types'

vi.mock('@/services', () => ({
  default: {
    getSnmpInterfaces: vi.fn(),
    getIpInterfaces: vi.fn(),
    getNodeOutages: vi.fn(),
    getNodeById: vi.fn(),
    getNodeIpInterfaces: vi.fn(),
    getNodeSnmpInterfaces: vi.fn(),
    getNodeAvailabilityPercentage: vi.fn()
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

const createMockIpInterface = (id: string, nodeId: number, ipAddress: string): IpInterface => ({
  id,
  ipAddress,
  isManaged: 'M',
  ifIndex: '1',
  isDown: false,
  lastCapsdPoll: 0,
  lastEgressFlow: null,
  lastIngressFlow: null,
  monitoredServiceCount: 0,
  nodeId,
  snmpInterface: undefined,
  snmpPrimary: '',
  hostName: ''
} as unknown as IpInterface)

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
      const firstResponse = new Promise((resolve) => {
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
      const firstResponse = new Promise((resolve) => {
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

  // NMS-20125 PR review (B1): getIpInterfacesForNodes now REPLACES nodeToIpInterfaceMap
  // wholesale (mirroring getSnmpInterfacesForNodes) instead of mutating it in place, and is
  // sequenced the same way, so these tests mirror the getSnmpInterfacesForNodes suite above.
  describe('getIpInterfacesForNodes', () => {
    it('groups a mocked response containing interfaces for two nodes into nodeToIpInterfaceMap', async () => {
      const node1Ip1 = createMockIpInterface('101', 1, '10.0.0.1')
      const node1Ip2 = createMockIpInterface('102', 1, '10.0.0.2')
      const node2Ip1 = createMockIpInterface('201', 2, '10.0.1.1')

      vi.mocked(API.getIpInterfaces).mockResolvedValue({
        ipInterface: [node1Ip1, node1Ip2, node2Ip1],
        totalCount: 3,
        count: 3,
        offset: 0
      })

      await store.getIpInterfacesForNodes(['1', '2'], false)

      expect(store.nodeToIpInterfaceMap.get('1')).toEqual([node1Ip1, node1Ip2])
      expect(store.nodeToIpInterfaceMap.get('2')).toEqual([node2Ip1])
    })

    it('leaves an empty map when the service returns false', async () => {
      vi.mocked(API.getIpInterfaces).mockResolvedValue(false)

      await store.getIpInterfacesForNodes(['1', '2'], false)

      expect(store.nodeToIpInterfaceMap.size).toEqual(0)
    })

    it('leaves an empty map when the service returns an empty interface list', async () => {
      vi.mocked(API.getIpInterfaces).mockResolvedValue({
        ipInterface: [],
        totalCount: 0,
        count: 0,
        offset: 0
      })

      await store.getIpInterfacesForNodes(['1', '2'], false)

      expect(store.nodeToIpInterfaceMap.size).toEqual(0)
    })

    it('resets the map to empty and does not call the service when nodeIds is empty', async () => {
      await store.getIpInterfacesForNodes([], false)

      expect(API.getIpInterfaces).not.toHaveBeenCalled()
      expect(store.nodeToIpInterfaceMap.size).toEqual(0)
    })

    it('ignores a stale response that resolves after a newer request (latest request wins)', async () => {
      const staleIp = createMockIpInterface('101', 1, '10.0.0.1')
      const freshIp = createMockIpInterface('201', 2, '10.0.1.1')

      let resolveFirst: (value: unknown) => void = () => {}
      const firstResponse = new Promise((resolve) => {
        resolveFirst = resolve
      })
      vi.mocked(API.getIpInterfaces)
        .mockReturnValueOnce(firstResponse as ReturnType<typeof API.getIpInterfaces>)
        .mockResolvedValueOnce({ ipInterface: [freshIp], totalCount: 1, count: 1, offset: 0 })

      const firstCall = store.getIpInterfacesForNodes(['1'], false)
      await store.getIpInterfacesForNodes(['2'], false)

      // The first request's response arrives on the wire after the second's.
      resolveFirst({ ipInterface: [staleIp], totalCount: 1, count: 1, offset: 0 })
      await firstCall

      expect(store.nodeToIpInterfaceMap.get('2')).toEqual([freshIp])
      expect(store.nodeToIpInterfaceMap.has('1')).toBe(false)
    })

    it('does not let an in-flight response overwrite a subsequent empty-ids reset', async () => {
      const staleIp = createMockIpInterface('101', 1, '10.0.0.1')

      let resolveFirst: (value: unknown) => void = () => {}
      const firstResponse = new Promise((resolve) => {
        resolveFirst = resolve
      })
      vi.mocked(API.getIpInterfaces).mockReturnValueOnce(firstResponse as ReturnType<typeof API.getIpInterfaces>)

      const firstCall = store.getIpInterfacesForNodes(['1'], false)
      await store.getIpInterfacesForNodes([], false)

      resolveFirst({ ipInterface: [staleIp], totalCount: 1, count: 1, offset: 0 })
      await firstCall

      expect(store.nodeToIpInterfaceMap.size).toEqual(0)
    })
  })
})

describe('nodeStore getNodeById', () => {
  const node = { id: '42', label: 'srv-42' } as unknown as Node

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('publishes the fetched node and marks it loaded', async () => {
    vi.mocked(API.getNodeById).mockResolvedValue(node)
    const store = useNodeStore()

    await store.getNodeById({ id: '42' } as Node)

    expect(store.node).toEqual(node)
    expect(store.nodeLoaded).toBe(true)
  })

  // The store is global and outlives the page. Without a reset, a second node's page renders
  // the first node's label and ids until the new fetch lands.
  it('clears the previous node before fetching another', async () => {
    vi.mocked(API.getNodeById).mockResolvedValue(node)
    const store = useNodeStore()
    await store.getNodeById({ id: '42' } as Node)

    let nodeDuringFetch: unknown
    let loadedDuringFetch: unknown
    vi.mocked(API.getNodeById).mockImplementation(async () => {
      nodeDuringFetch = { ...store.node }
      loadedDuringFetch = store.nodeLoaded
      return { id: '99', label: 'srv-99' } as unknown as Node
    })

    await store.getNodeById({ id: '99' } as Node)

    expect(nodeDuringFetch).toEqual({})
    expect(loadedDuringFetch).toBe(false)
    expect(store.node).toEqual({ id: '99', label: 'srv-99' })
  })

  // A failed fetch must not leave the previous node's data on screen, nor claim to be loaded.
  it('leaves no node loaded when the request fails, and records the failure', async () => {
    vi.mocked(API.getNodeById).mockResolvedValue(node)
    const store = useNodeStore()
    await store.getNodeById({ id: '42' } as Node)

    vi.mocked(API.getNodeById).mockResolvedValue(false as never)
    await store.getNodeById({ id: '99' } as Node)

    expect(store.node).toEqual({})
    expect(store.nodeLoaded).toBe(false)
    expect(store.nodeLoadFailed).toBe(true)
  })

  // The interface tables fetch by node id on their own, and only replace their rows on a
  // successful response -- so a 404 for the node would otherwise leave the previous node's
  // interfaces sitting under the new id.
  it('clears the interfaces when the request fails', async () => {
    vi.mocked(API.getNodeById).mockResolvedValue(false as never)
    const store = useNodeStore()
    store.ipInterfaces = [{ id: 'ip1' }] as never
    store.ipInterfacesTotalCount = 1
    store.snmpInterfaces = [{ id: 5 }] as never
    store.snmpInterfacesTotalCount = 1

    await store.getNodeById({ id: '99' } as Node)

    expect(store.ipInterfaces).toEqual([])
    expect(store.ipInterfacesTotalCount).toBe(0)
    expect(store.snmpInterfaces).toEqual([])
    expect(store.snmpInterfacesTotalCount).toBe(0)
  })

  // Everything this store holds is scoped to the node that failed to load, so none of it
  // should stay on screen under an id that has no node. Events belong to their own store and
  // are cleared by the page.
  it('clears the outages and availability when the request fails', async () => {
    vi.mocked(API.getNodeById).mockResolvedValue(false as never)
    const store = useNodeStore()
    store.outages = [{ id: 1 }] as never
    store.outagesTotalCount = 1
    store.availability = { availability: 98.7 } as never

    await store.getNodeById({ id: '99' } as Node)

    expect(store.outages).toEqual([])
    expect(store.outagesTotalCount).toBe(0)
    expect(store.availability).toEqual({})
  })

  it('clears the failure flag when a later fetch succeeds', async () => {
    vi.mocked(API.getNodeById).mockResolvedValue(false as never)
    const store = useNodeStore()
    await store.getNodeById({ id: '99' } as Node)

    vi.mocked(API.getNodeById).mockResolvedValue(node)
    await store.getNodeById({ id: '42' } as Node)

    expect(store.nodeLoadFailed).toBe(false)
    expect(store.nodeLoaded).toBe(true)
  })
})

describe('nodeStore getNodeSnmpPrimaryInterface', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // The node payload carries no interfaces, and the IP Interfaces table only holds its current
  // page, so the address the Update SNMP action needs is asked for directly.
  it('asks for the node SNMP-primary interface and keeps its address', async () => {
    vi.mocked(API.getNodeIpInterfaces).mockResolvedValue({
      ipInterface: [{ id: 'ip2', ipAddress: '10.0.0.44', snmpPrimary: 'P' }],
      totalCount: 1, count: 1, offset: 0
    } as never)
    const store = useNodeStore()

    await store.getNodeSnmpPrimaryInterface('42')

    expect(API.getNodeIpInterfaces).toHaveBeenCalledWith('42', {
      limit: 1,
      offset: 0,
      _s: 'snmpPrimary==P'
    })
    expect(store.snmpPrimaryIpAddress).toBe('10.0.0.44')
  })

  it('holds no address for a node with no SNMP-primary interface', async () => {
    vi.mocked(API.getNodeIpInterfaces).mockResolvedValue({
      ipInterface: [], totalCount: 0, count: 0, offset: 0
    } as never)
    const store = useNodeStore()
    store.snmpPrimaryIpAddress = '10.0.0.1'

    await store.getNodeSnmpPrimaryInterface('42')

    expect(store.snmpPrimaryIpAddress).toBeUndefined()
  })

  it('holds no address when the request fails', async () => {
    vi.mocked(API.getNodeIpInterfaces).mockResolvedValue(false as never)
    const store = useNodeStore()
    store.snmpPrimaryIpAddress = '10.0.0.1'

    await store.getNodeSnmpPrimaryInterface('42')

    expect(store.snmpPrimaryIpAddress).toBeUndefined()
  })
})

const deferred = <T>() => {
  let resolve: (value: T) => void = () => undefined
  const promise = new Promise<T>((r) => {
    resolve = r
  })

  return { promise, resolve }
}

// Moving between nodes fires these off per node id; the responses can come back in any order.
describe('nodeStore stale node responses', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('discards a node response a newer request has superseded', async () => {
    const first = deferred<unknown>()
    const second = deferred<unknown>()
    vi.mocked(API.getNodeById)
      .mockImplementationOnce(() => first.promise as never)
      .mockImplementationOnce(() => second.promise as never)
    const store = useNodeStore()

    const firstCall = store.getNodeById({ id: '1' } as Node)
    const secondCall = store.getNodeById({ id: '2' } as Node)

    second.resolve({ id: '2', label: 'node-2' })
    await secondCall
    first.resolve({ id: '1', label: 'node-1' })
    await firstCall

    expect(store.node).toEqual({ id: '2', label: 'node-2' })
    expect(store.nodeLoaded).toBe(true)
  })

  // The failure path wipes every panel's data, so a superseded failure must not fire at all.
  it('discards a superseded failure rather than clearing the node that did load', async () => {
    const first = deferred<unknown>()
    const second = deferred<unknown>()
    vi.mocked(API.getNodeById)
      .mockImplementationOnce(() => first.promise as never)
      .mockImplementationOnce(() => second.promise as never)
    const store = useNodeStore()
    store.outages = [{ id: 7 }] as never

    const firstCall = store.getNodeById({ id: '1' } as Node)
    const secondCall = store.getNodeById({ id: '2' } as Node)

    second.resolve({ id: '2', label: 'node-2' })
    await secondCall
    first.resolve(false)
    await firstCall

    expect(store.node).toEqual({ id: '2', label: 'node-2' })
    expect(store.nodeLoadFailed).toBe(false)
    expect(store.outages).toEqual([{ id: 7 }])
  })

  // A stale address here builds admin/updateSnmp.jsp for an address that is not the node's.
  it('discards an SNMP-primary response a newer request has superseded', async () => {
    const first = deferred<unknown>()
    const second = deferred<unknown>()
    vi.mocked(API.getNodeIpInterfaces)
      .mockImplementationOnce(() => first.promise as never)
      .mockImplementationOnce(() => second.promise as never)
    const store = useNodeStore()

    const firstCall = store.getNodeSnmpPrimaryInterface('1')
    const secondCall = store.getNodeSnmpPrimaryInterface('2')

    second.resolve({ ipInterface: [{ ipAddress: '10.0.0.2' }], totalCount: 1, count: 1, offset: 0 })
    await secondCall
    first.resolve({ ipInterface: [{ ipAddress: '10.0.0.1' }], totalCount: 1, count: 1, offset: 0 })
    await firstCall

    expect(store.snmpPrimaryIpAddress).toBe('10.0.0.2')
  })
})

// Same hazard in the panels' own fetches: they are fired per node id as the user moves between
// nodes, and per page as the paginator moves.
describe('nodeStore stale panel responses', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  interface OnceMock {
    mockImplementationOnce: (fn: () => never) => OnceMock
  }

  const racePair = <T>(mock: OnceMock) => {
    const first = deferred<T>()
    const second = deferred<T>()
    mock.mockImplementationOnce(() => first.promise as never)
      .mockImplementationOnce(() => second.promise as never)

    return { first, second }
  }

  it('discards superseded SNMP interfaces', async () => {
    const { first, second } = racePair(vi.mocked(API.getNodeSnmpInterfaces) as never)
    const store = useNodeStore()

    const firstCall = store.getNodeSnmpInterfaces({ id: '1' })
    const secondCall = store.getNodeSnmpInterfaces({ id: '2' })

    second.resolve({ snmpInterface: [{ id: 2 }], totalCount: 1 } as never)
    await secondCall
    first.resolve({ snmpInterface: [{ id: 1 }], totalCount: 9 } as never)
    await firstCall

    expect(store.snmpInterfaces).toEqual([{ id: 2 }])
    expect(store.snmpInterfacesTotalCount).toBe(1)
  })

  it('discards superseded IP interfaces', async () => {
    const { first, second } = racePair(vi.mocked(API.getNodeIpInterfaces) as never)
    const store = useNodeStore()

    const firstCall = store.getNodeIpInterfaces({ id: '1' })
    const secondCall = store.getNodeIpInterfaces({ id: '2' })

    second.resolve({ ipInterface: [{ id: 'ip2' }], totalCount: 1 } as never)
    await secondCall
    first.resolve({ ipInterface: [{ id: 'ip1' }], totalCount: 9 } as never)
    await firstCall

    expect(store.ipInterfaces).toEqual([{ id: 'ip2' }])
    expect(store.ipInterfacesTotalCount).toBe(1)
  })

  it('discards superseded outages', async () => {
    const { first, second } = racePair(vi.mocked(API.getNodeOutages) as never)
    const store = useNodeStore()

    const firstCall = store.getNodeOutages({ id: '1' })
    const secondCall = store.getNodeOutages({ id: '2' })

    second.resolve({ outage: [{ id: 2 }], totalCount: 1 } as never)
    await secondCall
    first.resolve({ outage: [{ id: 1 }], totalCount: 9 } as never)
    await firstCall

    expect(store.outages).toEqual([{ id: 2 }])
    expect(store.outagesTotalCount).toBe(1)
  })

  it('discards superseded availability', async () => {
    const { first, second } = racePair(vi.mocked(API.getNodeAvailabilityPercentage) as never)
    const store = useNodeStore()

    const firstCall = store.getNodeAvailabilityPercentage('1')
    const secondCall = store.getNodeAvailabilityPercentage('2')

    second.resolve({ availability: 22 } as never)
    await secondCall
    first.resolve({ availability: 11 } as never)
    await firstCall

    expect(store.availability).toEqual({ availability: 22 })
  })
})
