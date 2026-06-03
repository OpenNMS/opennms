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

      <span
        class="dashboard-toolbar__control"
        title="Time range applied to panels that show time-based data (e.g. Availability). Other panels show current state."
      >
        <PSelect
          v-model="timeframePreset"
          :options="timeframeOptions"
          option-label="label"
          option-value="value"
          aria-label="Dashboard timeframe"
        />
      </span>

      <span
        class="dashboard-toolbar__control"
        title="How often the dashboard automatically reloads its data. Use Pause to stop auto-refresh."
      >
        <PSelect
          v-model="refreshSeconds"
          :options="refreshOptions"
          option-label="label"
          option-value="value"
          aria-label="Refresh interval"
        />
      </span>

      <PButton
        text
        :icon="isPaused ? 'pi pi-play' : 'pi pi-pause'"
        :label="isPaused ? 'Resume' : 'Pause'"
        :title="isPaused ? 'Resume automatic data refresh' : 'Pause automatic data refresh'"
        @click="store.togglePaused()"
      />

      <PButton
        text
        :icon="isFullscreen ? 'pi pi-window-minimize' : 'pi pi-window-maximize'"
        :label="isFullscreen ? 'Exit full screen' : 'Full screen'"
        title="Show the dashboard full screen — for NOC wall displays"
        @click="toggleFullscreen"
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
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

// Full-screen the dashboard content for NOC wall displays. Tries the Fullscreen
// API; if it's blocked (e.g. Permissions-Policy), falls back to a CSS "maximize"
// that fills the browser viewport over the app chrome.
const isFullscreen = ref(false)
const cssMaximized = ref(false)

const dashboardEl = () => document.getElementById('dashboard-root')

const enterCssMaximize = () => {
  dashboardEl()?.classList.add('dashboard-maximized')
  cssMaximized.value = true
  isFullscreen.value = true
}
const exitCssMaximize = () => {
  dashboardEl()?.classList.remove('dashboard-maximized')
  cssMaximized.value = false
  isFullscreen.value = false
}

const toggleFullscreen = async () => {
  const el = dashboardEl()
  if (!el) return
  if (isFullscreen.value) {
    if (document.fullscreenElement) {
      await document.exitFullscreen().catch(() => undefined)
    }
    if (cssMaximized.value) exitCssMaximize()
    return
  }
  try {
    await el.requestFullscreen()
  } catch {
    // Fullscreen API unavailable/blocked — use CSS maximize instead.
    enterCssMaximize()
  }
}

const onFullscreenChange = () => {
  // keep state in sync when the user exits via Esc
  if (!cssMaximized.value) isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => document.addEventListener('fullscreenchange', onFullscreenChange))
onBeforeUnmount(() => document.removeEventListener('fullscreenchange', onFullscreenChange))
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
