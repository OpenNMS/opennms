<template>
  <FeatherDrawer
    id="left-drawer"
    data-test="left-drawer"
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

      <!-- Render each column -->
      <div
        v-for="(col, index) in selectedColumns"
        :key="index"
        class="column-row"
      >
        <FeatherButton
          icon="Apps"
          text
        >
          <FeatherIcon
            class="close-icon"
            :icon="Apps"
          />
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
        <FeatherButton
          icon="Cancel"
          text
          @click="removeColumn(index)"
        >
          <FeatherIcon
            class="close-icon"
            :icon="Cancel"
          />
        </FeatherButton>
      </div>

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
import { computed, ref, watch } from 'vue'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import Cancel from '@featherds/icon/navigation/Cancel'
import Apps from '@featherds/icon/navigation/Apps'
import { NodeColumnSelectionItem } from '@/types'
import { saveNodePreferences } from '@/services/localStorageService'

const nodeStructureStore = useNodeStructureStore()
const columns = computed<NodeColumnSelectionItem[]>(() => nodeStructureStore.columns)
const selectedColumns = ref<ISelectItemType[]>([])

const initializeSelectedColumns = () => {
  selectedColumns.value = columns.value
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
  const nodePrefs = await nodeStructureStore.getNodePreferences()
  saveNodePreferences(nodePrefs)
  nodeStructureStore.columnsDrawerState.visible = false
}

watch(
  [() => nodeStructureStore.columns, selectedColumns],
  ([newColumns], [oldColumns, oldSelected]) => {
    if (newColumns !== oldColumns) {
      initializeSelectedColumns()
    }
    const selectedIds = selectedColumns.value.map(c => c.value).filter(id => id !== '')
    nodeStructureStore.columns.forEach((col) => {
      col.selected = selectedIds.includes(col.id)
      col.order = selectedIds.indexOf(col.id)
    })
  },
  { immediate: true, deep: true }
)
</script>
<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.feather-drawer-custom-padding {
  padding: 20px;
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

