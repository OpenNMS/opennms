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

import { describe, expect, test } from 'vitest'
import {
  countInterfacesForNodes,
  getInterfaceListMode,
  getInterfaceRowsForNode,
  InterfaceListMode
} from '@/components/Nodes/hooks/useInterfaceListing'
import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
import { IpInterface, MatchType, NodeQueryFilter, SnmpInterface } from '@/types'

const { getDefaultNodeQueryFilter } = useNodeQuery()

const baseHref = '/opennms/'

const makeIp = (overrides: Partial<IpInterface>): IpInterface => ({
  id: '1',
  ipAddress: '192.168.1.1',
  isManaged: '',
  ifIndex: '1',
  isDown: false,
  lastCapsdPoll: 0,
  lastEgressFlow: null,
  lastIngressFlow: null,
  monitoredServiceCount: 0,
  nodeId: 1,
  snmpInterface: undefined as unknown as SnmpInterface,
  snmpPrimary: '',
  hostName: '',
  ...overrides
} as unknown as IpInterface)

const makeSnmp = (overrides: Partial<SnmpInterface>): SnmpInterface => ({
  collect: true,
  collectFlag: 'C',
  collectionUserSpecified: false,
  hasEgressFlows: false,
  hasFlows: false,
  hasIngressFlows: false,
  id: 1,
  ifAdminStatus: 1,
  ifAlias: null,
  ifDescr: null,
  ifIndex: 1,
  ifName: null,
  ifOperStatus: 1,
  ifSpeed: 0,
  ifType: 6,
  lastCapsdPoll: 0,
  lastEgressFlow: null,
  lastIngressFlow: null,
  lastSnmpPoll: 0,
  physAddr: null,
  poll: true,
  ...overrides
} as unknown as SnmpInterface)

