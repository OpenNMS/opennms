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
import { focusSubgraph, highestDegreeVertexId } from '@/components/Topology/focus'
import type { CanvasLink, CanvasNode, DiscoveredGraph } from '@/types/topology'

// core -- d1, d2 ; d1 -- a1 ; d2 -- a2 (a small two-tier tree)
const node = (id: string): CanvasNode => ({ id, label: id, x: 0, y: 0 })
const edge = (s: string, t: string): CanvasLink => ({ id: `${s}|${t}`, sourceId: s, targetId: t, origin: 'discovered' })
const graph: DiscoveredGraph = {
  source: { container: 'enlinkd', namespace: 'nodes:Layer2' },
  label: 'Layer2',
  nodes: ['core', 'd1', 'd2', 'a1', 'a2'].map(node),
  links: [edge('core', 'd1'), edge('core', 'd2'), edge('d1', 'a1'), edge('d2', 'a2')]
}

const ids = (g: DiscoveredGraph) => g.nodes.map(n => n.id).sort()

describe('focusSubgraph', () => {
  it('returns the whole graph when focus is null', () => {
    expect(focusSubgraph(graph, null, 2)).toBe(graph)
  })

  it('returns the whole graph when the focus id is not present', () => {
    expect(focusSubgraph(graph, 'ghost', 2)).toBe(graph)
  })

  it('hops=0 yields just the focus node and no edges', () => {
    const out = focusSubgraph(graph, 'core', 0)
    expect(ids(out)).toEqual(['core'])
    expect(out.links).toEqual([])
  })

  it('hops=1 yields the focus node and its direct neighbors', () => {
    const out = focusSubgraph(graph, 'core', 1)
    expect(ids(out)).toEqual(['core', 'd1', 'd2'])
    // only edges fully inside the kept set
    expect(out.links.map(e => e.id).sort()).toEqual(['core|d1', 'core|d2'])
  })

  it('hops=2 reaches the leaves', () => {
    const out = focusSubgraph(graph, 'core', 2)
    expect(ids(out)).toEqual(['a1', 'a2', 'core', 'd1', 'd2'])
    expect(out.links).toHaveLength(4)
  })

  it('focusing on a leaf walks back up', () => {
    const out = focusSubgraph(graph, 'a1', 1)
    expect(ids(out)).toEqual(['a1', 'd1'])
  })

  it('does not mutate the input graph', () => {
    focusSubgraph(graph, 'core', 1)
    expect(graph.nodes).toHaveLength(5)
    expect(graph.links).toHaveLength(4)
  })
})

describe('highestDegreeVertexId', () => {
  it('picks the most-connected vertex', () => {
    // d2 reaches degree 4 against core's 2. Deliberately neither first in the
    // node array nor lowest by id, so insertion order and the tie-break can't
    // produce this answer by accident.
    const g = { ...graph, links: [...graph.links, edge('d2', 'a1'), edge('d2', 'core')] }
    expect(highestDegreeVertexId(g)).toBe('d2')
  })

  it('breaks ties on the lowest id so the choice is stable', () => {
    // Two vertices of degree 1, with the lower id placed second in the array:
    // insertion order would answer 'd1'.
    const g: DiscoveredGraph = {
      ...graph,
      nodes: [node('d1'), node('core')],
      links: [edge('d1', 'core')]
    }
    expect(highestDegreeVertexId(g)).toBe('core')
  })

  it('ignores links whose endpoints are outside the graph', () => {
    // a1 would outrank core on raw link count, but both dangling edges point
    // at vertices this graph does not contain.
    const g = { ...graph, links: [...graph.links, edge('a1', 'ghost'), edge('a1', 'ghost2')] }
    expect(highestDegreeVertexId(g)).toBe('core')
  })

  it('returns the lowest id when there are no links', () => {
    expect(highestDegreeVertexId({ ...graph, links: [] })).toBe('a1')
  })

  it('returns null for an empty graph', () => {
    expect(highestDegreeVertexId({ ...graph, nodes: [], links: [] })).toBeNull()
  })
})
