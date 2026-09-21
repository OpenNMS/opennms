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
  INTERFACE_PATH,
  OUTAGE_DETAIL_PATH,
  OUTAGE_LIST_PATH,
  OUTAGE_LIST_TYPE_BOTH,
  SERVICE_PATH,
  SNMP_INTERFACE_PATH,
  interfaceByIdLink,
  interfaceLink,
  nodeLink,
  nodeAndInterfaceQuery,
  nodeOutageListLink,
  outageDetailLink,
  serviceLink,
  snmpInterfaceLink
} from '@/lib/linkUtils'

const BASE = '/opennms/'

// A plain v4 address survives encoding untouched; the v6 forms are the reason encoding is here
// at all. The zone-index case is the one that actually corrupts without it -- '%' starts a
// percent-escape, so 'fe80::1%eth0' would reach the server as a truncated or invalid address.
const IPV4 = '10.0.0.44'
const IPV6 = '2001:db8::1'
const IPV6_ZONE = 'fe80::1%eth0'

describe('linkUtils', () => {
  // The only link here whose path is not a constant: baseNodeUrl comes from the menu config and
  // already ends in its own separator, so the id is appended directly.
  describe('nodeLink', () => {
    const BASE_NODE_URL = 'element/node.jsp?node='

    test('appends the node id to the configured node url', () => {
      expect(nodeLink(BASE, BASE_NODE_URL, 42)).toBe('/opennms/element/node.jsp?node=42')
    })

    test('takes the node id as a string or a number', () => {
      expect(nodeLink(BASE, BASE_NODE_URL, '42')).toBe(nodeLink(BASE, BASE_NODE_URL, 42))
    })

    // baseNodeUrl is configuration, so it need not be a query string at all.
    test('works with a path-style node url', () => {
      expect(nodeLink(BASE, 'node/', 42)).toBe('/opennms/node/42')
    })
  })

  describe('nodeAndInterfaceQuery', () => {
    test('leaves an IPv4 address as-is', () => {
      expect(nodeAndInterfaceQuery(42, IPV4)).toBe('?node=42&intf=10.0.0.44')
    })

    test('encodes the colons of an IPv6 address', () => {
      expect(nodeAndInterfaceQuery(42, IPV6)).toBe('?node=42&intf=2001%3Adb8%3A%3A1')
    })

    test('encodes the percent of an IPv6 zone index', () => {
      expect(nodeAndInterfaceQuery(42, IPV6_ZONE)).toBe('?node=42&intf=fe80%3A%3A1%25eth0')
    })

    test('takes the node id as a string or a number', () => {
      expect(nodeAndInterfaceQuery('42', IPV4)).toBe(nodeAndInterfaceQuery(42, IPV4))
    })
  })

  describe('interfaceLink', () => {
    test('addresses the interface page by node and IPv4 address', () => {
      expect(interfaceLink(BASE, 42, IPV4)).toBe('/opennms/element/interface.jsp?node=42&intf=10.0.0.44')
    })

    test('encodes an IPv6 address', () => {
      expect(interfaceLink(BASE, 42, IPV6)).toBe('/opennms/element/interface.jsp?node=42&intf=2001%3Adb8%3A%3A1')
    })

    test('encodes an IPv6 zone index', () => {
      expect(interfaceLink(BASE, 42, IPV6_ZONE))
        .toBe('/opennms/element/interface.jsp?node=42&intf=fe80%3A%3A1%25eth0')
    })
  })

  // Addressing by id needs no encoding, which is the reason to prefer it where an id is in hand.
  describe('interfaceByIdLink', () => {
    test('addresses the same page by IP interface id', () => {
      expect(interfaceByIdLink(BASE, 55)).toBe('/opennms/element/interface.jsp?ipinterfaceid=55')
    })
  })

  describe('snmpInterfaceLink', () => {
    test('addresses the SNMP interface page by node and ifIndex', () => {
      expect(snmpInterfaceLink(BASE, 9, 22)).toBe('/opennms/element/snmpinterface.jsp?node=9&ifindex=22')
    })
  })

  describe('serviceLink', () => {
    test('adds the service to the node-and-interface query', () => {
      expect(serviceLink(BASE, 42, IPV4, 3))
        .toBe('/opennms/element/service.jsp?node=42&intf=10.0.0.44&service=3')
    })

    test('encodes an IPv6 address and keeps the service last', () => {
      expect(serviceLink(BASE, 42, IPV6_ZONE, 3))
        .toBe('/opennms/element/service.jsp?node=42&intf=fe80%3A%3A1%25eth0&service=3')
    })
  })

  describe('outageDetailLink', () => {
    test('addresses one outage by id', () => {
      expect(outageDetailLink(BASE, 2435)).toBe('/opennms/outage/detail.htm?id=2435')
    })
  })

  describe('nodeOutageListLink', () => {
    // The Recent Outages panel passes 'both' so the legacy list shows what the panel shows.
    test('includes outtype when one is given', () => {
      expect(nodeOutageListLink(BASE, 42, OUTAGE_LIST_TYPE_BOTH))
        .toBe('/opennms/outage/list.htm?filter=node%3D42&outtype=both')
    })

    // The node actions menu passes none, leaving the legacy list its own default.
    test('omits outtype when none is given', () => {
      expect(nodeOutageListLink(BASE, 42)).toBe('/opennms/outage/list.htm?filter=node%3D42')
    })

    // mapLink in nodeActionLinks returns relative links; its callers prepend baseHref.
    test('is relative with an empty baseHref', () => {
      expect(nodeOutageListLink('', 42)).toBe('outage/list.htm?filter=node%3D42')
    })
  })

  // The paths are exported so callers never re-spell them; pin the spellings the legacy app uses.
  describe('paths', () => {
    test.each([
      [INTERFACE_PATH, 'element/interface.jsp'],
      [SNMP_INTERFACE_PATH, 'element/snmpinterface.jsp'],
      [SERVICE_PATH, 'element/service.jsp'],
      [OUTAGE_LIST_PATH, 'outage/list.htm'],
      [OUTAGE_DETAIL_PATH, 'outage/detail.htm']
    ])('%s', (actual, expected) => {
      expect(actual).toBe(expected)
    })
  })
})
