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

import WsmanHelpPanel from '@/components/ManageWsman/WsmanHelpPanel.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const TogglePanelStub = { name: 'TogglePanel', template: '<div><slot name="header" /><slot /></div>' }

describe('WsmanHelpPanel.vue', () => {
  it('renders the help content', () => {
    const wrapper = mount(WsmanHelpPanel, {
      global: { plugins: [PrimeVue], stubs: { TogglePanel: TogglePanelStub }}
    })
    expect(wrapper.text()).toContain('About WS-Man')
    expect(wrapper.text()).toContain('wsman-config.xml')
  })
})
