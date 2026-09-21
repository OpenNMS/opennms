// ui/tests/components/Nodes/NodeActionsDropdown.test.ts
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'
import NodeActionsDropdown from '@/components/Nodes/NodeActionsDropdown.vue'

const node = { id: 42, label: 'srv-42', assetRecord: {}} as any

const mountIt = (overrides: any = {}) =>
  mount(NodeActionsDropdown, {
    props: { baseHref: '/opennms/', node, triggerNodeInfo: vi.fn(), ...overrides },
    global: { plugins: [PrimeVue] }
  })

describe('NodeActionsDropdown.vue', () => {
  it('puts Info... first and includes all link actions', () => {
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items[0].label).toBe('Info...')
    expect(items.map(i => i.label)).toContain('Events')
    expect(items.map(i => i.label)).toContain('View Topology Map')
    expect(items).toHaveLength(14) // Info + 13 links; Update SNMP needs an address
  })

  // Driven by the shared nodeActionLinks list, which filters a Site Status link the node
  // cannot supply a building for.
  it('offers Site Status for a node with a building', () => {
    const wrapper = mountIt({ node: { ...node, assetRecord: { building: 'HQ 1' }}})
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items.map(i => i.label)).toContain('Site Status')
    expect(items).toHaveLength(15) // Info + 14 links; Update SNMP needs an address
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
    expect(items).toHaveLength(13)
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
})
