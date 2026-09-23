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

import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useInfoStore } from '@/stores/infoStore'
import {
  DEFAULT_DATETIME_FORMAT,
  displayDateFormat,
  displayTimeZone,
  formatInDisplayZone
} from '@/lib/displayTimeZone'

const setConfig = (config?: { zoneId?: string; datetimeformat?: string }) => {
  const store = useInfoStore()
  store.info = { ...store.info, datetimeformatConfig: config } as any
}

describe('displayTimeZone', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('prefers the zone the server is configured with', () => {
    setConfig({ zoneId: 'Asia/Kolkata' })
    expect(displayTimeZone()).toBe('Asia/Kolkata')
  })

  it('falls back to the browser zone when the server has none', () => {
    setConfig(undefined)
    expect(displayTimeZone()).toBe(Intl.DateTimeFormat().resolvedOptions().timeZone)
  })

  it('falls back to an ISO-8601 pattern when the server has no format', () => {
    setConfig(undefined)
    expect(displayDateFormat()).toBe(DEFAULT_DATETIME_FORMAT)
  })

  it('uses the configured format when there is one', () => {
    setConfig({ datetimeformat: 'yyyy-MM-dd' })
    expect(displayDateFormat()).toBe('yyyy-MM-dd')
  })
})

describe('formatInDisplayZone', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  /*
   * date-fns-tz's format() reads the Date's system-local fields and uses `timeZone` only for zone
   * tokens; it does not convert. Without an explicit conversion, a configured zone that differs
   * from the browser's renders the browser's wall time stamped with the other zone's offset.
   */
  it('converts the instant into the requested zone rather than only relabelling it', () => {
    const noon = Date.UTC(2026, 5, 15, 12, 0, 0)

    expect(formatInDisplayZone(noon, 'HH:mm', 'UTC')).toBe('12:00')
    // UTC+5:30
    expect(formatInDisplayZone(noon, 'HH:mm', 'Asia/Kolkata')).toBe('17:30')
    // UTC-4 in June
    expect(formatInDisplayZone(noon, 'HH:mm', 'America/New_York')).toBe('08:00')
  })

  it('formats in the configured zone when none is passed', () => {
    setConfig({ zoneId: 'Asia/Kolkata', datetimeformat: 'HH:mm' })
    expect(formatInDisplayZone(Date.UTC(2026, 5, 15, 12, 0, 0))).toBe('17:30')
  })
})
