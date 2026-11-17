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

import AboutDialog from '@/components/ManageApplications/AboutDialog.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible" data-test="dialog"><h2 data-test="dialog-header">{{ header }}</h2><slot /></div>'
}

describe('AboutDialog.vue', () => {
  it('opens the help for applications and perspective locations from the Info button', async () => {
    const wrapper = mount(AboutDialog, { global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}})
    expect(wrapper.find('[data-test="dialog"]').exists()).toBe(false)
    const button = wrapper.find('[data-test="about-button"]')
    expect(button.attributes('title')).toBe('About Applications')
    await button.trigger('click')
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('About Applications')
    expect(wrapper.find('[data-test="about-applications"]').text()).toContain('groups monitored services')
    expect(wrapper.find('[data-test="about-perspectives"]').text()).toContain('perspective poller')
  })
})
