import { OnmsPassword } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's Password extends
// BaseInput, whose `$variant` computed reads `this.$primevue.config`
// unconditionally (no optional chaining). Mounting it without the PrimeVue
// config plugin installed throws "Cannot read properties of undefined
// (reading 'config')". The brief's mount calls are amended here with
// `global: { plugins: [PrimeVue] }` to reflect that real requirement.
const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsPassword', () => {
  it('renders a masked input bound to modelValue', async () => {
    const wrapper = mount(OnmsPassword, { props: { modelValue: '' }, global: globalPlugins })
    const input = wrapper.find('input')
    expect(input.attributes('type')).toBe('password')
    await input.setValue('s3cret')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['s3cret'])
  })

  it('always disables strength feedback and forwards inputId/invalid/toggleMask', () => {
    const inner = mount(OnmsPassword, {
      props: { inputId: 'pw', invalid: true, toggleMask: false },
      global: globalPlugins
    }).findComponent({ name: 'Password' })
    expect(inner.props('feedback')).toBe(false)
    expect(inner.props('inputId')).toBe('pw')
    expect(inner.props('invalid')).toBe(true)
    expect(inner.props('toggleMask')).toBe(false)
  })
})
