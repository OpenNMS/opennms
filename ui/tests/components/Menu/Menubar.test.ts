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

import Menubar from '@/components/Menu/Menubar.vue'
import { useMenuStore } from '@/stores/menuStore'
import { MainMenu } from '@/types/mainMenu'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'
import { nextTick } from 'vue'

// The dropdowns are the only things under test here; stub each down to a button
// that re-emits the show event Menubar listens for, and expose `expanded` so the
// tracked state is observable.
const makeMenuItemStub = () => ({
  props: ['expanded'],
  emits: ['menuShow', 'menuHide'],
  template: '<button @click="$emit(\'menuShow\')">menu</button>'
})

const NotificationsStub = makeMenuItemStub()
const SelfServiceStub = makeMenuItemStub()

const mountMenubar = async () => {
  const wrapper = mount(Menubar, {
    attachTo: document.body,
    global: {
      plugins: [PrimeVue, createTestingPinia({ stubActions: false })],
      directives: { 'onms-tooltip': {}},
      stubs: {
        UserNotificationsMenuItem: NotificationsStub,
        UserSelfServiceMenuItem: SelfServiceStub
      }
    }
  })

  // the dropdowns only render for a signed-in user
  useMenuStore().mainMenu = { baseHref: 'http://localhost:8980/opennms/', username: 'admin' } as MainMenu
  await nextTick()

  return wrapper
}

describe('Menubar dropdown state', () => {
  let wrapper: VueWrapper

  afterEach(() => wrapper?.unmount())

  const notifications = () => wrapper.findComponent(NotificationsStub)
  const selfService = () => wrapper.findComponent(SelfServiceStub)

  it('tracks which dropdown is expanded', async () => {
    wrapper = await mountMenubar()
    expect(notifications().props('expanded')).toBe(false)
    expect(selfService().props('expanded')).toBe(false)

    await notifications().trigger('click')
    expect(notifications().props('expanded')).toBe(true)
    expect(selfService().props('expanded')).toBe(false)
  })

  // Regression: useOutsideClick was called with `outsideClick.value` (undefined
  // at setup) and its returned `active` ref was never set, so this never fired.
  it('collapses the expanded dropdown on a click outside the menubar', async () => {
    wrapper = await mountMenubar()
    await notifications().trigger('click')
    expect(notifications().props('expanded')).toBe(true)

    const outside = document.createElement('div')
    document.body.appendChild(outside)
    outside.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await nextTick()

    expect(notifications().props('expanded')).toBe(false)
    outside.remove()
  })

  it('leaves the expanded dropdown alone when the click is inside the menubar', async () => {
    wrapper = await mountMenubar()
    await notifications().trigger('click')

    wrapper.find('header').element.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await nextTick()

    expect(notifications().props('expanded')).toBe(true)
  })
})
