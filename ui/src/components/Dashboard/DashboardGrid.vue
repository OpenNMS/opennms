<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!--
  The layout engine (milestone 2): grid-layout-plus provides drag + resize on a
  12-column grid, enabled only in edit mode. This component is the only place that
  knows about geometry; PanelFrame and the panel components are unchanged by it.
  Geometry flows grid -> store via @layout-updated; panel add/remove/collapse
  flow store -> grid via the reconcile watch (never on pure geometry changes, so
  we don't fight the user mid-drag).
-->
<template>
  <div
    class="dashboard-grid"
    :class="{ 'dashboard-grid--edit': editMode }"
  >
    <GridLayout
      v-if="layout.length"
      v-model:layout="layout"
      :col-num="12"
      :row-height="ROW_HEIGHT"
      :margin="[12, 0]"
      :is-draggable="editMode"
      :is-resizable="editMode"
      :vertical-compact="true"
      @layout-updated="onLayoutUpdated"
    >
      <GridItem
        v-for="item in layout"
        :key="item.i"
        :i="item.i"
        :x="item.x"
        :y="item.y"
        :w="item.w"
        :h="item.h"
        :min-w="minW(item.i)"
        :min-h="minH(item.i)"
        :is-resizable="editMode && !isAuto(item.i)"
        drag-allow-from=".p-panel-header"
      >
        <PanelFrame
          v-if="getPanel(item.i)"
          :panel="(getPanel(item.i) as DashboardPanel)"
          @request-height="onRequestHeight"
        />
      </GridItem>
    </GridLayout>
    <div
      v-else
      class="dashboard-grid__empty"
    >
      No panels. Use “Edit” → “Add panel” to add one.
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
// grid-layout-plus 1.x injects its own styles at runtime — no CSS import needed.
import { GridLayout, GridItem } from 'grid-layout-plus'
import PanelFrame from './PanelFrame.vue'
import { getPanelDefinition } from './registry'
import { useDashboardStore } from '@/stores/dashboardStore'
import type { DashboardPanel } from '@/types/dashboard'

// PIXEL-BASED grid. row-height is 1px and the vertical margin is 0, so a grid
// cell is exactly `h` pixels tall — eliminating the coarse ~56px row quantization
// that left big gaps under short panels. Vertical spacing between panels comes
// from GAP (baked into each cell's h); the panel itself does not fill the gap.
const ROW_HEIGHT = 1
const GAP = 12 // px of empty space below each panel (visual gap between rows)
const COLLAPSED_H = 40 + GAP // header-only height when a panel is collapsed
const MIN_AUTO_H = 40 + GAP
const MIN_FIXED_H = 96

// Old saved layouts (and the default) store h/y in ~44px ROW units; pixel h is
// always > 40. Convert small values up so legacy layouts render at sane sizes;
// values already in pixels pass through unchanged (idempotent).
const toPx = (h: number) => (h <= 40 ? Math.round(h * 56) : h)

interface GridItemModel {
  i: string
  x: number
  y: number
  w: number
  h: number
}

const store = useDashboardStore()
const { panels, editMode, layoutRevision } = storeToRefs(store)

const getPanel = (id: string): DashboardPanel | undefined => panels.value.find((p) => p.id === id)

const toItem = (p: DashboardPanel): GridItemModel => ({
  i: p.id,
  x: p.x,
  y: p.y,
  w: p.w,
  h: p.collapsed ? COLLAPSED_H : toPx(p.h)
})

const layout = ref<GridItemModel[]>(panels.value.map(toItem))

const minW = (id: string) => getPanelDefinition(getPanel(id)?.type ?? '')?.minSize?.w ?? 2
// Auto-height panels size to content, so they may shrink to their content height.
const minH = (id: string) => {
  const p = getPanel(id)
  if (p && !p.collapsed && store.resolvedHeightMode(p) === 'auto') return MIN_AUTO_H
  return MIN_FIXED_H
}

// Vertical compaction: place each item at the lowest non-colliding y within its
// columns. grid-layout-plus does NOT recompact on programmatic height changes,
// so we do it ourselves to guarantee no overlaps after auto-height resizes /
// add / remove. Only mutates the local layout (positions re-derive on load).
const compactLayout = () => {
  const sorted = [...layout.value].sort((a, b) => a.y - b.y || a.x - b.x)
  const placed: GridItemModel[] = []
  for (const it of sorted) {
    let bottom = 0
    for (const p of placed) {
      const xOverlap = it.x < p.x + p.w && p.x < it.x + it.w
      if (xOverlap) bottom = Math.max(bottom, p.y + p.h)
    }
    it.y = bottom
    placed.push(it)
  }
  // ALWAYS reassign with FRESH item objects. grid-layout-plus only reacts to a
  // change of the layout array reference / length (watch on [layout, length]) —
  // it ignores in-place h/y mutations. A new array of new objects forces it to
  // re-read and re-render (this is what makes auto-height + compaction apply).
  layout.value = layout.value.map((it) => ({ ...it }))
}

// A new authoritative layout (load / reset / factory default) fully replaces the
// grid geometry — otherwise a saved arrangement never reaches the rendered grid,
// because the id+collapsed reconcile key below is unchanged across a load.
watch(layoutRevision, () => {
  layout.value = panels.value.map(toItem)
  compactLayout()
})

// Reconcile the grid model on add/remove/collapse only — keeping existing items'
// live x/y/w so an in-flight drag isn't reset.
watch(
  () => panels.value.map((p) => `${p.id}:${p.collapsed}`).join('|'),
  () => {
    const byId = new Map(layout.value.map((it) => [it.i, it]))
    layout.value = panels.value.map((p) => {
      const existing = byId.get(p.id)
      if (existing) {
        existing.h = p.collapsed ? COLLAPSED_H : toPx(p.h)
        return existing
      }
      return toItem(p)
    })
    compactLayout()
  }
)

const isAuto = (id: string): boolean => {
  const p = getPanel(id)
  return !!p && !p.collapsed && store.resolvedHeightMode(p) === 'auto'
}

// An auto-height panel reported its natural pixel height; size the grid cell to
// exactly that height plus the inter-panel GAP (pixel-perfect, no quantization).
const onRequestHeight = (id: string, px: number) => {
  const item = layout.value.find((it) => it.i === id)
  if (!item || !isAuto(id)) return
  const h = Math.max(MIN_AUTO_H, Math.round(px) + GAP)
  if (item.h !== h) {
    item.h = h
    compactLayout()
  }
}

const onLayoutUpdated = (newLayout: GridItemModel[]) => {
  // syncGeometry itself ignores non-edit-mode calls, but avoid the churn too
  if (editMode.value) {
    store.syncGeometry(newLayout)
  }
}

// After initial render, once panels have reported their auto heights, compact the
// columns so there are no leftover gaps between short panels.
onMounted(() => {
  nextTick(compactLayout)
  setTimeout(compactLayout, 400)
  setTimeout(compactLayout, 1200)
})
</script>

<style scoped lang="scss">
.dashboard-grid {
  // scroll the panel area internally so the dashboard never overflows the footer
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 1rem;
  padding-bottom: 1.5rem;

  &--edit :deep(.vgl-item) {
    cursor: move;
  }

  &__empty {
    padding: 2rem;
    text-align: center;
    color: var(--p-text-muted-color, #666);
  }
}
</style>
