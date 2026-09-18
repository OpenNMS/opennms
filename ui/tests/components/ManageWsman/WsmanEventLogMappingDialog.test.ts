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
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { WsmanEventLogConfig, WsmanEventLogDefinition } from '@/types/wsmanAdmin'

const DialogStub = { name: 'Dialog', props: ['visible', 'header', 'modal'], template: '<div v-if="visible"><slot /><slot name="footer" /></div>' }

const config: WsmanEventLogConfig = {
  version: 'v1', threads: 4, retries: 1, targetRefreshInterval: '5m',
  packages: [{ name: 'windows', filter: 'IPADDR != \'0.0.0.0\'', logs: [], eventMappings: [{ logfile: 'System', source: null, eventId: 6008, uei: 'uei.opennms.org/wsman/eventlog/unexpectedShutdown', severity: 'Major' }] }]
}

const existing: WsmanEventLogDefinition = { uei: 'uei.opennms.org/wsman/eventlog/unexpectedShutdown', exists: true, editable: true, sourceName: 'opennms.wsman.eventlog.events', label: 'Windows unexpected shutdown', description: 'd', logMessage: 'l', severity: 'Major', alarm: true, alarmType: 3, reductionKey: '%uei%:%dpname%:%nodeid%' }
const locked: WsmanEventLogDefinition = { ...existing, uei: 'uei.opennms.org/wsman/eventlog/vendorDefined', editable: false, sourceName: 'vendor-events', label: 'Vendor event' }
const missing = (uei: string): WsmanEventLogDefinition => ({ uei, exists: false, editable: true, label: null, description: null, logMessage: null, severity: null, alarm: false, alarmType: null, reductionKey: null })

