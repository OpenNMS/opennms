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

// The one place that knows the shape of the Vue Node Details route. Vue code links with
// nodeDetailsRoute() through vue-router (router-link, or useRouter().resolve(...).href where a
// string is needed), so the path, name and id encoding live here only.
//
// The id accepts what the legacy element/node.jsp?node= accepted: a database id, or
// foreignSource:foreignId. The two differ for a foreign id holding ':'. This page splits at the
// first ':' and keeps the rest (FS:a:b is foreign id 'a:b'); the legacy node.jsp went through
// NodeDao.get(String), which split(":")s and uses only the second part (FS:a:b looked up 'a').

import { parsePositiveIntegerId } from '@/lib/ids'

export const NODE_DETAILS_ROUTE_NAME = 'Node Details'

export type NodeRouteId =
  | { kind: 'numeric', id: string }
  | { kind: 'foreign', value: string }
  | { kind: 'invalid', value: string }

export const parseNodeRouteId = (raw: string): NodeRouteId => {
  const value = raw.trim()

  if (/^\d+$/.test(value)) {
    const id = parsePositiveIntegerId(value)
    return id ? { kind: 'numeric', id } : { kind: 'invalid', value }
  }

  // Both halves must be non-empty: v2 /nodes/a: answers 500, not 404.
  const colon = value.indexOf(':')
  if (colon > 0 && colon < value.length - 1) {
    return { kind: 'foreign', value }
  }

  return { kind: 'invalid', value }
}

export const nodeDetailsRoute = (id: string | number) => ({
  name: NODE_DETAILS_ROUTE_NAME,
  params: { id: String(id) }
})
