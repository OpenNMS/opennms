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
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useDeviceStore } from '@/stores/deviceStore'

const { api } = vi.hoisted(() => ({
  api: {
    getDeviceConfigBackups: vi.fn(),
    backupDeviceConfig: vi.fn(),
    downloadDeviceConfigs: vi.fn(),
    getHistoryByIpInterface: vi.fn(),
    getVendorOptions: vi.fn(),
    getOsImageOptions: vi.fn()
  }
}))

vi.mock('@/services', () => ({ default: api }))

describe('deviceStore', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('backupSelectedDevices resolves true for a 202 response', async () => {
    api.backupDeviceConfig.mockResolvedValue({ status: 202 })
    expect(await useDeviceStore().backupSelectedDevices()).toBe(true)
  })

  it('backupSelectedDevices resolves true for a 200 response', async () => {
    api.backupDeviceConfig.mockResolvedValue({ status: 200 })
    expect(await useDeviceStore().backupSelectedDevices()).toBe(true)
  })

  it('backupSelectedDevices resolves false when the request failed', async () => {
    api.backupDeviceConfig.mockResolvedValue(false)
    expect(await useDeviceStore().backupSelectedDevices()).toBe(false)
  })

  it('backupSelectedDevices resolves false for an unexpected status', async () => {
    api.backupDeviceConfig.mockResolvedValue({ status: 500 })
    expect(await useDeviceStore().backupSelectedDevices()).toBe(false)
  })
})
