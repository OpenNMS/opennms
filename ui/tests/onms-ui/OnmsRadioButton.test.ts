import { OnmsRadioButton } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsRadioButton contract', () => {
  it('maps modelValue, value and inputId', () => {
    const wrapper = mount(OnmsRadioButton, {
      props: { modelValue: 'a', value: 'b', inputId: 'opt-b' },
      global: { plugins: [PrimeVue] }
    })
    const rb = wrapper.findComponent({ name: 'RadioButton' })
    expect(rb.props('modelValue')).toBe('a')
    expect(rb.props('value')).toBe('b')
    expect(rb.props('inputId')).toBe('opt-b')
  })

  it('forwards update:modelValue', async () => {
    const wrapper = mount(OnmsRadioButton, {
      props: { modelValue: 'a', value: 'b' },
      global: { plugins: [PrimeVue] }
    })
    await wrapper.findComponent({ name: 'RadioButton' }).vm.$emit('update:modelValue', 'b')
    expect(wrapper.emitted('update:modelValue')).toEqual([['b']])
  })
})
