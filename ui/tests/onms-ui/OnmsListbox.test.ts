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

  it('forwards the multi-select and option-shape props', () => {
    const objectOptions = [{ id: 'a', name: 'A', group: 'G' }]
    const inner = mount(OnmsListbox, {
      props: {
        options: objectOptions,
        multiple: true,
        optionLabel: 'name',
        optionValue: 'id',
        optionDisabled: 'locked',
        optionGroupLabel: 'group',
        optionGroupChildren: 'items',
        dataKey: 'id',
        checkmark: true,
        filterFields: ['name', 'group'],
        scrollHeight: '18rem',
        virtualScrollerOptions: { itemSize: 52 },
        emptyMessage: 'Nothing here',
        emptyFilterMessage: 'No matches',
        disabled: true
      },
      global: globalPlugins
    }).findComponent({ name: 'Listbox' })
    expect(inner.props('multiple')).toBe(true)
    expect(inner.props('optionLabel')).toBe('name')
    expect(inner.props('optionValue')).toBe('id')
    expect(inner.props('optionDisabled')).toBe('locked')
    expect(inner.props('optionGroupLabel')).toBe('group')
    expect(inner.props('optionGroupChildren')).toBe('items')
    expect(inner.props('dataKey')).toBe('id')
    expect(inner.props('checkmark')).toBe(true)
    expect(inner.props('filterFields')).toEqual(['name', 'group'])
    expect(inner.props('scrollHeight')).toBe('18rem')
    expect(inner.props('virtualScrollerOptions')).toEqual({ itemSize: 52 })
    expect(inner.props('emptyMessage')).toBe('Nothing here')
    expect(inner.props('emptyFilterMessage')).toBe('No matches')
    expect(inner.props('disabled')).toBe(true)
  })

  // Plain clicking must toggle a selection in a multi-select picker; PrimeVue's own
  // default of requiring ctrl/cmd is the behaviour users report as broken.
  it('defaults metaKeySelection to false', () => {
    const inner = mount(OnmsListbox, {
      props: { options, multiple: true },
      global: globalPlugins
    }).findComponent({ name: 'Listbox' })
    expect(inner.props('metaKeySelection')).toBe(false)
  })

  it('forwards the option slot', () => {
    const wrapper = mount(OnmsListbox, {
      props: { options },
      slots: { option: '<span class="custom-option">custom</span>' },
      global: globalPlugins
    })
    expect(wrapper.findAll('.custom-option').length).toBe(options.length)
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
