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
    <OnmsPanel
      class="panel-frame__panel"
      :class="{ 'panel-frame__panel--missing': !panelDef }"
      :toggleable="collapsible"
      :collapsed="panel.collapsed"
      @update:collapsed="onCollapsedChange"
    >
      <template #header>
        <!-- title + (edit-mode) icons share the header row; OnmsPanel forwards
             only #header, so the icons live here instead of PrimeVue's #icons -->
        <div class="panel-frame__header">
          <a
            v-if="titleHref && !editMode"
            class="panel-frame__title panel-frame__title--link"
            :href="titleHref"
            :title="`Open ${displayTitle}`"
          >{{ displayTitle }}</a>
          <span
            v-else
            class="panel-frame__title"
          >{{ displayTitle }}</span>

          <span
            v-if="editMode"
            class="panel-frame__icons"
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
          </span>
        </div>
      </template>

      <component
        :is="panelDef.component"
        v-if="panelDef"
        :panel-id="panel.id"
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
    </OnmsPanel>
  </div>

  <PanelOptionsDialog
    v-model:visible="showOptions"
    :panel="panel"
  />
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { OnmsPanel } from '@opennms/onms-ui'
import type { DashboardPanel } from '@/types/dashboard'
import { getPanelDefinition } from './registry'
import { useDashboardStore } from '@/stores/dashboardStore'
import PanelOptionsDialog from './PanelOptionsDialog.vue'

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
// Legacy deep-link for the panel title (matches the front-page box headers).
const titleHref = computed(() => panelDef.value?.titleHref ?? null)

const resolvedFilter = computed(() => store.resolvedFilter(props.panel))
const resolvedTimeframe = computed(() => store.resolvedTimeframe(props.panel))

const heightMode = computed(() => store.resolvedHeightMode(props.panel))
const heightModeClass = computed(() => (heightMode.value === 'auto' ? 'panel-frame--auto' : 'panel-frame--fixed'))

// --- Auto-height: measure the frame div's natural height and fit the grid cell ---
let resizeObserver: ResizeObserver | null = null

const measure = () => {
  if (heightMode.value !== 'auto' || props.panel.collapsed) {
    return
  }
  const el = frameRef.value
  if (el) {
    emit('request-height', props.panel.id, el.offsetHeight)
  }
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
    // fill the grid cell minus the inter-panel gap; content scrolls inside it
    height: calc(100% - 12px);
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
    background: var(--p-content-background, #ffffff);
    color: var(--p-text-color, #1f1f1f);
    border-color: var(--p-content-border-color, #e0e0e0);

    // tighter padding so short auto panels don't round up to an extra grid row,
    // and to fit more data / read closer to the legacy dashboard density
    :deep(.p-panel-header) {
      background: transparent;
      color: var(--p-text-color, #1f1f1f);
      padding: 0.4rem 0.6rem;
    }

    :deep(.p-panel-content) {
      background: transparent;
      color: inherit;
      padding: 0.3rem 0.6rem;
    }

    &--missing {
      color: var(--p-red-500, #b00020);
    }
  }

  // title + edit icons share the header row (icons pushed right); flex:1 lets the
  // row fill the header so justify-content can separate them
  &__header {
    flex: 1 1 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    min-width: 0;
  }

  &__icons {
    display: inline-flex;
    align-items: center;
    gap: 0.15rem;
  }

  &__title {
    font-weight: 600;
    color: inherit;

    &--link {
      text-decoration: none;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  &__missing {
    color: var(--p-red-500, #b00020);
    font-style: italic;
  }
}
</style>
