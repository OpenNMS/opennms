// ui/tests/components/Nodes/NodeActionsDropdown.test.ts
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'
import NodeActionsDropdown from '@/components/Nodes/NodeActionsDropdown.vue'
import { useAuthStore } from '@/stores/authStore'

const node = { id: 42, label: 'srv-42', assetRecord: {}} as any

// a fresh pinia for each test, made in beforeEach
let pinia: ReturnType<typeof createTestingPinia>

const setRoles = (...roles: string[]) => {
  const authStore = useAuthStore(pinia)
  authStore.whoAmI = { ...authStore.whoAmI, roles } as never
}

const mountIt = (overrides: any = {}) =>
  mount(NodeActionsDropdown, {
    props: { baseHref: '/opennms/', node, triggerNodeInfo: vi.fn(), ...overrides },
    global: { plugins: [pinia, PrimeVue] }
  })

const labelsOf = (wrapper: ReturnType<typeof mountIt>) =>
  ((wrapper.vm as any).items as Array<{ label: string }>).map(i => i.label)

describe('NodeActionsDropdown.vue', () => {
  // Most of these assert the full menu, so default to the role that sees all of it.
  beforeEach(() => {
    pinia = createTestingPinia({ createSpy: vi.fn })
    setRoles('ROLE_ADMIN')
  })
  it('puts Info... first and includes all link actions', () => {
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items[0].label).toBe('Info...')
    expect(items.map(i => i.label)).toContain('Events')
    expect(items.map(i => i.label)).toContain('View Topology Map')
    expect(items.map(i => i.label)).toContain('Surveillance Categories')
    expect(items).toHaveLength(15) // Info + 14 links as admin; Update SNMP needs an address
  })

  // Driven by the shared nodeActionLinks list, which filters a Site Status link the node
  // cannot supply a building for.
  it('offers Site Status for a node with a building', () => {
    const wrapper = mountIt({ node: { ...node, assetRecord: { building: 'HQ 1' }}})
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items.map(i => i.label)).toContain('Site Status')
    expect(items).toHaveLength(16) // Info + 15 links as admin; Update SNMP needs an address
  })

  it('omits Site Status for a node with no building', () => {
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items.map(i => i.label)).not.toContain('Site Status')
  })

  it('Site Status navigates to the site status view', () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = mountIt({ node: { ...node, assetRecord: { building: 'HQ 1' }}})
    const items = (wrapper.vm as any).items as Array<{ label: string, command: () => void }>
    items.find(i => i.label === 'Site Status')!.command()
    expect(assign).toHaveBeenCalledWith('/opennms/siteStatusView.htm?statusSite=HQ%201')
    vi.unstubAllGlobals()
  })

  it('offers Update SNMP Information only when given the SNMP-primary address', () => {
    const without = (mountIt().vm as any).items as Array<{ label: string }>
    expect(without.map(i => i.label)).not.toContain('Update SNMP Information')

    const wrapper = mountIt({ snmpPrimaryIpAddress: '10.0.0.44' })
    const items = (wrapper.vm as any).items as Array<{ label: string, command: () => void }>
    expect(items.map(i => i.label)).toContain('Update SNMP Information')

    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    items.find(i => i.label === 'Update SNMP Information')!.command()
    expect(assign).toHaveBeenCalledWith('/opennms/admin/updateSnmp.jsp?node=42&ipaddr=10.0.0.44')
    vi.unstubAllGlobals()
  })

  it('offers Node Link Details last', () => {
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items[items.length - 1].label).toBe('Node Link Details')
  })

  it('Node Link Details navigates to the linked node page', () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string, command: () => void }>
    items.find(i => i.label === 'Node Link Details')!.command()
    expect(assign).toHaveBeenCalledWith('/opennms/element/linkednode.jsp?node=42')
    vi.unstubAllGlobals()
  })

  // The Node Details page has no use for an Info... dialog describing the node it is already
  // showing, so it mounts the menu without a handler.
  it('omits Info... when no triggerNodeInfo is given', () => {
    const wrapper = mount(NodeActionsDropdown, {
      props: { baseHref: '/opennms/', node },
      global: { plugins: [PrimeVue] }
    })
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items.map(i => i.label)).not.toContain('Info...')
    expect(items[0].label).toBe('Events')
    expect(items).toHaveLength(14)
  })

  it('Info... command calls triggerNodeInfo with the node', () => {
    const triggerNodeInfo = vi.fn()
    const wrapper = mountIt({ triggerNodeInfo })
    const items = (wrapper.vm as any).items as Array<{ label: string, command: () => void }>
    items[0].command()
    expect(triggerNodeInfo).toHaveBeenCalledWith(node)
  })

  it('a link command navigates to the mapped href', () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string, command: () => void }>
    items.find(i => i.label === 'Events')!.command()
    expect(assign).toHaveBeenCalledWith('/opennms/event/list?filter=node%3D42')
    vi.unstubAllGlobals()
  })

  // The server refuses these to anyone but ROLE_ADMIN (/admin/** plus an explicit rule for
  // element/rescan.jsp), so showing them to a plain user only offers an access denial.
  describe('admin-only actions', () => {
    const ADMIN_ONLY = [
      'Surveillance Categories',
      'Node Rescan',
      'Admin / Node Management',
      'Update SNMP Information',
      'Schedule an Outage'
    ]

    const EVERYONE = [
      'Events',
      'Alarms',
      'Outages',
      'Assets',
      'Metadata',
      'Hardware Inventory',
      'Availability',
      'Resource Graphs',
      'View Topology Map',
      'Node Link Details'
    ]

    it.each(ADMIN_ONLY)('offers %s to an admin', (label) => {
      setRoles('ROLE_ADMIN')

      expect(labelsOf(mountIt({ snmpPrimaryIpAddress: '10.0.0.1' }))).toContain(label)
    })

    it.each(ADMIN_ONLY)('hides %s from a plain user', (label) => {
      setRoles('ROLE_USER')

      expect(labelsOf(mountIt({ snmpPrimaryIpAddress: '10.0.0.1' }))).not.toContain(label)
    })

    it.each(EVERYONE)('still offers %s to a plain user', (label) => {
      setRoles('ROLE_USER')

      expect(labelsOf(mountIt())).toContain(label)
    })

    // Update SNMP is admin-gated on ROLE_ADMIN alone -- admin/updateSnmp.jsp falls under the
    // /admin/** rule, unlike useRole's snmpRole, which also admits ROLE_PROVISION.
    it('hides Update SNMP Information from a provisioner', () => {
      setRoles('ROLE_PROVISION')

      expect(labelsOf(mountIt({ snmpPrimaryIpAddress: '10.0.0.1' })))
        .not.toContain('Update SNMP Information')
    })

    it('leaves a plain user the non-admin actions and Info...', () => {
      setRoles('ROLE_USER')

      const labels = labelsOf(mountIt({ snmpPrimaryIpAddress: '10.0.0.1' }))

      expect(labels[0]).toBe('Info...')
      expect(labels).toHaveLength(11) // Info + 10 links; the 5 admin ones are gone
    })

    it('offers nothing admin-only to a user with no roles at all', () => {
      setRoles()

      const labels = labelsOf(mountIt({ snmpPrimaryIpAddress: '10.0.0.1' }))

      ADMIN_ONLY.forEach(label => expect(labels).not.toContain(label))
    })
  })

  describe('Edit in Requisition', () => {
    const requisitioned = { ...node, foreignSource: 'fs', foreignId: 'fid' }

    it('is offered, last, when the page says the node is in its requisition', () => {
      const labels = labelsOf(mountIt({ node: requisitioned, existsInRequisition: true }))

      expect(labels[labels.length - 1]).toBe('Edit in Requisition')
    })

    // The node list passes nothing: it cannot tell without a request per row.
    it('is left out when the caller does not say', () => {
      expect(labelsOf(mountIt({ node: requisitioned }))).not.toContain('Edit in Requisition')
    })

    it('is offered to a provision user who is not an admin', () => {
      setRoles('ROLE_USER', 'ROLE_PROVISION')

      expect(labelsOf(mountIt({ node: requisitioned, existsInRequisition: true }))).toContain('Edit in Requisition')
    })

    it('is left out for a user who is neither admin nor provision', () => {
      setRoles('ROLE_USER')

      expect(labelsOf(mountIt({ node: requisitioned, existsInRequisition: true }))).not.toContain('Edit in Requisition')
    })
  })

  // The node list's rows: the same confirmation as the Node Details links row.
  describe('Assets confirmation', () => {
    const requisitioned = { ...node, foreignSource: 'fs', foreignId: 'fid' }
    const assetsItem = (wrapper: ReturnType<typeof mountIt>) =>
      ((wrapper.vm as any).items as Array<{ label: string, command: () => void }>).find(i => i.label === 'Assets')!

    // The node list has one of these per row, so the dialog waits until it is needed.
    it('creates no dialog until a confirmation is pending', () => {
      expect(mountIt({ node: requisitioned }).findComponent({ name: 'AssetEditConfirmDialog' }).exists()).toBe(false)
    })

    it('removes the dialog again when the confirmation is cancelled', async () => {
      const wrapper = mountIt({ node: requisitioned })
      assetsItem(wrapper).command()
      await wrapper.vm.$nextTick()

      wrapper.findComponent({ name: 'AssetEditConfirmDialog' }).vm.$emit('cancel')
      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent({ name: 'AssetEditConfirmDialog' }).exists()).toBe(false)
    })

    it('asks before opening the asset editor for a node from a requisition', async () => {
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)
      const wrapper = mountIt({ node: requisitioned })

      assetsItem(wrapper).command()
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'AssetEditConfirmDialog' })
      expect(assign).not.toHaveBeenCalled()
      expect(dialog.props('visible')).toBe(true)

      dialog.vm.$emit('ok')
      expect(assign).toHaveBeenCalledWith('/opennms/asset/modify.jsp?node=42')
      vi.unstubAllGlobals()
    })

    it('goes straight there for a node not from a requisition', () => {
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)

      assetsItem(mountIt()).command()

      expect(assign).toHaveBeenCalledWith('/opennms/asset/modify.jsp?node=42')
      vi.unstubAllGlobals()
    })
  })
})
