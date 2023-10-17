<template>
  <div class="extended-search-container">
    <FeatherSelect
      label="Search Type"
      :options="searchOptions"
      :textProp="'title'"
      v-model="currentSelection"
      @update:modelValue="onSelectionUpdated"
    />

    <FeatherInput
      v-model="searchTerm"
      @update:modelValue="onCurrentSearchUpdated"
      label="Search Term"
    />
  </div>
</template>

<script setup lang="ts">
import { isIP } from 'is-ip'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { useNodeQuery } from './hooks/useNodeQuery'
import { useNodeStore } from '@/stores/nodeStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { NodeQueryFilter, UpdateModelFunction } from '@/types'

const searchOptions: ISelectItemType[] = [
  { title: 'IP Address', value: 'ipAddress' },
  { title: 'SNMP Alias', value: 'snmpIfAlias' },
  { title: 'SNMP Description', value: 'snmpIfDesc' },
  { title: 'SNMP Index', value: 'snmpIfIndex' },
  { title: 'SNMP Name', value: 'snmpIfName' },
  { title: 'SNMP Type', value: 'snmpIfType' }
]
const snmpKeys = ['snmpIfAlias', 'snmpIfDesc', 'snmpIfIndex', 'snmpIfName', 'snmpIfType']

const { buildUpdatedNodeStructureQueryParameters } = useNodeQuery()
const nodeStore = useNodeStore()
const nodeStructureStore = useNodeStructureStore()
const searchTerm = ref('')
const currentSelection = ref<ISelectItemType | undefined>(undefined)

const onCurrentSearchUpdated = (item: string) => {
  if (currentSelection.value?.value === 'ipAddress') {
    if (!isIP(item)) {
      // prevent search with invalid IP addresses, they'll just cause 500 errors
      return
    }
    nodeStructureStore.setFilterWithIpAddress(item)
  } else if ((currentSelection.value?.value as string || '').startsWith('snmp')) {
    nodeStructureStore.setFilterWithSnmpParams((currentSelection.value?.value as string), item)
  }

  updateQuery()
}

const onSelectionUpdated: UpdateModelFunction = (selected: any) => {
  if (selected.value === 'ipAddress') {
    nodeStructureStore.setFilterWithIpAddress(searchTerm.value)
  } else if ((selected.value as string || '').startsWith('snmp')) {
    nodeStructureStore.setFilterWithSnmpParams(selected.value, searchTerm.value)
  }

  updateQuery()
}

// helper used in getOptionFromFilter
const getOptionFromObj = (obj: any, key: string) => {
  if (obj[key]) {
    return {
      value: obj[key],
      searchOption: searchOptions.find(x => x.value === key)
    }
  }

  return undefined
}

/**
 * Get an option object and search term based on the given filter.
 * This prioritizes which search item is used.
*/
const getOptionFromFilter = (queryFilter: NodeQueryFilter) => {
  if (queryFilter.ipAddress) {
    return getOptionFromObj(queryFilter, 'ipAddress')
  } else if (queryFilter.snmpParams) {
    const p = queryFilter.snmpParams

    for (let key of snmpKeys) {
      const o = getOptionFromObj(p, key)

      if (o) {
        return o
      }
    }
  }

  return undefined
}

const updateQuery = () => {
  // make sure anything setting nodeStore.nodeQueryParameters has been processed
  nextTick()
  const updatedParams = buildUpdatedNodeStructureQueryParameters(nodeStore.nodeQueryParameters, nodeStructureStore.queryFilter)

  nodeStore.getNodes(updatedParams, true)
}

const updateFromStore = () => {
  const option = getOptionFromFilter(nodeStructureStore.queryFilter)

  if (option) {
    searchTerm.value = option.value
    currentSelection.value = option.searchOption
  } else {
    searchTerm.value = ''
  }
}

watch([() => nodeStructureStore.queryFilter], () => {
  updateFromStore()
})

onMounted(() => {
  updateFromStore()
})

</script>

<style lang="scss" scoped>

</style>
