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

const ConfirmationStub = {
  name: 'OnmsConfirmationDialog',
  props: ['visible', 'title', 'actionButtonText'],
  emits: ['ok', 'cancel'],
  template: '<div v-if="visible" data-test="confirm"><slot name="content" /><button data-test="confirm-ok" @click="$emit(\'ok\')">ok</button></div>'
}

const mountTable = () => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const wrapper = mount(LocationsTable, {
    global: {
      plugins: [PrimeVue, pinia],
      stubs: {
        LocationEditorDialog: true, OnmsConfirmationDialog: ConfirmationStub, AboutDialogButton: true,
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

  it('summarizes the Minions of each location with down/unknown tags', async () => {
    ctx.store.locations = [loc('Default'), loc('East'), loc('West')] as any
    ctx.minionStore.minions = [
      minion('m1', 'East'), minion('m2', 'East', 'down'), minion('m3', 'East', null), minion('m4', 'West')
    ] as any
    await ctx.wrapper.vm.$nextTick()
    // rows sort by name: Default, East, West
    const [defaultRow, eastRow, westRow] = rows(ctx.wrapper)
    expect(defaultRow.find('[data-test="no-minions"]').text()).toBe('None deployed')
    expect(defaultRow.find('[data-test="location-minions-link"]').exists()).toBe(false)
    expect(eastRow.find('[data-test="location-minions-link"]').text()).toBe('3 Minions')
    expect(eastRow.find('[data-test="minions-down-tag"]').text()).toBe('1 down')
    expect(eastRow.find('[data-test="minions-down-tag"]').classes()).toContain('p-tag-danger')
    expect(eastRow.find('[data-test="minions-unknown-tag"]').text()).toBe('1 unknown')
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
    expect(rows(ctx.wrapper).map(r => r.find('[data-test="node-count"]').text())).toEqual(['12', '—', '—'])
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

  it('passes the store loading flag to the table', async () => {
    ctx.store.loading = true
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.findComponent({ name: 'DataTable' }).props('loading')).toBe(true)
  })

  it('offers the About dialog from the card header', () => {
    expect(ctx.wrapper.findComponent({ name: 'AboutDialogButton' }).exists()).toBe(true)
  })

  it('shows a search box once there are locations, and searchFor fills it', async () => {
    ctx.store.locations = []
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(false)
    ctx.store.locations = [loc('Raleigh'), loc('Zed')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="location-search"]').exists()).toBe(true)
    ;(ctx.wrapper.vm as any).searchFor('Zed')
    await ctx.wrapper.vm.$nextTick()
    expect((ctx.wrapper.find('[data-test="location-search"]').element as HTMLInputElement).value).toBe('Zed')
    expect(rows(ctx.wrapper)).toHaveLength(1)
    expect(rows(ctx.wrapper)[0].text()).toContain('Zed')
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
    const askAndConfirm = async () => {
      ctx.store.locations = [loc('Raleigh')] as any
      await ctx.wrapper.vm.$nextTick()
      await ctx.wrapper.find('[data-test="delete-location-button"]').trigger('click')
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([true])
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
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([false])
    })

    it('toasts the failure message as an error', async () => {
      vi.mocked(ctx.store.deleteLocation).mockResolvedValue({ success: false, message: 'Monitoring location \'Raleigh\' could not be deleted. Make sure no nodes are assigned to it.' })
      await askAndConfirm()
      expect(showToast).toHaveBeenCalledWith({ message: expect.stringContaining('Make sure no nodes are assigned'), severity: 'error' })
    })
  })
})
