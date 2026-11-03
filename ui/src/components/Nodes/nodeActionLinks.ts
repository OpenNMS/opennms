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

import { nodeOutageListLink } from '@/lib/linkUtils'
import { IpInterface, Node } from '@/types'

// The node action links, shared by every place the actions menu is rendered: the Node Details
// title row and each row of the node list.
//
// Intentionally left out: the extra links the legacy node page appends after these (`navEntries`
// in element/node.jsp). Those come from ConditionalPageNavEntry services registered for
// "Page=node": any OSGi bundle can register one with `registration.export=true`, and
// OnmsOSGiBridgeActivator copies it into the core ServiceRegistry, where the page finds it and
// asks it, per request and node, whether to show. They are code, not user configuration. The only
// one in this codebase is TopoMapNavEntry ("View in Topology", the topology app's blueprint.xml),
// whose URL is the same as the 'topology' link below. Supporting third-party ones would need a
// REST endpoint that evaluates them on the server; that can come later if needed.
export const linkItems = [
  { name: 'events', label: 'Events' },
  { name: 'alarms', label: 'Alarms' },
  { name: 'view-outages', label: 'Outages' },
  { name: 'assets', label: 'Assets' },
  { name: 'metadata', label: 'Metadata' },
  { name: 'hardware', label: 'Hardware Inventory' },
  { name: 'surveillance-categories', label: 'Surveillance Categories' },
  { name: 'availability', label: 'Availability' },
  { name: 'siteStatus', label: 'Site Status' },
  { name: 'graphs', label: 'Resource Graphs' },
  { name: 'rescan', label: 'Node Rescan' },
  { name: 'admin', label: 'Admin / Node Management' },
  { name: 'updateSnmp', label: 'Update SNMP Information' },
  { name: 'schedule-outage', label: 'Schedule an Outage' },
  { name: 'topology', label: 'View Topology Map' },
  { name: 'node-link', label: 'Node Link Details' },
  { name: 'edit-requisition', label: 'Edit in Requisition' }
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
  // Gates the admin-only destinations below. A boolean rather than a role list so the role
  // vocabulary stays in useRole, which already owns it.
  isAdmin?: boolean
  // Edit in Requisition: whether the user may edit requisitions (admin or provision), and whether
  // this node is in one. Both default to false, so a caller that cannot tell -- the node list,
  // which would need a request per row to find out -- leaves the action out.
  canEditRequisitions?: boolean
  existsInRequisition?: boolean
}

// Destinations the server refuses to anyone but ROLE_ADMIN, so offering them to everyone else is
// offering an access denial. Four sit under /admin/**, which
// applicationContext-spring-security.xml pins to ROLE_ADMIN wholesale; rescan is under /element/
// but has an intercept-url rule of its own. The legacy node page gates the same set
// (element/node.jsp wraps them in <c:if test="${model.admin}">).
//
// Note updateSnmp is plain ROLE_ADMIN here, NOT useRole's snmpRole (ROLE_ADMIN or
// ROLE_PROVISION) -- admin/updateSnmp.jsp is covered by the /admin/** rule like the rest.
const ADMIN_ONLY_LINKS = new Set([
  'surveillance-categories',
  'rescan',
  'admin',
  'updateSnmp',
  'schedule-outage'
])

