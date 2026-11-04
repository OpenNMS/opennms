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
import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import OnCallRolesAbout from '@/components/OnCallRoles/OnCallRolesAbout.vue'
import OnCallRolesTable from '@/components/OnCallRoles/OnCallRolesTable.vue'
import { adminWriteSuccess } from '@/types/adminWrite'
import { STALE_ONCALL_ROLE_MESSAGE } from '@/types/onCallRoleAdmin'

const store = vi.hoisted(() => ({
  openRoleEditor: vi.fn(),
  deleteRole: vi.fn(),
  fullName: vi.fn((id: string) => id === 'alice' ? 'Alice Liddell' : undefined),
  roles: [] as unknown[]
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

const roles = [
  { name: 'Ops', description: 'Operations rota', membershipGroup: 'Night', supervisor: 'alice', schedules: [], onCallNow: ['bob', 'Carol'], onCallNowIsSupervisor: false, version: 1 },
  { name: 'Network', description: 'Network <b>team</b>', membershipGroup: 'Day', supervisor: 'dave', schedules: [], onCallNow: ['dave'], onCallNowIsSupervisor: true, version: 2 },
  { name: 'Idle', description: '', membershipGroup: 'Day', supervisor: 'erin', schedules: [], version: 3 }
]

const mountTable = () => mount(OnCallRolesTable, {
  global: { plugins: [PrimeVue], directives: { 'onms-tooltip': noop, tooltip: noop }}
})

const names = (wrapper: ReturnType<typeof mountTable>) => wrapper.findAll('.role-name').map(e => e.text())
const rowOf = (wrapper: ReturnType<typeof mountTable>, name: string) => wrapper.findAll('tbody tr').find(r => r.text().includes(name))!

describe('OnCallRolesTable', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.roles = roles
  })

  it('renders a row per role with its columns', () => {
    const wrapper = mountTable()
    expect(names(wrapper)).toEqual(['Ops', 'Network', 'Idle'])
    const ops = rowOf(wrapper, 'Ops')
    expect(ops.text()).toContain('alice')
    expect(ops.text()).toContain('bob, Carol')
    expect(ops.text()).toContain('Night')
    expect(ops.text()).toContain('Operations rota')
    expect(ops.find('[data-test="edit-oncall-role-button"]').exists()).toBe(true)
    expect(ops.find('[data-test="delete-oncall-role-button"]').exists()).toBe(true)
  })

  it('shows the fallback tag only when the supervisor is on call as the fallback', () => {
    const wrapper = mountTable()
    expect(rowOf(wrapper, 'Network').find('.fallback-tag').text()).toBe('fallback')
    expect(rowOf(wrapper, 'Ops').find('.fallback-tag').exists()).toBe(false)
    expect(rowOf(wrapper, 'Idle').find('.fallback-tag').exists()).toBe(false)
  })

  it('renders markup in a description as text', () => {
    const wrapper = mountTable()
    expect(rowOf(wrapper, 'Network').text()).toContain('Network <b>team</b>')
    expect(wrapper.find('tbody b').exists()).toBe(false)
  })

  it('opens the editor for Edit and for Add', async () => {
    const wrapper = mountTable()
    await rowOf(wrapper, 'Idle').find('[data-test="edit-oncall-role-button"]').trigger('click')
    expect(store.openRoleEditor).toHaveBeenLastCalledWith(roles[2])
    await wrapper.find('[data-test="add-oncall-role-button"]').trigger('click')
    expect(store.openRoleEditor).toHaveBeenLastCalledWith(null)
  })

  describe('filter', () => {
    const type = async (wrapper: ReturnType<typeof mountTable>, value: string) => {
      await wrapper.find('[data-test="oncall-roles-filter"]').setValue(value)
      await nextTick()
    }

    it.each([
      ['name, ignoring case', 'NETW', ['Network']],
      ['supervisor', 'ERIN', ['Idle']],
      ['membership group', 'night', ['Ops']],
      ['description', 'operations', ['Ops']]
    ])('matches the %s', async (_what, term, expected) => {
      const wrapper = mountTable()
      await type(wrapper, term)
      expect(names(wrapper)).toEqual(expected)
    })

    it('says that nothing matches', async () => {
      const wrapper = mountTable()
      await type(wrapper, 'nobody-has-this')
      expect(wrapper.find('[data-test="no-match"]').text()).toBe('No on-call roles match the filter.')
    })
  })

  describe('delete', () => {
    const askDelete = async (wrapper: ReturnType<typeof mountTable>) => {
      const vm = wrapper.vm as unknown as { askDelete: (r: unknown) => void, confirmDelete: () => Promise<void>, showDeleteConfirmation: boolean }
      vm.askDelete(roles[0])
      await nextTick()
      return vm
    }

    it('asks first and only then deletes with the name and version', async () => {
      store.deleteRole.mockResolvedValue(adminWriteSuccess())
      const wrapper = mountTable()
      const vm = await askDelete(wrapper)
      expect(vm.showDeleteConfirmation).toBe(true)
      expect(store.deleteRole).not.toHaveBeenCalled()

      await vm.confirmDelete()
      expect(store.deleteRole).toHaveBeenCalledWith('Ops', 1)
      expect(showSnackBar.mock.calls).toEqual([[{ msg: 'On-call role \'Ops\' deleted.' }]])
      expect(vm.showDeleteConfirmation).toBe(false)
    })

    it('shows a conflict message and closes the confirmation', async () => {
      store.deleteRole.mockResolvedValue({ success: false, message: STALE_ONCALL_ROLE_MESSAGE, stale: true })
      const vm = await askDelete(mountTable())
      await vm.confirmDelete()
      expect(showSnackBar.mock.calls).toEqual([[{ msg: STALE_ONCALL_ROLE_MESSAGE, error: true }]])
      expect(vm.showDeleteConfirmation).toBe(false)
    })
  })
})

describe('OnCallRolesAbout', () => {
  it('explains the group, the supervisor and the schedules', () => {
    const t = mount(OnCallRolesAbout).text().toLowerCase()
    for (const word of ['membership group', 'supervisor', 'schedule']) {
      expect(t).toContain(word)
    }
  })
})
