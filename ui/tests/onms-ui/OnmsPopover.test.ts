import { OnmsPopover } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsPopover contract', () => {
  it('defaults appendTo to body and forwards it', () => {
    const wrapper = mount(OnmsPopover, {
      global: { plugins: [PrimeVue] }
    })
    expect(wrapper.findComponent({ name: 'Popover' }).props('appendTo')).toBe('body')
  })

  it('accepts appendTo self and forwards hide', async () => {
    const wrapper = mount(OnmsPopover, {
      props: { appendTo: 'self' },
      global: { plugins: [PrimeVue] }
    })
    const pop = wrapper.findComponent({ name: 'Popover' })
    expect(pop.props('appendTo')).toBe('self')
    await pop.vm.$emit('hide')
    expect(wrapper.emitted('hide')).toHaveLength(1)
  })

  it('exposes show/hide/toggle', () => {
    const wrapper = mount(OnmsPopover, {
      global: { plugins: [PrimeVue] }
    })
    expect(typeof wrapper.vm.show).toBe('function')
    expect(typeof wrapper.vm.hide).toBe('function')
    expect(typeof wrapper.vm.toggle).toBe('function')
  })
})
