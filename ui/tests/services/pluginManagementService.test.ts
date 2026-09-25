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
import { checkPluginKar, DEFAULT_LOG_LINES, downloadPluginManagementLog, fetchPluginFromRepository, getPluginCatalog, getPluginManagement, getPluginManagementLog, getPluginReleases, getPluginRestartInstructions, installPlugin, LOG_LINE_OPTIONS, MAX_LOG_LINES, unloadPlugin } from '@/services/pluginManagementService'
import { rest, v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn(), post: vi.fn(), delete: vi.fn() },
  rest: { get: vi.fn() }
}))

const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: 'abc', size: 10, uploadedBy: 'admin', uploadedAt: 1, features: ['alec'], bootFile: 'alec.boot', autoStart: true, status: 'staged', pendingRestart: true, source: 'upload' }
const INSTRUCTIONS = { packages: 'systemctl restart opennms', container: 'docker restart horizon', healthCheck: 'opennms status', note: 'Wait for the health check.' }

describe('pluginManagementService', () => {
  beforeEach(() => vi.clearAllMocks())

  it('reads /plugin-management and returns null on failure or an unexpected body', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { containerAvailable: true, opennmsHome: '/opt/opennms', deployDir: '/opt/opennms/deploy', restartRequired: true, plugins: [PLUGIN] }})
    const state = await getPluginManagement()
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/plugin-management')
    expect(state).toEqual({ containerAvailable: true, opennmsHome: '/opt/opennms', deployDir: '/opt/opennms/deploy', restartRequired: true, plugins: [PLUGIN] })
    vi.mocked(v2.get).mockRejectedValueOnce(new Error('403'))
    expect(await getPluginManagement()).toBeNull()
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: '<html>' })
    expect(await getPluginManagement()).toBeNull()
  })

  it('carries the temporary download area usage when the server reports it', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { containerAvailable: true, plugins: [], tempDir: '/opt/opennms/data/tmp/plugins', tempBytes: 2048, tempFiles: 1 }})
    expect(await getPluginManagement()).toMatchObject({ tempDir: '/opt/opennms/data/tmp/plugins', tempBytes: 2048, tempFiles: 1 })
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { containerAvailable: true, plugins: [], tempBytes: null }})
    const state = await getPluginManagement()
    expect(state).not.toHaveProperty('tempDir')
    expect(state).not.toHaveProperty('tempBytes')
    expect(state).not.toHaveProperty('tempFiles')
  })

  it('reads the catalog, dropping malformed entries, and returns null on failure', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { entries: [
      { id: 'alec', name: 'ALEC', description: 'Correlation', repository: 'OpenNMS-Plugins/alec', docsUrl: 'https://docs' },
      { id: 'bare', repository: 'o/r', docsUrl: '' },
      { name: 'no id' }
    ], customAllowed: true }})
    expect(await getPluginCatalog()).toEqual({ entries: [
      { id: 'alec', name: 'ALEC', description: 'Correlation', repository: 'OpenNMS-Plugins/alec', docsUrl: 'https://docs' },
      { id: 'bare', name: 'bare', description: '', repository: 'o/r', docsUrl: null }
    ], customAllowed: true })
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/plugin-management/catalog')
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { entries: [] }})
    expect(await getPluginCatalog()).toEqual({ entries: [], customAllowed: false })
    vi.mocked(v2.get).mockRejectedValueOnce(new Error('500'))
    expect(await getPluginCatalog()).toBeNull()
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: 'nope' })
    expect(await getPluginCatalog()).toBeNull()
  })

  it('reads the releases of a catalog entry or of any repository, surfacing the 502 text', async () => {
    const release = { tag: 'v3.0.4', name: 'v3.0.4', publishedAt: '2026-09-01T10:00:00Z', prerelease: false, notes: 'n', assets: [{ name: 'a.kar', size: 5, url: 'https://x' }] }
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { repository: 'OpenNMS-Plugins/alec', releases: [release, { tag: 'v1' }, { nothing: true }], fetchedAt: 't', cached: true }})
    const result = await getPluginReleases({ catalogId: 'alec' })
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/plugin-management/catalog/alec/releases')
    expect(result.success).toBe(true)
    expect(result.payload).toEqual({ repository: 'OpenNMS-Plugins/alec', releases: [
      release,
      { tag: 'v1', name: 'v1', publishedAt: '', prerelease: false, notes: '', assets: [] }
    ], fetchedAt: 't', cached: true })

    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { releases: [] }})
    const custom = await getPluginReleases({ repository: 'acme/my plugin' })
    expect(vi.mocked(v2.get).mock.calls[1][0]).toBe('/plugin-management/releases?repository=acme%2Fmy%20plugin')
    expect(custom.payload).toEqual({ repository: 'acme/my plugin', releases: [], fetchedAt: '', cached: false })

    vi.mocked(v2.get).mockRejectedValueOnce({ response: { status: 502, data: 'GitHub rate limit reached; try again after 14:30.' }})
    expect(await getPluginReleases({ catalogId: 'alec' })).toMatchObject({ success: false, message: 'GitHub rate limit reached; try again after 14:30.' })
    vi.mocked(v2.get).mockRejectedValueOnce({ response: { status: 502, data: '<html>Bad Gateway</html>' }})
    expect((await getPluginReleases({ catalogId: 'alec' })).message).toBe('Failed to read the releases from GitHub.')
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { repository: 'x' }})
    expect((await getPluginReleases({ catalogId: 'alec' })).success).toBe(false)
  })

  it('posts the fetch request and returns the inspection with its source, or the server reason', async () => {
    const input = { catalogId: 'alec', tag: 'v3.0.4', assetName: 'a.kar' }
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: { karName: 'alec', size: 5, sha256: 'abc', uploadToken: 'tok', checks: [], source: { repository: 'OpenNMS-Plugins/alec', tag: 'v3.0.4', assetName: 'a.kar', url: 'https://x' }}})
    const result = await fetchPluginFromRepository(input)
    expect(vi.mocked(v2.post).mock.calls[0][0]).toBe('/plugin-management/fetch')
    expect(vi.mocked(v2.post).mock.calls[0][1]).toBe(input)
    expect(result.success).toBe(true)
    expect(result.payload).toEqual({ karName: 'alec', size: 5, sha256: 'abc', uploadToken: 'tok', manifest: {}, features: [], bundles: [], checks: [], source: { repository: 'OpenNMS-Plugins/alec', tag: 'v3.0.4', assetName: 'a.kar', url: 'https://x' }})
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: { uploadToken: 'tok', checks: [] }})
    const bare = await fetchPluginFromRepository({ repository: 'o/r', tag: 'v1', assetName: 'b.kar' })
    expect(bare.payload).toMatchObject({ karName: 'b.kar', size: 0 })
    expect(bare.payload).not.toHaveProperty('source')
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 502, data: 'Download failed: connection reset.' }})
    expect(await fetchPluginFromRepository(input)).toMatchObject({ success: false, message: 'Download failed: connection reset.' })
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 500, data: '<html>' }})
    expect((await fetchPluginFromRepository(input)).message).toBe('Failed to download a.kar.')
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: {}})
    expect((await fetchPluginFromRepository(input)).success).toBe(false)
  })

  it('posts the KAR as the multipart field "upload" to /check and returns the inspection', async () => {
    const file = new File(['kar'], 'alec.kar')
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: { karName: 'alec', size: 3, sha256: 'abc', uploadToken: 'tok', manifest: { 'Karaf-Feature-Start': 'true' }, features: [], bundles: [], checks: [{ id: 'structure', level: 'PASS', message: 'ok' }] }})
    const result = await checkPluginKar(file)
    expect(vi.mocked(v2.post).mock.calls[0][0]).toBe('/plugin-management/check')
    const body = vi.mocked(v2.post).mock.calls[0][1] as FormData
    expect(body).toBeInstanceOf(FormData)
    expect(body.get('upload')).toBe(file)
    expect(result.success).toBe(true)
    expect(result.payload).toMatchObject({ karName: 'alec', uploadToken: 'tok', checks: [{ id: 'structure', level: 'PASS' }] })
  })

  it('surfaces a short 4xx text body from /check and falls back otherwise', async () => {
    const file = new File(['kar'], 'alec.kar')
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 400, data: 'Not a KAR file: no repository/ entries.' }})
    expect(await checkPluginKar(file)).toMatchObject({ success: false, message: 'Not a KAR file: no repository/ entries.' })
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 500, data: 'Internal error' }})
    expect((await checkPluginKar(file)).message).toBe('Failed to check alec.kar.')
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 400, data: '<html>boom</html>' }})
    expect((await checkPluginKar(file)).message).toBe('Failed to check alec.kar.')
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: { nothing: true }})
    expect((await checkPluginKar(file)).success).toBe(false)
  })

  it('posts the install JSON and surfaces the 409 reason or the 503 fallback', async () => {
    const input = { uploadToken: 'tok', karName: 'alec', acknowledgeWarnings: true }
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: { plugin: PLUGIN, restartRequired: true, restartInstructions: INSTRUCTIONS }})
    const result = await installPlugin(input)
    expect(vi.mocked(v2.post).mock.calls[0][0]).toBe('/plugin-management/install')
    expect(vi.mocked(v2.post).mock.calls[0][1]).toBe(input)
    expect(result.success).toBe(true)
    expect(result.payload?.plugin.karName).toBe('alec')

    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 409, data: 'Check compatibility failed: package org.foo is not exported.' }})
    expect(await installPlugin(input)).toMatchObject({ success: false, message: 'Check compatibility failed: package org.foo is not exported.' })
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 503, data: '<html>unavailable</html>' }})
    expect((await installPlugin(input)).message).toContain('container is not available')
  })

  it('deletes by encoded KAR name and returns the entry with the touched boot files, or the reason', async () => {
    vi.mocked(v2.delete).mockResolvedValueOnce({ status: 200, data: { plugin: { ...PLUGIN, status: 'unloaded' }, restartRequired: true, bootFilesRemoved: ['etc/featuresBoot.d/alec.boot', 'etc/featuresBoot.d/shared.boot'], restartInstructions: INSTRUCTIONS }})
    const result = await unloadPlugin('my plugin')
    expect(vi.mocked(v2.delete).mock.calls[0][0]).toBe('/plugin-management/my%20plugin')
    expect(result.success).toBe(true)
    expect(result.payload?.plugin.status).toBe('unloaded')
    expect(result.payload?.restartRequired).toBe(true)
    expect(result.payload?.bootFilesRemoved).toEqual(['etc/featuresBoot.d/alec.boot', 'etc/featuresBoot.d/shared.boot'])
    expect(result.payload?.restartInstructions).toEqual(INSTRUCTIONS)
    vi.mocked(v2.delete).mockResolvedValueOnce({ status: 200, data: { ...PLUGIN, status: 'unloaded' }})
    expect((await unloadPlugin('alec')).success).toBe(false)
    vi.mocked(v2.delete).mockResolvedValueOnce({ status: 200, data: { plugin: PLUGIN }})
    expect((await unloadPlugin('alec')).payload?.bootFilesRemoved).toEqual([])
    vi.mocked(v2.delete).mockRejectedValueOnce({ response: { status: 404, data: 'No plugin named nope.' }})
    expect(await unloadPlugin('nope')).toMatchObject({ success: false, message: 'No plugin named nope.' })
  })

  it('reads the restart instructions and returns null on failure', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: INSTRUCTIONS })
    expect(await getPluginRestartInstructions()).toEqual(INSTRUCTIONS)
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/plugin-management/restart-instructions')
    vi.mocked(v2.get).mockRejectedValueOnce(new Error('500'))
    expect(await getPluginRestartInstructions()).toBeNull()
  })

  it('reads the audit log newest-first with the requested line count, and returns null when it cannot be read', async () => {
    expect(DEFAULT_LOG_LINES).toBe(1000)
    expect(MAX_LOG_LINES).toBe(10000)
    expect(LOG_LINE_OPTIONS).toEqual([200, 1000, 5000, 10000])
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: 'line 1\nline 0' })
    expect(await getPluginManagementLog()).toBe('line 1\nline 0')
    expect(vi.mocked(rest.get).mock.calls[0][0]).toBe('/logs/contents?f=plugin-management.log&reverse=true&n=1000')
    expect(vi.mocked(rest.get).mock.calls[0][1]).toEqual({ headers: { Accept: 'text/plain' }})
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: 'x' })
    await getPluginManagementLog(5000)
    expect(vi.mocked(rest.get).mock.calls[1][0]).toBe('/logs/contents?f=plugin-management.log&reverse=true&n=5000')
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: 'x' })
    await getPluginManagementLog(99999)
    expect(vi.mocked(rest.get).mock.calls[2][0]).toContain('&n=10000')
    vi.mocked(rest.get).mockRejectedValueOnce(new Error('404'))
    expect(await getPluginManagementLog()).toBeNull()
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: '' })
    expect(await getPluginManagementLog()).toBe('')
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: { not: 'text' }})
    expect(await getPluginManagementLog()).toBe('')
  })

  it('downloads the whole log in file order and returns null on failure', async () => {
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: 'line 0\nline 1' })
    expect(await downloadPluginManagementLog()).toBe('line 0\nline 1')
    expect(vi.mocked(rest.get).mock.calls[0][0]).toBe('/logs/contents?f=plugin-management.log&reverse=false&n=10000')
    vi.mocked(rest.get).mockRejectedValueOnce(new Error('500'))
    expect(await downloadPluginManagementLog()).toBeNull()
  })
})
