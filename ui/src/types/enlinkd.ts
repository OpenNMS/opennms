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

// Wire shapes of the /api/v2/enlinkd/*_elems/{node} endpoints: what Enlinkd discovered about a node
// itself, as opposed to its links to neighbors. Every value is a display string formatted on the
// server, the same strings the legacy node page shows.

export interface LldpElement {
  lldpChassisId?: string | null
  lldpSysName?: string | null
  lldpCreateTime?: string | null
  lldpLastPollTime?: string | null
}

export interface CdpElement {
  cdpGlobalRun?: string | null
  cdpGlobalDeviceId?: string | null
  cdpGlobalDeviceIdFormat?: string | null
  cdpCreateTime?: string | null
  cdpLastPollTime?: string | null
}

export interface OspfElement {
  ospfRouterId?: string | null
  ospfVersionNumber?: number | null
  ospfAdminStat?: string | null
  ospfCreateTime?: string | null
  ospfLastPollTime?: string | null
}

export interface IsisElement {
  isisSysID?: string | null
  isisSysAdminState?: string | null
  isisCreateTime?: string | null
  isisLastPollTime?: string | null
}

// One per VLAN the bridge reports; vlan and vlanname are empty for the default bridge.
export interface BridgeElement {
  baseBridgeAddress?: string | null
  baseNumPorts?: number | null
  baseType?: string | null
  stpProtocolSpecification?: string | null
  stpPriority?: number | null
  stpDesignatedRoot?: string | null
  stpRootCost?: number | null
  stpRootPort?: number | null
  vlan?: number | null
  vlanname?: string | null
  bridgeNodeCreateTime?: string | null
  bridgeNodeLastPollTime?: string | null
}

// A node's elements; each protocol absent (or, for bridges, empty) when Enlinkd found none.
export interface NodeEnlinkdElements {
  lldp?: LldpElement
  cdp?: CdpElement
  ospf?: OspfElement
  isis?: IsisElement
  bridges: BridgeElement[]
}
