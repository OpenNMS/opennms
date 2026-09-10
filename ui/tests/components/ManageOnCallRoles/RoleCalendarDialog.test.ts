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
    getOnCallCalendar.mockResolvedValue({ role: 'NOC', year: 2026, month: 9, 'time-zone': 'UTC', day: [] })
    await open()

    expect(inDialog('[data-test="zone-hint"]')?.textContent).toContain('server time (UTC)')
    expect(inDialog('[data-test="dialog-error"]')).toBeNull()
  })

  it('refuses to add coverage when the calendar, and so the server zone, failed to load', async () => {
    getOnCallCalendar.mockResolvedValue(null)
    await open()

    expect(inDialog('[data-test="zone-hint"]')).toBeNull()
    expect(inDialog('[data-test="dialog-error"]')?.textContent).toContain('could not be loaded')
    expect((inDialog('[data-test="add-entry-button"]') as HTMLButtonElement).disabled).toBe(true)
  })
})
