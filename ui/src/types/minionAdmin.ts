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

// Wire shape of an OnmsMinion from /api/v2/minions (OnmsMonitoringSystem +
// status/version). type/status/date are server-maintained (read-only); only
// label, location and properties are editable, matching the legacy page.
export interface Minion {
  id: string
  label: string | null
  location: string | null
  type?: string | null
  status?: string | null
  version?: string | null
  date?: string | number | null // last updated
  properties?: Record<string, string>
}

export interface MinionApiResponse {
  minion: Minion[]
  totalCount: number
  count: number
  offset: number
}
