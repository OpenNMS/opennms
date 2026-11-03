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

import WsmanSystemDefinitionDialog from '@/components/ManageWsman/WsmanSystemDefinitionDialog.vue'
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
  groups: [{ name: 'drac-system', source: 'dell-idrac.xml', resourceType: 'node', resourceUri: 'uri', dialect: null, filter: null, attributes: [] }],
  systemDefinitions: [{ name: 'Dell iDRAC 8', source: 'dell-idrac.xml', rules: ['true'], includedGroups: ['drac-system'] }]
}

describe('WsmanSystemDefinitionDialog.vue', () => {
  let wrapper: VueWrapper<any>
  let store: any

  const mountDialog = async (original: any) => {
    wrapper = mount(WsmanSystemDefinitionDialog, {
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

  it('needs a rule and a group before it can be created', async () => {
    await mountDialog(null)
    await wrapper.find('[data-test="name-input"]').setValue('Custom')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="rules-input"]').setValue('#productVendor matches \'^Microsoft.*\'')
    await wrapper.find('[data-test="rules-add"]').trigger('click')
    expect(wrapper.find('[data-test="rules"]').text()).toContain('Microsoft')
    // still no group
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('saves an edited definition into its own file keeping the other objects there', async () => {
    await mountDialog(DC.systemDefinitions[0])
    await wrapper.find('[data-test="rules-input"]').setValue('#productVersion matches \'.*13G.*\'')
    await wrapper.find('[data-test="rules-add"]').trigger('click')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    const [file, input] = store.saveDataCollectionFile.mock.calls[0]
    expect(file).toBe('dell-idrac.xml')
    expect(input.groups.map((g: any) => g.name)).toEqual(['drac-system'])
    expect(input.systemDefinitions[0]).toEqual({ name: 'Dell iDRAC 8', rules: ['true', '#productVersion matches \'.*13G.*\''], includedGroups: ['drac-system'] })
  })
})
