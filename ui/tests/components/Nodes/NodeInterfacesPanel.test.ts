// ui/tests/components/Nodes/NodeInterfacesPanel.test.ts
import NodeInterfacesPanel from '@/components/Nodes/NodeInterfacesPanel.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const mountPanel = (node: any = { id: '1' }) => {
  const wrapper = mount(NodeInterfacesPanel, {
    props: { node },
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false })]
    }
  })

  return {
    wrapper,
    menuStore: useMenuStore(),
    nodeStore: useNodeStore(),
    structure: useNodeStructureStore()
  }
}

describe('NodeInterfacesPanel.vue', () => {
  it('shows the empty state when there are no rows for the node', () => {
    const { wrapper } = mountPanel()

    expect(wrapper.find('[data-test="no-interfaces"]').text()).toBe('No interfaces')
    expect(wrapper.findAll('[data-test="interface-row"]').length).toBe(0)
  })

  it('renders default-mode rows from the node IP interfaces, with hrefs', async () => {
    const { wrapper, nodeStore, menuStore } = mountPanel({ id: '1' })
    menuStore.mainMenu = { baseHref: '/opennms/' } as any
    nodeStore.nodeToIpInterfaceMap = new Map([
      ['1', [{ id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' }]]
    ]) as any

    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-test="interface-row"]')
    expect(rows.length).toBe(1)

    const link = rows[0].find('a')
    expect(link.text()).toBe('10.0.0.1')
    expect(link.attributes('href')).toBe('/opennms/element/interface.jsp?ipinterfaceid=ip1')
  })

  it('excludes deleted/unmanaged (0.0.0.0) IP interfaces in default mode', async () => {
    const { wrapper, nodeStore } = mountPanel({ id: '1' })
    nodeStore.nodeToIpInterfaceMap = new Map([
      ['1', [
        { id: 'ip1', ipAddress: '10.0.0.1', isManaged: 'M' },
        { id: 'ip2', ipAddress: '0.0.0.0', isManaged: 'M' },
        { id: 'ip3', ipAddress: '10.0.0.2', isManaged: 'D' }
      ]]
    ]) as any

    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-test="interface-row"]')
    expect(rows.length).toBe(1)
    expect(rows[0].text()).toContain('10.0.0.1')
  })

  it('renders maclike-mode rows with the matched physAddr as a suffix', async () => {
    const { wrapper, nodeStore, structure } = mountPanel({ id: '1' })
    structure.queryFilter = { ...structure.queryFilter, macAddress: 'aabbcc' }
    nodeStore.nodeToSnmpInterfaceMap = new Map([
      ['1', [{ id: 5, ifIndex: 2, physAddr: 'aabbccddeeff', collectFlag: 'N', ifName: 'eth0', ifDescr: null }]]
    ]) as any

    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('[data-test="interface-row"]')
    expect(rows.length).toBe(1)
    expect(rows[0].text()).toContain('eth0')
    expect(rows[0].text()).toContain('aabbccddeeff')
  })
})
