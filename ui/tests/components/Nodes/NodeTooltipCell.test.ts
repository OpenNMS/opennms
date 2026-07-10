import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Tooltip from 'primevue/tooltip'
import NodeTooltipCell from '@/components/Nodes/NodeTooltipCell.vue'

describe('NodeTooltipCell.vue', () => {
  it('renders ellipsified text in a span (no td wrapper) with the tooltip directive', () => {
    const wrapper = mount(NodeTooltipCell, { props: { text: 'some-foreign-source' }, directives: { tooltip: Tooltip }})
    expect(wrapper.find('td').exists()).toBe(false)
    const span = wrapper.find('span.pointer')
    expect(span.exists()).toBe(true)
    expect(span.text()).toContain('some-foreign-source')
  })

  it('renders nothing clickable when text is empty', () => {
    const wrapper = mount(NodeTooltipCell, { props: { text: '' }, directives: { tooltip: Tooltip }})
    expect(wrapper.find('span.pointer').exists()).toBe(false)
  })
})
