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
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import SCVForm from '@/components/SCV/SCVForm.vue'
import SCV from '@/containers/SecureCredentialsVault.vue'
import { useScvStore } from '@/stores/scvStore'

const { showSnackBarMock } = vi.hoisted(() => ({ showSnackBarMock: vi.fn() }))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

const ok = { success: true, message: '' }
const fail = (message: string) => ({ success: false, message })

const { api } = vi.hoisted(() => ({
  api: {
    getAliases: vi.fn(),
    getAllCredentials: vi.fn(),
    getCredentialsByAlias: vi.fn(),
    addCredentials: vi.fn(),
    updateCredentials: vi.fn()
  }
}))

vi.mock('@/services', () => ({ default: api }))

const mountWith = (component: any) => mount(component, {
  global: {
    plugins: [createTestingPinia({ stubActions: false }), PrimeVue],
    stubs: ['router-link']
  }
})

describe('SCV feedback', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.getAliases.mockResolvedValue({ ...ok, payload: [] })
    api.getAllCredentials.mockResolvedValue({ ...ok, payload: [] })
  })

  it('shows a success toast when credentials are added', async () => {
    api.addCredentials.mockResolvedValue(ok)
    const wrapper = mountWith(SCVForm)
    useScvStore().setValue({ alias: 'new-alias' })
    await flushPromises()

    await wrapper.get('[data-test="add-creds-btn"]').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Credentials added.' })
  })

  it('shows the failure as an error toast, not a success one, when add fails', async () => {
    api.addCredentials.mockResolvedValue(fail('Failed to add credentials.'))
    const wrapper = mountWith(SCVForm)
    useScvStore().setValue({ alias: 'new-alias' })
    await flushPromises()

    await wrapper.get('[data-test="add-creds-btn"]').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to add credentials.', error: true })
  })

  it('shows the update messages', async () => {
    const wrapper = mountWith(SCVForm)
    const store = useScvStore()
    store.setValue({ alias: 'a1' })
    store.isEditing = true
    await flushPromises()

    api.updateCredentials.mockResolvedValueOnce(ok)
    await wrapper.get('[data-test="update-creds-btn"]').trigger('click')
    await flushPromises()
    expect(showSnackBarMock).toHaveBeenLastCalledWith({ msg: 'Credentials updated.' })

    store.setValue({ alias: 'a1' })
    store.isEditing = true
    await flushPromises()
    api.updateCredentials.mockResolvedValueOnce(fail('Failed to update credentials.'))
    await wrapper.get('[data-test="update-creds-btn"]').trigger('click')
    await flushPromises()
    expect(showSnackBarMock).toHaveBeenLastCalledWith({ msg: 'Failed to update credentials.', error: true })
  })

  it('reports load failures as errors when the page opens', async () => {
    api.getAliases.mockResolvedValue(fail('Failed to return aliases.'))
    api.getAllCredentials.mockResolvedValue(fail('Failed to retrieve credentials.'))

    mountWith(SCV)
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to return aliases.', error: true })
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to retrieve credentials.', error: true })
  })

  it('stays quiet when the page loads fine', async () => {
    mountWith(SCV)
    await flushPromises()

    expect(showSnackBarMock).not.toHaveBeenCalled()
  })
})
