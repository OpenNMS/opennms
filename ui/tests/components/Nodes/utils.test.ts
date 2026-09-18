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
import { mock } from 'vitest-mock-extended'
import {
  buildSnmpNarrowing,
  formatIfSpeed,
  getTableCssClasses,
  hasEgressFlow,
  hasIngressFlow,
  ipInterfaceStatus,
  matchesSearchTerm,
  snmpIfStatusText,
  snmpInterfaceFlowGraphsTooltip,
  snmpInterfaceFlows,
  snmpInterfaceFlowsTooltip,
  snmpInterfaceStatus,
  snmpInterfaceStatusTooltip
} from '@/components/Nodes/utils'
import { Node, NodeColumnSelectionItem, SnmpInterface } from '@/types'

describe('Nodes utils test', () => {
  test('test getTableCssClasses', async () => {
    let result = getTableCssClasses([])
    expect(result).toEqual(['tl1'])

    const defaultColumns: NodeColumnSelectionItem[] = [
      { id: 'id', label: 'ID', selected: true, order: 0 },
      { id: 'label', label: 'Node Label', selected: true, order: 1 },
      { id: 'ipaddress', label: 'IP Address', selected: true, order: 2 },
      { id: 'location', label: 'Location', selected: false, order: 3 },
      { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 4 },
      { id: 'foreignId', label: 'Foreign ID', selected: true, order: 5 },
      { id: 'sysContact', label: 'Sys Contact', selected: true, order: 6 },
      { id: 'sysLocation', label: 'Sys Location', selected: true, order: 7 },
      { id: 'sysDescription', label: 'Sys Description', selected: true, order: 8 },
      { id: 'flows', label: 'Flows', selected: true, order: 9 }
    ]

    result = getTableCssClasses(defaultColumns)

    // tl1: implicit 'action' column
    // tr2: 'id' column is right-aligned
    // tc10: 'flows' column is center-aligned
    // 10 items: implicit action + 9 selected columns ('location' is unselected)
    expect(result).toEqual(['tl1', 'tl2', 'tl3', 'tl4', 'tl5', 'tl6', 'tl7', 'tl8', 'tl9', 'tc10'])
  })

  test('test hasEgressFlow, hasIngressFlow', async () => {
    const both = mock<Node>()
    both.lastEgressFlow = 1699909194000
    both.lastIngressFlow = 1699909194000

    expect(hasEgressFlow(both)).toBeTruthy()
    expect(hasIngressFlow(both)).toBeTruthy()

    const egress = mock<Node>()
    egress.lastEgressFlow = 1699909194000
    egress.lastIngressFlow = 0

    expect(hasEgressFlow(egress)).toBeTruthy()
    expect(hasIngressFlow(egress)).toBeFalsy()

    const ingress = mock<Node>()
    ingress.lastEgressFlow = 0
    ingress.lastIngressFlow = 1699909194000

    expect(hasEgressFlow(ingress)).toBeFalsy()
    expect(hasIngressFlow(ingress)).toBeTruthy()

    const neither = mock<Node>()
    neither.lastEgressFlow = 0
    neither.lastIngressFlow = 0

    expect(hasEgressFlow(neither)).toBeFalsy()
    expect(hasIngressFlow(neither)).toBeFalsy()
  })

  describe('buildSnmpNarrowing', () => {
    test('narrows a maclike fetch by the normalized MAC', () => {
      expect(buildSnmpNarrowing({ mode: 'maclike', mac: 'aa:bb:cc' })).toBe('physAddr==*aabbcc*')
    })

    test('narrows an snmpParm fetch by the searched attribute', () => {
      expect(buildSnmpNarrowing({ mode: 'snmpParm', attr: 'ifAlias', value: 'core', matchType: 'contains' }))
        .toBe('ifAlias==*core*')
    })

    // The narrowing sent to the server has to stay a superset of the client-side match, so a
    // value carrying LIKE wildcards or FIQL operators drops the attribute term entirely.
    test.each(['up%', 'up_link', 'a,b', 'a;b', 'a(b', 'a)b'])('omits narrowing for %s', (value) => {
      expect(buildSnmpNarrowing({ mode: 'snmpParm', attr: 'ifName', value, matchType: 'contains' }))
        .toBeUndefined()
    })

    test('narrows nothing in the default mode', () => {
      expect(buildSnmpNarrowing({ mode: 'default' })).toBeUndefined()
    })
  })

  // Deliberately finer-grained than the JSP interfaces page, which folded every non-up
  // ifAdminStatus into 'unknown'. ifAdminStatus decides first: it says what the interface is
  // being ASKED to do, and only an interface asked to be up is judged on what it is doing.
  describe('snmpInterfaceStatus', () => {
    const snmpInterface = (ifAdminStatus?: number, ifOperStatus?: number) =>
      ({ ifAdminStatus, ifOperStatus }) as SnmpInterface

    // admin, oper, expected -- the whole IF-MIB range, since devices report all of it
    test.each([
      // administratively up: the operational status decides
      [1, 1, 'UP'],
      [1, 2, 'DOWN'],
      [1, 3, 'TESTING'],
      [1, 4, 'DOWN'],
      [1, 5, 'DOWN'],
      [1, 6, 'DOWN'],
      [1, 7, 'DOWN'],
      // administratively down: turned off on purpose, whatever it is doing
      [2, 1, 'DISABLED'],
      [2, 2, 'DISABLED'],
      [2, 3, 'DISABLED'],
      // under test: mid-change, whatever it is doing
      [3, 1, 'TESTING'],
      [3, 2, 'TESTING'],
      [3, 3, 'TESTING']
    ])('admin %i, oper %i -> %s', (admin, oper, expected) => {
      expect(snmpInterfaceStatus(snmpInterface(admin, oper))).toBe(expected)
    })

    // UNKNOWN now means what it says -- no usable ifAdminStatus -- rather than standing in for
    // admin-down and testing as well.
    test.each([
      [undefined, undefined],
      [undefined, 1],
      [0, 1],
      [9, 1]
    ])('is UNKNOWN when ifAdminStatus is %s', (admin, oper) => {
      expect(snmpInterfaceStatus(snmpInterface(admin, oper))).toBe('UNKNOWN')
    })

    test('is UNKNOWN for an interface the poller has not reached', () => {
      expect(snmpInterfaceStatus({} as SnmpInterface)).toBe('UNKNOWN')
    })

    // An interface meant to be up with no operational status is not up.
    test('is DOWN when administratively up with no operational status', () => {
      expect(snmpInterfaceStatus(snmpInterface(1, undefined))).toBe('DOWN')
    })
  })

  describe('snmpIfStatusText', () => {
    test.each([
      [1, '1 (Up)'],
      [2, '2 (Down)'],
      [3, '3 (Testing)'],
      [4, '4 (Unknown)'],
      [5, '5 (Dormant)'],
      [6, '6 (Not Present)'],
      [7, '7 (Lower Layer Down)']
    ])('labels %i as %s', (status, expected) => {
      expect(snmpIfStatusText(status)).toBe(expected)
    })

    // Not guessed at: labelling it would conflate it with ifOperStatus 4, which means
    // exactly 'unknown'.
    test('shows a value outside the MIB range bare', () => {
      expect(snmpIfStatusText(9)).toBe('9')
    })

    test.each([undefined, null])('shows N/A for %s', (status) => {
      expect(snmpIfStatusText(status)).toBe('N/A')
    })
  })

  describe('tooltips', () => {
    test('the status tooltip shows both raw IF-MIB statuses', () => {
      expect(snmpInterfaceStatusTooltip({ ifAdminStatus: 1, ifOperStatus: 5 } as SnmpInterface))
        .toBe('Admin Status: 1 (Up)\nOperational Status: 5 (Dormant)')
    })

    test('the status tooltip falls back to N/A on an unpolled interface', () => {
      expect(snmpInterfaceStatusTooltip({} as SnmpInterface))
        .toBe('Admin Status: N/A\nOperational Status: N/A')
    })
  })

  // The term arrives already trimmed and lowercased from useDebouncedSearch, so these cases are
  // about matching, not normalising.
  describe('matchesSearchTerm', () => {
    test('matches an empty term against everything', () => {
      expect(matchesSearchTerm('', ['anything'])).toBe(true)
      expect(matchesSearchTerm('', [null, undefined])).toBe(true)
    })

    test('matches a substring of any value', () => {
      expect(matchesSearchTerm('eth', ['lo0', 'eth1', null])).toBe(true)
    })

    test('lowercases the values it compares', () => {
      expect(matchesSearchTerm('uplink', ['UPLINK Port'])).toBe(true)
    })

    test('matches numbers by their text', () => {
      expect(matchesSearchTerm('7', [17])).toBe(true)
      expect(matchesSearchTerm('7', [1, 2])).toBe(false)
    })

    // A single character has to work: it is the only way to reach a single-digit ifIndex.
    test('matches on a single character', () => {
      expect(matchesSearchTerm('7', [7])).toBe(true)
    })

    // Otherwise 'a' would pick up every row with a missing field, since the tables render
    // those cells as 'N/A'.
    test('does not match absent values', () => {
      expect(matchesSearchTerm('a', [null, undefined])).toBe(false)
      expect(matchesSearchTerm('n/a', [null])).toBe(false)
    })

    test('does not match when no value contains the term', () => {
      expect(matchesSearchTerm('zzz', ['eth0', 17, 'Uplink'])).toBe(false)
    })
  })

  // Parity with SIUtils.getHumanReadableIfSpeed, the platform's canonical rendering.
  describe('formatIfSpeed', () => {
    test.each([
      [0, '0 bps'],
      [1, '1 bps'],
      [999, '999 bps'],
      [1000, '1 kbps'],
      [100000, '100 kbps'],
      [1000000, '1 Mbps'],
      [10000000, '10 Mbps'],
      [100000000, '100 Mbps'],
      [1000000000, '1 Gbps'],
      [10000000000, '10 Gbps']
    ])('formats %i as %s', (speed, expected) => {
      expect(formatIfSpeed(speed)).toBe(expected)
    })

    // An exact multiple of the unit prints with no decimal point at all (DecimalFormat "0");
    // anything else gets one to three digits (DecimalFormat "0.0##").
    test.each([
      [2500000000, '2.5 Gbps'],
      [1500000, '1.5 Mbps'],
      [1536000, '1.536 Mbps'],
      [1544000, '1.544 Mbps'],
      [1200, '1.2 kbps']
    ])('formats %i as %s', (speed, expected) => {
      expect(formatIfSpeed(speed)).toBe(expected)
    })

    // Grouping is off in both Java formatters, so a very large value has no thousands separator.
    test('does not group thousands', () => {
      expect(formatIfSpeed(5000000000000)).toBe('5000 Gbps')
    })

    // Each threshold picks the largest unit the value actually reaches, never the next one up.
    test('keeps a value just under a threshold in the smaller unit', () => {
      expect(formatIfSpeed(999999)).toBe('999.999 kbps')
    })

    // A quirk inherited from SIUtils rather than a bug here: the unit is chosen from the raw
    // value, but the rounding happens after the division, so a value just under the next
    // threshold can round up past it in the display -- 999999999 is 999.999999 Mbps, which at
    // three decimals reads '1000.0 Mbps' rather than '1 Gbps'. Java's DecimalFormat("0.0##")
    // does the same, and parity is worth more here than a cosmetic divergence.
    test('rounds up within the unit rather than promoting to the next one', () => {
      expect(formatIfSpeed(999999999)).toBe('1000.0 Mbps')
    })

    test.each([undefined, null])('shows N/A for %s', (speed) => {
      expect(formatIfSpeed(speed)).toBe('N/A')
    })
  })

  // Flow availability is a property of the interface row -- unlike the flow graph URL, which
  // costs a request apiece -- so both the tags and the column's sort key derive from it.
  describe('flows', () => {
    test.each([
      [true, true, 'Ingress/Egress'],
      [true, false, 'Ingress'],
      [false, true, 'Egress'],
      [false, false, ''],
      [undefined, undefined, '']
    ])('reads %s / %s as "%s"', (hasIngressFlows, hasEgressFlows, expected) => {
      expect(snmpInterfaceFlows({ hasIngressFlows, hasEgressFlows } as SnmpInterface)).toBe(expected)
    })

    // Sorting the column groups the interfaces carrying flows at one end, which is the whole
    // reason to sort it. Ascending puts the ones with none first.
    test('sorts interfaces without flows below those with them', () => {
      const keys = [
        { hasIngressFlows: true, hasEgressFlows: true },
        { hasIngressFlows: false, hasEgressFlows: false },
        { hasIngressFlows: false, hasEgressFlows: true },
        { hasIngressFlows: true, hasEgressFlows: false }
      ].map(i => snmpInterfaceFlows(i as SnmpInterface)).sort()

      expect(keys).toEqual(['', 'Egress', 'Ingress', 'Ingress/Egress'])
    })

    test.each([
      [true, true, 'Ingress/egress flow data available'],
      [true, false, 'Ingress flow data available'],
      [false, true, 'Egress flow data available'],
      [false, false, '']
    ])('describes %s / %s flows as "%s"', (hasIngressFlows, hasEgressFlows, expected) => {
      expect(snmpInterfaceFlowsTooltip({ hasIngressFlows, hasEgressFlows } as SnmpInterface))
        .toBe(expected)
    })

    test.each([
      [true, true, 'View Ingress/Egress flow graphs.'],
      [true, false, 'View Ingress flow graphs.'],
      [false, true, 'View Egress flow graphs.'],
      [false, false, '']
    ])('labels the graphs button for %s / %s flows as "%s"', (hasIngressFlows, hasEgressFlows, expected) => {
      expect(snmpInterfaceFlowGraphsTooltip({ hasIngressFlows, hasEgressFlows } as SnmpInterface))
        .toBe(expected)
    })
  })


  // ipinterface.isManaged is an unconstrained char(1) -- no enum, no check constraint -- so this
  // has to cope with a code nobody has defined as well as the ones the product writes.
  describe('ipInterfaceStatus', () => {
    test.each([
      ['M', 'Managed'],
      ['U', 'Unmanaged'],
      ['F', 'Forced Unmanaged'],
      ['N', 'Not Monitored'],
      ['D', 'Deleted'],
      ['A', 'Unknown (A)']
    ])('reads %s as "%s"', (code, label) => {
      expect(ipInterfaceStatus(code)).toBe(label)
    })

    // ElementUtil returns null here and the JSP renders an empty cell, which hides the surprise.
    test('names an undefined code', () => {
      expect(ipInterfaceStatus('X')).toBe('Unknown (X)')
    })

    test.each([[null], [undefined], ['']])('reads %s as Unknown', (code) => {
      expect(ipInterfaceStatus(code)).toBe('Unknown')
    })
  })

})
