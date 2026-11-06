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

// Wire shapes of the v2 on-call role management API (/api/v2/oncall-roles). A role
// is a notification rota: a membership group, a supervisor and per-user schedules.
// onCallNow and onCallNowIsSupervisor are computed by the server and ignored on write.

export type OnCallScheduleType = 'specific' | 'daily' | 'weekly' | 'monthly'

export interface OnCallTime {
  id?: string | null
  day?: string | null
  begins: string
  ends: string
}

export interface OnCallSchedule {
  user: string
  type: string
  times: OnCallTime[]
}

export interface ManagedOnCallRole {
  name: string
  description?: string | null
  membershipGroup: string
  supervisor: string
  schedules: OnCallSchedule[]
  onCallNow?: string[]
  onCallNowIsSupervisor?: boolean
  version: number
}

export type ManagedOnCallRoleCreate = Omit<ManagedOnCallRole, 'version' | 'onCallNow' | 'onCallNowIsSupervisor'>

export interface OnCallCalendarEntry {
  start: string
  end: string
  user: string
  // index into the role's schedules; null when the supervisor covers the time
  scheduleIndex: number | null
}

export interface OnCallCalendar {
  timeZone: string
  month: string
  entries: OnCallCalendarEntry[]
}

export interface OnCallScheduleSettings {
  timeZone: string
  shortMonths: string[]
}

// Shown when a write is rejected with 412 (If-Match no longer matches the stored version).
export const STALE_ONCALL_ROLE_MESSAGE =
  'This on-call role was changed by someone else. The list has been reloaded; reapply your change.'

// Shown instead when the reload after a 412 failed too.
export const STALE_ONCALL_ROLE_RELOAD_FAILED_MESSAGE =
  'This on-call role was changed by someone else, and the list could not be reloaded. Reload the page.'
