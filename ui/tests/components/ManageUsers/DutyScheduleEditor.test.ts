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

    // an edit elsewhere triggers an emit; the raw entry survives in the value
    // (the new incomplete row is dropped until it has days and times)
    await wrapper.find('[data-test="add-duty-button"]').trigger('click')
    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual(['weird-legacy-entry'])
  })

  it('adds and removes rows', async () => {
    const wrapper = mountEditor([])
    expect(wrapper.find('[data-test="no-duty-schedules"]').exists()).toBe(true)

    await wrapper.find('[data-test="add-duty-button"]').trigger('click')
    expect(wrapper.find('[data-test="duty-row-0"]').exists()).toBe(true)

    await wrapper.find('[data-test="remove-duty-0"]').trigger('click')
    expect(wrapper.emitted('update:modelValue')?.at(-1)?.[0]).toEqual([])
  })
})
