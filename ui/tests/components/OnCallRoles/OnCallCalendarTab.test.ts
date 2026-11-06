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

import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { OnmsSelect } from '@opennms/onms-ui'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import OnCallCalendarTab from '@/components/OnCallRoles/OnCallCalendarTab.vue'
import { newCard } from '@/lib/onCallSchedule'
import { ManagedOnCallRoleCreate, OnCallCalendar } from '@/types/onCallRoleAdmin'

const api = vi.hoisted(() => ({ previewOnCallCalendar: vi.fn() }))
vi.mock('@/services', () => ({ default: api }))

// the dialog is teleported to the body
enableAutoUnmount(afterEach)

const draft = (): ManagedOnCallRoleCreate => ({
  name: 'Ops', description: '', membershipGroup: 'Night', supervisor: 'alice',
  schedules: [{ user: 'bob', type: 'daily', times: [{ id: null, day: null, begins: '08:00:00', ends: '17:00:00' }] }]
})

const calendar = (month = '2026-11'): OnCallCalendar => ({
  timeZone: 'UTC',
  month,
  entries: [
    { start: `${month}-03T08:00:00`, end: `${month}-03T17:00:00`, user: 'bob', scheduleIndex: 0 },
    { start: `${month}-03T17:00:00`, end: `${month}-03T23:59:59`, user: 'alice', scheduleIndex: null }
  ]
})


const ok = (payload: OnCallCalendar) => ({ success: true, message: '', payload })

const mountTab = async (props: Record<string, unknown> = {}) => {
  const wrapper = mount(OnCallCalendarTab, {
    props: { draft: draft(), cards: [newCard('bob', 'daily')], members: ['bob', 'carol'], timeZone: 'UTC', ...props },
    global: { plugins: [PrimeVue], directives: { 'onms-tooltip': { mounted: () => undefined }}}
  })
  await flushPromises()
  return wrapper
}

