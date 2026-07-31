import LocationsTable from '@/components/ManageMonitoringLocations/LocationsTable.vue'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const loc = (name: string) => ({
  'location-name': name, 'monitoring-area': name, name, area: name,
  geolocation: null, latitude: 0, longitude: 0, priority: 100, tags: []
})

const mountTable = () => {
  const wrapper = mount(LocationsTable, {
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { LocationEditorDialog: true, OnmsConfirmationDialog: true, TableCard: { template: '<div><slot /></div>' } }
    }
  })
  return { wrapper, store: useMonitoringLocationAdminStore() }
}

const headerText = (wrapper: VueWrapper<any>) =>
  wrapper.findAll('th').map((th) => th.text().trim()).filter(Boolean)

describe('LocationsTable.vue', () => {
  let ctx: ReturnType<typeof mountTable>

  beforeEach(() => {
    ctx = mountTable()
  })

  it('renders all column headers even when the list is empty', async () => {
    ctx.store.locations = []
    await ctx.wrapper.vm.$nextTick()
    const headers = headerText(ctx.wrapper)
    for (const h of ['Location Name', 'Monitoring Area', 'Geolocation', 'Latitude', 'Longitude', 'Priority']) {
      expect(headers).toContain(h)
    }
    expect(ctx.wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
  })

  it('disables Delete for the Default location', async () => {
    ctx.store.locations = [loc('Default'), loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    const deletes = ctx.wrapper.findAll('[data-test="delete-location-button"]')
    // first row is Default (default sort by name puts D before R)
    expect(deletes[0].attributes('disabled')).toBeDefined()
    expect(deletes[1].attributes('disabled')).toBeUndefined()
  })

  it('shows a search box once there are locations', async () => {
    ctx.store.locations = []
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(false)
    ctx.store.locations = [loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(true)
  })
})
