<template>
  <div class="asset-filter-container">
    <div class="onms-row add-row">
      <div class="onms-col-5">
        <FormField label="Asset Field">
          <Select
            v-model="currentSelection"
            :options="assetOptions"
            optionLabel="title"
            placeholder="Select a field"
            data-test="asset-field-select"
          />
        </FormField>
      </div>
      <div class="onms-col-5">
        <FormField label="Value">
          <InputText
            v-model="assetValue"
            data-test="asset-value-input"
          />
        </FormField>
      </div>
      <div class="onms-col-2 add-btn-col">
        <Button
          outlined
          data-test="asset-add-button"
          class="add-asset-filter-button"
          @click="onAddAssetFilter"
        >
          <FeatherIcon :icon="Add" />
          Add
        </Button>
      </div>
    </div>

    <PDataTable
      v-if="gridItems.length > 0"
      :value="gridItems"
      dataKey="column"
      class="asset-filter-table"
    >
      <PColumn field="label" header="Asset Field" style="width: 40%" />
      <PColumn field="value" header="Value">
        <template #body="{ data }">
          <PInputText
            v-model="data.value"
            class="asset-filter-input"
          />
        </template>
      </PColumn>
      <PColumn header="" style="width: 3.5rem">
        <template #body="{ data }">
          <Button
            text
            data-test="delete-asset-filter-button"
            @click="removeGridItem(data.column)"
          >
            <FeatherIcon :icon="DeleteIcon" />
          </Button>
        </template>
      </PColumn>
    </PDataTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import DataTableComponent from 'primevue/datatable'
import ColumnComponent from 'primevue/column'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Button from 'primevue/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import DeleteIcon from '@featherds/icon/action/Delete'
import FormField from '@/components/Common/FormField.vue'
import { ASSET_COLUMN_OPTIONS } from '@/components/Nodes/hooks/queryStringParser'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'

const PDataTable = DataTableComponent
const PColumn = ColumnComponent
const PInputText = InputText

interface GridItem {
  column: string
  label: string
  value: string
}

interface AssetOption { title: string; value: string }
const assetOptions: AssetOption[] = ASSET_COLUMN_OPTIONS.map(o => ({ title: o.label, value: o.value }))

const nodeStructureStore = useNodeStructureStore()
const assetValue = ref('')
const currentSelection = ref<AssetOption | undefined>(undefined)
const gridItems = ref<GridItem[]>([])

const onAddAssetFilter = () => {
  if (!currentSelection.value || !assetValue.value.trim()) {
    return
  }
  const column = currentSelection.value.value as string
  const label = currentSelection.value.title as string
  const existing = gridItems.value.findIndex(i => i.column === column)
  if (existing >= 0) {
    gridItems.value[existing].value = assetValue.value.trim()
  } else {
    gridItems.value.push({ column, label, value: assetValue.value.trim() })
  }
  assetValue.value = ''
  currentSelection.value = undefined
}

const removeGridItem = (column: string) => {
  gridItems.value = gridItems.value.filter(i => i.column !== column)
}

const applyToStore = () => {
  const assetFilters = gridItems.value
    .filter(i => i.value.trim())
    .map(i => ({ column: i.column, value: i.value.trim() }))
  nodeStructureStore.setFilterWithAssetFilters(assetFilters)
}

const resetFromStore = () => {
  gridItems.value = (nodeStructureStore.queryFilter.assetFilters ?? []).map(f => ({
    column: f.column,
    label: assetOptions.find(o => o.value === f.column)?.title as string ?? f.column,
    value: f.value
  }))
  assetValue.value = ''
  currentSelection.value = undefined
}

defineExpose({ applyToStore, resetFromStore, currentSelection, assetValue, gridItems })

onMounted(() => {
  resetFromStore()
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';

.asset-filter-container {
  .add-asset-filter-button {
    border-radius: 0;
    border: 1px solid var(--feather-primary);
    width: auto;
    padding: 0.5em 1em;
  }

  .add-btn-col {
    display: flex;
    align-items: flex-end;
    padding-bottom: 0.5rem;
  }

  .asset-filter-table {
    margin-top: 1rem;

    .asset-filter-input {
      width: 100%;
    }

    :deep(.p-datatable-tbody > tr > td) {
      padding: 0.25rem 0.5rem;
      vertical-align: middle;
    }

    :deep(.p-datatable-thead > tr > th) {
      padding: 0.4rem 0.5rem;
    }
  }
}
</style>
