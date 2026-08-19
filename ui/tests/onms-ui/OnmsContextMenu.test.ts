import { OnmsContextMenu, OnmsMenuItem } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const items: OnmsMenuItem[] = [
  { label: 'View node', command: () => {} },
  { label: 'Remove', command: () => {} }
]

describe('OnmsContextMenu contract', () => {
  it('maps items to the PrimeVue model', () => {
    const inner = mount(OnmsContextMenu, {
      props: { items },
      global: { plugins: [PrimeVue] }
    }).findComponent({ name: 'ContextMenu' })
    expect(inner.props('model')).toEqual(items)
    expect(inner.props('appendTo')).toBe('body')
  })

  it('exposes show and hide', () => {
    const wrapper = mount(OnmsContextMenu, {
      props: { items },
      global: { plugins: [PrimeVue] }
    })
    expect(typeof wrapper.vm.show).toBe('function')
    expect(typeof wrapper.vm.hide).toBe('function')
  })

  it('opens at the pointer event it is given', async () => {
    const wrapper = mount(OnmsContextMenu, {
      props: { items },
      global: { plugins: [PrimeVue] },
      attachTo: document.body
    })
    wrapper.vm.show(new MouseEvent('contextmenu', { clientX: 120, clientY: 80 }))
    await wrapper.vm.$nextTick()
    expect(document.querySelector('.p-contextmenu')).not.toBeNull()
    wrapper.unmount()
  })

  it('forwards the #item slot', async () => {
    const wrapper = mount(OnmsContextMenu, {
      props: { items: [{ label: 'X' }] },
      global: { plugins: [PrimeVue] },
      slots: { item: '<template #item="{ item }"><span class="custom-item">{{ item.label }}</span></template>' },
      attachTo: document.body
    })
    wrapper.vm.show(new MouseEvent('contextmenu'))
    await wrapper.vm.$nextTick()
    expect(document.querySelector('.custom-item')).not.toBeNull()
    wrapper.unmount()
  })
})
