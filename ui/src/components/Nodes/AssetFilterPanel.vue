<template>
  <div class="asset-filter-container">
    <div class="onms-row toggle-row" data-test="featured-fields-only">
      <label for="featured-fields-only">Featured Fields Only</label>
      <OnmsToggleSwitch
        v-model="featuredOnly"
        inputId="featured-fields-only"
      />
    </div>
    <div class="onms-row add-row">
      <div class="onms-col-5">
        <FormField label="Asset Field">
          <OnmsSelect
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
          <OnmsInputText
            v-model="assetValue"
            data-test="asset-value-input"
          />
        </FormField>
      </div>
      <div class="onms-col-2 add-btn-col">
        <OnmsButton
          variant="outlined"
          data-test="asset-add-button"
          class="add-asset-filter-button"
          @click="onAddAssetFilter"
        >
          <OnmsIcon :icon="Add" />
          Add
        </OnmsButton>
      </div>
    </div>

    <OnmsTable
      v-if="gridItems.length > 0"
      :value="gridItems"
      dataKey="column"
      class="asset-filter-table"
    >
      <OnmsColumn field="label" header="Asset Field" style="width: 40%" />
      <OnmsColumn field="value" header="Value">
        <template #body="{ data }">
          <OnmsInputText
            v-model="data.value"
            class="asset-filter-input"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="" style="width: 3.5rem">
        <template #body="{ data }">
          <OnmsIconButton
            data-test="delete-asset-filter-button"
            title="Remove asset filter"
            :icon="DeleteIcon"
            @click="removeGridItem(data.column)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { OnmsButton, OnmsColumn, OnmsIcon, OnmsIconButton, OnmsInputText, OnmsSelect, OnmsTable, OnmsToggleSwitch } from '@opennms/onms-ui'
import Add from '@/components/icons/action/Add.vue'
import DeleteIcon from '@/components/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'
import { ALL_ASSET_COLUMN_OPTIONS, ASSET_COLUMN_OPTIONS, getAssetColumnLabel } from '@/components/Nodes/hooks/queryStringParser'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'

interface GridItem {
  column: string
  label: string
  value: string
}

interface AssetOption { title: string; value: string }

// Featured columns are the curated 10; the toggle swaps in every ASSET_COLUMN_FIQL_MAP column
// (with server-derived titles) when the user wants to filter on a non-featured field.
const featuredColumnValues = new Set(ASSET_COLUMN_OPTIONS.map(o => o.value))
const featuredOnly = ref(true)
const assetOptions = computed<AssetOption[]>(() =>
  (featuredOnly.value ? ASSET_COLUMN_OPTIONS : ALL_ASSET_COLUMN_OPTIONS).map(o => ({ title: o.label, value: o.value }))
)

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
  const filters = nodeStructureStore.queryFilter.assetFilters ?? []
  gridItems.value = filters.map(f => ({
    column: f.column,
    label: getAssetColumnLabel(f.column),
    value: f.value
  }))
  // If an existing filter uses a column outside the featured 10, the featured-only dropdown
  // wouldn't be able to show/re-select it — switch to the full list so it's never blank/mismatched.
  featuredOnly.value = filters.every(f => featuredColumnValues.has(f.column))
  assetValue.value = ''
  currentSelection.value = undefined
}

defineExpose({ applyToStore, resetFromStore, currentSelection, assetValue, gridItems, featuredOnly, assetOptions })

onMounted(() => {
  resetFromStore()
})
</script>

<style lang="scss" scoped>
@use '@/styles/onms-typography' as *;
@use '@/styles/onms-tokens' as variables;

.asset-filter-container {
  // Label on the left, ToggleSwitch pushed to the right edge — same idiom as the drawer's
  // down-nodes/assets/outages toggles.
  .toggle-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1rem;
  }

  .add-asset-filter-button {
    border-radius: 0;
    border: 1px solid var(--onms-primary);
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
