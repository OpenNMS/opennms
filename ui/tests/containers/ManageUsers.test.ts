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
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import ManageUsers from '@/containers/ManageUsers.vue'
import { AdminEditMode } from '@/types/adminWrite'

const UNAVAILABLE = 'User management is temporarily unavailable.'

const store = vi.hoisted(() => ({
  editMode: 'table',
  populate: vi.fn(),
  closeUserEditor: vi.fn(),
  users: []
}))
vi.mock('@/stores/userAdminStore', () => ({ useUserAdminStore: () => store }))
vi.mock('@/stores/menuStore', () => ({ useMenuStore: () => ({ mainMenu: { homeUrl: '/' }}) }))

// the page gives the feedback for its load (the store and service give none)
const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
const spinner = vi.hoisted(() => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }))
vi.mock('@/composables/useSpinner', () => ({ default: () => spinner }))

const mountPage = async () => {
  const wrapper = mount(ManageUsers, { global: { stubs: { BreadCrumbs: true, UsersTable: true, UserEditorPanel: true }}})
  await flushPromises()
  return wrapper
}

describe('ManageUsers.vue (container)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    store.editMode = AdminEditMode.Table
  })

  it('loads the page under the spinner and says nothing when the load succeeds', async () => {
    store.populate.mockResolvedValue({ success: true, message: '' })
    await mountPage()

    expect(store.populate).toHaveBeenCalledTimes(1)
    expect(spinner.startSpinner).toHaveBeenCalledTimes(1)
    expect(spinner.stopSpinner).toHaveBeenCalledTimes(1)
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('hides the table while the editor is open, so the table keeps its filter and page', async () => {
    store.populate.mockResolvedValue({ success: true, message: '' })
    store.editMode = AdminEditMode.Edit
    const wrapper = await mountPage()

    expect(wrapper.findComponent({ name: 'UsersTable' }).exists()).toBe(true)
    expect(wrapper.find('[data-test="users-table-wrapper"]').attributes('style')).toContain('display: none')
    expect(wrapper.findComponent({ name: 'UserEditorPanel' }).exists()).toBe(true)
  })

  it('really hides the real table, which has several root nodes, without a Vue warning', async () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined)
    store.populate.mockResolvedValue({ success: true, message: '' })
    store.editMode = AdminEditMode.Edit
    const wrapper = mount(ManageUsers, {
      attachTo: document.body,
      global: { plugins: [PrimeVue], directives: { 'onms-tooltip': {}}, stubs: { BreadCrumbs: true, UserEditorPanel: true }}
    })
    try {
      await flushPromises()

      expect(wrapper.find('[data-test="users-table-wrapper"]').isVisible()).toBe(false)
      expect(warn.mock.calls.filter(c => String(c[0]).includes('non-element root node'))).toEqual([])
    } finally {
      wrapper.unmount()
      warn.mockRestore()
    }
  })

  it('shows only the table outside the editor', async () => {
    store.populate.mockResolvedValue({ success: true, message: '' })
    const wrapper = await mountPage()

    expect(wrapper.find('[data-test="users-table-wrapper"]').attributes('style') ?? '').not.toContain('display: none')
    expect(wrapper.findComponent({ name: 'UserEditorPanel' }).exists()).toBe(false)
  })

  it('shows a failed load with the server text', async () => {
    store.populate.mockResolvedValue({ success: false, message: UNAVAILABLE })
    await mountPage()

    expect(showSnackBar.mock.calls).toEqual([[{ msg: UNAVAILABLE, error: true }]])
  })
})
