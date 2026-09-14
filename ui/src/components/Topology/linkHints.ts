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

import { placedIdFor } from '@/components/Topology/nodeIds'
import type { CanvasLinkBinding, DiscoveredLinkType, DiscoveredNeighbor } from '@/types/topology'

/**
 * Phase 2 assisted composition: while editing a custom view, real discovered
 * adjacencies between the placed nodes are surfaced as faint "ghost" links
 * the user can adopt. This module is the pure computation; fetching lives in
 * the store and rendering in the canvas.
 */

/** A candidate link between two placed nodes, backed by a real adjacency. */
export interface LinkHint {
  /** Canvas (placed-) ids. */
  sourceId: string
  targetId: string
  binding: CanvasLinkBinding
}

/**
 * Prefer the protocols with the most precise port information when the same
 * node pair was discovered several ways.
 */
const PROTOCOL_PREFERENCE: DiscoveredLinkType[] = ['lldp', 'cdp', 'bridge', 'ospf', 'isis']

const protocolRank = (p: DiscoveredLinkType): number => {
  const i = PROTOCOL_PREFERENCE.indexOf(p)
  return i === -1 ? PROTOCOL_PREFERENCE.length : i
}

/**
 * The ghost-link candidates for the current canvas: every discovered
 * adjacency whose BOTH endpoints are placed and that has no existing link
 * yet. An adjacency reported from both ends (and over several protocols)
 * collapses to one hint per node pair, keeping the most-preferred protocol;
 * the binding's source/target ports follow the reporting side's orientation.
 */
export const computeGhostLinks = (
  neighborsByNode: Record<number, DiscoveredNeighbor[]>,
  placedNodeIds: Set<string>,
  hasLink: (canvasIdA: string, canvasIdB: string) => boolean
): LinkHint[] => {
  const byPair = new Map<string, LinkHint>()

  for (const key of Object.keys(neighborsByNode)) {
    const nodeId = Number(key)
    if (!placedNodeIds.has(String(nodeId))) {
      continue
    }
    for (const neighbor of neighborsByNode[nodeId] ?? []) {
      if (!placedNodeIds.has(String(neighbor.neighborNodeId))) {
        continue
      }
      if (neighbor.neighborNodeId === nodeId) {
        continue
      }

      const sourceId = placedIdFor(String(nodeId))
      const targetId = placedIdFor(String(neighbor.neighborNodeId))
      if (hasLink(sourceId, targetId)) {
        continue
      }

      const pairKey =
        nodeId < neighbor.neighborNodeId
          ? `${nodeId}|${neighbor.neighborNodeId}`
          : `${neighbor.neighborNodeId}|${nodeId}`
      const candidate: LinkHint = {
        sourceId,
        targetId,
        binding: {
          protocol: neighbor.linkType,
          sourcePort: neighbor.localPort,
          targetPort: neighbor.remotePort
        }
      }
      const existing = byPair.get(pairKey)
      if (!existing || protocolRank(candidate.binding.protocol) < protocolRank(existing.binding.protocol)) {
        byPair.set(pairKey, candidate)
      }
    }
  }

  return [...byPair.values()]
}

/**
 * The discovery detail for one node pair: every protocol entry the source
 * node's adjacency reports toward the target, one binding per protocol.
 * Used by the Inspector for links that carry no persisted binding (links in
 * discovered views, hand-drawn links).
 */
export const resolveLinkBindings = (
  neighbors: DiscoveredNeighbor[],
  targetNodeId: number
): CanvasLinkBinding[] => {
  const seen = new Set<string>()
  return neighbors
    .filter(n => n.neighborNodeId === targetNodeId)
    .filter((n) => {
      if (seen.has(n.linkType)) {
        return false
      }
      seen.add(n.linkType)
      return true
    })
    .map(n => ({ protocol: n.linkType, sourcePort: n.localPort, targetPort: n.remotePort }))
}
