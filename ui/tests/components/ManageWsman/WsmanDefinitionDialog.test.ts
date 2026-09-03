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

import WsmanDefinitionDialog from '@/components/ManageWsman/WsmanDefinitionDialog.vue'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/wsmanAdminStore')
// only the requisition-name lookup is faked; the rest of the module must stay
// real because the shared form components import from it
vi.mock('@/services', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/services')>()
  return { ...actual, default: { ...actual.default, getRequisitionNames: vi.fn().mockResolvedValue(['helper-smoke', 'windows']) }}
})

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

const SETTINGS = {
  retry: null, timeout: null, username: null, hasPassword: false, port: null, maxElements: null,
  ssl: null, strictSsl: null, path: null, productVendor: null, productVersion: null, gssAuth: null
}
const CONFIG = {
  version: 'v1',
  defaults: { ...SETTINGS, username: 'root', hasPassword: true },
  definitions: [{ ...SETTINGS, hasPassword: true, username: 'monitor', ranges: [{ begin: '10.0.0.1', end: '10.0.0.9' }], specifics: [], ipMatches: [], requisition: 'windows' }]
}

describe('WsmanDefinitionDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (index: number | null) => {
    wrapper = mount(WsmanDefinitionDialog, {
      props: { visible: false, config: CONFIG, index },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = { saveConfig: vi.fn().mockResolvedValue(null) }
    vi.mocked(useWsmanAdminStore).mockReturnValue(store)
  })

  it('refuses a new definition until it matches something, then appends it without a source index', async () => {
    await mountDialog(null)
    expect(wrapper.find('[data-test="criteria-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="specific-input"]').setValue('10.1.1.1')
    await wrapper.find('[data-test="add-specific"]').trigger('click')
    expect(wrapper.find('[data-test="specific-chips"]').text()).toContain('10.1.1.1')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const input = store.saveConfig.mock.calls[0][0]
    expect(input.definitions).toHaveLength(2)
    expect(input.definitions[0]).toMatchObject({ sourceIndex: 0, username: 'monitor', password: null, requisition: 'windows' })
    expect(input.definitions[1]).toMatchObject({ sourceIndex: null, specifics: ['10.1.1.1'], ranges: [], ipMatches: [], requisition: null })
    expect(input.version).toBe('v1')
    expect(input.defaults).toMatchObject({ username: 'root', password: null })
  })

  it('edits an existing definition in place, keeping its index', async () => {
    await mountDialog(0)
    expect(wrapper.find('[data-test="range-row-0"]').exists()).toBe(true)
    await wrapper.find('[data-test="username-input"]').setValue('svc-wsman')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const input = store.saveConfig.mock.calls[0][0]
    expect(input.definitions).toHaveLength(1)
    expect(input.definitions[0]).toMatchObject({ sourceIndex: 0, username: 'svc-wsman', ranges: [{ begin: '10.0.0.1', end: '10.0.0.9' }], requisition: 'windows' })
  })

  it('offers the existing requisitions and flags an invalid new name', async () => {
    await mountDialog(0)
    const select = wrapper.findAllComponents({ name: 'OnmsSelect' }).find(c => c.props('inputId') === 'wsman-definition-requisition')!
    expect((select.props('options') as any[]).map(o => o.value)).toEqual(['helper-smoke', 'windows'])
    await select.vm.$emit('update:modelValue', 'bad name')
    await flushPromises()
    expect(wrapper.text()).toContain('letters, digits')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('flags a reversed range and blocks saving', async () => {
    await mountDialog(0)
    await wrapper.find('[data-test="range-row-0"] [data-test="range-end"]').setValue('10.0.0.0')
    expect(wrapper.find('[data-test="range-row-0"]').text()).toContain('before')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('rejects a malformed or IPv6 IPLIKE pattern at the add button', async () => {
    await mountDialog(null)
    await wrapper.find('[data-test="ipmatch-input"]').setValue('10.0.*')
    expect(wrapper.find('[data-test="add-ipmatch"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="ipmatch-input"]').setValue('fe80:*:*:*:*:*:*:*')
    expect(wrapper.find('[data-test="add-ipmatch"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="ipmatch-input"]').setValue('10.0.*.*')
    expect(wrapper.find('[data-test="add-ipmatch"]').attributes('disabled')).toBeUndefined()
  })

  it('keeps the advanced connection settings folded until asked, and opens them when one is set', async () => {
    await mountDialog(null)
    expect(wrapper.find('[data-test="username-input"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="ssl-select"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="port-input"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="advanced-summary"]').exists()).toBe(false)
    await wrapper.find('[data-test="toggle-advanced"]').trigger('click')
    expect(wrapper.find('[data-test="port-input"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="vendor-input"]').exists()).toBe(true)

    wrapper.unmount()
    await mountDialog(0)
    expect(wrapper.find('[data-test="port-input"]').exists()).toBe(false)
    wrapper.unmount()

    wrapper = mount(WsmanDefinitionDialog, {
      props: { visible: false, config: { ...CONFIG, definitions: [{ ...CONFIG.definitions[0], port: 5986, productVendor: 'Dell' }] }, index: 0 },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    expect(wrapper.find('[data-test="port-input"]').exists()).toBe(true)
    await wrapper.find('[data-test="toggle-advanced"]').trigger('click')
    expect(wrapper.find('[data-test="port-input"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="advanced-summary"]').text()).toBe('Set: Port, Vendor')
  })

  it('re-evaluates the advanced fold every time the dialog opens', async () => {
    await mountDialog(null)
    expect(wrapper.find('[data-test="port-input"]').exists()).toBe(false)
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ config: { ...CONFIG, definitions: [{ ...CONFIG.definitions[0], timeout: 9000 }] }, index: 0 })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    expect(wrapper.find('[data-test="timeout-input"]').exists()).toBe(true)
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ index: null, visible: true })
    await flushPromises()
    expect(wrapper.find('[data-test="timeout-input"]').exists()).toBe(false)
  })
})
