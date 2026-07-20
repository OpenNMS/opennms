<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <FormField label="Search/Filter Resources" class="search-field">
        <PInputText
          :modelValue="searchValue"
          @update:modelValue="(val) => search(val as string)"
        />
      </FormField>
      <ul class="onms-list">
        <li class="list-header">Resources</li>
        <li
          class="list-item"
          v-for="resource in resources"
          :key="resource.label"
          @click="selectResource(resource.name)"
        >{{ resource.label }}</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import InputText from 'primevue/inputtext'
import FormField from '@/components/Common/FormField.vue'
import { useGraphStore } from '@/stores/graphStore'
import { useResourceStore } from '@/stores/resourceStore'
import { Resource } from '@/types'

const PInputText = InputText

const graphStore = useGraphStore()
const resourceStore = useResourceStore()
const searchValue = ref('')

const resources = computed<Resource[]>(() => resourceStore.getFilteredResourcesList())

const search = (val: string) => resourceStore.setSearchValue(val || '')

const selectResource = (name: string) => {
  resourceStore.getResourcesForNode(name)
  graphStore.getPreFabGraphs(name)
}
</script>

<style lang="scss" scoped>
.onms-list {
  list-style: none;
  padding: 0;
  margin: 0.5rem 0 0;

  .list-header {
    font-weight: 700;
    padding: 0.5rem 1rem;
    color: var(--p-text-muted-color);
  }

  .list-item {
    padding: 0.5rem 1rem;
    cursor: pointer;

    &:hover {
      background: var(--p-highlight-background);
    }
  }
}
</style>
