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
