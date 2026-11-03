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
import { loadDefaultPreferences, loadPreferences, savePreferences } from '@/services/localStorageService'

// Safari with 'Block all cookies' (and Firefox with cookies disabled) throws a
// SecurityError on ANY window.localStorage access. The services must degrade to
// defaults instead of letting the throw propagate into component setup, where
// it would abort mounting the menu app (NMS-20174).
const securityError = () => {
  throw new DOMException('The operation is insecure.', 'SecurityError')
}

describe('localStorageService', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  describe('loadPreferences', () => {
    it('returns stored preferences when localStorage works', () => {
      const prefs = loadDefaultPreferences()
      prefs.isSideMenuExpanded = true
      localStorage.setItem('opennms-preferences', JSON.stringify(prefs))

      expect(loadPreferences()?.isSideMenuExpanded).toBe(true)
    })

    it('returns null when localStorage access throws', () => {
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation(securityError)

      expect(loadPreferences()).toBeNull()
    })

    it('returns null when the stored value is corrupted JSON', () => {
      localStorage.setItem('opennms-preferences', '{not valid json')

      expect(loadPreferences()).toBeNull()
    })
  })

  describe('savePreferences', () => {
    it('does not throw when localStorage access throws', () => {
      vi.spyOn(Storage.prototype, 'setItem').mockImplementation(securityError)

      expect(() => savePreferences(loadDefaultPreferences())).not.toThrow()
    })
  })
})
