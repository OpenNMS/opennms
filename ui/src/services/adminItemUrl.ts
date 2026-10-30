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

import { isPathAddressable } from '@/lib/adminValidation'
import { ifMatch } from './axiosInstances'

export type AdminCollection = 'users' | 'groups'
export type AdminItemAction = 'rename' | 'password'

export interface AdminItemTarget {
  url: string
  params?: Record<string, string>
}

// Segments under each collection that are endpoints of their own: the path form of a
// user or group with one of these names would reach that endpoint instead.
const RESERVED_SEGMENTS: Record<AdminCollection, string[]> = {
  users: ['item', 'available-roles'],
  groups: ['item', 'available-categories']
}

const QUERY_PARAM: Record<AdminCollection, string> = {
  users: 'userId',
  groups: 'name'
}

// Where a per-item v2 request goes, relative to the v2 base URL. The path form is used
// whenever it can carry the name; a name with / \ or % (which the servlet container
// rejects in a path segment) or one that is a reserved segment goes in the query of
// the /item endpoint instead.
export const adminItemUrl = (collection: AdminCollection, name: string, action?: AdminItemAction): AdminItemTarget => {
  const suffix = action ? `/${action}` : ''
  if (isPathAddressable(name) && !RESERVED_SEGMENTS[collection].includes(name)) {
    return { url: `/${collection}/${encodeURIComponent(name)}${suffix}` }
  }
  return { url: `/${collection}/item${suffix}`, params: { [QUERY_PARAM[collection]]: name }}
}

// The request config of a per-item write: the If-Match header, plus the query of the /item form when the
// name needs it.
export const adminItemConfig = (target: AdminItemTarget, version: number) =>
  target.params ? { ...ifMatch(version), params: target.params } : ifMatch(version)
