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

import WsmanDefaultsDialog from '@/components/ManageWsman/WsmanDefaultsDialog.vue'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/stores/wsmanAdminStore')

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

const SETTINGS = {
  retry: 1, timeout: 30000, username: 'root', hasPassword: true, port: null, maxElements: null,
  ssl: true, strictSsl: false, path: '/wsman', productVendor: null, productVersion: null, gssAuth: null
}
const CONFIG = {
  version: 'v1',
  defaults: SETTINGS,
  definitions: [{ ...SETTINGS, ranges: [{ begin: '10.0.0.1', end: '10.0.0.9' }], specifics: [], ipMatches: [] }]
}

describe('WsmanDefaultsDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async () => {
    wrapper = mount(WsmanDefaultsDialog, {
      props: { visible: false, config: CONFIG },
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

  it('saves the edited defaults with the definitions riding along by index', async () => {
    await mountDialog()
    await wrapper.find('[data-test="username-input"]').setValue('operator')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()

    expect(store.saveConfig).toHaveBeenCalledTimes(1)
    const input = store.saveConfig.mock.calls[0][0]
    expect(input.version).toBe('v1')
    expect(input.defaults).toMatchObject({ username: 'operator', password: null, clearPassword: false, ssl: true })
    expect(input.definitions).toHaveLength(1)
    expect(input.definitions[0]).toMatchObject({ sourceIndex: 0, password: null, ranges: [{ begin: '10.0.0.1', end: '10.0.0.9' }] })
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('shows the server reason and stays open on failure', async () => {
    store.saveConfig.mockResolvedValue('The defaults: the port must be between 1 and 65535.')
    await mountDialog()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('port')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })

  it('disables saving while a setting is out of range', async () => {
    await mountDialog()
    await wrapper.find('[data-test="path-input"]').setValue('wsman path')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })
})
