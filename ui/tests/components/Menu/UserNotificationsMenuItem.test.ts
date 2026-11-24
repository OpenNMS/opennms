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

import UserNotificationsMenuItem from '@/components/Menu/UserNotificationsMenuItem.vue'
import { useMenuStore } from '@/stores/menuStore'
import { MainMenu, NotificationSummary } from '@/types/mainMenu'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

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
  noticeStatus: 'On',
  userNotificationMenu: {
    id: 'userNotificationMenu',
    name: 'Notifications',
    url: null,
    roles: null,
    items: [
      { id: 'userNotificationUser', name: 'User', url: 'notification/index.jsp', roles: null }
    ]
  }
} as unknown as MainMenu

const summary = (userCount: number, teamCount: number, severities: unknown[]): NotificationSummary => ({
  userUnacknowledgedCount: userCount,
  teamUnacknowledgedCount: teamCount,
  totalUnacknowledgedCount: userCount + teamCount,
  userUnacknowledgedNotifications: {
    notification: severities.map((severity, i) => ({ id: i, severity }))
  }
} as unknown as NotificationSummary)

describe('UserNotificationsMenuItem.vue', () => {
  let wrapper: VueWrapper<any>

  const mountWith = async (value: NotificationSummary) => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    const store = useMenuStore()
    store.mainMenu = mainMenu
    store.notificationSummary = value
    wrapper = mount(UserNotificationsMenuItem, {
      props: { expanded: false },
      global: {
        plugins: [pinia, PrimeVue],
        stubs: { OnmsPopover: OnmsPopoverStub }
      },
      attachTo: document.body
    })
    await nextTick()
  }

  const badgeClasses = () => {
    const pills = wrapper.findAll('.user-notification-badge-wrapper > .notification-badge-pill')
    return { user: pills[0]!.classes(), team: pills[1]!.classes() }
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('shows Normal when every notification is Normal', async () => {
    await mountWith(summary(2, 1, ['NORMAL', 'NORMAL']))
    expect(badgeClasses().user).toContain('badge-severity-normal')
  })

  it('shows the highest severity', async () => {
    await mountWith(summary(2, 1, ['MINOR', 'CRITICAL']))
    expect(badgeClasses().user).toContain('badge-severity-critical')
  })

  it('shows none when the severity is missing', async () => {
    await mountWith(summary(1, 1, [undefined]))
    expect(badgeClasses().user).toContain('badge-severity-none')
  })

  it('shows none when the severity is unrecognized', async () => {
    await mountWith(summary(1, 1, ['FOO']))
    expect(badgeClasses().user).toContain('badge-severity-none')
  })

  it('shows none when the user count is 0', async () => {
    await mountWith(summary(0, 1, []))
    expect(badgeClasses().user).toContain('badge-severity-none')
  })

  it('shows none when the team count is 0', async () => {
    await mountWith(summary(1, 0, ['MAJOR']))
    expect(badgeClasses().team).toContain('badge-severity-none')
  })

  it('keeps badge-info for a non-zero team count', async () => {
    await mountWith(summary(1, 3, ['MAJOR']))
    expect(badgeClasses().team).toContain('badge-info')
  })
})
