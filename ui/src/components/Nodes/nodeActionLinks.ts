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

import { IpInterface, Node } from '@/types'

// The node action links, shared by every place the actions menu is rendered: the Node Details
// title row and each row of the node list.
export const linkItems = [
  { name: 'events', label: 'Events' },
  { name: 'alarms', label: 'Alarms' },
  { name: 'view-outages', label: 'Outages' },
  { name: 'assets', label: 'Assets' },
  { name: 'metadata', label: 'Metadata' },
  { name: 'hardware', label: 'Hardware Inventory' },
  { name: 'availability', label: 'Availability' },
  { name: 'siteStatus', label: 'Site Status' },
  { name: 'graphs', label: 'Resource Graphs' },
  { name: 'rescan', label: 'Node Rescan' },
  { name: 'admin', label: 'Admin / Node Management' },
  { name: 'updateSnmp', label: 'Update SNMP Information' },
  { name: 'schedule-outage', label: 'Schedule an Outage' },
  { name: 'topology', label: 'View Topology Map' },
  { name: 'node-link', label: 'Node Link Details' }
]

/**
 * The address the Update SNMP action targets: the node's SNMP-primary interface, matching the
 * legacy node page's `model.snmpPrimaryIntf`. Undefined when the node has none, in which case
 * the action is not offered at all.
 */
export const getSnmpPrimaryIpAddress = (ipInterfaces: IpInterface[]): string | undefined =>
  ipInterfaces.find(ipInterface => ipInterface.snmpPrimary === 'P')?.ipAddress

// Data a link needs that the node payload does not carry. /api/v2/nodes sends no interfaces
// (OnmsNode.getPrimaryInterface is @Transient @JsonIgnore), so the caller supplies the
// SNMP-primary address from the interface data it has.
export interface NodeActionLinkContext {
  snmpPrimaryIpAddress?: string
}

export const mapLink = (name: string, node: Node, context: NodeActionLinkContext = {}) => {
  switch (name) {
    case 'events':
      return `event/list?filter=node%3D${node.id}`
    case 'alarms':
      return `alarm/list.htm?filter=node%3D${node.id}`
    case 'view-outages':
      return `outage/list.htm?filter=node%3D${node.id}`
    case 'assets':
      return `asset/modify.jsp?node=${node.id}`
    case 'metadata':
      return `element/node-metadata.jsp?node=${node.id}`
    case 'hardware':
      return `hardware/list.jsp?node=${node.id}`
    case 'availability':
      return `element/availability.jsp?node=${node.id}`
    case 'siteStatus': {
      if (node.assetRecord?.building && node.assetRecord.building.length > 0) {
        const encodedBuilding = encodeURIComponent(node.assetRecord.building)
        return `siteStatusView.htm?statusSite=${encodedBuilding}`
      }
      return ''
    }
    case 'graphs':
      return `graph/chooseresource.jsp?node=${node.id}&reports=all`
    case 'rescan':
      return `element/rescan.jsp?node=${node.id}`
    case 'admin':
      return `admin/nodemanagement/index.jsp?node=${node.id}`
    case 'updateSnmp': {
      // The legacy node page shows this only for a node with an SNMP-primary interface, and
      // points it at that address. Without one there is nothing safe to link to: the form
      // would rewrite SNMP configuration for whatever address it was handed.
      if (!context.snmpPrimaryIpAddress) {
        return ''
      }

      return `admin/updateSnmp.jsp?node=${node.id}&ipaddr=${encodeURIComponent(context.snmpPrimaryIpAddress)}`
    }
    case 'schedule-outage':
      // The label goes into a query parameter, so it has to be encoded: an & or # in it would
      // otherwise swallow the parameters that follow.
      return `admin/sched-outages/editoutage.jsp?newName=${encodeURIComponent(node.label)}&addNew=true&nodeID=${node.id}`
    case 'topology':
      return `topology?provider=Enhanced+Linkd&szl=1&focus-vertices=${node.id}`
    // The legacy node page links to this as "View Node Link Detailed Info".
    case 'node-link':
      return `element/linkednode.jsp?node=${node.id}`
    default: return ''
  }
}

export const createLinkItemsList = (node: Node, context: NodeActionLinkContext = {}) => {
  return linkItems.map(li => ({
    label: li.label,
    link: mapLink(li.name, node, context)
  }))
    .filter(li => li.link)
}
