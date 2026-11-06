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
import { afterEach, describe, expect, it, vi } from 'vitest'

import { clockTo12Hour, clockToDate, dateToClock, dateToDay, dayToDate } from '@/lib/pickerTime'

describe('pickerTime', () => {
  it('round-trips a clock time through a Date', () => {
    const date = clockToDate('08:05')
    expect(date?.getHours()).toBe(8)
    expect(date?.getMinutes()).toBe(5)
    expect(dateToClock(date)).toBe('08:05')
  })

  it('round-trips a day', () => {
    expect(dateToDay(dayToDate('2026-03-08'))).toBe('2026-03-08')
    expect(dateToDay(dayToDate('2026-12-31'))).toBe('2026-12-31')
  })

  it('takes the picked wall-clock fields as they are', () => {
    expect(dateToClock(new Date(2026, 0, 1, 13, 7))).toBe('13:07')
  })

  it('maps empty or malformed values to null, and null to an empty string', () => {
    for (const value of ['', '8:00', '08:00:00']) {
      expect(clockToDate(value)).toBeNull()
    }
    expect(dayToDate('2026-3-8')).toBeNull()
    expect(dateToClock(null)).toBe('')
    expect(dateToDay(null)).toBe('')
  })

  it('shows a clock time in 12-hour form', () => {
    expect(clockTo12Hour('00:00')).toBe('12:00 am')
    expect(clockTo12Hour('08:05')).toBe('8:05 am')
    expect(clockTo12Hour('12:30')).toBe('12:30 pm')
    expect(clockTo12Hour('23:59')).toBe('11:59 pm')
    expect(clockTo12Hour('8:00')).toBe('')
  })

  describe('on the browser\'s daylight-saving day', () => {
    const zone = process.env.TZ
    afterEach(() => {
      process.env.TZ = zone
      vi.useRealTimers()
    })

    it('keeps every clock time, including the hour the clocks skip today', () => {
      process.env.TZ = 'America/New_York'
      vi.useFakeTimers({ toFake: ['Date'] })
      // 2026-03-08 is the spring-forward day in New York: 02:00-02:59 does not exist on it
      vi.setSystemTime(new Date('2026-03-08T12:00:00Z'))
      for (let h = 0; h < 24; h++) {
        for (const m of [0, 30, 59]) {
          const hhmm = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
          expect(dateToClock(clockToDate(hhmm))).toBe(hhmm)
        }
      }
    })
  })
})
