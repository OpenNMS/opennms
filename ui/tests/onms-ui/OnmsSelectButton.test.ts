import { OnmsSelectButton } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const options = [
  { label: 'View', value: 'view' },
  { label: 'Edit', value: 'edit' }
]

const mountIt = (props: Record<string, unknown> = {}) =>
  mount(OnmsSelectButton, {
    props: { options, optionLabel: 'label', optionValue: 'value', ...props },
    global: { plugins: [PrimeVue] }
  })

describe('OnmsSelectButton contract', () => {
  it('maps props onto PrimeVue SelectButton', () => {
    const inner = mountIt({ modelValue: 'view' }).findComponent({ name: 'SelectButton' })
    expect(inner.props('modelValue')).toBe('view')
    expect(inner.props('options')).toEqual(options)
    expect(inner.props('optionLabel')).toBe('label')
    expect(inner.props('optionValue')).toBe('value')
  })

  it('defaults allowEmpty to false, unlike PrimeVue', () => {
    expect(mountIt().findComponent({ name: 'SelectButton' }).props('allowEmpty')).toBe(false)
  })

  it('honors an explicit allowEmpty', () => {
    expect(mountIt({ allowEmpty: true }).findComponent({ name: 'SelectButton' }).props('allowEmpty')).toBe(true)
  })

  it('forwards update:modelValue', async () => {
    const wrapper = mountIt({ modelValue: 'view' })
    wrapper.findComponent({ name: 'SelectButton' }).vm.$emit('update:modelValue', 'edit')
    expect(wrapper.emitted('update:modelValue')).toEqual([['edit']])
  })

  it('emits the selected value on change, not PrimeVue\'s event object', () => {
    const wrapper = mountIt({ modelValue: 'view' })
    wrapper.findComponent({ name: 'SelectButton' }).vm.$emit('change', { originalEvent: new Event('click'), value: 'edit' })
    expect(wrapper.emitted('change')).toEqual([['edit']])
  })

  it('falls DOM attrs through to the root', () => {
    expect(mountIt({ 'aria-label': 'Mode' }).attributes('aria-label')).toBe('Mode')
  })
})
