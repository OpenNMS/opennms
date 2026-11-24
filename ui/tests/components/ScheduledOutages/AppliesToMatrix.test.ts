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

import AppliesToMatrix from '@/components/ScheduledOutages/AppliesToMatrix.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const PROPS = {
  notifications: false,
  pollers: [{ name: 'example1', applied: false, calendars: [] }],
  thresholders: [{ name: 'mib2', applied: true, calendars: [] }],
  collectors: []
}

const mountMatrix = () => mount(AppliesToMatrix, {
  props: PROPS,
  global: { plugins: [PrimeVue] }
})

describe('AppliesToMatrix.vue', () => {
  it('emits togglePackage with the subsystem and package name', async () => {
    const wrapper = mountMatrix()
    await wrapper.find('[data-test="applies-pollerd"] input').setValue(true)
    expect(wrapper.emitted('togglePackage')).toEqual([['pollerd', 'example1', true]])
  })

  it('emits setAll per subsystem from the bulk links', async () => {
    const wrapper = mountMatrix()
    await wrapper.findAll('[data-test="select-all"]')[0].trigger('click')
    await wrapper.findAll('[data-test="unselect-all"]')[2].trigger('click')
    expect(wrapper.emitted('setAll')).toEqual([['pollerd', true], ['collectd', false]])
  })

  it('emits update:notifications from the notifications checkbox', async () => {
    const wrapper = mountMatrix()
    await wrapper.find('[data-test="applies-notifications"] input').setValue(true)
    expect(wrapper.emitted('update:notifications')).toEqual([[true]])
  })
})
