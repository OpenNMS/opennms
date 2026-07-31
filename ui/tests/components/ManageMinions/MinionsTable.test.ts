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
      stubs: { MinionEditorDialog: true, OnmsConfirmationDialog: true, TableCard: { template: '<div><slot /></div>' } }
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
    const headers = ctx.wrapper.findAll('th').map((th) => th.text().trim()).filter(Boolean)
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
})
