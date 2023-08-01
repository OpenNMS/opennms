<template>
  <div class="button-panel">
      <FeatherButton primary class="category-btn" @click="onClearAll">Clear All</FeatherButton>
  </div>
  <FeatherExpansionPanel>
    <template v-slot:title>
      <div v-if="selectedCategoryCount">
        <span>{{ `Categories (${selectedCategoryCount})` }}</span>
        <FeatherButton icon="Clear" @click="onClearCategories">
          <FeatherIcon :icon="clearIcon"> </FeatherIcon>
        </FeatherButton>
      </div>
      <div v-else>Categories</div>
    </template>
    <FeatherList class="category-list">
      <FeatherListItem
        v-for="cat of categories"
        :selected="isCategorySelected(cat)"
        :key="cat.name"
        @click="onCategoryClick(cat)">
        {{ cat.name }}
      </FeatherListItem>
    </FeatherList>
  </FeatherExpansionPanel>
  <FeatherExpansionPanel>
    <template v-slot:title>
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
    <template v-slot:title>
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
</template>

<script setup lang="ts">
import ClearIcon from '@featherds/icon/action/Cancel'
import { FeatherButton } from '@featherds/button'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherIcon } from '@featherds/icon'
import { FeatherList, FeatherListItem } from '@featherds/list'
import { useStore } from 'vuex'
import { Category, MonitoringLocation } from '@/types'

const store = useStore()
const clearIcon = ref(ClearIcon)
const categories = computed<Category[]>(() => store.state.hierarchyModule.categories)
const selectedCategories = computed<Category[]>(() => store.state.hierarchyModule.selectedCategories)
const flowTypes = computed<string[]>(() => ['Ingress', 'Egress'])
const selectedFlows = computed<string[]>(() => store.state.hierarchyModule.selectedFlows)

const locations = computed<MonitoringLocation[]>(() => store.state.hierarchyModule.monitoringLocations)
const selectedLocations = computed<MonitoringLocation[]>(() => store.state.hierarchyModule.selectedMonitoringLocations)
const selectedCategoryCount = computed<number>(() => selectedCategories.value?.length || 0)
const selectedFlowCount = computed<number>(() => selectedFlows.value?.length || 0)
const selectedLocationCount = computed<number>(() => selectedLocations.value?.length || 0)

const isCategorySelected = (cat: Category) => {
  return selectedCategories.value.some(c => c.id === cat.id)
}

const isFlowSelected = (flow: string) => {
  return selectedFlows.value.some(f => f === flow)
}

const isLocationSelected = (loc: MonitoringLocation) => {
  return selectedLocations.value.some(x => x.name === loc.name)
}

const onClearCategories = () => {
  store.dispatch('hierarchyModule/setSelectedCategories', [])
}

const onClearFlows = () => {
  store.dispatch('hierarchyModule/setSelectedFlows', [])
}

const onClearLocations = () => {
  store.dispatch('hierarchyModule/setSelectedMonitoringLocations', [])
}

const onClearAll = () => {
  onClearCategories()
  onClearFlows()
  onClearLocations()
}

/**
* Create a new array of selected items in a hierarchy filter by taking existing items and adding/removing the selected item.
* @param item the item clicked
* @param isSelected predicate for determining whether the item was previously selected
* @param existingItems array of existing values
* @param deselector function for determining the item that should be deselected
* @param dispatchName dispatch name within the 'hierarchymodule' for setting the new selected item
*/
const onSelectionClick = <T,>(item: T, isSelected: boolean, existingItems: T[],
  deselector: ((existingItem: T, clickedItem: T) => boolean), dispatchName: string) => {

  let newSelection: T[] = []

  if (isSelected) {
    // deselect clicked item
    newSelection = existingItems.filter(c => deselector(c, item))
  } else {
    // add clicked item to selection
    newSelection = [...existingItems, item]
  }

  store.dispatch(`hierarchyModule/${dispatchName}`, newSelection)
}

const onCategoryClick = (cat: Category) => {
  onSelectionClick(cat, isCategorySelected(cat), selectedCategories.value, c => c.id !== cat.id, 'setSelectedCategories')
}

const onFlowClick = (flow: string) => {
  onSelectionClick(flow, isFlowSelected(flow), selectedFlows.value, f => f !== flow, 'setSelectedFlows')
}

const onLocationClick = (loc: MonitoringLocation) => {
  onSelectionClick(loc, isLocationSelected(loc), selectedLocations.value, x => x.name !== loc.name, 'setSelectedMonitoringLocations')
}

onMounted(() => {
  store.dispatch('hierarchyModule/getCategories', true)
  store.dispatch('hierarchyModule/getMonitoringLocations', true)
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.button-panel {
  margin-bottom: 4px;
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
</style>

<style lang="scss">
.category-list {
  .feather-list-item-text {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.drawer-container .content {
  top: var(--feather-header-height);
}
</style>
