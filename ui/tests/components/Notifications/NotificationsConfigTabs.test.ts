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

import NotificationsConfigTabs from '@/components/Notifications/NotificationsConfigTabs.vue'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { OnmsTabs } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'
import { useRoute } from 'vue-router'

vi.mock('vue-router', () => ({ useRoute: vi.fn() }))

const mountTabs = async (query: Record<string, string>) => {
  vi.mocked(useRoute).mockReturnValue({ query } as never)
  const pinia = createTestingPinia({ createSpy: vi.fn })
  const store = useNotificationConfigStore(pinia)
  const ok = vi.fn().mockResolvedValue({ success: true, message: '' })
  store.getEventNotifications = ok
  store.getDestinationPaths = ok
  store.getPathOutages = ok

  const wrapper = mount(NotificationsConfigTabs, {
    global: {
      plugins: [pinia, PrimeVue],
      stubs: { EventNotificationsTab: true, DestinationPathsTab: true, PathOutagesTab: true }
    }
  })
  await flushPromises()

  return { wrapper, store }
}

// Only the opening tab is covered here: which one a link can ask for.
describe('NotificationsConfigTabs.vue opening tab', () => {
  it('opens on Event Notifications by default', async () => {
    const { wrapper } = await mountTabs({})

    expect(wrapper.findComponent(OnmsTabs).props('value')).toBe('event-notifications')
  })

  // The Node Details critical path panel links here.
  it('opens on the tab a link asks for with ?tab=', async () => {
    const { wrapper, store } = await mountTabs({ tab: 'path-outages' })

    expect(wrapper.findComponent(OnmsTabs).props('value')).toBe('path-outages')
    expect(store.getPathOutages).toHaveBeenCalled()
  })

  it('ignores a ?tab= that is not one of its tabs', async () => {
    const { wrapper } = await mountTabs({ tab: 'nope' })

    expect(wrapper.findComponent(OnmsTabs).props('value')).toBe('event-notifications')
  })
})
