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
  Chrome around a single dashboard panel: title, collapse, and (in edit mode)
  rename / options / remove. Renders the registered panel component and feeds it
  the resolved filter / timeframe / refresh contracts.

  Height mode: 'fixed' = fixed grid height with an internal scrollbar; 'auto' =
  the panel measures its natural content height and asks DashboardGrid to size
  the grid cell to fit (so e.g. empty list panels shrink to a couple of lines).
-->
<template>
  <PPanel
    ref="frameRef"
    class="panel-frame"
    :class="[heightModeClass, { 'panel-frame--missing': !panelDef }]"
    :header="displayTitle"
    :toggleable="collapsible"
    :collapsed="panel.collapsed"
    @update:collapsed="onCollapsedChange"
  >
    <template
      v-if="editMode"
      #icons
    >
      <button
        v-if="renamable"
        type="button"
        class="p-panel-header-icon"
        title="Rename panel"
        @click="onRename"
      >
        <i class="pi pi-pencil" />
      </button>
      <button
        type="button"
        class="p-panel-header-icon"
        title="Panel options"
        @click="showOptions = true"
      >
        <i class="pi pi-cog" />
      </button>
      <button
        type="button"
        class="p-panel-header-icon"
        title="Remove panel"
        @click="onRemove"
      >
        <i class="pi pi-times" />
      </button>
    </template>

    <component
      :is="panelDef.component"
      v-if="panelDef"
      :options="panel.options"
      :filter="resolvedFilter"
      :timeframe="resolvedTimeframe"
      :refresh-tick="refreshTick"
    />
    <div
      v-else
      class="panel-frame__missing"
    >
      Unknown panel type: <code>{{ panel.type }}</code>
    </div>
  </PPanel>

  <PanelOptionsDialog
    v-model:visible="showOptions"
    :panel="panel"
  />
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import Panel from 'primevue/panel'
import type { DashboardPanel } from '@/types/dashboard'
import { getPanelDefinition } from './registry'
import { useDashboardStore } from '@/stores/dashboardStore'
import PanelOptionsDialog from './PanelOptionsDialog.vue'

const PPanel = Panel

const props = defineProps<{ panel: DashboardPanel }>()
const emit = defineEmits<{ (e: 'request-height', id: string, px: number): void }>()

const store = useDashboardStore()
const { editMode, refreshTick } = storeToRefs(store)

const frameRef = ref<{ $el?: HTMLElement } | null>(null)
const showOptions = ref(false)

const panelDef = computed(() => getPanelDefinition(props.panel.type))
const collapsible = computed(() => panelDef.value?.collapsible !== false)
const renamable = computed(() => panelDef.value?.renamable !== false)

const displayTitle = computed(() => props.panel.titleOverride || panelDef.value?.title || props.panel.type)

const resolvedFilter = computed(() => store.resolvedFilter(props.panel))
const resolvedTimeframe = computed(() => store.resolvedTimeframe(props.panel))

const heightMode = computed(() => store.resolvedHeightMode(props.panel))
const heightModeClass = computed(() => (heightMode.value === 'auto' ? 'panel-frame--auto' : 'panel-frame--fixed'))

// --- Auto-height: measure natural content and ask the grid to fit the cell ---
let resizeObserver: ResizeObserver | null = null

const measure = () => {
  if (heightMode.value !== 'auto' || props.panel.collapsed) return
  const el = frameRef.value?.$el
  if (el) emit('request-height', props.panel.id, el.offsetHeight)
}

const setupObserver = () => {
  resizeObserver?.disconnect()
  resizeObserver = null
  const el = frameRef.value?.$el
  if (heightMode.value === 'auto' && el) {
    resizeObserver = new ResizeObserver(() => measure())
    resizeObserver.observe(el)
  }
  nextTick(measure)
}

onMounted(setupObserver)
watch([heightMode, () => props.panel.collapsed], setupObserver)
onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
})

const onCollapsedChange = (collapsed: boolean) => {
  store.setPanelCollapsed(props.panel.id, collapsed)
}

const onRename = () => {
  // eslint-disable-next-line no-alert
  const next = window.prompt('Panel title', displayTitle.value)
  if (next !== null) {
    store.setPanelTitle(props.panel.id, next)
  }
}

const onRemove = () => {
  store.removePanel(props.panel.id)
}
</script>

<style scoped lang="scss">
.panel-frame {
  min-width: 0;

  &--fixed {
    // fill the grid cell; content scrolls inside it. The full PrimeVue 4 chain is
    // .p-panel > .p-panel-content-container > .p-panel-content-wrapper > .p-panel-content
    // — every wrapper must flex down for the content to get a bounded height.
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.p-panel-content-container),
    :deep(.p-panel-content-wrapper) {
      flex: 1 1 auto;
      min-height: 0;
      display: flex;
      flex-direction: column;
    }

    :deep(.p-panel-content) {
      flex: 1 1 auto;
      min-height: 0;
      overflow: auto;
    }
  }

  &--auto {
    // size to content; the grid cell is fitted to this via request-height
    height: auto;

    :deep(.p-panel-content) {
      overflow: visible;
    }
  }

  &__missing {
    color: var(--feather-error, #b00020);
    font-style: italic;
  }
}
</style>
