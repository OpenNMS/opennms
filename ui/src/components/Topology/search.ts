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

import type { CanvasNode } from '@/types/topology'

/**
 * A search hit. `label` is flat so the autocomplete can render every kind from
 * one option-label. `matchedOn` is set only when a node hit came from something
 * other than its label, so the suggestion can show what it was: two nodes
 * matching "127.0.0" look identical if only labels are shown.
 */
export type SearchMatch =
  | { kind: 'node', label: string, node: CanvasNode, matchedOn?: { key: string, value: string }}
  | { kind: 'category', label: string, canvasIds: string[] }

/** Kept for the node-only matcher; a superset of what searchNodes returns. */
export type NodeSearchMatch = Extract<SearchMatch, { kind: 'node' }>

/** How a matched property key reads in the suggestion list. */
const KEY_LABELS: Record<string, string> = {
  ipAddress: 'IP',
  nodeId: 'Node ID'
}

export const searchFieldLabel = (key: string): string =>
  KEY_LABELS[key] ?? key.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/^./, c => c.toUpperCase())

/**
 * Substring search over a node's label, its id and every provider property (an
 * IP, an application id, a GraphML author's attributes). Label hits sort first;
 * an empty query yields the first `limit` nodes, as the dropdown does on focus.
 */
export const searchNodes = (
  nodes: CanvasNode[],
  query: string,
  limit: number
): NodeSearchMatch[] => {
  const q = query.trim().toLowerCase()
  if (!q) {
    return nodes.slice(0, limit).map(node => ({ kind: 'node' as const, label: node.label, node }))
  }

  const byLabel: NodeSearchMatch[] = []
  const byOther: NodeSearchMatch[] = []

  for (const node of nodes) {
    const base = { kind: 'node' as const, label: node.label, node }
    if (node.label.toLowerCase().includes(q)) {
      byLabel.push(base)
      continue
    }
    if (node.nodeId != null && String(node.nodeId).includes(q)) {
      byOther.push({ ...base, matchedOn: { key: 'nodeId', value: String(node.nodeId) }})
      continue
    }
    const hit = Object.entries(node.properties ?? {})
      .find(([, value]) => value != null && String(value).toLowerCase().includes(q))
    if (hit) {
      byOther.push({ ...base, matchedOn: { key: hit[0], value: String(hit[1]) }})
    }
  }

  return [...byLabel, ...byOther].slice(0, limit)
}

/**
 * Categories and nodes in one list, as the old map's single box did. A category
 * resolves to its members that are vertices of *this* graph -- a node with no
 * discovered links is not one. Categories sort first, being few and standing for
 * many nodes.
 */
export const searchTopology = (
  nodes: CanvasNode[],
  categoriesByNodeId: Record<number, string[]>,
  query: string,
  limit: number
): SearchMatch[] => {
  const q = query.trim().toLowerCase()
  if (!q) {
    return searchNodes(nodes, query, limit)
  }

  const members = new Map<string, string[]>()
  for (const node of nodes) {
    if (node.nodeId == null) {
      continue
    }
    for (const name of categoriesByNodeId[node.nodeId] ?? []) {
      if (!name.toLowerCase().includes(q)) {
        continue
      }
      const ids = members.get(name)
      if (ids) {
        ids.push(node.id)
      } else {
        members.set(name, [node.id])
      }
    }
  }

  const categoryMatches: SearchMatch[] = Array.from(members.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([name, canvasIds]) => ({ kind: 'category', label: name, canvasIds }))

  // Categories lead, but never fill the list on their own: a one-letter query on
  // a category-heavy install can match more categories than the limit, and node
  // hits -- the thing most people are actually typing -- were evicted entirely.
  const categoryRoom = Math.min(categoryMatches.length, Math.max(1, Math.floor(limit / 2)))
  const shown = categoryMatches.slice(0, categoryRoom)
  return [...shown, ...searchNodes(nodes, query, limit - shown.length)]
}
