import MinionsTable from '@/components/ManageMinions/MinionsTable.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const minion = (id: string, over: Record<string, any> = {}) => ({
  id, label: id, location: 'Default', type: 'Minion', status: 'up', version: '1', date: 0, properties: {}, ...over
})

const mountTable = () => {
  const wrapper = mount(MinionsTable, {
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { MinionEditorDialog: true, OnmsConfirmationDialog: true, TableCard: { template: '<div><slot /></div>' }}
    }
  })
  return { wrapper, store: useMinionAdminStore() }
}

describe('MinionsTable.vue', () => {
  let ctx: ReturnType<typeof mountTable>

  beforeEach(() => {
    ctx = mountTable()
  })

  it('renders all column headers even when there are no minions', async () => {
    ctx.store.minions = []
    ctx.store.isLoading = false
    await ctx.wrapper.vm.$nextTick()
    const headers = ctx.wrapper.findAll('th').map(th => th.text().trim()).filter(Boolean)
    for (const h of ['ID', 'Label', 'Location', 'Type', 'Status', 'Version', 'Last Updated', 'Properties']) {
      expect(headers).toContain(h)
    }
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

  it('refresh button re-fetches minions', async () => {
    ctx.store.minions = [minion('m1')] as any
    await ctx.wrapper.vm.$nextTick()
    await ctx.wrapper.find('[data-test="refresh-button"]').trigger('click')
    expect(ctx.store.getMinions).toHaveBeenCalled()
  })

  it('links the ID to its node when a node id is known', async () => {
    vi.mocked(ctx.store.nodeIdFor).mockReturnValue(42)
    ctx.store.minions = [minion('m1')] as any
    await ctx.wrapper.vm.$nextTick()
    const link = ctx.wrapper.find('[data-test="minion-node-link"]')
    expect(link.exists()).toBe(true)
    expect(link.attributes('href')).toContain('element/node.jsp?node=42')
  })

  it('renders the ID as plain text when no node id is known', async () => {
    // default testing-pinia spy returns undefined
    ctx.store.minions = [minion('m1')] as any
    await ctx.wrapper.vm.$nextTick()
    expect(ctx.wrapper.find('[data-test="minion-node-link"]').exists()).toBe(false)
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
})
