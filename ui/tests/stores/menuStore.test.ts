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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMenuStore } from '@/stores/menuStore'
import API from '@/services'
import { MainMenu } from '@/types/mainMenu'

vi.mock('@/services', () => ({
  default: {
    getMainMenu: vi.fn()
  }
}))

describe('useMenuStore.getMainMenu', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('stores the menu and marks it loaded', async () => {
    vi.mocked(API.getMainMenu).mockResolvedValue({ baseHref: '/opennms/' } as MainMenu)
    const store = useMenuStore()
    expect(store.mainMenuLoaded).toBe(false)

    await store.getMainMenu()

    expect(store.mainMenu.baseHref).toBe('/opennms/')
    expect(store.mainMenuLoaded).toBe(true)
  })

  // route guards wait on mainMenuLoaded, so a failed fetch must still set it
  it('marks the menu loaded even when the fetch fails', async () => {
    vi.mocked(API.getMainMenu).mockResolvedValue(false)
    const store = useMenuStore()

    await store.getMainMenu()

    expect(store.mainMenuLoaded).toBe(true)
  })
})
