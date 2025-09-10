<template>
  <FeatherDrawer
    id="column-selection-drawer"
    data-test="column-selection-drawer"
    @shown="() => nodeStructureStore.columnsDrawerState.visible"
    v-model="nodeStructureStore.columnsDrawerState.visible"
    :labels="{ close: 'close', title: 'Customize Columns' }"
    width="55em"
  >
    <div class="feather-drawer-custom-padding">
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
        <template #item="{ index }">
          <div class="column-row">
            <FeatherButton icon="Apps" text>
              <FeatherIcon class="close-icon drag-handle" :icon="Apps" />
            </FeatherButton>
            <FeatherSelect
              v-model="selectedColumns[index]"
              :options="getAvailableOptions(index)"
              text-prop="name"
              value-prop="value"
              :placeholder="'Select column...'"
              :label="`Column ${index + 1}`"
              class="columns-selector"
            />
            <FeatherButton icon="Cancel" text @click="removeColumn(index)">
              <FeatherIcon class="close-icon" :icon="Cancel" />
            </FeatherButton>
          </div>
        </template>
      </Draggable>
      <div class="spacer-medium"></div>
      <div class="button-column">
        <FeatherButton
          secondary
          :disabled="selectedColumns.length >= 10"
          @click="addColumn"
        >
          Add Column
        </FeatherButton>
        <FeatherButton
          secondary
          @click="resetColumns"
        >
          Reset Columns
        </FeatherButton>
        <FeatherButton
          primary
          @click="customizeTable"
        >
          Customize Table
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import Apps from '@featherds/icon/navigation/Apps'
import Cancel from '@featherds/icon/navigation/Cancel'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import Draggable from 'vuedraggable'
import { saveNodePreferences } from '@/services/localStorageService'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { NodeColumnSelectionItem } from '@/types'
import { defaultColumns } from './utils'

const nodeStructureStore = useNodeStructureStore()
const columns = ref<NodeColumnSelectionItem[]>(defaultColumns)
const selectedColumns = ref<ISelectItemType[]>([])

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

const customizeTable = async() => {
  nodeStructureStore.columns = selectedColumns.value.map((col, index) => ({
    id: col.value as string,
    label: col.name as string,
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
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.feather-drawer-custom-padding {
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

.footer {
  display: flex;
  padding-top: 20px;
}

.column-row {
  display: flex;
  gap: 1rem;
  width: 80%;
  margin-bottom: 1rem;
  border: 1px solid var($border-on-surface);
  padding-left: 10px;
  padding-top: 3px;
  padding-bottom: 3px;
  border-radius: 5px;
}

.column-header {
  font-weight: bold;
  width: 100px;
}

button.primary {
  margin-top: 2rem;
  background-color: #1d2f75;
  color: white;
  padding: 0.5em 1.5em;
  border: none;
}

.columns-selector {
    width: 80%;
}

:deep(.feather-input-sub-text) {
    display: none;
}

.button-column{
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items:flex-start;

 :deep(.btn + .btn) {
    margin-left: 0 !important;
  }
}
</style>

