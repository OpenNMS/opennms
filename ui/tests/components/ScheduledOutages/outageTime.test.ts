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
import { buildOutageTime, defaultTimeSpanFields } from '@/components/ScheduledOutages/outageTime'

// BasicScheduleUtils selects the parser by the exact string length: 20 for the
// 'dd-MMM-yyyy HH:mm:ss' (specific) format, 8 for 'HH:mm:ss' (recurring).
describe('buildOutageTime', () => {
  const base = () => ({
    ...defaultTimeSpanFields(2026),
    startDay: '05',
    startMonth: 'Aug',
    startYear: '2026',
    startHour: '01',
    startMinute: '02',
    startSecond: '03',
    endDay: '06',
    endMonth: 'Sep',
    endYear: '2026',
    endHour: '23',
    endMinute: '59',
    endSecond: '59',
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
})
