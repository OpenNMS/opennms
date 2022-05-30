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
          :class="{ selected: resource.name === selectedResourceName }"
          v-for="resource in resources"
          :key="resource.label"
          @click="selectResource(resource.name)"
          >{{ resource.label }}</FeatherListItem
        >
      </FeatherList>
    </div>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import {
  FeatherListHeader,
  FeatherListItem,
  FeatherList
} from '@featherds/list'
import { Resource, UpdateModelFunction } from '@/types'
import { useRoute } from 'vue-router'

const store = useStore()
const route = useRoute()
const searchValue = ref('')
const selectedResourceName = ref('')
const resources = computed<Resource[]>(() => store.getters['resourceModule/getFilteredResourcesList'])

const search: UpdateModelFunction = (val: string) => store.dispatch('resourceModule/setSearchValue', val || '')
const selectResource = (name: string) => {
  store.dispatch('resourceModule/getResourcesForNode', name)
  store.dispatch('graphModule/getPreFabGraphs', name)
  selectedResourceName.value = name
}

onMounted(() => {
  const defaultSelectedResource = route.params.name as string
  if (defaultSelectedResource) {
    selectResource(defaultSelectedResource)
    selectedResourceName.value = defaultSelectedResource
  }
})
</script>

