import { OnmsSelect } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's Select extends
// BaseInput, whose `$variant` computed reads `this.$primevue.config`
// unconditionally (no optional chaining) — same issue documented in
// OnmsInputText.test.ts. Mounting without the PrimeVue config plugin
// installed throws "Cannot read properties of undefined (reading
// 'config')". The brief's mount calls are amended here with
// `global: { plugins: [PrimeVue] }`; no other assertion changes.
const globalPlugins = { plugins: [PrimeVue] }

const options = [{ name: 'Minion', id: 1 }, { name: 'Sentinel', id: 2 }]

describe('OnmsSelect', () => {
  it('forwards options and optionLabel/optionValue', () => {
    const inner = mount(OnmsSelect, {
      props: { options, optionLabel: 'name', optionValue: 'id', inputId: 's1', invalid: true },
      global: globalPlugins
    }).findComponent({ name: 'Select' })
    expect(inner.props('options')).toEqual(options)
    expect(inner.props('optionLabel')).toBe('name')
    expect(inner.props('optionValue')).toBe('id')
    expect(inner.props('inputId')).toBe('s1')
    expect(inner.props('invalid')).toBe(true)
  })

  it('re-emits update:modelValue from the inner select', () => {
    const wrapper = mount(OnmsSelect, { props: { options, optionLabel: 'name' }, global: globalPlugins })
    wrapper.findComponent({ name: 'Select' }).vm.$emit('update:modelValue', options[1])
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([options[1]])
  })

  it('forwards fluid and showClear to the inner select', () => {
    const inner = mount(OnmsSelect, {
      props: { options, optionLabel: 'name', showClear: true, fluid: true },
      global: globalPlugins
    }).findComponent({ name: 'Select' })
    expect(inner.props('showClear')).toBe(true)
    expect(inner.props('fluid')).toBe(true)
  })
})
