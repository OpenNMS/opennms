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

import AboutDialog from '@/components/DistributedMonitoring/AboutDialog.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible" data-test="dialog"><h2 data-test="dialog-header">{{ header }}</h2><slot /></div>'
}

const mountDialog = () => mount(AboutDialog, { global: { plugins: [PrimeVue], stubs: { Dialog: DialogStub }}})

describe('AboutDialog.vue', () => {
  it('shows only the Info button until it is clicked', async () => {
    const wrapper = mountDialog()
    const button = wrapper.find('[data-test="about-button"]')
    expect(button.attributes('title')).toBe('About Minions and Locations')
    expect(wrapper.find('[data-test="dialog"]').exists()).toBe(false)
    await button.trigger('click')
    expect(wrapper.find('[data-test="dialog-header"]').text()).toBe('About Minions and Locations')
  })

  it('explains Minions first and monitoring locations second, in the order of the tabs', async () => {
    const wrapper = mountDialog()
    await wrapper.find('[data-test="about-button"]').trigger('click')
    const sections = wrapper.findAll('.help-section').map(s => s.find('.section-heading').text())
    expect(sections).toEqual(['Minions', 'Monitoring locations'])
    const minions = wrapper.find('[data-test="about-minions"]').text()
    expect(minions).toContain('org.opennms.minion.controller')
    expect(minions).toContain('a Minion that is up cannot be deleted')
    const locations = wrapper.find('[data-test="about-locations"]').text()
    expect(locations).toContain('Created automatically')
    expect(locations).toContain('Only possible with no nodes or Minions assigned.')
    expect(locations).not.toContain('typing')
  })
})
