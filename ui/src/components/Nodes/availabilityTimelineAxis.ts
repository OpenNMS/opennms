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

import { startOfMinute, startOfHour, startOfDay, startOfMonth, startOfYear } from 'date-fns'
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
  /** Nominal length. Used only to choose a step; positions come from real instants. */
  approxMs: number
  unit: FloorUnit
  /** How many units each step advances. */
  stepAmount: number
  /** The first tick is pulled back to a multiple of this many units. */
  floorAmount: number
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
  { approxMs: 60_000, unit: 'minute', stepAmount: 1, floorAmount: 1, label: 'HH:mm' },
  { approxMs: 300_000, unit: 'minute', stepAmount: 5, floorAmount: 5, label: 'HH:mm' },
  { approxMs: 900_000, unit: 'minute', stepAmount: 15, floorAmount: 15, label: 'HH:mm' },
  { approxMs: 1_800_000, unit: 'minute', stepAmount: 30, floorAmount: 30, label: 'HH:mm' },
  { approxMs: 3_600_000, unit: 'hour', stepAmount: 1, floorAmount: 1, label: 'HH:mm' },
  { approxMs: 10_800_000, unit: 'hour', stepAmount: 3, floorAmount: 3, label: 'HH:mm' },
  { approxMs: 21_600_000, unit: 'hour', stepAmount: 6, floorAmount: 6, label: 'HH:mm' },
  { approxMs: 43_200_000, unit: 'hour', stepAmount: 12, floorAmount: 12, label: 'd MMM HH:mm' },
  { approxMs: 86_400_000, unit: 'day', stepAmount: 1, floorAmount: 1, label: 'd MMM' },
  // Floored to the day rather than to a multiple of seven: a multiple of the day-of-month would
  // restart awkwardly at the end of every month, and the start of the week would drag in a
  // locale's first-day-of-week for no benefit.
  { approxMs: 604_800_000, unit: 'day', stepAmount: 7, floorAmount: 1, label: 'd MMM' },
  { approxMs: 2_592_000_000, unit: 'month', stepAmount: 1, floorAmount: 1, label: 'MMM yyyy' },
  { approxMs: 7_862_400_000, unit: 'month', stepAmount: 3, floorAmount: 3, label: 'MMM yyyy' },
  { approxMs: 31_536_000_000, unit: 'year', stepAmount: 1, floorAmount: 1, label: 'yyyy' }
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
 * Advance by whole calendar units rather than by a duration in milliseconds.
 *
 * The date is the zoned representation, whose local fields are the display zone's wall time, so a
 * millisecond addition walks it through the BROWSER's transitions instead. On a browser fall-back
 * day a six hour step advanced the represented wall time by five hours once, and every label after
 * it sat off the step: 00:00 05:00 11:00 17:00 rather than 00:00 06:00 12:00 18:00. The epochs
 * stayed distinct and increasing, so the duplicate guard below could not see it.
 *
 * Incrementing the field instead keeps the wall clock on the step whatever either zone does.
 */
const stepBy = (date: Date, unit: FloorUnit, amount: number): Date => {
  const d = new Date(date)

  switch (unit) {
    case 'minute': d.setMinutes(d.getMinutes() + amount); break
    case 'hour': d.setHours(d.getHours() + amount); break
    case 'day': d.setDate(d.getDate() + amount); break
    case 'month': d.setMonth(d.getMonth() + amount); break
    case 'year': d.setFullYear(d.getFullYear() + amount); break
  }

  return d
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

  let zoned = floorTo(toZonedTime(window.start, zone), spec.unit, spec.floorAmount)
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

    zoned = stepBy(zoned, spec.unit, spec.stepAmount)
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
