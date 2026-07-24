import { OnmsTextarea } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's Textarea extends
// BaseInput, whose `$variant` computed reads `this.$primevue.config`
// unconditionally (no optional chaining). Mounting it without the PrimeVue
// config plugin installed throws "Cannot read properties of undefined
// (reading 'config')". The brief's mount calls are amended here with
// `global: { plugins: [PrimeVue] }` to reflect that real requirement.
const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsTextarea', () => {
  it('binds modelValue and emits updates', async () => {
    const wrapper = mount(OnmsTextarea, { props: { modelValue: '' }, global: globalPlugins })
    await wrapper.find('textarea').setValue('hello')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['hello'])
  })

  it('applies rows and invalid', () => {
    const wrapper = mount(OnmsTextarea, { props: { rows: 5, invalid: true }, global: globalPlugins })
    expect(wrapper.find('textarea').attributes('rows')).toBe('5')
    expect(wrapper.findComponent({ name: 'Textarea' }).props('invalid')).toBe(true)
  })
})
