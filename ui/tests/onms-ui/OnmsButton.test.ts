import { OnmsButton } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

describe('OnmsButton', () => {
  it('renders the label through the inner button', () => {
    const wrapper = mount(OnmsButton, { props: { label: 'Save' }})
    expect(wrapper.find('button').text()).toBe('Save')
  })

  it('renders default slot content instead of label', () => {
    const wrapper = mount(OnmsButton, {
      props: { label: 'ignored' },
      slots: { default: '<span data-test="custom">X</span>' }
    })
    expect(wrapper.find('[data-test="custom"]').exists()).toBe(true)
  })

  it('maps variant to the inner PrimeVue flags', () => {
    const outlined = mount(OnmsButton, { props: { variant: 'outlined' }}).findComponent({ name: 'Button' })
    expect(outlined.props('outlined')).toBe(true)
    expect(outlined.props('text')).toBe(false)
    const text = mount(OnmsButton, { props: { variant: 'text' }}).findComponent({ name: 'Button' })
    expect(text.props('text')).toBe(true)
    const filled = mount(OnmsButton).findComponent({ name: 'Button' })
    expect(filled.props('outlined')).toBe(false)
    expect(filled.props('text')).toBe(false)
    // 'ghost' maps to both PrimeVue flags true: the bordered, transparent
    // "Cancel" style (PrimeVue's own `.p-button-outlined.p-button-text`
    // combo), see primevue-overrides.scss.
    const ghost = mount(OnmsButton, { props: { variant: 'ghost' }}).findComponent({ name: 'Button' })
    expect(ghost.props('text')).toBe(true)
    expect(ghost.props('outlined')).toBe(true)
  })

  it('maps danger severity and leaves primary as PrimeVue default', () => {
    const danger = mount(OnmsButton, { props: { severity: 'danger' }}).findComponent({ name: 'Button' })
    expect(danger.props('severity')).toBe('danger')
    const primary = mount(OnmsButton).findComponent({ name: 'Button' })
    // PrimeVue's own `severity` prop default is `null` (not `undefined`); Vue
    // applies that default whenever the bound value resolves to `undefined`,
    // so this is PrimeVue's untouched default, not a value OnmsButton forces.
    expect(primary.props('severity')).toBeNull()
  })

  it('forwards disabled and loading', () => {
    const wrapper = mount(OnmsButton, { props: { disabled: true, loading: true }})
    const inner = wrapper.findComponent({ name: 'Button' })
    // PrimeVue's Button does not declare `disabled` as a reactive component
    // prop (it derives it from `$attrs.disabled` internally), so it never
    // appears in `.props()`; assert the forwarded DOM attribute instead.
    expect(wrapper.find('button').attributes('disabled')).toBe('')
    expect(inner.props('loading')).toBe(true)
  })

  it('lets click and data-test fall through', async () => {
    const onClick = vi.fn()
    const wrapper = mount(OnmsButton, { attrs: { 'data-test': 'btn', onClick }})
    expect(wrapper.find('button').attributes('data-test')).toBe('btn')
    await wrapper.find('button').trigger('click')
    expect(onClick).toHaveBeenCalledTimes(1)
  })
})
