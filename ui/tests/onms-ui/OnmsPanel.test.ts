import { OnmsPanel } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsPanel contract', () => {
  it('maps header, toggleable and collapsed', () => {
    const wrapper = mount(OnmsPanel, {
      props: { header: 'Advanced', toggleable: true, collapsed: true },
      global: { plugins: [PrimeVue] }
    })
    const panel = wrapper.findComponent({ name: 'Panel' })
    expect(panel.props('header')).toBe('Advanced')
    expect(panel.props('toggleable')).toBe(true)
    expect(panel.props('collapsed')).toBe(true)
  })

  it('forwards update:collapsed and the header slot', async () => {
    const wrapper = mount(OnmsPanel, {
      props: { toggleable: true },
      global: { plugins: [PrimeVue] },
      slots: { header: '<h4 class="hdr">Steps</h4>', default: '<p>body</p>' }
    })
    expect(wrapper.find('.hdr').exists()).toBe(true)
    await wrapper.findComponent({ name: 'Panel' }).vm.$emit('update:collapsed', false)
    expect(wrapper.emitted('update:collapsed')).toEqual([[false]])
  })

  it('binds unsafePt through to pt', () => {
    const pt = { header: { style: { cursor: 'pointer' }}}
    const wrapper = mount(OnmsPanel, {
      props: { unsafePt: pt },
      global: { plugins: [PrimeVue] }
    })
    expect(wrapper.findComponent({ name: 'Panel' }).props('pt')).toEqual(pt)
  })
})
