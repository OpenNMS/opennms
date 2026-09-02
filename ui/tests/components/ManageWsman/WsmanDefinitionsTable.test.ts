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

import WsmanDefinitionsTable from '@/components/ManageWsman/WsmanDefinitionsTable.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const BASE = {
  retry: null, timeout: null, username: null, hasPassword: false, port: null, maxElements: null,
  ssl: null, strictSsl: null, path: null, productVendor: null, productVersion: null, gssAuth: null,
  ranges: [], specifics: [], ipMatches: []
}

const mountTable = (definitions: any[]) => mount(WsmanDefinitionsTable, {
  props: { definitions },
  global: { plugins: [PrimeVue], stubs: { OnmsCard: OnmsCardStub }}
})

describe('WsmanDefinitionsTable.vue', () => {
  it('shows the empty state when there are no definitions', () => {
    const wrapper = mountTable([])
    expect(wrapper.find('[data-test="no-definitions"]').exists()).toBe(true)
  })

  it('lists ranges, specific addresses and IPLIKE matches with endpoint and credential summaries', () => {
    const wrapper = mountTable([{
      ...BASE,
      ranges: [{ begin: '10.0.0.1', end: '10.0.0.50' }],
      specifics: ['10.0.1.7'],
      ipMatches: ['192.168.*.*'],
      username: 'monitor', hasPassword: true, ssl: false, port: 5985, timeout: 5000
    }])
    const matches = wrapper.find('[data-test="definition-0-matches"]').text()
    expect(matches).toContain('10.0.0.1 – 10.0.0.50')
    expect(matches).toContain('10.0.1.7')
    expect(matches).toContain('IPLIKE 192.168.*.*')
    expect(wrapper.text()).toContain('http · port 5985')
    expect(wrapper.text()).toContain('monitor · password set')
    expect(wrapper.text()).toContain('Timeout (ms): 5000')
    expect(wrapper.find('[data-test="no-definitions"]').exists()).toBe(false)
  })
})
