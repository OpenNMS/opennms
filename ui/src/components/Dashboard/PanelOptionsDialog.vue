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

<!-- Per-panel options: height mode (all panels) + panel-type-specific settings. -->
<template>
  <PDialog
    v-model:visible="visibleModel"
    modal
    :header="`Panel options — ${title}`"
    :style="{ width: '32rem' }"
  >
    <div class="opts">
      <fieldset class="opts__group">
        <legend class="opts__legend">Panel height</legend>
        <label class="opts__radio">
          <input
            v-model="heightMode"
            type="radio"
            value="fixed"
          >
          Fixed height (scrollbar)
        </label>
        <label class="opts__radio">
          <input
            v-model="heightMode"
            type="radio"
            value="auto"
          >
          Auto-fit to content
        </label>
      </fieldset>

      <div
        v-if="panel.type === 'notes'"
        class="opts__field"
      >
        <label class="opts__label">Notes</label>
        <PTextarea
          v-model="notesText"
          rows="6"
          auto-resize
          class="opts__control"
        />
      </div>

      <div
        v-else-if="panel.type === 'html-content'"
        class="opts__field"
      >
        <label class="opts__label">Content URL</label>
        <PInputText
          v-model="htmlUrl"
          placeholder="https://… (same-origin)"
          class="opts__control"
        />
        <small class="opts__hint">
          Loaded in an iframe. External sites require the server's <code>frame-src</code> CSP to allow them.
        </small>
      </div>
    </div>

    <template #footer>
      <PButton
        text
        label="Cancel"
        @click="visibleModel = false"
      />
      <PButton
        label="Apply"
        @click="apply"
      />
    </template>
  </PDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import Textarea from 'primevue/textarea'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import type { DashboardPanel, PanelHeightMode } from '@/types/dashboard'
import { getPanelDefinition } from './registry'
import { useDashboardStore } from '@/stores/dashboardStore'

const PDialog = Dialog
const PTextarea = Textarea
const PInputText = InputText
const PButton = Button

const props = defineProps<{ panel: DashboardPanel; visible: boolean }>()
const emit = defineEmits<{ (e: 'update:visible', value: boolean): void }>()

const store = useDashboardStore()

const visibleModel = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v)
})

const title = computed(
  () => props.panel.titleOverride || getPanelDefinition(props.panel.type)?.title || props.panel.type
)

const heightMode = ref<PanelHeightMode>('auto')
const notesText = ref('')
const htmlUrl = ref('')

const syncFromPanel = () => {
  heightMode.value = store.resolvedHeightMode(props.panel)
  notesText.value = String(props.panel.options?.text ?? '')
  htmlUrl.value = String(props.panel.options?.url ?? '')
}

watch(
  () => props.visible,
  (v) => {
    if (v) syncFromPanel()
  }
)

const apply = () => {
  store.setPanelHeightMode(props.panel.id, heightMode.value)
  const opts: Record<string, unknown> = { ...props.panel.options }
  if (props.panel.type === 'notes') opts.text = notesText.value
  if (props.panel.type === 'html-content') opts.url = htmlUrl.value.trim()
  store.setPanelOptions(props.panel.id, opts)
  visibleModel.value = false
}
</script>

<style scoped lang="scss">
.opts {
  display: flex;
  flex-direction: column;
  gap: 1rem;

  &__group {
    border: 1px solid var(--feather-border-on-surface, #ddd);
    border-radius: 4px;
    padding: 0.5rem 0.75rem;
  }

  &__legend {
    font-weight: 600;
    padding: 0 0.25rem;
  }

  &__radio {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.2rem 0;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
  }

  &__label {
    font-weight: 600;
  }

  &__control {
    width: 100%;
  }

  &__hint {
    color: var(--feather-secondary-text-on-surface, #666);
  }
}
</style>
