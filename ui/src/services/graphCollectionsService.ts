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

import { rest } from './axiosInstances'

// Graph Collections are the KSC reports of old; the v1 endpoint keeps its
// historical name. GET /rest/ksc returns the terse list ({ id, label }).
export interface GraphCollection {
  id: number
  label: string
}

export const getGraphCollections = async (): Promise<GraphCollection[]> => {
  try {
    const resp = await rest.get('/ksc', { headers: { Accept: 'application/json' }})
    const raw = resp.data?.kscReport
    const arr = Array.isArray(raw) ? raw : raw ? [raw] : []
    return arr
      .map((r: any) => ({ id: Number(r.id), label: String(r.label ?? '') }))
      .filter((r: GraphCollection) => Number.isFinite(r.id) && r.label)
  } catch (_err) {
    return []
  }
}
