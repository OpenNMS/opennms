import { OnmsSlider } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountIt = (props: Record<string, unknown> = {}) =>
  mount(OnmsSlider, { props, global: { plugins: [PrimeVue] }})

describe('OnmsSlider contract', () => {
  it('maps props onto PrimeVue Slider', () => {
    const inner = mountIt({ modelValue: 12, min: 4, max: 40, step: 2 }).findComponent({ name: 'Slider' })
    expect(inner.props('modelValue')).toBe(12)
    expect(inner.props('min')).toBe(4)
    expect(inner.props('max')).toBe(40)
    expect(inner.props('step')).toBe(2)
  })

  it('routes ariaLabel to the handle rather than the root', () => {
    const wrapper = mountIt({ modelValue: 1, ariaLabel: 'Node size' })
    expect(wrapper.findComponent({ name: 'Slider' }).props('ariaLabel')).toBe('Node size')
    expect(wrapper.find('[role="slider"]').attributes('aria-label')).toBe('Node size')
  })

  it('forwards update:modelValue', () => {
    const wrapper = mountIt({ modelValue: 1 })
    wrapper.findComponent({ name: 'Slider' }).vm.$emit('update:modelValue', 7)
    expect(wrapper.emitted('update:modelValue')).toEqual([[7]])
  })

  it('forwards change', () => {
    const wrapper = mountIt({ modelValue: 1 })
    wrapper.findComponent({ name: 'Slider' }).vm.$emit('change', 9)
    expect(wrapper.emitted('change')).toEqual([[9]])
  })
})
