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
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import API from '@/services'

vi.mock('@/services', () => ({
  default: {
    getPluginManagement: vi.fn(),
    checkPluginKar: vi.fn(),
    getPluginCatalog: vi.fn(),
    getPluginReleases: vi.fn(),
    fetchPluginFromRepository: vi.fn(),
    installPlugin: vi.fn(),
    unloadPlugin: vi.fn(),
    getPluginRestartInstructions: vi.fn(),
    getPluginManagementLog: vi.fn(),
    downloadPluginManagementLog: vi.fn()
  }
}))

const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: 'abc', size: 10, uploadedBy: 'admin', uploadedAt: 1, features: ['alec'], bootFile: 'alec.boot', autoStart: true, status: 'staged' as const, pendingRestart: true, source: 'upload', managed: true }
const STATE = { containerAvailable: true, opennmsHome: '/opt/opennms', deployDir: '/opt/opennms/deploy', restartRequired: false, plugins: [PLUGIN] }
const INSTRUCTIONS = { packages: 'p', container: 'c', healthCheck: 'h', note: 'n' }

describe('pluginManagementStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('loads the state and exposes the list, container and restart flags', async () => {
    vi.mocked(API.getPluginManagement).mockResolvedValueOnce(STATE)
    const store = usePluginManagementStore()
    expect(store.containerAvailable).toBe(true)
    expect(store.plugins).toEqual([])
    await store.load()
    expect(store.state).toEqual(STATE)
    expect(store.plugins).toEqual([PLUGIN])
    expect(store.restartRequired).toBe(false)
    expect(store.loadError).toBe(false)
    expect(store.isLoading).toBe(false)
  })

  it('flags a load error and keeps the previous state', async () => {
    const store = usePluginManagementStore()
    vi.mocked(API.getPluginManagement).mockResolvedValueOnce(STATE)
    await store.load()
    vi.mocked(API.getPluginManagement).mockResolvedValueOnce(null)
    await store.load()
    expect(store.loadError).toBe(true)
    expect(store.state).toEqual(STATE)
  })

  it('re-reads the list and the log after a successful install or unload only', async () => {
    const store = usePluginManagementStore()
    vi.mocked(API.getPluginManagement).mockResolvedValue({ ...STATE, restartRequired: true })
    vi.mocked(API.getPluginManagementLog).mockResolvedValue('entry')
    vi.mocked(API.installPlugin).mockResolvedValueOnce({ success: true, message: '', payload: { plugin: PLUGIN, restartRequired: true, restartInstructions: INSTRUCTIONS }})
    const install = await store.install({ uploadToken: 't', acknowledgeWarnings: false, features: ['alec'] })
    expect(install.success).toBe(true)
    expect(API.getPluginManagement).toHaveBeenCalledTimes(1)
    expect(store.restartRequired).toBe(true)
    expect(store.log).toBe('entry')
    expect(API.getPluginManagementLog).toHaveBeenCalledWith(1000)

    vi.mocked(API.installPlugin).mockResolvedValueOnce({ success: false, message: 'nope' })
    expect(await store.install({ uploadToken: 't', acknowledgeWarnings: false, features: ['alec'] })).toMatchObject({ success: false, message: 'nope' })
    expect(API.getPluginManagement).toHaveBeenCalledTimes(1)

    vi.mocked(API.unloadPlugin).mockResolvedValueOnce({ success: true, message: '', payload: { plugin: { ...PLUGIN, status: 'unloaded' }, restartRequired: true, bootFilesRemoved: [], restartInstructions: INSTRUCTIONS }})
    expect((await store.unload('alec')).payload?.plugin.status).toBe('unloaded')
    expect(API.unloadPlugin).toHaveBeenCalledWith('alec')
    expect(API.getPluginManagement).toHaveBeenCalledTimes(2)
    vi.mocked(API.unloadPlugin).mockResolvedValueOnce({ success: false, message: 'gone' })
    expect((await store.unload('alec')).message).toBe('gone')
    expect(API.getPluginManagement).toHaveBeenCalledTimes(2)
  })

  it('passes a check through and caches the restart instructions', async () => {
    const store = usePluginManagementStore()
    const file = new File(['x'], 'x.kar')
    vi.mocked(API.checkPluginKar).mockResolvedValueOnce({ success: false, message: 'bad' })
    expect(await store.check(file)).toMatchObject({ success: false, message: 'bad' })
    expect(API.checkPluginKar).toHaveBeenCalledWith(file)

    vi.mocked(API.getPluginRestartInstructions).mockResolvedValue(INSTRUCTIONS)
    expect(await store.getRestartInstructions()).toEqual(INSTRUCTIONS)
    expect(await store.getRestartInstructions()).toEqual(INSTRUCTIONS)
    expect(API.getPluginRestartInstructions).toHaveBeenCalledTimes(1)
    expect(store.restartInstructions).toEqual(INSTRUCTIONS)
  })

  it('records a failed log read as null', async () => {
    const store = usePluginManagementStore()
    expect(store.log).toBeUndefined()
    vi.mocked(API.getPluginManagementLog).mockResolvedValueOnce(null)
    await store.refreshLog()
    expect(store.log).toBeNull()
  })

  it('re-reads the log with the new line count and hands the full file to the caller', async () => {
    const store = usePluginManagementStore()
    expect(store.logLines).toBe(1000)
    vi.mocked(API.getPluginManagementLog).mockResolvedValueOnce('more')
    await store.setLogLines(5000)
    expect(store.logLines).toBe(5000)
    expect(API.getPluginManagementLog).toHaveBeenCalledWith(5000)
    expect(store.log).toBe('more')
    vi.mocked(API.downloadPluginManagementLog).mockResolvedValueOnce('whole file')
    expect(await store.downloadLog()).toBe('whole file')
    expect(store.log).toBe('more')
  })

  it('reads the catalog, recording a failure as null', async () => {
    const store = usePluginManagementStore()
    expect(store.catalog).toBeUndefined()
    const catalog = { entries: [{ id: 'alec', name: 'ALEC', description: '', repository: 'OpenNMS-Plugins/alec', docsUrl: null }], customAllowed: false }
    vi.mocked(API.getPluginCatalog).mockResolvedValueOnce(catalog)
    expect(await store.loadCatalog()).toEqual(catalog)
    expect(store.catalog).toEqual(catalog)
    vi.mocked(API.getPluginCatalog).mockResolvedValueOnce(null)
    expect(await store.loadCatalog()).toBeNull()
    expect(store.catalog).toBeNull()
  })

  it('keeps the releases of the last successful lookup only', async () => {
    const store = usePluginManagementStore()
    const releases = { repository: 'OpenNMS-Plugins/alec', releases: [], fetchedAt: '', cached: false }
    vi.mocked(API.getPluginReleases).mockResolvedValueOnce({ success: true, message: '', payload: releases })
    expect((await store.loadReleases({ catalogId: 'alec' })).success).toBe(true)
    expect(API.getPluginReleases).toHaveBeenCalledWith({ catalogId: 'alec' })
    expect(store.releases).toEqual(releases)
    vi.mocked(API.getPluginReleases).mockResolvedValueOnce({ success: false, message: 'rate limit' })
    expect((await store.loadReleases({ repository: 'o/r' })).message).toBe('rate limit')
    expect(store.releases).toBeNull()
  })

  it('passes a repository fetch through without re-reading the list', async () => {
    const store = usePluginManagementStore()
    const input = { catalogId: 'alec', tag: 'v3.0.4', assetName: 'a.kar' }
    vi.mocked(API.fetchPluginFromRepository).mockResolvedValueOnce({ success: false, message: 'refused' })
    expect(await store.fetchFromRepository(input)).toMatchObject({ success: false, message: 'refused' })
    expect(API.fetchPluginFromRepository).toHaveBeenCalledWith(input)
    expect(API.getPluginManagement).not.toHaveBeenCalled()
  })
})
