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

import { describe, expect, it } from 'vitest'
import {
  DAYS_OF_MONTH,
  buildOutageTime,
  defaultTimeSpanFields,
  formatSpecific,
  formatTimeOfDay
} from '@/components/ScheduledOutages/outageTime'

// BasicScheduleUtils selects the parser by the exact string length: 20 for the
// 'dd-MMM-yyyy HH:mm:ss' (specific) format, 8 for 'HH:mm:ss' (recurring).
describe('buildOutageTime', () => {
  const base = () => ({
    ...defaultTimeSpanFields(2026),
    start: new Date(2026, 7, 5, 1, 2, 3),
    end: new Date(2026, 8, 6, 23, 59, 59),
    day: 'wednesday'
  })

  it('formats a specific span as dd-MMM-yyyy HH:mm:ss (length 20, no day)', () => {
    const t = buildOutageTime('specific', base())
    expect(t.begins).toBe('05-Aug-2026 01:02:03')
    expect(t.ends).toBe('06-Sep-2026 23:59:59')
    expect(t.begins.length).toBe(20)
    expect(t.day).toBeUndefined()
  })

  it('formats a daily span as HH:mm:ss (length 8, no day)', () => {
    const t = buildOutageTime('daily', base())
    expect(t.begins).toBe('01:02:03')
    expect(t.ends).toBe('23:59:59')
    expect(t.begins.length).toBe(8)
    expect(t.day).toBeUndefined()
  })

  it('carries the weekday name for a weekly span', () => {
    const t = buildOutageTime('weekly', base())
    expect(t.begins).toBe('01:02:03')
    expect(t.day).toBe('wednesday')
  })

  it('carries the day-of-month for a monthly span', () => {
    const t = buildOutageTime('monthly', { ...base(), day: '15' })
    expect(t.begins).toBe('01:02:03')
    expect(t.day).toBe('15')
  })

  // Regression: a single-digit day or hour must still be zero-padded, or the
  // length-keyed Java parser misreads the string.
  it('pads every field, so the default (1 January, midnight) span is 20 chars', () => {
    const t = buildOutageTime('specific', defaultTimeSpanFields(2026))
    expect(t.begins).toBe('01-Jan-2026 00:00:00')
    expect(t.ends).toBe('01-Jan-2026 23:59:59')
    expect(formatSpecific(new Date(2026, 0, 9, 7, 8, 9))).toBe('09-Jan-2026 07:08:09')
    expect(formatTimeOfDay(new Date(2026, 0, 9, 7, 8, 9))).toBe('07:08:09')
  })

  it('keeps unpadded 1..31 day-of-month values for the monthly attribute', () => {
    expect(DAYS_OF_MONTH[0].value).toBe('1')
    expect(DAYS_OF_MONTH[8].value).toBe('9')
  })
})
