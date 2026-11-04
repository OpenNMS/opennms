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

  it('closes on Back and on Cancel without saving', async () => {
    const wrapper = await mountPanel(storedRole())
    await wrapper.find('[data-test="back-button"]').trigger('click')
    await wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(2)
    expect(store.updateRole).not.toHaveBeenCalled()
  })
})
