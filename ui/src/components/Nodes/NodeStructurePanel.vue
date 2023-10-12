<template>
  <h1 class="title">Filtering</h1>
  <div class="button-panel">
      <FeatherButton primary class="category-btn" @click="onClearAll" :disabled="!isAnyFilterSelected">Clear All</FeatherButton>
  </div>
  <FeatherExpansionPanel>
    <template #title>
      <div v-if="selectedCategoryCount">
        <span>{{ `Categories (${selectedCategoryCount})` }}</span>
        <FeatherButton icon="Clear" @click="onClearCategories">
          <FeatherIcon :icon="clearIcon"> </FeatherIcon>
        </FeatherButton>
      </div>
      <div v-else>Categories</div>
    </template>
    <template #default>
      <div class="category-button-group">
        <FeatherButton
          class="switcher-button"
          :primary="categoryMode === SetOperator.Union"
          :secondary="categoryMode !== SetOperator.Union"
          @click="categoryModeUpdated(SetOperator.Union)"
        >Any</FeatherButton>
        <FeatherButton
          class="switcher-button"
          :primary="categoryMode === SetOperator.Intersection"
          :secondary="categoryMode !== SetOperator.Intersection"
          @click="categoryModeUpdated(SetOperator.Intersection)"
        >All</FeatherButton>
      </div>
      <FeatherList class="category-list">
        <FeatherListItem
          v-for="cat of nodeStructureStore.categories"
          :selected="isCategorySelected(cat)"
          :key="cat.name"
          @click="onCategoryClick(cat)">
          {{ cat.name }}
        </FeatherListItem>
      </FeatherList>
    </template>
  </FeatherExpansionPanel>
  <FeatherExpansionPanel>
    <template #title>
      <div v-if="selectedFlowCount">
        <span>{{ `Flows (${selectedFlowCount})` }}</span>
        <FeatherButton icon="Clear" @click="onClearFlows">
          <FeatherIcon :icon="clearIcon"> </FeatherIcon>
        </FeatherButton>
      </div>
      <div v-else>Flows</div>
    </template>
    <FeatherList class="category-list">
      <FeatherListItem
        v-for="flow of flowTypes"
        :selected="isFlowSelected(flow)"
        :key="flow"
        @click="onFlowClick(flow)">
        {{ flow }}
      </FeatherListItem>
    </FeatherList>
  </FeatherExpansionPanel>
  <FeatherExpansionPanel>
    <template #title>
      <div v-if="selectedLocationCount">
        <span>{{ `Locations (${selectedLocationCount})` }}</span>
        <FeatherButton icon="Clear" @click="onClearLocations">
          <FeatherIcon :icon="clearIcon"> </FeatherIcon>
        </FeatherButton>
      </div>
      <div v-else>Locations</div>
    </template>
    <FeatherList class="category-list">
      <FeatherListItem
        v-for="loc of locations"
        :selected="isLocationSelected(loc)"
        :key="loc.name"
        @click="onLocationClick(loc)">
        {{ loc.name }}
      </FeatherListItem>
    </FeatherList>
  </FeatherExpansionPanel>
  <div class="search-autocomplete-panel">
    <h1 class="title">Metadata Search</h1>
    <FeatherAutocomplete
      v-model="metaSearchString"
      type="multi"
      :results="metadataSearchResults"
      label="Search"
      class="map-search"
      @search="resetMetaSearch"
      :loading="loading"
      :hideLabel="true"
      text-prop="label"
      @update:modelValue="selectMetaItem"
      :labels="metaLabels"
    ></FeatherAutocomplete>
  </div>
</template>

<script setup lang="ts">
import ClearIcon from '@featherds/icon/action/Cancel'
import { FeatherAutocomplete } from '@featherds/autocomplete'
import { FeatherButton } from '@featherds/button'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherIcon } from '@featherds/icon'
import { FeatherList, FeatherListItem } from '@featherds/list'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { Category, MonitoringLocation, SetOperator } from '@/types'

const nodeStructureStore = useNodeStructureStore()
const clearIcon = ref(ClearIcon)
const flowTypes = computed<string[]>(() => ['Ingress', 'Egress'])
const categoryMode = computed(() => nodeStructureStore.queryFilter.categoryMode)

