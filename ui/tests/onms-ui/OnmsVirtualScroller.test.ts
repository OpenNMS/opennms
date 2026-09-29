import { OnmsVirtualScroller } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { defineComponent } from 'vue'
import { describe, expect, it } from 'vitest'

const items = Array.from({ length: 200 }, (_, i) => ({ id: i, label: `node-${i}` }))

const mountIt = (props: Record<string, unknown> = {}, slots: Record<string, string> = {}) =>
  mount(OnmsVirtualScroller, {
    props: { items, itemSize: 40, ...props },
    global: { plugins: [PrimeVue] },
    slots
  })

// PrimeVue's VirtualScroller decides how many rows to render by measuring the
// mounted element, and happy-dom reports every box as zero-sized, so it renders
// an empty window here regardless of the item count. The virtualization itself
// is PrimeVue's behavior, not the seam's; what this wrapper owns is the prop
// mapping and the narrowed slot payload, so the slot is driven directly.
const renderItemSlot = (wrapper: ReturnType<typeof mountIt>, payload: unknown) => {
  const slot = wrapper.findComponent({ name: 'VirtualScroller' }).vm.$slots.item!
  return mount(defineComponent({ render: () => slot(payload) }))
}

describe('OnmsVirtualScroller contract', () => {
  it('maps props onto PrimeVue VirtualScroller', () => {
    const inner = mountIt({ scrollHeight: '300px' }).findComponent({ name: 'VirtualScroller' })
    expect(inner.props('items')).toEqual(items)
    expect(inner.props('itemSize')).toBe(40)
    expect(inner.props('scrollHeight')).toBe('300px')
  })

  it('narrows the item slot to { item, index }, dropping PrimeVue\'s options object', () => {
    const wrapper = mountIt(
      {},
      { item: '<template #item="slotProps"><div class="row">{{ Object.keys(slotProps).sort().join(",") }}</div></template>' }
    )
    const rendered = renderItemSlot(wrapper, { item: items[5], options: { index: 5, count: 200 }})
    expect(rendered.find('.row').text()).toBe('index,item')
  })

  it('passes the item and its index through to the slot', () => {
    const wrapper = mountIt(
      {},
      { item: '<template #item="{ item, index }"><div class="row">{{ index }}:{{ item.label }}</div></template>' }
    )
    const rendered = renderItemSlot(wrapper, { item: items[5], options: { index: 5 }})
    expect(rendered.find('.row').text()).toBe('5:node-5')
  })
})
