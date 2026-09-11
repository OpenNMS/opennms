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

// Wire shapes of the v2 on-call role API (/api/v2/on-call-roles). Schedule
// times use the groups.xml formats: specific entries carry full
// 'dd-MMM-yyyy HH:mm:ss' timestamps (English month abbreviations), the other
// types carry 'HH:mm:ss' with an optional day.

export type OnCallScheduleType = 'specific' | 'daily' | 'weekly' | 'monthly'

export interface OnCallTime {
  id?: string
  day?: string | null
  begins: string
  ends: string
}

export interface OnCallSchedule {
  user: string
  type: OnCallScheduleType
  time: OnCallTime[]
}

export interface OnCallRole {
  name: string
  'membership-group'?: string | null
  supervisor?: string | null
  description?: string | null
  schedule?: OnCallSchedule[]
  'currently-on-call'?: string[]
  'schedule-error'?: string
}

export interface OnCallCalendarEntry {
  start: number
  end: number
  user?: string[]
  supervisor: boolean
}

export interface OnCallCalendarDay {
  date: string
  entry?: OnCallCalendarEntry[]
}

export interface OnCallCalendar {
  role: string
  year: number
  month: number
  'time-zone'?: string
  'schedule-error'?: string
  day: OnCallCalendarDay[]
}

const MONTHS_EN = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

// groups.xml stores specific times as dd-MMM-yyyy HH:mm:ss with English
// month abbreviations regardless of browser locale. notifd reads them in the
// server's zone, so a picked instant is expressed in `timeZone` when given;
// an unknown zone id falls back to the browser's wall clock.
export const formatScheduleTimestamp = (date: Date, timeZone?: string): string => {
  const pad = (n: number) => String(n).padStart(2, '0')
  if (timeZone) {
    try {
      const parts = new Intl.DateTimeFormat('en-US', {
        timeZone, hourCycle: 'h23',
        year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric'
      }).formatToParts(date)
      const get = (type: Intl.DateTimeFormatPartTypes) => Number(parts.find(p => p.type === type)?.value)
      return `${pad(get('day'))}-${MONTHS_EN[get('month') - 1]}-${get('year')} ${pad(get('hour'))}:${pad(get('minute'))}:${pad(get('second'))}`
    } catch (_err) {
      // RangeError for a zone id this browser does not know
    }
  }
  return `${pad(date.getDate())}-${MONTHS_EN[date.getMonth()]}-${date.getFullYear()} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
