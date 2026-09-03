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
  Palette of OpenNMS nodes available to drag onto the canvas. Backed by
  topologyService.fetchPaletteNodes (the real /api/v2/nodes inventory).

  Each list item is HTML5-draggable; the drop target is the canvas, which
  reads the JSON payload from dataTransfer with the custom MIME type
  declared in PALETTE_DRAG_MIME.
-->

<template>
  <div class="topology-palette">
    <div class="palette-header">
      <OnmsSearchInput
        v-model="searchText"
        placeholder="Search nodes..."
        class="palette-search"
        aria-label="Search palette nodes"
      />
      <OnmsMultiSelect
        v-model="selectedCategories"
        :options="availableCategories"
        placeholder="Category"
        display="chip"
        class="palette-category-filter"
        :showToggleAll="false"
        :maxSelectedLabels="2"
      />
    </div>

    <div class="palette-list-container">
      <div v-if="loading" class="palette-status">Loading nodes&hellip;</div>
      <div v-else-if="loadError" class="palette-status">
        <p>Couldn't load nodes.</p>
        <OnmsButton label="Retry" size="small" variant="text" @click="loadNodes" />
      </div>
      <div v-else-if="allNodes.length === 0" class="palette-status">No nodes available.</div>
      <div v-else-if="filteredNodes.length === 0" class="palette-status">
        No nodes match your search or filters.
      </div>
      <OnmsVirtualScroller
        v-else
        :items="filteredNodes"
        :itemSize="56"
        class="palette-list"
      >
        <template #item="{ item }">
          <div
            class="palette-item"
            draggable="true"
            :data-node-id="item.id"
            @dragstart="onDragStart($event, item)"
          >
            <div class="palette-item-label">{{ item.label }}</div>
            <div class="palette-item-meta">
              <span class="palette-item-location">{{ item.location }}</span>
              <span
                v-for="cat in item.categories"
                :key="cat.id"
                class="palette-item-category"
              >{{ cat.name }}</span>
            </div>
          </div>
        </template>
      </OnmsVirtualScroller>
    </div>

    <div class="palette-footer">
      <span>{{ filteredNodes.length }} of {{ allNodes.length }} nodes</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { OnmsButton, OnmsMultiSelect, OnmsSearchInput, OnmsVirtualScroller } from '@opennms/onms-ui'
import { fetchPaletteNodes } from '@/services/topologyService'
import { PALETTE_DRAG_MIME, type PaletteDragPayload } from '@/components/Topology/dragTypes'
import { useTopologyStore } from '@/stores/topologyStore'
import type { Node } from '@/types'


const store = useTopologyStore()

const allNodes = ref<Node[]>([])
const searchText = ref('')
const selectedCategories = ref<string[]>([])
const loading = ref<boolean>(true)
const loadError = ref<boolean>(false)

const availableCategories = computed<string[]>(() => {
  const set = new Set<string>()
  allNodes.value.forEach(n => n.categories?.forEach(c => set.add(c.name)))
  return Array.from(set).sort()
})

const filteredNodes = computed<Node[]>(() => {
  const q = searchText.value.trim().toLowerCase()
  const cats = selectedCategories.value
  const placed = store.placedNodeIds
  return allNodes.value.filter((n) => {
    if (placed.has(n.id)) {
      return false
    }
    if (q && !n.label.toLowerCase().includes(q)) {
      return false
    }
    if (cats.length > 0) {
      const nodeCatNames = (n.categories ?? []).map(c => c.name)
      if (!cats.some(c => nodeCatNames.includes(c))) {
        return false
      }
    }
    return true
  })
})

const loadNodes = async () => {
  loading.value = true
  loadError.value = false
  const resp = await fetchPaletteNodes({ limit: 200 })
  loading.value = false
  if (resp === false) {
    loadError.value = true
    return
  }
  allNodes.value = resp.node
}

const onDragStart = (event: DragEvent, node: Node) => {
  if (!event.dataTransfer) {
    return
  }
  const payload: PaletteDragPayload = {
    nodeId: node.id,
    label: node.label
  }
  event.dataTransfer.setData(PALETTE_DRAG_MIME, JSON.stringify(payload))
  event.dataTransfer.effectAllowed = 'copy'
}

onMounted(() => {
  loadNodes()
})
</script>

<style scoped>
.topology-palette {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--onms-surface);
  border: 1px solid var(--onms-border-on-surface);
  border-radius: 4px;
  min-width: 260px;
  width: 280px;
}

.palette-header {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.5rem;
  border-bottom: 1px solid var(--onms-border-on-surface);
}

.palette-search,
.palette-category-filter {
  width: 100%;
}

.palette-list-container {
  flex: 1 1 auto;
  min-height: 0;
  position: relative;
}

.palette-list {
  height: 100%;
  width: 100%;
}

.palette-item {
  height: 56px;
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid var(--onms-border-on-surface);
  cursor: grab;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.125rem;
  user-select: none;
}

.palette-item:hover {
  background: color-mix(in srgb, var(--onms-topology-accent) 10%, transparent);
}

.palette-item:active {
  cursor: grabbing;
}

.palette-item-label {
  font-weight: 500;
  font-size: 0.875rem;
  color: var(--onms-primary-text-on-surface);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.palette-item-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--onms-secondary-text-on-surface);
  overflow: hidden;
}

.palette-item-location {
  font-style: italic;
}

.palette-item-category {
  background: color-mix(in srgb, var(--onms-topology-accent) 10%, transparent);
  border-radius: 3px;
  padding: 0 0.375rem;
  font-size: 0.6875rem;
  color: var(--onms-primary-text-on-surface);
}

.palette-status {
  padding: 1rem;
  text-align: center;
  color: var(--onms-secondary-text-on-surface);
  font-size: 0.875rem;
}

.palette-footer {
  padding: 0.5rem;
  border-top: 1px solid var(--onms-border-on-surface);
  font-size: 0.75rem;
  color: var(--onms-secondary-text-on-surface);
  text-align: right;
}
</style>
