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
  Chrome around a single dashboard panel. The outer <div> (frameRef) is the grid
  item content and the element we measure for auto-height (PrimeVue's component
  ref doesn't reliably expose a DOM node). Panel surface/text use Feather theme
  variables so the dashboard follows light/dark mode (the .open-dark theme).
-->
<template>
  <div
    ref="frameRef"
    class="panel-frame"
    :class="heightModeClass"
  >
    <PPanel
      class="panel-frame__panel"
      :class="{ 'panel-frame__panel--missing': !panelDef }"
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
  </div>

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

const frameRef = ref<HTMLElement | null>(null)
const showOptions = ref(false)

const panelDef = computed(() => getPanelDefinition(props.panel.type))
const collapsible = computed(() => panelDef.value?.collapsible !== false)
const renamable = computed(() => panelDef.value?.renamable !== false)

const displayTitle = computed(() => props.panel.titleOverride || panelDef.value?.title || props.panel.type)

const resolvedFilter = computed(() => store.resolvedFilter(props.panel))
const resolvedTimeframe = computed(() => store.resolvedTimeframe(props.panel))

const heightMode = computed(() => store.resolvedHeightMode(props.panel))
const heightModeClass = computed(() => (heightMode.value === 'auto' ? 'panel-frame--auto' : 'panel-frame--fixed'))

// --- Auto-height: measure the frame div's natural height and fit the grid cell ---
let resizeObserver: ResizeObserver | null = null

const measure = () => {
  if (heightMode.value !== 'auto' || props.panel.collapsed) return
  const el = frameRef.value
  if (el) emit('request-height', props.panel.id, el.offsetHeight)
}

const setupObserver = () => {
  resizeObserver?.disconnect()
  resizeObserver = null
  if (heightMode.value === 'auto' && frameRef.value) {
    resizeObserver = new ResizeObserver(() => measure())
    resizeObserver.observe(frameRef.value)
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
    // fill the grid cell; content scrolls inside it
    height: 100%;
    display: flex;
    flex-direction: column;

    .panel-frame__panel {
      flex: 1 1 auto;
      min-height: 0;
      display: flex;
      flex-direction: column;

      // PrimeVue 4 chain: content-container > content-wrapper > content
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
  }

  &--auto {
    height: auto;

    .panel-frame__panel {
      height: auto;
    }

    :deep(.p-panel-content) {
      overflow: visible;
    }
  }

  // Theme via Feather variables so panels follow light/dark mode.
  .panel-frame__panel {
    background: var(--feather-elevation-background-2, #ffffff);
    color: var(--feather-primary-text-on-surface, #1f1f1f);
    border-color: var(--feather-border-on-surface, #e0e0e0);

    // tighter padding so short auto panels don't round up to an extra grid row
    :deep(.p-panel-header) {
      background: transparent;
      color: var(--feather-primary-text-on-surface, #1f1f1f);
      padding: 0.5rem 0.75rem;
    }

    :deep(.p-panel-content) {
      background: transparent;
      color: inherit;
      padding: 0.5rem 0.75rem;
    }

    &--missing {
      color: var(--feather-error, #b00020);
    }
  }

  &__missing {
    color: var(--feather-error, #b00020);
    font-style: italic;
  }
}
</style>
