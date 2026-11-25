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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { refreshOptions, timeframeOptions, timeframeRange } from '@/lib/dashboardTimeframe'
import { TimeframePreset } from '@/types/dashboard'

const DAY = 24 * 3_600_000

describe('timeframeRange', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date(Date.parse('2026-07-15T12:00:00Z')))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  const at = (preset: TimeframePreset, from: string | null = null, to: string | null = null) =>
    timeframeRange({ preset, from, to }, Date.now(), 'UTC')

  const NY = 'America/New_York'
  const ny = (preset: TimeframePreset, now: string) => {
    const { start, end } = timeframeRange({ preset, from: null, to: null }, Date.parse(now), NY)
    return { start: new Date(start).toISOString(), end: new Date(end).toISOString() }
  }
  const utc = (preset: TimeframePreset, now: string) => {
    const { start, end } = timeframeRange({ preset, from: null, to: null }, Date.parse(now), 'UTC')
    return { start: new Date(start).toISOString(), end: new Date(end).toISOString() }
  }
  const iso = (s: string) => new Date(Date.parse(s)).toISOString()

  it('last 24 hours ends now and spans one day', () => {
    const { start, end } = at(TimeframePreset.Last24h)
    expect(end).toBe(Date.now())
    expect(end - start).toBe(DAY)
  })

  it('last 7 and 30 days are rolling windows', () => {
    const week = at(TimeframePreset.Last7Days)
    expect(week.end - week.start).toBe(7 * DAY)
    const month = at(TimeframePreset.Last30Days)
    expect(month.end - month.start).toBe(30 * DAY)
  })

  describe('calendar presets in America/New_York, two days after the DST start', () => {
    const now = '2026-03-10T16:00:00Z'

    it('today starts at local midnight', () => {
      expect(ny(TimeframePreset.Today, now)).toEqual({ start: iso('2026-03-10T04:00:00Z'), end: iso(now) })
    })

    it('yesterday is the previous local day', () => {
      expect(ny(TimeframePreset.Yesterday, now)).toEqual({ start: iso('2026-03-09T04:00:00Z'), end: iso('2026-03-10T04:00:00Z') })
    })

    it('this week starts on Monday', () => {
      expect(ny(TimeframePreset.ThisWeek, now)).toEqual({ start: iso('2026-03-09T04:00:00Z'), end: iso(now) })
    })

    it('last week spans the DST change', () => {
      expect(ny(TimeframePreset.LastWeek, now)).toEqual({ start: iso('2026-03-02T05:00:00Z'), end: iso('2026-03-09T04:00:00Z') })
    })

    it('this month starts on the first', () => {
      expect(ny(TimeframePreset.ThisMonth, now)).toEqual({ start: iso('2026-03-01T05:00:00Z'), end: iso(now) })
    })

    it('last month is the previous calendar month', () => {
      expect(ny(TimeframePreset.LastMonth, now)).toEqual({ start: iso('2026-02-01T05:00:00Z'), end: iso('2026-03-01T05:00:00Z') })
    })

    it('on a Sunday this week started the previous Monday', () => {
      expect(ny(TimeframePreset.ThisWeek, '2026-03-08T15:00:00Z').start).toBe(iso('2026-03-02T05:00:00Z'))
    })
  })

  describe('calendar presets in UTC across a year boundary', () => {
    const now = '2026-01-05T12:00:00Z'

    it('this week starts on the Monday it is', () => {
      expect(utc(TimeframePreset.ThisWeek, now).start).toBe(iso('2026-01-05T00:00:00Z'))
    })

    it('last week starts in the previous year', () => {
      expect(utc(TimeframePreset.LastWeek, now)).toEqual({ start: iso('2025-12-29T00:00:00Z'), end: iso('2026-01-05T00:00:00Z') })
    })

    it('last month is December', () => {
      expect(utc(TimeframePreset.LastMonth, now)).toEqual({ start: iso('2025-12-01T00:00:00Z'), end: iso('2026-01-01T00:00:00Z') })
    })
  })

  it('custom uses the given range and falls back when absent', () => {
    const from = new Date(2026, 0, 1).toISOString()
    const to = new Date(2026, 0, 2).toISOString()
    expect(at(TimeframePreset.Custom, from, to)).toEqual({ start: Date.parse(from), end: Date.parse(to) })

    const fallback = at(TimeframePreset.Custom)
    expect(fallback.end).toBe(Date.now())
    expect(fallback.end - fallback.start).toBe(DAY)
  })

  it('an unknown preset behaves like last 24 hours', () => {
    const { start, end } = timeframeRange({ preset: 'bogus' as TimeframePreset, from: null, to: null }, Date.now(), 'UTC')
    expect(end - start).toBe(DAY)
  })
})

describe('timeframe and refresh option lists', () => {
  it('cover every preset exactly once', () => {
    const values = timeframeOptions.map(o => o.value)
    expect(new Set(values).size).toBe(values.length)
    for (const preset of Object.values(TimeframePreset)) {
      expect(values).toContain(preset)
    }
  })

  it('refresh options start with off and ascend', () => {
    expect(refreshOptions[0].value).toBe(0)
    const rest = refreshOptions.slice(1).map(o => o.value)
    expect([...rest].sort((a, b) => a - b)).toEqual(rest)
  })
})
