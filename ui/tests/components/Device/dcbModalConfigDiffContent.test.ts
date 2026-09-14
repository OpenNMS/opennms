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

import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import { OnmsTooltip } from '@opennms/onms-ui'
import { nextTick } from 'vue'
import { describe, expect, test } from 'vitest'
import dateFormatDirective from '@/directives/v-date'
import DCBModalConfigDiffContent from '@/components/Device/DCBModalConfigDiffContent.vue'
import { useDeviceStore } from '@/stores/deviceStore'
import { DeviceConfigBackup } from '@/types/deviceConfig'

const mockBackup = (id: number, config: string, lastBackupDate: number): DeviceConfigBackup => ({
  id,
  deviceName: 'Cisco-7201',
  location: 'location',
  ipAddress: '10.21.10.81',
  lastSucceededDate: lastBackupDate,
  lastUpdatedDate: lastBackupDate,
  backupStatus: 'success',
  nextScheduledBackupDate: lastBackupDate,
  scheduledInterval: { deviceConfig: 'daily' },
  config,
  configType: 'running',
  configName: 'Running Configuration',
  ipInterfaceId: 1,
  lastBackupDate,
  lastFailedDate: lastBackupDate,
  fileName: 'filename',
  failureReason: 'reason',
  encoding: '',
  nodeId: 1,
  nodeLabel: 'node1',
  operatingSystem: '',
  isSuccessfulBackup: true,
  monitoredServiceId: 1,
  serviceName: 'DeviceConfig-running'
})

const mountDiffContent = (backups: DeviceConfigBackup[]) => {
  const wrapper = mount(DCBModalConfigDiffContent, {
    global: {
      plugins: [createTestingPinia(), PrimeVue],
      directives: {
        date: dateFormatDirective,
        'onms-tooltip': OnmsTooltip
      },
      stubs: {
        DCBDiff: true
      }
    }
  })

  const deviceStore = useDeviceStore()
  deviceStore.historyModalBackups = backups

  return wrapper
}

// Guards the `diffLines` contract from the `diff` package (NMS-19725: v8 -> v9
// major): change objects must keep the `added` / `removed` flags this
// component counts to render the +/- differences summary.
describe('DCBModalConfigDiffContent diff counts', () => {
  test('counts one added and one removed chunk for a single changed line', async () => {
    const wrapper = mountDiffContent([
      mockBackup(1, 'line1\nline2\nline3\n', 1643831118973),
      mockBackup(2, 'line1\nlineX\nline3\n', 1643831128973)
    ])
    await nextTick()

    const checkboxes = wrapper.findAll('.history-date input')
    expect(checkboxes.length).toBe(2)

    await checkboxes[0].setValue(true)
    await checkboxes[1].setValue(true)

    await wrapper.get('.compare-btn').trigger('click')
    await nextTick()

    expect(wrapper.get('.changes').text()).toContain('-1')
    expect(wrapper.get('.changes').text()).toContain('+1')
  })

  test('counts each non-contiguous changed region as its own chunk', async () => {
    const wrapper = mountDiffContent([
      mockBackup(1, 'a\nb\nc\nd\ne\n', 1643831118973),
      mockBackup(2, 'a\nB\nc\nD\ne\n', 1643831128973)
    ])
    await nextTick()

    const checkboxes = wrapper.findAll('.history-date input')

    await checkboxes[0].setValue(true)
    await checkboxes[1].setValue(true)

    await wrapper.get('.compare-btn').trigger('click')
    await nextTick()

    expect(wrapper.get('.changes').text()).toContain('-2')
    expect(wrapper.get('.changes').text()).toContain('+2')
  })
})
