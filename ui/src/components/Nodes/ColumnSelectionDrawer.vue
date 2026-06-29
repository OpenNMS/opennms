<template>
  <Drawer
    v-model:visible="drawerVisible"
    position="right"
    header="Customize Columns"
    :style="{ width: '55em' }"
  >
    <div class="drawer-content">
      <section>
        <h3>Customize the available columns</h3>
        <p>Select which columns you wish to showcase</p>
      </section>
      <div class="spacer-large"></div>
      <Draggable
        v-model="selectedColumns"
        item-key="value"
        handle=".drag-handle"
        class="columns-drag-container"
      >
        <template #item="{ element, index }">
          <div class="column-row">
            <Button text class="drag-btn">
              <FeatherIcon class="close-icon drag-handle" :icon="Apps" />
            </Button>
            <Select
              v-model="element.value"
              :options="getAvailableOptions(index)"
              optionLabel="name"
              optionValue="value"
              :placeholder="`Column ${index + 1}`"
              class="columns-selector"
            />
            <Button
              text
              :data-test="`remove-column-${index}`"
              @click="removeColumn(index)"
            >
              <FeatherIcon class="close-icon" :icon="Cancel" />
            </Button>
          </div>
        </template>
      </Draggable>
      <div class="spacer-medium"></div>
      <div class="button-row">
        <Button @click="customizeTable">Save</Button>
        <Button
          outlined
          :disabled="selectedColumns.length >= 10"
          @click="addColumn"
        >Add Column</Button>
        <Button outlined @click="resetColumns">Reset Columns</Button>
        <Button outlined @click="nodeStructureStore.columnsDrawerState.visible = false">Close</Button>
      </div>
    </div>
  </Drawer>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'

import { FeatherIcon } from '@featherds/icon'
import Apps from '@featherds/icon/navigation/Apps'
import Cancel from '@featherds/icon/navigation/Cancel'
import Draggable from 'vuedraggable'
import Button from 'primevue/button'
import Drawer from 'primevue/drawer'
import Select from 'primevue/select'
import { saveNodePreferences } from '@/services/localStorageService'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { NodeColumnSelectionItem } from '@/types'
import { defaultColumns } from './utils'

const nodeStructureStore = useNodeStructureStore()
const columns = ref<NodeColumnSelectionItem[]>(defaultColumns)
const selectedColumns = ref<{ name: string; value: string }[]>([])

const drawerVisible = computed({
  get: () => nodeStructureStore.columnsDrawerState.visible,
  set: (val: boolean) => {
    if (!val) {
      nodeStructureStore.columnsDrawerState.visible = false
    }
  }
})

const initializeSelectedColumns = (columns: NodeColumnSelectionItem[]) => {
  selectedColumns.value = columns
    .filter(col => col.selected)
    .sort((a, b) => a.order - b.order)
    .map(col => ({ name: col.label, value: col.id }))
}

const getAvailableOptions = (currentIndex: number) => {
  const currentSelection = selectedColumns.value[currentIndex]?.value

  return columns.value
    .filter(col =>
      !selectedColumns.value.some((sc, i) => i !== currentIndex && sc.value === col.id) ||
      col.id === currentSelection
    )
    .map(col => ({ name: col.label, value: col.id }))
}

const addColumn = () => {
  if (selectedColumns.value.length < 10) {
    selectedColumns.value = [
      ...selectedColumns.value,
      { name: '', value: '' }
    ]
  }
}

const removeColumn = (index: number) => {
  selectedColumns.value = selectedColumns.value.filter((_, i) => i !== index)
}

const customizeTable = async () => {
  // Resolve each column's label from the canonical definitions by id. The
  // per-row Select binds `optionValue="value"`, so selecting a column updates
  // only `col.value` (the id) and never `col.name` — trusting `col.name` here
  // persisted an empty label for any re-added/changed column, which rendered as
  // a header with no text. The id is always correct, so derive the label from it.
  nodeStructureStore.columns = selectedColumns.value
    .filter(col => col.value)
    .map((col, index) => ({
      id: col.value as string,
      label: columns.value.find(c => c.id === col.value)?.label ?? (col.name as string),
      selected: true,
      order: index
    }))

  const nodePrefs = await nodeStructureStore.getNodePreferences()
  saveNodePreferences(nodePrefs)
  nodeStructureStore.columnsDrawerState.visible = false
}

const resetColumns = async () => {
  nodeStructureStore.columns = [...defaultColumns]
  const nodePrefs = await nodeStructureStore.getNodePreferences()
  saveNodePreferences(nodePrefs)
  nodeStructureStore.columnsDrawerState.visible = false
}

watch(() => nodeStructureStore.columns, (newColumns) => {
  initializeSelectedColumns(newColumns)
}, { immediate: true, deep: true })
</script>

<style lang="scss" scoped>
.drawer-content {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.spacer-large {
  margin-bottom: 2rem;
}

.spacer-medium {
  margin-bottom: 0.25rem;
}

.column-row {
  display: flex;
  gap: 1rem;
  width: 80%;
  margin-bottom: 1rem;
  border: 1px solid var(--p-content-border-color);
  padding-left: 10px;
  padding-top: 3px;
  padding-bottom: 3px;
  border-radius: 5px;
  align-items: center;
}

.columns-selector {
  width: 80%;
}

.drag-btn {
  cursor: grab;
  padding: 0;
}

.button-row {
  display: flex;
  flex-direction: row;
  gap: 1rem;
  align-items: flex-start;
}
</style>
