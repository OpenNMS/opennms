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

// One place that knows how the Vue UI addresses the legacy (JSP/htm) pages it still links out
// to. These were spelled out at each call site, and the copies had drifted: the node list built
// an interface link without encoding the address, which corrupts an IPv6 one (a zone index's
// '%' reads as the start of a percent-escape).
//
// Lives in lib rather than under a feature because the callers span features -- the node list,
// the node details panels and its interface tables, and device config backup. Every builder
// takes baseHref (from menuStore.mainMenu) rather than reading a store itself, so it stays
// usable from components, hooks and plain functions alike.
//
// Will need to replace with the Vue pages once they are implemented.
export const OUTAGE_LIST_PATH = 'outage/list.htm'
export const OUTAGE_DETAIL_PATH = 'outage/detail.htm'
export const INTERFACE_PATH = 'element/interface.jsp'
export const SNMP_INTERFACE_PATH = 'element/snmpinterface.jsp'
export const SERVICE_PATH = 'element/service.jsp'

// outtype=both makes the legacy outage list show current AND resolved outages, matching what
// the Recent Outages panel shows.
export const OUTAGE_LIST_TYPE_BOTH = 'both'

/**
 * The node detail page. Unlike every other link here the path is not a constant: it comes from
 * the menu configuration as `baseNodeUrl`, which already ends in whatever separator it needs
 * (e.g. 'element/node.jsp?node='), so the id is appended directly.
 */
export const nodeLink = (baseHref: string, baseNodeUrl: string, nodeId: string | number) =>
  `${baseHref}${baseNodeUrl}${nodeId}`

/**
 * The '?node=&intf=' query the interface and service pages share. Both take the interface by
 * address rather than by id, so the address is encoded -- an IPv6 address is full of reserved
 * characters, a zone index ('fe80::1%eth0') especially, whose '%' would otherwise start an
 * escape sequence and corrupt the parameter.
 */
export const nodeAndInterfaceQuery = (nodeId: string | number, ipAddress: string) =>
  `?node=${nodeId}&intf=${encodeURIComponent(ipAddress)}`

/** The interface page for one address on a node. */
export const interfaceLink = (baseHref: string, nodeId: string | number, ipAddress: string) =>
  `${baseHref}${INTERFACE_PATH}${nodeAndInterfaceQuery(nodeId, ipAddress)}`

/**
 * The same interface page addressed by IP interface id instead of node + address. Nothing needs
 * encoding here, which is the reason to prefer it where an id is in hand.
 */
export const interfaceByIdLink = (baseHref: string, ipInterfaceId: string | number) =>
  `${baseHref}${INTERFACE_PATH}?ipinterfaceid=${ipInterfaceId}`

/** The SNMP interface page, which takes an ifIndex rather than an address. */
export const snmpInterfaceLink = (baseHref: string, nodeId: string | number, ifIndex: string | number) =>
  `${baseHref}${SNMP_INTERFACE_PATH}?node=${nodeId}&ifindex=${ifIndex}`

/** The service page for one service on one address of a node. */
export const serviceLink = (
  baseHref: string,
  nodeId: string | number,
  ipAddress: string,
  serviceId: string | number
) => `${baseHref}${SERVICE_PATH}${nodeAndInterfaceQuery(nodeId, ipAddress)}&service=${serviceId}`

/** One outage's detail page. */
export const outageDetailLink = (baseHref: string, outageId: string | number) =>
  `${baseHref}${OUTAGE_DETAIL_PATH}?id=${outageId}`

/**
 * The outage list filtered to one node. `outageType` is optional because the two callers differ
 * deliberately: the Recent Outages panel passes 'both' so the legacy list shows what the panel
 * shows, while the node actions menu links to the list's own default.
 */
export const nodeOutageListLink = (baseHref: string, nodeId: string | number, outageType?: string) =>
  `${baseHref}${OUTAGE_LIST_PATH}?filter=node%3D${nodeId}${outageType ? `&outtype=${outageType}` : ''}`
