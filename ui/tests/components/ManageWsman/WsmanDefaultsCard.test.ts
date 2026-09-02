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

import WsmanDefaultsCard from '@/components/ManageWsman/WsmanDefaultsCard.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const OnmsCardStub = { name: 'OnmsCard', template: '<div><slot name="title" /><slot name="content" /></div>' }

const SETTINGS = {
  retry: 1, timeout: 30000, username: 'root', hasPassword: true, port: null, maxElements: null,
  ssl: true, strictSsl: false, path: '/wsman', productVendor: null, productVersion: null, gssAuth: null
}

describe('WsmanDefaultsCard.vue', () => {
  it('renders every setting, with the password as set/not set and absent values as a dash', () => {
    const wrapper = mount(WsmanDefaultsCard, {
      props: { settings: SETTINGS },
      global: { plugins: [PrimeVue], stubs: { OnmsCard: OnmsCardStub }}
    })
    expect(wrapper.find('[data-test="default-username"]').text()).toBe('root')
    expect(wrapper.find('[data-test="default-hasPassword"]').text()).toBe('Set')
    expect(wrapper.find('[data-test="default-ssl"]').text()).toBe('Yes')
    expect(wrapper.find('[data-test="default-strictSsl"]').text()).toBe('No')
    expect(wrapper.find('[data-test="default-timeout"]').text()).toBe('30000')
    expect(wrapper.find('[data-test="default-port"]').text()).toBe('—')
    // the value itself is never part of the payload, so it can never be rendered
    expect(wrapper.text()).not.toContain('calvin')
  })

  it('reports an unset password', () => {
    const wrapper = mount(WsmanDefaultsCard, {
      props: { settings: { ...SETTINGS, hasPassword: false }},
      global: { plugins: [PrimeVue], stubs: { OnmsCard: OnmsCardStub }}
    })
    expect(wrapper.find('[data-test="default-hasPassword"]').text()).toBe('Not set')
  })
})
