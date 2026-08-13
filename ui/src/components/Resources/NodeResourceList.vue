<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <div class="action-buttons" v-if="resources.length">
        <OnmsButton @click="selectAll">Select All</OnmsButton>
        <OnmsButton @click="clearAll">Clear All</OnmsButton>
        <OnmsButton @click="graphAll">Graph All</OnmsButton>
        <OnmsButton @click="graphSelected" :disabled="!resourceIsSelected">Graph Selected</OnmsButton>
      </div>
      <ul class="onms-list">
        <template v-for="(resources, header) in groupedResourcesObject" :key="header">
          <li class="list-header">{{ header }}</li>
          <li
            class="list-item"
            v-for="resource in resources"
            :key="resource.label"
          >
            <OnmsCheckbox
              :inputId="`resource-${resource.id}`"
              @update:modelValue="selectCheckbox(resource.id)"
              :modelValue="selectedResourceObject[resource.id]"
            />
            <label :for="`resource-${resource.id}`">{{ resource.label }}</label>
          </li>
          <li class="list-separator" aria-hidden="true"></li>
        </template>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { groupBy } from 'lodash'
import { OnmsButton, OnmsCheckbox } from '@opennms/onms-ui'
import { useGraphStore } from '@/stores/graphStore'
import { useResourceStore } from '@/stores/resourceStore'
import { Resource } from '@/types'

interface GroupedResourcesObject {
  [x: string]: Resource[]
}

const graphStore = useGraphStore()
const resourceStore = useResourceStore()
const router = useRouter()

const selectedResourceObject = ref<any>({})

const resources = computed<Resource[]>(() => resourceStore.nodeResource.children?.resource || [])
const groupedResourcesObject = computed<GroupedResourcesObject>(() => groupBy(resources.value, 'typeLabel'))
const resourceIsSelected = computed<boolean>(() => Object.values(selectedResourceObject.value).includes(true))

const selectCheckbox = (resourceId: string) => selectedResourceObject.value[resourceId] = !selectedResourceObject.value[resourceId]

const selectAll = () => {
  for (const resource of resources.value) {
    selectedResourceObject.value[resource.id] = true
  }
}

const clearAll = () => selectedResourceObject.value = {}

const graphSelected = async () => {
  const selectedIds = []

  for (const key in selectedResourceObject.value) {
    if (selectedResourceObject.value[key]) {
      selectedIds.push(key)
    }
  }

  await graphStore.getGraphDefinitionsByResourceIds(selectedIds, resources.value)
  router.push('/resource-graphs/graphs')
}

const graphAll = async () => {
  const resourceIds = resources.value.map(resource => resource.id)
  // Await the definitions before navigating: Graphs.vue snapshots
  // graphStore.definitionsList on mount, so if this fetch is still in flight
  // the graph list is empty and nothing renders on the first attempt.
  await graphStore.getGraphDefinitionsByResourceIds(resourceIds, resources.value)
  router.push('/resource-graphs/graphs')
}
</script>

<style lang="scss" scoped>
.action-buttons {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
.onms-list {
  list-style: none;
  padding: 0;
  margin: 0;

  .list-header {
    font-weight: 700;
    padding: 0.5rem 1rem;
    color: var(--p-text-muted-color);
  }

  .list-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;

    label {
      cursor: pointer;
    }
  }

  .list-separator {
    border-bottom: 1px solid var(--p-content-border-color);
    margin: 0.25rem 0;
  }
}
</style>
