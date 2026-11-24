import { OnmsMultiSelect } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's MultiSelect
// extends BaseComponent, whose computed properties read `this.$primevue.config`
// unconditionally — same issue documented in OnmsSelect.test.ts /
// OnmsAutoComplete.test.ts. Mounting without the PrimeVue config plugin
// installed throws "Cannot read properties of undefined (reading
// 'config')". The brief's mount calls are amended here with
// `global: { plugins: [PrimeVue] }`; no other assertion changes.
const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsMultiSelect', () => {
  it('maps option props and defaults', () => {
    const wrapper = mount(OnmsMultiSelect, {
      props: { options: [{ _text: 'A', _value: 1 }], optionLabel: '_text', dataKey: '_value', filter: true, display: 'chip', placeholder: 'Select' },
      global: { plugins: [PrimeVue] }
    })
    const ms = wrapper.findComponent({ name: 'MultiSelect' })
    expect(ms.props('optionLabel')).toBe('_text')
    expect(ms.props('dataKey')).toBe('_value')
    expect(ms.props('filter')).toBe(true)
    expect(ms.props('display')).toBe('chip')
  })

  it('defaults display to comma', () => {
    const wrapper = mount(OnmsMultiSelect, {
      props: { options: [] },
      global: globalPlugins
    })
    expect(wrapper.findComponent({ name: 'MultiSelect' }).props('display')).toBe('comma')
  })

  it('re-emits update:modelValue from the inner MultiSelect', () => {
    const wrapper = mount(OnmsMultiSelect, { props: { options: [] }, global: globalPlugins })
    wrapper.findComponent({ name: 'MultiSelect' }).vm.$emit('update:modelValue', [1, 2])
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([[1, 2]])
  })

  it('forwards placeholder to the inner MultiSelect', () => {
    const wrapper = mount(OnmsMultiSelect, {
      props: { options: [], placeholder: 'Choose one' },
      global: globalPlugins
    })
    expect(wrapper.findComponent({ name: 'MultiSelect' }).props('placeholder')).toBe('Choose one')
  })

  it('maps showToggleAll and maxSelectedLabels, keeping PrimeVue\'s defaults', () => {
    const plain = mount(OnmsMultiSelect, { props: { options: [] }, global: globalPlugins })
    expect(plain.findComponent({ name: 'MultiSelect' }).props('showToggleAll')).toBe(true)
    // undefined falls back to PrimeVue's own default, which is null
    expect(plain.findComponent({ name: 'MultiSelect' }).props('maxSelectedLabels')).toBeNull()

    const narrowed = mount(OnmsMultiSelect, {
      props: { options: [], showToggleAll: false, maxSelectedLabels: 2 },
      global: globalPlugins
    })
    expect(narrowed.findComponent({ name: 'MultiSelect' }).props('showToggleAll')).toBe(false)
    expect(narrowed.findComponent({ name: 'MultiSelect' }).props('maxSelectedLabels')).toBe(2)
  })
})
