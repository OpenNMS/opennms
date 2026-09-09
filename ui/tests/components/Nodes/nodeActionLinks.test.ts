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

  // 'node-link-details' pointed at the same page as 'node-link', so only one of the two
  // survived the merge into this list.
  it('has no leftover duplicate of the linked-node action', () => {
    expect(linkItems.map(li => li.name)).not.toContain('node-link-details')
    expect(linkItems.filter(li => mapLink(li.name, node).startsWith('element/linkednode.jsp'))).toHaveLength(1)
  })

  it('maps node-link to the linked node page', () => {
    expect(mapLink('node-link', node)).toBe('element/linkednode.jsp?node=42')
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
