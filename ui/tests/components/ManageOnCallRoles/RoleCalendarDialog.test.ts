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

import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const getOnCallCalendar = vi.fn()
const getOnCallRole = vi.fn()
vi.mock('@/services', () => ({ default: { getOnCallCalendar: (...args: unknown[]) => getOnCallCalendar(...args), getOnCallRole: (...args: unknown[]) => getOnCallRole(...args) }}))

import RoleCalendarDialog from '@/components/ManageOnCallRoles/RoleCalendarDialog.vue'

// Coverage entries are stored on the server's wall clock, and the zone comes
// from the calendar response. Without it the dialog must not accept an entry.

const mounted: { unmount: () => void }[] = []

const role = { name: 'NOC', 'membership-group': 'Admin', supervisor: 'admin', schedule: [] }

const open = async () => {
  const wrapper = mount(RoleCalendarDialog, {
    props: { visible: false, roleName: 'NOC' },
    global: { plugins: [PrimeVue] },
    attachTo: document.body
  })
  mounted.push(wrapper)
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return wrapper
}

const inDialog = (selector: string) => document.querySelector(`.p-dialog ${selector}`)
const addButton = () => inDialog('[data-test="add-entry-button"]') as HTMLButtonElement

const september = {
  role: 'NOC', year: 2026, month: 9, 'time-zone': 'UTC',
  day: [{ date: '2026-09-01', entry: [{ start: 1788606000000, end: 1788634800000, user: ['alice'], supervisor: false }] }]
}

describe('RoleCalendarDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    getOnCallRole.mockResolvedValue(role)
  })

  afterEach(() => {
    while (mounted.length) {
      mounted.pop()!.unmount()
    }
    document.body.innerHTML = ''
  })

  it('shows the server zone once the calendar loads', async () => {
    getOnCallCalendar.mockResolvedValue(september)
    await open()

    expect(inDialog('[data-test="zone-hint"]')?.textContent).toContain('server time (UTC)')
    expect(inDialog('[data-test="calendar-load-error"]')).toBeNull()
    expect(document.querySelectorAll('.p-dialog .entry').length).toBe(1)
  })

  it('refuses to add coverage when the calendar, and so the server zone, failed to load', async () => {
    getOnCallCalendar.mockResolvedValue(null)
    await open()

    expect(inDialog('[data-test="zone-hint"]')).toBeNull()
    expect(inDialog('[data-test="calendar-load-error"]')?.textContent).toContain('could not be loaded')
    expect(addButton().disabled).toBe(true)
  })

  it('drops the previous month and disables adding when a month change fails to load', async () => {
    getOnCallCalendar.mockResolvedValueOnce(september).mockResolvedValueOnce(null)
    await open()
    expect(document.querySelectorAll('.p-dialog .entry').length).toBe(1)

    ;(inDialog('[data-test="next-month-button"]') as HTMLButtonElement).click()
    await flushPromises()

    expect(document.querySelectorAll('.p-dialog .entry').length).toBe(0)
    expect(inDialog('[data-test="zone-hint"]')).toBeNull()
    expect(inDialog('[data-test="calendar-load-error"]')?.textContent).toContain('could not be loaded')
    expect(addButton().disabled).toBe(true)
  })

  it('clears the load error once a later month loads', async () => {
    getOnCallCalendar.mockResolvedValueOnce(null).mockResolvedValueOnce(september)
    await open()
    expect(inDialog('[data-test="calendar-load-error"]')).not.toBeNull()

    ;(inDialog('[data-test="next-month-button"]') as HTMLButtonElement).click()
    await flushPromises()

    expect(inDialog('[data-test="calendar-load-error"]')).toBeNull()
    expect(inDialog('[data-test="zone-hint"]')?.textContent).toContain('server time (UTC)')
  })
})
