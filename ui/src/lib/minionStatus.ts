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

// MinionStatusTracker reports up / down / unknown; both tabs treat anything else,
// including a missing status, as unknown so their counts and colours agree.
export type MinionState = 'up' | 'down' | 'unknown'

export const minionState = (status: string | null | undefined): MinionState => {
  const s = (status ?? '').toLowerCase()
  return s === 'up' ? 'up' : s === 'down' ? 'down' : 'unknown'
}

export const minionStateSeverity = (status: string | null | undefined): 'success' | 'danger' | 'warn' => {
  const state = minionState(status)
  return state === 'up' ? 'success' : state === 'down' ? 'danger' : 'warn'
}
