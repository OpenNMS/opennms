import NodeDetails from '@/containers/NodeDetails.vue'
import API from '@/services'
import { createResultWithPayload } from '@/types/validation'
import { NodeDetailsTab, useNodeStore } from '@/stores/nodeStore'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { computed, ref } from 'vue'

const routerReplace = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({ params: { id: '42' }, query: {}})),
  useRouter: vi.fn(() => ({ push: vi.fn(), replace: routerReplace }))
}))

vi.mock('@/services', () => ({
  default: { resolveNodeId: vi.fn() }
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
    nodeStore.getNodeLinkServices = vi.fn().mockResolvedValue(undefined)
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
          NodeDetailsHeader: true,
          NodeDetailsLinks: {
            name: 'NodeDetailsLinks',
            template: '<div></div>',
            props: ['baseHref', 'node', 'snmpPrimaryIpAddress', 'existsInRequisition', 'services', 'triggerNodeInfo']
          },
          NodeNotificationsPanel: true,
          NodeStatusBox: true,
          NodeScheduledOutagesBox: true,
          NodeCriticalPathPanel: true,
          AlarmsTable: true,
          NodeDetailsDialog: true,
          NodeLookupMessage: true,
          EventsTable: true,
          OutagesTable: true,
          NodeDetailsNetworkTab: true,
          InterfacesTabs: true
        }
      }
    })

    return { wrapper, nodeStore }
  }

  beforeEach(() => {
    vi.clearAllMocks()
    routerReplace.mockClear()
    canEditRequisitions.value = true
  })

  // node starts as {} in the store -- truthy, and undefined for every field -- so panels that
  // read it must wait for a real one rather than render "Label: undefined" and
  // element/rescan.jsp?node=undefined.
  it('renders no node panels until the node has loaded', async () => {
    const { wrapper } = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeDetailsHeader' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeDetailsLinks' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeNotificationsPanel' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'NodeAvailabilityGraph' }).exists()).toBe(false)
  })

  it('shows Node Not Found, and no tabs, when the node does not exist', async () => {
    const { wrapper, nodeStore } = mountComponent()
    nodeStore.nodeLoadError = 'notFound'
    await flushPromises()

    const message = wrapper.findComponent({ name: 'NodeLookupMessage' })
    expect(message.exists()).toBe(true)
    expect(message.props()).toEqual({ state: 'notFound', value: '42' })
    expect(wrapper.findComponent({ name: 'NodeDetailsMainTab' }).exists()).toBe(false)
  })

  it('shows Node Lookup Failed, and no tabs, when the node could not be loaded for another reason', async () => {
    const { wrapper, nodeStore } = mountComponent()
    nodeStore.nodeLoadError = 'failed'
    await flushPromises()

    const message = wrapper.findComponent({ name: 'NodeLookupMessage' })
    expect(message.exists()).toBe(true)
    expect(message.props()).toEqual({ state: 'error', value: '42' })
    expect(wrapper.findComponent({ name: 'NodeDetailsMainTab' }).exists()).toBe(false)
  })

  it('fetches the node again when the lookup message asks to retry after a failed load', async () => {
    const { wrapper, nodeStore } = mountComponent()
    nodeStore.nodeLoadError = 'failed'
    await flushPromises()
    expect(nodeStore.getNodeById).toHaveBeenCalledTimes(1)

    wrapper.findComponent({ name: 'NodeLookupMessage' }).vm.$emit('retry')
    await flushPromises()

    expect(nodeStore.getNodeById).toHaveBeenCalledTimes(2)
    expect(nodeStore.getNodeById).toHaveBeenLastCalledWith({ id: '42' })
  })

  it('shows Invalid Node ID, fetches nothing and mounts no tabs, for a bad id', async () => {
    const { wrapper, nodeStore } = mountComponent('abc')
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeLookupMessage' }).props()).toEqual({ state: 'invalid', value: 'abc' })
    expect(nodeStore.getNodeById).not.toHaveBeenCalled()
    expect(wrapper.findComponent({ name: 'NodeDetailsMainTab' }).exists()).toBe(false)
  })

  it('replaces a non-canonical numeric id with the canonical one before fetching', async () => {
    const { nodeStore } = mountComponent('007')
    await flushPromises()

    expect(routerReplace).toHaveBeenCalledWith({ name: 'Node Details', params: { id: '7' }, query: {}})
    expect(nodeStore.getNodeById).not.toHaveBeenCalled()
  })

  it('resolves foreignSource:foreignId and replaces the route with the node id', async () => {
    vi.mocked(API.resolveNodeId).mockResolvedValue(createResultWithPayload(true, '', '153'))
    const { wrapper, nodeStore } = mountComponent('req3:1757365364177')

    expect(wrapper.find('[data-test="node-details-resolving"]').exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'NodeDetailsMainTab' }).exists()).toBe(false)
    await flushPromises()

    expect(API.resolveNodeId).toHaveBeenCalledWith('req3:1757365364177')
    expect(routerReplace).toHaveBeenCalledWith({ name: 'Node Details', params: { id: '153' }, query: {}})
    expect(nodeStore.getNodeById).not.toHaveBeenCalled()
  })

  it('shows Node Not Found for an unknown foreign id', async () => {
    vi.mocked(API.resolveNodeId).mockResolvedValue(createResultWithPayload(true, '', null))
    const { wrapper } = mountComponent('req3:nope')
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeLookupMessage' }).props()).toEqual({ state: 'notFound', value: 'req3:nope' })
  })

  it('shows Node Lookup Failed when the foreign id lookup errors', async () => {
    vi.mocked(API.resolveNodeId).mockResolvedValue(createResultWithPayload<string | null>(false, 'Failed'))
    const { wrapper } = mountComponent('req3:1')
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeLookupMessage' }).props('state')).toBe('error')
  })

  it('looks the foreign id up again when the lookup message asks to retry', async () => {
    vi.mocked(API.resolveNodeId).mockResolvedValue(createResultWithPayload<string | null>(false, 'Failed'))
    const { wrapper } = mountComponent('req3:1')
    await flushPromises()
    expect(API.resolveNodeId).toHaveBeenCalledTimes(1)

    wrapper.findComponent({ name: 'NodeLookupMessage' }).vm.$emit('retry')
    await flushPromises()

    expect(API.resolveNodeId).toHaveBeenCalledTimes(2)
  })

  // The store outlives the page: a failure left by an earlier visit must not show as Node Not
  // Found while this one is still being fetched. getNodeById clears the flag synchronously, so
  // the fetch has to start before the first render.
  it('does not show Node Not Found on first render from an earlier visit\'s failure', () => {
    const pinia = createTestingPinia({ stubActions: false })
    setActivePinia(pinia)
    const nodeStore = useNodeStore()
    nodeStore.nodeLoadError = 'notFound'
    nodeStore.getNodeById = vi.fn(async () => {
      nodeStore.nodeLoadError = null
    })
    nodeStore.getNodeSnmpPrimaryInterface = vi.fn().mockResolvedValue(undefined)
    nodeStore.getNodeExistsInRequisition = vi.fn().mockResolvedValue(undefined)
    nodeStore.getNodeCriticalPath = vi.fn().mockResolvedValue(undefined)
    nodeStore.getNodeLinkServices = vi.fn().mockResolvedValue(undefined)

    const wrapper = mount(NodeDetails, {
      props: { id: '42' },
      global: {
        plugins: [PrimeVue, pinia],
        stubs: {
          BreadCrumbs: true,
          NodeDetailsLinks: true,
          NodeDetailsDialog: true,
          NodeLookupMessage: true,
          NodeDetailsMainTab: true
        }
      }
    })

    expect(wrapper.findComponent({ name: 'NodeLookupMessage' }).exists()).toBe(false)
  })

  it('leaves the title unnamed while the node is still being fetched', async () => {
    const { wrapper } = mountComponent()
    await flushPromises()

    expect(wrapper.find('h2').text()).toBe('Node Details for')
  })

  // The Update SNMP action needs the SNMP-primary address, which the node payload does not
  // carry and the IP Interfaces table only holds a page of, so the page asks for it directly.
  it('asks for the node SNMP-primary address and hands it to the links row', async () => {
    const { wrapper, nodeStore } = mountComponent('42', true)
    await flushPromises()

    expect(nodeStore.getNodeSnmpPrimaryInterface).toHaveBeenCalledWith('42')

    nodeStore.snmpPrimaryIpAddress = '10.0.0.44'
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeDetailsLinks' }).props('snmpPrimaryIpAddress'))
      .toBe('10.0.0.44')
  })

  it('points the Nodes breadcrumb at the node list', async () => {
    const { wrapper } = mountComponent('42', true)
    await flushPromises()

    const items = wrapper.findComponent({ name: 'BreadCrumbs' }).props('items') as Array<{ label: string, to: string }>

    expect(items.find(i => i.label === 'Nodes')?.to).toBe('/nodes')
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
    expect(wrapper.findComponent({ name: 'AlarmsTable' }).exists()).toBe(true)
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
      expect(wrapper.findComponent({ name: 'NodeDetailsLinks' }).exists()).toBe(true)
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

    it('shows the Network tab when chosen', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()
      await selectTab(wrapper, 'network')

      expect(wrapper.findComponent({ name: 'NodeDetailsNetworkTab' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'NodeDetailsMainTab' }).exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EventsTable' }).exists()).toBe(false)
    })

    it('offers Main, Additional and Network, in that order', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      const options = wrapper.findComponent({ name: 'OnmsSelectButton' }).props('options') as Array<{ label: string }>
      expect(options.map(o => o.label)).toEqual(['Main', 'Additional', 'Network'])
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

    it('hands the links row a handler that opens the dialog on this node', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      const dropdown = wrapper.findComponent({ name: 'NodeDetailsLinks' })
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
      ;(wrapper.findComponent({ name: 'NodeDetailsLinks' }).props('triggerNodeInfo') as () => void)()
      await flushPromises()

      wrapper.findComponent({ name: 'NodeDetailsDialog' }).vm.$emit('close')
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsDialog' }).props('visible')).toBe(false)
    })
  })

  describe('links row', () => {
    it('sits under the header once the node has loaded, with what the actions menu gets', async () => {
      const { wrapper, nodeStore } = mountComponent('42', true)
      nodeStore.snmpPrimaryIpAddress = '10.0.0.44'
      nodeStore.existsInRequisition = true
      await flushPromises()

      const links = wrapper.findComponent({ name: 'NodeDetailsLinks' })
      expect(links.exists()).toBe(true)
      expect(links.props('baseHref')).toBe('/base')
      expect((links.props('node') as any).id).toBe('42')
      expect(links.props('snmpPrimaryIpAddress')).toBe('10.0.0.44')
      expect(links.props('existsInRequisition')).toBe(true)
    })

    it('is not shown before the node has loaded', async () => {
      const { wrapper } = mountComponent('42', false)
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsLinks' }).exists()).toBe(false)
    })

    it('opens the info dialog from its Info item', async () => {
      const { wrapper } = mountComponent('42', true)
      await flushPromises()

      ;(wrapper.findComponent({ name: 'NodeDetailsLinks' }).props('triggerNodeInfo') as () => void)()
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsDialog' }).props('visible')).toBe(true)
    })
  })

  // The links row under the header replaced the title row's actions menu.
  it('has no actions menu on the title row', async () => {
    const { wrapper } = mountComponent('42', true)
    await flushPromises()

    const header = wrapper.find('.header')
    expect(header.text()).toContain('Node Details for')
    expect(header.findComponent({ name: 'NodeActionsDropdown' }).exists()).toBe(false)
    expect(header.find('[data-test="node-details-tab-select"]').exists()).toBe(true)
  })

  it('asks for the node\'s Services links alongside the node, and hands them to the links row', async () => {
    const { wrapper, nodeStore } = mountComponent('42', true)
    await flushPromises()

    expect(nodeStore.getNodeLinkServices).toHaveBeenCalledWith('42')

    nodeStore.linkServices = [{ serviceName: 'SSH', ipAddress: '10.0.0.1' }]
    await flushPromises()

    expect(wrapper.findComponent({ name: 'NodeDetailsLinks' }).props('services')).toEqual([{ serviceName: 'SSH', ipAddress: '10.0.0.1' }])
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
    it('asks whether the loaded node is in its requisition, and hands the answer to the links row', async () => {
      const { wrapper, nodeStore } = mountComponent('42', true)
      await flushPromises()

      expect(nodeStore.getNodeExistsInRequisition).toHaveBeenCalledWith(nodeStore.node)

      nodeStore.existsInRequisition = true
      await flushPromises()

      expect(wrapper.findComponent({ name: 'NodeDetailsLinks' }).props('existsInRequisition')).toBe(true)
    })

    it('marks the links row once the check has answered for this node', async () => {
      const { wrapper, nodeStore } = mountComponent('42', true)
      await flushPromises()

      const links = () => wrapper.findComponent({ name: 'NodeDetailsLinks' })
      expect(links().attributes('data-requisition-checked')).toBeUndefined()

      // An answer for another node, from before the user moved on, does not count.
      nodeStore.existsInRequisitionNodeId = '41'
      await flushPromises()
      expect(links().attributes('data-requisition-checked')).toBeUndefined()

      nodeStore.existsInRequisitionNodeId = '42'
      await flushPromises()
      expect(links().attributes('data-requisition-checked')).toBe('true')
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
