import NodeDetails from '@/containers/NodeDetails.vue'
import { NodeDetailsTab, useNodeStore } from '@/stores/nodeStore'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { computed, ref } from 'vue'

vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    params: { id: '42' }
  })),
  useRouter: vi.fn(() => ({
    push: vi.fn()
  }))
}))

const canEditRequisitions = ref(true)
vi.mock('@/composables/useRole', () => ({
  default: () => ({ provisionRole: computed(() => canEditRequisitions.value) })
}))

describe('NodeDetails.vue', () => {
  const mountComponent = (id = '42', loaded = false, tab: NodeDetailsTab = 'main') => {
    const pinia = createTestingPinia({ stubActions: false })
    setActivePinia(pinia)

    const nodeStore = useNodeStore()
    nodeStore.getNodeById = vi.fn().mockResolvedValue(undefined)
    nodeStore.getNodeSnmpPrimaryInterface = vi.fn().mockResolvedValue(undefined)
    nodeStore.getNodeExistsInRequisition = vi.fn().mockResolvedValue(undefined)
    nodeStore.getNodeCriticalPath = vi.fn().mockResolvedValue(undefined)
    nodeStore.nodeDetailsTab = tab

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
          NodeActionsDropdown: {
            name: 'NodeActionsDropdown',
            template: '<div></div>',
            props: ['baseHref', 'node', 'snmpPrimaryIpAddress', 'existsInRequisition', 'triggerNodeInfo']
          },
          NodeDetailsHeader: true,
          NodeNotificationsPanel: true,
          NodeStatusBox: true,
          NodeScheduledOutagesBox: true,
          NodeCriticalPathPanel: true,
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
    canEditRequisitions.value = true
  })

  // node starts as {} in the store -- truthy, and undefined for every field -- so panels that
  // read it must wait for a real one rather than render "Label: undefined" and
  // element/rescan.jsp?node=undefined.
  it('renders no node panels until the node has loaded', async () => {
    const { wrapper } = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeDetailsHeader' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeActionsDropdown' }).exists()).toBe(false)
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

  it('renders the node panels once the node has loaded', async () => {
    const { wrapper } = mountComponent('42', true)
    await flushPromises()

    expect(wrapper.findComponent({ name: 'BreadCrumbs' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeAvailabilityGraph' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeDetailsHeader' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeNotificationsPanel' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'InterfacesTabs' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeStatusBox' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeScheduledOutagesBox' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeCriticalPathPanel' }).exists()).toBe(true)
  })

  describe('Main / Additional tabs', () => {
    const selectTab = async (wrapper: any, tab: NodeDetailsTab) => {
      wrapper.findComponent({ name: 'OnmsSelectButton' }).vm.$emit('update:modelValue', tab)
      await flushPromises()
    }

    it('opens on Main, without mounting the Additional tables', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsMainTab' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'NodeDetailsAdditionalTab' }).exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EventsTable' }).exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'OutagesTable' }).exists()).toBe(false)
    })

    it('shows events and outages on Additional, and keeps the header in view', async () => {
      const { wrapper, nodeStore } = mountComponent('42', true)
      await flushPromises()
      await selectTab(wrapper, 'additional')

      expect(nodeStore.nodeDetailsTab).toBe('additional')
      expect(wrapper.findComponent({ name: 'EventsTable' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'OutagesTable' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'NodeAvailabilityGraph' }).exists()).toBe(false)

      expect(wrapper.findComponent({ name: 'BreadCrumbs' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'NodeDetailsHeader' }).exists()).toBe(true)
      expect(wrapper.find('.header').findComponent({ name: 'NodeActionsDropdown' }).exists()).toBe(true)
    })

    // KeepAlive: going back to Main reuses the mounted tab rather than rebuilding it.
    it('keeps a tab mounted after switching away and back', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()
      const mainTabUid = wrapper.findComponent({ name: 'NodeDetailsMainTab' }).vm.$.uid

      await selectTab(wrapper, 'additional')
      await selectTab(wrapper, 'main')

      expect(wrapper.findComponent({ name: 'NodeDetailsMainTab' }).vm.$.uid).toBe(mainTabUid)
    })

    it('opens on the tab last chosen, which the store keeps', async () => {
      const { wrapper } = mountComponent('42', true, 'additional')
      await flushPromises()

      expect(wrapper.findComponent({ name: 'OnmsSelectButton' }).props('modelValue')).toBe('additional')
      expect(wrapper.findComponent({ name: 'EventsTable' }).exists()).toBe(true)
    })

    it('puts the tab switch on the title row', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      expect(wrapper.find('.header').findComponent({ name: 'OnmsSelectButton' }).exists()).toBe(true)
    })
  })

  // The SNMP Attributes panel is gone; those attributes are now behind the actions menu's
  // Info..., which opens the same dialog the node list uses.
  describe('Node info dialog', () => {
    it('does not show the dialog until Info... is chosen', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsDialog' }).props('visible')).toBe(false)
    })

    // The dialog picks the node's best IP out of nodeToIpInterfaceMap. This page used to issue a
    // second limit=0 request purely to fill that; the IP interfaces table's own fetch now does it
    // (see nodeStore), so there is nothing for the page to do.
    it('does not fetch interfaces of its own for the dialog', async () => {
      const { nodeStore } = mountComponent('42', true)
      await flushPromises()

      expect(nodeStore.getIpInterfacesForNodes).not.toHaveBeenCalled()
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

  it('asks for the node\'s critical path alongside the node', async () => {
    const { nodeStore } = mountComponent('42')
    await flushPromises()

    expect(nodeStore.getNodeCriticalPath).toHaveBeenCalledWith('42')
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

  describe('Edit in Requisition', () => {
    it('asks whether the loaded node is in its requisition, and hands the answer to the menu', async () => {
      const { wrapper, nodeStore } = mountComponent('42', true)
      await flushPromises()

      expect(nodeStore.getNodeExistsInRequisition).toHaveBeenCalledWith(nodeStore.node)

      nodeStore.existsInRequisition = true
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeActionsDropdown' }).props('existsInRequisition')).toBe(true)
    })

    it('does not ask before the node has loaded', async () => {
      const { nodeStore } = mountComponent('42', false)
      await flushPromises()

      expect(nodeStore.getNodeExistsInRequisition).not.toHaveBeenCalled()
    })

    // The answer is only any use to someone who could follow the link.
    it('does not ask for a user who cannot edit requisitions', async () => {
      canEditRequisitions.value = false
      const { nodeStore } = mountComponent('42', true)
      await flushPromises()

      expect(nodeStore.getNodeExistsInRequisition).not.toHaveBeenCalled()
    })

    // Roles can arrive after the node.
    it('asks once the roles arrive', async () => {
      canEditRequisitions.value = false
      const { nodeStore } = mountComponent('42', true)
      await flushPromises()

      canEditRequisitions.value = true
      await flushPromises()

      expect(nodeStore.getNodeExistsInRequisition).toHaveBeenCalledTimes(1)
    })
  })
})