const mountDialog = async (originalIndex: number | null, original: any) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const store = useWsmanAdminStore(pinia)
  vi.mocked(store.saveEventLog).mockResolvedValue({ success: true, message: '' })
  vi.mocked(store.saveEventLogDefinition).mockResolvedValue({ success: true, message: '' })
  vi.mocked(store.getEventLogDefinition).mockImplementation(async (uei: string) => [existing, locked].find(d => d.uei === uei) ?? missing(uei))
  const wrapper = mount(WsmanEventLogMappingDialog, {
    props: { visible: false, config, packageName: 'windows', originalIndex, original },
    global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

const setEventId = async (wrapper: VueWrapper, value: number | null) => {
  wrapper.findComponent({ name: 'OnmsInputNumber' }).vm.$emit('update:modelValue', value)
  await flushPromises()
}

// types a UEI and lets the debounced lookup answer
const typeUei = async (wrapper: VueWrapper, uei: string) => {
  await wrapper.find('[data-test="uei-input"]').setValue(uei)
  vi.advanceTimersByTime(500)
  await flushPromises()
}

const saveDisabled = (wrapper: VueWrapper) => wrapper.find('[data-test="save-button"]').attributes('disabled') !== undefined

describe('WsmanEventLogMappingDialog.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('requires a UEI starting with uei. and a name after the prefix', async () => {
    const { wrapper } = await mountDialog(null, null)
    await setEventId(wrapper, 41)
    // the default is the bare prefix
    expect(saveDisabled(wrapper)).toBe(true)
    expect(wrapper.text()).toContain('Add a name after the last /.')
    await typeUei(wrapper, 'not-a-uei')
    expect(saveDisabled(wrapper)).toBe(true)
    expect(wrapper.text()).toContain('A UEI starting with uei. is required.')
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull/')
    expect(saveDisabled(wrapper)).toBe(true)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    // a new UEI creates its definition by default, so the label is needed too
    expect(saveDisabled(wrapper)).toBe(true)
    await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    expect(saveDisabled(wrapper)).toBe(false)
  })

  it('requires an Event ID of 1 or more', async () => {
    const { wrapper } = await mountDialog(0, config.packages[0].eventMappings[0])
    expect(saveDisabled(wrapper)).toBe(false)
    await setEventId(wrapper, 0)
    expect(saveDisabled(wrapper)).toBe(true)
    expect(wrapper.text()).toContain('An Event ID of 1 or more is required.')
    await setEventId(wrapper, null)
    expect(saveDisabled(wrapper)).toBe(true)
    await setEventId(wrapper, 12)
    expect(saveDisabled(wrapper)).toBe(false)
  })

  it('appends a mapping and replaces one by index', async () => {
    const added = await mountDialog(null, null)
    await setEventId(added.wrapper, 2013)
    await typeUei(added.wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    await added.wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    await added.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const appended = vi.mocked(added.store.saveEventLog).mock.calls[0][0].packages[0].eventMappings
    expect(appended).toHaveLength(2)
    expect(appended[1]).toMatchObject({ eventId: 2013, uei: 'uei.opennms.org/wsman/eventlog/diskFull' })

    const edited = await mountDialog(0, config.packages[0].eventMappings[0])
    await typeUei(edited.wrapper, 'uei.opennms.org/wsman/eventlog/crash')
    await edited.wrapper.find('[data-test="definition-label"]').setValue('Crash')
    await edited.wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const saved = vi.mocked(edited.store.saveEventLog).mock.calls[0][0]
    expect(saved.packages[0].eventMappings).toHaveLength(1)
    expect(saved.packages[0].eventMappings[0].uei).toBe('uei.opennms.org/wsman/eventlog/crash')
    expect(saved.packages[0].eventMappings[0].eventId).toBe(6008)
  })

  it('offers to create the definition of a new UEI and saves it after the mapping', async () => {
    const { wrapper, store } = await mountDialog(null, null)
    await setEventId(wrapper, 2013)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    expect(wrapper.find('[data-test="definition-missing"]').exists()).toBe(true)
    expect((wrapper.find('[data-test="save-definition"] input').element as HTMLInputElement).checked).toBe(true)
    // a label is required while the definition is being created
    expect(saveDisabled(wrapper)).toBe(true)
    await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    expect(saveDisabled(wrapper)).toBe(false)
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(vi.mocked(store.saveEventLog)).toHaveBeenCalledTimes(1)
    expect(vi.mocked(store.saveEventLogDefinition).mock.calls[0][0]).toMatchObject({ uei: 'uei.opennms.org/wsman/eventlog/diskFull', label: 'Windows disk full', severity: 'Warning', alarm: true, alarmType: 3 })
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
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

  it('keeps a hand-set checkbox through later lookups', async () => {
    const { wrapper } = await mountDialog(null, null)
    await setEventId(wrapper, 2013)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    await wrapper.find('[data-test="save-definition"] input').setValue(false)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskAlmostFull')
    expect((wrapper.find('[data-test="save-definition"] input').element as HTMLInputElement).checked).toBe(false)
    expect(saveDisabled(wrapper)).toBe(false)
  })

  it('keeps the typed definition when the UEI is corrected afterwards', async () => {
    const { wrapper, store } = await mountDialog(null, null)
    await setEventId(wrapper, 2013)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFul')
    await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    await wrapper.find('[data-test="definition-logmsg"]').setValue('Disk full on %parm[computerName]%')
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    expect((wrapper.find('[data-test="definition-label"]').element as HTMLInputElement).value).toBe('Windows disk full')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(vi.mocked(store.saveEventLogDefinition).mock.calls[0][0]).toMatchObject({ uei: 'uei.opennms.org/wsman/eventlog/diskFull', label: 'Windows disk full', logMessage: 'Disk full on %parm[computerName]%' })
  })

  it('starts each open from a fresh draft', async () => {
    const { wrapper } = await mountDialog(null, null)
    await setEventId(wrapper, 2013)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    expect((wrapper.find('[data-test="definition-label"]').element as HTMLInputElement).value).toBe('')
  })

  it('finishes a pending lookup before saving so a locked definition is never written', async () => {
    const { wrapper, store } = await mountDialog(null, null)
    await setEventId(wrapper, 7034)
    await wrapper.find('[data-test="uei-input"]').setValue(locked.uei)
    // the debounce has not fired: no lookup answered yet
    expect(vi.mocked(store.getEventLogDefinition)).not.toHaveBeenCalledWith(locked.uei)
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(vi.mocked(store.getEventLogDefinition)).toHaveBeenCalledWith(locked.uei)
    expect(vi.mocked(store.saveEventLog)).toHaveBeenCalledTimes(1)
    expect(vi.mocked(store.saveEventLogDefinition)).not.toHaveBeenCalled()
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('drops a slow answer for an earlier UEI', async () => {
    const { wrapper, store } = await mountDialog(null, null)
    let answerFirst: (d: WsmanEventLogDefinition) => void = () => undefined
    vi.mocked(store.getEventLogDefinition).mockImplementationOnce(() => new Promise((resolve) => {
      answerFirst = resolve
    }))
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/slow')
    expect(wrapper.find('[data-test="definition-loading"]').exists()).toBe(true)
    await typeUei(wrapper, locked.uei)
    expect(wrapper.find('[data-test="definition-locked"]').exists()).toBe(true)
    answerFirst(missing('uei.opennms.org/wsman/eventlog/slow'))
    await flushPromises()
    expect(wrapper.find('[data-test="definition-locked"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="definition-loading"]').exists()).toBe(false)
  })

  it('shows a definition from another source as locked and never saves it', async () => {
    const { wrapper, store } = await mountDialog(0, { ...config.packages[0].eventMappings[0], uei: locked.uei })
    expect(wrapper.find('[data-test="definition-locked"]').text()).toContain('vendor-events')
    expect(wrapper.find('[data-test="definition-locked"]').text()).toContain('Event Configuration page')
    expect(wrapper.find('[data-test="save-definition"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="definition-label"]').exists()).toBe(false)
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(vi.mocked(store.saveEventLog)).toHaveBeenCalledTimes(1)
    expect(vi.mocked(store.saveEventLogDefinition)).not.toHaveBeenCalled()
  })

  it('says when the UEI could not be looked up and does not create a definition', async () => {
    const { wrapper } = await mountDialog(null, null)
    const store = useWsmanAdminStore()
    vi.mocked(store.getEventLogDefinition).mockResolvedValue(null)
    await setEventId(wrapper, 2013)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    expect(wrapper.find('[data-test="definition-unavailable"]').text()).toBe('The UEI could not be looked up.')
    expect((wrapper.find('[data-test="save-definition"] input').element as HTMLInputElement).checked).toBe(false)
    expect(saveDisabled(wrapper)).toBe(false)
  })

  it('keeps the dialog open when the definition fails after the mapping saved, and retries in place', async () => {
    const { wrapper, store } = await mountDialog(null, null)
    vi.mocked(store.saveEventLogDefinition).mockResolvedValue({ success: false, message: 'Unknown severity' })
    await setEventId(wrapper, 2013)
    await typeUei(wrapper, 'uei.opennms.org/wsman/eventlog/diskFull')
    await wrapper.find('[data-test="definition-label"]').setValue('Windows disk full')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('Unknown severity')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
    expect((wrapper.find('[data-test="definition-label"]').element as HTMLInputElement).value).toBe('Windows disk full')

    // the store has re-read the document with the mapping in it
    const savedDoc = vi.mocked(store.saveEventLog).mock.calls[0][0]
    await wrapper.setProps({ config: savedDoc })
    vi.mocked(store.saveEventLogDefinition).mockResolvedValue({ success: true, message: '' })
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const retried = vi.mocked(store.saveEventLog).mock.calls[1][0]
    expect(retried.packages[0].eventMappings).toHaveLength(savedDoc.packages[0].eventMappings.length)
    expect(retried.packages[0].eventMappings[1].uei).toBe('uei.opennms.org/wsman/eventlog/diskFull')
    expect(vi.mocked(store.saveEventLogDefinition)).toHaveBeenCalledTimes(2)
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })
})
