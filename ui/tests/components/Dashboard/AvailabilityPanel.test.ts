import { describe, expect, it, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AvailabilityPanel from '@/components/Dashboard/panels/AvailabilityPanel.vue'
import { getAvailability, type AvailabilityCategory, type AvailabilitySection } from '@/services/availabilityService'
import { TimeframePreset } from '@/types/dashboard'

vi.mock('@/services/availabilityService', async importOriginal => ({
  ...(await importOriginal<typeof import('@/services/availabilityService')>()),
  getAvailability: vi.fn()
}))

const category = (name: string, overrides: Partial<AvailabilityCategory> = {}): AvailabilityCategory => ({
  name,
  outageText: '0 of 4',
  availabilityText: '100.000%',
  availability: 100,
  availabilityClass: 'Normal',
  outageClass: 'Normal',
  lastUpdated: Date.UTC(2026, 8, 11, 12, 0, 0),
  stale: false,
  ...overrides
})

const mountPanel = async (sections: AvailabilitySection[] | null) => {
  vi.mocked(getAvailability).mockResolvedValue(sections)
  const w = mount(AvailabilityPanel, {
    props: {
      panelId: 'test-panel',
      options: {},
      filter: { surveillanceCategories: [], ipMatch: null },
      timeframe: { preset: TimeframePreset.Last24h, from: null, to: null },
      refreshTick: 0
    }
  })
  await flushPromises()
  return w
}

describe('AvailabilityPanel staleness', () => {
  beforeEach(() => vi.clearAllMocks())

  it('shows the figures without a warning when every category is current', async () => {
    const w = await mountPanel([{ name: 'Total', categories: [category('Overall Service Availability')] }])
    expect(w.find('.avail__stale').exists()).toBe(false)
    expect(w.text()).toContain('100.000%')
  })

  it('keeps the figures but warns when a category is stale, naming the oldest snapshot', async () => {
    const oldest = Date.UTC(2026, 8, 10, 8, 30, 0)
    const w = await mountPanel([
      { name: 'Total', categories: [category('Overall Service Availability', { stale: true, lastUpdated: oldest })] },
      { name: 'Servers', categories: [category('Web Servers', { stale: true }), category('Email Servers')] }
    ])
    const warning = w.find('.avail__stale')
    expect(warning.exists()).toBe(true)
    expect(warning.text()).toContain('Availability data is stale')
    expect(warning.text()).toContain(new Date(oldest).toLocaleString())
    expect(warning.text()).toContain('Is the Availability daemon running?')
    // the table is still rendered underneath the warning
    expect(w.find('table.avail__table').exists()).toBe(true)
    expect(w.text()).toContain('Web Servers')
  })

  it('still distinguishes a failed fetch from stale data', async () => {
    const w = await mountPanel(null)
    expect(w.text()).toContain('Waiting for availability data')
    expect(w.find('.avail__stale').exists()).toBe(false)
  })
})
