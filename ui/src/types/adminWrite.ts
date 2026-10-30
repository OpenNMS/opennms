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

import { ValidationResult } from '@/types/validation'

// Result of a user/group write. On failure, `stale` is set when the server answered
// 412 (the If-Match version no longer matches, or the object is gone); callers branch
// on the flag, never on the message text. On success, `errors` carries follow-up
// problems, such as a failed reload of the list.
export interface AdminWriteResult extends ValidationResult {
  stale: boolean
}

export const adminWriteSuccess = (): AdminWriteResult => ({ success: true, message: '', stale: false })

// Manage Users and Manage Groups: the table, or the editor panel in place of it.
export enum AdminEditMode {
  Table = 'table',
  Edit = 'edit',
  Create = 'create'
}
