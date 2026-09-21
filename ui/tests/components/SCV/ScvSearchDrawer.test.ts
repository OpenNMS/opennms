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
import { nextTick } from 'vue'
import { useScvStore } from '@/stores/scvStore'
import ScvSearchDrawer from '@/components/SCV/ScvSearchDrawer.vue'

vi.mock('@/services', () => ({
  default: {
    getAliases: vi.fn().mockResolvedValue([]),
    getAllCredentials: vi.fn().mockResolvedValue([
      { alias: 'router-creds', username: 'admin', password: '******', attributes: {}},
      { alias: 'switch-creds', username: 'admin', password: '******', attributes: {}}
    ]),
    getCredentialsByAlias: vi.fn().mockResolvedValue(null),
    addCredentials: vi.fn().mockResolvedValue(true),
    updateCredentials: vi.fn().mockResolvedValue(true)
  }
}))

const mountDrawer = (isOpen: boolean) =>
  mount(ScvSearchDrawer, {
    props: { isOpen },
    global: {
      plugins: [
        createTestingPinia({ stubActions: false }),
        PrimeVue
      ]
    }
  })

describe('ScvSearchDrawer', () => {
  beforeEach(() => {
    document.body.innerHTML = ''
  })

  test('shows all credentials when opened, without requiring the user to type', async () => {
    const wrapper = mountDrawer(false)
    const scvStore = useScvStore()

    // simulate the container loading credentials from the REST API
    await scvStore.populate()

    const querySpy = vi.spyOn(scvStore, 'queryCredentials')

    // open the drawer
    await wrapper.setProps({ isOpen: true })
    await nextTick()
    await nextTick()

    // opening should trigger a query (empty term => show all)
    expect(querySpy).toHaveBeenCalled()

    // and the loaded aliases should be visible (PDrawer teleports to body)
    expect(document.body.textContent).toContain('router-creds')
    expect(document.body.textContent).toContain('switch-creds')
  })
})
