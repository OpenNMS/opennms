import FormField from '@/components/Common/FormField.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { h } from 'vue'

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

  it('marks the error as an alert with an id derived from for', () => {
    const wrapper = mount(FormField, { props: { label: 'Alias', for: 'scv-alias', error: 'Bad alias' }})
    const small = wrapper.find('.field-error')
    expect(small.attributes('role')).toBe('alert')
    expect(small.attributes('id')).toBe('scv-alias-error')
  })

  it('omits the error id when no for is provided', () => {
    const wrapper = mount(FormField, { props: { label: 'Alias', error: 'Bad alias' }})
    expect(wrapper.find('.field-error').attributes('id')).toBeUndefined()
  })

  it('exposes errorId and invalid to the default slot when there is an error', () => {
    const wrapper = mount(FormField, {
      props: { label: 'Alias', for: 'scv-alias', error: 'Bad alias' },
      slots: {
        default: (sp: any) => h('input', {
          'data-test': 'control',
          'aria-describedby': sp.errorId,
          'data-invalid': String(sp.invalid)
        })
      }
    })
    const input = wrapper.find('input[data-test="control"]')
    expect(input.attributes('aria-describedby')).toBe('scv-alias-error')
    expect(input.attributes('data-invalid')).toBe('true')
  })

  it('exposes undefined errorId and invalid=false to the slot when there is no error', () => {
    const wrapper = mount(FormField, {
      props: { label: 'Alias', for: 'scv-alias' },
      slots: {
        default: (sp: any) => h('input', {
          'data-test': 'control',
          'aria-describedby': sp.errorId,
          'data-invalid': String(sp.invalid)
        })
      }
    })
    const input = wrapper.find('input[data-test="control"]')
    expect(input.attributes('aria-describedby')).toBeUndefined()
    expect(input.attributes('data-invalid')).toBe('false')
  })

  it('auto-associates the error with a slotted input even when the consumer does not wire aria-describedby', async () => {
    const wrapper = mount(FormField, {
      props: { label: 'Port', for: 'trap-port', error: 'Port is required' },
      slots: { default: '<input data-test="control" />' }
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('input[data-test="control"]').attributes('aria-describedby')).toBe('trap-port-error')
  })

  it('removes the auto-associated aria-describedby when the error clears', async () => {
    const wrapper = mount(FormField, {
      props: { label: 'Port', for: 'trap-port', error: 'Port is required' },
      slots: { default: '<input data-test="control" />' }
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('input[data-test="control"]').attributes('aria-describedby')).toBe('trap-port-error')

    await wrapper.setProps({ error: undefined })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('input[data-test="control"]').attributes('aria-describedby')).toBeUndefined()
  })

  it('renders no label element when label is omitted', () => {
    const wrapper = mount(FormField, {
      props: { for: 'x', error: 'Bad' },
      slots: { default: '<input data-test="control" />' }
    })
    expect(wrapper.find('label.form-field__label').exists()).toBe(false)
    // control + error still render
    expect(wrapper.find('input[data-test="control"]').exists()).toBe(true)
    expect(wrapper.find('.field-error').text()).toBe('Bad')
  })

  it('still renders the label element when label is provided', () => {
    const wrapper = mount(FormField, { props: { label: 'Name' }})
    expect(wrapper.find('label.form-field__label').text()).toContain('Name')
  })
})
