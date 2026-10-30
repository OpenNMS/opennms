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
import { beforeEach, describe, expect, it, vi } from 'vitest'

import ManageGroups from '@/containers/ManageGroups.vue'
import { AdminEditMode } from '@/types/adminWrite'

const UNAVAILABLE = 'User management through this API needs org.opennms.usermgmt.store=db.'

const store = vi.hoisted(() => ({
  editMode: 'table',
  populate: vi.fn(),
  closeGroupEditor: vi.fn()
}))
vi.mock('@/stores/groupAdminStore', () => ({ useGroupAdminStore: () => store }))
vi.mock('@/stores/menuStore', () => ({ useMenuStore: () => ({ mainMenu: { homeUrl: '/' }}) }))

// the page gives the feedback for its load (the store and service give none)
const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
const spinner = vi.hoisted(() => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }))
vi.mock('@/composables/useSpinner', () => ({ default: () => spinner }))

const mountPage = async () => {
  const wrapper = mount(ManageGroups, { global: { stubs: { BreadCrumbs: true, GroupsTable: true, GroupEditorPanel: true }}})
  await flushPromises()
  return wrapper
}

describe('ManageGroups.vue (container)', () => {
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

    expect(wrapper.findComponent({ name: 'GroupsTable' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'GroupsTable' }).attributes('style')).toContain('display: none')
    expect(wrapper.findComponent({ name: 'GroupEditorPanel' }).exists()).toBe(true)
  })

  it('shows only the table outside the editor', async () => {
    store.populate.mockResolvedValue({ success: true, message: '' })
    const wrapper = await mountPage()

    expect(wrapper.findComponent({ name: 'GroupsTable' }).attributes('style') ?? '').not.toContain('display: none')
    expect(wrapper.findComponent({ name: 'GroupEditorPanel' }).exists()).toBe(false)
  })

  it('shows a failed load with the server text', async () => {
    store.populate.mockResolvedValue({ success: false, message: UNAVAILABLE })
    await mountPage()

    expect(showSnackBar.mock.calls).toEqual([[{ msg: UNAVAILABLE, error: true }]])
  })
})
