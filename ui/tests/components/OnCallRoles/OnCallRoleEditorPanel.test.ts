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

import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { OnmsSelect } from '@opennms/onms-ui'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import OnCallCalendarTab from '@/components/OnCallRoles/OnCallCalendarTab.vue'
import OnCallRoleEditorPanel from '@/components/OnCallRoles/OnCallRoleEditorPanel.vue'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { adminWriteSuccess } from '@/types/adminWrite'
import { ManagedOnCallRole, STALE_ONCALL_ROLE_MESSAGE } from '@/types/onCallRoleAdmin'

const store = vi.hoisted(() => ({
  users: [] as unknown[],
  groups: [] as unknown[],
  scheduleSettings: null as unknown,
  createRole: vi.fn(),
  updateRole: vi.fn(),
  fullName: vi.fn((id: string) => id === 'alice' ? 'Alice Liddell' : undefined),
  groupMembers: vi.fn(() => [] as string[])
}))

vi.mock('@/stores/onCallRoleAdminStore', async () => {
  const { reactive } = await import('vue')
  const live = reactive(store)
  return { useOnCallRoleAdminStore: () => live }
})

const api = vi.hoisted(() => ({ previewOnCallCalendar: vi.fn() }))
vi.mock('@/services', () => ({ default: api }))

const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

enableAutoUnmount(afterEach)

const noop = { mounted: () => undefined }
const SHORT_MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

const storedRole = (): ManagedOnCallRole => ({
  name: 'Ops',
  description: 'Operations rota',
  membershipGroup: 'Night',
  supervisor: 'alice',
  schedules: [
    { user: 'bob', type: 'daily', times: [{ id: null, day: null, begins: '08:00:00', ends: '17:00:00' }] },
    { user: 'carol', type: 'fortnightly', times: [{ id: null, day: null, begins: 'whenever', ends: 'later' }] }
  ],
  onCallNow: ['bob'],
  onCallNowIsSupervisor: false,
  version: 4
})

const mountPanel = async (role: ManagedOnCallRole | null = null) => {
  const wrapper = mount(OnCallRoleEditorPanel, {
    props: { role },
    global: { plugins: [PrimeVue], directives: { 'onms-tooltip': noop, tooltip: noop }}
  })
  await flushPromises()
  return wrapper
}

// the PrimeVue select behind a data-test hook
const select = (wrapper: Awaited<ReturnType<typeof mountPanel>>, hook: string) =>
  wrapper.findAllComponents(OnmsSelect).find(c => c.attributes('data-test') === hook)!

const save = async (wrapper: Awaited<ReturnType<typeof mountPanel>>) => {
  await wrapper.find('[data-test="save-button"]').trigger('click')
  await flushPromises()
}

