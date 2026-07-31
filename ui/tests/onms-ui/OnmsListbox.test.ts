import { OnmsListbox } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's Listbox extends
// BaseComponent, whose computed properties read `this.$primevue.config`
// unconditionally — same issue documented in OnmsSelect.test.ts /
// OnmsAutoComplete.test.ts. Mounting without the PrimeVue config plugin
// installed throws "Cannot read properties of undefined (reading
// 'config')". The brief's mount calls are amended here with
// `global: { plugins: [PrimeVue] }`; no other assertion changes.
const globalPlugins = { plugins: [PrimeVue] }

const options = ['a', 'b']

describe('OnmsListbox', () => {
  it('forwards options/filter/filterPlaceholder/listStyle', () => {
    const inner = mount(OnmsListbox, {
      props: { options, filter: true, filterPlaceholder: 'Search', listStyle: 'max-height: 200px' },
      global: globalPlugins
    }).findComponent({ name: 'Listbox' })
    expect(inner.props('options')).toEqual(options)
    expect(inner.props('filter')).toBe(true)
    expect(inner.props('filterPlaceholder')).toBe('Search')
    expect(inner.props('listStyle')).toBe('max-height: 200px')
  })

  it('re-emits update:modelValue from the inner listbox', () => {
    const wrapper = mount(OnmsListbox, { props: { options }, global: globalPlugins })
    wrapper.findComponent({ name: 'Listbox' }).vm.$emit('update:modelValue', 'b')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['b'])
  })

  it('re-emits change with the selected value only', async () => {
    const wrapper = mount(OnmsListbox, {
      props: { options: ['a', 'b'] },
      global: { plugins: [PrimeVue] }
    })
    await wrapper.findComponent({ name: 'Listbox' }).vm.$emit('change', { originalEvent: new Event('click'), value: 'b' })
    expect(wrapper.emitted('change')).toEqual([['b']])
  })
})