describe('OnCallCalendarTab', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout', 'Date'] })
    vi.setSystemTime(new Date('2026-11-04T12:00:00Z'))
    api.previewOnCallCalendar.mockImplementation(async (_d: unknown, month: string) => ok(calendar(month)))
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  it('starts at the month it is given and reports the shown month', async () => {
    const wrapper = await mountTab({ month: '2027-02' })
    expect(api.previewOnCallCalendar).toHaveBeenCalledWith(draft(), '2027-02')
    expect(wrapper.text()).toContain('February 2027')
    await wrapper.find('[data-test="calendar-next"]').trigger('click')
    expect(wrapper.emitted('update:month')!.at(-1)).toEqual(['2027-03'])
  })

  it('says the settings could not be loaded rather than asking to fix the schedules', async () => {
    const wrapper = await mountTab({ draft: null, settingsMissing: true })
    expect(wrapper.find('[data-test="calendar-settings-missing"]').text()).toBe('Schedule settings could not be loaded; reload the page.')
    expect(wrapper.text()).not.toContain('Fix the problems on the Schedules tab')
    expect(wrapper.find('[data-test="calendar-error"]').exists()).toBe(false)
  })

  it('shows the server month with the time zone and a grid that starts on the Sunday', async () => {
    const wrapper = await mountTab()
    expect(api.previewOnCallCalendar).toHaveBeenCalledWith(draft(), '2026-11')
    expect(wrapper.text()).toContain('Times are in the server time zone: UTC')
    expect(wrapper.text()).toContain('November 2026')
    const cells = wrapper.findAll('[data-test="calendar-cell"]')
    expect(cells.length % 7).toBe(0)
    // 1 Nov 2026 is a Sunday: the first cell is the 1st
    expect(cells[0].attributes('data-date')).toBe('2026-11-01')
    // October 2026 starts on a Thursday: the grid leads with four empty cells
    await wrapper.find('[data-test="calendar-prev"]').trigger('click')
    await flushPromises()
    const october = wrapper.findAll('[data-test="calendar-cell"]')
    expect(october.slice(0, 4).every(c => !c.attributes('data-date'))).toBe(true)
    expect(october[4].attributes('data-date')).toBe('2026-10-01')
  })

  it('uses the month of the server time zone, not of the browser', async () => {
    vi.setSystemTime(new Date('2026-11-30T23:30:00Z'))
    await mountTab({ timeZone: 'Pacific/Auckland' })
    expect(api.previewOnCallCalendar).toHaveBeenCalledWith(draft(), '2026-12')
  })

  it('does not call an entry whose schedule card is gone a supervisor entry', async () => {
    api.previewOnCallCalendar.mockResolvedValue(ok({
      timeZone: 'UTC',
      month: '2026-11',
      entries: [{ start: '2026-11-03T08:00:00', end: '2026-11-03T17:00:00', user: 'bob', scheduleIndex: 5 }]
    }))
    const wrapper = await mountTab()
    const orphan = wrapper.find('[data-date="2026-11-03"] [data-test="calendar-entry-fallback"]')
    expect(orphan.text()).toBe('8:00 am bob')
  })

  it('names the server time zone, and says nothing when it is not known', async () => {
    expect((await mountTab()).find('[data-test="calendar-zone"]').text()).toBe('Times are in the server time zone: UTC')
    expect((await mountTab({ timeZone: '' })).find('[data-test="calendar-zone"]').exists()).toBe(false)
  })

  it('lists entries under their day; the supervisor entry is muted and not a button', async () => {
    const wrapper = await mountTab()
    const cell = wrapper.find('[data-date="2026-11-03"]')
    const entries = cell.findAll('[data-test="calendar-entry"]')
    expect(entries).toHaveLength(1)
    expect(entries[0].text()).toBe('8:00 am bob')
    expect(entries[0].element.tagName).toBe('BUTTON')
    const fallback = cell.find('[data-test="calendar-entry-fallback"]')
    expect(fallback.text()).toContain('5:00 pm alice')
    expect(fallback.text()).toContain('supervisor')
    expect(fallback.element.tagName).not.toBe('BUTTON')
    expect(wrapper.find('[data-date="2026-11-04"]').findAll('[data-test^="calendar-entry"]')).toHaveLength(0)
  })

  it('asks for the neighbouring month at once on ‹ and ›', async () => {
    const wrapper = await mountTab()
    await wrapper.find('[data-test="calendar-next"]').trigger('click')
    expect(api.previewOnCallCalendar).toHaveBeenLastCalledWith(draft(), '2026-12')
    await flushPromises()
    expect(wrapper.text()).toContain('December 2026')
  })

  it('shows the server message instead of the grid when the preview fails', async () => {
    api.previewOnCallCalendar.mockResolvedValue({ success: false, message: 'The supervisor must be a user.' })
    const wrapper = await mountTab()
    expect(wrapper.find('[data-test="calendar-error"]').text()).toBe('The supervisor must be a user.')
    expect(wrapper.find('[data-test="calendar-cell"]').exists()).toBe(false)
  })

  it('does not call the preview without a draft and says what to fix', async () => {
    const wrapper = await mountTab({ draft: null })
    expect(api.previewOnCallCalendar).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="calendar-error"]').text()).toBe('Fix the problems on the Schedules tab to see the calendar.')
  })

  it('reloads 400 ms after the draft changes, once, and recovers from a missing draft', async () => {
    const wrapper = await mountTab()
    api.previewOnCallCalendar.mockClear()
    await wrapper.setProps({ draft: { ...draft(), supervisor: 'bob' }})
    await vi.advanceTimersByTimeAsync(200)
    await wrapper.setProps({ draft: { ...draft(), supervisor: 'carol' }})
    await vi.advanceTimersByTimeAsync(399)
    expect(api.previewOnCallCalendar).not.toHaveBeenCalled()
    await vi.advanceTimersByTimeAsync(1)
    expect(api.previewOnCallCalendar).toHaveBeenCalledTimes(1)
    expect(api.previewOnCallCalendar.mock.calls[0][0].supervisor).toBe('carol')

    await wrapper.setProps({ draft: null })
    await vi.advanceTimersByTimeAsync(500)
    expect(wrapper.find('[data-test="calendar-error"]').exists()).toBe(true)
    await wrapper.setProps({ draft: draft() })
    await vi.advanceTimersByTimeAsync(400)
    await flushPromises()
    expect(wrapper.find('[data-test="calendar-error"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="calendar-cell"]').exists()).toBe(true)
  })

  it('applies only the latest response', async () => {
    const slow: { resolve: (v: unknown) => void } = { resolve: () => undefined }
    api.previewOnCallCalendar.mockImplementationOnce(() => new Promise((resolve) => {
      slow.resolve = resolve
    }))
    const wrapper = mount(OnCallCalendarTab, {
      props: { draft: draft(), cards: [newCard('bob', 'daily')], members: [], timeZone: 'UTC' },
      global: { plugins: [PrimeVue], directives: { 'onms-tooltip': { mounted: () => undefined }}}
    })
    await wrapper.find('[data-test="calendar-next"]').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('December 2026')
    slow.resolve(ok(calendar('2026-11')))
    await flushPromises()
    expect(wrapper.find('[data-date="2026-12-03"] [data-test="calendar-entry"]').exists()).toBe(true)
    expect(wrapper.find('[data-date="2026-11-03"]').exists()).toBe(false)
  })

  it('emits show-schedule with the schedule index when an entry is clicked', async () => {
    const wrapper = await mountTab()
    await wrapper.find('[data-test="calendar-entry"]').trigger('click')
    expect(wrapper.emitted('show-schedule')).toEqual([[0]])
  })

  it('opens the day dialog on a day click and emits add-entry on OK', async () => {
    const wrapper = await mountTab()
    await wrapper.find('[data-date="2026-11-05"] [data-test="calendar-day"]').trigger('click')
    await flushPromises()
    const dialog = document.body.querySelector('[data-test="oncall-day-entry-dialog"]')
    expect(dialog).not.toBeNull()
    wrapper.findComponent({ name: 'OnCallDayEntryDialog' }).findComponent(OnmsSelect).vm.$emit('update:modelValue', 'carol')
    await flushPromises()
    ;(document.body.querySelector('[data-test="ok-button"]') as HTMLElement).click()
    await flushPromises()
    expect(wrapper.emitted('add-entry')).toEqual([[{ user: 'carol', start: '2026-11-05T00:00', end: '2026-11-05T23:59', endOfDay: true }]])
  })
})