export const mapLink = (name: string, node: Node, context: NodeActionLinkContext = {}) => {
  if (ADMIN_ONLY_LINKS.has(name) && !context.isAdmin) {
    return ''
  }

  switch (name) {
    case 'events':
      return `event/list?filter=node%3D${node.id}`
    case 'alarms':
      return `alarm/list.htm?filter=node%3D${node.id}`
    case 'view-outages':
      // Relative, like every link here: mapLink's callers prepend baseHref themselves. No
      // outtype, so the legacy list applies its own default.
      return nodeOutageListLink('', node.id)
    case 'assets':
      return `asset/modify.jsp?node=${node.id}`
    case 'metadata':
      return `element/node-metadata.jsp?node=${node.id}`
    case 'hardware':
      return `hardware/list.jsp?node=${node.id}`
    // The category edit page, which is where the Surveillance Category Memberships panel's Edit
    // button used to go -- and which that button was gated on admin for, as is this.
    case 'surveillance-categories':
      return `admin/categories.htm?edit&node=${node.id}`
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
    // As the legacy node page: offered only for a node in its requisition, to admin and provision
    // users. Always the plain node editor; the page's vertical-layout cookie variant is not carried
    // over.
    case 'edit-requisition': {
      if (!context.canEditRequisitions || !context.existsInRequisition || !node.foreignSource || !node.foreignId) {
        return ''
      }

      return `admin/ng-requisitions/index.jsp#/requisitions/${encodeURIComponent(node.foreignSource)}/nodes/${encodeURIComponent(node.foreignId)}`
    }
    default: return ''
  }
}

export const createLinkItemsList = (node: Node, context: NodeActionLinkContext = {}) => {
  return linkItems.map(li => ({
    name: li.name,
    label: li.label,
    link: mapLink(li.name, node, context)
  }))
    .filter(li => li.link)
}

// The Node Details links row (NodeDetailsLinks): the same actions, grouped under top-level menus.
// Every link item appears in exactly one group, built by the same mapLink, so the row and the
// actions menu cannot disagree about where a link goes or who may see it. `info` is not a link:
// it opens the node info dialog, which the component handles.
export const INFO_ITEM = 'info'

export interface NodeLinkGroup {
  label: string
  items: { name: string, label: string }[]
}

export const linkGroups: NodeLinkGroup[] = [
  {
    label: 'Inventory',
    items: [
      { name: INFO_ITEM, label: 'Info' },
      { name: 'assets', label: 'Assets' },
      { name: 'metadata', label: 'Metadata' },
      { name: 'hardware', label: 'Hardware Inventory' },
      { name: 'surveillance-categories', label: 'Surveillance Categories' },
      { name: 'node-link', label: 'Node Link Details' }
    ]
  },
  {
    label: 'Monitoring',
    items: [
      { name: 'alarms', label: 'Alarms' },
      { name: 'events', label: 'Events' },
      { name: 'view-outages', label: 'Outages' },
      { name: 'availability', label: 'Availability' },
      { name: 'siteStatus', label: 'Site Status' }
    ]
  },
  {
    label: 'Graphs',
    items: [
      { name: 'graphs', label: 'Resource Graphs' },
      { name: 'topology', label: 'Topology Map' }
    ]
  },
  {
    label: 'Admin',
    items: [
      { name: 'rescan', label: 'Node Rescan' },
      { name: 'updateSnmp', label: 'Update SNMP Information' },
      { name: 'admin', label: 'Admin / Node Management' },
      { name: 'schedule-outage', label: 'Schedule an Outage' },
      { name: 'edit-requisition', label: 'Edit in Requisition' }
    ]
  }
]

export interface NodeLinkGroupItem {
  name: string
  label: string
  // Empty for the info item, which is not a link.
  link: string
}

/**
 * The groups with each link resolved for this node and user. A link mapLink leaves out (not
 * allowed, or nothing to link to) is dropped, and so is a group left with nothing in it -- Admin,
 * for a user who is not an admin.
 */
export const createLinkGroups = (node: Node, context: NodeActionLinkContext = {}): { label: string, items: NodeLinkGroupItem[] }[] =>
  linkGroups
    .map(group => ({
      label: group.label,
      items: group.items
        .map(item => ({ ...item, link: item.name === INFO_ITEM ? '' : mapLink(item.name, node, context) }))
        .filter(item => item.name === INFO_ITEM || item.link)
    }))
    .filter(group => group.items.length > 0)

/**
 * Whether opening the asset editor should be confirmed first, as the legacy node page's
 * confirmAssetEdit() decided: for a node from a requisition, whose next sync or rescan rolls the
 * edits back -- except for a read-only user, who cannot save them anyway.
 */
export const needsAssetEditConfirm = (node: Node, readOnly: boolean): boolean => !!node.foreignSource && !readOnly
