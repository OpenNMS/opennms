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

import { describe, expect, it } from 'vitest'
import { parsePositiveIntegerId } from '@/lib/ids'

describe('parsePositiveIntegerId', () => {
  it('accepts positive integers', () => {
    expect(parsePositiveIntegerId('42')).toBe('42')
    expect(parsePositiveIntegerId('9007199254740991')).toBe('9007199254740991')
  })

  it('returns the canonical form', () => {
    expect(parsePositiveIntegerId('007')).toBe('7')
  })

  it('allows surrounding whitespace', () => {
    expect(parsePositiveIntegerId(' 42 ')).toBe('42')
  })

  it.each(['0', '000', '-1', '1.5', '1e3', '4 2', 'abc', '', '   '])('rejects %j', (raw) => {
    expect(parsePositiveIntegerId(raw)).toBeNull()
  })

  it('rejects null and undefined', () => {
    expect(parsePositiveIntegerId(null)).toBeNull()
    expect(parsePositiveIntegerId(undefined)).toBeNull()
  })

  it('rejects ids too large to be exact', () => {
    expect(parsePositiveIntegerId('9007199254740992')).toBeNull()
    expect(parsePositiveIntegerId('99999999999999999999')).toBeNull()
  })
})
