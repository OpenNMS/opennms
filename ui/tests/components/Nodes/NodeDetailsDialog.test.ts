// ui/tests/components/Nodes/NodeDetailsDialog.test.ts
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'
import NodeDetailsDialog from '@/components/Nodes/NodeDetailsDialog.vue'
import { useMenuStore } from '@/stores/menuStore'

// Stub the teleporting PrimeVue Dialog so its slots render inline and can be
// queried directly, consistent with the established pattern in this codebase.
const DialogStub = {
  name: 'Dialog',
  template: '<div class="dialog-stub" v-if="visible"><slot></slot></div>',
  props: ['visible', 'header', 'modal', 'style'],
  emits: ['update:visible']
}

const node = {
  id: 7, label: 'srv-7', location: 'Default', foreignSource: 'fs', foreignId: 'fid',
  sysContact: '', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '',
  assetRecord: {}, ipInterfaces: []
} as any

// The dialog builds its own links off menuStore now, rather than taking computeNodeLink /
// computeNodeIpInterfaceLink props from NodesTable.
const mountIt = (visible: boolean, nodeProp = node) => {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  useMenuStore(pinia).mainMenu = { baseHref: '/opennms/', baseNodeUrl: 'element/node.jsp?node=' } as never

  return mount(NodeDetailsDialog, {
    props: { visible, node: nodeProp },
    global: {
      plugins: [PrimeVue, pinia],
      stubs: { Dialog: DialogStub }
    }
  })
}

describe('NodeDetailsDialog.vue', () => {
  it('renders the node detail rows when visible', () => {
    const wrapper = mountIt(true)
    // Assert via wrapper.text() since Dialog is stubbed inline (not teleported)
    expect(wrapper.text()).toContain('Node ID')
    expect(wrapper.text()).toContain('Node Label')
  })

  it('links the node id and label to the node page', () => {
    const wrapper = mountIt(true)
    const hrefs = wrapper.findAll('a').map(a => a.attributes('href'))

    expect(hrefs).toContain('/opennms/element/node.jsp?node=7')
  })

  it('emits close when the dialog requests hide', async () => {
    const wrapper = mountIt(true)
    // Simulate PrimeVue Dialog update:visible(false)
    await (wrapper.findComponent({ name: 'Dialog' }) as any).vm.$emit('update:visible', false)
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  // The legacy node page's Asset Information box showed the description and comments.
  describe('asset rows', () => {
    const rows = (assetRecord: object) => {
      const wrapper = mountIt(true, { ...node, assetRecord })

      return wrapper.findAll('.onms-row').map(row => row.text().replace(/\s+/g, ' ').trim())
    }

    it('shows the asset comments right after the asset description', () => {
      const all = rows({ description: 'Core router', comment: 'Rack 4, top shelf' })
      const description = all.findIndex(r => r.startsWith('Asset Description'))

      expect(all[description]).toContain('Core router')
      expect(all[description + 1]).toMatch(/^Asset Comments\s*Rack 4, top shelf$/)
    })

    it('shows the empty marker when the node has no asset comments', () => {
      const comments = rows({ comment: null }).find(r => r.startsWith('Asset Comments'))

      expect(comments).toMatch(/^Asset Comments\s*--$/)
    })
  })
})
