///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { OnmsButton, OnmsDatePicker } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import { getUnixTime } from 'date-fns'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it } from 'vitest'

import TimeControls from '@/components/Common/TimeControls.vue'

// OnmsPopover renders its slot only while the overlay is open, and appends the
// panel to <body>. Stubbing it keeps this test on TimeControls' own custom-range
// arithmetic instead of PrimeVue's overlay lifecycle; toggle/hide are stubbed
// because the component calls them through the template ref.
const popoverHideCalls = { count: 0 }

const OnmsPopoverStub = {
  template: '<div><slot /></div>',
  methods: {
    toggle: () => undefined,
    hide: () => {
      popoverHideCalls.count++
    }
  }
}

const mountControls = (props: Record<string, unknown> = {}) => mount(TimeControls, {
  props,
  global: {
    plugins: [PrimeVue],
    stubs: { OnmsPopover: OnmsPopoverStub }
  }
})

/** Drive the two custom-range pickers the way a user's selections would. */
const pick = async (wrapper: ReturnType<typeof mountControls>, start: Date | null, end: Date | null) => {
  const pickers = wrapper.findAllComponents(OnmsDatePicker)
  expect(pickers).toHaveLength(2)

  await pickers[0].vm.$emit('update:modelValue', start)
  await pickers[1].vm.$emit('update:modelValue', end)
}

const applyButton = (wrapper: ReturnType<typeof mountControls>) =>
  wrapper.findAll('button').find(button => button.text().includes('Apply custom time'))!

describe('TimeControls custom range', () => {
  let wrapper: ReturnType<typeof mountControls>

  beforeEach(() => {
    popoverHideCalls.count = 0
    wrapper = mountControls()
  })

  it('offers one date-and-time picker per range end', () => {
    const pickers = wrapper.findAllComponents(OnmsDatePicker)

    expect(pickers).toHaveLength(2)
    for (const picker of pickers) {
      expect(picker.props('showTime')).toBe(true)
      expect(picker.props('hourFormat')).toBe('12')
    }
  })

  it('emits the picked instants, time of day included', async () => {
    const start = new Date(2026, 8, 1, 13, 30)
    const end = new Date(2026, 8, 2, 9, 45)

    await pick(wrapper, start, end)
    await applyButton(wrapper).trigger('click')

    expect(wrapper.emitted('updateTime')).toEqual([[{
      startTime: getUnixTime(start),
      endTime: getUnixTime(end),
      format: 'hours'
    }]])
  })

  it('labels a sub-hour window as minutes', async () => {
    const start = new Date(2026, 8, 1, 13, 0)

    await pick(wrapper, start, new Date(2026, 8, 1, 13, 30))
    await applyButton(wrapper).trigger('click')

    expect(wrapper.emitted('updateTime')![0][0]).toMatchObject({ format: 'minutes' })
  })

  it('labels a multi-day window as days', async () => {
    await pick(wrapper, new Date(2026, 8, 1, 13, 0), new Date(2026, 8, 5, 13, 0))
    await applyButton(wrapper).trigger('click')

    expect(wrapper.emitted('updateTime')![0][0]).toMatchObject({ format: 'days' })
  })

  // An absolute window must never carry `range`, or the consumer would slide it
  // forward on every refresh.
  it('emits no relative range for an absolute window', async () => {
    await pick(wrapper, new Date(2026, 8, 1, 13, 0), new Date(2026, 8, 2, 13, 0))
    await applyButton(wrapper).trigger('click')

    expect(wrapper.emitted('updateTime')![0][0]).not.toHaveProperty('range')
  })

  it('disables apply until both instants are picked', async () => {
    expect(applyButton(wrapper).attributes('disabled')).toBeDefined()

    await pick(wrapper, new Date(2026, 8, 1, 13, 0), null)
    expect(applyButton(wrapper).attributes('disabled')).toBeDefined()

    await pick(wrapper, new Date(2026, 8, 1, 13, 0), new Date(2026, 8, 2, 13, 0))
    expect(applyButton(wrapper).attributes('disabled')).toBeUndefined()
  })

  it('bounds each picker by the other, so a range cannot invert', async () => {
    const start = new Date(2026, 8, 1, 13, 0)
    const end = new Date(2026, 8, 2, 9, 0)

    await pick(wrapper, start, end)
    const [startPicker, endPicker] = wrapper.findAllComponents(OnmsDatePicker)

    expect(startPicker.props('maxDate')).toEqual(end)
    expect(endPicker.props('minDate')).toEqual(start)
  })

  it('renders apply as a ghost button', () => {
    const apply = wrapper.findAllComponents(OnmsButton)
      .find(button => button.text().includes('Apply custom time'))!

    expect(apply.props('variant')).toBe('ghost')
  })
})

