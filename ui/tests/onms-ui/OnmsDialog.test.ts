import { OnmsDialog } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsDialog', () => {
  it('maps props onto the inner Dialog and applies width as style', () => {
    const inner = mount(OnmsDialog, {
      props: { visible: true, header: 'Confirm', width: '40rem', appendTo: 'self' },
      global: globalPlugins
    }).findComponent({ name: 'Dialog' })
    expect(inner.props('visible')).toBe(true)
    expect(inner.props('header')).toBe('Confirm')
    expect(inner.props('modal')).toBe(true)
    expect(inner.props('closable')).toBe(true)
    expect(inner.props('draggable')).toBe(false)
    expect(inner.props('closeOnEscape')).toBe(true)
  })

  it('renders body and footer slots when visible', () => {
    const wrapper = mount(OnmsDialog, {
      props: { visible: true, appendTo: 'self' },
      slots: { default: '<p data-test="body">Body</p>', footer: '<span data-test="foot">F</span>' },
      global: globalPlugins
    })
    expect(wrapper.find('[data-test="body"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="foot"]').exists()).toBe(true)
  })

  it('re-emits update:visible, show and hide', () => {
    const wrapper = mount(OnmsDialog, { props: { visible: true, appendTo: 'self' }, global: globalPlugins })
    const inner = wrapper.findComponent({ name: 'Dialog' })
    inner.vm.$emit('update:visible', false)
    inner.vm.$emit('show')
    inner.vm.$emit('hide')
    expect(wrapper.emitted('update:visible')![0]).toEqual([false])
    expect(wrapper.emitted('show')).toHaveLength(1)
    expect(wrapper.emitted('hide')).toHaveLength(1)
  })
})
