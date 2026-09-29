import { OnmsMenuItem, OnmsTieredMenu } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const items: OnmsMenuItem[] = [
  {
    label: 'Layer 2',
    items: [
      { label: 'LLDP', command: () => {} },
      { label: 'CDP', command: () => {} }
    ]
  },
  { label: 'Layer 3', items: [{ label: 'OSPF', command: () => {} }] }
]

describe('OnmsTieredMenu contract', () => {
  it('maps items to the PrimeVue model and defaults popup to true', () => {
    const inner = mount(OnmsTieredMenu, {
      props: { items },
      global: { plugins: [PrimeVue] }
    }).findComponent({ name: 'TieredMenu' })
    expect(inner.props('model')).toEqual(items)
    expect(inner.props('popup')).toBe(true)
    expect(inner.props('appendTo')).toBe('body')
  })

  it('allows inline (non-popup) mode', () => {
    const inner = mount(OnmsTieredMenu, {
      props: { items, popup: false },
      global: { plugins: [PrimeVue] }
    }).findComponent({ name: 'TieredMenu' })
    expect(inner.props('popup')).toBe(false)
  })

  it('renders nested items as submenus rather than flattening them', () => {
    const wrapper = mount(OnmsTieredMenu, {
      props: { items, popup: false },
      global: { plugins: [PrimeVue] }
    })
    // Only the two groups render; their children sit in collapsed submenus
    // that open on hover. A flat menu would have rendered all five labels.
    expect(wrapper.findAll('.p-tieredmenu-item-label').map(l => l.text()))
      .toEqual(['Layer 2', 'Layer 3'])
    expect(wrapper.findAll('.p-tieredmenu-submenu')).toHaveLength(2)
    expect(wrapper.findAll('.p-tieredmenu-submenu-icon')).toHaveLength(2)
  })

  it('exposes toggle and hide', () => {
    const wrapper = mount(OnmsTieredMenu, {
      props: { items },
      global: { plugins: [PrimeVue] }
    })
    expect(typeof wrapper.vm.toggle).toBe('function')
    expect(typeof wrapper.vm.hide).toBe('function')
  })

  it('forwards the #item slot', () => {
    const wrapper = mount(OnmsTieredMenu, {
      props: { items: [{ label: 'X' }], popup: false },
      global: { plugins: [PrimeVue] },
      slots: { item: '<template #item="{ item }"><span class="custom-item">{{ item.label }}</span></template>' }
    })
    expect(wrapper.find('.custom-item').exists()).toBe(true)
  })
})
