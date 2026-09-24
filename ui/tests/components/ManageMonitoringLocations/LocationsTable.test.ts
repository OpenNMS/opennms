import LocationsTable from '@/components/ManageMonitoringLocations/LocationsTable.vue'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

const loc = (name: string) => ({
  'location-name': name, 'monitoring-area': name, name, area: name,
  geolocation: null, latitude: 0, longitude: 0, priority: 100, tags: []
})

const ConfirmationStub = {
  name: 'OnmsConfirmationDialog',
  props: ['visible', 'title', 'actionButtonText'],
  emits: ['ok', 'cancel'],
  template: '<div v-if="visible" data-test="confirm"><slot name="content" /><button data-test="confirm-ok" @click="$emit(\'ok\')">ok</button></div>'
}

const mountTable = () => {
  const wrapper = mount(LocationsTable, {
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: {
        LocationEditorDialog: true, OnmsConfirmationDialog: ConfirmationStub, AboutDialogButton: true,
        TableCard: { template: '<div><slot /></div>' }
      }
    }
  })
  return { wrapper, store: useMonitoringLocationAdminStore() }
}

const headerText = (wrapper: VueWrapper<any>) =>
  wrapper.findAll('th').map(th => th.text().trim()).filter(Boolean)

describe('LocationsTable.vue', () => {
  let ctx: ReturnType<typeof mountTable>

  beforeEach(() => {
    showToast.mockClear()
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

  it('renders icon buttons with titles and disables Delete for the Default location', async () => {
    ctx.store.locations = [loc('Default'), loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    const edits = ctx.wrapper.findAll('[data-test="edit-location-button"]')
    const deletes = ctx.wrapper.findAll('[data-test="delete-location-button"]')
    expect(edits).toHaveLength(2)
    expect(edits[1].attributes('aria-label')).toBe('Edit Raleigh')
    expect(edits[1].attributes('title')).toBe('Edit Raleigh')
    expect(edits[1].find('svg').exists()).toBe(true)
    // first row is Default (default sort by name puts D before R)
    expect(deletes[0].attributes('disabled')).toBeDefined()
    expect(deletes[1].attributes('disabled')).toBeUndefined()
    expect(deletes[1].attributes('aria-label')).toBe('Delete Raleigh')
  })

  it('shows the not-editable note instead of actions for a name the API cannot address', async () => {
    ctx.store.locations = [loc('east/west')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="unaddressable-note"]').text()).toBe('not editable here')
    expect(ctx.wrapper.find('[data-test="edit-location-button"]').exists()).toBe(false)
    expect(ctx.wrapper.find('[data-test="delete-location-button"]').exists()).toBe(false)
  })

  it('passes the store loading flag to the table', async () => {
    ctx.store.loading = true
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.findComponent({ name: 'DataTable' }).props('loading')).toBe(true)
  })

  it('offers the About dialog from the card header', () => {
    expect(ctx.wrapper.findComponent({ name: 'AboutDialogButton' }).exists()).toBe(true)
  })

  it('shows a search box once there are locations', async () => {
    ctx.store.locations = []
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(false)
    ctx.store.locations = [loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(true)
  })

  it('shows the truncation note when the list was capped', async () => {
    ctx.store.locations = [loc('Raleigh')] as any
    ctx.store.totalCount = 5
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="truncation-note"]').text()).toContain('first 1 of 5')
  })

  describe('delete', () => {
    const askAndConfirm = async () => {
      ctx.store.locations = [loc('Raleigh')] as any
      await ctx.wrapper.vm.$nextTick()
      await ctx.wrapper.find('[data-test="delete-location-button"]').trigger('click')
      expect(ctx.wrapper.find('[data-test="confirm"]').text()).toContain('must have no nodes assigned')
      await ctx.wrapper.find('[data-test="confirm-ok"]').trigger('click')
      await flushPromises()
    }

    it('toasts success after the store confirms the delete', async () => {
      vi.mocked(ctx.store.deleteLocation).mockResolvedValue({ success: true, message: '' })
      await askAndConfirm()
      expect(ctx.store.deleteLocation).toHaveBeenCalledWith('Raleigh')
      expect(showToast).toHaveBeenCalledWith({ message: 'Monitoring location \'Raleigh\' deleted.', severity: 'success' })
      expect(ctx.wrapper.find('[data-test="confirm"]').exists()).toBe(false)
    })

    it('toasts the failure message as an error', async () => {
      vi.mocked(ctx.store.deleteLocation).mockResolvedValue({ success: false, message: 'Monitoring location \'Raleigh\' could not be deleted. Make sure no nodes are assigned to it.' })
      await askAndConfirm()
      expect(showToast).toHaveBeenCalledWith({ message: expect.stringContaining('Make sure no nodes are assigned'), severity: 'error' })
    })
  })
})
