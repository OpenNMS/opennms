import { OnmsDatePicker } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's DatePicker
// extends BaseComponent, whose computed properties read `this.$primevue.config`
// unconditionally — same issue documented in OnmsSelect.test.ts /
// OnmsAutoComplete.test.ts. Mounting without the PrimeVue config plugin
// installed throws "Cannot read properties of undefined (reading
// 'config')". The brief's mount calls are amended here with
// `global: { plugins: [PrimeVue] }`; no other assertion changes.
const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsDatePicker', () => {
  it('maps modelValue and forwards updates', async () => {
    const d = new Date(2026, 6, 27)
    const wrapper = mount(OnmsDatePicker, {
      props: { modelValue: d },
      global: { plugins: [PrimeVue] }
    })
    expect(wrapper.findComponent({ name: 'DatePicker' }).props('modelValue')).toEqual(d)
    await wrapper.findComponent({ name: 'DatePicker' }).vm.$emit('update:modelValue', null)
    expect(wrapper.emitted('update:modelValue')).toEqual([[null]])
  })

  it('defaults modelValue to null', () => {
    const wrapper = mount(OnmsDatePicker, { global: globalPlugins })
    expect(wrapper.findComponent({ name: 'DatePicker' }).props('modelValue')).toBe(null)
  })
})
