// ui/tests/components/Nodes/NodeDetailsDialog.test.ts
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'
import NodeDetailsDialog from '@/components/Nodes/NodeDetailsDialog.vue'

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

const mountIt = (visible: boolean) =>
  mount(NodeDetailsDialog, {
    props: {
      visible,
      node,
      computeNodeLink: (id: any) => `/node/${id}`,
      computeNodeIpInterfaceLink: (id: any, ip: any) => `/intf/${id}/${ip}`
    },
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn })],
      stubs: { Dialog: DialogStub }
    }
  })

describe('NodeDetailsDialog.vue', () => {
  it('renders the node detail rows when visible', () => {
    const wrapper = mountIt(true)
    // Assert via wrapper.text() since Dialog is stubbed inline (not teleported)
    expect(wrapper.text()).toContain('Node ID')
    expect(wrapper.text()).toContain('Node Label')
  })

  it('emits close when the dialog requests hide', async () => {
    const wrapper = mountIt(true)
    // Simulate PrimeVue Dialog update:visible(false)
    await (wrapper.findComponent({ name: 'Dialog' }) as any).vm.$emit('update:visible', false)
    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
