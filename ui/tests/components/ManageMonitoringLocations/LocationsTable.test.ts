import LocationsTable from '@/components/ManageMonitoringLocations/LocationsTable.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
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
  'location-name': name, 'monitoring-area': `${name} area`, name, area: name,
  geolocation: null, latitude: 0, longitude: 0, priority: 100, tags: []
})

const minion = (id: string, location: string, status: string | null = 'up') =>
  ({ id, label: id, location, type: 'Minion', status, version: '34.0.0', date: 0, properties: {}})

// the delete dialogs are stubbed to their emit surface; their contents are tested on their own
const DeleteDialogStub = {
  name: 'LocationDeleteDialog',
  props: ['visible', 'location'],
  emits: ['update:visible', 'deleted'],
  template: '<div v-if="visible" data-test="delete-dialog">{{ location?.[\'location-name\'] }}</div>'
}
const DeletedDialogStub = {
  name: 'LocationDeletedDialog',
  props: ['visible', 'summary'],
  emits: ['update:visible'],
  template: '<div v-if="visible" data-test="deleted-dialog">{{ summary?.name }}</div>'
}

const mountTable = () => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const wrapper = mount(LocationsTable, {
    global: {
      plugins: [PrimeVue, pinia],
      stubs: {
        LocationEditorDialog: true, LocationDeleteDialog: DeleteDialogStub, LocationDeletedDialog: DeletedDialogStub,
        TableCard: { template: '<div><slot /></div>' }
      }
    }
  })
  return { wrapper, store: useMonitoringLocationAdminStore(), minionStore: useMinionAdminStore() }
}

const headerText = (wrapper: VueWrapper<any>) =>
  wrapper.findAll('th').map(th => th.text().trim()).filter(Boolean)

const rows = (wrapper: VueWrapper<any>) => wrapper.findAll('tbody tr')

