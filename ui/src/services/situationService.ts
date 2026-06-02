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

import { v2 } from './axiosInstances'

// Situations are alarms flagged isSituation; the relevant extra fields come
// straight from the alarm JSON.
export interface Situation {
  id: number | string
  severity: string
  situationAlarmCount?: number
  affectedNodeCount?: number
  lastEventTime?: number
  logMessage?: string
}

export const getPendingSituations = async (limit = 12): Promise<Situation[]> => {
  try {
    const fiql = encodeURIComponent('isSituation==true')
    const resp = await v2.get(`/alarms?_s=${fiql}&limit=${limit}&orderBy=lastEventTime&order=DESC`)
    if (resp.status === 204) {
      return []
    }
    return (resp.data?.alarm ?? []) as Situation[]
  } catch (err) {
    return []
  }
}
