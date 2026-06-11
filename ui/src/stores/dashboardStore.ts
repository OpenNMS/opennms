///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { defineStore } from 'pinia'
import { v4 as uuidv4 } from 'uuid'
import type { DashboardFilter, DashboardLayout, DashboardPanel, PanelHeightMode, Timeframe } from '@/types/dashboard'
import { createDefaultLayout } from '@/components/Dashboard/defaultLayout'
import { getPanelDefinition } from '@/components/Dashboard/registry'
import { getSystemDashboard, saveSystemDashboard } from '@/services/dashboardService'

interface DashboardStoreState {
  layout: DashboardLayout
  isLoading: boolean
  isSaving: boolean
  isDirty: boolean
  editMode: boolean
  refreshTick: number
}

export const useDashboardStore = defineStore('dashboardStore', {
  state: (): DashboardStoreState => ({
    layout: createDefaultLayout(),
    isLoading: false,
    isSaving: false,
    isDirty: false,
    editMode: false,
    refreshTick: 0
  }),
  getters: {
    panels: (state): DashboardPanel[] => state.layout.panels,
    isPaused: (state): boolean => state.layout.refresh.paused,
    refreshSeconds: (state): number => state.layout.refresh.seconds,
    // Effective filter / timeframe / cadence for a panel: override wins, else global.
    resolvedFilter:
      (state) =>
        (panel: DashboardPanel): DashboardFilter =>
          panel.filterOverride ?? state.layout.globalFilter,
    resolvedTimeframe:
      (state) =>
        (panel: DashboardPanel): Timeframe =>
          panel.timeframeOverride ?? state.layout.globalTimeframe,
    resolvedRefreshSeconds:
      (state) =>
        (panel: DashboardPanel): number =>
          panel.refreshSeconds ?? state.layout.refresh.seconds,
    // Effective height mode: panel override, else registry default, else 'auto'.
    resolvedHeightMode:
      () =>
        (panel: DashboardPanel): PanelHeightMode =>
          panel.heightMode ?? getPanelDefinition(panel.type)?.defaultHeightMode ?? 'auto'
  },
  actions: {
    async load() {
      this.isLoading = true
      try {
        this.layout = await getSystemDashboard()
        this.isDirty = false
      } finally {
        this.isLoading = false
      }
    },
    async save(): Promise<boolean> {
      this.isSaving = true
      try {
        await saveSystemDashboard(this.layout)
        this.isDirty = false
        return true
      } catch (error) {
        // keep isDirty so the user can retry; surfacing UI comes with the snackbar wiring
        console.error('Failed to save dashboard layout:', error)
        return false
      } finally {
        this.isSaving = false
      }
    },
    // Reload from the server (or default), discarding unsaved edits.
    async reset() {
      await this.load()
    },
    // Replace the working layout with the built-in factory default (legacy
    // homepage parity, incl. the Regional Map). Persists only on Save.
    applyFactoryDefault() {
      this.layout = createDefaultLayout()
      this.isDirty = true
    },
    setEditMode(on: boolean) {
      this.editMode = on
    },
    togglePaused() {
      this.layout.refresh.paused = !this.layout.refresh.paused
      this.isDirty = true
    },
    setRefreshSeconds(seconds: number) {
      this.layout.refresh.seconds = seconds
      this.isDirty = true
    },
    setGlobalTimeframe(timeframe: Timeframe) {
      this.layout.globalTimeframe = timeframe
      this.isDirty = true
    },
    setGlobalFilter(filter: DashboardFilter) {
      this.layout.globalFilter = filter
      this.isDirty = true
    },
    // Persist grid geometry coming back from the layout engine. Collapsed panels
    // report a header-only height, so we keep their stored (expanded) height.
    syncGeometry(items: { i: string; x: number; y: number; w: number; h: number }[]) {
      let changed = false
      for (const item of items) {
        const panel = this.layout.panels.find((p) => p.id === item.i)
        if (!panel) continue
        // Don't persist grid height for collapsed (header-only) or auto-height
        // panels — those are derived at runtime, not user-set.
        const derivedH = panel.collapsed || this.resolvedHeightMode(panel) === 'auto'
        const nextH = derivedH ? panel.h : item.h
        if (panel.x !== item.x || panel.y !== item.y || panel.w !== item.w || panel.h !== nextH) {
          panel.x = item.x
          panel.y = item.y
          panel.w = item.w
          panel.h = nextH
          changed = true
        }
      }
      if (changed) this.isDirty = true
    },
    addPanel(type: string) {
      const def = getPanelDefinition(type)
      if (!def) {return}
      // place the new panel below everything currently laid out
      const nextY = this.layout.panels.reduce((max, p) => Math.max(max, p.y + p.h), 0)
      this.layout.panels.push({
        id: uuidv4(),
        type,
        x: 0,
        y: nextY,
        w: def.defaultSize.w,
        h: def.defaultSize.h,
        collapsed: false,
        titleOverride: null,
        filterOverride: null,
        timeframeOverride: null,
        refreshSeconds: null,
        options: {}
      })
      this.isDirty = true
    },
    removePanel(id: string) {
      this.layout.panels = this.layout.panels.filter((p) => p.id !== id)
      this.isDirty = true
    },
    setPanelCollapsed(id: string, collapsed: boolean) {
      const panel = this.layout.panels.find((p) => p.id === id)
      if (panel) {
        panel.collapsed = collapsed
        this.isDirty = true
      }
    },
    setPanelTitle(id: string, title: string | null) {
      const panel = this.layout.panels.find((p) => p.id === id)
      if (panel) {
        const trimmed = title?.trim()
        panel.titleOverride = trimmed ? trimmed : null
        this.isDirty = true
      }
    },
    setPanelOptions(id: string, options: Record<string, unknown>) {
      const panel = this.layout.panels.find((p) => p.id === id)
      if (panel) {
        panel.options = { ...options }
        this.isDirty = true
      }
    },
    setPanelHeightMode(id: string, mode: PanelHeightMode) {
      const panel = this.layout.panels.find((p) => p.id === id)
      if (panel) {
        panel.heightMode = mode
        this.isDirty = true
      }
    },
    tickRefresh() {
      this.refreshTick++
    }
  }
})
