import { OnmsAutoComplete } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's AutoComplete
// extends BaseInput, whose computed properties (searchMessage,
// emptySearchMessage, etc.) read `this.$primevue.config.locale` unconditionally
// — same issue documented in OnmsInputText.test.ts / OnmsSelect.test.ts.
// Mounting without the PrimeVue config plugin installed throws "Cannot read
// properties of undefined (reading 'config')". The brief's mount calls are
// amended here with `global: { plugins: [PrimeVue] }`; no other assertion
// changes.
const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsAutoComplete', () => {
  it('forwards suggestions/optionLabel/forceSelection and re-emits model updates', () => {
    const suggestions = [{ label: 'node1' }]
    const wrapper = mount(OnmsAutoComplete, {
      props: { suggestions, optionLabel: 'label', forceSelection: true, invalid: true },
      global: globalPlugins
    })
    const inner = wrapper.findComponent({ name: 'AutoComplete' })
    expect(inner.props('suggestions')).toEqual(suggestions)
    expect(inner.props('optionLabel')).toBe('label')
    expect(inner.props('forceSelection')).toBe(true)
    expect(inner.props('invalid')).toBe(true)
    inner.vm.$emit('update:modelValue', 'node1')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['node1'])
  })

  it('emits complete with the query string and optionSelect with the value', () => {
    const wrapper = mount(OnmsAutoComplete, { props: { suggestions: [] }, global: globalPlugins })
    const inner = wrapper.findComponent({ name: 'AutoComplete' })
    inner.vm.$emit('complete', { originalEvent: new Event('input'), query: 'nod' })
    expect(wrapper.emitted('complete')![0]).toEqual(['nod'])
    inner.vm.$emit('option-select', { originalEvent: new Event('click'), value: { label: 'node1' }})
    expect(wrapper.emitted('optionSelect')![0]).toEqual([{ label: 'node1' }])
  })

  it('forwards dropdown/multiple/fluid to the inner AutoComplete', () => {
    const wrapper = mount(OnmsAutoComplete, {
      props: { suggestions: [], dropdown: true, multiple: true, fluid: true },
      global: globalPlugins
    })
    const inner = wrapper.findComponent({ name: 'AutoComplete' })
    expect(inner.props('dropdown')).toBe(true)
    expect(inner.props('multiple')).toBe(true)
    expect(inner.props('fluid')).toBe(true)
  })

  it('forwards the empty slot', () => {
    const wrapper = mount(OnmsAutoComplete, {
      props: { suggestions: [] },
      slots: { empty: '<div data-test="empty-msg">No results</div>' },
      global: globalPlugins
    })
    expect(wrapper.findComponent({ name: 'AutoComplete' }).vm.$slots.empty).toBeTruthy()
  })

  it('exposes clearInput() to wipe typed-but-unselected text, and focus()', async () => {
    const wrapper = mount(OnmsAutoComplete, {
      props: { suggestions: [], multiple: true },
      attachTo: document.body,
      global: globalPlugins
    })
    const input = wrapper.find('input')
    await input.setValue('node1')
    expect((input.element as HTMLInputElement).value).toBe('node1')

    // multiple mode leaves the query in the DOM, so there is no model to reset
    expect(wrapper.emitted('update:modelValue')).toBeUndefined()

    wrapper.vm.clearInput()
    expect((input.element as HTMLInputElement).value).toBe('')

    wrapper.vm.focus()
    expect(document.activeElement).toBe(input.element)
    wrapper.unmount()
  })

  // Regression: clearInput() has to go through PrimeVue's own onInput handler.
  // Typing arms a `delay`-ms timer inside AutoComplete, and only that handler
  // cancels it — assigning the DOM value silently would leave the timer running,
  // so a clear inside the delay window still delivered the stale query to the
  // caller, which then re-ran the search it had just cancelled.
  it('cancels the pending suggestion fetch when clearInput() runs', async () => {
    vi.useFakeTimers()

    try {
      const wrapper = mount(OnmsAutoComplete, {
        props: { suggestions: [], multiple: true },
        global: globalPlugins
      })
      await wrapper.find('input').setValue('node1')

      wrapper.vm.clearInput()
      vi.advanceTimersByTime(1000)

      expect(wrapper.emitted('complete')).toBeUndefined()
      wrapper.unmount()
    } finally {
      vi.useRealTimers()
    }
  })

  it('forwards the option slot for custom suggestion rendering', () => {
    const wrapper = mount(OnmsAutoComplete, {
      props: { suggestions: [{ label: 'node1' }] },
      slots: { option: '<div data-test="opt">custom</div>' },
      global: globalPlugins
    })
    expect(wrapper.findComponent({ name: 'AutoComplete' }).vm.$slots.option).toBeTruthy()
  })
})
