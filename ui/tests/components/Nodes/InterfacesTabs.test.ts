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

import InterfacesTabs from '@/components/Nodes/InterfacesTabs.vue'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '42' }})
}))

// Stub child tables to avoid needing store data; we're testing tab structure only
vi.mock('@/components/Nodes/IpInterfacesTable.vue', () => ({
  default: { name: 'IpInterfacesTable', template: '<div data-test="ip-interfaces-table-stub" />' }
}))

vi.mock('@/components/Nodes/SnmpInterfacesTable.vue', () => ({
  default: { name: 'SnmpInterfacesTable', template: '<div data-test="snmp-interfaces-table-stub" />' }
}))

let wrapper: VueWrapper<any>
const mountComponent = () => {
  wrapper = mount(InterfacesTabs, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn }), PrimeVue]
    }
  })
  return wrapper
}

describe('InterfacesTabs.vue', () => {
  afterEach(() => {
    // Unmount so PrimeVue TabList's template refs are cleared before its
    // orphaned mounted() setTimeout(updateInkBar, 150) fires. Without this the
    // timer outlives the test file and runs against the torn-down happy-dom
    // environment, throwing "HTMLElement is not defined" (flaky on CI).
    wrapper?.unmount()
  })

  describe('Tab labels', () => {
    it('renders two tabs with correct labels', () => {
      const wrapper = mountComponent()
      const tabs = wrapper.findAll('[data-pc-name="tab"]')
      const tabTexts = tabs.map(t => t.text())
      expect(tabTexts).toContain('IP Interfaces')
      expect(tabTexts).toContain('SNMP Interfaces')
    })
  })

  describe('Tab panel content', () => {
    it('renders IpInterfacesTable in tab 0 by default', () => {
      const wrapper = mountComponent()
      expect(wrapper.find('[data-test="ip-interfaces-table-stub"]').exists()).toBe(true)
    })

    it('renders SnmpInterfacesTable in tab 1 after switching', async () => {
      const wrapper = mountComponent()
      // Click the second tab
      const tabs = wrapper.findAll('[data-pc-name="tab"]')
      await tabs[1].trigger('click')
      expect(wrapper.find('[data-test="snmp-interfaces-table-stub"]').exists()).toBe(true)
    })
  })
})