describe('TimeControls preset options', () => {
  beforeEach(() => {
    popoverHideCalls.count = 0
  })

  const firstOption = (wrapper: ReturnType<typeof mountControls>) =>
    wrapper.findAll('.options-col .list-item')[0]

  it('selects a preset and closes the popover', async () => {
    const wrapper = mountControls()
    const option = firstOption(wrapper)

    await option.trigger('mousedown')
    await option.trigger('click')

    expect(wrapper.emitted('updateTime')).toHaveLength(1)
    expect(popoverHideCalls.count).toBe(1)
  })

  // PrimeVue's DatePicker unbinds its overlay on document MOUSEDOWN, so by the
  // time this click fires the panel is already gone and `datePickerOpen` reads
  // false. The suppression therefore has to latch on mousedown, which reaches
  // the <li> target before the document-level listener bubbles. A test that
  // only triggered 'click' would pass against a naive click-time check.
  it('ignores a preset click that only dismissed an open picker', async () => {
    const wrapper = mountControls()
    await wrapper.findAllComponents(OnmsDatePicker)[0].vm.$emit('show')

    const option = firstOption(wrapper)
    await option.trigger('mousedown')
    await wrapper.findAllComponents(OnmsDatePicker)[0].vm.$emit('hide')
    await option.trigger('click')

    expect(wrapper.emitted('updateTime')).toBeUndefined()
    expect(popoverHideCalls.count).toBe(0)
  })

  it('selects a preset on the next click after the picker is dismissed', async () => {
    const wrapper = mountControls()
    const pickers = wrapper.findAllComponents(OnmsDatePicker)
    const option = firstOption(wrapper)

    await pickers[0].vm.$emit('show')
    await option.trigger('mousedown')
    await pickers[0].vm.$emit('hide')
    await option.trigger('click')

    await option.trigger('mousedown')
    await option.trigger('click')

    expect(wrapper.emitted('updateTime')).toHaveLength(1)
    expect(popoverHideCalls.count).toBe(1)
  })

  // An unmatched hide must not drive the count below zero: a later show would
  // then only bring it back to 0, the latch would read "no picker open" while
  // one is on screen, and the preset click would be taken as a selection. Only
  // `> 0` is read, so an unclamped decrement fails silently rather than loudly.
  it('survives a hide with no matching show', async () => {
    const wrapper = mountControls()
    const picker = wrapper.findAllComponents(OnmsDatePicker)[0]

    await picker.vm.$emit('hide')
    await picker.vm.$emit('show')

    const option = firstOption(wrapper)
    await option.trigger('mousedown')
    await option.trigger('click')

    expect(wrapper.emitted('updateTime')).toBeUndefined()
    expect(popoverHideCalls.count).toBe(0)
  })

  // Two pickers share one flag; the second closing must not be masked by the
  // first, nor the flag left latched while one is still open.
  it('stays suppressed while the second picker is still open', async () => {
    const wrapper = mountControls()
    const pickers = wrapper.findAllComponents(OnmsDatePicker)

    await pickers[0].vm.$emit('show')
    await pickers[1].vm.$emit('show')
    await pickers[0].vm.$emit('hide')

    const option = firstOption(wrapper)
    await option.trigger('mousedown')
    await option.trigger('click')

    expect(wrapper.emitted('updateTime')).toBeUndefined()
  })
})

