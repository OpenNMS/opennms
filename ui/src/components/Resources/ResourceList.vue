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
  
<script setup lang=ts>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import {
  FeatherListHeader,
  FeatherListItem,
  FeatherList
} from '@featherds/list'
import { Resource } from '@/types'

const store = useStore()

const searchValue = ref('')

const resources = computed<Resource[]>(() => store.getters['resourceModule/getFilteredResourcesList'])

const search = (val: string) => store.dispatch('resourceModule/setSearchValue', val || '')
const selectResource = (name: string) => { 
  store.dispatch('resourceModule/getResourcesForNode', name)
  store.dispatch('graphModule/getPreFabGraphs', name)
}
</script>
