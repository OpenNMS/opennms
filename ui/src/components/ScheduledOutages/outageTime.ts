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

const range = (start: number, end: number, width = 2): Option[] => {
  const out: Option[] = []
  for (let i = start; i <= end; i++) {
    out.push({ value: pad(i, width), label: pad(i, width) })
  }
  return out
}

// Unpadded 1..31 for the monthly 'day' attribute (parsed with Integer.parseInt;
// legacy chooseDayOfMonth emitted the bare integer).
export const DAYS_OF_MONTH: Option[] = Array.from({ length: 31 }, (_, i) => ({
  value: String(i + 1),
  label: String(i + 1)
}))

// Zero-padded 01..31 for the specific-date 'dd' field. The 'dd-MMM-yyyy HH:mm:ss'
// string MUST be exactly 20 chars — BasicScheduleUtils keys the parser on length,
// so an unpadded single-digit day (19 chars) is silently misread as a daily span.
export const DAYS_OF_MONTH_PADDED: Option[] = Array.from({ length: 31 }, (_, i) => ({
  value: pad(i + 1),
  label: pad(i + 1)
}))

export const HOURS = range(0, 23)
export const MINUTES = range(0, 59)
export const SECONDS = range(0, 59)

export const yearOptions = (currentYear: number): Option[] =>
  range(currentYear, currentYear + 10, 4)

// One time-span row in the editor, independent of the wire format.
export interface TimeSpanFields {
  // 'specific' uses the date parts; the others use only hour/minute/second (+ day)
  startDay: string
  startMonth: string
  startYear: string
  startHour: string
  startMinute: string
  startSecond: string
  endDay: string
  endMonth: string
  endYear: string
  endHour: string
  endMinute: string
  endSecond: string
  // weekday name (weekly) or day-of-month (monthly)
  day: string
}

export const defaultTimeSpanFields = (currentYear: number): TimeSpanFields => ({
  startDay: '01',
  startMonth: 'Jan',
  startYear: String(currentYear),
  startHour: '00',
  startMinute: '00',
  startSecond: '00',
  endDay: '01',
  endMonth: 'Jan',
  endYear: String(currentYear),
  endHour: '23',
  endMinute: '59',
  endSecond: '59',
  day: 'sunday'
})

// Build the wire <time> from the editor row for the given outage type.
export const buildOutageTime = (type: OutageType, f: TimeSpanFields): OutageTime => {
  if (type === 'specific') {
    return {
      begins: `${f.startDay}-${f.startMonth}-${f.startYear} ${f.startHour}:${f.startMinute}:${f.startSecond}`,
      ends: `${f.endDay}-${f.endMonth}-${f.endYear} ${f.endHour}:${f.endMinute}:${f.endSecond}`
    }
  }
  const time: OutageTime = {
    begins: `${f.startHour}:${f.startMinute}:${f.startSecond}`,
    ends: `${f.endHour}:${f.endMinute}:${f.endSecond}`
  }
  if (type === 'weekly') {
    time.day = f.day
  }
  if (type === 'monthly') {
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
