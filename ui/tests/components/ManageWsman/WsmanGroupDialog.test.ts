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

import WsmanGroupDialog from '@/components/ManageWsman/WsmanGroupDialog.vue'
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

const DC = {
  rrdRepository: '/rrd',
  sources: ['wsman-datacollection-config.xml', 'dell-idrac.xml'],
  versions: { 'wsman-datacollection-config.xml': 'root-v', 'dell-idrac.xml': 'drac-v' },
  collections: [],
  groups: [{ name: 'drac-system', source: 'dell-idrac.xml', resourceType: 'node', resourceUri: 'uri', dialect: null, filter: null, attributes: [{ name: 'A', alias: 'a', type: 'gauge', indexOf: null, filter: null }] }],
  systemDefinitions: []
}

describe('WsmanGroupDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (original: any) => {
    wrapper = mount(WsmanGroupDialog, {
      props: { visible: false, dataCollection: DC, original },
      global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}
    })
    await wrapper.setProps({ visible: true })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    store = { saveDataCollectionFile: vi.fn().mockResolvedValue(null) }
    vi.mocked(useWsmanAdminStore).mockReturnValue(store)
  })

  it('refuses a group without attributes, then saves a new one into the root file with its version', async () => {
    await mountDialog(null)
    await wrapper.find('[data-test="name-input"]').setValue('win-cpu')
    await wrapper.find('[data-test="resource-uri-input"]').setValue('http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/*')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="add-attribute"]').trigger('click')
    await wrapper.find('[data-test="attribute-row-0"] [data-test="attribute-name"]').setValue('LoadPercentage')
    await wrapper.find('[data-test="attribute-row-0"] [data-test="attribute-alias"]').setValue('cpuLoad')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()

    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const [file, input] = store.saveDataCollectionFile.mock.calls[0]
    expect(file).toBe('wsman-datacollection-config.xml')
    expect(input.version).toBe('root-v')
    expect(input.rrdRepository).toBe('/rrd')
    expect(input.groups).toEqual([{ name: 'win-cpu', resourceType: 'node', resourceUri: 'http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/*', dialect: null, filter: null,
      attributes: [{ name: 'LoadPercentage', alias: 'cpuLoad', type: 'gauge', indexOf: null, filter: null }] }])
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('flags a name already used in another file', async () => {
    await mountDialog(null)
    await wrapper.find('[data-test="name-input"]').setValue('drac-system')
    expect(wrapper.text()).toContain('already exists')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('edits an existing group in place in its own file', async () => {
    await mountDialog(DC.groups[0])
    await wrapper.find('[data-test="filter-input"]').setValue('select A from X')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const [file, input] = store.saveDataCollectionFile.mock.calls[0]
    expect(file).toBe('dell-idrac.xml')
    expect(input.version).toBe('drac-v')
    expect(input.groups).toHaveLength(1)
    expect(input.groups[0]).toMatchObject({ name: 'drac-system', filter: 'select A from X' })
  })

  it('shows the server reason and stays open on failure', async () => {
    store.saveDataCollectionFile.mockResolvedValue('System definition Dell iDRAC 8 includes group drac-system, which does not exist in any file.')
    await mountDialog(DC.groups[0])
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toContain('does not exist')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })
})
