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

import { BridgeElement, CdpElement, IsisElement, LldpElement, OspfElement } from '@/types/enlinkd'

// What the Network tab's panels show for each Enlinkd element: the legacy node page's rows, with
// tidied labels. Kept apart from the components so the rules can be tested on their own.

export interface NodeDetailsField {
  label: string
  text: string
}

export interface BridgeGroup {
  title: string
  fields: NodeDetailsField[]
}

const EMPTY = '--'

const show = (value: unknown) => (value === null || value === undefined || value === '' ? EMPTY : String(value))

export const lldpFields = (lldp: LldpElement): NodeDetailsField[] => [
  { label: 'Chassis ID', text: show(lldp.lldpChassisId) },
  { label: 'Sys Name', text: show(lldp.lldpSysName) },
  { label: 'Last Poll Time', text: show(lldp.lldpLastPollTime) }
]

export const cdpFields = (cdp: CdpElement): NodeDetailsField[] => [
  { label: 'Global Device ID', text: show(cdp.cdpGlobalDeviceId) },
  { label: 'Global Run', text: show(cdp.cdpGlobalRun) },
  { label: 'Last Poll Time', text: show(cdp.cdpLastPollTime) }
]

// The legacy page runs these together as "enabled version:2".
export const ospfFields = (ospf: OspfElement): NodeDetailsField[] => {
  const status = ospf.ospfVersionNumber !== null && ospf.ospfVersionNumber !== undefined
    ? `${show(ospf.ospfAdminStat)} (version ${ospf.ospfVersionNumber})`
    : show(ospf.ospfAdminStat)

  return [
    { label: 'Router ID', text: show(ospf.ospfRouterId) },
    { label: 'Status', text: status },
    { label: 'Last Poll Time', text: show(ospf.ospfLastPollTime) }
  ]
}

export const isisFields = (isis: IsisElement): NodeDetailsField[] => [
  { label: 'Sys ID', text: show(isis.isisSysID) },
  { label: 'Admin State', text: show(isis.isisSysAdminState) },
  { label: 'Last Poll Time', text: show(isis.isisLastPollTime) }
]

const isSet = (value: string | null | undefined): value is string => !!value
const isPositive = (value: number | null | undefined): value is number => typeof value === 'number' && value > 0

/**
 * One group per bridge element (one per VLAN), as the legacy page's rows: the VLAN in the title,
 * the base address and type always, and only the spanning-tree values that are set -- the legacy
 * page leaves out empty strings and zero priority, root port and root cost.
 */
export const bridgeGroup = (bridge: BridgeElement): BridgeGroup => {
  const hasVlan = bridge.vlan !== null && bridge.vlan !== undefined
  const name = [
    isSet(bridge.vlanname) ? `Vlan ${bridge.vlanname}` : '',
    hasVlan ? `(vlanid ${bridge.vlan})` : 'Default'
  ].filter(Boolean).join(' ')
  const ports = bridge.baseNumPorts ?? 0

  const fields: NodeDetailsField[] = [
    { label: 'Base Address', text: show(bridge.baseBridgeAddress) },
    { label: 'Type', text: show(bridge.baseType) }
  ]

  if (isSet(bridge.stpProtocolSpecification)) {
    fields.push({ label: 'STP Protocol', text: bridge.stpProtocolSpecification })
  }

  if (isPositive(bridge.stpPriority)) {
    fields.push({ label: 'Priority', text: String(bridge.stpPriority) })
  }

  if (isSet(bridge.stpDesignatedRoot)) {
    fields.push({ label: 'Designated Root', text: bridge.stpDesignatedRoot })
  }

  if (isPositive(bridge.stpRootPort)) {
    fields.push({ label: 'Root Port', text: String(bridge.stpRootPort) })
  }

  if (isPositive(bridge.stpRootCost)) {
    fields.push({ label: 'Root Cost', text: String(bridge.stpRootCost) })
  }

  return { title: `${name} · ${ports} ${ports === 1 ? 'port' : 'ports'}`, fields }
}
