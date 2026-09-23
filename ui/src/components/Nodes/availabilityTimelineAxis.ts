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

import { add, Duration, startOfMinute, startOfHour, startOfDay, startOfMonth, startOfYear } from 'date-fns'
import { fromZonedTime, toZonedTime } from 'date-fns-tz'
import { displayTimeZone, formatInDisplayZone } from '@/lib/displayTimeZone'
import { TimelineWindow } from './availabilityTimelineModel'

export interface TimelineTick {
  epoch: number
  /** Position along the strip, 0-100. */
  pct: number
  label: string
}

type FloorUnit = 'minute' | 'hour' | 'day' | 'month' | 'year'

interface TickStep {
  step: Duration
  /** Nominal length. Used only to choose a step; positions come from real instants. */
  approxMs: number
  floor: FloorUnit
  /** How many `floor` units the step spans, so the first tick can land on a multiple of it. */
  amount: number
  label: string
}

/**
 * Target number of labels. Eight keeps the wider labels from colliding in a half-width panel and
 * bounds the number of gridline elements drawn per service row.
 */
export const MAX_TICKS = 8

/**
 * Replaces the 47 timescale descriptors the server carried. Thirteen steps cover a minute to a
 * year; the first one coarse enough to fit within MAX_TICKS wins.
 */
const TICK_STEPS: TickStep[] = [
  { step: { minutes: 1 }, approxMs: 60_000, floor: 'minute', amount: 1, label: 'HH:mm' },
  { step: { minutes: 5 }, approxMs: 300_000, floor: 'minute', amount: 5, label: 'HH:mm' },
  { step: { minutes: 15 }, approxMs: 900_000, floor: 'minute', amount: 15, label: 'HH:mm' },
  { step: { minutes: 30 }, approxMs: 1_800_000, floor: 'minute', amount: 30, label: 'HH:mm' },
  { step: { hours: 1 }, approxMs: 3_600_000, floor: 'hour', amount: 1, label: 'HH:mm' },
  { step: { hours: 3 }, approxMs: 10_800_000, floor: 'hour', amount: 3, label: 'HH:mm' },
  { step: { hours: 6 }, approxMs: 21_600_000, floor: 'hour', amount: 6, label: 'HH:mm' },
  { step: { hours: 12 }, approxMs: 43_200_000, floor: 'hour', amount: 12, label: 'd MMM HH:mm' },
  { step: { days: 1 }, approxMs: 86_400_000, floor: 'day', amount: 1, label: 'd MMM' },
  { step: { days: 7 }, approxMs: 604_800_000, floor: 'day', amount: 1, label: 'd MMM' },
  { step: { months: 1 }, approxMs: 2_592_000_000, floor: 'month', amount: 1, label: 'MMM yyyy' },
  { step: { months: 3 }, approxMs: 7_862_400_000, floor: 'month', amount: 3, label: 'MMM yyyy' },
  { step: { years: 1 }, approxMs: 31_536_000_000, floor: 'year', amount: 1, label: 'yyyy' }
]

/**
 * The budget is on labels, and n intervals across a window yield n+1 fence posts, so the step has
 * to divide the span into at most MAX_TICKS - 1 intervals.
 */
export const chooseTickStep = (spanMs: number): TickStep =>
  TICK_STEPS.find(s => spanMs / s.approxMs <= MAX_TICKS - 1) ?? TICK_STEPS[TICK_STEPS.length - 1]

/**
 * Floor to a multiple of the step, not merely to the start of its unit.
 *
 * Flooring a 15 minute step only to the minute puts the first tick wherever the window happens to
 * begin, so a window opening at 10:23:17 labels 10:38, 10:53, 11:08 rather than 10:30, 10:45,
 * 11:00. The same applies to the multi-hour steps.
 *
 * Days are floored to the day and no further: a 7 day step aligned to a multiple of the
 * day-of-month would restart awkwardly at the end of every month, and aligning to the start of the
 * week would drag in a locale's first-day-of-week for no benefit.
 *
 * The date is the zoned representation, so the local getters and setters here read and write the
 * wall time in the display zone.
 */
const floorTo = (date: Date, unit: FloorUnit, amount: number): Date => {
  const floorField = (d: Date, get: () => number, set: (v: number) => void) => {
    set(Math.floor(get() / amount) * amount)
    return d
  }

  switch (unit) {
    case 'minute': {
      const d = startOfMinute(date)
      return floorField(d, () => d.getMinutes(), v => d.setMinutes(v))
    }
    case 'hour': {
      const d = startOfHour(date)
      return floorField(d, () => d.getHours(), v => d.setHours(v))
    }
    case 'day': return startOfDay(date)
    case 'month': {
      const d = startOfMonth(date)
      return floorField(d, () => d.getMonth(), v => d.setMonth(v))
    }
    case 'year': return startOfYear(date)
  }
}

/**
 * Tick marks for a window.
 *
 * Two things are load-bearing here.
 *
 * The step is floored and advanced *in the display zone* and only then converted back, so a server
 * zone at a half-hour offset from the browser still gets labels on the hour rather than at :30.
 *
 * Every tick's position is computed from its own instant rather than from its index. That is what
 * makes daylight-saving correct for free: a one-day step across a spring-forward boundary is
 * twenty-three real hours, and the tick lands twenty-three twenty-fourths of the way along, which
 * is where the outage data for that moment actually is. Laying ticks out at index/count would put
 * it at a flat fraction and skew every bar after the transition by an hour.
 *
 * Ticks are then kept only while the instant strictly advances. The walk is over wall time, and a
 * wall time skipped by a spring-forward does not exist: it and the hour after it resolve to the
 * same instant, so without this two ticks would share an epoch -- which is also the row key, so Vue
 * would warn about a duplicate and the labels would sit on top of each other. Dropping the skipped
 * one is right on its own terms, too: that wall time never happened. The same guard covers a
 * fall-back, where an ambiguous wall time could otherwise resolve backwards.
 */
export const buildTicks = (window: TimelineWindow, zone: string = displayTimeZone()): TimelineTick[] => {
  const span = window.end - window.start

  if (span <= 0) {
    return []
  }

  const spec = chooseTickStep(span)
  const ticks: TimelineTick[] = []

  let zoned = floorTo(toZonedTime(window.start, zone), spec.floor, spec.amount)
  let previousEpoch = -Infinity

  // Bounded rather than while(true): a malformed step would otherwise spin forever.
  for (let i = 0; i <= MAX_TICKS * 4; i++) {
    const epoch = fromZonedTime(zoned, zone).getTime()

    if (epoch > window.end) {
      break
    }

    if (epoch >= window.start && epoch > previousEpoch) {
      previousEpoch = epoch
      ticks.push({
        epoch,
        pct: ((epoch - window.start) / span) * 100,
        label: formatInDisplayZone(epoch, spec.label, zone)
      })
    }

    zoned = add(zoned, spec.step)
  }

  return ticks
}

/**
 * Which edge, if any, a tick's label sits against. The label is centred on its tick, so the first
 * and last would hang outside the column and be clipped; these classes anchor them inward.
 */
export const tickEdgeClass = (tick: TimelineTick): string => {
  if (tick.pct < 4) {
    return 'axis__tick--first'
  }

  if (tick.pct > 96) {
    return 'axis__tick--last'
  }

  return ''
}