const locations = computed<MonitoringLocation[]>(() => nodeStructureStore.monitoringLocations)
const selectedCategoryCount = computed<number>(() => nodeStructureStore.queryFilter.selectedCategories?.length || 0)
const selectedFlowCount = computed<number>(() => nodeStructureStore.queryFilter.selectedFlows?.length || 0)
const selectedLocationCount = computed<number>(() => nodeStructureStore.queryFilter.selectedMonitoringLocations?.length || 0)
const isAnyFilterSelected = computed<boolean>(() => nodeStructureStore.queryFilter.searchTerm?.length > 0 || selectedCategoryCount.value > 0 || selectedFlowCount.value > 0 || selectedLocationCount.value > 0)

const metaSearchString = ref()
const loading = ref(false)
const defaultMetaLabels = { noResults: 'Searching...' }
const metaLabels = ref(defaultMetaLabels)

const metadataSearchResults = computed(() => {
  return ['cat', 'dog', 'parakeet'].map(x => ({ _text: x }))
})

const selectMetaItem = (obj: any) => {
  console.log('selectMetaItem')
}

const resetMetaSearch = () => {
  console.log('resetMetaSearch')
}

const categoryModeUpdated = (val: any) => {
  nodeStructureStore.setCategoryMode(val)
}

const isCategorySelected = (cat: Category) => {
  return nodeStructureStore.queryFilter.selectedCategories.some(c => c.id === cat.id)
}

const isFlowSelected = (flow: string) => {
  return nodeStructureStore.queryFilter.selectedFlows.some(f => f === flow)
}

const isLocationSelected = (loc: MonitoringLocation) => {
  return nodeStructureStore.queryFilter.selectedMonitoringLocations.some(x => x.name === loc.name)
}

const onClearCategories = () => {
  nodeStructureStore.setSelectedCategories([])
}

const onClearFlows = () => {
  nodeStructureStore.setSelectedFlows([])
}

const onClearLocations = () => {
  nodeStructureStore.setSelectedMonitoringLocations([])
}

const onClearAll = () => {
  nodeStructureStore.clearAllFilters(SetOperator.Union)
}

/**
* Create a new array of selected items in a hierarchy filter by taking existing items and adding/removing the selected item.
* @param item the item clicked
* @param isSelected predicate for determining whether the item was previously selected
* @param existingItems array of existing values
* @param deselector function for determining the item that should be deselected
*/
const getNewSelection = <T,>(item: T, isSelected: boolean, existingItems: T[], deselector: ((existingItem: T, clickedItem: T) => boolean)) => {
  if (isSelected) {
    // deselect clicked item
    return existingItems.filter(c => deselector(c, item))
  } else {
    // add clicked item to selection
    return [...existingItems, item]
  }
}

const onCategoryClick = (cat: Category) => {
  const newSelection = getNewSelection(cat, isCategorySelected(cat), nodeStructureStore.queryFilter.selectedCategories, c => c.id !== cat.id)
  nodeStructureStore.setSelectedCategories(newSelection)
}

const onFlowClick = (flow: string) => {
  const newSelection = getNewSelection(flow, isFlowSelected(flow), nodeStructureStore.queryFilter.selectedFlows, f => f !== flow)
  nodeStructureStore.setSelectedFlows(newSelection)
}

const onLocationClick = (loc: MonitoringLocation) => {
  const newSelection = getNewSelection(loc, isLocationSelected(loc), nodeStructureStore.queryFilter.selectedMonitoringLocations, x => x.name !== loc.name)
  nodeStructureStore.setSelectedMonitoringLocations(newSelection)
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.button-panel {
  margin-bottom: 6px;
}

div.category-button-group {
  margin-bottom: 1em;

  > button.btn.switcher-button {
    &.btn-primary, &.btn-secondary {
      margin-left: 0;
    }
  }
}

.category-list {
  @include elevation(2);
  background: var($surface);
  overflow-y: auto;

  .title {
    @include headline3
  }
}
.category-btn {
  margin-bottom: 4px;
  margin-right: 4px;
}

.search-autocomplete-panel {
  margin-top: 1.5em;
}

.title {
  @include overline();
  color: #4b5ad6;
  margin-bottom: 8px;
}
</style>

<style lang="scss">
.category-list {
  .feather-list-item-text {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
