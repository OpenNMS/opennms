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

import { NodePage, QueryParameters } from '@/types'

const queryParametersHandler = (queryParameters: QueryParameters, endpoint: string): string => {
  let modifiedEndpoint = endpoint + '?'
  let queryString = ''

  for (const key in queryParameters) {
    queryString = `${queryString}${key}=${(queryParameters as any)[key]}&`
  }

  modifiedEndpoint += queryString

  // remove last useless ampersand
  return modifiedEndpoint.slice(0, -1)
}

/**
 * Narrow a v2 query to one node, keeping any FIQL the caller already has. The caller's clause is
 * parenthesised before the `;` (AND): FIQL binds `;` tighter than `,` (OR), so a bare
 * `node.id==1;a,b` would read as `(node AND a) OR b` and let in other nodes' rows.
 */
/** The page a query asks for, as a node slice records it: no offset is the start, no limit is all. */
const nodePageOf = (queryParameters?: QueryParameters): NodePage => ({
  offset: queryParameters?.offset ?? 0,
  limit: queryParameters?.limit ?? 0
})

const withNodeFilter = (nodeId: string | number, queryParameters?: QueryParameters): QueryParameters => {
  const nodeClause = `node.id==${nodeId}`
  const _s = queryParameters?._s ? `${nodeClause};(${queryParameters._s})` : nodeClause

  return { ...queryParameters, _s }
}

export { nodePageOf, queryParametersHandler, withNodeFilter }
