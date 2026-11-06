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
///

import { enableAutoUnmount, mount } from '@vue/test-utils'
import { OnmsDatePicker, OnmsIconButton, OnmsSelect } from '@opennms/onms-ui'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import OnCallScheduleCard from '@/components/OnCallRoles/OnCallScheduleCard.vue'
import { newCard, ScheduleCard, toCard, toSchedule } from '@/lib/onCallSchedule'
import { pickDate } from '../../utils'

enableAutoUnmount(afterEach)

const noop = { mounted: () => undefined }
const SHORT_MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

const mountCard = (card: ScheduleCard, members = ['bob', 'carol']) =>
  mount(OnCallScheduleCard, {
    props: { card, members, highlighted: false },
    global: { plugins: [PrimeVue], directives: { 'onms-tooltip': noop, tooltip: noop }}
  })

const select = (w: ReturnType<typeof mountCard>, hook: string) =>
  w.findAllComponents(OnmsSelect).find(c => c.attributes('data-test') === hook)!

const lastCard = (w: ReturnType<typeof mountCard>) => {
  const e = w.emitted('update:card')!
  return e[e.length - 1][0] as ScheduleCard
}

// the begin and end pickers of the first time range: [showTime, timeOnly, hourFormat] each
const pickerModes = (w: ReturnType<typeof mountCard>) =>
  w.findAllComponents(OnmsDatePicker).map(p => [p.props('showTime'), p.props('timeOnly'), p.props('hourFormat')])

const stored = (type: string, day: string | null, begins: string, ends: string) =>
  toCard({ user: 'bob', type, times: [{ id: null, day, begins, ends }] }, SHORT_MONTHS)

