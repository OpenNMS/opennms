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

const existing = { uei: 'uei.opennms.org/wsman/eventlog/unexpectedShutdown', exists: true, sourceName: 'opennms.wsman.eventlog.events', label: 'Windows unexpected shutdown', description: 'd', logMessage: 'l', severity: 'Major', alarm: true, alarmType: 3, reductionKey: '%uei%:%dpname%:%nodeid%' }

const mountDialog = async (originalIndex: number | null, original: any) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useWsmanAdminStore(pinia)
  vi.mocked(store.saveEventLog).mockResolvedValue({ success: true, message: '' })
  vi.mocked(store.saveEventLogDefinition).mockResolvedValue({ success: true, message: '' })
  vi.mocked(store.getEventLogDefinition).mockImplementation(async (uei: string) => (uei === existing.uei ? existing : { uei, exists: false, label: null, description: null, logMessage: null, severity: null, alarm: false, alarmType: null, reductionKey: null }))
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
    // a new UEI creates its definition by default, so the label is needed too
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
  })

  it('appends a mapping and replaces one by index', async () => {
    const added = await mountDialog(null, null)
    await added.wrapper.find('[data-test="uei-input"]').setValue('uei.opennms.org/wsman/eventlog/diskFull')
    await added.wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
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

  it('offers to create the definition of a new UEI and saves it after the mapping', async () => {
    vi.useFakeTimers()
    try {
      const { wrapper, store } = await mountDialog(null, null)
      await wrapper.find('[data-test="uei-input"]').setValue('uei.opennms.org/wsman/eventlog/diskFull')
      vi.advanceTimersByTime(500)
      await flushPromises()
      expect(wrapper.find('[data-test="definition-missing"]').exists()).toBe(true)
      expect((wrapper.find('[data-test="save-definition"] input').element as HTMLInputElement).checked).toBe(true)
      // a label is required while the definition is being created
      expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
      await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
      expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
      await wrapper.find('[data-test="save-button"]').trigger('click')
      await flushPromises()
      expect(vi.mocked(store.saveEventLog)).toHaveBeenCalledTimes(1)
      expect(vi.mocked(store.saveEventLogDefinition).mock.calls[0][0]).toMatchObject({ uei: 'uei.opennms.org/wsman/eventlog/diskFull', label: 'Windows disk full', severity: 'Warning', alarm: true, alarmType: 3 })
      expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    } finally {
      vi.useRealTimers()
    }
  })

  it('leaves an existing definition alone unless asked to update it', async () => {
    const { wrapper, store } = await mountDialog(0, config.packages[0].eventMappings[0])
    expect(wrapper.find('[data-test="definition-exists"]').text()).toContain('opennms.wsman.eventlog.events')
    expect(wrapper.find('[data-test="definition-label"]').exists()).toBe(false)
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(vi.mocked(store.saveEventLogDefinition)).not.toHaveBeenCalled()

    const again = await mountDialog(0, config.packages[0].eventMappings[0])
    await again.wrapper.find('[data-test="save-definition"] input').setValue(true)
    expect((again.wrapper.find('[data-test="definition-label"]').element as HTMLInputElement).value).toBe('Windows unexpected shutdown')
    await again.wrapper.find('[data-test="definition-label"]').setValue('Unexpected shutdown')
    await again.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(vi.mocked(again.store.saveEventLogDefinition).mock.calls[0][0]).toMatchObject({ label: 'Unexpected shutdown', exists: true })
  })

  it('keeps the dialog open when the definition fails after the mapping saved', async () => {
    const { wrapper, store } = await mountDialog(null, null)
    vi.mocked(store.saveEventLogDefinition).mockResolvedValue({ success: false, message: 'Unknown severity' })
    await wrapper.find('[data-test="uei-input"]').setValue('uei.opennms.org/wsman/eventlog/diskFull')
    await new Promise(resolve => setTimeout(resolve, 450))
    await flushPromises()
    await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('Unknown severity')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })
})