describe('LocationsTable.vue', () => {
  let ctx: ReturnType<typeof mountTable>

  beforeEach(() => {
    showToast.mockClear()
    ctx = mountTable()
  })

  it('renders the Description, Minions and Nodes columns and hides the map fields', async () => {
    ctx.store.locations = []
    await ctx.wrapper.vm.$nextTick()
    expect(headerText(ctx.wrapper)).toEqual(['Location Name', 'Description', 'Minions', 'Nodes', 'Actions'])
    expect(ctx.wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
  })

  it('shows the description text in the Description column', async () => {
    ctx.store.locations = [loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(rows(ctx.wrapper)[0].findAll('td')[1].text()).toBe('Raleigh area')
  })

  it('summarizes the Minions of each location, counting missing or unrecognised states as unknown', async () => {
    ctx.store.locations = [loc('Default'), loc('East'), loc('West')] as any
    ctx.minionStore.minions = [
      minion('m1', 'East'), minion('m2', 'East', 'DOWN'), minion('m3', 'East', null), minion('m4', 'West'), minion('m5', 'East', 'degraded')
    ] as any
    await ctx.wrapper.vm.$nextTick()
    // rows sort by name: Default, East, West
    const [defaultRow, eastRow, westRow] = rows(ctx.wrapper)
    expect(defaultRow.find('[data-test="no-minions"]').text()).toBe('None deployed')
    expect(defaultRow.find('[data-test="location-minions-link"]').exists()).toBe(false)
    expect(eastRow.find('[data-test="location-minions-link"]').text()).toBe('4 Minions')
    expect(eastRow.find('[data-test="minions-down-tag"]').text()).toBe('1 down')
    expect(eastRow.find('[data-test="minions-down-tag"]').classes()).toContain('p-tag-danger')
    expect(eastRow.find('[data-test="minions-unknown-tag"]').text()).toBe('2 unknown')
    expect(eastRow.find('[data-test="minions-unknown-tag"]').classes()).toContain('p-tag-warn')
    expect(westRow.find('[data-test="location-minions-link"]').text()).toBe('1 Minion')
    expect(westRow.find('[data-test="minions-down-tag"]').exists()).toBe(false)
    expect(westRow.find('[data-test="minions-unknown-tag"]').exists()).toBe(false)
  })

  it('emits showMinions with the location name from the Minions link', async () => {
    ctx.store.locations = [loc('dc-east')] as any
    ctx.minionStore.minions = [minion('m1', 'dc-east')] as any
    await ctx.wrapper.vm.$nextTick()
    await ctx.wrapper.find('[data-test="location-minions-link"]').trigger('click')
    expect(ctx.wrapper.emitted('showMinions')).toEqual([['dc-east']])
  })

  it('shows the node count from the store and a dash when it is unknown', async () => {
    ctx.store.locations = [loc('Default'), loc('Raleigh'), loc('Zed')] as any
    ctx.store.nodeCounts = { Default: 12, Raleigh: null }
    await ctx.wrapper.vm.$nextTick()
    const counts = rows(ctx.wrapper).map(r => r.find('[data-test="node-count"]'))
    expect(counts.map(c => c.text())).toEqual(['12', '—', '—'])
    // null means the count could not be determined for that name; not-yet-fetched has no note
    expect(counts[0].attributes('title')).toBeUndefined()
    expect(counts[1].attributes('title')).toBe('Node count is not available for this name')
    expect(counts[2].attributes('title')).toBeUndefined()
  })

  it('greys out the Default row and disables both its actions with the core-location note', async () => {
    ctx.store.locations = [loc('Default'), loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    const edits = ctx.wrapper.findAll('[data-test="edit-location-button"]')
    const deletes = ctx.wrapper.findAll('[data-test="delete-location-button"]')
    expect(rows(ctx.wrapper)[0].classes()).toContain('default-location-row')
    expect(rows(ctx.wrapper)[1].classes()).not.toContain('default-location-row')
    expect(edits[0].attributes('disabled')).toBeDefined()
    expect(edits[0].attributes('title')).toBe('Default is the core location')
    expect(deletes[0].attributes('disabled')).toBeDefined()
    expect(deletes[0].attributes('title')).toBe('Default is the core location')
    expect(edits[1].attributes('disabled')).toBeUndefined()
    expect(edits[1].attributes('title')).toBe('Edit Raleigh')
    expect(edits[1].find('svg').exists()).toBe(true)
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

  it('masks the table only for the initial load, not for a refresh of existing rows', async () => {
    ctx.store.loading = true
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.findComponent({ name: 'DataTable' }).props('loading')).toBe(true)
    ctx.store.locations = [loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.findComponent({ name: 'DataTable' }).props('loading')).toBe(false)
  })

  it('shows a search box once there are locations', async () => {
    ctx.store.locations = []
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(false)
    ctx.store.locations = [loc('Raleigh'), loc('Zed')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(true)
  })

  describe('searchFor (hand-off from the Minions tab)', () => {
    beforeEach(async () => {
      ctx.store.locations = [loc('Raleigh'), loc('Zed'), loc('Zed-2')] as any
      await ctx.wrapper.vm.$nextTick()
    })

    it('narrows the rows to the exact name and shows a chip with a focusable Clear button', async () => {
      ;(ctx.wrapper.vm as any).searchFor('Zed')
      await ctx.wrapper.vm.$nextTick()
      expect(ctx.wrapper.find('[data-test="name-filter-chip"]').text()).toContain('Location: Zed')
      expect(ctx.wrapper.find('[data-test="name-filter-chip"] .p-chip-remove-icon').exists()).toBe(false)
      expect(rows(ctx.wrapper)).toHaveLength(1)
      expect(rows(ctx.wrapper)[0].text()).toContain('Zed')
      const clear = ctx.wrapper.find('button[data-test="clear-name-filter"]')
      expect(clear.attributes('aria-label')).toBe('Clear name filter')
      await clear.trigger('click')
      expect(ctx.wrapper.find('[data-test="name-filter-chip"]').exists()).toBe(false)
      expect(ctx.wrapper.find('[data-test="clear-name-filter"]').exists()).toBe(false)
      expect(rows(ctx.wrapper)).toHaveLength(3)
    })

    it('clears a typed search so it cannot hide the requested row', async () => {
      await ctx.wrapper.find('[data-test="location-search"]').setValue('Ral')
      expect(rows(ctx.wrapper)).toHaveLength(1)
      ;(ctx.wrapper.vm as any).searchFor('Zed')
      await ctx.wrapper.vm.$nextTick()
      expect((ctx.wrapper.find('[data-test="location-search"]').element as HTMLInputElement).value).toBe('')
      expect(rows(ctx.wrapper)[0].text()).toContain('Zed')
    })

    it('says so when the name matches no location', async () => {
      ;(ctx.wrapper.vm as any).searchFor('nowhere')
      await ctx.wrapper.vm.$nextTick()
      expect(ctx.wrapper.find('[data-test="empty-list"]').text()).toContain('No monitoring locations match the current filter')
    })
  })

  it('shows the truncation note when the list was capped', async () => {
    ctx.store.locations = [loc('Raleigh')] as any
    ctx.store.totalCount = 5
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="truncation-note"]').text()).toContain('first 1 of 5')
  })

  it('reports when the editor opens and closes', async () => {
    ctx.store.locations = [loc('Raleigh')] as any
    await ctx.wrapper.vm.$nextTick()
    await ctx.wrapper.find('[data-test="add-location-button"]').trigger('click')
    expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([true])
    ctx.wrapper.findComponent({ name: 'LocationEditorDialog' }).vm.$emit('update:visible', false)
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([false])
  })

  describe('delete', () => {
    const askDelete = async () => {
      ctx.store.locations = [loc('Raleigh')] as any
      await ctx.wrapper.vm.$nextTick()
      await ctx.wrapper.find('[data-test="delete-location-button"]').trigger('click')
      await ctx.wrapper.vm.$nextTick()
    }

    it('opens the delete dialog for the row and reports the dialog state', async () => {
      await askDelete()
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([true])
      const dialog = ctx.wrapper.findComponent({ name: 'LocationDeleteDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('location')['location-name']).toBe('Raleigh')
      expect(ctx.wrapper.find('[data-test="delete-dialog"]').text()).toBe('Raleigh')
      dialog.vm.$emit('update:visible', false)
      await ctx.wrapper.vm.$nextTick()
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([false])
      expect(ctx.wrapper.find('[data-test="delete-dialog"]').exists()).toBe(false)
    })

    it('shows the result dialog with the summary once the delete dialog reports success, without a toast', async () => {
      await askDelete()
      const dialog = ctx.wrapper.findComponent({ name: 'LocationDeleteDialog' })
      const summary = { name: 'Raleigh', applications: [{ id: 1, name: 'Web' }], outageCount: 2 }
      dialog.vm.$emit('update:visible', false)
      dialog.vm.$emit('deleted', summary)
      await flushPromises()
      // the result dialog keeps the auto-refresh paused
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([true])
      const deleted = ctx.wrapper.findComponent({ name: 'LocationDeletedDialog' })
      expect(deleted.props('visible')).toBe(true)
      expect(deleted.props('summary')).toEqual(summary)
      expect(showToast).not.toHaveBeenCalled()
      deleted.vm.$emit('update:visible', false)
      await ctx.wrapper.vm.$nextTick()
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([false])
    })
  })
})
