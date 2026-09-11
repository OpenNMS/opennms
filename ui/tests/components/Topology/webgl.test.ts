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

import { describe, expect, it, vi, afterEach } from 'vitest'
import { hasWebGL } from '@/components/Topology/webgl'

const stubContext = (impl: (kind: string) => unknown) =>
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext')
    .mockImplementation((kind: string) => impl(kind) as never)

afterEach(() => {
  vi.restoreAllMocks()
})

describe('hasWebGL', () => {
  it('is true when the browser gives up a context', () => {
    stubContext(kind => (kind === 'webgl2' ? { getExtension: () => null } : null))
    expect(hasWebGL()).toBe(true)
  })

  it('accepts webgl where webgl2 is unavailable', () => {
    stubContext(kind => (kind === 'webgl' ? { getExtension: () => null } : null))
    expect(hasWebGL()).toBe(true)
  })

  it('is false when no context is available', () => {
    stubContext(() => null)
    expect(hasWebGL()).toBe(false)
  })

  // Some builds throw instead of returning null.
  it('is false when asking for a context throws', () => {
    stubContext(() => {
      throw new Error('blocked')
    })
    expect(hasWebGL()).toBe(false)
  })

  // Contexts are rationed, so the probe must not keep the one it took.
  it('hands its probe context back', () => {
    const loseContext = vi.fn()
    stubContext(kind => (kind === 'webgl2'
      ? { getExtension: (name: string) => (name === 'WEBGL_lose_context' ? { loseContext } : null) }
      : null))
    expect(hasWebGL()).toBe(true)
    expect(loseContext).toHaveBeenCalled()
  })
})
