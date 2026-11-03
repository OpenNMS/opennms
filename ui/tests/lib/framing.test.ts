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

import { describe, expect, it } from 'vitest'
import { hidesMenuInFrame, isFramed } from '@/lib/framing'

describe('isFramed', () => {
  it('returns false at top level', () => {
    const win = {} as Window
    Object.defineProperty(win, 'self', { value: win })
    Object.defineProperty(win, 'top', { value: win })
    expect(isFramed(win)).toBe(false)
  })

  it('returns true when self !== top', () => {
    const win = { self: {}, top: {}} as unknown as Window
    expect(isFramed(win)).toBe(true)
  })

  it('returns true when accessing top throws', () => {
    const win = {} as Window
    Object.defineProperty(win, 'self', { value: win })
    Object.defineProperty(win, 'top', {
      get: () => {
        throw new DOMException('Blocked a frame', 'SecurityError')
      }
    })
    expect(isFramed(win)).toBe(true)
  })
})

describe('hidesMenuInFrame', () => {
  const framed = (name: string) => ({ self: {}, top: {}, name }) as unknown as Window

  it('returns false at top level', () => {
    const win = { name: '' } as Window
    Object.defineProperty(win, 'self', { value: win })
    Object.defineProperty(win, 'top', { value: win })
    expect(hidesMenuInFrame(win)).toBe(false)
  })

  it('returns true in a frame without the marker', () => {
    expect(hidesMenuInFrame(framed(''))).toBe(true)
    expect(hidesMenuInFrame(framed('opennms-0.123'))).toBe(true)
  })

  it('returns false in a frame whose name asks for the menu', () => {
    expect(hidesMenuInFrame(framed('noc-with-header'))).toBe(false)
  })
})
