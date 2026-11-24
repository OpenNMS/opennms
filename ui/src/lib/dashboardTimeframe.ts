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

import { startOfDay, startOfMonth, startOfWeek, subDays, subMonths, subWeeks } from 'date-fns'
import { fromZonedTime, toZonedTime } from 'date-fns-tz'
import { displayTimeZone } from '@/lib/displayTimeZone'
import { TimeframePreset, type Timeframe } from '@/types/dashboard'

export const timeframeOptions: { label: string; value: TimeframePreset }[] = [
  { label: 'Last 24 hours', value: TimeframePreset.Last24h },
  { label: 'Today', value: TimeframePreset.Today },
  { label: 'Yesterday', value: TimeframePreset.Yesterday },
  { label: 'This week', value: TimeframePreset.ThisWeek },
  { label: 'Last week', value: TimeframePreset.LastWeek },
  { label: 'Last 7 days', value: TimeframePreset.Last7Days },
  { label: 'Last 30 days', value: TimeframePreset.Last30Days },
  { label: 'This month', value: TimeframePreset.ThisMonth },
  { label: 'Last month', value: TimeframePreset.LastMonth },
  { label: 'Custom range', value: TimeframePreset.Custom }
]

export const refreshOptions: { label: string; value: number }[] = [
  { label: 'Off', value: 0 },
  { label: 'Every 30 seconds', value: 30 },
  { label: 'Every minute', value: 60 },
  { label: 'Every 2 minutes', value: 120 },
  { label: 'Every 5 minutes', value: 300 }
]

export const timeframeRange = (
  tf: Timeframe,
  now: number = Date.now(),
  zone: string = displayTimeZone()
): { start: number; end: number } => {
  const day = 24 * 3_600_000
  // calendar math runs on the zone's wall-clock fields, then maps back to an instant
  const local = (): Date => toZonedTime(now, zone)
  const instant = (d: Date): number => fromZonedTime(d, zone).getTime()
  const todayStart = (): number => instant(startOfDay(local()))
  const weekStart = (): Date => startOfWeek(local(), { weekStartsOn: 1 })
  const monthStart = (): Date => startOfMonth(local())

  switch (tf.preset) {
    case TimeframePreset.Today:
      return { start: todayStart(), end: now }
    case TimeframePreset.Yesterday:
      return { start: instant(subDays(startOfDay(local()), 1)), end: todayStart() }
    case TimeframePreset.ThisWeek:
      return { start: instant(weekStart()), end: now }
    case TimeframePreset.LastWeek:
      return { start: instant(subWeeks(weekStart(), 1)), end: instant(weekStart()) }
    case TimeframePreset.Last7Days:
      return { start: now - 7 * day, end: now }
    case TimeframePreset.Last30Days:
      return { start: now - 30 * day, end: now }
    case TimeframePreset.ThisMonth:
      return { start: instant(monthStart()), end: now }
    case TimeframePreset.LastMonth:
      return { start: instant(subMonths(monthStart(), 1)), end: instant(monthStart()) }
    case TimeframePreset.Custom:
      return { start: tf.from ? Date.parse(tf.from) : now - day, end: tf.to ? Date.parse(tf.to) : now }
    case TimeframePreset.Last24h:
    default:
      return { start: now - day, end: now }
  }
}
