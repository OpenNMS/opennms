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

import type { Duration } from 'date-fns'

export interface TimeOption {
  label: string
  /**
   * A date-fns Duration. The values MUST be numbers, not strings: date-fns sums
   * fields before applying them (`months + years * 12`, `days + weeks * 7`,
   * `minutes + hours * 60`), and a string first operand makes `+` concatenate
   * instead of add — so `{ minutes: '60' }` subtracted 600 minutes, not 60, and
   * `{ days: '7' }` subtracted 70 days. Multiplication coerces, which is why only
   * the hours-only options ever looked right.
   */
  time: Duration
}

/** Relative ranges offered by the time picker, newest first. */
export const TIME_RANGE_OPTIONS: TimeOption[] = [
  { label: 'Last hour', time: { hours: 1 }},
  { label: 'Last 2 hours', time: { hours: 2 }},
  { label: 'Last 4 hours', time: { hours: 4 }},
  { label: 'Last 8 hours', time: { hours: 8 }},
  { label: 'Last 12 hours', time: { hours: 12 }},
  { label: 'Last day', time: { hours: 24 }},
  { label: 'Last two days', time: { hours: 48 }},
  { label: 'Last week', time: { days: 7 }},
  { label: 'Last month', time: { months: 1 }},
  { label: 'Last three months', time: { months: 3 }},
  { label: 'Last six months', time: { months: 6 }},
  { label: 'Last year', time: { years: 1 }}
]

/** Hour-of-day choices for the custom range. */
export const HOUR_OPTIONS: TimeOption[] = [
  { label: '12 AM', time: { hours: 0 }},
  { label: '1 AM', time: { hours: 1 }},
  { label: '2 AM', time: { hours: 2 }},
  { label: '3 AM', time: { hours: 3 }},
  { label: '4 AM', time: { hours: 4 }},
  { label: '5 AM', time: { hours: 5 }},
  { label: '6 AM', time: { hours: 6 }},
  { label: '7 AM', time: { hours: 7 }},
  { label: '8 AM', time: { hours: 8 }},
  { label: '9 AM', time: { hours: 9 }},
  { label: '10 AM', time: { hours: 10 }},
  { label: '11 AM', time: { hours: 11 }},
  { label: '12 PM', time: { hours: 12 }},
  { label: '1 PM', time: { hours: 13 }},
  { label: '2 PM', time: { hours: 14 }},
  { label: '3 PM', time: { hours: 15 }},
  { label: '4 PM', time: { hours: 16 }},
  { label: '5 PM', time: { hours: 17 }},
  { label: '6 PM', time: { hours: 18 }},
  { label: '7 PM', time: { hours: 19 }},
  { label: '8 PM', time: { hours: 20 }},
  { label: '9 PM', time: { hours: 21 }},
  { label: '10 PM', time: { hours: 22 }},
  { label: '11 PM', time: { hours: 23 }}
]
