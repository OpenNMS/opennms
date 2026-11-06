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
import { enableAutoUnmount, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import OnCallRoleRenameDialog from '@/components/OnCallRoles/OnCallRoleRenameDialog.vue'
import { adminWriteSuccess } from '@/types/adminWrite'
import { STALE_ONCALL_ROLE_MESSAGE } from '@/types/onCallRoleAdmin'

const store = vi.hoisted(() => ({ renameRole: vi.fn() }))
vi.mock('@/stores/onCallRoleAdminStore', () => ({ useOnCallRoleAdminStore: () => store }))

const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
const spinner = vi.hoisted(() => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }))
vi.mock('@/composables/useSpinner', () => ({ default: () => spinner }))

// the dialog is teleported to the body
enableAutoUnmount(afterEach)

type Vm = { newName: string, save: () => Promise<void> }

describe('OnCallRoleRenameDialog', () => {
  const mountDialog = () => mount(OnCallRoleRenameDialog, { props: { visible: true, roleName: 'Ops', version: 4 }, global: { plugins: [PrimeVue] }})
  const find = (selector: string) => document.body.querySelector(selector) as HTMLElement
  const fill = async (wrapper: ReturnType<typeof mountDialog>, value: string) => {
    ;(wrapper.vm as unknown as Vm).newName = value
    await nextTick()
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it.each([
    ['a/b', 'The on-call role name must start with a letter or digit'],
    ['Ops  Rota', 'single spaces between words'],
    ['calendar', 'The on-call role name "calendar" is reserved.'],
    ['a'.repeat(257), 'must not be longer than 256 characters']
  ])('flags "%s" and disables Rename', async (value, message) => {
    const wrapper = mountDialog()
    await fill(wrapper, value)
    expect(document.body.textContent).toContain(message)
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(true)
  })

  it('does not offer Rename before a name is typed, or for the current name', async () => {
    const wrapper = mountDialog()
    await nextTick()
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(true)
    await fill(wrapper, 'Ops')
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(true)
  })

  it('accepts another valid name', async () => {
    const wrapper = mountDialog()
    await fill(wrapper, 'Müller (Nacht)')
    expect(find('[data-test="save-button"]').hasAttribute('disabled')).toBe(false)
  })

  it('renames with the name and version, closes and says so', async () => {
    store.renameRole.mockResolvedValue(adminWriteSuccess())
    const wrapper = mountDialog()
    await fill(wrapper, 'Dev')
    await (wrapper.vm as unknown as Vm).save()

    expect(store.renameRole).toHaveBeenCalledWith('Ops', 'Dev', 4)
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'On-call role \'Ops\' renamed to \'Dev\'.' }]])
  })

  it('closes on a stale rename and shows the stale text once', async () => {
    store.renameRole.mockResolvedValue({ success: false, message: STALE_ONCALL_ROLE_MESSAGE, stale: true })
    const wrapper = mountDialog()
    await fill(wrapper, 'Dev')
    await (wrapper.vm as unknown as Vm).save()

    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(showSnackBar.mock.calls).toEqual([[{ msg: STALE_ONCALL_ROLE_MESSAGE, error: true }]])
  })

  it('stays open and shows another error in the dialog only', async () => {
    store.renameRole.mockResolvedValue({ success: false, message: 'Something else went wrong', stale: false })
    const wrapper = mountDialog()
    await fill(wrapper, 'Dev')
    await (wrapper.vm as unknown as Vm).save()
    await nextTick()

    expect(wrapper.emitted('update:visible')).toBeUndefined()
    expect(find('[data-test="dialog-error"]').textContent).toBe('Something else went wrong')
    expect(showSnackBar).not.toHaveBeenCalled()
  })
})
