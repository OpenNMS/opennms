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
import { OnmsSelect } from '@opennms/onms-ui'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

import OnCallScheduleCard from '@/components/OnCallRoles/OnCallScheduleCard.vue'
import { newCard, ScheduleCard, toCard, toSchedule } from '@/lib/onCallSchedule'

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

const stored = (type: string, day: string | null, begins: string, ends: string) =>
  toCard({ user: 'bob', type, times: [{ id: null, day, begins, ends }] }, SHORT_MONTHS)

describe('OnCallScheduleCard', () => {
  it('renders two datetime-local inputs for a specific card', () => {
    const w = mountCard(stored('specific', null, '05-Mar-2026 08:00:00', '05-Mar-2026 17:00:00'))
    expect(w.findAll('input[type="datetime-local"]')).toHaveLength(2)
  })

  it('renders two time inputs for a daily card', () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    expect(w.findAll('input[type="time"]')).toHaveLength(2)
    expect(w.find('[data-test="time-day-0"]').exists()).toBe(false)
  })

  it('renders a weekday select and two time inputs for a weekly card', () => {
    const w = mountCard(stored('weekly', 'monday', '08:00:00', '17:00:00'))
    expect(w.find('[data-test="time-day-0"]').exists()).toBe(true)
    expect(w.findAll('input[type="time"]')).toHaveLength(2)
  })

  it('offers days 1 to 31 for a monthly card', () => {
    const w = mountCard(stored('monthly', '5', '08:00:00', '17:00:00'))
    const options = select(w, 'time-day-0').props('options') as unknown[]
    expect(options).toHaveLength(31)
    expect(w.findAll('input[type="time"]')).toHaveLength(2)
  })

  it('emits a card that serializes to the edited weekly time', async () => {
    const w = mountCard(stored('weekly', 'tuesday', '08:00:00', '17:00:00'))
    select(w, 'time-day-0').vm.$emit('update:modelValue', 'monday')
    await w.setProps({ card: lastCard(w) })
    await w.find('[data-test="time-end-0"]').setValue('17:30')
    const card = lastCard(w)
    expect(card.dirty).toBe(true)
    expect(toSchedule(card, SHORT_MONTHS).times[0]).toEqual({ id: null, day: 'monday', begins: '08:00:00', ends: '17:30:00' })
  })

  it('serializes End of day as 23:59:59 and disables the end input', async () => {
    const w = mountCard(stored('daily', null, '08:00:00', '17:00:00'))
    await w.find('[data-test="time-end-of-day-0"] input').setValue(true)
    const card = lastCard(w)
    expect(toSchedule(card, SHORT_MONTHS).times[0].ends).toBe('23:59:59')
    await w.setProps({ card })
    expect(w.find('[data-test="time-end-0"]').attributes('disabled')).toBeDefined()
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
    await w.find('[data-test="time-begin-0"]').setValue('2026-03-05T08:00')
    await w.setProps({ card: lastCard(w) })
    await w.find('[data-test="time-end-of-day-0"] input').setValue(true)
    await w.setProps({ card: lastCard(w) })
    await w.find('[data-test="time-end-date-0"]').setValue('2026-03-06')
    const card = lastCard(w)
    expect(toSchedule(card, SHORT_MONTHS).times[0]).toEqual({ id: null, day: null, begins: '05-Mar-2026 08:00:00', ends: '06-Mar-2026 23:59:59' })
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
