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

  it('refuses to add a span whose end is not after its start', async () => {
    const wrapper = mountEditor({ type: 'daily', times: [] })
    const vm = wrapper.vm as any
    vm.fields.start = new Date(2026, 0, 1, 10, 0, 0)
    vm.fields.end = new Date(2026, 0, 1, 9, 0, 0)
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="span-error"]').text()).toContain('end must come after the start')
    expect(wrapper.find('[data-test="add-time"]').attributes('disabled')).toBeDefined()
    await wrapper.find('[data-test="add-time"]').trigger('click')
    expect(wrapper.emitted('add')).toBeUndefined()
  })

  it('refuses to add a span with a cleared time field instead of throwing', async () => {
    const wrapper = mountEditor({ type: 'specific', times: [] })
    const vm = wrapper.vm as any
    vm.fields.end = null
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="span-error"]').text()).toContain('both a start and an end')
    await wrapper.find('[data-test="add-time"]').trigger('click')
    expect(wrapper.emitted('add')).toBeUndefined()
  })

  it('lists the saved spans before the add controls', () => {
    // an opened outage must read as filled in; the pickers only add a new span
    const wrapper = mountEditor({ type: 'daily', times: [{ begins: '01:00:00', ends: '02:00:00' }] })
    const html = wrapper.html()
    expect(html.indexOf('data-test="time-list"')).toBeLessThan(html.indexOf('data-test="new-span"'))
    expect(wrapper.find('[data-test="new-span"]').text()).toContain('Add a time span')
    expect(wrapper.find('[data-test="time-empty"]').exists()).toBe(false)
  })

  it('shows an empty-state line when there are no spans', () => {
    const wrapper = mountEditor({ type: 'daily', times: [] })
    expect(wrapper.find('[data-test="time-empty"]').text()).toContain('No time spans yet')
    expect(wrapper.find('[data-test="time-row"]').exists()).toBe(false)
  })

  it('describes weekly and monthly spans with their day', () => {
    const weekly = mountEditor({ type: 'weekly', times: [{ day: 'monday', begins: '01:00:00', ends: '02:00:00' }] })
    expect(weekly.find('[data-test="time-row"]').text()).toContain('Monday 01:00:00')
    const monthly = mountEditor({ type: 'monthly', times: [{ day: '15', begins: '01:00:00', ends: '02:00:00' }] })
    expect(monthly.find('[data-test="time-row"]').text()).toContain('Day 15 01:00:00')
  })

  it('lists existing spans with a remove control', async () => {
    const wrapper = mountEditor({ type: 'daily', times: [{ begins: '01:00:00', ends: '02:00:00' }] })
    expect(wrapper.find('[data-test="time-row"]').text()).toContain('01:00:00')
    await wrapper.find('[data-test="remove-time"]').trigger('click')
    expect(wrapper.emitted('remove')).toEqual([[0]])
  })

  it('resets the shared day field to a value the new type offers', async () => {
    const wrapper = mountEditor({ type: 'weekly', times: [] })
    const vm = wrapper.vm as any
    expect(vm.fields.day).toBe('sunday')
    await wrapper.setProps({ type: 'monthly' })
    expect(vm.fields.day).toBe('1')
    await wrapper.find('[data-test="add-time"]').trigger('click')
    expect((wrapper.emitted('add')![0] as any[])[0].day).toBe('1')
    await wrapper.setProps({ type: 'weekly' })
    expect(vm.fields.day).toBe('sunday')
  })

  it('ignores a direct add while the fields are invalid', () => {
    const wrapper = mountEditor({ type: 'daily', times: [] })
    const vm = wrapper.vm as any
    vm.fields.start = null
    vm.addSpan()
    expect(wrapper.emitted('add')).toBeUndefined()
  })
})
