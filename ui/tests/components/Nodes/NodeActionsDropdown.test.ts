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
    expect(items).toHaveLength(15) // Info + 14 links
  })

  it('offers Node Link Detailed Info last', () => {
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string }>
    expect(items[items.length - 1].label).toBe('Node Link Detailed Info')
  })

  it('Node Link Detailed Info navigates to the linked node page', () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = mountIt()
    const items = (wrapper.vm as any).items as Array<{ label: string, command: () => void }>
    items.find(i => i.label === 'Node Link Detailed Info')!.command()
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
})
