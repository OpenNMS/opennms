// ui/tests/components/Nodes/NodeDetailsHeader.test.ts
import NodeDetailsHeader from '@/components/Nodes/NodeDetailsHeader.vue'
import { OnmsMenu, OnmsTag } from '@opennms/onms-ui'
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

  // getNodeStatusString derives these three from node.type; the tag colours them.
  describe('status severity', () => {
    const statusTag = (type?: string) => {
      const wrapper = mount(NodeDetailsHeader, {
        props: { node: { ...node, type }},
        global: { plugins: [PrimeVue] }
      })

      return wrapper.findAllComponents(OnmsTag)[0]
    }

    it('shows an active node in success', () => {
      const tag = statusTag('A')

      expect(tag.props('value')).toBe('Status: Active')
      expect(tag.props('severity')).toBe('success')
    })

    it('shows a deleted node in danger', () => {
      const tag = statusTag('D')

      expect(tag.props('value')).toBe('Status: Deleted')
      expect(tag.props('severity')).toBe('danger')
    })

    it('shows an unknown status in info', () => {
      const tag = statusTag('X')

      expect(tag.props('value')).toBe('Status: Unknown')
      expect(tag.props('severity')).toBe('info')
    })

    it('falls back to info for a node with no type at all', () => {
      const tag = statusTag(undefined)

      expect(tag.props('severity')).toBe('info')
    })
  })

  // The actions menu lives on the Node Details title row now; this header carried a second
  // copy of the same link list.
  it('no longer renders its own actions menu', () => {
    const wrapper = mountHeader()

    expect(wrapper.findComponent(OnmsMenu).exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Actions')
  })
})
