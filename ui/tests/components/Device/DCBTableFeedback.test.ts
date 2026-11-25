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

import { flushPromises, mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import dateFormatDirective from '@/directives/v-date'
import DCBTable from '@/components/Device/DCBTable.vue'
import { useDeviceStore } from '@/stores/deviceStore'

const { showSnackBarMock, startSpinner, stopSpinner } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  startSpinner: vi.fn(),
  stopSpinner: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('@/composables/useSpinner', () => ({
  default: () => ({ startSpinner, stopSpinner })
}))

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

const backup = {
  id: 1,
  deviceName: 'Cisco-7201',
  location: 'Default',
  ipAddress: '10.21.10.81',
  lastSucceededDate: 1700000000000,
  lastUpdatedDate: 1700000000000,
  backupStatus: 'success',
  nextScheduledBackupDate: 1700000000000,
  scheduledInterval: { deviceConfig: 'daily' },
  configType: 'running',
  configName: 'Running Configuration',
  ipInterfaceId: 1,
  lastBackupDate: 1700000000000,
  lastFailedDate: 1700000000000,
  nodeId: 1,
  nodeLabel: 'node1',
  serviceName: 'DeviceConfig-running'
}

const mountTable = async () => {
  const wrapper = mount(DCBTable, {
    global: {
      plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
      directives: { date: dateFormatDirective },
      stubs: ['router-link']
    }
  })
  await useDeviceStore().getDeviceConfigBackups()
  await flushPromises()
  await wrapper.get('[data-test="all-checkbox"] input').setValue(true)
  await flushPromises()
  return wrapper
}

describe('DCBTable feedback', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.getDeviceConfigBackups.mockResolvedValue({ data: [backup], headers: { 'content-range': '1' }})
  })

  it('shows a success toast and wraps the action in the spinner when backup is triggered', async () => {
    api.backupDeviceConfig.mockResolvedValue({ status: 202 })
    const wrapper = await mountTable()

    await wrapper.get('[data-test="backup-now-btn"]').trigger('click')
    await flushPromises()

    expect(startSpinner).toHaveBeenCalledTimes(1)
    expect(stopSpinner).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Device backup triggered.' })
  })

  it('shows an error toast when backup is not triggered', async () => {
    api.backupDeviceConfig.mockResolvedValue(false)
    const wrapper = await mountTable()

    await wrapper.get('[data-test="backup-now-btn"]').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Device backup not triggered.', error: true })
    expect(showSnackBarMock).not.toHaveBeenCalledWith({ msg: 'Device backup triggered.' })
  })

  it('wraps download-all, including the chunked requests, in one spinner run', async () => {
    api.downloadDeviceConfigs.mockResolvedValue(false)
    const wrapper = await mountTable()
    vi.clearAllMocks()

    await wrapper.get('[data-test="download-btn"]').trigger('click')
    await flushPromises()

    expect(startSpinner).toHaveBeenCalledTimes(1)
    expect(stopSpinner).toHaveBeenCalledTimes(1)
  })
})
