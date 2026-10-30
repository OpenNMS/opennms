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
  userId: string
  fullName?: string | null
  userComments?: string | null
  email?: string | null
  pagerEmail?: string | null
  workPhone?: string | null
  mobilePhone?: string | null
  homePhone?: string | null
  numericPagerService?: string | null
  numericPagerPin?: string | null
  textPagerService?: string | null
  textPagerPin?: string | null
  tuiPin?: string | null
  timeZoneId?: string | null
  dutySchedules?: string[]
  roles?: string[]
  readOnly?: boolean
  version: number
}

export interface ManagedUserCreate extends Omit<ManagedUser, 'version'> {
  password: string
}

// Shown when a write is rejected with 412 (If-Match no longer matches the stored version).
export const STALE_USER_MESSAGE =
  'This user was changed by someone else. The list has been reloaded; reapply your change.'

// Shown instead when the reload after a 412 failed too.
export const STALE_USER_RELOAD_FAILED_MESSAGE =
  'This user was changed by someone else, and the list could not be reloaded. Reload the page.'

// System accounts the server refuses to delete or rename; mirrored here so
// the UI can disable the controls with an explanation instead of a 400. The
// server also refuses a case variant of these as a new or renamed user-id.
// See UsersRestService.PROTECTED_USERS.
export const PROTECTED_USER_IDS = ['admin', 'rtc']
