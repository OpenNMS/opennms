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
import { timeframeRange } from '@/lib/dashboardTimeframe'
import { timeframeOptions, refreshOptions } from '@/lib/dashboardTimeframe'
import { TimeframePreset } from '@/types/dashboard'

const HOUR = 3_600_000
const DAY = 24 * HOUR

describe('timeframeRange', () => {
  beforeEach(() => {
    // a fixed noon so start-of-day math is unambiguous
    vi.useFakeTimers()
    vi.setSystemTime(new Date(2026, 6, 15, 12, 0, 0))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  const at = (preset: TimeframePreset, from: string | null = null, to: string | null = null) =>
    timeframeRange({ preset, from, to })

  it('last 24 hours ends now and spans one day', () => {
    const { start, end } = at(TimeframePreset.Last24h)
    expect(end).toBe(Date.now())
    expect(end - start).toBe(DAY)
  })

  it('today starts at midnight', () => {
    const { start, end } = at(TimeframePreset.Today)
    expect(new Date(start).getHours()).toBe(0)
    expect(end).toBe(Date.now())
    expect(end - start).toBe(12 * HOUR)
  })

  it('yesterday is the full previous day', () => {
    const { start, end } = at(TimeframePreset.Yesterday)
    expect(end - start).toBe(DAY)
    expect(new Date(start).getDate()).toBe(14)
    expect(new Date(end).getHours()).toBe(0)
  })

  it('last week is the seven days before the last seven', () => {
    const { start, end } = at(TimeframePreset.LastWeek)
    expect(end).toBe(Date.now() - 7 * DAY)
    expect(end - start).toBe(7 * DAY)
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
    const { start, end } = timeframeRange({ preset: 'bogus' as TimeframePreset, from: null, to: null })
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
