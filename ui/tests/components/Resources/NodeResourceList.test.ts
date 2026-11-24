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

import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import NodeResourceList from '@/components/Resources/NodeResourceList.vue'
import { Resource } from '@/types'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() })
}))

const resource = (id: string, label: string, typeLabel: string) => ({
  id,
  label,
  typeLabel,
  name: label,
  link: '',
  parentId: 'node[1]',
  externalValueAttributes: {},
  rrdGraphAttributes: {},
  stringPropertyAttributes: {}
}) as Resource

const mountList = (resources: Resource[]) => mount(NodeResourceList, {
  global: {
    plugins: [
      createTestingPinia({
        initialState: { resourceStore: { nodeResource: { children: { resource: resources }}}}
      }),
      PrimeVue
    ]
  }
})

describe('NodeResourceList.vue', () => {
  it('groups the resources under their type label, in order of first appearance', () => {
    const wrapper = mountList([
      resource('node[1].interfaceSnmp[eth0]', 'eth0', 'SNMP Interface Data'),
      resource('node[1].nodeSnmp[]', 'Node-level Performance Data', 'SNMP Node Data'),
      resource('node[1].interfaceSnmp[eth1]', 'eth1', 'SNMP Interface Data')
    ])

    expect(wrapper.findAll('.list-header').map(header => header.text())).toEqual(['SNMP Interface Data', 'SNMP Node Data'])
    expect(wrapper.findAll('.list-item label').map(label => label.text())).toEqual(['eth0', 'eth1', 'Node-level Performance Data'])
  })

  it('groups type labels that are also Object.prototype property names like any other', () => {
    const wrapper = mountList([
      resource('node[1].a[1]', 'first', 'constructor'),
      resource('node[1].b[1]', 'second', '__proto__'),
      resource('node[1].c[1]', 'third', 'toString'),
      resource('node[1].a[2]', 'fourth', 'constructor')
    ])

    expect(wrapper.findAll('.list-header').map(header => header.text())).toEqual(['constructor', '__proto__', 'toString'])
    expect(wrapper.findAll('.list-item label').map(label => label.text())).toEqual(['first', 'fourth', 'second', 'third'])
  })
})
