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

import { format as fnsFormat, toZonedTime } from 'date-fns-tz'
import { useInfoStore } from '@/stores/infoStore'

/** What v-date falls back to when the server has no datetime format configured. */
export const DEFAULT_DATETIME_FORMAT = 'yyyy-MM-dd\'T\'HH:mm:ssxxx'

/**
 * The zone the UI renders timestamps in: the server's configured zone, falling back to the
 * browser's. Extracted from the v-date directive so anything that formats a date outside a
 * directive -- a chart axis, a tooltip string -- names the same clock as the rest of the page.
 *
 * Read at call time rather than cached: the info store is populated asynchronously at app start,
 * so a value captured at module scope would be the browser fallback on a cold load.
 */
export const displayTimeZone = (): string =>
  useInfoStore().info.datetimeformatConfig?.zoneId || Intl.DateTimeFormat().resolvedOptions().timeZone

/** The server's configured display format, falling back to ISO-8601 with an offset. */
export const displayDateFormat = (): string =>
  useInfoStore().info.datetimeformatConfig?.datetimeformat || DEFAULT_DATETIME_FORMAT

/**
 * Format an instant in the display zone. `pattern` defaults to the server's configured format;
 * callers that need a specific shape -- an axis label, say -- pass their own.
 *
 * The toZonedTime call is load-bearing. date-fns-tz's format() reads the Date's *system-local*
 * fields and uses `timeZone` only to resolve zone tokens such as xxx and z; it does not convert.
 * Without the conversion, a configured zone that differs from the browser's renders the browser's
 * wall time stamped with the configured zone's offset -- the wrong time and a misleading offset.
 */
export const formatInDisplayZone = (
  value: number | Date,
  pattern?: string,
  zone?: string
): string => {
  const tz = zone ?? displayTimeZone()
  return fnsFormat(toZonedTime(value, tz), pattern ?? displayDateFormat(), { timeZone: tz })
}
