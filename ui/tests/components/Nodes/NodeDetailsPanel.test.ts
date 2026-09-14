// ui/tests/components/Nodes/NodeDetailsPanel.test.ts
import NodeDetailsPanel from '@/components/Nodes/NodeDetailsPanel.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const mountPanel = (slots: Record<string, string> = {}) =>
  mount(NodeDetailsPanel, { props: { title: 'Recent Outages' }, slots })

describe('NodeDetailsPanel.vue', () => {
  it('renders the title in the card title row', () => {
    const wrapper = mountPanel()

    expect(wrapper.find('.card .title-row .title').text()).toBe('Recent Outages')
  })

  it('renders the default slot as the card body', () => {
    const wrapper = mountPanel({ default: '<p data-test="body">panel body</p>' })

    expect(wrapper.find('.card [data-test="body"]').text()).toBe('panel body')
  })

  it('renders actions in a right-aligned container on the title row', () => {
    const wrapper = mountPanel({ actions: '<button data-test="action">Go</button>' })

    const buttons = wrapper.find('.title-row .action-buttons-container')
    expect(buttons.exists()).toBe(true)
    expect(buttons.find('[data-test="action"]').exists()).toBe(true)
  })

  // A panel with no actions must not leave an empty flex container on the title row.
  it('omits the actions container when no actions are given', () => {
    const wrapper = mountPanel()

    expect(wrapper.find('.action-buttons-container').exists()).toBe(false)
  })
})