describe('OnCallScheduleCard', () => {
  it('picks a specific range with two date-and-time pickers in 12-hour format', () => {
    const w = mountCard(stored('specific', null, '05-Mar-2026 08:00:00', '05-Mar-2026 17:00:00'))
    expect(pickerModes(w)).toEqual([[true, false, '12'], [true, false, '12']])
    expect((w.find('[data-test="time-begin-0"] input').element as HTMLInputElement).value).toContain('08:00 am')
  })

  it('renders two time inputs for a daily card', () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    expect(pickerModes(w)).toEqual([[false, true, '12'], [false, true, '12']])
    expect((w.find('[data-test="time-end-0"] input').element as HTMLInputElement).value).toBe('05:00 pm')
    expect(w.find('[data-test="time-day-0"]').exists()).toBe(false)
  })

  it('renders a weekday select and two time inputs for a weekly card', () => {
    const w = mountCard(stored('weekly', 'monday', '08:00:00', '17:00:00'))
    expect(w.find('[data-test="time-day-0"]').exists()).toBe(true)
    expect(pickerModes(w)).toEqual([[false, true, '12'], [false, true, '12']])
  })

  it('offers days 1 to 31 for a monthly card', () => {
    const w = mountCard(stored('monthly', '5', '08:00:00', '17:00:00'))
    const options = select(w, 'time-day-0').props('options') as unknown[]
    expect(options).toHaveLength(31)
    expect(pickerModes(w)).toEqual([[false, true, '12'], [false, true, '12']])
  })

  it('emits a card that serializes to the edited weekly time', async () => {
    const w = mountCard(stored('weekly', 'tuesday', '08:00:00', '17:00:00'))
    select(w, 'time-day-0').vm.$emit('update:modelValue', 'monday')
    await w.setProps({ card: lastCard(w) })
    await pickDate(w, 'time-end-0', new Date(2026, 0, 1, 17, 30))
    const card = lastCard(w)
    expect(card.dirty).toBe(true)
    expect(toSchedule(card, SHORT_MONTHS).times[0]).toEqual({ id: null, day: 'monday', begins: '08:00:00', ends: '17:30:00' })
  })

  it('serializes End of day as 23:59:59 and shows no end time to pick', async () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    await w.find('[data-test="time-end-of-day-0"] input').setValue(true)
    const card = lastCard(w)
    expect(toSchedule(card, SHORT_MONTHS).times[0].ends).toBe('23:59:59')
    await w.setProps({ card })
    expect(w.find('[data-test="time-end-0"]').text()).toBe('end of day')
    expect(w.findAllComponents(OnmsDatePicker)).toHaveLength(1)
  })

  it('shows the overnight hint when begin is not before end', () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    const c = { ...w.props('card'), times: [{ ...w.props('card').times[0], begin: '22:00', end: '06:00' }] }
    return w.setProps({ card: c }).then(() => {
      expect(w.find('[data-test="schedule-problems"]').text()).toContain('add two time ranges')
    })
  })

  it('keeps the end date editable for a specific time that runs to the end of the day', async () => {
    const w = mountCard(newCard('bob', 'specific'))
    await w.find('[data-test="add-time"]').trigger('click')
    await w.setProps({ card: lastCard(w) })
    await pickDate(w, 'time-begin-0', new Date(2026, 2, 5, 8, 0))
    await w.setProps({ card: lastCard(w) })
    await w.find('[data-test="time-end-of-day-0"] input').setValue(true)
    await w.setProps({ card: lastCard(w) })
    // the end-date picker picks a day only
    expect(pickerModes(w)[1]).toEqual([false, false, '24'])
    await pickDate(w, 'time-end-date-0', new Date(2026, 2, 6))
    const card = lastCard(w)
    expect(toSchedule(card, SHORT_MONTHS).times[0]).toEqual({ id: null, day: null, begins: '05-Mar-2026 08:00:00', ends: '06-Mar-2026 23:59:59' })
  })

  it('removes a schedule with a red trash-can icon button', () => {
    for (const card of [newCard('bob', 'daily'), stored('daily', null, '08:00:00', '17:00:00')]) {
      const remove = mountCard(card).findAllComponents(OnmsIconButton).find(b => b.attributes('data-test') === 'remove-schedule')!
      expect(remove.props('severity')).toBe('danger')
      expect(remove.props('icon')).toStrictEqual(Delete)
    }
  })

  it('marks a schedule added in this editor as pending, left of its remove button, and not a stored one', () => {
    const badge = mountCard(newCard('bob', 'daily')).find('[data-test="schedule-pending"]')
    expect(badge.exists()).toBe(true)
    expect(badge.element.nextElementSibling?.getAttribute('data-test')).toBe('remove-schedule')
    expect(mountCard(stored('daily', null, '08:00:00', '17:00:00')).find('[data-test="schedule-pending"]').exists()).toBe(false)
  })

  it('gives the remove button of a time range the tooltip Remove time range', () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    const remove = w.findAllComponents(OnmsIconButton).find(b => b.attributes('data-test') === 'remove-time-0')!
    expect(remove.props('tooltip')).toBe('Remove time range')
  })

  it('marks each time range added in this editor as pending, left of its remove button', async () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    expect(w.find('[data-test="time-pending-0"]').exists()).toBe(false)

    await w.find('[data-test="add-time"]').trigger('click')
    await w.setProps({ card: lastCard(w) })

    expect(w.find('[data-test="time-pending-0"]').exists()).toBe(false)
    const badge = w.find('[data-test="time-pending-1"]')
    expect(badge.exists()).toBe(true)
    expect(badge.element.nextElementSibling?.getAttribute('data-test')).toBe('remove-time-1')
  })

  it('shows no inputs on a legacy card and Remove works', async () => {
    const w = mountCard(toCard({ user: 'bob', type: 'fortnightly', times: [{ id: null, day: 'x', begins: 'whenever', ends: 'later' }] }, SHORT_MONTHS))
    expect(w.find('[data-test="oncall-schedule-legacy"]').exists()).toBe(true)
    expect(w.findAll('input')).toHaveLength(0)
    expect(w.text()).toContain('whenever')
    await w.find('[data-test="remove-schedule"]').trigger('click')
    expect(w.emitted('remove')).toHaveLength(1)
  })

  it('asks before changing the type of a card with times, then clears them', async () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    select(w, 'schedule-type').vm.$emit('update:modelValue', 'weekly')
    await w.vm.$nextTick()
    expect(w.emitted('update:card')).toBeUndefined()
    await w.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
    const card = lastCard(w)
    expect(card.type).toBe('weekly')
    expect(card.times).toEqual([])
    expect(card.dirty).toBe(true)
  })

  it('changes the type of an empty card without asking', () => {
    const w = mountCard(newCard('bob', 'daily'))
    select(w, 'schedule-type').vm.$emit('update:modelValue', 'monthly')
    expect(lastCard(w).type).toBe('monthly')
  })

  it('warns when the stored user is not a member and keeps them selected', () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'), ['carol'])
    expect(w.find('[data-test="not-in-group"]').text()).toContain('not in group')
    expect(select(w, 'schedule-user').props('modelValue')).toBe('bob')
  })

  it('adds and removes a time', async () => {
    const w = mountCard(newCard('bob', 'daily'))
    await w.find('[data-test="add-time"]').trigger('click')
    expect(lastCard(w).times).toHaveLength(1)
    await w.setProps({ card: lastCard(w) })
    await w.find('[data-test="remove-time-0"]').trigger('click')
    expect(lastCard(w).times).toHaveLength(0)
  })
})
