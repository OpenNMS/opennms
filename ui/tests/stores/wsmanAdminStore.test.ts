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
    getWsmanDataCollection: vi.fn(),
    getWsmanStatus: vi.fn(),
    syncWsmanDefinition: vi.fn(),
    updateWsmanConfig: vi.fn(),
    updateWsmanDataCollectionFile: vi.fn()
  }
}))

const CONFIG = { version: 'v1', defaults: { username: 'root', hasPassword: true }, definitions: [] }

describe('wsmanAdminStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('stores the configuration and the poller status together on success', async () => {
    vi.mocked(API.getWsmanConfig).mockResolvedValue(CONFIG as any)
    const status = { serviceName: 'WS-Man', servers: 0, definitions: [], defaults: { servers: 0, responding: 0, down: 0, lastResponse: null }}
    vi.mocked(API.getWsmanStatus).mockResolvedValue(status)
    const store = useWsmanAdminStore()
    await store.getConfig()
    expect(store.config).toEqual(CONFIG)
    expect(store.status).toEqual(status)
    expect(store.loadError).toBe(false)
    expect(store.isLoading).toBe(false)
  })

  it('re-reads the configuration after every save and passes a failure through', async () => {
    const store = useWsmanAdminStore()
    const input = { defaults: {}, definitions: [] } as any
    vi.mocked(API.updateWsmanConfig).mockResolvedValueOnce(null)
    vi.mocked(API.getWsmanConfig).mockResolvedValueOnce(CONFIG as any)
    expect(await store.saveConfig(input)).toBeNull()
    expect(API.getWsmanConfig).toHaveBeenCalledTimes(1)
    expect(store.config).toEqual(CONFIG)

    // a failure (e.g. a version conflict) also re-reads, so the next attempt starts from the current file
    vi.mocked(API.updateWsmanConfig).mockResolvedValueOnce('nope')
    vi.mocked(API.getWsmanConfig).mockResolvedValueOnce({ ...CONFIG, version: 'v2' } as any)
    expect(await store.saveConfig(input)).toBe('nope')
    expect(API.getWsmanConfig).toHaveBeenCalledTimes(2)
    expect(store.config?.version).toBe('v2')
  })

  it('loads the data collection view and flags its failure separately', async () => {
    const store = useWsmanAdminStore()
    const dc = { rrdRepository: null, sources: ['wsman-datacollection-config.xml'], versions: {}, collections: [], groups: [], systemDefinitions: [] }
    vi.mocked(API.getWsmanDataCollection).mockResolvedValueOnce(dc)
    await store.getDataCollection()
    expect(store.dataCollection).toEqual(dc)
    expect(store.dataCollectionError).toBe(false)
    vi.mocked(API.getWsmanDataCollection).mockResolvedValueOnce(null)
    await store.getDataCollection()
    expect(store.dataCollectionError).toBe(true)
    expect(store.dataCollection).toEqual(dc)
  })

  it('re-reads the data collection after saving a file, success or not', async () => {
    const store = useWsmanAdminStore()
    const dc = { rrdRepository: null, sources: ['custom.xml'], versions: { 'custom.xml': 'v2' }, collections: [], groups: [], systemDefinitions: [] }
    vi.mocked(API.updateWsmanDataCollectionFile).mockResolvedValueOnce(null)
    vi.mocked(API.getWsmanDataCollection).mockResolvedValueOnce(dc)
    expect(await store.saveDataCollectionFile('custom.xml', { version: null, rrdRepository: null, collections: [], groups: [], systemDefinitions: [] })).toBeNull()
    expect(store.dataCollection?.versions['custom.xml']).toBe('v2')
    vi.mocked(API.updateWsmanDataCollectionFile).mockResolvedValueOnce('nope')
    vi.mocked(API.getWsmanDataCollection).mockResolvedValueOnce(dc)
    expect(await store.saveDataCollectionFile('custom.xml', { version: 'v2', rrdRepository: null, collections: [], groups: [], systemDefinitions: [] })).toBe('nope')
    expect(API.getWsmanDataCollection).toHaveBeenCalledTimes(2)
  })

  it('re-reads the status after a successful sync and passes a failure through', async () => {
    const store = useWsmanAdminStore()
    const result = { requisition: 'windows', addedNodes: [], existingNodes: 1, addedRanges: [], existingRanges: 0, skippedPatterns: [], importRequested: false, discoveryReloadRequested: false }
    const status = { serviceName: 'WS-Man', servers: 1, definitions: [], defaults: { servers: 0, responding: 0, down: 0, lastResponse: null }}
    vi.mocked(API.syncWsmanDefinition).mockResolvedValueOnce(result)
    vi.mocked(API.getWsmanStatus).mockResolvedValueOnce(status)
    expect(await store.syncDefinition(0)).toEqual(result)
    expect(store.status).toEqual(status)
    vi.mocked(API.syncWsmanDefinition).mockResolvedValueOnce('nope')
    expect(await store.syncDefinition(0)).toBe('nope')
    expect(API.getWsmanStatus).toHaveBeenCalledTimes(1)
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
