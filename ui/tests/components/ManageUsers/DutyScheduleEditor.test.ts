import DutyScheduleEditor from '@/components/ManageUsers/DutyScheduleEditor.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, it, expect } from 'vitest'

describe('DutyScheduleEditor.vue', () => {
  const mountEditor = (modelValue: string[]) =>
    mount(DutyScheduleEditor, { props: { modelValue }, global: { plugins: [PrimeVue] }})

  it('parses a schedule string into day toggles and times', () => {
    const wrapper = mountEditor(['MoWeFr800-1700'])

    expect((wrapper.find('[data-test="duty-0-begin"]').element as HTMLInputElement).value).toBe('08:00')
    expect((wrapper.find('[data-test="duty-0-end"]').element as HTMLInputElement).value).toBe('17:00')
    expect(wrapper.find('[data-test="duty-0-day-Mo"]').attributes('aria-pressed')).toBe('true')
    expect(wrapper.find('[data-test="duty-0-day-We"]').attributes('aria-pressed')).toBe('true')
    expect(wrapper.find('[data-test="duty-0-day-Tu"]').attributes('aria-pressed')).toBe('false')
  })

  it('emits a canonical string when a day is toggled', async () => {
    const wrapper = mountEditor(['Mo800-1700'])

    await wrapper.find('[data-test="duty-0-day-We"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual(['MoWe800-1700'])
  })

  it('parses half-hour times and re-serializes an edited end time', async () => {
    const wrapper = mountEditor(['Mo930-1030'])
    expect((wrapper.find('[data-test="duty-0-begin"]').element as HTMLInputElement).value).toBe('09:30')

    await wrapper.find('[data-test="duty-0-end"]').setValue('11:00')

    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual(['Mo930-1100'])
  })

  it('keeps a non-parseable entry as editable raw text and preserves it', async () => {
    const wrapper = mountEditor(['weird-legacy-entry'])

    expect(wrapper.find('[data-test="duty-0-raw"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="duty-0-begin"]').exists()).toBe(false)
    expect((wrapper.find('[data-test="duty-0-raw"]').element as HTMLInputElement).value).toBe('weird-legacy-entry')

    // adding an incomplete row does not change the value, so nothing is emitted
    await wrapper.find('[data-test="add-duty-button"]').trigger('click')
    expect(wrapper.emitted('update:modelValue')).toBeUndefined()

    // editing the raw text emits it verbatim
    await wrapper.find('[data-test="duty-0-raw"]').setValue('still-weird')
    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual(['still-weird'])
  })

  it('adds and removes rows', async () => {
    const wrapper = mountEditor(['Mo800-1700'])
    expect(wrapper.find('[data-test="duty-row-0"]').exists()).toBe(true)

    await wrapper.find('[data-test="add-duty-button"]').trigger('click')
    expect(wrapper.find('[data-test="duty-row-1"]').exists()).toBe(true)

    // removing the real schedule changes the value; the incomplete row stays out of it
    await wrapper.find('[data-test="remove-duty-0"]').trigger('click')
    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual([])
  })

  it('shows the empty hint when there are no schedules', () => {
    const wrapper = mountEditor([])
    expect(wrapper.find('[data-test="no-duty-schedules"]').exists()).toBe(true)
  })

  // The server only grandfathers legacy schedule strings byte-identically, so
  // merely opening the dialog must not canonicalize day order or strip the
  // zero-padding — that would make the user unsavable for unrelated edits.
  it('does not rewrite a non-canonical schedule on mount', async () => {
    const wrapper = mountEditor(['TuMo2000-0800'])
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeUndefined()
    // the form still renders it as a parsed row, not raw text
    expect(wrapper.find('[data-test="duty-0-begin"]').exists()).toBe(true)
  })

  it('preserves untouched non-canonical rows when another row is edited', async () => {
    const wrapper = mountEditor(['TuMo2000-0800', 'We900-1700'])

    await wrapper.find('[data-test="duty-1-end"]').setValue('18:00')

    // row 0 stays byte-identical; only the edited row re-serializes
    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual(['TuMo2000-0800', 'We900-1800'])
  })

  it('re-serializes a non-canonical row only once the user edits it', async () => {
    const wrapper = mountEditor(['TuMo2000-0800'])

    await wrapper.find('[data-test="duty-0-day-We"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual(['MoTuWe2000-800'])
  })
})
