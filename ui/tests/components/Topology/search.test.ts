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

import { describe, it, expect } from 'vitest'
import { searchFieldLabel, searchNodes, searchTopology } from '@/components/Topology/search'
import type { CanvasNode } from '@/types/topology'

const node = (id: string, label: string, extra: Partial<CanvasNode> = {}): CanvasNode =>
  ({ id, label, x: 0, y: 0, ...extra })

const nodes: CanvasNode[] = [
  node('a', 'core-01', { nodeId: 1, properties: { ipAddress: '10.0.0.1' }}),
  node('b', 'dist-02', { nodeId: 22, properties: { ipAddress: '10.0.0.22' }}),
  node('c', 'access-03', { nodeId: 3, properties: { ipAddress: '192.168.1.3' }}),
  node('d', 'Payroll', { properties: { applicationId: '77', vertexType: 'Application' }})
]

const labels = (query: string, limit = 12) =>
  searchNodes(nodes, query, limit).map(m => m.node.label)

describe('searchNodes', () => {
  it('matches on label', () => {
    expect(labels('dist')).toEqual(['dist-02'])
  })

  it('matches on an IP address the label does not contain', () => {
    expect(labels('192.168')).toEqual(['access-03'])
  })

  it('reports which field matched, so the suggestion can show it', () => {
    const [match] = searchNodes(nodes, '192.168', 12)
    expect(match.matchedOn).toEqual({ key: 'ipAddress', value: '192.168.1.3' })
  })

  it('leaves matchedOn unset for a label hit', () => {
    expect(searchNodes(nodes, 'dist', 12)[0].matchedOn).toBeUndefined()
  })

  it('matches any provider property, not just the ones enlinkd sets', () => {
    const [match] = searchNodes(nodes, '77', 12)
    expect(match.node.label).toBe('Payroll')
    expect(match.matchedOn).toEqual({ key: 'applicationId', value: '77' })
  })

  it('matches on node id', () => {
    const [match] = searchNodes(nodes, '22', 12)
    // 'dist-02' also has 22 in its IP; the node id is checked first.
    expect(match.matchedOn).toEqual({ key: 'nodeId', value: '22' })
  })

  it('sorts label hits ahead of property hits', () => {
    // '10.0.0.1' is in two IPs; 'core-01' has no '10' in its label, so both are
    // property hits. Add a label containing '10' to prove ordering.
    const withLabelHit = [...nodes, node('e', 'switch-10')]
    expect(searchNodes(withLabelHit, '10', 12)[0].node.label).toBe('switch-10')
  })

  it('is case-insensitive', () => {
    expect(labels('PAYROLL')).toEqual(['Payroll'])
  })

  it('returns the first N nodes for an empty query, as the dropdown does on focus', () => {
    expect(labels('  ', 2)).toEqual(['core-01', 'dist-02'])
  })

  it('honors the limit', () => {
    expect(labels('0', 1)).toHaveLength(1)
  })

  it('does not match a node twice', () => {
    // 'core-01' matches both its label and its IP's '0'; it must appear once.
    const found = searchNodes(nodes, '0', 12).filter(m => m.node.id === 'a')
    expect(found).toHaveLength(1)
  })
})

describe('searchFieldLabel', () => {
  it('uses the short form for known keys', () => {
    expect(searchFieldLabel('ipAddress')).toBe('IP')
    expect(searchFieldLabel('nodeId')).toBe('Node ID')
  })

  it('humanizes an unknown provider key', () => {
    expect(searchFieldLabel('applicationId')).toBe('Application Id')
  })
})

describe('searchTopology', () => {
  // core-01 and dist-02 are in Core, access-03 is not. Node 9 is in Core too but
  // absent from the graph, standing in for a node with no discovered links.
  const cats: Record<number, string[]> = {
    1: ['Core', 'Routers'],
    22: ['Core'],
    3: ['Access'],
    9: ['Core']
  }

  it('offers a category as a hit, resolved to its members in this graph', () => {
    const [match] = searchTopology(nodes, cats, 'core', 12)
    expect(match).toEqual({ kind: 'category', label: 'Core', canvasIds: ['a', 'b'] })
  })

  it('does not include category members that are not vertices of this graph', () => {
    const [match] = searchTopology(nodes, cats, 'core', 12)
    // Node 9 is in Core but has no vertex, so it cannot be selected.
    expect(match.kind === 'category' && match.canvasIds).not.toContain('9')
  })

  it('sorts categories ahead of node hits', () => {
    // 'core' matches the Core category and the label 'core-01'.
    expect(searchTopology(nodes, cats, 'core', 12).map(m => m.kind))
      .toEqual(['category', 'node'])
  })

  it('sorts multiple categories by name', () => {
    const found = searchTopology(nodes, { 1: ['Zebra', 'Alpha'] }, 'a', 12)
      .filter(m => m.kind === 'category')
      .map(m => m.label)
    expect(found).toEqual(['Alpha', 'Zebra'])
  })

  it('falls back to node hits when no category matches', () => {
    expect(searchTopology(nodes, cats, 'dist', 12)).toEqual([
      { kind: 'node', label: 'dist-02', node: nodes[1] }
    ])
  })

  it('needs no categories at all', () => {
    expect(searchTopology(nodes, {}, 'dist', 12).map(m => m.label)).toEqual(['dist-02'])
  })

  it('honors the limit across both kinds', () => {
    expect(searchTopology(nodes, cats, 'core', 1)).toHaveLength(1)
  })
})

// A one-letter query on a category-heavy install matched more categories than
// the limit, and every node hit was sliced away.
describe('searchTopology crowding', () => {
  it('always leaves room for node hits', () => {
    const many: Record<number, string[]> = {
      1: Array.from({ length: 20 }, (_, i) => `Category-e-${i}`)
    }
    const found = searchTopology(nodes, many, 'e', 12)
    // 20 categories match, but they may take at most half the list.
    expect(found.filter(m => m.kind === 'category')).toHaveLength(6)
    expect(found.some(m => m.kind === 'node')).toBe(true)
  })

  it('still fills with nodes when few categories match', () => {
    const found = searchTopology(nodes, { 1: ['Core'] }, 'core', 12)
    expect(found[0].kind).toBe('category')
    expect(found.filter(m => m.kind === 'node').length).toBeGreaterThan(0)
  })
})
