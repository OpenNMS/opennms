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

import { CHECK_TAG, formatSize, formatUploadedAt, shortSha, statusOf } from '@/components/PluginManagement/pluginDisplay'
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

  it('maps levels and statuses', () => {
    expect(CHECK_TAG).toEqual({ PASS: 'success', WARN: 'warn', FAIL: 'danger' })
    expect(statusOf('installed').severity).toBe('success')
    expect(statusOf('staged').severity).toBe('warn')
    expect(statusOf('failed')).toMatchObject({ severity: 'danger', label: 'failed', title: 'one or more features did not start; see karaf.log' })
    expect(statusOf('unloaded').severity).toBe('secondary')
    expect(statusOf('unmanaged').title).toContain('not loaded through this page')
    expect(statusOf('weird')).toMatchObject({ severity: 'secondary', label: 'weird' })
  })
})
