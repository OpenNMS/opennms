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

/**
 * IF-MIB interface state, named rather than reduced to up/down: lowerLayerDown
 * and down mean different things, and `up-values`/`down-values` are per-package
 * config the API does not expose, so any boolean would guess at their policy.
 */

/** ifAdminStatus, OID 1.3.6.1.2.1.2.2.1.7. */
const ADMIN_STATUS: Record<number, string> = {
  1: 'up',
  2: 'down',
  3: 'testing'
}

/** ifOperStatus, OID 1.3.6.1.2.1.2.2.1.8. */
const OPER_STATUS: Record<number, string> = {
  1: 'up',
  2: 'down',
  3: 'testing',
  4: 'unknown',
  5: 'dormant',
  6: 'notPresent',
  7: 'lowerLayerDown'
}

export const adminStatusName = (value?: number | null): string | undefined =>
  value == null ? undefined : ADMIN_STATUS[value] ?? `unrecognized (${value})`

export const operStatusName = (value?: number | null): string | undefined =>
  value == null ? undefined : OPER_STATUS[value] ?? `unrecognized (${value})`

/**
 * How the state got into the database, which decides how far to trust it.
 * Provisiond's node scan writes these columns daily; the SNMP Interface Poller
 * makes them fresher but is off by default. Only lastSnmpPoll tells them apart.
 */
export type StateSource = 'poller' | 'scan'

export interface InterfaceStateProvenance {
  source: StateSource
  /** Milliseconds since that write. */
  age: number
}

export const stateProvenance = (
  iface: { lastSnmpPoll?: number | null, lastCapsdPoll?: number | null },
  now: number
): InterfaceStateProvenance | undefined => {
  // A poller timestamp wins: it is both the more recent writer and the more
  // precise claim. OpenNMS leaves it null where the poller never ran.
  if (iface.lastSnmpPoll) {
    return { source: 'poller', age: Math.max(0, now - iface.lastSnmpPoll) }
  }
  if (iface.lastCapsdPoll) {
    return { source: 'scan', age: Math.max(0, now - iface.lastCapsdPoll) }
  }
  return undefined
}

const MINUTE = 60_000
const HOUR = 60 * MINUTE
const DAY = 24 * HOUR

/** Coarse on purpose: the point is which order of magnitude, not the exact gap. */
export const describeAge = (ms: number): string => {
  if (ms < MINUTE) {
    return 'seconds ago'
  }
  if (ms < HOUR) {
    const n = Math.round(ms / MINUTE)
    return `${n} minute${n === 1 ? '' : 's'} ago`
  }
  if (ms < DAY) {
    const n = Math.round(ms / HOUR)
    return `${n} hour${n === 1 ? '' : 's'} ago`
  }
  const n = Math.round(ms / DAY)
  return `${n} day${n === 1 ? '' : 's'} ago`
}

/**
 * The provenance line shown under the state. Named so a reader knows whether
 * they are looking at a live signal or at inventory: the poller's number is
 * worth acting on, a day-old scan is not.
 */
export const describeProvenance = (p: InterfaceStateProvenance): string =>
  p.source === 'poller'
    ? `polled ${describeAge(p.age)}`
    : `from the last node scan, ${describeAge(p.age)}`
