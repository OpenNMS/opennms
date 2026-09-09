// ui/tests/components/Nodes/NodeDetailsHeader.test.ts
import NodeDetailsHeader from '@/components/Nodes/NodeDetailsHeader.vue'
import { OnmsMenu } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const node = { id: 42, label: 'srv-42', location: 'Default', assetRecord: {}} as any

const mountHeader = () =>
  mount(NodeDetailsHeader, {
    props: { node },
    global: { plugins: [PrimeVue] }
  })

describe('NodeDetailsHeader.vue', () => {
  it('renders the node status, label, id and location badges', () => {
    const text = mountHeader().text()

    expect(text).toContain('Status:')
    expect(text).toContain('Label: srv-42')
    expect(text).toContain('ID: 42')
    expect(text).toContain('Monitoring Location: Default')
  })

  // The actions menu lives on the Node Details title row now; this header carried a second
  // copy of the same link list.
  it('no longer renders its own actions menu', () => {
    const wrapper = mountHeader()

    expect(wrapper.findComponent(OnmsMenu).exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Actions')
  })
})
