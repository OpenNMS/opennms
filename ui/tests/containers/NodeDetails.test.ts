import NodeDetails from '@/containers/NodeDetails.vue'
import { useNodeStore } from '@/stores/nodeStore'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    params: { id: '42' }
  })),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}))

describe('NodeDetails.vue', () => {
  const mountComponent = (id = '42') => {
    const pinia = createTestingPinia({ stubActions: false })
    setActivePinia(pinia)

    const nodeStore = useNodeStore()
    nodeStore.getNodeById = vi.fn().mockResolvedValue(undefined)

    const menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/home' } as any

    const wrapper = mount(NodeDetails, {
      props: { id },
      global: {
        plugins: [PrimeVue, pinia],
        stubs: {
          BreadCrumbs: true,
          NodeAvailabilityGraph: true,
          InterfacesTabs: true,
          EventsTable: true,
          OutagesTable: true
        }
      }
    })

    return { wrapper, nodeStore }
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders the four child components', async () => {
    const { wrapper } = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeAvailabilityGraph' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'InterfacesTabs' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'EventsTable' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'OutagesTable' }).exists()).toBe(true)
  })

  it('calls nodeStore.getNodeById with the id prop on mount', async () => {
    const { nodeStore } = mountComponent('42')
    await flushPromises()

    expect(nodeStore.getNodeById).toHaveBeenCalledWith({ id: '42' })
  })

  it('calls nodeStore.getNodeById again when id prop changes', async () => {
    const { wrapper, nodeStore } = mountComponent('42')
    await flushPromises()

    await wrapper.setProps({ id: '99' })
    await flushPromises()

    expect(nodeStore.getNodeById).toHaveBeenCalledTimes(2)
    expect(nodeStore.getNodeById).toHaveBeenLastCalledWith({ id: '99' })
  })

  it('does not render the deprecated stub message', async () => {
    const { wrapper } = mountComponent()
    await flushPromises()

    expect(wrapper.text()).not.toContain('deprecated')
    expect(wrapper.text()).not.toContain('Temp node details page')
  })
})
