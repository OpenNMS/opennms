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

  it('renders the baked search icon before the input (leading icon)', () => {
    const wrapper = mountIt()
    expect(wrapper.findComponent({ name: 'InputIcon' }).exists()).toBe(true)
    const children = Array.from(wrapper.element.children) as Element[]
    expect(children[0]?.classList.contains('p-inputicon')).toBe(true)
    expect(children[1]?.tagName).toBe('INPUT')
  })

  it('emits update:modelValue on typing', async () => {
    const wrapper = mountIt({ modelValue: '' })
    await wrapper.find('input').setValue('snmp')
    expect(wrapper.emitted('update:modelValue')).toEqual([['snmp']])
  })

  it('shows no clear button while there is nothing to clear', () => {
    expect(mountIt({ modelValue: '' }).find('.onms-search-input__clear-button').exists()).toBe(false)
    expect(mountIt().find('.onms-search-input__clear-button').exists()).toBe(false)
  })

  it('clears the value via the trailing clear button', async () => {
    const wrapper = mountIt({ modelValue: 'snmp', dataTest: 'search-input' })
    const clear = wrapper.find('.onms-search-input__clear-button')
    expect(clear.exists()).toBe(true)
    expect(clear.attributes('aria-label')).toBe('Clear search')
    expect(clear.attributes('data-test')).toBe('search-input-clear')

    await clear.trigger('click')
    expect(wrapper.emitted('update:modelValue')).toEqual([['']])
    expect(wrapper.emitted('clear')).toEqual([[]])
  })

  it('exposes focus() for callers that need to focus the field', () => {
    // attached to the document: a detached input can't take focus
    const wrapper = mount(OnmsSearchInput, {
      props: { modelValue: '' },
      attachTo: document.body,
      global: { plugins: [PrimeVue] }
    })
    wrapper.vm.focus()
    expect(document.activeElement).toBe(wrapper.find('input').element)
    wrapper.unmount()
  })
})