describe('OnCallRoleEditorPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.users = [{ userId: 'alice', fullName: 'Alice Liddell' }, { userId: 'bob', fullName: '' }]
    store.groups = [{ name: 'Night', users: ['bob'] }, { name: 'Day', users: [] }]
    store.scheduleSettings = { timeZone: 'UTC', shortMonths: SHORT_MONTHS }
    store.createRole.mockResolvedValue(adminWriteSuccess())
    store.updateRole.mockResolvedValue(adminWriteSuccess())
    api.previewOnCallCalendar.mockResolvedValue({ success: true, message: '', payload: { timeZone: 'UTC', month: '2026-11', entries: [] }})
  })

  it('creates a role with the four fields and no schedules, then closes', async () => {
    const wrapper = await mountPanel()
    expect(wrapper.text()).toContain('Add New On-Call Role')
    expect(wrapper.find('[data-test="oncall-now"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()

    await wrapper.find('[data-test="role-name-input"]').setValue('Weekend Rota')
    await wrapper.find('[data-test="role-description-input"]').setValue('Covers weekends')
    select(wrapper, 'supervisor-select').vm.$emit('update:modelValue', 'alice')
    select(wrapper, 'group-select').vm.$emit('update:modelValue', 'Day')
    await flushPromises()
    await save(wrapper)

    expect(store.createRole).toHaveBeenCalledWith({
      name: 'Weekend Rota',
      description: 'Covers weekends',
      supervisor: 'alice',
      membershipGroup: 'Day',
      schedules: []
    })
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('offers every user as id (Full Name) and every group', async () => {
    const wrapper = await mountPanel()
    expect(select(wrapper, 'supervisor-select').props('options')).toEqual([
      { value: 'alice', label: 'alice (Alice Liddell)' },
      { value: 'bob', label: 'bob' }
    ])
    expect(select(wrapper, 'group-select').props('options')).toEqual(['Night', 'Day'])
  })

  it('flags an invalid name and disables Save', async () => {
    const wrapper = await mountPanel()
    await wrapper.find('[data-test="role-name-input"]').setValue('bad/name')
    expect(wrapper.find('#oncall-role-name-error').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('sends an unchanged edit as a PUT with the stored schedules, legacy one included', async () => {
    const role = storedRole()
    const wrapper = await mountPanel(role)
    expect(wrapper.text()).toContain('Edit On-Call Role: Ops')
    expect(wrapper.find('[data-test="role-name-input"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="oncall-now"]').text()).toContain('bob')
    await save(wrapper)

    expect(store.updateRole).toHaveBeenCalledTimes(1)
    const body = store.updateRole.mock.calls[0][0]
    expect(body.schedules).toEqual(storedRole().schedules)
    expect(body).toEqual({
      name: 'Ops',
      description: 'Operations rota',
      membershipGroup: 'Night',
      supervisor: 'alice',
      schedules: storedRole().schedules,
      version: 4
    })
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('notes when the supervisor is on call because nobody is scheduled', async () => {
    const wrapper = await mountPanel({ ...storedRole(), onCallNow: ['alice'], onCallNowIsSupervisor: true })
    expect(wrapper.find('[data-test="oncall-now"]').text()).toContain('alice (supervisor, nobody scheduled)')
  })

  it('marks the Details tab and disables Save when the supervisor or group is missing', async () => {
    const wrapper = await mountPanel({ ...storedRole(), supervisor: '', membershipGroup: '' })
    expect(wrapper.find('[data-test="tab-details"] [data-test="tab-problem-marker"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('waits for the schedule settings before enabling Save', async () => {
    store.scheduleSettings = null
    const role = storedRole()
    role.schedules = [{ user: 'bob', type: 'specific', times: [{ id: null, day: null, begins: '05-Mar-2026 08:00:00', ends: '05-Mar-2026 17:00:00' }] }]
    const wrapper = await mountPanel(role)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()

    // the reactive store the panel sees, not the plain object behind it
    const live = useOnCallRoleAdminStore() as unknown as typeof store
    live.scheduleSettings = { timeZone: 'UTC', shortMonths: SHORT_MONTHS }
    await flushPromises()
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
    await save(wrapper)
    expect(store.updateRole.mock.calls[0][0].schedules).toEqual(role.schedules)
  })

  it('says the settings could not be loaded on the Schedules and Calendar tabs while they are missing', async () => {
    store.scheduleSettings = null
    const wrapper = await mountPanel(storedRole())
    await wrapper.find('[data-test="tab-schedules"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="settings-missing"]').text()).toBe('Schedule settings could not be loaded; reload the page.')
    expect(wrapper.text()).not.toContain('No schedules: the supervisor covers every time.')
    await wrapper.find('[data-test="tab-calendar"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="calendar-settings-missing"]').exists()).toBe(true)
    expect(wrapper.text()).not.toContain('Fix the problems on the Schedules tab')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('flags an edited specific time in a month the server locale abbreviates to other than 3 characters', async () => {
    store.scheduleSettings = { timeZone: 'UTC', shortMonths: ['Jan.', 'Feb.', 'März', ...SHORT_MONTHS.slice(3)] }
    const role = storedRole()
    role.schedules = [{ user: 'bob', type: 'specific', times: [{ id: null, day: null, begins: '05-Apr-2026 08:00:00', ends: '05-Apr-2026 17:00:00' }] }]
    const wrapper = await mountPanel(role)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
    await wrapper.find('[data-test="tab-schedules"]').trigger('click')
    await wrapper.find('[data-test="time-begin-0"]').setValue('2026-03-05T08:00')
    await flushPromises()
    expect(wrapper.text()).toContain('This server\'s locale writes March as \'März\'')
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('rejects a description over 4096 characters unless it is the untouched stored one', async () => {
    const long = 'x'.repeat(4100)
    const wrapper = await mountPanel({ ...storedRole(), description: long })
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeUndefined()
    await save(wrapper)
    expect(store.updateRole.mock.calls[0][0].description).toBe(long)

    await wrapper.find('[data-test="role-description-input"]').setValue('y'.repeat(4097))
    expect(wrapper.find('#oncall-role-description-error').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('shows the store message and closes on a stale result', async () => {
    store.updateRole.mockResolvedValue({ message: STALE_ONCALL_ROLE_MESSAGE, stale: true })
    const wrapper = await mountPanel(storedRole())
    await save(wrapper)
    expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: STALE_ONCALL_ROLE_MESSAGE }))
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows another failure in the panel and stays open', async () => {
    store.updateRole.mockResolvedValue({ message: 'Supervisor does not exist.', stale: false })
    const wrapper = await mountPanel(storedRole())
    await save(wrapper)
    expect(wrapper.find('[data-test="save-error"]').text()).toContain('Supervisor does not exist.')
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  it('marks the Schedules tab and disables Save while a card has a problem', async () => {
    const role = storedRole()
    role.schedules = [{ user: 'bob', type: 'daily', times: [{ id: null, day: null, begins: '08:00:00', ends: '17:00:00' }] }]
    const wrapper = await mountPanel(role)
    expect(wrapper.find('[data-test="tab-schedules"] [data-test="tab-problem-marker"]').exists()).toBe(false)
    await wrapper.find('[data-test="tab-schedules"]').trigger('click')
    await wrapper.find('[data-test="add-schedule"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="tab-schedules"] [data-test="tab-problem-marker"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="save-button"]').attributes('disabled')).toBeDefined()
  })

  it('sends an edited time while the untouched stored schedules stay byte-identical', async () => {
    const role = storedRole()
    role.schedules.push({ user: 'bob', type: 'weekly', times: [{ id: null, day: 'monday', begins: '08:00:00', ends: '17:00:00' }] })
    const wrapper = await mountPanel(role)
    await wrapper.find('[data-test="tab-schedules"]').trigger('click')
    await flushPromises()
    const cards = wrapper.findAll('[data-test="oncall-schedule-card"]')
    await cards[0].find('[data-test="time-end-0"]').setValue('17:30')
    await save(wrapper)

    const schedules = store.updateRole.mock.calls[0][0].schedules
    expect(schedules[0]).toEqual({ user: 'bob', type: 'daily', times: [{ id: null, day: null, begins: '08:00:00', ends: '17:30:00' }] })
    expect(schedules[1]).toEqual(storedRole().schedules[1])
    expect(schedules[2]).toEqual(role.schedules[2])
  })

  it('closes on Back and on Cancel without saving', async () => {
    const wrapper = await mountPanel(storedRole())
    await wrapper.find('[data-test="back-button"]').trigger('click')
    await wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(2)
    expect(store.updateRole).not.toHaveBeenCalled()
  })

  describe('calendar tab', () => {
    const specificRole = () => {
      const role = storedRole()
      role.schedules = [{ user: 'bob', type: 'specific', times: [{ id: null, day: null, begins: '05-Mar-2026 08:00:00', ends: '05-Mar-2026 17:00:00' }] }]
      return role
    }
    const openCalendar = async (wrapper: Awaited<ReturnType<typeof mountPanel>>) => {
      await wrapper.find('[data-test="tab-calendar"]').trigger('click')
      await flushPromises()
      return wrapper.findComponent(OnCallCalendarTab)
    }
    const entry = (user: string, start: string, end: string, endOfDay = false) => ({ user, start, end, endOfDay })

    it('keeps the shown month when leaving the tab and coming back', async () => {
      const wrapper = await mountPanel(specificRole())
      const calendar = await openCalendar(wrapper)
      const first = api.previewOnCallCalendar.mock.calls[0][1] as string
      await calendar.find('[data-test="calendar-next"]').trigger('click')
      await flushPromises()
      const shifted = api.previewOnCallCalendar.mock.calls.at(-1)![1] as string
      expect(shifted).not.toBe(first)
      await wrapper.find('[data-test="tab-schedules"]').trigger('click')
      await flushPromises()
      expect(wrapper.findComponent(OnCallCalendarTab).exists()).toBe(false)
      api.previewOnCallCalendar.mockClear()
      await openCalendar(wrapper)
      expect(api.previewOnCallCalendar.mock.calls[0][1]).toBe(shifted)
    })

    it('previews the draft, with the stored role name, once the tab is shown', async () => {
      const wrapper = await mountPanel(specificRole())
      expect(api.previewOnCallCalendar).not.toHaveBeenCalled()
      await openCalendar(wrapper)
      expect(api.previewOnCallCalendar).toHaveBeenCalledTimes(1)
      const [draft, month] = api.previewOnCallCalendar.mock.calls[0]
      expect(draft.name).toBe('Ops')
      expect(draft.schedules).toEqual(specificRole().schedules)
      expect(month).toMatch(/^\d{4}-\d{2}$/)
    })

    it('adds a time from the calendar to a new Specific card, not to the stored one, and reuses it', async () => {
      const wrapper = await mountPanel(specificRole())
      const calendar = await openCalendar(wrapper)
      calendar.vm.$emit('add-entry', entry('bob', '2026-11-05T00:00', '2026-11-05T23:59', true))
      await flushPromises()
      calendar.vm.$emit('add-entry', entry('bob', '2026-11-06T09:00', '2026-11-06T10:00'))
      await flushPromises()
      await save(wrapper)

      const schedules = store.updateRole.mock.calls[0][0].schedules
      expect(schedules).toHaveLength(2)
      expect(schedules[0]).toEqual(specificRole().schedules[0])
      expect(schedules[1]).toEqual({
        user: 'bob',
        type: 'specific',
        times: [
          { id: null, day: null, begins: '05-Nov-2026 00:00:00', ends: '05-Nov-2026 23:59:59' },
          { id: null, day: null, begins: '06-Nov-2026 09:00:00', ends: '06-Nov-2026 10:00:00' }
        ]
      })
    })

    it('keeps separate new cards for different users', async () => {
      const wrapper = await mountPanel(null)
      const calendar = await openCalendar(wrapper)
      calendar.vm.$emit('add-entry', entry('bob', '2026-11-05T00:00', '2026-11-05T23:59', true))
      calendar.vm.$emit('add-entry', entry('carol', '2026-11-05T00:00', '2026-11-05T23:59', true))
      await flushPromises()
      expect((wrapper.vm as unknown as { cards: unknown[] }).cards).toHaveLength(2)
    })

    it('switches to the Schedules tab and highlights the card when an entry is clicked', async () => {
      Element.prototype.scrollIntoView = vi.fn()
      try {
        const wrapper = await mountPanel(storedRole())
        const calendar = await openCalendar(wrapper)
        calendar.vm.$emit('show-schedule', 0)
        await flushPromises()
        expect(wrapper.find('[data-test="oncall-schedule-card"]').exists()).toBe(true)
        expect(wrapper.findAll('[data-test="oncall-schedule-card"]')[0].classes()).toContain('highlight')
      } finally {
        // @ts-expect-error restoring the missing DOM method
        delete Element.prototype.scrollIntoView
      }
    })

    it('asks for the Schedules tab fixes instead of previewing a draft that cannot be built', async () => {
      const wrapper = await mountPanel(storedRole())
      await wrapper.find('[data-test="tab-schedules"]').trigger('click')
      await wrapper.find('[data-test="add-schedule"]').trigger('click')
      await flushPromises()
      await openCalendar(wrapper)
      expect(api.previewOnCallCalendar).not.toHaveBeenCalled()
      expect(wrapper.find('[data-test="calendar-error"]').text()).toBe('Fix the problems on the Schedules tab to see the calendar.')
    })

    it('shows a server rejection of the draft in the calendar', async () => {
      api.previewOnCallCalendar.mockResolvedValue({ success: false, message: 'The supervisor must be a user.' })
      const wrapper = await mountPanel(storedRole())
      await openCalendar(wrapper)
      expect(wrapper.find('[data-test="calendar-error"]').text()).toBe('The supervisor must be a user.')
    })
  })
})
