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

import { add, getMonth, getUnixTime, set, setDate, setMonth, startOfDay, startOfWeek, sub } from 'date-fns'
import type { Duration } from 'date-fns'
import { StartEndTime } from '@/types'

// The symbolic timespans a KSC graph may carry, in the same order the legacy
// editor presented them. Kept in lockstep with
// KSC_PerformanceReportFactory.TIMESPAN_OPTIONS on the server.
export const KSC_TIMESPAN_OPTIONS: string[] = [
  '1_hour',
  '2_hour',
  '4_hour',
  '6_hour',
  '8_hour',
  '12_hour',
  '1_day',
  '2_day',
  '7_day',
  '1_month',
  '3_month',
  '6_month',
  '1_year',
  'Today',
  'Yesterday',
  'Yesterday 9am-5pm',
  'Yesterday 5pm-10pm',
  'This Week',
  'Last Week',
  'This Month',
  'Last Month',
  'This Quarter',
  'Last Quarter',
  'This Year',
  'Last Year'
]

// Relative spans that just subtract a fixed amount from "now"; the month/year
// values mirror the day-count arithmetic in getBeginEndTime (30/90/183/365).
const RELATIVE: Record<string, Duration> = {
  '1_hour': { hours: 1 },
  '2_hour': { hours: 2 },
  '4_hour': { hours: 4 },
  '6_hour': { hours: 6 },
  '8_hour': { hours: 8 },
  '12_hour': { hours: 12 },
  '1_day': { days: 1 },
  '2_day': { days: 2 },
  '7_day': { days: 7 },
  '1_month': { days: 30 },
  '3_month': { days: 90 },
  '6_month': { days: 183 },
  '1_year': { days: 365 }
}

// Match the label granularity to the span so the x-axis stays legible; mirrors
// what TimeControls picks for the equivalent ranges on the resource graphs.
const pickFormat = (startSec: number, endSec: number): string => {
  const spanHours = (endSec - startSec) / 3600
  if (spanHours <= 1) {
    return 'minutes'
  }
  if (spanHours <= 48) {
    return 'hours'
  }
  if (spanHours <= 24 * 60) {
    return 'days'
  }
  if (spanHours <= 24 * 365 * 2) {
    return 'months'
  }
  return 'years'
}

// The first month of the quarter containing `month` (0-based), and the month
// that starts the following quarter (12 → wrap into next year).
const quarterBounds = (month: number): { begin: number, end: number } => {
  const begin = Math.floor(month / 3) * 3
  return { begin, end: begin + 3 }
}

// Resolve a KSC symbolic timespan (e.g. '7_day', 'Last Quarter') to concrete
// start/end times plus a label format, mirroring
// KSC_PerformanceReportFactory.getBeginEndTime. `now` is injectable for tests.
export const kscTimespanToStartEndTime = (timespan: string, now: Date = new Date()): StartEndTime => {
  let begin: Date
  let end: Date

  const relative = RELATIVE[timespan]
  if (relative) {
    begin = sub(now, relative)
    end = now
  } else {
    // Calendar-aligned spans: zero the time-of-day on both ends first.
    const begin0 = startOfDay(now)
    const end0 = startOfDay(now)

    switch (timespan) {
      case 'Today':
        begin = begin0
        end = add(end0, { days: 1 })
        break
      case 'Yesterday':
        begin = sub(begin0, { days: 1 })
        end = end0
        break
      case 'Yesterday 9am-5pm':
        begin = set(sub(begin0, { days: 1 }), { hours: 9 })
        end = set(sub(end0, { days: 1 }), { hours: 17 })
        break
      case 'Yesterday 5pm-10pm':
        begin = set(sub(begin0, { days: 1 }), { hours: 17 })
        end = set(sub(end0, { days: 1 }), { hours: 22 })
        break
      case 'This Week':
      case 'Last Week': {
        begin = startOfWeek(begin0)
        end = set(add(startOfWeek(end0), { days: 6 }), { hours: 23, minutes: 59 })
        if (timespan === 'Last Week') {
          begin = sub(begin, { days: 7 })
          end = sub(end, { days: 7 })
        }
        break
      }
      case 'This Month':
        begin = setDate(begin0, 1)
        end = setDate(add(end0, { months: 1 }), 1)
        break
      case 'Last Month':
        begin = setDate(sub(begin0, { months: 1 }), 1)
        end = setDate(end0, 1)
        break
      case 'This Quarter':
      case 'Last Quarter': {
        const b1 = setDate(begin0, 1)
        const e1 = setDate(end0, 1)
        const { begin: bMonth, end: eMonth } = quarterBounds(getMonth(b1))
        begin = setMonth(b1, bMonth)
        // eMonth is 3..12; setMonth rolls 12 into January of the next year,
        // matching the Java case that bumps the year for Q4.
        end = setMonth(e1, eMonth)
        if (timespan === 'Last Quarter') {
          begin = sub(begin, { months: 3 })
          end = sub(end, { months: 3 })
        }
        break
      }
      case 'This Year':
        begin = setDate(setMonth(begin0, 0), 1)
        end = add(setDate(setMonth(end0, 0), 1), { years: 1 })
        break
      case 'Last Year':
        begin = sub(setDate(setMonth(begin0, 0), 1), { years: 1 })
        end = setDate(setMonth(end0, 0), 1)
        break
      default:
        // Unknown/invalid timespan: fall back to the last day rather than throw,
        // so a single bad graph can't break a whole report view.
        begin = sub(now, { days: 1 })
        end = now
    }
  }

  const startTime = getUnixTime(begin)
  const endTime = getUnixTime(end)
  return { startTime, endTime, format: pickFormat(startTime, endTime) }
}
