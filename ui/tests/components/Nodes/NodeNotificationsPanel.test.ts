import NodeNotificationsPanel from '@/components/Nodes/NodeNotificationsPanel.vue'
import { mount, RouterLinkStub } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

const push = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

const mountPanel = () => mount(NodeNotificationsPanel, {
  props: { node: { id: '1' } as never },
  global: { plugins: [PrimeVue], stubs: { RouterLink: RouterLinkStub }}
})

describe('NodeNotificationsPanel.vue', () => {
  it.each([
    ['your-outstanding-notifications-link', 'Your outstanding notifications for this node', 'yourOutstanding'],
    ['your-acknowledged-notifications-link', 'Your acknowledged notifications for this node', 'yourAcknowledged'],
    ['all-outstanding-notifications-link', 'All outstanding notifications for this node', 'allOutstanding'],
    ['all-acknowledged-notifications-link', 'All acknowledged notifications for this node', 'allAcknowledged']
  ])('%s opens the Notifications page for this node', (dataTest, label, preset) => {
    const link = mountPanel().findAllComponents(RouterLinkStub).find(l => l.attributes('data-test') === dataTest)!
    expect(link.text()).toBe(label)
    expect(link.props('to')).toEqual({ name: 'Notifications', query: { preset, node: '1' }})
  })

  it('opens the Notifications page from the header button', async () => {
    await mountPanel().find('[data-test="notifications-link-button"]').trigger('click')
    expect(push).toHaveBeenCalledWith({ name: 'Notifications', query: { preset: 'yourOutstanding' }})
  })
})
