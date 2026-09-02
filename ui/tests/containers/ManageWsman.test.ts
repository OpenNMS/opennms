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

import ManageWsman from '@/containers/ManageWsman.vue'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

const SETTINGS = {
  retry: 1, timeout: 30000, username: 'root', hasPassword: true, port: null, maxElements: null,
  ssl: true, strictSsl: false, path: '/wsman', productVendor: null, productVersion: null, gssAuth: null
}

const mountPage = async (state: Record<string, unknown>) => {
  const wrapper = mount(ManageWsman, {
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true, initialState: { wsmanAdminStore: state }})],
      stubs: { WsmanHelpPanel: true, WsmanDefaultsCard: true, WsmanDefinitionsTable: true, BreadCrumbs: true }
    }
  })
  await flushPromises()
  return wrapper
}

describe('ManageWsman.vue (container)', () => {
  it('loads the configuration on mount and renders the page title', async () => {
    const wrapper = await mountPage({ config: { defaults: SETTINGS, definitions: [] }, loadError: false })
    const store = useWsmanAdminStore()
    expect(store.getConfig).toHaveBeenCalled()
    expect(wrapper.find('.page-title').text()).toBe('Manage WS-Man')
    expect(wrapper.find('[data-test="tab-definitions"]').text()).toContain('Definitions (0)')
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
  })

  it('shows an error instead of an empty configuration when the read fails', async () => {
    const wrapper = await mountPage({ config: null, loadError: true })
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="tab-defaults"]').exists()).toBe(false)
  })
})
