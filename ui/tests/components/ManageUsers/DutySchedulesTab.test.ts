import DutySchedulesTab from '@/components/ManageUsers/DutySchedulesTab.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountTab = (modelValue: string[]) =>
  mount(DutySchedulesTab, {
    props: { modelValue },
    global: { plugins: [PrimeVue] }
  })

describe('DutySchedulesTab.vue', () => {
  it('renders multi-day entries as one row with full day names', () => {
    const wrapper = mountTab(['MoTuWe800-1700'])
    const table = wrapper.find('[data-test="duty-table"]')
    expect(table.text()).toContain('Monday, Tuesday, Wednesday')
    expect(table.text()).toContain('08:00')
    expect(table.text()).toContain('17:00')
  })

  it('shows an unparseable hand-edited entry verbatim instead of dropping it', () => {
    const wrapper = mountTab(['always-on-call'])
    expect(wrapper.find('[data-test="duty-raw"]').text()).toBe('always-on-call')
  })

  it('adds a single-day entry in the wire format', async () => {
    const wrapper = mountTab([])
    // day defaults to Monday, times default to 09:00-17:00
    await wrapper.find('[data-test="duty-add-button"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')?.at(-1)).toEqual([['Mo900-1700']])
  })

  it('removes exactly the deleted entry and round-trips the rest byte-identically', async () => {
    // legacy strings that only the server grandfathers (overnight, multi-day)
    // must survive a delete of a sibling untouched
    const wrapper = mountTab(['MoTu2000-800', 'weird-entry', 'Fr900-1700'])
    await wrapper.find('[data-test="remove-duty-2"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')?.at(-1)).toEqual([['MoTu2000-800', 'weird-entry']])
  })

  it('flags an exact duplicate instead of adding it twice', async () => {
    const wrapper = mountTab(['Mo900-1700'])
    await wrapper.find('[data-test="duty-add-button"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeUndefined()
    expect(wrapper.find('[data-test="duty-duplicate-note"]').exists()).toBe(true)
  })

  it('shows the availability hint when there are no schedules', () => {
    const wrapper = mountTab([])
    expect(wrapper.find('[data-test="no-duty-schedules"]').text()).toContain('available at all times')
  })
})
