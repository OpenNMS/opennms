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

// Wire shapes of the v2 group management API (/api/v2/groups). Field names
// follow groups.xml; the user list is ordered — the order drives notification
// escalation. Fields the API does not expose (default-map) are preserved
// server-side on update.

export interface ManagedGroup {
  name: string
  comments?: string | null
  users?: string[]
  dutySchedules?: string[]
}

// The server refuses to delete or rename this group; mirrored here so the UI
// can disable the controls with an explanation instead of a 400.
export const PROTECTED_GROUP_NAMES = ['Admin']
