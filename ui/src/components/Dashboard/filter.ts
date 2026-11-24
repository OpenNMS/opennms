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

import { v2 } from '@/services/axiosInstances'
import type { DashboardFilter } from '@/types/dashboard'

// Shared dashboard filter (NMS-10507): surveillance categories + IP match,
// translated into FIQL for the node-scoped panels (alarms / situations /
// outages). node.id and ipInterface.ipAddress both resolve on those entities;
// node.categories.name does NOT, so categories are resolved to node ids first.

export const isFilterActive = (filter: DashboardFilter): boolean =>
  filter.surveillanceCategories.length > 0 || !!filter.ipMatch?.trim()

// Cache per sorted category set for the life of the page; the membership of a
// surveillance category rarely changes within a dashboard session.
const nodeIdCache = new Map<string, Promise<number[]>>()

// Resolve the selected categories to the union of their member node ids.
// Returns null when no category is selected (no node constraint).
export const resolveFilterNodeIds = async (filter: DashboardFilter): Promise<number[] | null> => {
  const categories = [...filter.surveillanceCategories].sort()
  if (!categories.length) {
    return null
  }
  const key = categories.join('|')
  if (!nodeIdCache.has(key)) {
    nodeIdCache.set(key, (async () => {
      const ids = new Set<number>()
      let ok = true
      for (const category of categories) {
        try {
          const resp = await v2.get(`/nodes?_s=${encodeURIComponent(`category.name==${category}`)}&limit=0`)
          for (const node of resp.data?.node ?? []) {
            if (node?.id != null) {
              ids.add(Number(node.id))
            }
          }
        } catch {
          // a transient failure must not cache an empty set for the session; evict
          // so the next refresh retries instead of leaving the panels permanently empty
          ok = false
        }
      }
      if (!ok) {
        nodeIdCache.delete(key)
      }
      return [...ids]
    })())
  }
  return nodeIdCache.get(key)!
}

// URLs have length limits, so cap the node-id OR-group; the panels are homepage
// summaries, not exhaustive reports.
const MAX_NODE_IDS = 300

// FIQL fragments (already grouped) that constrain a node-scoped query to the
// active filter. AND these into the panel's own `_s` with ';'.
// `nodeIdProperty` is the FIQL path of the node id on the queried entity:
// `node.id` where the node is an alias (alarms, outages), `id` on /nodes itself.
export const filterFiqlClauses = (filter: DashboardFilter, nodeIds: number[] | null, nodeIdProperty = 'node.id'): string[] => {
  const clauses: string[] = []
  const ip = filter.ipMatch?.trim()
  if (ip) {
    clauses.push(`ipInterface.ipAddress==${ip}`)
  }
  if (nodeIds) {
    // a category selected but matching no nodes must return nothing, not everything
    const capped = nodeIds.slice(0, MAX_NODE_IDS)
    clauses.push(capped.length ? `(${capped.map(id => `${nodeIdProperty}==${id}`).join(',')})` : `${nodeIdProperty}==-1`)
  }
  return clauses
}

// Convenience: resolve + build the clauses in one call.
export const buildFilterClauses = async (filter: DashboardFilter, nodeIdProperty = 'node.id'): Promise<string[]> =>
  filterFiqlClauses(filter, await resolveFilterNodeIds(filter), nodeIdProperty)

// Resolve the WHOLE filter (categories AND ip match) to a node-id set, for
// consumers of server-aggregated data that cannot take FIQL (e.g. the alarm
// summaries endpoint). null = no active filter; an empty set = filter active
// but matching nothing.
export const resolveFilterToNodeIdSet = async (filter: DashboardFilter): Promise<Set<number> | null> => {
  if (!isFilterActive(filter)) {
    return null
  }
  const categoryIds = await resolveFilterNodeIds(filter)
  const ip = filter.ipMatch?.trim()
  let ipIds: Set<number> | null = null
  if (ip) {
    ipIds = new Set()
    try {
      const resp = await v2.get(`/nodes?_s=${encodeURIComponent(`ipInterface.ipAddress==${ip}`)}&limit=0`)
      for (const node of resp.data?.node ?? []) {
        if (node?.id != null) {
          ipIds.add(Number(node.id))
        }
      }
    } catch {
      // an unresolvable ip clause must fail closed (match nothing), not open
    }
  }
  if (categoryIds && ipIds) {
    return new Set([...categoryIds].filter(id => ipIds!.has(id)))
  }
  if (categoryIds) {
    return new Set(categoryIds)
  }
  return ipIds ?? new Set()
}
