import MinionsTable from '@/components/ManageMinions/MinionsTable.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

const NOW = new Date('2026-09-24T12:00:00Z').getTime()
const MIN = 60_000
const HOUR = 60 * MIN

const minion = (id: string, over: Record<string, any> = {}) => ({
  id, label: id, location: 'Default', type: 'Minion', status: 'up', version: '34.0.0', date: NOW - 30_000, properties: {}, ...over
})

const ConfirmationStub = {
  name: 'OnmsConfirmationDialog',
  props: ['visible', 'title', 'actionButtonText'],
  emits: ['ok', 'cancel'],
  template: '<div v-if="visible" data-test="confirm"><slot name="content" /><button data-test="confirm-ok" @click="$emit(\'ok\')">ok</button></div>'
}

const mountTable = (props: Record<string, unknown> = {}) => {
  const wrapper = mount(MinionsTable, {
    props: { now: NOW, ...props },
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { OnmsConfirmationDialog: ConfirmationStub, AboutDialogButton: true, TableCard: { template: '<div><slot /></div>' }}
    }
  })
  return { wrapper, store: useMinionAdminStore() }
}

const headerText = (wrapper: VueWrapper<any>) => wrapper.findAll('th').map(th => th.text().trim()).filter(Boolean)
const rowIds = (wrapper: VueWrapper<any>) => wrapper.findAll('tbody tr').map(tr => tr.find('td').text())
const quickFilterLabels = (wrapper: VueWrapper<any>) => wrapper.findAll('[data-test="quick-filters"] [role="radio"], [data-test="quick-filters"] .p-togglebutton').map(b => b.text())
const clickQuickFilter = async (wrapper: VueWrapper<any>, prefix: string) => {
  const button = wrapper.findAll('[data-test="quick-filters"] .p-togglebutton').find(b => b.text().startsWith(prefix))
  await button!.trigger('click')
  await flushPromises()
}

