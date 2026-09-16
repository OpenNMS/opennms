import NodeDetails from '@/containers/NodeDetails.vue'
import { useNodeStore } from '@/stores/nodeStore'
import { useMenuStore } from '@/stores/menuStore'
import { useEventStore } from '@/stores/eventStore'
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
  const mountComponent = (id = '42', loaded = false) => {
    const pinia = createTestingPinia({ stubActions: false })
    setActivePinia(pinia)

    const nodeStore = useNodeStore()
    nodeStore.getNodeById = vi.fn().mockResolvedValue(undefined)
    nodeStore.getNodeSnmpPrimaryInterface = vi.fn().mockResolvedValue(undefined)

    if (loaded) {
      nodeStore.node = { id: '42', label: 'srv-42' } as any
      nodeStore.nodeLoaded = true
    }

    const menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/home', baseHref: '/base' } as any

    const wrapper = mount(NodeDetails, {
      props: { id },
      global: {
        plugins: [PrimeVue, pinia],
        stubs: {
          BreadCrumbs: true,
          NodeAvailabilityGraph: true,
          NodeCategoriesPanel: true,
          NodeActionsDropdown: {
            name: 'NodeActionsDropdown',
            template: '<div></div>',
            props: ['baseHref', 'node', 'snmpPrimaryIpAddress', 'triggerNodeInfo']
          },
          NodeDetailsHeader: true,
          NodeNotificationsPanel: true,
          NodeDetailsDialog: true,
          EventsTable: true,
          OutagesTable: true,
          InterfacesTabs: true
        }
      }
    })

    return { wrapper, nodeStore }
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  // node starts as {} in the store -- truthy, and undefined for every field -- so panels that
  // read it must wait for a real one rather than render "Label: undefined" and
  // element/rescan.jsp?node=undefined.
  it('renders no node panels until the node has loaded', async () => {
    const { wrapper } = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeDetailsHeader' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeActionsDropdown' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeCategoriesPanel' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeNotificationsPanel' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeAvailabilityGraph' }).exists()).toBe(false)
  })

  // A 404 for a nonexistent node id leaves nothing to name in the heading.
  it('titles the page N/A when the node could not be fetched', async () => {
    const { wrapper } = mountComponent()
    useNodeStore().nodeLoadFailed = true
    await flushPromises()

    expect(wrapper.find('h2').text()).toBe('Node Details for N/A')
  })

  it('leaves the title unnamed while the node is still being fetched', async () => {
    const { wrapper } = mountComponent()
    await flushPromises()

    expect(wrapper.find('h2').text()).toBe('Node Details for')
  })

  // The Update SNMP action needs the SNMP-primary address, which the node payload does not
  // carry and the IP Interfaces table only holds a page of, so the page asks for it directly.
  it('asks for the node SNMP-primary address and hands it to the actions menu', async () => {
    const { wrapper, nodeStore } = mountComponent('42', true)
    await flushPromises()

    expect(nodeStore.getNodeSnmpPrimaryInterface).toHaveBeenCalledWith('42')

    nodeStore.snmpPrimaryIpAddress = '10.0.0.44'
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeActionsDropdown' }).props('snmpPrimaryIpAddress'))
      .toBe('10.0.0.44')
  })

  // The events table fetches by node id on its own and only replaces its rows on a successful
  // response, so a node that 404s would otherwise keep the previous node's events on screen.
  it('clears the events when the node fails to load', async () => {
    const { wrapper, nodeStore } = mountComponent()
    nodeStore.getNodeById = vi.fn().mockImplementation(async () => {
      nodeStore.nodeLoadFailed = true
    })
    const eventStore = useEventStore()
    eventStore.clearEvents = vi.fn()

    await wrapper.vm.$nextTick()
    await (wrapper.vm as any).fetchNode()

    expect(eventStore.clearEvents).toHaveBeenCalled()
  })

  it('leaves the events alone when the node loads', async () => {
    const { wrapper, nodeStore } = mountComponent('42', true)
    nodeStore.getNodeById = vi.fn().mockResolvedValue(undefined)
    const eventStore = useEventStore()
    eventStore.clearEvents = vi.fn()

    await (wrapper.vm as any).fetchNode()

    expect(eventStore.clearEvents).not.toHaveBeenCalled()
  })

  it('renders the node panels once the node has loaded', async () => {
    const { wrapper } = mountComponent('42', true)
    await flushPromises()

    expect(wrapper.findComponent({ name: 'BreadCrumbs' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeAvailabilityGraph' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeCategoriesPanel' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeDetailsHeader' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeNotificationsPanel' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'EventsTable' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'InterfacesTabs' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'OutagesTable' }).exists()).toBe(true)
  })

  // The SNMP Attributes panel is gone; those attributes are now behind the actions menu's
  // Info..., which opens the same dialog the node list uses.
  describe('Node info dialog', () => {
    it('does not show the dialog until Info... is chosen', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsDialog' }).props('visible')).toBe(false)
    })

    // The dialog picks the node's best IP from nodeToIpInterfaceMap, which only the node list
    // used to fill -- so without this fetch its IP Address row is blank on this page.
    it('fetches the IP interfaces the dialog needs to show an address', async () => {
      const { wrapper, nodeStore } = mountComponent('42', true)
      await flushPromises()
      void wrapper

      expect(nodeStore.getIpInterfacesForNodes).toHaveBeenCalledWith(['42'], false)
    })

    it('hands the actions menu a handler that opens the dialog on this node', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      const dropdown = wrapper.findComponent({ name: 'NodeActionsDropdown' })
      const trigger = dropdown.props('triggerNodeInfo') as () => void
      expect(trigger).toBeTypeOf('function')

      trigger()
      await flushPromises()

      const dialog = wrapper.findComponent({ name: 'NodeDetailsDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect((dialog.props('node') as any).id).toBe('42')
    })

    it('closes the dialog when it asks to be closed', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()
      ;(wrapper.findComponent({ name: 'NodeActionsDropdown' }).props('triggerNodeInfo') as () => void)()
      await flushPromises()

      wrapper.findComponent({ name: 'NodeDetailsDialog' }).vm.$emit('close')
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsDialog' }).props('visible')).toBe(false)
    })
  })

  it('puts the node actions menu on the title row', async () => {
    const { wrapper } = mountComponent('42', true)
    await flushPromises()

    const header = wrapper.find('.header')
    expect(header.text()).toContain('Node Details for')
    expect(header.findComponent({ name: 'NodeActionsDropdown' }).exists()).toBe(true)
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
