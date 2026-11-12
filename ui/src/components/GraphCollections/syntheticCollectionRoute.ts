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

/**
 * The SPA route of the on-the-fly collection for a top-level resource, or null for a resource
 * that has none. Top-level resource ids are `node[1]` for a node stored by id, `node[fs:fid]`
 * (or the older `nodeSource[fs:fid]`) for one stored by foreign source, and `domain[name]`.
 */
export const syntheticCollectionRoute = (resourceId: string): string | null => {
  const match = /^(node|nodeSource|domain)\[([^\]]+)\]$/.exec(resourceId)
  if (!match) {
    return null
  }
  const [, kind, key] = match
  const route = kind === 'node' && !/^\d+$/.test(key) ? 'nodeSource' : kind
  return `/graph-collections/${route}/${encodeURIComponent(key)}`
}
