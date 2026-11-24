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

import type { DashboardFilter } from '@/types/dashboard'

// Shared dashboard filter (NMS-10507): surveillance categories + IP match,
// translated into FIQL for the node-scoped panels (alarms / situations /
// outages). node.id and ipInterface.ipAddress both resolve on those entities;
// node.categories.name does NOT, so categories are resolved to node ids first.

export const isFilterActive = (filter: DashboardFilter): boolean =>
  filter.surveillanceCategories.length > 0 || !!filter.ipMatch?.trim()

// URLs have length limits, so cap the node-id OR-group; the panels are homepage
// summaries, not exhaustive reports.
export const MAX_NODE_IDS = 300

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
