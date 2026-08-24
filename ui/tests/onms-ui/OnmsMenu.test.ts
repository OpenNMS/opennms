import { OnmsMenu, OnmsMenuItem } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const items: OnmsMenuItem[] = [
  { label: 'Download', command: () => {} },
  { label: 'Delete', command: () => {} }
]

describe('OnmsMenu contract', () => {
  it('maps items to PrimeVue model and defaults popup to true', () => {
    const wrapper = mount(OnmsMenu, {
      props: { items },
      global: { plugins: [PrimeVue] }
    })
    const menu = wrapper.findComponent({ name: 'Menu' })
    expect(menu.props('model')).toEqual(items)
    expect(menu.props('popup')).toBe(true)
    expect(menu.props('appendTo')).toBe('body')
  })

  it('allows inline (non-popup) mode', () => {
    const wrapper = mount(OnmsMenu, {
      props: { items, popup: false },
      global: { plugins: [PrimeVue] }
    })
    expect(wrapper.findComponent({ name: 'Menu' }).props('popup')).toBe(false)
  })

  it('exposes toggle and hide', () => {
    const wrapper = mount(OnmsMenu, {
      props: { items },
      global: { plugins: [PrimeVue] }
    })
    expect(typeof wrapper.vm.toggle).toBe('function')
    expect(typeof wrapper.vm.hide).toBe('function')
  })

  it('forwards the #item slot', () => {
    const wrapper = mount(OnmsMenu, {
      props: { items: [{ label: 'X', statusClass: 'ok' }], popup: false },
      global: { plugins: [PrimeVue] },
      slots: { item: '<template #item="{ item }"><span class="custom-item">{{ item.label }}</span></template>' }
    })
    expect(wrapper.find('.custom-item').exists()).toBe(true)
  })
})
