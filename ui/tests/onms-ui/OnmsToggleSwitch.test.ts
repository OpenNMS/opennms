import { OnmsToggleSwitch } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsToggleSwitch contract', () => {
  it('maps modelValue and inputId', () => {
    const wrapper = mount(OnmsToggleSwitch, {
      props: { modelValue: true, inputId: 'down-only' },
      global: { plugins: [PrimeVue] }
    })
    const ts = wrapper.findComponent({ name: 'ToggleSwitch' })
    expect(ts.props('modelValue')).toBe(true)
    expect(ts.props('inputId')).toBe('down-only')
  })

  it('forwards update:modelValue', async () => {
    const wrapper = mount(OnmsToggleSwitch, {
      props: { modelValue: false },
      global: { plugins: [PrimeVue] }
    })
    await wrapper.findComponent({ name: 'ToggleSwitch' }).vm.$emit('update:modelValue', true)
    expect(wrapper.emitted('update:modelValue')).toEqual([[true]])
  })
})
