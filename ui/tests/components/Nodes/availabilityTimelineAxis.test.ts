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
import { getUnixTime, sub } from 'date-fns'
import {
  buildTicks,
  chooseTickStep,
  MAX_TICKS,
  tickEdgeClass
} from '@/components/Nodes/availabilityTimelineAxis'
import { TIME_RANGE_OPTIONS } from '@/components/Common/utils/timeRangeOptions'
import { TimelineWindow } from '@/components/Nodes/availabilityTimelineModel'

const HOUR = 3_600_000
const UTC = 'UTC'
const NOW = new Date('2026-06-15T12:00:00Z')

const windowFor = (option: (typeof TIME_RANGE_OPTIONS)[number]): TimelineWindow => ({
  start: getUnixTime(sub(NOW, option.time)) * 1000,
  end: getUnixTime(NOW) * 1000
})

describe('buildTicks', () => {
  // Every range the picker offers has to produce a legible axis; this is the table that keeps a
  // new option from silently producing 400 labels or none.
  it.each(TIME_RANGE_OPTIONS.map(o => [o.label, o] as const))(
    'produces a bounded, ordered axis for %s',
    (_label, option) => {
      const ticks = buildTicks(windowFor(option), UTC)

      expect(ticks.length).toBeGreaterThanOrEqual(1)
      expect(ticks.length).toBeLessThanOrEqual(MAX_TICKS)

      ticks.forEach((t) => {
        expect(t.pct).toBeGreaterThanOrEqual(0)
        expect(t.pct).toBeLessThanOrEqual(100)
        expect(t.label).not.toBe('')
      })

      const positions = ticks.map(t => t.pct)
      expect(positions).toEqual([...positions].sort((a, b) => a - b))
      expect(new Set(positions).size).toBe(positions.length)
    }
  )

  it('labels an hour-scale window as a time of day', () => {
    const end = Date.UTC(2026, 5, 15, 12, 0, 0)
    const ticks = buildTicks({ start: end - HOUR, end }, UTC)
    ticks.forEach(t => expect(t.label).toMatch(/^\d{2}:\d{2}$/))
  })

  it('labels a week-scale window as a day and month', () => {
    const end = Date.UTC(2026, 5, 15, 12, 0, 0)
    const ticks = buildTicks({ start: end - 7 * 24 * HOUR, end }, UTC)
    ticks.forEach(t => expect(t.label).toMatch(/^\d{1,2} [A-Z][a-z]{2}$/))
  })

  it('labels a year-scale window by month and year', () => {
    const end = Date.UTC(2026, 5, 15, 12, 0, 0)
    const ticks = buildTicks({ start: end - 365 * 24 * HOUR, end }, UTC)
    ticks.forEach(t => expect(t.label).toMatch(/^[A-Z][a-z]{2} \d{4}$/))
  })

  it('labels a multi-year window as a year', () => {
    const end = Date.UTC(2026, 5, 15, 12, 0, 0)
    const ticks = buildTicks({ start: end - 6 * 365 * 24 * HOUR, end }, UTC)
    ticks.forEach(t => expect(t.label).toMatch(/^\d{4}$/))
  })

  it('puts ticks on round boundaries in the display zone', () => {
    const end = Date.UTC(2026, 5, 15, 12, 17, 33)
    const ticks = buildTicks({ start: end - 12 * HOUR, end }, UTC)
    ticks.forEach(t => expect(t.label).toMatch(/:00$/))
  })

  // A zone offset at a half hour from UTC is what catches flooring in the wrong zone: the labels
  // would come out at :30 instead of on the hour.
  it('rounds to the hour in a zone offset by half an hour', () => {
    const end = Date.UTC(2026, 5, 15, 12, 17, 33)
    const ticks = buildTicks({ start: end - 12 * HOUR, end }, 'Asia/Kolkata')

    expect(ticks.length).toBeGreaterThan(0)
    ticks.forEach(t => expect(t.label).toMatch(/:00$/))
  })

  /*
   * The regression guard against laying ticks out at index/count.
   *
   * This window spans the US spring-forward, where one calendar day is 23 real hours. Because a
   * tick's position comes from its own instant, the gaps between consecutive positions are not all
   * equal -- and that inequality is exactly what keeps the axis aligned with the bars, which are
   * positioned from real timestamps. An evenly-spaced axis would drift by an hour after the
   * transition.
   */
  it('keeps ticks on real instants across a daylight-saving transition', () => {
    const zone = 'America/New_York'
    // 2026-03-08 is the US spring-forward.
    const start = Date.UTC(2026, 2, 5, 5, 0, 0)
    const end = Date.UTC(2026, 2, 12, 4, 0, 0)
    const ticks = buildTicks({ start, end }, zone)

    expect(ticks.length).toBeGreaterThan(2)

    const gaps = ticks.slice(1).map((t, i) => Number((t.pct - ticks[i].pct).toFixed(6)))
    expect(new Set(gaps).size).toBeGreaterThan(1)

    // And every position is still its own instant's share of the window.
    const span = end - start
    ticks.forEach((t) => {
      expect(t.pct).toBeCloseTo(((t.epoch - start) / span) * 100, 6)
    })
  })

  // A step coarser than a minute has to land on a multiple of itself, not merely on a whole
  // minute: flooring 10:23:17 to 10:23 and stepping by 15 gives 10:38, 10:53, 11:08.
  it('puts sub-hour ticks on multiples of the step, not on arbitrary minutes', () => {
    const start = Date.UTC(2026, 5, 15, 10, 23, 17)
    const ticks = buildTicks({ start, end: start + HOUR }, UTC)

    expect(ticks.length).toBeGreaterThan(0)
    ticks.forEach(t => expect(['00', '15', '30', '45']).toContain(t.label.slice(-2)))
  })

  it('puts multi-hour ticks on multiples of the step', () => {
    const start = Date.UTC(2026, 5, 15, 10, 23, 17)
    // 12 hours selects the 3-hour step, which should land on 00/03/06/09/12/...
    const ticks = buildTicks({ start, end: start + 12 * HOUR }, UTC)

    expect(ticks.length).toBeGreaterThan(0)
    ticks.forEach((t) => {
      expect(t.label.slice(-2)).toBe('00')
      expect(Number(t.label.slice(0, 2)) % 3).toBe(0)
    })
  })

  /*
   * The hour and minute steps advance the zoned representation by a fixed number of milliseconds,
   * which walks wall-clock time. Across a spring-forward the skipped wall hour does not exist, and
   * both it and the hour after it resolve to the same instant -- so two ticks share an epoch, which
   * is also the row key, and the labels overlap.
   *
   * It only bites when the display zone differs from the browser's, which is why the day-step test
   * above did not catch it: a day step advances by calendar arithmetic, not by milliseconds.
   *
   * The test environment runs in America/New_York, so Europe/London gives a differing display zone
   * with a spring-forward on 2026-03-29, 01:00 GMT -> 02:00 BST.
   */
  it('produces no duplicate ticks across a spring-forward in a differing display zone', () => {
    // Five hours selects the 1 hour step, so the walk lands on the wall hour that does not exist.
    const start = Date.UTC(2026, 2, 28, 23, 0, 0)
    const ticks = buildTicks({ start, end: start + 5 * HOUR }, 'Europe/London')

    const epochs = ticks.map(t => t.epoch)
    expect(new Set(epochs).size).toBe(epochs.length)

    const positions = ticks.map(t => t.pct)
    expect(positions).toEqual([...positions].sort((a, b) => a - b))
    expect(new Set(positions).size).toBe(positions.length)
  })

  it('produces no duplicate ticks across a spring-forward at a sub-hour step', () => {
    const start = Date.UTC(2026, 2, 29, 0, 30, 0)
    const ticks = buildTicks({ start, end: start + HOUR }, 'Europe/London')

    const epochs = ticks.map(t => t.epoch)
    expect(new Set(epochs).size).toBe(epochs.length)
  })

  it('returns no ticks for an empty window', () => {
    expect(buildTicks({ start: 1000, end: 1000 }, UTC)).toEqual([])
  })
})

describe('chooseTickStep', () => {
  it('picks the first step coarse enough to stay within the label budget', () => {
    expect(chooseTickStep(HOUR).step).toEqual({ minutes: 15 })
    expect(chooseTickStep(24 * HOUR).step).toEqual({ hours: 6 })
  })

  it('falls back to the coarsest step for an absurdly long window', () => {
    expect(chooseTickStep(500 * 365 * 24 * HOUR).step).toEqual({ years: 1 })
  })
})

describe('tickEdgeClass', () => {
  it('anchors the outermost labels inward so they are not clipped', () => {
    expect(tickEdgeClass({ epoch: 0, pct: 0, label: 'a' })).toBe('axis__tick--first')
    expect(tickEdgeClass({ epoch: 0, pct: 100, label: 'a' })).toBe('axis__tick--last')
    expect(tickEdgeClass({ epoch: 0, pct: 50, label: 'a' })).toBe('')
  })
})
