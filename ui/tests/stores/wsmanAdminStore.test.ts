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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import API from '@/services'

vi.mock('@/services', () => ({
  default: {
    getWsmanConfig: vi.fn(),
    updateWsmanConfig: vi.fn()
  }
}))

const CONFIG = { defaults: { username: 'root', hasPassword: true }, definitions: [] }

describe('wsmanAdminStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('stores the configuration on success', async () => {
    vi.mocked(API.getWsmanConfig).mockResolvedValue(CONFIG as any)
    const store = useWsmanAdminStore()
    await store.getConfig()
    expect(store.config).toEqual(CONFIG)
    expect(store.loadError).toBe(false)
    expect(store.isLoading).toBe(false)
  })

  it('re-reads the configuration after a successful save and passes a failure through', async () => {
    const store = useWsmanAdminStore()
    const input = { defaults: {}, definitions: [] } as any
    vi.mocked(API.updateWsmanConfig).mockResolvedValueOnce(null)
    vi.mocked(API.getWsmanConfig).mockResolvedValueOnce(CONFIG as any)
    expect(await store.saveConfig(input)).toBeNull()
    expect(API.getWsmanConfig).toHaveBeenCalledTimes(1)
    expect(store.config).toEqual(CONFIG)

    vi.mocked(API.updateWsmanConfig).mockResolvedValueOnce('nope')
    expect(await store.saveConfig(input)).toBe('nope')
    expect(API.getWsmanConfig).toHaveBeenCalledTimes(1)
  })

  it('flags a load error and keeps the previous configuration', async () => {
    const store = useWsmanAdminStore()
    vi.mocked(API.getWsmanConfig).mockResolvedValueOnce(CONFIG as any)
    await store.getConfig()
    vi.mocked(API.getWsmanConfig).mockResolvedValueOnce(null)
    await store.getConfig()
    expect(store.loadError).toBe(true)
    expect(store.config).toEqual(CONFIG)
  })
})
