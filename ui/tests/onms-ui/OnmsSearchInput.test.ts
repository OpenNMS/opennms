import { OnmsSearchInput } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('OnmsSearchInput contract', () => {
  const mountIt = (props: Record<string, unknown> = {}) => mount(OnmsSearchInput, {
    props,
    global: { plugins: [PrimeVue] }
  })

  it('renders an IconField wrapping an input with mapped attrs', () => {
    const wrapper = mountIt({ modelValue: 'abc', placeholder: 'Search profiles', inputId: 'profile-search', ariaLabel: 'Search', dataTest: 'search-input' })
    expect(wrapper.findComponent({ name: 'IconField' }).exists()).toBe(true)
    const input = wrapper.find('input')
    expect(input.attributes('id')).toBe('profile-search')
    expect(input.attributes('placeholder')).toBe('Search profiles')
    expect(input.attributes('aria-label')).toBe('Search')
    expect(input.attributes('data-test')).toBe('search-input')
    expect((input.element as HTMLInputElement).value).toBe('abc')
  })

  it('renders the baked search glyph after the input (trailing icon)', () => {
    const wrapper = mountIt()
    expect(wrapper.findComponent({ name: 'InputIcon' }).exists()).toBe(true)
    expect(wrapper.find('svg').exists()).toBe(true)
  })

  it('emits update:modelValue on typing', async () => {
    const wrapper = mountIt({ modelValue: '' })
    await wrapper.find('input').setValue('snmp')
    expect(wrapper.emitted('update:modelValue')).toEqual([['snmp']])
  })
})
