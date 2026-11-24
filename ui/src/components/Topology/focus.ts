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

import type { DiscoveredGraph } from '@/types/topology'

/**
 * Reduce a discovered graph to a focus node plus everything within a given
 * number of hops (the legacy "Semantic Zoom Level"). This keeps the on-screen
 * node count bounded on large discovered topologies: pick a node of interest,
 * see its neighborhood, step the radius out as needed.
 *
 * Pure and non-mutating. Returns the same graph unchanged if the focus id
 * isn't present (e.g. it was filtered out by a prior focus) so callers can
 * treat "no/!invalid focus" as "show everything". hops <= 0 yields just the
 * focus node.
 */
export const focusSubgraph = (
  graph: DiscoveredGraph,
  focusId: string | null,
  hops: number
): DiscoveredGraph => {
  if (focusId === null || !graph.nodes.some(n => n.id === focusId)) {
    return graph
  }

  // Undirected adjacency over the node set (edges to unknown nodes ignored).
  const adjacency = new Map<string, string[]>()
  for (const n of graph.nodes) {
    adjacency.set(n.id, [])
  }
  for (const e of graph.links) {
    if (adjacency.has(e.sourceId) && adjacency.has(e.targetId)) {
      adjacency.get(e.sourceId)!.push(e.targetId)
      adjacency.get(e.targetId)!.push(e.sourceId)
    }
  }

  // BFS out to `hops` rings.
  const reached = new Set<string>([focusId])
  let frontier = [focusId]
  for (let ring = 0; ring < Math.max(0, hops); ring++) {
    const next: string[] = []
    for (const u of frontier) {
      for (const v of adjacency.get(u) ?? []) {
        if (!reached.has(v)) {
          reached.add(v)
          next.push(v)
        }
      }
    }
    if (next.length === 0) {
      break
    }
    frontier = next
  }

  return {
    ...graph,
    nodes: graph.nodes.filter(n => reached.has(n.id)),
    links: graph.links.filter(e => reached.has(e.sourceId) && reached.has(e.targetId))
  }
}

/**
 * Pick a default anchor for a graph too large to render whole: the vertex with
 * the most links, which in practice lands on an aggregation device rather than
 * a leaf. Structural on purpose. The API does offer the provider's own
 * `defaultFocus`, but its enlinkd pick is a global "highest summed ifSpeed"
 * node that falls back to an arbitrary vertex when it isn't in the graph being
 * shown, and the response gives no way to tell the two apart.
 *
 * Ties break on the lowest id so the choice is stable across loads. Returns
 * null for an empty graph; a graph with no links yields its lowest id.
 */
export const highestDegreeVertexId = (graph: DiscoveredGraph): string | null => {
  const degree = new Map<string, number>()
  for (const n of graph.nodes) {
    degree.set(n.id, 0)
  }
  for (const e of graph.links) {
    // Edges to vertices outside this graph don't count towards degree, matching
    // what focusSubgraph will actually be able to traverse.
    if (degree.has(e.sourceId) && degree.has(e.targetId)) {
      degree.set(e.sourceId, degree.get(e.sourceId)! + 1)
      degree.set(e.targetId, degree.get(e.targetId)! + 1)
    }
  }

  let best: string | null = null
  let bestDegree = -1
  for (const [id, d] of degree) {
    if (d > bestDegree || (d === bestDegree && best !== null && id < best)) {
      best = id
      bestDegree = d
    }
  }
  return best
}
