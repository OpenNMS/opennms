import { OnmsCheckbox } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsCheckbox', () => {
  it('is always binary and toggles the boolean model', async () => {
    const wrapper = mount(OnmsCheckbox, { props: { modelValue: false }, global: globalPlugins })
    expect(wrapper.findComponent({ name: 'Checkbox' }).props('binary')).toBe(true)
    await wrapper.find('input[type="checkbox"]').setValue(true)
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([true])
  })

  it('forwards inputId and disabled', () => {
    const inner = mount(OnmsCheckbox, {
      props: { modelValue: true, inputId: 'cb1', disabled: true },
      global: globalPlugins
    }).findComponent({ name: 'Checkbox' })
    expect(inner.props('inputId')).toBe('cb1')
    expect(inner.props('disabled')).toBe(true)
  })
})
