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

import { RelativeTimeRange, StartEndTime } from '@/types'
import {
  DEFAULT_RANGE,
  relativeRangeOf,
  resolveRelativeRange,
  TIME_RANGE_OPTIONS
} from '@/components/Common/utils/timeRangeOptions'
import { TimelineWindow } from './availabilityTimelineModel'

/**
 * What the user picked, rather than the window it resolved to at the time.
 *
 * A relative range is re-resolved against the clock whenever it is used, so it cannot go stale; a
 * custom absolute range is a fixed window and is kept as one. Held in the node store so it survives
 * a node change -- the page rebuilds the availability panel for each node, and how the user chose
 * to look at a node is not data belonging to one.
 */
export type RangeSelection =
  | { kind: 'relative'; range: RelativeTimeRange }
  | { kind: 'absolute'; start: number; end: number }

/** The range the panel shows before anything is picked, matching the picker's own default. */
export const DEFAULT_SELECTION: RangeSelection = { kind: 'relative', range: DEFAULT_RANGE }

/** TimeControls works in seconds; everything downstream is in milliseconds. */
export const toMillis = (time: StartEndTime): TimelineWindow => ({
  start: Number(time.startTime) * 1000,
  end: Number(time.endTime) * 1000
})

export const selectionFromTime = (time: StartEndTime): RangeSelection =>
  time.range ? { kind: 'relative', range: time.range } : { kind: 'absolute', ...toMillis(time) }

export const windowFor = (sel: RangeSelection): TimelineWindow =>
  sel.kind === 'relative' ? toMillis(resolveRelativeRange(sel.range)) : { start: sel.start, end: sel.end }

/**
 * The picker's own wording for a range, so the heading and the picker's button agree. Building a
 * label out of unit and amount instead gave 'last 1 hours' for Last hour, and 'last 24 hours' for
 * Last day while the button said 'LAST DAY'.
 */
export const labelFor = (range: RelativeTimeRange | undefined): string => {
  if (!range) {
    return 'custom range'
  }

  const option = TIME_RANGE_OPTIONS.find((o) => {
    const r = relativeRangeOf(o)
    return r?.unit === range.unit && r?.amount === range.amount
  })

  if (option) {
    return option.label
  }

  // Not one of the offered options, so there is no label to borrow.
  const unit = range.amount === 1 ? range.unit.replace(/s$/, '') : range.unit
  return `Last ${range.amount} ${unit}`
}

/** The label for a whole selection, which is what seeds the picker's button on a fresh mount. */
export const selectionLabel = (sel: RangeSelection): string =>
  labelFor(sel.kind === 'relative' ? sel.range : undefined)
