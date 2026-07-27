import { OnmsDrawer } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsDrawer contract', () => {
  const mountDrawer = (props: Record<string, unknown> = {}) => mount(OnmsDrawer, {
    props: { visible: true, ...props },
    global: { plugins: [PrimeVue] }
  })

  it('defaults position to right and maps header/width', () => {
    const wrapper = mountDrawer({ header: 'Customize Columns', width: '55em' })
    const drawer = wrapper.findComponent({ name: 'Drawer' })
    expect(drawer.props('position')).toBe('right')
    expect(drawer.props('header')).toBe('Customize Columns')
    expect(drawer.props('visible')).toBe(true)
  })

  it('forwards update:visible and hide', async () => {
    const wrapper = mountDrawer()
    const drawer = wrapper.findComponent({ name: 'Drawer' })
    await drawer.vm.$emit('update:visible', false)
    await drawer.vm.$emit('hide')
    expect(wrapper.emitted('update:visible')).toEqual([[false]])
    expect(wrapper.emitted('hide')).toHaveLength(1)
  })
})
