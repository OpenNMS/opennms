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
import { bridgeGroup, cdpFields, isisFields, lldpFields, ospfFields } from '@/components/Nodes/nodeEnlinkdFields'

const text = (fields: { label: string, text: string }[]) => Object.fromEntries(fields.map(f => [f.label, f.text]))

describe('nodeEnlinkdFields', () => {
  it('lldp: chassis id, sys name and last poll time', () => {
    expect(lldpFields({ lldpChassisId: '00:11:22:33:44:55', lldpSysName: 'sw1', lldpLastPollTime: 'T1' }))
      .toEqual([
        { label: 'Chassis ID', text: '00:11:22:33:44:55' },
        { label: 'Sys Name', text: 'sw1' },
        { label: 'Last Poll Time', text: 'T1' }
      ])
  })

  it('shows -- for a missing value', () => {
    expect(text(lldpFields({ lldpChassisId: null }))['Chassis ID']).toBe('--')
  })

  it('cdp: global device id, global run and last poll time', () => {
    expect(text(cdpFields({ cdpGlobalDeviceId: 'r1', cdpGlobalRun: 'true', cdpLastPollTime: 'T1' })))
      .toEqual({ 'Global Device ID': 'r1', 'Global Run': 'true', 'Last Poll Time': 'T1' })
  })

  it('ospf: puts the version with the admin status', () => {
    expect(text(ospfFields({ ospfRouterId: '10.0.0.1', ospfAdminStat: 'enabled', ospfVersionNumber: 2, ospfLastPollTime: 'T1' })))
      .toEqual({ 'Router ID': '10.0.0.1', 'Status': 'enabled (version 2)', 'Last Poll Time': 'T1' })
  })

  it('ospf: leaves the version out when there is none', () => {
    expect(text(ospfFields({ ospfAdminStat: 'disabled' })).Status).toBe('disabled')
  })

  it('isis: sys id, admin state and last poll time', () => {
    expect(text(isisFields({ isisSysID: '0000.0000.0001', isisSysAdminState: 'on', isisLastPollTime: 'T1' })))
      .toEqual({ 'Sys ID': '0000.0000.0001', 'Admin State': 'on', 'Last Poll Time': 'T1' })
  })

  describe('bridgeGroup', () => {
    const full = {
      baseBridgeAddress: '00:11:22:33:44:55',
      baseNumPorts: 24,
      baseType: 'transparent-only',
      stpProtocolSpecification: 'ieee8021d',
      stpPriority: 32768,
      stpDesignatedRoot: '80000011223344aa',
      stpRootPort: 3,
      stpRootCost: 19,
      vlan: 1,
      vlanname: 'default'
    }

    it('names the VLAN and its ports, and lists every value that is set', () => {
      const group = bridgeGroup(full)

      expect(group.title).toBe('Vlan default (vlanid 1) · 24 ports')
      expect(text(group.fields)).toEqual({
        'Base Address': '00:11:22:33:44:55',
        'Type': 'transparent-only',
        'STP Protocol': 'ieee8021d',
        'Priority': '32768',
        'Designated Root': '80000011223344aa',
        'Root Port': '3',
        'Root Cost': '19'
      })
    })

    // As the legacy page.
    it('calls a bridge with no VLAN the default one', () => {
      expect(bridgeGroup({ baseNumPorts: 1 }).title).toBe('Default · 1 port')
    })

    it('shows the VLAN id without a name when there is none', () => {
      expect(bridgeGroup({ vlan: 7, baseNumPorts: 2 }).title).toBe('(vlanid 7) · 2 ports')
    })

    // The legacy page leaves out empty spanning-tree values and zero priority, root port and cost.
    it('leaves out spanning-tree values that are not set', () => {
      const group = bridgeGroup({ ...full, stpProtocolSpecification: '', stpPriority: 0, stpDesignatedRoot: null, stpRootPort: 0, stpRootCost: 0 })

      expect(group.fields.map(f => f.label)).toEqual(['Base Address', 'Type'])
    })
  })
})
