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
import { checkPluginKar, getPluginManagement, getPluginManagementLog, getPluginRestartInstructions, installPlugin, LOG_LINES, unloadPlugin } from '@/services/pluginManagementService'
import { rest, v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn(), post: vi.fn(), delete: vi.fn() },
  rest: { get: vi.fn() }
}))

const PLUGIN = { karName: 'alec', fileName: 'alec.kar', sha256: 'abc', size: 10, uploadedBy: 'admin', uploadedAt: 1, features: ['alec'], bootFile: 'alec.boot', autoStart: true, status: 'staged', pendingRestart: true }
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

  it('reads the audit log newest-first, trimmed, and returns null when it cannot be read', async () => {
    const lines = Array.from({ length: LOG_LINES + 5 }, (_v, i) => `line ${i}`)
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: lines.join('\n') })
    const log = await getPluginManagementLog()
    expect(vi.mocked(rest.get).mock.calls[0][0]).toBe('/logs/contents?f=plugin-management.log&reverse=true')
    expect(log?.split('\n')).toHaveLength(LOG_LINES)
    expect(log?.startsWith('line 0')).toBe(true)
    vi.mocked(rest.get).mockRejectedValueOnce(new Error('404'))
    expect(await getPluginManagementLog()).toBeNull()
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: '' })
    expect(await getPluginManagementLog()).toBe('')
  })
})