describe('MinionsTable.vue', () => {
  let ctx: ReturnType<typeof mountTable>

  beforeEach(() => {
    showToast.mockClear()
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout', 'setInterval', 'clearInterval', 'Date'] })
    vi.setSystemTime(new Date(NOW))
    ctx = mountTable()
  })
  afterEach(() => {
    ctx.wrapper.unmount()
    vi.useRealTimers()
  })

  it('renders the renamed columns and hides label, type and properties', async () => {
    ctx.store.minions = []
    ctx.store.isLoading = false
    await ctx.wrapper.vm.$nextTick()
    const headers = headerText(ctx.wrapper)
    expect(headers).toEqual(['Minion', 'Monitoring location', 'Status', 'Version', 'Last heartbeat', 'Actions'])
    expect(ctx.wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
  })

  it('does not show the empty message while loading', async () => {
    ctx.store.minions = []
    ctx.store.isLoading = true
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="empty-list"]').exists()).toBe(false)
  })

  it('shows the error copy when a load failed', async () => {
    ctx.store.minions = []
    ctx.store.isLoading = false
    ctx.store.loadError = true
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="empty-list"]').text()).toContain('Failed to load minions')
  })

  it('has no Edit action or editor dialog, only Delete', async () => {
    ctx.store.minions = [minion('m1')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="edit-minion-button"]').exists()).toBe(false)
    expect(ctx.wrapper.findComponent({ name: 'MinionEditorDialog' }).exists()).toBe(false)
    const del = ctx.wrapper.find('[data-test="delete-minion-button"]')
    expect(del.exists()).toBe(true)
    expect(del.attributes('aria-label')).toBe('Delete m1')
  })

  it('offers the About dialog from the card header', () => {
    expect(ctx.wrapper.findComponent({ name: 'AboutDialogButton' }).exists()).toBe(true)
  })

  it('links the Minion id to its node when a node id is known', async () => {
    vi.mocked(ctx.store.nodeIdFor).mockReturnValue(42)
    ctx.store.minions = [minion('m1')] as any
    await ctx.wrapper.vm.$nextTick()
    const link = ctx.wrapper.find('[data-test="minion-node-link"]')
    expect(link.exists()).toBe(true)
    expect(link.attributes('href')).toContain('element/node.jsp?node=42')
  })

  it('renders the status as an uppercase tag, unrecognised and missing states in the unknown colour', async () => {
    ctx.store.minions = [
      minion('m1', { status: 'up' }), minion('m2', { status: 'DOWN' }), minion('m3', { status: null }), minion('m4', { status: 'degraded' })
    ] as any
    await ctx.wrapper.vm.$nextTick()
    const tags = ctx.wrapper.findAll('[data-test="status-tag"]')
    expect(tags.map(t => t.text())).toEqual(['UP', 'DOWN', 'UNKNOWN', 'DEGRADED'])
    expect(tags[0].classes()).toContain('p-tag-success')
    expect(tags[1].classes()).toContain('p-tag-danger')
    expect(tags[2].classes()).toContain('p-tag-warn')
    expect(tags[3].classes()).toContain('p-tag-warn')
    expect(quickFilterLabels(ctx.wrapper)[1]).toBe('Down or unknown (3)')
  })

  it('colours the heartbeat by age and shows the absolute time as the title', async () => {
    ctx.store.minions = [
      minion('m1', { date: NOW - 4 * MIN }),
      minion('m2', { date: NOW - 90 * MIN }),
      minion('m3', { date: NOW - 3 * HOUR }),
      minion('m4', { date: null })
    ] as any
    await ctx.wrapper.vm.$nextTick()
    const tags = ctx.wrapper.findAll('[data-test="heartbeat-tag"]')
    expect(tags.map(t => t.text())).toEqual(['4 min ago', '1 h ago', '3 h ago', '-'])
    expect(tags[0].classes()).toContain('p-tag-success')
    expect(tags[1].classes()).toContain('p-tag-warn')
    expect(tags[2].classes()).toContain('p-tag-danger')
    expect(tags[3].classes()).toContain('p-tag-danger')
    expect(tags[0].attributes('title')).toBe(new Date(NOW - 4 * MIN).toLocaleString())
  })

  it('recomputes the heartbeat from the shared clock prop, not from a clock of its own', async () => {
    ctx.store.minions = [minion('m1', { date: NOW - 10_000 })] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="heartbeat-tag"]').text()).toBe('10 s ago')
    await vi.advanceTimersByTimeAsync(5_000)
    expect(ctx.wrapper.find('[data-test="heartbeat-tag"]').text()).toBe('10 s ago')
    await ctx.wrapper.setProps({ now: NOW + 5_000 })
    expect(ctx.wrapper.find('[data-test="heartbeat-tag"]').text()).toBe('15 s ago')
  })

  it('compares the Minion VersionBean string with the core version by major.minor.patch', async () => {
    ctx.store.coreVersion = '37.0.0'
    ctx.store.minions = [
      minion('m1', { version: 'v37.0.0-SNAPSHOT' }), minion('m2', { version: 'v36.0.2' }), minion('m3', { version: '1.0' })
    ] as any
    await ctx.wrapper.vm.$nextTick()
    const tags = ctx.wrapper.findAll('[data-test="version-tag"]')
    expect(tags.map(t => t.text())).toEqual(['v37.0.0-SNAPSHOT', 'v36.0.2', '1.0'])
    expect(tags[0].classes()).toContain('p-tag-success')
    expect(tags[0].attributes('title')).toBe('Matches the core version 37.0.0')
    expect(tags[1].classes()).toContain('p-tag-danger')
    expect(tags[1].attributes('title')).toBe('The core runs 37.0.0')
    expect(tags[2].classes()).toContain('p-tag-secondary')
    expect(tags[2].attributes('title')).toBe('This version cannot be compared with the core version 37.0.0')
    expect(quickFilterLabels(ctx.wrapper)[3]).toBe('Version differs from core (1)')
  })

  it('leaves the version neutral and counts no difference while the core version is unknown', async () => {
    ctx.store.coreVersion = null
    ctx.store.minions = [minion('m1', { version: 'v34.0.0' }), minion('m2', { version: 'v33.0.0' })] as any
    await ctx.wrapper.vm.$nextTick()
    const tags = ctx.wrapper.findAll('[data-test="version-tag"]')
    expect(tags[0].classes()).toContain('p-tag-secondary')
    expect(tags[1].classes()).toContain('p-tag-secondary')
    expect(tags[1].classes()).not.toContain('p-tag-danger')
    expect(tags[0].attributes('title')).toBe('The core version could not be determined')
    expect(quickFilterLabels(ctx.wrapper)[3]).toBe('Version differs from core (0)')
  })

  it('offers a link to the location that emits showLocation', async () => {
    ctx.store.minions = [minion('m1', { location: 'dc-east' })] as any
    await ctx.wrapper.vm.$nextTick()
    await ctx.wrapper.find('[data-test="minion-location-link"]').trigger('click')
    expect(ctx.wrapper.emitted('showLocation')).toEqual([['dc-east']])
  })

  describe('quick filters', () => {
    beforeEach(async () => {
      ctx.store.coreVersion = '34.0.0'
      ctx.store.minions = [
        minion('m1'),
        minion('m2', { status: 'down' }),
        minion('m3', { status: 'unknown', date: NOW - 25 * HOUR }),
        minion('m4', { version: '33.0.0', date: null })
      ] as any
      await ctx.wrapper.vm.$nextTick()
    })

    it('shows a count on every option and defaults to All', () => {
      expect(quickFilterLabels(ctx.wrapper)).toEqual([
        'All (4)', 'Down or unknown (2)', 'Not seen in 24 h (2)', 'Version differs from core (1)'
      ])
      expect(rowIds(ctx.wrapper)).toEqual(['m1', 'm2', 'm3', 'm4'])
    })

    it('narrows the rows to the selected filter', async () => {
      await clickQuickFilter(ctx.wrapper, 'Down or unknown')
      expect(rowIds(ctx.wrapper)).toEqual(['m2', 'm3'])
      await clickQuickFilter(ctx.wrapper, 'Not seen in 24 h')
      expect(rowIds(ctx.wrapper)).toEqual(['m3', 'm4'])
      await clickQuickFilter(ctx.wrapper, 'Version differs')
      expect(rowIds(ctx.wrapper)).toEqual(['m4'])
      await clickQuickFilter(ctx.wrapper, 'All')
      expect(rowIds(ctx.wrapper)).toEqual(['m1', 'm2', 'm3', 'm4'])
    })

    it('says so when a filter matches nothing', async () => {
      ctx.store.minions = [minion('m1')] as any
      await ctx.wrapper.vm.$nextTick()
      await clickQuickFilter(ctx.wrapper, 'Down or unknown')
      expect(ctx.wrapper.find('[data-test="empty-list"]').text()).toContain('No minions match the current filter')
    })
  })

  describe('location filter', () => {
    it('pre-filters by location, shows a chip and scopes the quick-filter counts', async () => {
      ctx.store.minions = [minion('m1', { location: 'dc-east' }), minion('m2', { location: 'dc-west', status: 'down' })] as any
      await ctx.wrapper.setProps({ locationFilter: 'dc-east' })
      const chip = ctx.wrapper.find('[data-test="location-filter-chip"]')
      expect(chip.text()).toContain('Location: dc-east')
      expect(rowIds(ctx.wrapper)).toEqual(['m1'])
      expect(quickFilterLabels(ctx.wrapper)[0]).toBe('All (1)')
      expect(quickFilterLabels(ctx.wrapper)[1]).toBe('Down or unknown (0)')
    })

    it('resets the quick filter to All when a location filter arrives', async () => {
      ctx.store.minions = [minion('m1', { location: 'dc-east' }), minion('m2', { location: 'dc-east', status: 'down' })] as any
      await ctx.wrapper.vm.$nextTick()
      await clickQuickFilter(ctx.wrapper, 'Down or unknown')
      expect(rowIds(ctx.wrapper)).toEqual(['m2'])
      await ctx.wrapper.setProps({ locationFilter: 'dc-east' })
      expect(rowIds(ctx.wrapper)).toEqual(['m1', 'm2'])
      const checked = ctx.wrapper.findAll('[data-test="quick-filters"] .p-togglebutton-checked').map(b => b.text())
      expect(checked).toEqual(['All (2)'])
    })

    it('dismissing the chip asks the parent to clear the filter', async () => {
      ctx.store.minions = [minion('m1', { location: 'dc-east' })] as any
      await ctx.wrapper.setProps({ locationFilter: 'dc-east' })
      await ctx.wrapper.find('[data-test="location-filter-chip"] .p-chip-remove-icon').trigger('click')
      expect(ctx.wrapper.emitted('update:locationFilter')).toEqual([[null]])
      await ctx.wrapper.setProps({ locationFilter: null })
      expect(ctx.wrapper.find('[data-test="location-filter-chip"]').exists()).toBe(false)
    })
  })

  it('shows a truncation note when the safety cap was hit', async () => {
    ctx.store.minions = [minion('m1')] as any
    ctx.store.totalCount = 9
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="truncation-note"]').exists()).toBe(true)
  })

  it('shows a stale-data note when a reload failed but rows remain', async () => {
    ctx.store.minions = [minion('m1')] as any
    ctx.store.loadError = true
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="stale-note"]').exists()).toBe(true)
  })

  describe('delete', () => {
    it('reports the dialog state and deletes after confirmation', async () => {
      vi.mocked(ctx.store.deleteMinion).mockResolvedValue({ success: true, message: '' })
      ctx.store.minions = [minion('m1')] as any
      await ctx.wrapper.vm.$nextTick()
      await ctx.wrapper.find('[data-test="delete-minion-button"]').trigger('click')
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([true])
      expect(ctx.wrapper.find('[data-test="confirm"]').text()).toContain('m1')
      await ctx.wrapper.find('[data-test="confirm-ok"]').trigger('click')
      await flushPromises()
      expect(ctx.wrapper.emitted('dialogOpen')?.at(-1)).toEqual([false])
      expect(ctx.store.deleteMinion).toHaveBeenCalledWith('m1')
      expect(showToast).toHaveBeenCalledWith({ message: 'Minion \'m1\' deleted.', severity: 'success' })
    })
  })
})
