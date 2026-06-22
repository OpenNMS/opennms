<template>
  <div class="extended-search-container">
    <div class="onms-row add-row">
      <div class="onms-col-5">
        <FeatherSelect
          label="Search Type"
          :options="searchOptions"
          :textProp="'title'"
          v-model="currentSelection"
        />
      </div>
      <div class="onms-col-5">
        <FeatherInput
          v-model="searchTerm"
          label="Search Term"
        />
      </div>
      <div class="onms-col-2 add-btn-col">
        <FeatherButton
          secondary
          icon="Add"
          data-test="add-search-term-button"
          class="add-search-term-button"
          @click="onAddSearchTerm"
        >
          <FeatherIcon :icon="Add" />
          Add
        </FeatherButton>
      </div>
    </div>

    <PDataTable
      v-if="gridItems.length > 0"
      :value="gridItems"
      dataKey="key"
      class="extended-search-table"
    >
      <PColumn field="label" header="Search Type" style="width: 40%" />
      <PColumn field="value" header="Search Term">
        <template #body="{ data }">
          <PInputText
            v-model="data.value"
            class="extended-search-input"
          />
        </template>
      </PColumn>
      <PColumn header="" style="width: 3.5rem">
        <template #body="{ data }">
          <FeatherButton
            icon="Delete"
            data-test="delete-search-term-button"
            @click="removeGridItem(data.key)"
          >
            <FeatherIcon :icon="DeleteIcon" />
          </FeatherButton>
        </template>
      </PColumn>
    </PDataTable>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import DataTableComponent from 'primevue/datatable'
import ColumnComponent from 'primevue/column'
import InputTextComponent from 'primevue/inputtext'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import DeleteIcon from '@featherds/icon/action/Delete'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
import { NodeQueryExtendedSearchParams } from '@/types'

const PDataTable = DataTableComponent
const PColumn = ColumnComponent
const PInputText = InputTextComponent

const {
  getExtendedSearchValues,
  getDefaultNodeQueryForeignSourceParams,
  getDefaultNodeQuerySnmpParams,
  getDefaultNodeQuerySysParams
} = useNodeQuery()

interface GridItem {
  key: string
  label: string
  value: string
}

const searchOptions: ISelectItemType[] = [
  { title: 'Foreign Source', value: 'foreignSource' },
  { title: 'Foreign ID', value: 'foreignId' },
  { title: 'Foreign Source:Foreign ID', value: 'foreignSourceId' },
  { title: 'Sys Contact', value: 'sysContact' },
  { title: 'Sys Description', value: 'sysDescription' },
  { title: 'Sys Location', value: 'sysLocation' },
  { title: 'Sys Name', value: 'sysName' },
  { title: 'Sys Object ID', value: 'sysObjectId' },
  { title: 'SNMP Alias', value: 'snmpIfAlias' },
  { title: 'SNMP Description', value: 'snmpIfDescription' },
  { title: 'SNMP Index', value: 'snmpIfIndex' },
  { title: 'SNMP Name', value: 'snmpIfName' },
  { title: 'SNMP Type', value: 'snmpIfType' }
]

const foreignSourceKeys = ['foreignSource', 'foreignId', 'foreignSourceId']
const snmpKeys = ['snmpIfAlias', 'snmpIfDescription', 'snmpIfIndex', 'snmpIfName', 'snmpIfType']
const sysKeys = ['sysContact', 'sysDescription', 'sysLocation', 'sysName', 'sysObjectId']

const nodeStructureStore = useNodeStructureStore()
const searchTerm = ref('')
const currentSelection = ref<ISelectItemType | undefined>(undefined)
const gridItems = ref<GridItem[]>([])

const onAddSearchTerm = () => {
  if (!currentSelection.value || !searchTerm.value.trim()) {
    return
  }
  const key = currentSelection.value.value as string
  const label = currentSelection.value.title as string
  const existing = gridItems.value.findIndex(i => i.key === key)
  if (existing >= 0) {
    gridItems.value[existing].value = searchTerm.value.trim()
  } else {
    gridItems.value.push({ key, label, value: searchTerm.value.trim() })
  }
  searchTerm.value = ''
  currentSelection.value = undefined
}

const removeGridItem = (key: string) => {
  gridItems.value = gridItems.value.filter(i => i.key !== key)
}

const applyToStore = () => {
  const ext: NodeQueryExtendedSearchParams = {}
  for (const item of gridItems.value) {
    if (!item.value.trim()) {
      continue
    }
    if (foreignSourceKeys.includes(item.key)) {
      if (!ext.foreignSourceParams) {
        ext.foreignSourceParams = getDefaultNodeQueryForeignSourceParams()
      }
      Object.assign(ext.foreignSourceParams, { [item.key]: item.value })
    } else if (snmpKeys.includes(item.key)) {
      if (!ext.snmpParams) {
        ext.snmpParams = getDefaultNodeQuerySnmpParams()
      }
      Object.assign(ext.snmpParams, { [item.key]: item.value })
    } else if (sysKeys.includes(item.key)) {
      if (!ext.sysParams) {
        ext.sysParams = getDefaultNodeQuerySysParams()
      }
      Object.assign(ext.sysParams, { [item.key]: item.value })
    }
  }
  nodeStructureStore.setExtendedSearchParams(ext)
}

const resetFromStore = () => {
  const values = getExtendedSearchValues(nodeStructureStore.queryFilter.extendedSearch)
  gridItems.value = values.map(v => ({ key: v.key, label: v.name, value: v.value }))
  searchTerm.value = ''
  currentSelection.value = undefined
}

defineExpose({ applyToStore, resetFromStore })

onMounted(() => {
  resetFromStore()
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/themes/variables';

.extended-search-container {
  .add-search-term-button {
    border-radius: 0;
    border: 1px solid var(--feather-primary);
    width: auto;
    padding: 0.5em 1em;
  }

  .add-btn-col {
    display: flex;
    padding-bottom: 0.25rem;
  }

  .extended-search-table {
    margin-top: 1rem;

    .extended-search-input {
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
