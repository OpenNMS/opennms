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

import WsmanEventLogMappingDialog from '@/components/ManageWsman/WsmanEventLogMappingDialog.vue'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'
import { WsmanEventLogConfig } from '@/types/wsmanAdmin'

const DialogStub = { name: 'Dialog', props: ['visible', 'header', 'modal'], template: '<div v-if="visible"><slot /><slot name="footer" /></div>' }

const config: WsmanEventLogConfig = {
  version: 'v1', threads: 4, retries: 1, targetRefreshInterval: '5m',
  packages: [{ name: 'windows', filter: 'IPADDR != \'0.0.0.0\'', logs: [], eventMappings: [{ logfile: 'System', source: null, eventId: 6008, uei: 'uei.opennms.org/wsman/eventlog/unexpectedShutdown', severity: 'Major' }] }]
}

const mountDialog = async (originalIndex: number | null, original: any) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useWsmanAdminStore(pinia)
  vi.mocked(store.saveEventLog).mockResolvedValue({ success: true, message: '' })
  const wrapper = mount(WsmanEventLogMappingDialog, {
    props: { visible: false, config, packageName: 'windows', originalIndex, original },
    global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

describe('WsmanEventLogMappingDialog.vue', () => {
  it('requires a UEI starting with uei.', async () => {
    const { wrapper } = await mountDialog(null, null)
    await wrapper.find('[data-test="uei-input"]').setValue('not-a-uei')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="uei-input"]').setValue('uei.opennms.org/wsman/eventlog/diskFull')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
  })

  it('appends a mapping and replaces one by index', async () => {
    const added = await mountDialog(null, null)
    await added.wrapper.find('[data-test="uei-input"]').setValue('uei.opennms.org/wsman/eventlog/diskFull')
    await added.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(vi.mocked(added.store.saveEventLog).mock.calls[0][0].packages[0].eventMappings).toHaveLength(2)

    const edited = await mountDialog(0, config.packages[0].eventMappings[0])
    await edited.wrapper.find('[data-test="uei-input"]').setValue('uei.opennms.org/wsman/eventlog/crash')
    await edited.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const saved = vi.mocked(edited.store.saveEventLog).mock.calls[0][0]
    expect(saved.packages[0].eventMappings).toHaveLength(1)
    expect(saved.packages[0].eventMappings[0].uei).toBe('uei.opennms.org/wsman/eventlog/crash')
    expect(saved.packages[0].eventMappings[0].eventId).toBe(6008)
  })
})
