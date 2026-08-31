import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDashboardStore } from '@/stores/dashboardStore'
import { createDefaultLayout } from '@/components/Dashboard/defaultLayout'
import { getSystemDashboard, saveSystemDashboard } from '@/services/dashboardService'
import { TimeframePreset } from '@/types/dashboard'

vi.mock('@/services/dashboardService', () => ({
  getSystemDashboard: vi.fn(),
  saveSystemDashboard: vi.fn()
}))

describe('useDashboardStore', () => {
  let store: ReturnType<typeof useDashboardStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useDashboardStore()
    vi.clearAllMocks()
  })

  it('starts with the empty base layout and a clean slate', () => {
    expect(store.layout.panels).toEqual([])
    expect(store.isDirty).toBe(false)
    expect(store.editMode).toBe(false)
  })

  // seed a panel so the per-panel operations below have something to act on
  const seed = () => {
    store.addPanel('notes'); store.isDirty = false; return store.layout.panels[0]
  }

  it('load replaces the layout and clears dirty', async () => {
    const layout = createDefaultLayout()
    layout.panels = [{ id: 'x', type: 'notes', x: 0, y: 0, w: 3, h: 100, collapsed: false, titleOverride: null, filterOverride: null, timeframeOverride: null, refreshSeconds: null, options: {}}]
    vi.mocked(getSystemDashboard).mockResolvedValue(layout)
    store.isDirty = true

    await store.load()

    expect(store.layout.panels).toHaveLength(1)
    expect(store.isDirty).toBe(false)
    expect(store.isLoading).toBe(false)
  })

  it('load failure keeps the current layout, dirty flag and flags an error', async () => {
    const before = store.layout
    store.isDirty = true
    vi.mocked(getSystemDashboard).mockRejectedValue(new Error('500'))

    await store.load()

    expect(store.layout).toBe(before)
    expect(store.isDirty).toBe(true)
    expect(store.loadError).toBe(true)
  })

  it('load bumps the layout revision so the grid rebuilds', async () => {
    vi.mocked(getSystemDashboard).mockResolvedValue(createDefaultLayout())
    const before = store.layoutRevision
    await store.load()
    expect(store.layoutRevision).toBe(before + 1)
  })

  it('syncGeometry is a no-op outside edit mode', () => {
    seed()
    const panel = store.layout.panels[0]
    const origX = panel.x
    store.editMode = false
    store.syncGeometry([{ i: panel.id, x: origX + 3, y: panel.y, w: panel.w, h: panel.h }])
    expect(panel.x).toBe(origX)
    expect(store.isDirty).toBe(false)
  })

  it('save clears dirty on success and keeps it on failure', async () => {
    store.isDirty = true
    vi.mocked(saveSystemDashboard).mockResolvedValue()
    expect(await store.save()).toBe(true)
    expect(store.isDirty).toBe(false)

    store.isDirty = true
    vi.mocked(saveSystemDashboard).mockRejectedValue(new Error('boom'))
    expect(await store.save()).toBe(false)
    expect(store.isDirty).toBe(true)
  })

  it('addPanel places the new panel below everything and marks dirty', () => {
    const before = store.layout.panels.length
    const maxY = store.layout.panels.reduce((max, p) => Math.max(max, p.y + p.h), 0)

    store.addPanel('notes')

    expect(store.layout.panels).toHaveLength(before + 1)
    const added = store.layout.panels[store.layout.panels.length - 1]
    expect(added.type).toBe('notes')
    expect(added.y).toBe(maxY)
    expect(store.isDirty).toBe(true)
  })

  it('addPanel ignores unknown panel types', () => {
    const before = store.layout.panels.length
    store.addPanel('no-such-panel')
    expect(store.layout.panels).toHaveLength(before)
    expect(store.isDirty).toBe(false)
  })

  it('removePanel drops the panel and marks dirty', () => {
    seed()
    const id = store.layout.panels[0].id
    store.removePanel(id)
    expect(store.layout.panels.find(p => p.id === id)).toBeUndefined()
    expect(store.isDirty).toBe(true)
  })

  it('setPanelTitle trims and nulls empty overrides', () => {
    seed()
    const id = store.layout.panels[0].id
    store.setPanelTitle(id, '  My Panel  ')
    expect(store.layout.panels[0].titleOverride).toBe('My Panel')
    store.setPanelTitle(id, '   ')
    expect(store.layout.panels[0].titleOverride).toBeNull()
  })

  it('resolved getters prefer the panel override over the global', () => {
    seed()
    const panel = store.layout.panels[0]
    expect(store.resolvedTimeframe(panel)).toEqual(store.layout.globalTimeframe)
    panel.timeframeOverride = { preset: TimeframePreset.Today, from: null, to: null }
    expect(store.resolvedTimeframe(panel).preset).toBe(TimeframePreset.Today)

    expect(store.resolvedRefreshSeconds(panel)).toBe(store.layout.refresh.seconds)
    panel.refreshSeconds = 30
    expect(store.resolvedRefreshSeconds(panel)).toBe(30)
  })

  it('syncGeometry persists moves but keeps stored height for collapsed panels', () => {
    store.editMode = true
    seed()
    const panel = store.layout.panels[0]
    const storedH = panel.h
    panel.collapsed = true

    store.syncGeometry([{ i: panel.id, x: 5, y: 7, w: panel.w, h: 1 }])

    expect(panel.x).toBe(5)
    expect(panel.y).toBe(7)
    expect(panel.h).toBe(storedH)
    expect(store.isDirty).toBe(true)
  })

  it('syncGeometry with no changes does not mark dirty', () => {
    store.editMode = true
    seed()
    const panel = store.layout.panels[0]
    panel.collapsed = false
    store.syncGeometry([{ i: panel.id, x: panel.x, y: panel.y, w: panel.w, h: panel.h }])
    expect(store.isDirty).toBe(false)
  })

  it('applyFactoryDefault restores the (empty) base default and marks dirty', () => {
    store.addPanel('notes')
    store.applyFactoryDefault()
    expect(store.layout.panels).toEqual([])
    expect(store.isDirty).toBe(true)
  })

  it('togglePaused and setRefreshSeconds mutate the refresh block', () => {
    const paused = store.layout.refresh.paused
    store.togglePaused()
    expect(store.layout.refresh.paused).toBe(!paused)
    store.setRefreshSeconds(300)
    expect(store.refreshSeconds).toBe(300)
  })

  it('setAutoCompact toggles the squeeze option, marks dirty and bumps the revision', () => {
    expect(store.autoCompact).toBe(true)
    const before = store.layoutRevision
    store.setAutoCompact(false)
    expect(store.autoCompact).toBe(false)
    expect(store.layout.autoCompact).toBe(false)
    expect(store.isDirty).toBe(true)
    expect(store.layoutRevision).toBe(before + 1)
  })

  it('setAutoCompact is a no-op (no dirty, no revision bump) when unchanged', () => {
    const before = store.layoutRevision
    store.setAutoCompact(true) // already true by default
    expect(store.isDirty).toBe(false)
    expect(store.layoutRevision).toBe(before)
  })
})
