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
