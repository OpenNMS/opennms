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

// Wire-format helpers for poll-outages <time> spans. BasicScheduleUtils keys
// on the exact string LENGTH: 'dd-MMM-yyyy HH:mm:ss' (20 chars, month as a
// three-letter English abbreviation) for the 'specific' type, and 'HH:mm:ss'
// (8 chars) for daily/weekly/monthly. Every numeric part is zero-padded so the
// length is exact — matching the legacy admin/sched-outages/editoutage.jsp.

import { OutageTime, OutageType } from '@/types/scheduledOutage'

export interface Option {
  label: string
  value: string
}

// three-letter month values (MMM, Locale.US) with full-name labels
export const MONTHS: Option[] = [
  'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
].map((m, i) => ({
  value: m,
  label: new Date(2000, i, 1).toLocaleString('en-US', { month: 'long' })
}))

export const DAYS_OF_WEEK: Option[] = [
  'sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'
].map(d => ({ value: d, label: d.charAt(0).toUpperCase() + d.slice(1) }))

const pad = (n: number, width = 2): string => String(n).padStart(width, '0')

// Unpadded 1..31 for the monthly 'day' attribute (parsed with Integer.parseInt;
// legacy chooseDayOfMonth emitted the bare integer).
export const DAYS_OF_MONTH: Option[] = Array.from({ length: 31 }, (_, i) => ({
  value: String(i + 1),
  label: String(i + 1)
}))

// One time-span row in the editor, independent of the wire format.
export interface TimeSpanFields {
  // 'specific' uses the full date and time of both; the others use only the
  // time of day (+ day for weekly/monthly)
  start: Date
  end: Date
  // weekday name (weekly) or day-of-month (monthly)
  day: string
}

export const defaultTimeSpanFields = (currentYear: number): TimeSpanFields => ({
  start: new Date(currentYear, 0, 1, 0, 0, 0),
  end: new Date(currentYear, 0, 1, 23, 59, 59),
  day: 'sunday'
})

// HH:mm:ss, the recurring-outage wire format (8 characters)
export const formatTimeOfDay = (d: Date): string =>
  `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`

// dd-MMM-yyyy HH:mm:ss with English month abbreviations, the specific-outage
// wire format (20 characters); BasicScheduleUtils keys its parser on the length
export const formatSpecific = (d: Date): string =>
  `${pad(d.getDate())}-${MONTHS[d.getMonth()].value}-${d.getFullYear()} ${formatTimeOfDay(d)}`

// Build the wire <time> from the editor row for the given outage type.
export const buildOutageTime = (type: OutageType, f: TimeSpanFields): OutageTime => {
  if (type === 'specific') {
    return { begins: formatSpecific(f.start), ends: formatSpecific(f.end) }
  }
  const time: OutageTime = { begins: formatTimeOfDay(f.start), ends: formatTimeOfDay(f.end) }
  if (type === 'weekly' || type === 'monthly') {
    time.day = f.day
  }
  return time
}

// Human-readable label for an existing span in the list.
export const describeOutageTime = (type: OutageType, t: OutageTime): string => {
  if (type === 'specific') {
    return `${t.begins}  →  ${t.ends}`
  }
  const prefix =
    type === 'weekly' && t.day ? `${t.day.charAt(0).toUpperCase()}${t.day.slice(1)} ` :
      type === 'monthly' && t.day ? `Day ${t.day} ` : ''
  return `${prefix}${t.begins} → ${t.ends}`
}

export const outageTimesEqual = (a: OutageTime, b: OutageTime): boolean =>
  a.begins === b.begins && a.ends === b.ends && (a.day ?? '') === (b.day ?? '')
