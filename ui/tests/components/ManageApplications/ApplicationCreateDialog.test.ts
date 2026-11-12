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

import ApplicationCreateDialog from '@/components/ManageApplications/ApplicationCreateDialog.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

const mountDialog = async () => {
  const wrapper = mount(ApplicationCreateDialog, {
    props: { visible: false },
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { Dialog: DialogStub }
    }
  })
  const store = useApplicationAdminStore()
  store.applications = [{ id: 1, name: 'Billing', serviceCount: 0, perspectiveLocations: [] }]
  vi.mocked(store.createApplication).mockResolvedValue({ success: true, message: '', payload: 9 })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store }
}

const input = (wrapper: VueWrapper<any>) => wrapper.find('[data-test="application-name-input"]')
const saveDisabled = (wrapper: VueWrapper<any>) => wrapper.find('[data-test="save-button"]').attributes('disabled') !== undefined
const fieldError = (wrapper: VueWrapper<any>) => wrapper.find('.field-error')

describe('ApplicationCreateDialog.vue', () => {
  beforeEach(() => showToast.mockClear())

  it('needs a name before Add is enabled, and offers inline help', async () => {
    const { wrapper } = await mountDialog()
    expect(saveDisabled(wrapper)).toBe(true)
    await input(wrapper).setValue('   ')
    expect(saveDisabled(wrapper)).toBe(true)
    await input(wrapper).setValue('Web Store')
    expect(saveDisabled(wrapper)).toBe(false)
    expect(wrapper.find('[data-test="help-badge"]').exists()).toBe(true)
    expect(input(wrapper).attributes('maxlength')).toBe('32')
  })

  it('flags a name that is too long or already used', async () => {
    const { wrapper } = await mountDialog()
    await input(wrapper).setValue('x'.repeat(33))
    expect(fieldError(wrapper).text()).toBe('The name cannot be longer than 32 characters.')
    expect(saveDisabled(wrapper)).toBe(true)
    await input(wrapper).setValue(' Billing ')
    expect(fieldError(wrapper).text()).toBe('An application named \'Billing\' already exists.')
    expect(saveDisabled(wrapper)).toBe(true)
  })

  it('creates the trimmed name, closes, and hands the name and id on for the editor', async () => {
    const { wrapper, store } = await mountDialog()
    await input(wrapper).setValue('  Web Store ')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(store.createApplication).toHaveBeenCalledWith('Web Store')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(wrapper.emitted('created')?.at(-1)).toEqual(['Web Store', 9])
    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }))
  })

  it('keeps the dialog open and shows the server message when the create is rejected', async () => {
    const { wrapper, store } = await mountDialog()
    vi.mocked(store.createApplication).mockResolvedValue({ success: false, message: 'An application named Web already exists.' })
    await input(wrapper).setValue('Web')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toBe('An application named Web already exists.')
    expect(wrapper.emitted('created')).toBeUndefined()
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })

  it('submits on Enter once a valid name is typed, and not before', async () => {
    const { wrapper, store } = await mountDialog()
    await input(wrapper).trigger('keydown', { key: 'Enter' })
    expect(store.createApplication).not.toHaveBeenCalled()
    await input(wrapper).setValue('Web Store')
    await input(wrapper).trigger('keydown', { key: 'Enter' })
    await flushPromises()
    expect(store.createApplication).toHaveBeenCalledTimes(1)
  })

  it('starts empty each time it opens', async () => {
    const { wrapper } = await mountDialog()
    await input(wrapper).setValue('Web')
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true })
    expect((input(wrapper).element as HTMLInputElement).value).toBe('')
  })
})
