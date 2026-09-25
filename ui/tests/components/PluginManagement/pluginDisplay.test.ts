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

import { CHECK_TAG, defaultRelease, fetchedSourceLine, formatReleaseDate, formatSize, formatUploadedAt, isRepository, karAssets, releaseLabel, restartCounts, restartSummary, shortSha, sortReleases, sourceOf, statusOf, uploadedSourceLine } from '@/components/PluginManagement/pluginDisplay'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

describe('pluginDisplay', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('formats sizes, checksums and dates', () => {
    expect(formatSize(0)).toBe('0 B')
    expect(formatSize(1023)).toBe('1023 B')
    expect(formatSize(1536)).toBe('1.5 KB')
    expect(formatSize(5 * 1024 * 1024)).toBe('5.0 MB')
    expect(formatSize(250 * 1024 * 1024)).toBe('250 MB')
    expect(formatSize(null)).toBe('—')
    expect(shortSha('0123456789abcdef')).toBe('0123456789ab')
    expect(shortSha('')).toBe('—')
    expect(formatUploadedAt(null)).toBe('—')
    expect(formatUploadedAt(Date.UTC(2026, 8, 25, 12, 0, 0))).toContain('2026-09-25')
    expect(formatUploadedAt(String(Date.UTC(2026, 8, 25, 12, 0, 0)))).toContain('2026-09-25')
    expect(formatUploadedAt('2026-09-25T12:00:00Z')).toContain('2026-09-25')
    expect(formatUploadedAt('not a date')).toBe('—')
  })

  it('maps levels and statuses, telling a pending unload from a finished one', () => {
    expect(CHECK_TAG).toEqual({ PASS: 'success', WARN: 'warn', FAIL: 'danger' })
    expect(statusOf('installed')).toMatchObject({ severity: 'success', label: 'Loaded' })
    expect(statusOf('staged')).toMatchObject({ severity: 'warn', label: 'Load pending restart' })
    expect(statusOf('failed')).toMatchObject({ severity: 'danger', label: 'Failed to start', title: 'one or more features did not start; see karaf.log' })
    expect(statusOf('unloaded', true)).toMatchObject({ severity: 'warn', label: 'Unload pending restart' })
    expect(statusOf('unloaded', false)).toMatchObject({ severity: 'secondary', label: 'Unloaded' })
    expect(statusOf('unloaded')).toMatchObject({ severity: 'secondary', label: 'Unloaded' })
    expect(statusOf('unmanaged')).toMatchObject({ severity: 'info', label: 'Not managed here' })
    expect(statusOf('unmanaged').title).toContain('not loaded through this page')
    expect(statusOf('unknown')).toMatchObject({ severity: 'secondary', label: 'Unknown' })
    expect(statusOf('weird')).toMatchObject({ severity: 'secondary', label: 'weird' })
  })

  it('counts the plugins waiting for a restart', () => {
    const entry = (status: string, pendingRestart: boolean) => ({ karName: status, status, pendingRestart } as any)
    const plugins = [
      entry('staged', true), entry('staged', false), entry('installed', false), entry('installed', true),
      entry('unloaded', true), entry('unloaded', false), entry('failed', false), entry('unmanaged', false)
    ]
    expect(restartCounts(plugins)).toEqual({ toLoad: 3, toUnload: 1 })
    expect(restartSummary(plugins)).toBe('A restart is required to finish loading or unloading plugins: 3 to load, 1 to unload.')
    expect(restartSummary([])).toBe('A restart is required to finish loading or unloading plugins: 0 to load, 0 to unload.')
  })

  it('validates owner/repository names', () => {
    expect(isRepository('OpenNMS-Plugins/alec')).toBe(true)
    expect(isRepository('a.b_c-d/e.f_g-h')).toBe(true)
    expect(isRepository('alec')).toBe(false)
    expect(isRepository('a/b/c')).toBe(false)
    expect(isRepository('https://github.com/a/b')).toBe(false)
    expect(isRepository('a b/c')).toBe(false)
    expect(isRepository('../etc')).toBe(false)
    expect(isRepository('a/..')).toBe(false)
    expect(isRepository('a..b/c')).toBe(false)
    expect(isRepository('')).toBe(false)
    expect(isRepository(null)).toBe(false)
  })

  it('orders releases newest first with pre-releases last and picks the default', () => {
    const release = (tag: string, publishedAt: string, prerelease = false, assets: string[] = ['a.kar']) =>
      ({ tag, name: tag, publishedAt, prerelease, notes: '', assets: assets.map(name => ({ name, size: 1, url: '' })) })
    const rc = release('v2-rc1', '2026-09-20T12:00:00Z', true)
    const old = release('v1', '2026-01-01T12:00:00Z')
    const latest = release('v1.1', '2026-08-01T12:00:00Z')
    const undated = release('v0', '')
    expect(sortReleases([rc, old, latest, undated]).map(r => r.tag)).toEqual(['v1.1', 'v1', 'v0', 'v2-rc1'])
    expect(defaultRelease([rc, old, latest])?.tag).toBe('v1.1')
    expect(defaultRelease([rc])?.tag).toBe('v2-rc1')
    expect(defaultRelease([])).toBeNull()
    expect(releaseLabel(rc)).toBe('v2-rc1 · 2026-09-20 · pre-release')
    expect(releaseLabel(latest)).toBe('v1.1 · 2026-08-01')
    expect(releaseLabel(undated)).toBe('v0 · —')
    expect(formatReleaseDate('garbage')).toBe('—')
    expect(karAssets(release('v3', '', false, ['A.KAR', 'notes.md', 'b.kar'])).map(a => a.name)).toEqual(['A.KAR', 'b.kar'])
    expect(karAssets(null)).toEqual([])
  })

  it('describes where a KAR came from', () => {
    expect(sourceOf('github:OpenNMS-Plugins/alec@v3.0.4')).toEqual({ label: 'OpenNMS-Plugins/alec@v3.0.4', title: 'github:OpenNMS-Plugins/alec@v3.0.4' })
    expect(sourceOf('upload')).toEqual({ label: 'Uploaded file', title: 'upload' })
    expect(sourceOf(undefined)).toEqual({ label: 'Uploaded file', title: '' })
    expect(sourceOf('mirror:x')).toEqual({ label: 'mirror:x', title: 'mirror:x' })
    expect(fetchedSourceLine({ repository: 'OpenNMS-Plugins/alec', tag: 'v3.0.4', assetName: 'opennms-alec-plugin.kar', url: '' }, 93634560))
      .toBe('Fetched from OpenNMS-Plugins/alec v3.0.4 (opennms-alec-plugin.kar, 89.3 MB)')
    expect(uploadedSourceLine('alec.kar', 2048)).toBe('Uploaded alec.kar (2.0 KB)')
  })
})
