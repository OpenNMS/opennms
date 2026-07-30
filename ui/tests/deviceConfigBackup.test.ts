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
import PrimeVue from 'primevue/config'
import Tooltip from 'primevue/tooltip'
import { nextTick } from 'vue'
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
    plugins: [createTestingPinia(), PrimeVue],
    directives: {
      date: dateFormatDirective,
      tooltip: Tooltip
    },
    stubs: {
      RouterLink: RouterLinkStub
    }
  }
})

// PrimeVue Button forwards attrs to its root <button>; a disabled Button renders
// the native `disabled` attribute (there is no more FeatherDS `aria-disabled`).
const isBtnDisabled = (dataTest: string) =>
  (wrapper.get(`[data-test="${dataTest}"]`).element as HTMLButtonElement).disabled

describe('deviceConfigBackup test', () => {
  beforeAll(() => {
    const deviceStore = useDeviceStore()
    deviceStore.deviceConfigBackups = mockDeviceConfigBackups
    deviceStore.deviceConfigTotal = mockDeviceConfigBackups.length
  })

  test('action btns enable and disable correctly', async () => {
    await nextTick()

    // one PrimeVue checkbox per mock record (binary checkboxes render an <input>)
    const rowCheckboxes = wrapper.findAll('.dcb-config-checkbox input')
    const firstConfigCheckbox = rowCheckboxes[0]
    const secondConfigCheckbox = rowCheckboxes[1]
    const allCheckbox = wrapper.get('[data-test="all-checkbox"] input')

    // two DCB mock records
    expect(rowCheckboxes.length).toBe(2)

    // all actions init disabled
    expect(isBtnDisabled('view-history-btn')).toBe(true)
    expect(isBtnDisabled('download-btn')).toBe(true)
    expect(isBtnDisabled('backup-now-btn')).toBe(true)
    expect((allCheckbox.element as HTMLInputElement).checked).toBe(false)

    // select first config
    await firstConfigCheckbox.setValue(true)
    await nextTick()
    // all btns should be enabled
    expect(isBtnDisabled('view-history-btn')).toBe(false)
    expect(isBtnDisabled('download-btn')).toBe(false)
    expect(isBtnDisabled('backup-now-btn')).toBe(false)

    // deselect first config
    await firstConfigCheckbox.setValue(false)
    await nextTick()

    // select 'all devices'
    await allCheckbox.setValue(true)
    await nextTick()
    // view history + backup disabled (multi-select); download enabled
    expect(isBtnDisabled('view-history-btn')).toBe(true)
    expect(isBtnDisabled('download-btn')).toBe(false)
    expect(isBtnDisabled('backup-now-btn')).toBe(false)

    // change 'all devices' back to false
    await allCheckbox.setValue(false)
    await nextTick()
    // all actions back to disabled
    expect(isBtnDisabled('view-history-btn')).toBe(true)
    expect(isBtnDisabled('download-btn')).toBe(true)
    expect(isBtnDisabled('backup-now-btn')).toBe(true)

    // select second config (its service name is empty)
    await secondConfigCheckbox.setValue(true)
    await nextTick()
    // backup btn disabled because the only selected config has no service name
    expect(isBtnDisabled('backup-now-btn')).toBe(true)

    // also select first config (which has a service name)
    await firstConfigCheckbox.setValue(true)
    await nextTick()
    // backup btn enabled: more than one selected, so the single-no-service-name
    // guard no longer applies
    expect(isBtnDisabled('backup-now-btn')).toBe(false)
  })
})
