import { OnmsDatePicker } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

// NOTE (PrimeVue-reality rule): installed primevue@4.5.5's DatePicker
// extends BaseComponent, whose computed properties read `this.$primevue.config`
// unconditionally — same issue documented in OnmsSelect.test.ts /
// OnmsAutoComplete.test.ts. Mounting without the PrimeVue config plugin
// installed throws "Cannot read properties of undefined (reading
// 'config')". The brief's mount calls are amended here with
// `global: { plugins: [PrimeVue] }`; no other assertion changes.
const globalPlugins = { plugins: [PrimeVue] }

describe('OnmsDatePicker', () => {
  it('maps modelValue and forwards updates', async () => {
    const d = new Date(2026, 6, 27)
    const wrapper = mount(OnmsDatePicker, {
      props: { modelValue: d },
      global: { plugins: [PrimeVue] }
    })
    expect(wrapper.findComponent({ name: 'DatePicker' }).props('modelValue')).toEqual(d)
    await wrapper.findComponent({ name: 'DatePicker' }).vm.$emit('update:modelValue', null)
    expect(wrapper.emitted('update:modelValue')).toEqual([[null]])
  })

  it('defaults modelValue to null', () => {
    const wrapper = mount(OnmsDatePicker, { global: globalPlugins })
    expect(wrapper.findComponent({ name: 'DatePicker' }).props('modelValue')).toBe(null)
  })

  // Seam rule 2: the public API is the DECLARED props only. Asserting via
  // wrapper.props() (which lists declared props, not $attrs) is deliberate --
  // an undeclared attr still reaches the inner DatePicker by fallthrough, so
  // asserting only on the inner component would pass without a real contract.
  it('declares the time props with PrimeVue-matching defaults', () => {
    const wrapper = mount(OnmsDatePicker, { global: globalPlugins })
    const inner = wrapper.findComponent({ name: 'DatePicker' })

    expect(wrapper.props()).toMatchObject({
      showTime: false,
      timeOnly: false,
      hourFormat: '24',
      showSeconds: false,
      stepHour: 1,
      stepMinute: 1,
      stepSecond: 1
    })
    expect(inner.props('showTime')).toBe(false)
    expect(inner.props('hourFormat')).toBe('24')
  })

  it('declares and forwards the time props', () => {
    const wrapper = mount(OnmsDatePicker, {
      props: {
        showTime: true,
        hourFormat: '12',
        showSeconds: true,
        stepHour: 2,
        stepMinute: 15,
        stepSecond: 30
      },
      global: globalPlugins
    })
    const inner = wrapper.findComponent({ name: 'DatePicker' })

    expect(wrapper.props()).toMatchObject({
      showTime: true,
      hourFormat: '12',
      showSeconds: true,
      stepHour: 2,
      stepMinute: 15,
      stepSecond: 30
    })
    expect(inner.props('showTime')).toBe(true)
    expect(inner.props('hourFormat')).toBe('12')
    expect(inner.props('showSeconds')).toBe(true)
    expect(inner.props('stepHour')).toBe(2)
    expect(inner.props('stepMinute')).toBe(15)
    expect(inner.props('stepSecond')).toBe(30)
  })

  // timeOnly needs no companion showTime: PrimeVue gates the time panel on
  // `showTime || timeOnly` and the date panel on `!timeOnly`.
  it('declares and forwards timeOnly on its own', () => {
    const wrapper = mount(OnmsDatePicker, {
      props: { timeOnly: true, showSeconds: true },
      global: globalPlugins
    })

    expect(wrapper.props('timeOnly')).toBe(true)
    expect(wrapper.findComponent({ name: 'DatePicker' }).props('timeOnly')).toBe(true)
  })

  it('declares and forwards placeholder and the date bounds', () => {
    const min = new Date(2026, 0, 1)
    const max = new Date(2026, 11, 31)
    const wrapper = mount(OnmsDatePicker, {
      props: { placeholder: 'From', minDate: min, maxDate: max },
      global: globalPlugins
    })
    const inner = wrapper.findComponent({ name: 'DatePicker' })

    expect(wrapper.props()).toMatchObject({ placeholder: 'From', minDate: min, maxDate: max })
    expect(inner.props('placeholder')).toBe('From')
    expect(inner.props('minDate')).toEqual(min)
    expect(inner.props('maxDate')).toEqual(max)
  })

  // Consumers hold their bounds in nullable refs (TimeControls' startDateRef /
  // endDateRef). PrimeVue types minDate/maxDate as `Date | undefined` and treats
  // any non-undefined value as a real bound, so a null must not reach it.
  it('normalizes null date bounds to undefined', () => {
    const inner = mount(OnmsDatePicker, {
      props: { minDate: null, maxDate: null },
      global: globalPlugins
    }).findComponent({ name: 'DatePicker' })

    expect(inner.props('minDate')).toBeUndefined()
    expect(inner.props('maxDate')).toBeUndefined()
  })

  // Overlay-visibility events, in framework-neutral vocabulary. TimeControls
  // needs them to know whether a picker panel is on screen; without them a
  // consumer's only route is reaching into PrimeVue internals.
  it('forwards the overlay show and hide events', async () => {
    const wrapper = mount(OnmsDatePicker, { global: globalPlugins })
    const inner = wrapper.findComponent({ name: 'DatePicker' })

    await inner.vm.$emit('show')
    await inner.vm.$emit('hide')

    expect(wrapper.emitted('show')).toHaveLength(1)
    expect(wrapper.emitted('hide')).toHaveLength(1)
  })
})
