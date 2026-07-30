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

// Wire shapes of the v2 user management API (/api/v2/users). Field names
// follow users.xml; the password hash is never part of any response, and
// contact types the API does not expose (XMPP among them) are preserved
// server-side on update.

export interface ManagedUser {
  'user-id': string
  'full-name'?: string | null
  'user-comments'?: string | null
  email?: string | null
  'pager-email'?: string | null
  'tui-pin'?: string | null
  'time-zone-id'?: string | null
  'duty-schedule'?: string[]
  role?: string[]
  'read-only'?: boolean
}

export interface ManagedUserCreate extends ManagedUser {
  password: string
}

// System accounts the server refuses to delete or rename; mirrored here so
// the UI can disable the controls with an explanation instead of a 400.
export const PROTECTED_USER_IDS = ['admin', 'rtc']
