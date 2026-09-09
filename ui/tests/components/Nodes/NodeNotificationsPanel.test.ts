// ui/tests/components/Nodes/NodeNotificationsPanel.test.ts
import NodeNotificationsPanel from '@/components/Nodes/NodeNotificationsPanel.vue'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { useAuthStore } from '@/stores/authStore'
import PrimeVue from 'primevue/config'

const mountPanel = (username = 'admin', node: any = { id: '1' }, roles: string[] = ['ROLE_ADMIN']) => {
  const wrapper = mount(NodeNotificationsPanel, {
    props: { node, baseHref: '/opennms/' },
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue]
    }
  })

  const menuStore = useMenuStore()
  menuStore.mainMenu = { baseHref: '/opennms/', username } as any

  const authStore = useAuthStore()
  authStore.whoAmI = { id: 'admin', fullName: 'Administrator', internal: true, roles }

  return { wrapper, menuStore, authStore }
}

describe('NodeNotificationsPanel.vue', () => {
  it('links to the current user outstanding notifications for this node', async () => {
    const { wrapper } = mountPanel()
    await wrapper.vm.$nextTick()

    const link = wrapper.find('[data-test="outstanding-notifications-link"]')
    expect(link.text()).toBe('Your outstanding notifications for this node')
    expect(link.attributes('href'))
      .toBe('/opennms/notification/browse?acktype=unack&filter=node%3D1&filter=user%3Dadmin')
  })

  it('links to the current user acknowledged notifications for this node', async () => {
    const { wrapper } = mountPanel()
    await wrapper.vm.$nextTick()

    const link = wrapper.find('[data-test="acknowledged-notifications-link"]')
    expect(link.text()).toBe('Your acknowledged notifications for this node')
    expect(link.attributes('href'))
      .toBe('/opennms/notification/browse?acktype=ack&filter=node%3D1&filter=user%3Dadmin')
  })

  it('url-encodes a username containing reserved characters', async () => {
    const { wrapper } = mountPanel('some user&x')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-test="outstanding-notifications-link"]').attributes('href'))
      .toBe('/opennms/notification/browse?acktype=unack&filter=node%3D1&filter=user%3Dsome%20user%26x')
  })

  it('renders no notification links until the username is known', async () => {
    const { wrapper } = mountPanel('')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-test="outstanding-notifications-link"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="acknowledged-notifications-link"]').exists()).toBe(false)
  })

  it('navigates to the legacy notifications index page', async () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)

    const { wrapper } = mountPanel()
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="notifications-link-button"]').trigger('click')

    expect(assign).toHaveBeenCalledWith('/opennms/notification/index.jsp')
    vi.unstubAllGlobals()
  })

  // The panel mounts before menuStore.getMainMenu() resolves, so the links have to appear on
  // their own once mainMenu lands -- i.e. `username`/`links` must stay reactive reads of the
  // store, not values snapshotted during setup.
  it('shows the links once the main menu populates the username', async () => {
    const { wrapper, menuStore } = mountPanel('')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-test="outstanding-notifications-link"]').exists()).toBe(false)

    menuStore.mainMenu = { baseHref: '/opennms/', username: 'admin' } as any
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-test="outstanding-notifications-link"]').attributes('href'))
      .toBe('/opennms/notification/browse?acktype=unack&filter=node%3D1&filter=user%3Dadmin')
  })

  // The legacy card header link was open to any logged-in user, not just admins.
  it('navigates to the notifications index for a user without the admin role', async () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)

    const { wrapper } = mountPanel('admin', { id: '1' }, ['ROLE_USER'])
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="notifications-link-button"]').trigger('click')

    expect(assign).toHaveBeenCalledWith('/opennms/notification/index.jsp')
    vi.unstubAllGlobals()
  })
})
