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

import { beforeAll, describe, expect, test } from 'vitest'
import { mock } from 'vitest-mock-extended'
import { useIpInterfaceQuery } from '@/components/Nodes/hooks/useIpInterfaceQuery'
import { IpInterface } from '@/types'

const { getBestIpInterfaceForNode } = useIpInterfaceQuery()

const nodeToIpInterfaceMap = new Map<string, IpInterface[]>()

const defaultIpAddressInfo = {
  label: '',
  managed: false,
  primaryLabel: '',
  primaryType: ''
}

const createMockIpAddress = (ipAddress: string, isManaged: string, snmpPrimary: string) => {
  const item = mock<IpInterface>()
  item.ipAddress = ipAddress
  item.isManaged = isManaged
  item.snmpPrimary = snmpPrimary

  return item
}

const setupMap = () => {
  nodeToIpInterfaceMap.set('1', [createMockIpAddress('192.168.0.1', 'M', 'P')])
  nodeToIpInterfaceMap.set('2', [createMockIpAddress('192.168.0.2', '', 'S')])
  nodeToIpInterfaceMap.set('3', [createMockIpAddress('192.168.0.3', '', 'N')])
  nodeToIpInterfaceMap.set('4', [createMockIpAddress('192.168.0.4', '', 'Q')])
  nodeToIpInterfaceMap.set('888', [])

  nodeToIpInterfaceMap.set('5', [
    createMockIpAddress('192.168.0.50', 'M', 'Q'),
    createMockIpAddress('192.168.0.51', '', 'P'),
    createMockIpAddress('192.168.0.52', '', '')
  ])

  nodeToIpInterfaceMap.set('6', [
    createMockIpAddress('192.168.0.60', '', 'Q'),
    createMockIpAddress('192.168.0.61', '', ''),
    createMockIpAddress('192.168.0.62', 'M', 'S')
  ])

  nodeToIpInterfaceMap.set('7', [
    createMockIpAddress('192.168.0.70', '', 'Q'),
    createMockIpAddress('192.168.0.71', '', ''),
    createMockIpAddress('192.168.0.72', '', 'S')
  ])
}

describe('Nodes useIpInterfaceQuery test', () => {
  beforeAll(() => {
    setupMap()
  })

  describe('test getBestIpInterfaceForNode', () => {
    test.each([
      ['empty', '', defaultIpAddressInfo],
      ['nodeId not found', '999', defaultIpAddressInfo],
      ['nodeId found but has no interfaces', '888', defaultIpAddressInfo],
      [
        'node has only one Secondary interface',
        '2',
        { label: '192.168.0.2', managed: false, primaryLabel: 'Secondary', primaryType: 'S' }
      ],
      [
        'node has only one Not Eligible interface',
        '3',
        { label: '192.168.0.3', managed: false, primaryLabel: 'Not Eligible', primaryType: 'N' }
      ],
      [
        'node has only one Other interface',
        '4',
        { label: '192.168.0.4', managed: false, primaryLabel: '', primaryType: 'Q' }
      ],
      [
        'get the SNMP Primary from multiple IPs, even if it is not managed',
        '5',
        { label: '192.168.0.51', managed: false, primaryLabel: 'Primary', primaryType: 'P' }
      ],
      [
        'get the managed one, if no SNMP Primary',
        '6',
        { label: '192.168.0.62', managed: true, primaryLabel: 'Secondary', primaryType: 'S' }
      ],
      [
        'get the first one, if no SNMP Primary or managed',
        '7',
        { label: '192.168.0.70', managed: false, primaryLabel: '', primaryType: 'Q' }
      ]
    ]) (
      'getBestIpInterfaceForNode: %s',
      (title, nodeId, expected) => {
        const result = getBestIpInterfaceForNode(nodeId, nodeToIpInterfaceMap)
        expect(result).toEqual(expected)
      }
    )
  })
})
