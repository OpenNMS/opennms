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
  getTableCssClasses,
  hasEgressFlow,
  hasIngressFlow,
  matchesSearchTerm,
  snmpIfStatusText,
  snmpInterfaceNameTooltip,
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

  // Mirrors setStylesForSnmpInterfaces() in the JSP interfaces page. Note the rule keys off
  // ifAdminStatus/ifOperStatus -- NOT the isManaged/isDown rule that page uses for IP interfaces.
  describe('snmpInterfaceStatus', () => {
    const snmpInterface = (ifAdminStatus: number, ifOperStatus: number) =>
      ({ ifAdminStatus, ifOperStatus }) as SnmpInterface

    test('is UP when administratively and operationally up', () => {
      expect(snmpInterfaceStatus(snmpInterface(1, 1))).toBe('UP')
    })

    // Every non-up ifOperStatus counts as down once the interface is meant to be up.
    test.each([2, 3, 4, 5, 6, 7])('is DOWN when administratively up but operationally %i', (oper) => {
      expect(snmpInterfaceStatus(snmpInterface(1, oper))).toBe('DOWN')
    })

    // Nobody is asking these to run, so their operational status says nothing -- even an
    // operationally-up one is UNKNOWN.
    test.each([
      [2, 1],
      [2, 2],
      [3, 1]
    ])('is UNKNOWN when not administratively up (admin %i, oper %i)', (admin, oper) => {
      expect(snmpInterfaceStatus(snmpInterface(admin, oper))).toBe('UNKNOWN')
    })

    // An interface the poller has not reached yet carries neither status.
    test('is UNKNOWN when the statuses are missing', () => {
      expect(snmpInterfaceStatus({} as SnmpInterface)).toBe('UNKNOWN')
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

    // ifDescr has no column of its own any more, so this tooltip is the only place it shows.
    test('the name tooltip carries the name and the description', () => {
      expect(snmpInterfaceNameTooltip({ ifName: 'eth0', ifDescr: 'Uplink port' } as SnmpInterface))
        .toBe('Name: eth0\nDescription: Uplink port')
    })

    test.each([
      [null, null],
      [undefined, undefined],
      ['', '']
    ])('the name tooltip falls back to N/A for %s / %s', (ifName, ifDescr) => {
      expect(snmpInterfaceNameTooltip({ ifName, ifDescr } as SnmpInterface))
        .toBe('Name: N/A\nDescription: N/A')
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
})
