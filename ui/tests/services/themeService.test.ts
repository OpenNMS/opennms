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

import { afterEach, describe, expect, it, vi } from 'vitest'
import { DARK_THEME, DEFAULT_THEME, loadTheme, saveTheme } from '@/services/themeService'

// Safari with 'Block all cookies' (and Firefox with cookies disabled) throws a
// SecurityError on ANY window.localStorage access. The menu app calls loadTheme
// (via initTheme) before app.mount(), so a propagated throw would prevent the
// menu from ever mounting (NMS-20174).
const securityError = () => {
  throw new DOMException('The operation is insecure.', 'SecurityError')
}

describe('themeService', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  describe('loadTheme', () => {
    it('returns the stored theme when localStorage works', () => {
      localStorage.setItem('theme', DARK_THEME)

      expect(loadTheme()).toBe(DARK_THEME)
    })

    it('returns the default theme for an unknown stored value', () => {
      localStorage.setItem('theme', 'neon-pink')

      expect(loadTheme()).toBe(DEFAULT_THEME)
    })

    it('returns the default theme when localStorage access throws', () => {
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation(securityError)

      expect(loadTheme()).toBe(DEFAULT_THEME)
    })
  })

  describe('saveTheme', () => {
    it('does not throw when localStorage access throws', () => {
      vi.spyOn(Storage.prototype, 'setItem').mockImplementation(securityError)

      expect(() => saveTheme(DARK_THEME)).not.toThrow()
    })
  })
})
