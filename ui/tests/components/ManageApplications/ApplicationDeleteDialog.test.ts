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

import ApplicationDeleteDialog from '@/components/ManageApplications/ApplicationDeleteDialog.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
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

const web = { id: 4, name: 'Web Store', serviceCount: 3, perspectiveLocations: ['Fulda', 'RDU'] }

const mountDialog = async (application: any = web) => {
  const wrapper = mount(ApplicationDeleteDialog, {
    props: { visible: true, application },
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { Dialog: DialogStub }
    }
  })
  const store = useApplicationAdminStore()
  vi.mocked(store.deleteApplication).mockResolvedValue({ success: true, message: '' })
  await flushPromises()
  return { wrapper, store }
}

describe('ApplicationDeleteDialog.vue', () => {
  beforeEach(() => showToast.mockClear())

  it('names what is deleted and what is kept', async () => {
    const { wrapper } = await mountDialog()
    expect(wrapper.find('[data-test="application-name"]').text()).toBe('Web Store')
    expect(wrapper.find('[data-test="service-count"]').text()).toBe('3')
    expect(wrapper.find('[data-test="perspective-locations"]').text()).toBe('Fulda, RDU')
    expect(wrapper.find('[data-test="delete-note"]').text()).toContain('Its services, their nodes and their outage history are kept.')
    expect(wrapper.find('[data-test="delete-note"]').text()).toContain('their open perspective outages are closed')
  })

  it('leaves out the perspective sentence for an application without perspective locations', async () => {
    const { wrapper } = await mountDialog({ ...web, perspectiveLocations: [] })
    expect(wrapper.find('[data-test="perspective-locations"]').text()).toBe('None')
    expect(wrapper.find('[data-test="delete-note"]').text()).not.toContain('perspective outages')
  })

  it('deletes, toasts and closes', async () => {
    const { wrapper, store } = await mountDialog()
    await wrapper.find('[data-test="delete-button"]').trigger('click')
    await flushPromises()
    expect(store.deleteApplication).toHaveBeenCalledWith(web)
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }))
  })

  it('stays open with the error when the delete fails', async () => {
    const { wrapper, store } = await mountDialog()
    vi.mocked(store.deleteApplication).mockResolvedValue({ success: false, message: 'Failed to delete application \'Web Store\'.' })
    await wrapper.find('[data-test="delete-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toBe('Failed to delete application \'Web Store\'.')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })
})
