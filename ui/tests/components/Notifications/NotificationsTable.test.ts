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

import NotificationsTable from '@/components/Notifications/NotificationsTable.vue'
import { useAuthStore } from '@/stores/authStore'
import { useMenuStore } from '@/stores/menuStore'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { NotificationQueryPreset, OnmsNotification } from '@/types/notifications'
import { createTestingPinia } from '@pinia/testing'
import { mount, RouterLinkStub } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

// useRole binds the first pinia this file creates (see NodeActionsDropdown.test.ts), so one shared
// pinia, with the store's state set per test.
const pinia = createTestingPinia({ createSpy: vi.fn })
useAuthStore(pinia).whoAmI = { id: 'admin', fullName: 'Admin', internal: true, roles: ['ROLE_ADMIN'] } as never
useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as never

const outstanding = { id: 1, subject: 'Node down', pageTime: 1_700_000_000_000 } as OnmsNotification
const acknowledged = { id: 2, subject: 'Node up', pageTime: 1_700_000_000_000, ackUser: 'operator', ackTime: 1_700_000_100_000 } as OnmsNotification

const mountTable = (preset: NotificationQueryPreset, notifications: OnmsNotification[] = [outstanding, acknowledged]) => {
  const store = useNotificationsStore(pinia)
  store.preset = preset
  store.notifications = notifications
  store.totalCount = notifications.length

  return mount(NotificationsTable, {
    global: {
      plugins: [PrimeVue, pinia],
      stubs: { RouterLink: RouterLinkStub }
    }
  })
}

const headers = (wrapper: ReturnType<typeof mountTable>) => wrapper.findAll('th').map(th => th.text())

describe('NotificationsTable.vue', () => {
  // The all preset mixes outstanding and acknowledged notifications on one page.
  describe('the all preset', () => {
    it('shows who responded and when, and the Acknowledge action', () => {
      expect(headers(mountTable('all'))).toEqual(expect.arrayContaining(['Responder', 'Respond Time', 'Actions']))
    })

    it('offers Acknowledge on outstanding rows only', () => {
      const wrapper = mountTable('all')

      expect(wrapper.find('[data-test="ack-button-1"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="ack-button-2"]').exists()).toBe(false)
    })

    it('says there are no notifications, of either kind, when the page is empty', () => {
      expect(mountTable('all', []).text()).toContain('No notifications found.')
    })
  })

  it('shows the responder but no actions for acknowledged notifications', () => {
    const wrapper = mountTable('allAcknowledged', [acknowledged])

    expect(headers(wrapper)).toEqual(expect.arrayContaining(['Responder', 'Respond Time']))
    expect(headers(wrapper)).not.toContain('Actions')
  })

  it('shows the actions but no responder for outstanding notifications', () => {
    const wrapper = mountTable('allOutstanding', [outstanding])

    expect(headers(wrapper)).toContain('Actions')
    expect(headers(wrapper)).not.toContain('Responder')
    expect(wrapper.find('[data-test="ack-button-1"]').exists()).toBe(true)
  })

  it.each([
    ['allOutstanding', 'No outstanding notifications found.'],
    ['allAcknowledged', 'No acknowledged notifications found.']
  ] as const)('words the empty %s page for what it shows', (preset, message) => {
    expect(mountTable(preset, []).text()).toContain(message)
  })
})
