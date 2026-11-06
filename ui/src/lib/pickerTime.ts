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

import { toTimeOnlyDate } from '@opennms/onms-ui'

// OnmsDatePicker works on Dates; the editors keep server-local wall-clock strings ('HH:mm' and 'yyyy-MM-dd'; a date
// and time is picked as one of each, see SplitDateTimePicker). These convert between the two using only the Date's
// local fields, so no time zone shift ever applies: a picked 08:00 stays 08:00.

const pad = (n: number) => String(n).padStart(2, '0')

const CLOCK = /^(\d{2}):(\d{2})$/
const DAY = /^(\d{4})-(\d{2})-(\d{2})$/

/**
 * 'HH:mm' → a Date at that time, for a time-only OnmsDatePicker; null for an empty or malformed value. Only the clock
 * part matters, so the date is the picker's fixed day (toTimeOnlyDate), not today: on a day when the browser's clocks
 * skip an hour for daylight saving, 02:30 does not exist and a Date would turn it into 03:30.
 */
export const clockToDate = (hhmm: string): Date | null => {
  const m = CLOCK.exec(hhmm)
  return m ? toTimeOnlyDate(Number(m[1]), Number(m[2])) : null
}

/** a Date → its 'HH:mm'; '' for null */
export const dateToClock = (date: Date | null): string =>
  date ? `${pad(date.getHours())}:${pad(date.getMinutes())}` : ''

/** 'yyyy-MM-dd' → a Date at midnight of that day; null for an empty or malformed value */
export const dayToDate = (day: string): Date | null => {
  const m = DAY.exec(day)
  return m ? new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3])) : null
}

/** a Date → its 'yyyy-MM-dd'; '' for null */
export const dateToDay = (date: Date | null): string =>
  date ? `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` : ''

/** 'HH:mm' → the 12-hour form the pickers show, e.g. '8:00 am', '12:30 pm'; '' for a malformed value */
export const clockTo12Hour = (hhmm: string): string => {
  const m = CLOCK.exec(hhmm)
  if (!m) {
    return ''
  }
  const hours = Number(m[1])
  return `${hours % 12 || 12}:${m[2]} ${hours < 12 ? 'am' : 'pm'}`
}
