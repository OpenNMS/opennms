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

import { describe, expect, it } from 'vitest'
import { buildServiceLinks, ipAddressBytes, lowestAddress, SERVICE_LINK_NAMES, urlHost } from '@/components/Nodes/nodeServiceLinks'

describe('nodeServiceLinks', () => {
  it('looks for the legacy page\'s six services', () => {
    expect(SERVICE_LINK_NAMES).toEqual(['Telnet', 'SSH', 'HTTP', 'HTTPS', 'Dell-OpenManage', 'MS-RDP'])
  })

  describe('ipAddressBytes', () => {
    it.each([
      ['10.0.0.1', [10, 0, 0, 1]],
      ['::1', [...Array(15).fill(0), 1]],
      ['fe80::1%eth0', [0xfe, 0x80, ...Array(13).fill(0), 1]],
      ['::ffff:10.0.0.1', [...Array(10).fill(0), 0xff, 0xff, 10, 0, 0, 1]],
      ['2001:db8:0:0:0:0:0:1', [0x20, 0x01, 0x0d, 0xb8, ...Array(11).fill(0), 1]]
    ])('%s', (ip, bytes) => {
      expect(ipAddressBytes(ip)).toEqual(bytes)
    })

    it.each(['', 'host', '10.0.0.256', '1::2::3', '1:2:3', '1:2:3:4:5:6:7:8:9'])('rejects %j', (ip) => {
      expect(ipAddressBytes(ip)).toBeUndefined()
    })
  })

  // As InetAddressUtils.getLowestInetAddress: IPv4 before IPv6, then byte order -- not string order.
  describe('lowestAddress', () => {
    it('compares numerically, not as strings', () => {
      expect(lowestAddress(['10.0.0.20', '10.0.0.3', '9.255.255.255'])).toBe('9.255.255.255')
    })

    it('puts any IPv4 address before IPv6', () => {
      expect(lowestAddress(['::1', '192.168.0.1'])).toBe('192.168.0.1')
    })

    it('ignores what is not an address, and has nothing for an empty list', () => {
      expect(lowestAddress(['nope', '10.0.0.2'])).toBe('10.0.0.2')
      expect(lowestAddress([])).toBeUndefined()
    })
  })

  describe('urlHost', () => {
    it('leaves IPv4 alone', () => {
      expect(urlHost('10.0.0.1')).toBe('10.0.0.1')
    })

    it('brackets IPv6 and drops a zone id', () => {
      expect(urlHost('fe80::1%eth0')).toBe('[fe80::1]')
    })
  })

  describe('buildServiceLinks', () => {
    it('builds each link the legacy page did, in its order', () => {
      const services = ['MS-RDP', 'Dell-OpenManage', 'HTTPS', 'HTTP', 'SSH', 'Telnet']
        .map(serviceName => ({ serviceName, ipAddress: '10.0.0.1' }))

      expect(buildServiceLinks(services)).toEqual([
        { label: 'Telnet', url: 'telnet://10.0.0.1', newTab: false },
        { label: 'SSH', url: 'ssh://10.0.0.1', newTab: false },
        { label: 'HTTP', url: 'http://10.0.0.1/', newTab: true },
        { label: 'HTTPS', url: 'https://10.0.0.1/', newTab: true },
        { label: 'OpenManage', url: 'https://10.0.0.1:1311', newTab: true },
        { label: 'Microsoft RDP', url: 'rdp://10.0.0.1:3389', newTab: false }
      ])
    })

    it('links a service on several interfaces to its lowest address', () => {
      expect(buildServiceLinks([
        { serviceName: 'SSH', ipAddress: '10.0.0.9' },
        { serviceName: 'SSH', ipAddress: '10.0.0.10' },
        { serviceName: 'SSH', ipAddress: '10.0.0.2' }
      ])).toEqual([{ label: 'SSH', url: 'ssh://10.0.0.2', newTab: false }])
    })

    it('brackets an IPv6 address in the URL', () => {
      expect(buildServiceLinks([{ serviceName: 'HTTP', ipAddress: 'fe80::1' }])[0].url).toBe('http://[fe80::1]/')
    })

    it('has no links for a node with none of the services', () => {
      expect(buildServiceLinks([{ serviceName: 'ICMP', ipAddress: '10.0.0.1' }])).toEqual([])
    })
  })
})
