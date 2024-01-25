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

import { mount, RouterLinkStub } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import dateFormatDirective from '@/directives/v-date'
import DCB from '@/containers/DeviceConfigBackup.vue'
import { useDeviceStore } from '@/stores/deviceStore'
import { beforeAll, describe, expect, test } from 'vitest'
import { DeviceConfigBackup } from '@/types/deviceConfig'

const mockDeviceConfigBackups: DeviceConfigBackup[] = [
  {
    id: 123,
    deviceName: 'Cisco-7201',
    location: 'location',
    ipAddress: '10.21.10.81',
    lastSucceededDate: 1643831118973,
    lastUpdatedDate: 1643831118973,
    backupStatus: 'success',
    nextScheduledBackupDate: 1643831118973,
    scheduledInterval: { deviceConfig: 'daily' },
    config: 'mock cisco config',
    configType: 'running',
    configName: 'Running Configuration',
    ipInterfaceId: 1,
    lastBackupDate: 1643831118973,
    lastFailedDate: 1643831118973,
    fileName: 'filename',
    failureReason: 'reason',
    encoding: '',
    nodeId: 1,
    nodeLabel: 'node1',
    operatingSystem: '',
    isSuccessfulBackup: true,
    monitoredServiceId: 1,
    serviceName: 'DeviceConfig-running'
  },
  {
    id: 12,
    deviceName: 'Aruba-7003-1',
    location: 'location',
    ipAddress: '10.21.10.81',
    lastSucceededDate: 1643831118973,
    lastUpdatedDate: 1643831118973,
    backupStatus: 'failed',
    nextScheduledBackupDate: 1643831118973,
    scheduledInterval: { deviceConfig: 'daily' },
    config: 'mock aruba config',
    configType: 'default',
    configName: 'Startup Configuration',
    ipInterfaceId: 1,
    lastBackupDate: 1643831118973,
    lastFailedDate: 1643831118973,
    fileName: 'filename',
    failureReason: 'reason',
    encoding: '',
    nodeId: 1,
    nodeLabel: 'node1',
    operatingSystem: '',
    isSuccessfulBackup: true,
    monitoredServiceId: 1,
    serviceName: ''
  }
]

const wrapper = mount(DCB, {
  global: {
    plugins: [createTestingPinia()],
    directives: {
      date: dateFormatDirective
    },
    stubs: {
      RouterLink: RouterLinkStub
    }
  }
})

describe('deviceConfigBackupStore test', () => {
  beforeAll(() => {
    const deviceStore = useDeviceStore()
    deviceStore.deviceConfigBackups = mockDeviceConfigBackups
  })

  test('action btns enable and disable correctly', async () => {
    const viewHistoryBtn = wrapper.get('[data-test="view-history-btn"]')
    const downloadBtn = wrapper.get('[data-test="download-btn"]')
    const backupNowBtn = wrapper.get('[data-test="backup-now-btn"]')
    const checkboxes = wrapper.findAll('.dcb-config-checkbox')
    const firstDeviceConfig = wrapper.find('.dcb-config-checkbox > .feather-checkbox')
    const checkboxArray = wrapper.findAll('.dcb-config-checkbox > .feather-checkbox')
    const allCheckbox = wrapper.find('[data-test="all-checkbox"] > .feather-checkbox')

    // two DCB mock records
    expect(checkboxes.length).toBe(2)

    // all actions init disabled
    expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
    expect(downloadBtn.attributes('aria-disabled')).toBe('true')
    expect(backupNowBtn.attributes('aria-disabled')).toBe('true')
    expect(allCheckbox.attributes('aria-checked')).toBe('false')

    // select first config
    await firstDeviceConfig.trigger('click')
    // all btns should be enabled
    expect(viewHistoryBtn.attributes('aria-disabled')).toBeUndefined()
    expect(downloadBtn.attributes('aria-disabled')).toBeUndefined()
    expect(backupNowBtn.attributes('aria-disabled')).toBeUndefined()

    // select 'all devices' checkbox
    await allCheckbox.trigger('click')
    // the view history and backup btns should be disabled. Dwnld btn enabled
    expect(allCheckbox.attributes('aria-checked')).toBe('true')
    expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
    expect(downloadBtn.attributes('aria-disabled')).toBeUndefined()
    expect(backupNowBtn.attributes('aria-disabled')).toBeUndefined()

    // change 'all devices' to false
    await allCheckbox.trigger('click')
    // all actions back to disabled
    expect(viewHistoryBtn.attributes('aria-disabled')).toBe('true')
    expect(downloadBtn.attributes('aria-disabled')).toBe('true')
    expect(backupNowBtn.attributes('aria-disabled')).toBe('true')
    expect(allCheckbox.attributes('aria-checked')).toBe('false')

    // select second config checkbox
    await checkboxArray[1]?.trigger('click')
    // expect backup btn to be disabled because the only one selected has no service name
    expect(backupNowBtn.attributes('aria-disabled')).toBe('true')
    // select first checkbox again
    await firstDeviceConfig.trigger('click')
    // expect backup btn enabled because at least one selected has a service name
    expect(backupNowBtn.attributes('aria-disabled')).toBeUndefined()
  })
})
