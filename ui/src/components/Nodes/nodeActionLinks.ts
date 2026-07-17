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

import { Node } from '@/types'

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
  { name: 'node-link-details', label: 'Node Link Details' },
  { name: 'updateSnmp', label: 'Update SNMP Information' },
  { name: 'schedule-outage', label: 'Schedule an Outage' },
  { name: 'topology', label: 'View Topology Map' }
]

export const mapLink = (name: string, node: Node) => {
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
    case 'node-link-details':
      return `element/linkednode.jsp?node=${node.id}`
    case 'updateSnmp':
      // TODO: Get IP Address
      return `admin/updateSnmp.jsp?node=${node.id}&ipaddr=0.0.0.0`
    case 'schedule-outage':
      return `admin/sched-outages/editoutage.jsp?newName=${node.label}&addNew=true&nodeID=${node.id}`
    case 'topology':
      return `topology?provider=Enhanced+Linkd&szl=1&focus-vertices=${node.id}`
    default: return ''
  }
}

export const createLinkItemsList = (node: Node) => {
  return linkItems.map(li => ({
    label: li.label,
    link: mapLink(li.name, node)
  }))
    .filter(li => li.link)
}
