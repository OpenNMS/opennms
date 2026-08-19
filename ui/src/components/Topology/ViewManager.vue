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
  Saved-views catalog dialog. Lists the shared topology views from the
  /api/v2/topology/views resource and lets the user open or delete one.
  Opening emits an event the page handles by loading the view into the
  canvas; deleting goes straight through the store.
-->

<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Saved views"
    width="42rem"
    @update:visible="emit('update:visible', $event)"
  >
    <div v-if="loading" class="tv-empty">Loading views&hellip;</div>
    <div v-else-if="loadError" class="tv-empty">
      <p>Couldn't load saved views.</p>
      <OnmsButton label="Retry" size="small" variant="text" @click="refresh" />
    </div>
    <div v-else-if="store.catalog.length === 0" class="tv-empty">
      No saved views yet. Compose a canvas and use <strong>Save</strong> to create one.
    </div>
    <OnmsTable v-else :value="store.catalog" dataKey="id" :rows="10" paginator>
      <OnmsColumn field="name" header="Name" sortable />
      <OnmsColumn header="" :style="{ width: '16rem' }">
        <template #body="{ data }">
          <div class="tv-row-actions">
            <OnmsButton label="Open" size="small" variant="text" @click="onOpen(data.id)" />
            <OnmsButton label="Rename" size="small" variant="text" @click="onRename(data.id, data.name)" />
            <OnmsButton
              label="Delete"
              size="small"
              variant="text"
              severity="danger"
              @click="onDelete(data.id, data.name)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { OnmsButton, OnmsColumn, OnmsDialog, OnmsTable, useOnmsToast } from '@opennms/onms-ui'
import { useTopologyStore } from '@/stores/topologyStore'

const { showToast } = useOnmsToast()

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'open', id: string): void
}>()

const store = useTopologyStore()

const loading = ref(false)
const loadError = ref(false)

const refresh = async () => {
  loading.value = true
  loadError.value = false
  const ok = await store.refreshCatalog()
  loading.value = false
  loadError.value = !ok
}

// Refresh the catalog from the server each time the dialog opens.
watch(
  () => props.visible,
  (open) => {
    if (open) {
      refresh()
    }
  }
)

const onOpen = (id: string) => {
  emit('open', id)
  emit('update:visible', false)
}

const onRename = async (id: string, currentName: string) => {
  const name = window.prompt('Rename view:', currentName)
  if (!name || name.trim() === '' || name === currentName) {
    return
  }
  const ok = await store.renameView(id, name.trim())
  showToast(
    ok
      ? { message: `View renamed to "${name.trim()}"`, severity: 'success', timeout: 3000 }
      : { message: `Could not rename view "${currentName}"`, severity: 'error', timeout: 5000 }
  )
}

const onDelete = async (id: string, name: string) => {
  if (!window.confirm(`Delete view "${name}"? This cannot be undone.`)) {
    return
  }
  const ok = await store.removeView(id)
  showToast(
    ok
      ? { message: `View "${name}" deleted`, severity: 'success', timeout: 3000 }
      : { message: `Could not delete view "${name}"`, severity: 'error', timeout: 5000 }
  )
}
</script>

<style scoped>
.tv-empty {
  padding: 1rem 0.25rem;
  color: var(--onms-secondary-text-on-surface);
}

.tv-row-actions {
  display: flex;
  gap: 0.25rem;
}
</style>
