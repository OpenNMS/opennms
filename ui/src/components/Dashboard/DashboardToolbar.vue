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
  Dashboard-level controls: global timeframe, refresh interval + pause (NMS-4404),
  and the edit-mode affordances (add panel, save, done). The global filter control
  (NMS-10507) lands in milestone 2 alongside drag/resize.
-->
<template>
  <div class="dashboard-toolbar">
    <h2 class="dashboard-toolbar__title">Home</h2>

    <div class="dashboard-toolbar__controls">
      <DashboardFilterControl />

      <PSelect
        v-model="timeframePreset"
        :options="timeframeOptions"
        option-label="label"
        option-value="value"
        aria-label="Dashboard timeframe"
      />

      <PSelect
        v-model="refreshSeconds"
        :options="refreshOptions"
        option-label="label"
        option-value="value"
        aria-label="Refresh interval"
      />

      <PButton
        text
        :icon="isPaused ? 'pi pi-play' : 'pi pi-pause'"
        :label="isPaused ? 'Resume' : 'Pause'"
        @click="store.togglePaused()"
      />

      <template v-if="editMode">
        <PSelect
          v-model="panelToAdd"
          :options="addablePanels"
          option-label="title"
          option-value="type"
          placeholder="Add panel…"
          :show-clear="false"
          aria-label="Add panel"
          @change="onAdd"
        />
        <PButton
          label="Save"
          icon="pi pi-save"
          :disabled="!isDirty"
          :loading="isSaving"
          @click="store.save()"
        />
        <PButton
          text
          label="Done"
          @click="store.setEditMode(false)"
        />
      </template>
      <PButton
        v-else
        outlined
        label="Edit"
        icon="pi pi-pencil"
        @click="store.setEditMode(true)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import Button from 'primevue/button'
import Select from 'primevue/select'
import { TimeframePreset } from '@/types/dashboard'
import { refreshOptions, timeframeOptions } from './timeframe'
import { listPanelDefinitions } from './registry'
import DashboardFilterControl from './DashboardFilterControl.vue'
import { useDashboardStore } from '@/stores/dashboardStore'

const PButton = Button
const PSelect = Select

const store = useDashboardStore()
const { editMode, isDirty, isSaving, isPaused } = storeToRefs(store)

const panelToAdd = ref<string | null>(null)

const timeframePreset = computed<TimeframePreset>({
  get: () => store.layout.globalTimeframe.preset,
  set: (preset) => store.setGlobalTimeframe({ preset, from: null, to: null })
})

const refreshSeconds = computed<number>({
  get: () => store.layout.refresh.seconds,
  set: (seconds) => store.setRefreshSeconds(seconds)
})

// Offer every registered panel; duplicates are allowed (e.g. two filtered copies).
const addablePanels = computed(() => listPanelDefinitions().map((d) => ({ type: d.type, title: d.title })))

const onAdd = () => {
  if (panelToAdd.value) {
    store.addPanel(panelToAdd.value)
    panelToAdd.value = null
  }
}
</script>

<style scoped lang="scss">
.dashboard-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding: 0.75rem 1rem;

  &__title {
    margin: 0;
  }

  &__controls {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-wrap: wrap;
  }
}
</style>
