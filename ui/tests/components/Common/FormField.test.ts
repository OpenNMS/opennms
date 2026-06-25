import FormField from '@/components/Common/FormField.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

describe('FormField.vue', () => {
  it('renders the label text', () => {
    const wrapper = mount(FormField, { props: { label: 'Port' }})
    expect(wrapper.find('label.form-field__label').text()).toContain('Port')
  })

  it('associates the label with the control via the for prop', () => {
    const wrapper = mount(FormField, { props: { label: 'Port', for: 'trap-port' }})
    expect(wrapper.find('label').attributes('for')).toBe('trap-port')
  })

  it('shows the required asterisk only when required is true', () => {
    const without = mount(FormField, { props: { label: 'Port' }})
    expect(without.find('.form-field__required').exists()).toBe(false)

    const withRequired = mount(FormField, { props: { label: 'Port', required: true }})
    expect(withRequired.find('.form-field__required').text()).toBe('*')
  })

  it('renders the default slot content (the control)', () => {
    const wrapper = mount(FormField, {
      props: { label: 'Port' },
      slots: { default: '<input data-test="control" />' }
    })
    expect(wrapper.find('input[data-test="control"]').exists()).toBe(true)
  })

  it('renders the hint when there is no error', () => {
    const wrapper = mount(FormField, { props: { label: 'Port', hint: 'Default: 10162' }})
    expect(wrapper.find('.field-hint').text()).toBe('Default: 10162')
    expect(wrapper.find('.field-error').exists()).toBe(false)
  })

  it('renders the error and hides the hint when both are set', () => {
    const wrapper = mount(FormField, {
      props: { label: 'Port', hint: 'Default: 10162', error: 'Port is required' }
    })
    expect(wrapper.find('.field-error').text()).toBe('Port is required')
    expect(wrapper.find('.field-hint').exists()).toBe(false)
  })
})
