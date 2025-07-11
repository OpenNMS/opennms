<template>
  <FeatherDrawer
    id="left-drawer"
    data-test="left-drawer"
    @shown="() => nodeStructureStore.drawerState.visible"
    v-model="nodeStructureStore.drawerState.visible"
    :labels="{ close: 'close', title: 'Advanced Node Filters' }"
    width="60em"
  >
    <div class="feather-drawer-custom-padding">
      <section>
        <h3>Advanced Filters</h3>
      </section>
      <div class="spacer-large"></div>
      <div class="spacer-large"></div>
      <div>Choose one or more attributes to find a service.</div>
      <div class="spacer-large"></div>
      <FeatherAutocomplete
        class="my-autocomplete"
        label="Categories"
        type="multi"
        v-model="selectedFilters.categories"
        :loading="loading"
        :results="categoryResults"
        @search="handleCategorySearch"
        :allow-new="false"
        text-prop="_text"
        @update:modelValue="(items: any) => updateFilter('categories', items)"
      ></FeatherAutocomplete>
      <FeatherAutocomplete
        class="filter-autocomplete"
        label="Flows"
        type="multi"
        v-model="selectedFilters.flows"
        :loading="flowsLoading"
        :results="flowResults"
        @search="handleFlowSearch"
        @update:modelValue="(items: any) => updateFilter('flows', items)"
        text-prop="_text"
      ></FeatherAutocomplete>
      <FeatherAutocomplete
        class="last-filter-autocomplete"
        label="Locations"
        type="multi"
        v-model="selectedFilters.locations"
        :loading="locationsLoading"
        :results="locationResults"
        @search="handleLocationSearch"
        @update:modelValue="(items: any) => updateFilter('locations', items)"
      >
      </FeatherAutocomplete>
      <div class="spacer-medium"></div>
      <div>
        <h4 class="title">Extended Search</h4>
        <div class="spacer-medium"></div>
        <ExtendedSearchPanel />
      </div>
      <div class="footer">
        <FeatherButton
          primary
          @click="applySelectedFilters()"
        >
          Apply Filters
        </FeatherButton>
        <FeatherButton
          secondary
          @click="nodeStructureStore.closeInstancesDrawerModal()"
        >
          Close
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherButton } from '@featherds/button'
import { ref } from 'vue'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { MonitoringLocation } from '@/types'
import ExtendedSearchPanel from './ExtendedSearchPanel.vue'

const nodeStructureStore = useNodeStructureStore()
const selectedFilters = reactive({
  categories: [] as IAutocompleteItemType[],
  flows: [] as IAutocompleteItemType[],
  locations: [] as IAutocompleteItemType[]
})


const categoryResults = ref([] as IAutocompleteItemType[])
const loading = ref(false)
const searchTimeout = ref<number>(-1)
const flowsLoading = ref(false)
const flowResults = ref<IAutocompleteItemType[]>([])
const locationsLoading = ref(false)
const locationResults = ref<IAutocompleteItemType[]>([])

watch(() => nodeStructureStore.drawerState.visible, (visible) => {
  if (visible) {
    selectedFilters.categories = [...nodeStructureStore.selectedCategories]
    selectedFilters.flows = [...nodeStructureStore.selectedFlows]
    selectedFilters.locations = [...nodeStructureStore.selectedLocations]
  }
})

const handleCategorySearch = (query: string) => {
  loading.value = true
  clearTimeout(searchTimeout.value)

  searchTimeout.value = window.setTimeout(() => {
    const categoriesArray = Array.isArray(nodeStructureStore.categories)
      ? nodeStructureStore.categories
      : []

    const filteredCategories = categoriesArray
      .filter((category) =>
        category.name && category.name.toLowerCase().includes(query.toLowerCase())
      )
      .map((category) => ({
        _text: category.name,
        _value: category.id,
      } as IAutocompleteItemType))
    categoryResults.value = filteredCategories
    loading.value = false
  }, 500)
}

const handleFlowSearch = (query: string) => {
  flowsLoading.value = true
  clearTimeout(searchTimeout.value)

  searchTimeout.value = window.setTimeout(() => {
    flowResults.value = [
      { _text: 'Egress', _value: 'lastEgressFlow' },
      { _text: 'Ingress', _value: 'lastIngressFlow' }
    ].filter(flow => flow._text.toLowerCase().includes(query.toLowerCase()))
    flowsLoading.value = false
  }, 500)
}

const handleLocationSearch = (query: string) => {
  locationsLoading.value = true
  clearTimeout(searchTimeout.value)

  searchTimeout.value = window.setTimeout(() => {
    locationResults.value = nodeStructureStore.monitoringLocations
      .filter(location => location.name.toLowerCase().includes(query.toLowerCase()))
      .map(location => ({
        _text: location.name,
        _value: location.name,
        longitude: location.longitude,
        latitude: location.latitude,
        geolocation: location.geolocation,
        tags: location.tags,
        priority: location.priority,
        'location-name': location['location-name'],
        'monitoring-area': location['monitoring-area']
      }))
    locationsLoading.value = false
  }, 500)
}

const updateFilter = (key: keyof typeof selectedFilters, items: IAutocompleteItemType[]) => {
  selectedFilters[key] = items
}

const applySelectedFilters = () => {
  nodeStructureStore.setSelectedCategoriess(selectedFilters.categories)
  nodeStructureStore.setSelectedFlowss(selectedFilters.flows)
  const monitoringLocations = selectedFilters.locations.map(item => ({
    name: item._text,
    longitude: item.longitude,
    latitude: item.latitude,
    geolocation: item.geolocation,
    tags: item.tags,
    priority: item.priority,
    'location-name': item['location-name'],
    'monitoring-area': item['monitoring-area']
  } as MonitoringLocation))

  nodeStructureStore.setSelectedLocations(monitoringLocations)
  nodeStructureStore.closeInstancesDrawerModal()
}
</script>
<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.feather-drawer-custom-padding {
  padding: 20px;
}

.spacer-large {
  margin-bottom: 2rem;
}

.spacer-medium {
  margin-bottom: 0.25rem;
}

.footer {
  display: flex;
  padding-top: 20px;
}

.inventory-auto {
  min-width: 400px;

  :deep(.feather-autocomplete-input) {
    min-width: 100px;
  }

  :deep(.feather-autocomplete-content) {
    display: block;
  }
}

.last-filter-autocomplete{
  :deep(.feather-input-sub-text) {
    display: none !important;
  }
}
</style>

