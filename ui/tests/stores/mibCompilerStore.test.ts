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

import { createPinia, setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'

const mockListMibFiles = vi.fn()
vi.mock('@/services/mibCompilerService', () => ({
  listMibFiles: (...args: unknown[]) => mockListMibFiles(...args)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar })
}))

describe('mibCompilerStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('fetchMibFiles populates pending and compiled lists', async () => {
    const pending = [{ name: 'A-MIB.txt', size: 10, lastModified: 1 }]
    const compiled = [{ name: 'B-MIB.mib', size: 20, lastModified: 2 }]
    mockListMibFiles.mockResolvedValue({ pending, compiled })

    const store = useMibCompilerStore()
    const result = await store.fetchMibFiles()

    expect(result.success).toBe(true)
    expect(store.pendingFiles).toEqual(pending)
    expect(store.compiledFiles).toEqual(compiled)
    expect(store.isLoading).toBe(false)
    expect(mockShowSnackBar).not.toHaveBeenCalled()
  })

  it('fetchMibFiles reports the error and resets loading', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    mockListMibFiles.mockRejectedValue(new Error('boom'))

    const store = useMibCompilerStore()
    const result = await store.fetchMibFiles()

    expect(result).toEqual({ success: false, message: 'Failed to load the MIB files. Try again.' })
    expect(store.pendingFiles).toEqual([])
    expect(store.compiledFiles).toEqual([])
    expect(store.isLoading).toBe(false)
    // the store returns the message; the caller shows it
    expect(mockShowSnackBar).not.toHaveBeenCalled()
    expect(consoleError).toHaveBeenCalled()
  })
})
