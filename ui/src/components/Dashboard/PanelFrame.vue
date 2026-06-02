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
  the resolved filter / timeframe / refresh contracts. Drag & resize are added
  in milestone 2 with grid-layout-plus; this frame stays unchanged when they are.
-->
<template>
  <PPanel
    class="panel-frame"
    :class="{ 'panel-frame--missing': !panelDef }"
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
        @click="onOptions"
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
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import Panel from 'primevue/panel'
import type { DashboardPanel } from '@/types/dashboard'
import { getPanelDefinition } from './registry'
import { useDashboardStore } from '@/stores/dashboardStore'

const PPanel = Panel

const props = defineProps<{ panel: DashboardPanel }>()

const store = useDashboardStore()
const { editMode, refreshTick } = storeToRefs(store)

const panelDef = computed(() => getPanelDefinition(props.panel.type))
const collapsible = computed(() => panelDef.value?.collapsible !== false)
const renamable = computed(() => panelDef.value?.renamable !== false)

const displayTitle = computed(() => props.panel.titleOverride || panelDef.value?.title || props.panel.type)

const resolvedFilter = computed(() => store.resolvedFilter(props.panel))
const resolvedTimeframe = computed(() => store.resolvedTimeframe(props.panel))

const onCollapsedChange = (collapsed: boolean) => {
  store.setPanelCollapsed(props.panel.id, collapsed)
}

const onRename = () => {
  // Placeholder editor; milestone 5 replaces this with an inline / dialog editor.
  // eslint-disable-next-line no-alert
  const next = window.prompt('Panel title', displayTitle.value)
  if (next !== null) {
    store.setPanelTitle(props.panel.id, next)
  }
}

const onOptions = () => {
  // Per-panel options dialog (content / filter / timeframe / refresh overrides)
  // is milestone 5. Stubbed for now.
  console.info('Panel options not yet implemented for', props.panel.id)
}

const onRemove = () => {
  store.removePanel(props.panel.id)
}
</script>

<style scoped lang="scss">
// Fill the grid cell provided by DashboardGrid (grid-layout-plus GridItem).
.panel-frame {
  height: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;

  :deep(.p-panel-content) {
    overflow: auto;
  }

  &__missing {
    color: var(--feather-error, #b00020);
    font-style: italic;
  }
}
</style>
