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

import UserSelfServiceMenuItem from '@/components/Menu/UserSelfServiceMenuItem.vue'
import { performLogout } from '@/services/logoutService'
import { useMenuStore } from '@/stores/menuStore'
import { MainMenu } from '@/types/mainMenu'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

vi.mock('@/services/logoutService', () => ({
  performLogout: vi.fn()
}))

// Render the popover content inline so the dropdown items are always in the DOM.
const OnmsPopoverStub = {
  template: '<div><slot /></div>',
  methods: {
    show: () => {},
    hide: () => {}
  }
}

const mainMenu = {
  baseHref: 'http://localhost:8980/opennms/',
  username: 'admin',
  selfServiceMenu: {
    id: 'selfService',
    name: 'admin',
    url: null,
    locationMatch: null,
    roles: null,
    items: [
      {
        id: 'changePassword',
        name: 'Change Password',
        url: 'account/selfService/newPasswordEntry',
        locationMatch: null,
        roles: null
      },
      {
        id: 'logout',
        name: 'Log Out',
        url: 'j_spring_security_logout',
        action: 'logout',
        locationMatch: null,
        roles: null
      }
    ]
  }
} as unknown as MainMenu

describe('UserSelfServiceMenuItem.vue', () => {
  let wrapper: VueWrapper<any>

  beforeEach(async () => {
    wrapper = mount(UserSelfServiceMenuItem, {
      props: {
        expanded: false
      },
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
        stubs: { OnmsPopover: OnmsPopoverStub }
      },
      attachTo: document.body
    })
    useMenuStore().mainMenu = mainMenu
    await nextTick()
    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  it('renders the logout item', () => {
    expect(wrapper.find('a[name="self-service-logout"]').exists()).toBe(true)
  })

  it('performs logout AND cancels the anchor navigation on click', async () => {
    const logoutLink = wrapper.get('a[name="self-service-logout"]')

    // A real, cancelable click: on embedded JSP pages the anchor's '#' href
    // resolves against the document <base href> (bootstrap.jsp), so an
    // uncancelled click starts a full-page navigation that races the logout
    // POST — Firefox tears the request down before it is sent (NMS-20174).
    const event = new MouseEvent('click', { bubbles: true, cancelable: true })
    logoutLink.element.dispatchEvent(event)
    await nextTick()

    expect(performLogout).toHaveBeenCalledTimes(1)
    expect(event.defaultPrevented).toBe(true)
  })

  it('does not cancel navigation for regular items', async () => {
    const changePassword = wrapper.get('a[name="self-service-changePassword"]')

    const event = new MouseEvent('click', { bubbles: true, cancelable: true })
    changePassword.element.dispatchEvent(event)
    await nextTick()

    expect(performLogout).not.toHaveBeenCalled()
    expect(event.defaultPrevented).toBe(false)
  })
})
