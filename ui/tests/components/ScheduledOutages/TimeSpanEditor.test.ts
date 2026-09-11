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

import TimeSpanEditor from '@/components/ScheduledOutages/TimeSpanEditor.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountEditor = (props: any) => mount(TimeSpanEditor, {
  props,
  global: { plugins: [PrimeVue] }
})

describe('TimeSpanEditor.vue', () => {
  // The Java parser keys on exact string length (20 for specific), so the add
  // event must carry a padded date even for the default (day 01) selection.
  it('emits a 20-char begins/ends for a specific span from the default fields', async () => {
    const wrapper = mountEditor({ type: 'specific', times: [] })
    await wrapper.find('[data-test="add-time"]').trigger('click')

    const [time] = wrapper.emitted('add')![0] as any[]
    expect(time.begins.length).toBe(20)
    expect(time.ends.length).toBe(20)
    expect(time.day).toBeUndefined()
  })

  it('emits an 8-char span with the weekday for a weekly outage', async () => {
    const wrapper = mountEditor({ type: 'weekly', times: [] })
    await wrapper.find('[data-test="add-time"]').trigger('click')

    const [time] = wrapper.emitted('add')![0] as any[]
    expect(time.begins.length).toBe(8)
    expect(time.day).toBe('sunday')
  })

  it('lists existing spans with a remove control', async () => {
    const wrapper = mountEditor({ type: 'daily', times: [{ begins: '01:00:00', ends: '02:00:00' }] })
    expect(wrapper.find('[data-test="time-row"]').text()).toContain('01:00:00')
    await wrapper.find('[data-test="remove-time"]').trigger('click')
    expect(wrapper.emitted('remove')).toEqual([[0]])
  })
})