describe('TimeControls label', () => {
  it('renders no label by default', () => {
    const wrapper = mountControls()

    expect(wrapper.find('.time-range-label').exists()).toBe(false)
  })

  // The label has to live here rather than in each consumer: aria-labelledby on
  // the component from outside would land on TimeControls' plain root div, where
  // it is inert. Inside, it can be put on the actual trigger button.
  it('renders the label and names the trigger button with it', () => {
    const wrapper = mountControls({ label: 'Time Range:' })
    const label = wrapper.find('.time-range-label')

    expect(label.text()).toBe('Time Range:')
    expect(label.attributes('id')).toBeTruthy()
    // The label leads the accessible name; the exact composition (label id then
    // the button's own id) is asserted in the accessible-name test below.
    expect(wrapper.get('button[aria-haspopup="true"]').attributes('aria-labelledby'))
      .toMatch(new RegExp(`^${label.attributes('id')}\\b`))
  })

  it('leaves the trigger button unnamed when there is no label', () => {
    const wrapper = mountControls()

    expect(wrapper.get('button[aria-haspopup="true"]').attributes('aria-labelledby'))
      .toBeUndefined()
  })

  // Two instances must be mounted in ONE app: Vue's useId counter is per app
  // instance, so two separate mount() calls would both yield 'v-0' and the
  // assertion would be vacuous.
  // aria-labelledby REPLACES the element's content as its accessible name, so
  // pointing it at the label span alone renamed the button from "Last day" to
  // "Time Range:" -- the selected range, the only changing information on the
  // button, stopped being announced. Referencing the button's own id after the
  // label's concatenates the two.
  it('names the trigger button with the label AND the selected range', () => {
    const wrapper = mountControls({ label: 'Time Range:' })
    const button = wrapper.get('button[aria-haspopup="true"]')
    const labelId = wrapper.get('.time-range-label').attributes('id')

    expect(button.attributes('id')).toBeTruthy()
    expect(button.attributes('aria-labelledby'))
      .toBe(`${labelId} ${button.attributes('id')}`)
    expect(button.text()).toContain('Last day')
  })

  it('gives each instance on a page its own label id', () => {
    const Host = {
      components: { TimeControls },
      template: '<div><TimeControls label="From:" /><TimeControls label="To:" /></div>'
    }
    const wrapper = mount(Host, {
      global: {
        plugins: [PrimeVue],
        stubs: { OnmsPopover: OnmsPopoverStub }
      }
    })

    const [first, second] = wrapper.findAll('.time-range-label')

    expect(first.attributes('id')).toBeTruthy()
    expect(first.attributes('id')).not.toBe(second.attributes('id'))
  })
})

describe('TimeControls range validity', () => {
  const applyButton = (wrapper: ReturnType<typeof mountControls>) =>
    wrapper.findAll('button').find(button => button.text().includes('Apply custom time'))!

  const pickRange = async (wrapper: ReturnType<typeof mountControls>, start: Date, end: Date) => {
    const pickers = wrapper.findAllComponents(OnmsDatePicker)
    await pickers[0].vm.$emit('update:modelValue', start)
    await pickers[1].vm.$emit('update:modelValue', end)
  }

  // minDate/maxDate cross-wiring is NOT sufficient. PrimeVue's isSelectable
  // compares only year/month/day, and isValidSelection (the typed-input path,
  // with manualInput defaulting to true) goes through it -- so typing a later
  // time on the boundary DAY into the start field is accepted unclamped.
  it('rejects an inverted range', async () => {
    const wrapper = mountControls()

    await pickRange(wrapper, new Date(2026, 8, 2, 23, 30), new Date(2026, 8, 2, 9, 0))

    expect(applyButton(wrapper).attributes('disabled')).toBeDefined()
  })

  // A calendar click DOES clamp at full granularity (selectDate assigns
  // date = maxDate), so clicking a too-late day in the start field lands
  // exactly on the end instant. That zero-width window is the likely outcome,
  // not the inverted one.
  it('rejects a zero-width range', async () => {
    const wrapper = mountControls()
    const instant = new Date(2026, 8, 2, 9, 0)

    await pickRange(wrapper, instant, new Date(instant))

    expect(applyButton(wrapper).attributes('disabled')).toBeDefined()
  })

  it('emits nothing for an invalid range', async () => {
    const wrapper = mountControls()

    await pickRange(wrapper, new Date(2026, 8, 2, 23, 30), new Date(2026, 8, 2, 9, 0))
    await applyButton(wrapper).trigger('click')

    expect(wrapper.emitted('updateTime')).toBeUndefined()
  })

  it('explains why an invalid range is rejected', async () => {
    const wrapper = mountControls()

    await pickRange(wrapper, new Date(2026, 8, 2, 23, 30), new Date(2026, 8, 2, 9, 0))

    expect(wrapper.text()).toContain('End must be after start')
  })

  it('accepts a well-ordered range', async () => {
    const wrapper = mountControls()

    await pickRange(wrapper, new Date(2026, 8, 1, 13, 0), new Date(2026, 8, 2, 9, 0))

    expect(applyButton(wrapper).attributes('disabled')).toBeUndefined()
    expect(wrapper.text()).not.toContain('End must be after start')
  })

  it('labels each range field for its own picker input', () => {
    const wrapper = mountControls()
    const labels = wrapper.findAll('.custom-col label[for]')
    const inputIds = wrapper.findAll('.custom-col input').map(input => input.attributes('id'))

    expect(labels).toHaveLength(2)
    for (const label of labels) {
      expect(inputIds).toContain(label.attributes('for'))
    }
    expect(labels[0].attributes('for')).not.toBe(labels[1].attributes('for'))
  })
})
