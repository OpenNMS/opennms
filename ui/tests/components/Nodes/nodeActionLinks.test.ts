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

import { createLinkItemsList, linkItems, mapLink } from '@/components/Nodes/nodeActionLinks'
import { Node } from '@/types'
import { describe, expect, it } from 'vitest'

const node = { id: 42, label: 'srv-42', assetRecord: {}} as unknown as Node

describe('nodeActionLinks', () => {
  // The single source of truth for the node action links, shared by the actions menu wherever
  // it is rendered.
  it('offers Node Link Details last', () => {
    expect(linkItems[linkItems.length - 1]).toEqual({ name: 'node-link', label: 'Node Link Details' })
  })

  it('still offers Site Status', () => {
    expect(linkItems.map(li => li.name)).toContain('siteStatus')
  })

  // Two entries pointed at the linked-node page before these lists were merged, under
  // different labels. Only one should reach the menu.
  it('offers the linked-node page exactly once', () => {
    expect(linkItems.filter(li => mapLink(li.name, node).startsWith('element/linkednode.jsp'))).toHaveLength(1)
  })

  it('maps node-link to the linked node page', () => {
    expect(mapLink('node-link', node)).toBe('element/linkednode.jsp?node=42')
  })

  // A label with & or # would otherwise corrupt the query string, losing addNew and nodeID.
  it('url-encodes the node label in the schedule-outage link', () => {
    const awkward = { ...node, id: 42, label: 'srv & #1' } as unknown as Node

    expect(mapLink('schedule-outage', awkward))
      .toBe('admin/sched-outages/editoutage.jsp?newName=srv%20%26%20%231&addNew=true&nodeID=42')
  })

  // The legacy node page shows Update SNMP only when the node has an SNMP-primary interface,
  // and points it at that interface's address. A hardcoded 0.0.0.0 would have the form rewrite
  // SNMP config for an address that is not the node's.
  it('points Update SNMP Information at the SNMP-primary address', () => {
    expect(mapLink('updateSnmp', node, { snmpPrimaryIpAddress: '10.0.0.44' }))
      .toBe('admin/updateSnmp.jsp?node=42&ipaddr=10.0.0.44')
  })

  it('drops Update SNMP Information when no SNMP-primary address is known', () => {
    expect(mapLink('updateSnmp', node)).toBe('')
    expect(createLinkItemsList(node).map(li => li.label)).not.toContain('Update SNMP Information')
  })

  it('createLinkItemsList keeps Update SNMP Information when the address is known', () => {
    expect(createLinkItemsList(node, { snmpPrimaryIpAddress: '10.0.0.44' })).toContainEqual({
      label: 'Update SNMP Information',
      link: 'admin/updateSnmp.jsp?node=42&ipaddr=10.0.0.44'
    })
  })

  it('createLinkItemsList drops Site Status for a node with no building', () => {
    expect(createLinkItemsList(node).map(li => li.label)).not.toContain('Site Status')
  })

  it('createLinkItemsList keeps Site Status for a node with a building', () => {
    const withBuilding = { ...node, assetRecord: { building: 'HQ 1' }} as unknown as Node

    expect(createLinkItemsList(withBuilding)).toContainEqual({
      label: 'Site Status',
      link: 'siteStatusView.htm?statusSite=HQ%201'
    })
  })
})
