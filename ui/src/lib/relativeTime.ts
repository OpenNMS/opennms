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

export type AgeSeverity = 'success' | 'warn' | 'danger'

export const FRESH_MS = 5 * 60 * 1000
export const STALE_MS = 2 * 60 * 60 * 1000

// null for anything that is not a parseable instant, so callers can render a dash
export const toMillis = (value: string | number | Date | null | undefined): number | null => {
  if (value === null || value === undefined || value === '') {
    return null
  }
  const ms = value instanceof Date ? value.getTime() : new Date(value).getTime()
  return isNaN(ms) ? null : ms
}

// "11 s ago", "18 min ago", "3 h ago", "12 days ago". `now` is a parameter so a
// caller that re-renders on its own tick passes the same instant to every row.
export const relativeTimeSince = (value: string | number | Date | null | undefined, now: number = Date.now()): string | null => {
  const ms = toMillis(value)
  if (ms === null) {
    return null
  }
  const seconds = Math.max(0, Math.floor((now - ms) / 1000))
  if (seconds < 60) {
    return `${seconds} s ago`
  }
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) {
    return `${minutes} min ago`
  }
  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    return `${hours} h ago`
  }
  const days = Math.floor(hours / 24)
  return `${days} ${days === 1 ? 'day' : 'days'} ago`
}

// success within FRESH_MS, warn within STALE_MS, danger beyond that or when unknown
export const ageSeverity = (value: string | number | Date | null | undefined, now: number = Date.now()): AgeSeverity => {
  const ms = toMillis(value)
  if (ms === null) {
    return 'danger'
  }
  const age = now - ms
  return age < FRESH_MS ? 'success' : age < STALE_MS ? 'warn' : 'danger'
}

export const isOlderThan = (value: string | number | Date | null | undefined, maxAgeMs: number, now: number = Date.now()): boolean => {
  const ms = toMillis(value)
  return ms === null || now - ms > maxAgeMs
}

export const formatAbsolute = (value: string | number | Date | null | undefined): string => {
  const ms = toMillis(value)
  return ms === null ? '-' : new Date(ms).toLocaleString()
}
