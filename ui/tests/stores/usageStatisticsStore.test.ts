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
import { useUsageStatisticsStore } from '@/stores/usageStatisticsStore'

const { api, showSnackBarMock } = vi.hoisted(() => ({
  api: {
    getUsageStatistics: vi.fn(),
    getUsageStatisticsMetadata: vi.fn(),
    getUsageStatisticsStatus: vi.fn(),
    setUsageStatisticsStatus: vi.fn()
  },
  showSnackBarMock: vi.fn()
}))

vi.mock('@/services', () => ({ default: api }))
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

describe('usageStatisticsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('enableSharing returns true and refreshes the status', async () => {
    api.setUsageStatisticsStatus.mockResolvedValue({ status: 200 })
    api.getUsageStatisticsStatus.mockResolvedValue({ enabled: true })

    const store = useUsageStatisticsStore()
    const ok = await store.enableSharing()

    expect(ok).toBe(true)
    expect(api.setUsageStatisticsStatus).toHaveBeenCalledWith(true)
    expect(store.status.enabled).toBe(true)
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })

  it('disableSharing accepts a 202 and refreshes the status', async () => {
    api.setUsageStatisticsStatus.mockResolvedValue({ status: 202 })
    api.getUsageStatisticsStatus.mockResolvedValue({ enabled: false })

    const store = useUsageStatisticsStore()
    store.status = { enabled: true }
    const ok = await store.disableSharing()

    expect(ok).toBe(true)
    expect(api.setUsageStatisticsStatus).toHaveBeenCalledWith(false)
    expect(store.status.enabled).toBe(false)
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })

  it('returns false without refreshing or showing anything when the update fails', async () => {
    api.setUsageStatisticsStatus.mockResolvedValue(undefined)

    const store = useUsageStatisticsStore()
    const enabled = await store.enableSharing()
    const disabled = await store.disableSharing()

    expect(enabled).toBe(false)
    expect(disabled).toBe(false)
    expect(api.getUsageStatisticsStatus).not.toHaveBeenCalled()
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })
})
