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
  { step: { minutes: 1 }, approxMs: 60_000, floor: 'minute', label: 'HH:mm' },
  { step: { minutes: 5 }, approxMs: 300_000, floor: 'minute', label: 'HH:mm' },
  { step: { minutes: 15 }, approxMs: 900_000, floor: 'minute', label: 'HH:mm' },
  { step: { minutes: 30 }, approxMs: 1_800_000, floor: 'minute', label: 'HH:mm' },
  { step: { hours: 1 }, approxMs: 3_600_000, floor: 'hour', label: 'HH:mm' },
  { step: { hours: 3 }, approxMs: 10_800_000, floor: 'hour', label: 'HH:mm' },
  { step: { hours: 6 }, approxMs: 21_600_000, floor: 'hour', label: 'HH:mm' },
  { step: { hours: 12 }, approxMs: 43_200_000, floor: 'hour', label: 'd MMM HH:mm' },
  { step: { days: 1 }, approxMs: 86_400_000, floor: 'day', label: 'd MMM' },
  { step: { days: 7 }, approxMs: 604_800_000, floor: 'day', label: 'd MMM' },
  { step: { months: 1 }, approxMs: 2_592_000_000, floor: 'month', label: 'MMM yyyy' },
  { step: { months: 3 }, approxMs: 7_862_400_000, floor: 'month', label: 'MMM yyyy' },
  { step: { years: 1 }, approxMs: 31_536_000_000, floor: 'year', label: 'yyyy' }
]

/**
 * The budget is on labels, and n intervals across a window yield n+1 fence posts, so the step has
 * to divide the span into at most MAX_TICKS - 1 intervals.
 */
export const chooseTickStep = (spanMs: number): TickStep =>
  TICK_STEPS.find(s => spanMs / s.approxMs <= MAX_TICKS - 1) ?? TICK_STEPS[TICK_STEPS.length - 1]

const floorTo = (date: Date, unit: FloorUnit): Date => {
  switch (unit) {
    case 'minute': return startOfMinute(date)
    case 'hour': return startOfHour(date)
    case 'day': return startOfDay(date)
    case 'month': return startOfMonth(date)
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
 */
export const buildTicks = (window: TimelineWindow, zone: string = displayTimeZone()): TimelineTick[] => {
  const span = window.end - window.start

  if (span <= 0) {
    return []
  }

  const spec = chooseTickStep(span)
  const ticks: TimelineTick[] = []

  let zoned = floorTo(toZonedTime(window.start, zone), spec.floor)

  // Bounded rather than while(true): a malformed step would otherwise spin forever.
  for (let i = 0; i <= MAX_TICKS * 4; i++) {
    const epoch = fromZonedTime(zoned, zone).getTime()

    if (epoch > window.end) {
      break
    }

    if (epoch >= window.start) {
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
