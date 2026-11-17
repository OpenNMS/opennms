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

import NotificationsPanel from '@/components/Dashboard/panels/NotificationsPanel.vue'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

vi.mock('@/services/notificationService', async importOriginal => ({
  ...await importOriginal<object>(),
  getNotificationSummary: vi.fn().mockResolvedValue({ totalCount: 5, totalUnacknowledgedCount: 3, userUnacknowledgedCount: 1, teamUnacknowledgedCount: 2 })
}))

describe('Dashboard NotificationsPanel', () => {
  it('links your and everyone\'s outstanding notifications to the Notifications page', async () => {
    const wrapper = mount(NotificationsPanel, {
      props: { panelId: 'p1', options: {}, filter: {}, timeframe: '24h', refreshTick: 0 } as never,
      global: { plugins: [createTestingPinia()], stubs: { RouterLink: RouterLinkStub }}
    })
    await flushPromises()

    expect(wrapper.findAllComponents(RouterLinkStub).map(l => l.props('to'))).toEqual([
      { name: 'Notifications', query: { preset: 'yourOutstanding' }},
      { name: 'Notifications', query: { preset: 'allOutstanding' }}
    ])
  })
})
