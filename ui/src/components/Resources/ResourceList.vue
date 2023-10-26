<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherInput
        label="Search/Filter Resources"
        :modelValue="searchValue"
        @update:modelValue="search"
      />
      <FeatherList>
        <FeatherListHeader>Resources</FeatherListHeader>
        <FeatherListItem
          v-for="resource in resources"
          :key="resource.label"
          @click="selectResource(resource.name)"
        >{{ resource.label }}</FeatherListItem>
      </FeatherList>
    </div>
  </div>
</template>
  
<script setup lang="ts">
import { FeatherInput } from '@featherds/input'
import {
  FeatherListHeader,
  FeatherListItem,
  FeatherList
} from '@featherds/list'
import { useGraphStore } from '@/stores/graphStore'
import { useResourceStore } from '@/stores/resourceStore'
import { Resource, UpdateModelFunction } from '@/types'

const graphStore = useGraphStore()
const resourceStore = useResourceStore()
const searchValue = ref('')

const resources = computed<Resource[]>(() => resourceStore.getFilteredResourcesList())

const search: UpdateModelFunction = (val: string) => resourceStore.setSearchValue(val || '')

const selectResource = (name: string) => {
  resourceStore.getResourcesForNode(name)
  graphStore.getPreFabGraphs(name)
}
</script>
