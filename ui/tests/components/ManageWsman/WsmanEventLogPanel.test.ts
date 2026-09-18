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

import WsmanEventLogPanel from '@/components/ManageWsman/WsmanEventLogPanel.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import { WsmanEventLogConfig } from '@/types/wsmanAdmin'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const config: WsmanEventLogConfig = {
  version: 'v1',
  threads: 4,
  retries: 1,
  targetRefreshInterval: '5m',
  packages: [{
    name: 'windows-servers',
    filter: 'IPADDR != \'0.0.0.0\'',
    logs: [
      { name: 'System', enabled: true, interval: 60000, maxRecords: 500, lookback: '1h', levels: 'Error,Warning', includeEventIds: null, excludeEventIds: null, mode: 'wql' },
      { name: 'Microsoft-Windows-TaskScheduler/Operational', enabled: false, interval: 300000, maxRecords: 200, lookback: '1h', levels: null, includeEventIds: null, excludeEventIds: null, mode: 'shell' }
    ],
    eventMappings: [{ logfile: 'System', source: null, eventId: 6008, uei: 'uei.opennms.org/wsman/eventlog/unexpectedShutdown', severity: 'Major' }]
  }]
}

const mountPanel = () => mount(WsmanEventLogPanel, {
  props: { config },
  global: { plugins: [PrimeVue], stubs: { OnmsCard: OnmsCardStub }}
})

describe('WsmanEventLogPanel.vue', () => {
  it('lists the logs with frequency, mode and enabled state per package', () => {
    const wrapper = mountPanel()
    expect(wrapper.find('[data-test="package-windows-servers"]').exists()).toBe(true)
    const rows = wrapper.findAll('[data-test="logs-table"] tbody tr')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('System')
    expect(rows[0].text()).toContain('minute')
    expect(rows[0].text()).toContain('WQL')
    expect(rows[1].text()).toContain('5 minutes')
    expect(rows[1].text()).toContain('Get-WinEvent')
    expect(rows[1].text()).toContain('All')
  })

  it('emits toggle, edit, delete and add for logs', async () => {
    const wrapper = mountPanel()
    await wrapper.findAll('[data-test="edit-log"]')[0].trigger('click')
    expect(wrapper.emitted('editLog')?.[0]).toEqual(['windows-servers', config.packages[0].logs[0]])
    await wrapper.findAll('[data-test="delete-log"]')[1].trigger('click')
    expect(wrapper.emitted('deleteLog')?.[0]).toEqual(['windows-servers', config.packages[0].logs[1]])
    await wrapper.find('[data-test="add-log"]').trigger('click')
    expect(wrapper.emitted('addLog')?.[0]).toEqual(['windows-servers'])
    wrapper.findAllComponents({ name: 'OnmsToggleSwitch' })[1].vm.$emit('update:modelValue', true)
    expect(wrapper.emitted('toggleLog')?.[0]).toEqual(['windows-servers', 'Microsoft-Windows-TaskScheduler/Operational', true])
  })

  it('lists the mappings and emits their actions with the index', async () => {
    const wrapper = mountPanel()
    const rows = wrapper.findAll('[data-test="mappings-table"] tbody tr')
    expect(rows).toHaveLength(1)
    expect(rows[0].text()).toContain('6008')
    expect(rows[0].text()).toContain('Major')
    await wrapper.find('[data-test="edit-mapping"]').trigger('click')
    expect(wrapper.emitted('editMapping')?.[0]).toEqual(['windows-servers', 0, config.packages[0].eventMappings[0]])
    await wrapper.find('[data-test="add-mapping"]').trigger('click')
    expect(wrapper.emitted('addMapping')?.[0]).toEqual(['windows-servers'])
  })
})
