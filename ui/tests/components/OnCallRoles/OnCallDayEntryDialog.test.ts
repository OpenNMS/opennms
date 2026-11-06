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

import { enableAutoUnmount, mount } from '@vue/test-utils'
import { OnmsSelect } from '@opennms/onms-ui'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, describe, expect, it } from 'vitest'

import OnCallDayEntryDialog from '@/components/OnCallRoles/OnCallDayEntryDialog.vue'
import { datePicker, pickDate } from '../../utils'

// the dialog is teleported to the body
enableAutoUnmount(afterEach)

describe('OnCallDayEntryDialog', () => {
  const mountDialog = () => mount(OnCallDayEntryDialog, {
    props: { visible: true, day: '2026-11-05', members: ['bob', 'carol'] },
    global: { plugins: [PrimeVue] }
  })
  const find = (selector: string) => document.body.querySelector(selector) as HTMLInputElement
  const pickUser = async (wrapper: ReturnType<typeof mountDialog>, user: string) => {
    // the dialog renders its content a tick after it opens
    await nextTick()
    wrapper.findComponent(OnmsSelect).vm.$emit('update:modelValue', user)
    await nextTick()
  }

  it('starts with no user and the whole day, and disables OK until a user is chosen', async () => {
    const wrapper = mountDialog()
    await nextTick()
    expect(wrapper.findComponent(OnmsSelect).props('modelValue')).toBe('')
    expect(datePicker(wrapper, 'entry-begin').props('modelValue')).toEqual(new Date(2026, 10, 5, 0, 0))
    expect(datePicker(wrapper, 'entry-begin').props()).toMatchObject({ showTime: true, timeOnly: false, hourFormat: '12' })
    expect(datePicker(wrapper, 'entry-end-date').props('modelValue')).toEqual(new Date(2026, 10, 5))
    expect(datePicker(wrapper, 'entry-end-date').props()).toMatchObject({ showTime: false, timeOnly: false })
    expect(find('[data-test="ok-button"]').hasAttribute('disabled')).toBe(true)
  })

  it('emits the whole day for the chosen user and closes', async () => {
    const wrapper = mountDialog()
    await pickUser(wrapper, 'bob')
    find('[data-test="ok-button"]').click()
    await nextTick()
    expect(wrapper.emitted('ok')).toEqual([[{ user: 'bob', start: '2026-11-05T00:00', end: '2026-11-05T23:59', endOfDay: true }]])
    expect(wrapper.emitted('update:visible')).toEqual([[false]])
  })

  it('emits an explicit range when end of day is off, and rejects a range that ends before it begins', async () => {
    const wrapper = mountDialog()
    await pickUser(wrapper, 'carol')
    find('#oncall-day-entry-eod').click()
    await nextTick()
    expect(datePicker(wrapper, 'entry-end').props()).toMatchObject({ showTime: true, hourFormat: '12' })
    await pickDate(wrapper, 'entry-end', new Date(2026, 10, 5, 0, 0))
    expect(document.body.textContent).toContain('The time must begin before it ends.')
    expect(find('[data-test="ok-button"]').hasAttribute('disabled')).toBe(true)

    await pickDate(wrapper, 'entry-end', new Date(2026, 10, 5, 9, 30))
    find('[data-test="ok-button"]').click()
    expect(wrapper.emitted('ok')).toEqual([[{ user: 'carol', start: '2026-11-05T00:00', end: '2026-11-05T09:30', endOfDay: false }]])
  })

  it('accepts a begin at 23:59 with end of day, which runs through 23:59:59', async () => {
    const wrapper = mountDialog()
    await pickUser(wrapper, 'bob')
    await pickDate(wrapper, 'entry-begin', new Date(2026, 10, 5, 23, 59))
    expect(document.body.textContent).not.toContain('The time must begin before it ends.')
    find('[data-test="ok-button"]').click()
    expect(wrapper.emitted('ok')).toEqual([[{ user: 'bob', start: '2026-11-05T23:59', end: '2026-11-05T23:59', endOfDay: true }]])
  })

  it('does not emit on Cancel', async () => {
    const wrapper = mountDialog()
    await nextTick()
    find('[data-test="cancel-button"]').click()
    expect(wrapper.emitted('ok')).toBeUndefined()
    expect(wrapper.emitted('update:visible')).toEqual([[false]])
  })
})