describe('useInterfaceListing', () => {
  describe('getInterfaceListMode', () => {
    test('returns default mode for an unfiltered query', () => {
      const filter = getDefaultNodeQueryFilter()
      expect(getInterfaceListMode(filter)).toEqual({ mode: 'default' })
    })

    test('returns maclike mode when macAddress is set', () => {
      const filter: NodeQueryFilter = { ...getDefaultNodeQueryFilter(), macAddress: 'AA:BB:CC:DD:EE:FF' }
      expect(getInterfaceListMode(filter)).toEqual({ mode: 'maclike', mac: 'AA:BB:CC:DD:EE:FF' })
    })

    test('maclike takes priority over an SNMP parm field', () => {
      const filter: NodeQueryFilter = getDefaultNodeQueryFilter()
      filter.macAddress = 'aabbccddeeff'
      filter.extendedSearch.snmpParams!.snmpIfAlias = 'uplink'
      expect(getInterfaceListMode(filter)).toEqual({ mode: 'maclike', mac: 'aabbccddeeff' })
    })

    test.each([
      ['snmpIfAlias', 'ifAlias' as const],
      ['snmpIfName', 'ifName' as const],
      ['snmpIfDescription', 'ifDescr' as const]
    ])('returns snmpParm mode for %s (contains, default match type)', (field, attr) => {
      const filter: NodeQueryFilter = getDefaultNodeQueryFilter()
      ;(filter.extendedSearch.snmpParams as any)[field] = 'uplink'
      expect(getInterfaceListMode(filter)).toEqual({ mode: 'snmpParm', attr, value: 'uplink', matchType: 'contains' })
    })

    test('returns snmpParm mode with equals matchType when snmpMatchType is Equals', () => {
      const filter: NodeQueryFilter = getDefaultNodeQueryFilter()
      filter.extendedSearch.snmpParams!.snmpIfName = 'eth0'
      filter.extendedSearch.snmpParams!.snmpMatchType = MatchType.Equals
      expect(getInterfaceListMode(filter)).toEqual({ mode: 'snmpParm', attr: 'ifName', value: 'eth0', matchType: 'equals' })
    })

    test('falls back to default mode when multiple SNMP fields are set', () => {
      const filter: NodeQueryFilter = getDefaultNodeQueryFilter()
      filter.extendedSearch.snmpParams!.snmpIfName = 'eth0'
      filter.extendedSearch.snmpParams!.snmpIfAlias = 'uplink'
      expect(getInterfaceListMode(filter)).toEqual({ mode: 'default' })
    })

    test('blank/whitespace-only field values do not trigger a mode', () => {
      const filter: NodeQueryFilter = getDefaultNodeQueryFilter()
      filter.macAddress = '   '
      filter.extendedSearch.snmpParams!.snmpIfName = '  '
      expect(getInterfaceListMode(filter)).toEqual({ mode: 'default' })
    })
  })

  describe('default mode rows', () => {
    const mode: InterfaceListMode = { mode: 'default' }

    test('excludes isManaged D and 0.0.0.0, includes the rest', () => {
      const ips = [
        makeIp({ id: '1', ipAddress: '192.168.1.1', isManaged: '' }),
        makeIp({ id: '2', ipAddress: '10.0.0.5', isManaged: 'D' }),
        makeIp({ id: '3', ipAddress: '0.0.0.0', isManaged: '' }),
        makeIp({ id: '4', ipAddress: '10.0.0.9', isManaged: null })
      ]
      const rows = getInterfaceRowsForNode('7', mode, ips, [], baseHref)
      expect(rows.map(r => r.key)).toEqual(['ip-4', 'ip-1'])
    })

    test('sorts by IP address bytes ascending, not lexicographically', () => {
      const ips = [
        makeIp({ id: 'a', ipAddress: '192.168.1.1' }),
        makeIp({ id: 'b', ipAddress: '10.0.0.2' }),
        makeIp({ id: 'c', ipAddress: '9.0.0.1' })
      ]
      const rows = getInterfaceRowsForNode('1', mode, ips, [], baseHref)
      expect(rows.map(r => r.label)).toEqual(['9.0.0.1', '10.0.0.2', '192.168.1.1'])
    })

    test('sorts IPv4 addresses before IPv6 addresses', () => {
      const ips = [
        makeIp({ id: 'a', ipAddress: '::1' }),
        makeIp({ id: 'b', ipAddress: '192.168.1.1' }),
        makeIp({ id: 'c', ipAddress: 'fe80::1' })
      ]
      const rows = getInterfaceRowsForNode('1', mode, ips, [], baseHref)
      expect(rows.map(r => r.label)).toEqual(['192.168.1.1', '::1', 'fe80::1'])
    })

    test('row shape: label = ipAddress, no suffix, href, key', () => {
      const ips = [makeIp({ id: '42', ipAddress: '192.168.1.1' })]
      const rows = getInterfaceRowsForNode('9', mode, ips, [], baseHref)
      expect(rows).toEqual([
        { key: 'ip-42', label: '192.168.1.1', href: `${baseHref}element/interface.jsp?ipinterfaceid=42` }
      ])
    })
  })

  describe('maclike mode rows', () => {
    const mac = 'AA:BB-cc'

    test('matches physAddr aabbccddeeff ignoring separators/case, excludes collectFlag D', () => {
      const snmps = [
        makeSnmp({ id: 1, physAddr: 'aabbccddeeff', collectFlag: 'C' }),
        makeSnmp({ id: 2, physAddr: 'aabbccddeeff', collectFlag: 'D' }),
        makeSnmp({ id: 3, physAddr: 'ffeeddccbbaa', collectFlag: 'C' })
      ]
      const mode: InterfaceListMode = { mode: 'maclike', mac }
      const rows = getInterfaceRowsForNode('1', mode, [], snmps, baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-1'])
    })

    test('label falls back: associated IP -> ifName -> ifDescr -> ifIndex:{n}', () => {
      const withIp = makeSnmp({ id: 1, ifIndex: 1, physAddr: 'aabbccddeeff' })
      const withName = makeSnmp({ id: 2, ifIndex: 2, physAddr: 'aabbccddeeff', ifName: 'eth0' })
      const withDescr = makeSnmp({ id: 3, ifIndex: 3, physAddr: 'aabbccddeeff', ifDescr: 'Ethernet0' })
      const withNeither = makeSnmp({ id: 4, ifIndex: 4, physAddr: 'aabbccddeeff' })

      const ips = [makeIp({ id: '100', ipAddress: '10.1.1.1', snmpInterface: withIp })]

      const mode: InterfaceListMode = { mode: 'maclike', mac }
      const rows = getInterfaceRowsForNode('1', mode, ips, [withIp, withName, withDescr, withNeither], baseHref)

      expect(rows.find(r => r.key === 'snmp-1')?.label).toBe('10.1.1.1')
      expect(rows.find(r => r.key === 'snmp-2')?.label).toBe('eth0')
      expect(rows.find(r => r.key === 'snmp-3')?.label).toBe('Ethernet0')
      expect(rows.find(r => r.key === 'snmp-4')?.label).toBe('ifIndex:4')
    })

    test('href links to the ip interface when associated, otherwise to snmpinterface.jsp', () => {
      const withIp = makeSnmp({ id: 1, ifIndex: 1, physAddr: 'aabbccddeeff' })
      const withoutIp = makeSnmp({ id: 2, ifIndex: 22, physAddr: 'aabbccddeeff' })
      const ips = [makeIp({ id: '55', ipAddress: '10.1.1.1', snmpInterface: withIp })]

      const mode: InterfaceListMode = { mode: 'maclike', mac }
      const rows = getInterfaceRowsForNode('9', mode, ips, [withIp, withoutIp], baseHref)

      expect(rows.find(r => r.key === 'snmp-1')?.href).toBe(`${baseHref}element/interface.jsp?ipinterfaceid=55`)
      expect(rows.find(r => r.key === 'snmp-2')?.href).toBe(`${baseHref}element/snmpinterface.jsp?node=9&ifindex=22`)
    })

    test('suffix is the physAddr', () => {
      const snmp = makeSnmp({ id: 1, physAddr: 'aabbccddeeff' })
      const mode: InterfaceListMode = { mode: 'maclike', mac }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows[0].suffix).toBe('aabbccddeeff')
    })

    test('excludes null physAddr', () => {
      const snmp = makeSnmp({ id: 1, physAddr: null })
      const mode: InterfaceListMode = { mode: 'maclike', mac }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows).toEqual([])
    })
  })

  describe('snmpParm contains mode rows', () => {
    test('matches case-insensitively', () => {
      const snmp = makeSnmp({ id: 1, ifAlias: 'Uplink-To-Core' })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifAlias', value: 'uplink-to-core', matchType: 'contains' }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-1'])
    })

    test('% is a SQL LIKE wildcard translated to .*', () => {
      const snmp = makeSnmp({ id: 1, ifName: 'GigabitEthernet0/1' })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifName', value: 'Gigabit%0/1', matchType: 'contains' }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-1'])
    })

    test('_ is a SQL LIKE wildcard translated to . (matches any single character)', () => {
      const snmp = makeSnmp({ id: 1, ifDescr: 'upXlink' })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifDescr', value: 'up_link', matchType: 'contains' }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-1'])
    })

    test('a literal "." in the search value is escaped and does not act as a wildcard', () => {
      // 'up.link' -> escaped to 'up\.link', which requires a literal '.' character between
      // 'up' and 'link' -- 'uplink' (no separator at all) must NOT match.
      const snmp = makeSnmp({ id: 1, ifDescr: 'uplink' })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifDescr', value: 'up.link', matchType: 'contains' }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows).toEqual([])
    })

    test('a literal "." in the search value does match when the literal dot is present', () => {
      const snmp = makeSnmp({ id: 1, ifDescr: 'up.link' })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifDescr', value: 'up.link', matchType: 'contains' }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-1'])
    })

    test('excludes collectFlag D and null attr values', () => {
      const collected = makeSnmp({ id: 1, ifAlias: 'uplink', collectFlag: 'C' })
      const notCollected = makeSnmp({ id: 2, ifAlias: 'uplink', collectFlag: 'D' })
      const nullAlias = makeSnmp({ id: 3, ifAlias: null })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifAlias', value: 'uplink', matchType: 'contains' }
      const rows = getInterfaceRowsForNode('1', mode, [], [collected, notCollected, nullAlias], baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-1'])
    })

    test('suffix is the matched attribute value; label falls back to ifIndex {n} with a space', () => {
      const snmp = makeSnmp({ id: 1, ifIndex: 7, ifAlias: 'uplink' })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifAlias', value: 'uplink', matchType: 'contains' }
      const rows = getInterfaceRowsForNode('1', mode, [], [snmp], baseHref)
      expect(rows[0].suffix).toBe('uplink')
      expect(rows[0].label).toBe('ifIndex 7')
    })
  })

  describe('snmpParm equals mode rows', () => {
    test('matches case-insensitively but not as a substring', () => {
      const exact = makeSnmp({ id: 1, ifName: 'eth0' })
      const substring = makeSnmp({ id: 2, ifName: 'eth01' })
      const mode: InterfaceListMode = { mode: 'snmpParm', attr: 'ifName', value: 'ETH0', matchType: 'equals' }
      const rows = getInterfaceRowsForNode('1', mode, [], [exact, substring], baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-1'])
    })
  })

  describe('sorting (maclike / snmpParm share SnmpInterfaceComparator)', () => {
    test('sorts by ifName, then ifDescr, then ifIndex, then id (nulls last)', () => {
      const a = makeSnmp({ id: 10, ifName: 'b', physAddr: 'aabbccddeeff' })
      const b = makeSnmp({ id: 11, ifName: 'a', physAddr: 'aabbccddeeff' })
      const c = makeSnmp({ id: 12, ifName: null, ifDescr: 'x', physAddr: 'aabbccddeeff' })
      const d = makeSnmp({ id: 13, ifName: null, ifDescr: null, ifIndex: 1, physAddr: 'aabbccddeeff' })
      const e = makeSnmp({ id: 14, ifName: null, ifDescr: null, ifIndex: 2, physAddr: 'aabbccddeeff' })

      const mode: InterfaceListMode = { mode: 'maclike', mac: 'aabbccddeeff' }
      const rows = getInterfaceRowsForNode('1', mode, [], [a, b, c, d, e], baseHref)
      expect(rows.map(r => r.key)).toEqual(['snmp-11', 'snmp-10', 'snmp-12', 'snmp-13', 'snmp-14'])
    })
  })

  describe('countInterfacesForNodes', () => {
    test('sums rows across nodes and tolerates missing map entries', () => {
      const ipMap = new Map<string, IpInterface[]>()
      ipMap.set('1', [makeIp({ id: '1', ipAddress: '10.0.0.1' }), makeIp({ id: '2', ipAddress: '10.0.0.2' })])
      ipMap.set('2', [makeIp({ id: '3', ipAddress: '10.0.0.3' })])
      // node '3' intentionally absent from ipMap

      const snmpMap = new Map<string, SnmpInterface[]>()

      const mode: InterfaceListMode = { mode: 'default' }
      const total = countInterfacesForNodes(['1', '2', '3'], mode, ipMap, snmpMap)
      expect(total).toBe(3)
    })

    test('returns 0 for an empty node id list', () => {
      const mode: InterfaceListMode = { mode: 'default' }
      const total = countInterfacesForNodes([], mode, new Map(), new Map())
      expect(total).toBe(0)
    })
  })
})
