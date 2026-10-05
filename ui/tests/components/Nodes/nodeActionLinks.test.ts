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

import { createLinkGroups, createLinkItemsList, INFO_ITEM, linkGroups, linkItems, mapLink } from '@/components/Nodes/nodeActionLinks'
import { Node } from '@/types'
import { describe, expect, it } from 'vitest'

const node = { id: 42, label: 'srv-42', assetRecord: {}} as unknown as Node

// Most cases below are about a destination's shape rather than who may reach it, so they ask as
// the role that can reach everything. The gating itself is asserted in its own block.
const ADMIN = { isAdmin: true }

describe('nodeActionLinks', () => {
  // The single source of truth for the node action links, shared by the actions menu wherever
  // it is rendered.
  it('offers Node Link Details, then Edit in Requisition last', () => {
    expect(linkItems.slice(-2)).toEqual([
      { name: 'node-link', label: 'Node Link Details' },
      { name: 'edit-requisition', label: 'Edit in Requisition' }
    ])
  })

  it('still offers Site Status', () => {
    expect(linkItems.map(li => li.name)).toContain('siteStatus')
  })

  // Replaces the Surveillance Category Memberships panel's Edit button, which is why it points
  // at the same category edit page.
  it('offers Surveillance Categories between Hardware Inventory and Availability', () => {
    const names = linkItems.map(li => li.name)

    expect(linkItems.find(li => li.name === 'surveillance-categories')?.label)
      .toBe('Surveillance Categories')
    expect(names.indexOf('surveillance-categories')).toBe(names.indexOf('hardware') + 1)
    expect(names.indexOf('surveillance-categories')).toBe(names.indexOf('availability') - 1)
  })

  it('links Surveillance Categories to the category edit page for the node', () => {
    expect(mapLink('surveillance-categories', node, ADMIN)).toBe('admin/categories.htm?edit&node=42')
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

    expect(mapLink('schedule-outage', awkward, ADMIN))
      .toBe('admin/sched-outages/editoutage.jsp?newName=srv%20%26%20%231&addNew=true&nodeID=42')
  })

  // The legacy node page shows Update SNMP only when the node has an SNMP-primary interface,
  // and points it at that interface's address. A hardcoded 0.0.0.0 would have the form rewrite
  // SNMP config for an address that is not the node's.
  it('points Update SNMP Information at the SNMP-primary address', () => {
    expect(mapLink('updateSnmp', node, { ...ADMIN, snmpPrimaryIpAddress: '10.0.0.44' }))
      .toBe('admin/updateSnmp.jsp?node=42&ipaddr=10.0.0.44')
  })

  it('drops Update SNMP Information when no SNMP-primary address is known', () => {
    expect(mapLink('updateSnmp', node, ADMIN)).toBe('')
    expect(createLinkItemsList(node, ADMIN).map(li => li.label)).not.toContain('Update SNMP Information')
  })

  it('createLinkItemsList keeps Update SNMP Information when the address is known', () => {
    expect(createLinkItemsList(node, { ...ADMIN, snmpPrimaryIpAddress: '10.0.0.44' })).toContainEqual({
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

  // The server refuses these to anyone but ROLE_ADMIN: four fall under the /admin/** rule in
  // applicationContext-spring-security.xml, and element/rescan.jsp has an intercept-url of its
  // own. The legacy node page gates the same five together.
  describe('admin-only links', () => {
    const ADMIN_ONLY = ['surveillance-categories', 'rescan', 'admin', 'updateSnmp', 'schedule-outage']

    it.each(ADMIN_ONLY)('maps %s for an admin', (name) => {
      expect(mapLink(name, node, { ...ADMIN, snmpPrimaryIpAddress: '10.0.0.44' })).not.toBe('')
    })

    it.each(ADMIN_ONLY)('maps %s to nothing without the role', (name) => {
      expect(mapLink(name, node, { snmpPrimaryIpAddress: '10.0.0.44' })).toBe('')
    })

    // An absent context is the same as a non-admin one -- it must not fail open.
    it.each(ADMIN_ONLY)('maps %s to nothing when no context is given at all', (name) => {
      expect(mapLink(name, node)).toBe('')
    })

    it('drops them all from a non-admin list, keeping the rest', () => {
      const labels = createLinkItemsList(node, { snmpPrimaryIpAddress: '10.0.0.44' }).map(li => li.label)

      expect(labels).not.toContain('Surveillance Categories')
      expect(labels).not.toContain('Node Rescan')
      expect(labels).not.toContain('Admin / Node Management')
      expect(labels).not.toContain('Update SNMP Information')
      expect(labels).not.toContain('Schedule an Outage')
      expect(labels).toContain('Events')
      expect(labels).toContain('Node Link Details')
    })

    // Everything else stays reachable by a plain user; /** grants those to ROLE_USER.
    it.each([
      'events', 'alarms', 'view-outages', 'assets', 'metadata',
      'hardware', 'availability', 'graphs', 'topology', 'node-link'
    ])('leaves %s ungated', (name) => {
      expect(mapLink(name, node)).not.toBe('')
    })
  })

  // As the legacy node page: only for a node in its requisition, and only for admin or provision.
  describe('Edit in Requisition', () => {
    const requisitioned = { ...node, foreignSource: 'Demo Stores', foreignId: 'AL/pelham#1' } as unknown as Node
    const allowed = { canEditRequisitions: true, existsInRequisition: true }

    it('links to the node in the requisition editor, encoding the foreign source and id', () => {
      expect(mapLink('edit-requisition', requisitioned, allowed))
        .toBe('admin/ng-requisitions/index.jsp#/requisitions/Demo%20Stores/nodes/AL%2Fpelham%231')
    })

    it.each([
      ['a user who cannot edit requisitions', { ...allowed, canEditRequisitions: false }],
      ['a node not in its requisition', { ...allowed, existsInRequisition: false }],
      ['a caller that does not say (the node list)', {}]
    ])('is left out for %s', (_case, context) => {
      expect(mapLink('edit-requisition', requisitioned, context)).toBe('')
    })

    it('is left out for a node with no foreign source', () => {
      expect(mapLink('edit-requisition', node, allowed)).toBe('')
    })

    // Unlike the admin-only set: a provision user who is not an admin still gets it.
    it('does not need admin', () => {
      expect(mapLink('edit-requisition', requisitioned, { ...allowed, isAdmin: false })).not.toBe('')
    })

    it('comes last in createLinkItemsList', () => {
      const items = createLinkItemsList(requisitioned, { ...allowed, isAdmin: true })

      expect(items[items.length - 1].label).toBe('Edit in Requisition')
    })
  })

  // The links row: the same actions as the menu, grouped.
  describe('link groups', () => {
    const groupLabels = (groups: { label: string, items: { label: string }[] }[]) =>
      groups.map(g => [g.label, g.items.map(i => i.label)])

    it('has the four menus, in order', () => {
      expect(linkGroups.map(g => g.label)).toEqual(['Inventory', 'Monitoring', 'Graphs', 'Admin'])
    })

    // So the row and the actions menu offer the same set.
    it('holds every action link exactly once, plus Info', () => {
      const grouped = linkGroups.flatMap(g => g.items.map(i => i.name))

      expect(grouped.filter(n => n !== INFO_ITEM).sort()).toEqual(linkItems.map(li => li.name).sort())
      expect(new Set(grouped).size).toBe(grouped.length)
    })

    it('resolves every link for an admin, with a building, SNMP primary and requisition', () => {
      const full = { ...node, foreignSource: 'fs', foreignId: 'fid', assetRecord: { building: 'HQ' }} as unknown as Node
      const groups = createLinkGroups(full, {
        isAdmin: true, snmpPrimaryIpAddress: '10.0.0.44', canEditRequisitions: true, existsInRequisition: true
      })

      expect(groupLabels(groups)).toEqual([
        ['Inventory', ['Info', 'Assets', 'Metadata', 'Hardware Inventory', 'Surveillance Categories', 'Node Link Details']],
        ['Monitoring', ['Alarms', 'Events', 'Outages', 'Availability', 'Site Status']],
        ['Graphs', ['Resource Graphs', 'Topology Map']],
        ['Admin', ['Node Rescan', 'Update SNMP Information', 'Admin / Node Management', 'Schedule an Outage', 'Edit in Requisition']]
      ])
      expect(groups[1].items.find(i => i.label === 'Alarms')?.link).toBe(mapLink('alarms', full))
      expect(groups[0].items[0].link).toBe('')
    })

    it('drops the links a node or user cannot have, and an Admin menu left empty', () => {
      const groups = createLinkGroups(node, { isAdmin: false })

      expect(groupLabels(groups)).toEqual([
        ['Inventory', ['Info', 'Assets', 'Metadata', 'Hardware Inventory', 'Node Link Details']],
        ['Monitoring', ['Alarms', 'Events', 'Outages', 'Availability']],
        ['Graphs', ['Resource Graphs', 'Topology Map']]
      ])
    })
  })
})
