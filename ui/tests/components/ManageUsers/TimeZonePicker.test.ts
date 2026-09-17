import TimeZonePicker from '@/components/ManageUsers/TimeZonePicker.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

const ZONES = ['Africa/Cairo', 'America/New_York', 'America/Argentina/Buenos_Aires', 'Pacific/Auckland', 'UTC']

vi.stubGlobal('Intl', Object.assign(Object.create(Intl), {
  supportedValuesOf: () => ZONES
}))

const mountPicker = (modelValue: string | null = null) =>
  mount(TimeZonePicker, {
    props: { modelValue, idBase: 'tz' },
    global: { plugins: [PrimeVue] }
  })

const regionSelect = (wrapper: ReturnType<typeof mountPicker>) =>
  wrapper.findComponent('[data-test="timezone-region-select"]') as any
const citySelect = (wrapper: ReturnType<typeof mountPicker>) =>
  wrapper.findComponent('[data-test="timezone-city-select"]') as any

describe('TimeZonePicker.vue', () => {
  it('offers regions from the zone list, standalone zones included', () => {
    const wrapper = mountPicker()
    expect(regionSelect(wrapper).props('options')).toEqual(['Africa', 'America', 'Pacific', 'UTC'])
  })

  it('shows cities of the picked region with underscores as spaces', async () => {
    const wrapper = mountPicker()
    await regionSelect(wrapper).setValue('America')

    const labels = citySelect(wrapper).props('options').map((o: { label: string }) => o.label)
    expect(labels).toEqual(['Argentina / Buenos Aires', 'New York'])
  })

  it('emits the full IANA id once region and city are picked', async () => {
    const wrapper = mountPicker()
    await regionSelect(wrapper).setValue('America')
    await citySelect(wrapper).setValue('America/New_York')

    expect(wrapper.emitted('update:modelValue')?.at(-1)).toEqual(['America/New_York'])
  })

  it('treats a region-only zone as a complete value', async () => {
    const wrapper = mountPicker()
    await regionSelect(wrapper).setValue('UTC')

    expect(wrapper.emitted('update:modelValue')?.at(-1)).toEqual(['UTC'])
    expect(citySelect(wrapper).props('disabled')).toBe(true)
  })

  it('pre-selects both halves from a stored zone', () => {
    const wrapper = mountPicker('Africa/Cairo')
    expect(regionSelect(wrapper).props('modelValue')).toBe('Africa')
    expect(citySelect(wrapper).props('modelValue')).toBe('Africa/Cairo')
  })

  it('keeps a stored zone the browser list does not know selectable', () => {
    // hand-edited users.xml can hold a legacy alias; it must render, not vanish
    const wrapper = mountPicker('US/Eastern')
    expect(regionSelect(wrapper).props('options')).toContain('US')
    expect(regionSelect(wrapper).props('modelValue')).toBe('US')
    expect(citySelect(wrapper).props('modelValue')).toBe('US/Eastern')
  })

  it('clears to null when the region is cleared', async () => {
    const wrapper = mountPicker('Africa/Cairo')
    await regionSelect(wrapper).setValue(null)

    expect(wrapper.emitted('update:modelValue')?.at(-1)).toEqual([null])
  })
})
