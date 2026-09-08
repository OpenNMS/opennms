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

import { describe, it, expect } from 'vitest'
import {
  adminStatusName,
  describeAge,
  describeProvenance,
  operStatusName,
  stateProvenance
} from '@/components/Topology/interfaceState'

describe('IF-MIB state names', () => {
  it('names every ifOperStatus value rather than reducing them to up/down', () => {
    expect([1, 2, 3, 4, 5, 6, 7].map(v => operStatusName(v))).toEqual([
      'up', 'down', 'testing', 'unknown', 'dormant', 'notPresent', 'lowerLayerDown'
    ])
  })

  it('keeps lowerLayerDown distinct from down, which is the useful difference', () => {
    expect(operStatusName(7)).not.toBe(operStatusName(2))
  })

  it('names the three ifAdminStatus values', () => {
    expect([1, 2, 3].map(v => adminStatusName(v))).toEqual(['up', 'down', 'testing'])
  })

  it('says so rather than guessing when a device reports something else', () => {
    expect(operStatusName(9)).toBe('unrecognized (9)')
  })

  it('is undefined when the field is absent, so no row is rendered', () => {
    expect(operStatusName(undefined)).toBeUndefined()
    expect(operStatusName(null)).toBeUndefined()
    expect(adminStatusName(undefined)).toBeUndefined()
  })

  // 0 is not a valid IF-MIB value, but it must not be mistaken for absent.
  it('does not treat 0 as absent', () => {
    expect(operStatusName(0)).toBe('unrecognized (0)')
  })
})

describe('stateProvenance', () => {
  const now = 1_000_000_000

  it('prefers the poller timestamp, being the fresher and more precise writer', () => {
    expect(stateProvenance({ lastSnmpPoll: now - 90_000, lastCapsdPoll: now - 6 * 3600_000 }, now))
      .toEqual({ source: 'poller', age: 90_000 })
  })

  it('falls back to the node scan, which is the only writer by default', () => {
    expect(stateProvenance({ lastSnmpPoll: null, lastCapsdPoll: now - 6 * 3600_000 }, now))
      .toEqual({ source: 'scan', age: 6 * 3600_000 })
  })

  it('is undefined when nothing has written the row, so no age is claimed', () => {
    expect(stateProvenance({ lastSnmpPoll: null, lastCapsdPoll: null }, now)).toBeUndefined()
  })

  it('never reports a negative age from a clock skewed the other way', () => {
    expect(stateProvenance({ lastSnmpPoll: now + 5000 }, now)?.age).toBe(0)
  })
})

describe('describeAge', () => {
  it('is coarse on purpose', () => {
    expect(describeAge(20_000)).toBe('seconds ago')
    expect(describeAge(90_000)).toBe('2 minutes ago')
    expect(describeAge(60_000)).toBe('1 minute ago')
    expect(describeAge(6 * 3600_000)).toBe('6 hours ago')
    expect(describeAge(26 * 3600_000)).toBe('1 day ago')
    expect(describeAge(3 * 86_400_000)).toBe('3 days ago')
  })
})

describe('describeProvenance', () => {
  // The wording is the whole point: one of these is worth acting on and the
  // other is inventory, and the numbers themselves look identical.
  it('distinguishes a live poll from a daily scan', () => {
    expect(describeProvenance({ source: 'poller', age: 90_000 })).toBe('polled 2 minutes ago')
    expect(describeProvenance({ source: 'scan', age: 6 * 3600_000 }))
      .toBe('from the last node scan, 6 hours ago')
  })
})
