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

import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import { SCV_GET_ALL_ALIAS } from '@/lib/constants'
import { useScvStore } from '@/stores/scvStore'
import { SCVCredentials } from '@/types/scv'
import SCV from '@/containers/SecureCredentialsVault.vue'
import { nextTick } from 'vue'

vi.mock('@/services', () => ({
  default: {
    getAliases: vi.fn().mockResolvedValue([]),
    getAllCredentials: vi.fn().mockResolvedValue([]),
    getCredentialsByAlias: vi.fn().mockResolvedValue(null),
    addCredentials: vi.fn().mockResolvedValue(true),
    updateCredentials: vi.fn().mockResolvedValue(true)
  }
}))

const mockCredentials: SCVCredentials = {
  alias: 'alias',
  username: 'name',
  password: '******',
  attributes: {}
}

describe('scv test', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(SCV, {
      global: {
        plugins: [
          createTestingPinia({ stubActions: false }),
          PrimeVue
        ],
        stubs: ['router-link']
      }
    })
  })

  test('adding an alias should enable the add btn', async () => {
    const addCredsBtn = wrapper.get('[data-test="add-creds-btn"]')
    const aliasInput = wrapper.get('[data-test="alias-input"] input')

    // expect add btn to start disabled
    expect((addCredsBtn.element as HTMLButtonElement).disabled).toBe(true)

    // adding a value to alias should enable the add btn
    await aliasInput.setValue('some alias')
    await nextTick()
    expect((addCredsBtn.element as HTMLButtonElement).disabled).toBe(false)
  })

  test('the user may not add a duplicate alias', async () => {
    const scvStore = useScvStore()

    const addCredsBtn = wrapper.get('[data-test="add-creds-btn"]')
    const aliasInput = wrapper.get('[data-test="alias-input"] input')

    // add alias1 to the list of current aliases
    scvStore.aliases = ['alias1']
    // start to create new with alias1
    await aliasInput.setValue('alias1')
    // expect add btn to remain disabled
    expect((addCredsBtn.element as HTMLButtonElement).disabled).toBe(true)
    // replace with alias2
    await aliasInput.setValue('alias2')
    // expect add btn to be enabled
    expect((addCredsBtn.element as HTMLButtonElement).disabled).toBe(false)
  })

  test('the user may not add a reserved alias', async () => {
    const scvStore = useScvStore()

    const addCredsBtn = wrapper.get('[data-test="add-creds-btn"]')
    const aliasInput = wrapper.get('[data-test="alias-input"] input')

    // add alias1 to the list of current aliases
    scvStore.aliases = ['alias1']
    // start to create new with SCV_GET_ALL_ALIAS
    await aliasInput.setValue(SCV_GET_ALL_ALIAS)
    // expect add btn to remain disabled
    expect((addCredsBtn.element as HTMLButtonElement).disabled).toBe(true)
    // replace with alias2
    await aliasInput.setValue('alias2')
    // expect add btn to be enabled
    expect((addCredsBtn.element as HTMLButtonElement).disabled).toBe(false)
  })

  test('the update btn should appear and be enabled', async () => {
    const scvStore = useScvStore()

    const updateCreds = wrapper.find('[data-test="update-creds-btn"]')

    // the update btn should not be available
    expect(updateCreds.exists()).toBeFalsy()
    // simulate clicking on an alias to update
    scvStore.credentials = mockCredentials
    scvStore.isEditing = true
    await nextTick()

    // the update btn should be there, and enabled
    const updateCredsBtn = wrapper.find('[data-test="update-creds-btn"]')
    expect((updateCredsBtn.element as HTMLButtonElement).disabled).toBe(false)
  })

  // NOTE: skipping this test, need to fix
  test.skip('if password is masked and username is being updated, prevent submission', async () => {
    const scvStore = useScvStore()

    const usernameInput = wrapper.get('[data-test="username-input"] input')
    const passwordInput = wrapper.get('[data-test="password-input"] input')

    // simulate clicking on an alias to update
    scvStore.credentials = mockCredentials
    scvStore.isEditing = true
    await nextTick()

    // the update btn should be there, and be enabled
    const updateCredsBtn = wrapper.get('[data-test="update-creds-btn"]')
    expect((updateCredsBtn.element as HTMLButtonElement).disabled).toBe(false)

    // modify the username
    await usernameInput.setValue('newusername')

    // the update btn should be disabled, because the password is masked
    expect((updateCredsBtn.element as HTMLButtonElement).disabled).toBe(true)

    // modify the password
    await passwordInput.setValue('newpassword')

    // the update btn should be enabled
    expect((updateCredsBtn.element as HTMLButtonElement).disabled).toBe(false)
  })

  test('the clear btn', async () => {
    const scvStore = useScvStore()

    // happy-dom reports an empty input's `.value` as undefined; normalize to ''.
    const inputValue = (testId: string) =>
      (wrapper.get(`[data-test="${testId}"] input`).element as HTMLInputElement).value ?? ''

    const clearBtn = wrapper.get('[data-test="clear-btn"]')
    await clearBtn.trigger('click')

    // simulate clicking on an alias to update
    scvStore.credentials = mockCredentials
    scvStore.isEditing = true
    await nextTick()

    // expect form to be populated
    expect(inputValue('username-input')).toBe('name')
    expect(inputValue('password-input')).toBe('******')
    expect(inputValue('alias-input')).toBe('alias')

    // clear
    await clearBtn.trigger('click')
    await nextTick()
    expect(inputValue('username-input')).toBe('')
    expect(inputValue('password-input')).toBe('')
    expect(inputValue('alias-input')).toBe('')
  })

  test('the add and remove attribute btn', async () => {
    const addAttrBtn = wrapper.get('[data-test="add-attr-btn"]')
    let attrKeyInput = wrapper.find('[data-test="attr-key"]')
    let attrValueInput = wrapper.find('[data-test="attr-value"]')

    // form starts off without attribute inputs
    expect(attrKeyInput.exists()).toBeFalsy()
    expect(attrValueInput.exists()).toBeFalsy()

    // click add attributes btn
    await addAttrBtn.trigger('click')

    // try to find attr inputs, expect they render correctly
    attrKeyInput = wrapper.find('[data-test="attr-key"]')
    attrValueInput = wrapper.find('[data-test="attr-value"]')
    expect(attrKeyInput.exists()).toBeTruthy()
    expect(attrValueInput.exists()).toBeTruthy()

    // click remove attributes btn
    const rmAttrBtn = wrapper.get('[data-test="rm-attr-btn"]')
    await rmAttrBtn.trigger('click')

    // try to find attr inputs, expect they no longer render
    attrKeyInput = wrapper.find('[data-test="attr-key"]')
    attrValueInput = wrapper.find('[data-test="attr-value"]')
    expect(attrKeyInput.exists()).toBeFalsy()
    expect(attrValueInput.exists()).toBeFalsy()
  })
})
