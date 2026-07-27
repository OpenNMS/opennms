import { OnmsInputNumber } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsInputNumber contract', () => {
  const mountIt = (props: Record<string, unknown> = {}) => mount(OnmsInputNumber, {
    props,
    global: { plugins: [PrimeVue] }
  })

  it('defaults useGrouping to false (seam flip) and maps numeric props', () => {
    const wrapper = mountIt({ modelValue: 162, min: 1, max: 65535 })
    const inner = wrapper.findComponent({ name: 'InputNumber' })
    expect(inner.props('useGrouping')).toBe(false)
    expect(inner.props('min')).toBe(1)
    expect(inner.props('max')).toBe(65535)
    expect(inner.props('modelValue')).toBe(162)
  })

  it('maps maxFractionDigits, invalid, inputId and forwards inputProps to the inner <input>', () => {
    const wrapper = mountIt({ maxFractionDigits: 6, invalid: true, inputId: 'trap-port', inputProps: { 'data-test': 'x' }})
    const inner = wrapper.findComponent({ name: 'InputNumber' })
    expect(inner.props('maxFractionDigits')).toBe(6)
    expect(inner.props('invalid')).toBe(true)
    expect(inner.props('inputId')).toBe('trap-port')
    // PrimeVue-reality: installed primevue@4.5.5's InputNumber does not
    // declare `inputProps` as a component prop at all (it exists only on
    // CascadeSelect, InputChips, Password and TreeSelect) — so
    // `inner.props('inputProps')` is always undefined regardless of what's
    // passed. The wrapper instead routes inputProps through
    // `pt.pcInputText.root`, PrimeVue's own forwarding path to the nested
    // InputText's rendered <input> (confirmed via DOM probe). Assert the
    // rendered attribute, same pattern as OnmsInputText's `placeholder` test.
    expect(wrapper.find('input').attributes('data-test')).toBe('x')
  })

  it('leaves fluid undefined by default (Fluid context inheritance)', () => {
    const wrapper = mountIt()
    expect(wrapper.props('fluid')).toBeUndefined()
  })

  it('forwards update:modelValue including null', async () => {
    const wrapper = mountIt({ modelValue: 5 })
    await wrapper.findComponent({ name: 'InputNumber' }).vm.$emit('update:modelValue', null)
    expect(wrapper.emitted('update:modelValue')).toEqual([[null]])
  })
})
