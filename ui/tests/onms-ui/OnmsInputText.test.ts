import { OnmsInputText } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's InputText extends
// BaseInput, whose `$variant` computed reads `this.$primevue.config`
// unconditionally (no optional chaining). Mounting it without the PrimeVue
// config plugin installed throws "Cannot read properties of undefined
// (reading 'config')" — unlike Button, which does not hit this path. The
// brief's mount calls are amended here with `global: { plugins: [PrimeVue] }`
// to reflect that real requirement; no other assertion changes.
const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsInputText', () => {
  it('binds modelValue and emits update:modelValue', async () => {
    const wrapper = mount(OnmsInputText, { props: { modelValue: 'a' }, global: globalPlugins })
    await wrapper.find('input').setValue('abc')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['abc'])
  })

  it('trims when the trim modifier is set', async () => {
    const wrapper = mount(OnmsInputText, { props: { modelValue: '', modelModifiers: { trim: true }}, global: globalPlugins })
    await wrapper.find('input').setValue('  x  ')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['x'])
  })

  it('forwards invalid, disabled and placeholder', () => {
    const wrapper = mount(OnmsInputText, {
      props: { invalid: true, disabled: true, placeholder: 'IP address' },
      global: globalPlugins
    })
    const inner = wrapper.findComponent({ name: 'InputText' })
    expect(inner.props('invalid')).toBe(true)
    expect(inner.props('disabled')).toBe(true)
    // PrimeVue's InputText does not declare `placeholder` as a reactive
    // component prop (it is not in BaseEditableHolder/BaseInput/BaseInputText
    // at all) — it only ever reaches the DOM as a fallthrough attribute, so
    // `.props('placeholder')` is always undefined. Assert the rendered
    // attribute instead (same pattern as OnmsButton's `disabled` handling).
    expect(wrapper.find('input').attributes('placeholder')).toBe('IP address')
  })

  it('forwards fluid to the inner InputText', () => {
    const wrapper = mount(OnmsInputText, { props: { fluid: true }, global: globalPlugins })
    expect(wrapper.findComponent({ name: 'InputText' }).props('fluid')).toBe(true)
  })

  it('lets DOM attrs fall through to the input', () => {
    const wrapper = mount(OnmsInputText, { attrs: { id: 'f1', 'data-test': 'ip', 'aria-label': 'IP' }, global: globalPlugins })
    const input = wrapper.find('input')
    expect(input.attributes('id')).toBe('f1')
    expect(input.attributes('data-test')).toBe('ip')
    expect(input.attributes('aria-label')).toBe('IP')
  })
})
