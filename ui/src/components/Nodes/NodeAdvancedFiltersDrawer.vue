<template>
  <FeatherDrawer
    id="left-drawer"
    data-test="left-drawer"
    @hidden="nodeStructureStore.closeInstancesDrawerModal()"
    v-model="nodeStructureStore.drawerState.visible"
    :labels="{ close: 'close', title: 'Advanced Node Filters' }"
    width="60em"
  >
    <div class="feather-drawer-custom-padding">
      <section>
        <h3>Advanced Filters</h3>
      </section>
      <div class="info-section">
        <span>Choose one or more attributes to find matching nodes.</span>
        <FeatherIcon
          :icon="InfoIcon"
          class="info-icon"
          @click="isMessageDialogVisible = true"
          data-test="snmp-config-lookup-info-icon"
        />
      </div>
      <h4 class="title">Categories</h4>
      <div class="category-row">
        <FeatherAutocomplete
          class="category-autocomplete"
          label="Categories"
          type="multi"
          v-model="selectedFilters.categories"
          :loading="categoriesLoading"
          :results="categoryResults"
          @search="handleCategorySearch"
          :allow-new="false"
          text-prop="_text"
          hint="You may select up to two category groups to filter nodes by"
          @update:modelValue="(items: any) => updateFilter('categories', items)"
        ></FeatherAutocomplete>
        <FeatherButton
          v-if="!showSecondCategories"
          icon="Add category group"
          class="category-add-btn"
          @click="showSecondCategories = true"
        >
          <FeatherIcon :icon="AddIcon" />
        </FeatherButton>
      </div>
      <div v-if="showSecondCategories" class="category-row">
        <FeatherAutocomplete
          class="category-autocomplete"
          label="Additional Categories"
          type="multi"
          v-model="selectedFilters.categories2"
          :loading="categories2Loading"
          :results="category2Results"
          @search="handleCategory2Search"
          :allow-new="false"
          text-prop="_text"
          @update:modelValue="(items: any) => updateFilter('categories2', items)"
        ></FeatherAutocomplete>
        <FeatherButton
          icon="Remove category group"
          class="category-add-btn"
          @click="removeSecondCategories"
        >
          <FeatherIcon :icon="DeleteIcon" />
        </FeatherButton>
      </div>
      <div class="spacer-large"></div>
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
        class="filter-autocomplete"
        label="Monitoring Locations"
        type="multi"
        v-model="selectedFilters.locations"
        :loading="locationsLoading"
        :results="locationResults"
        @search="handleLocationSearch"
        @update:modelValue="(items: any) => updateFilter('locations', items)"
      >
      </FeatherAutocomplete>
      <FeatherAutocomplete
        class="last-filter-autocomplete"
        label="Monitored Services"
        type="multi"
        v-model="selectedFilters.services"
        :results="serviceResults"
        @search="handleServiceSearch"
        @update:modelValue="(items: any) => updateFilter('services', items)"
        text-prop="_text"
      ></FeatherAutocomplete>
      <div class="spacer-large"></div>
      <hr />
      <div class="spacer-medium"></div>
      <div>
        <h4 class="title">Extended Search</h4>
        <div class="spacer-medium"></div>
        <ExtendedSearchPanel />
      </div>
      <div class="footer">
        <FeatherButton
          primary
          @click="applySelectedFilters"
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
  <MessageDialog
    :visible="isMessageDialogVisible"
    maxHeight="22em"
    maxWidth="50em"
    title="Advanced Filters"
    @close="isMessageDialogVisible = false"
  >
    <template #content>
      <div>
        <p>Use the advanced filters to find nodes that match specific criteria.</p>
        <p>You can filter by categories, flows, locations, and monitored services.</p>
        <p>You may select up to two category groups to filter nodes by. Each category group can contain multiple categories "unioned" together.</p>
        <p>Category groups are then "intersected" with each other to refine the search results.</p>
      </div>
    </template>
  </MessageDialog>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import AddIcon from '@featherds/icon/action/Add'
import DeleteIcon from '@featherds/icon/action/Delete'
import InfoIcon from '@featherds/icon/action/Info'
import MessageDialog from '../Common/MessageDialog.vue'
import ExtendedSearchPanel from './ExtendedSearchPanel.vue'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'

const searchTimeout = ref<number>(-1)
const category2SearchTimeout = ref<number>(-1)
const categoriesLoading = ref(false)
const categoryResults = ref([] as IAutocompleteItemType[])
const categories2Loading = ref(false)
const category2Results = ref<IAutocompleteItemType[]>([])
const flowsLoading = ref(false)
const flowResults = ref<IAutocompleteItemType[]>([])
const locationsLoading = ref(false)
const locationResults = ref<IAutocompleteItemType[]>([])
const serviceResults = ref<IAutocompleteItemType[]>([])
const isMessageDialogVisible = ref(false)
// we already have items in memory, don't really need to use setTimeout at all,
// but will keep it just to have the pattern. Timeout can be minimal (5ms)
const TIMEOUT = 5

const nodeStructureStore = useNodeStructureStore()
const showSecondCategories = ref(false)
const selectedFilters = reactive({
  categories: [] as IAutocompleteItemType[],
  categories2: [] as IAutocompleteItemType[],
  flows: [] as IAutocompleteItemType[],
  locations: [] as IAutocompleteItemType[],
  services: [] as IAutocompleteItemType[]
})

const filterCategoryItems = (query: string): IAutocompleteItemType[] => {
  const categoriesArray = Array.isArray(nodeStructureStore.categories)
    ? nodeStructureStore.categories
    : []
  return categoriesArray
    .filter(c => c.name && c.name.toLowerCase().includes(query.toLowerCase()))
    .map(c => ({ _text: c.name, _value: c.id } as IAutocompleteItemType))
}

const handleCategorySearch = (query: string) => {
  categoriesLoading.value = true
  clearTimeout(searchTimeout.value)
  searchTimeout.value = window.setTimeout(() => {
    categoryResults.value = filterCategoryItems(query)
    categoriesLoading.value = false
  }, TIMEOUT)
}

const handleCategory2Search = (query: string) => {
  categories2Loading.value = true
  clearTimeout(category2SearchTimeout.value)
  category2SearchTimeout.value = window.setTimeout(() => {
    category2Results.value = filterCategoryItems(query)
    categories2Loading.value = false
  }, TIMEOUT)
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
  }, TIMEOUT)
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
        name: location.name
      }))

    locationsLoading.value = false
  }, TIMEOUT)
}

const handleServiceSearch = (query: string) => {
  const lower = query.toLowerCase()
  serviceResults.value = nodeStructureStore.allServiceTypes
    .filter(s => s.name.toLowerCase().includes(lower))
    .map(s => ({ _value: s.id, _text: s.name } as IAutocompleteItemType))
}

const updateFilter = (key: keyof typeof selectedFilters, items: IAutocompleteItemType[]) => {
  selectedFilters[key] = items
}

const removeSecondCategories = () => {
  showSecondCategories.value = false
  selectedFilters.categories2 = []
}

const applySelectedFilters = () => {
  nodeStructureStore.updateSelectedCategories(selectedFilters.categories)
  nodeStructureStore.updateSelectedCategories2(showSecondCategories.value ? selectedFilters.categories2 : [])
  nodeStructureStore.updateSelectedFlows(selectedFilters.flows)
  nodeStructureStore.updateSelectedMonitoringLocations(selectedFilters.locations)
  nodeStructureStore.updateSelectedServices(selectedFilters.services)
  nodeStructureStore.closeInstancesDrawerModal()
}

watch(() => nodeStructureStore.drawerState.visible, (visible) => {
  if (visible) {
    selectedFilters.categories = [...nodeStructureStore.selectedCategories]
    selectedFilters.categories2 = [...nodeStructureStore.selectedCategories2]
    showSecondCategories.value = nodeStructureStore.selectedCategories2.length > 0
    selectedFilters.flows = [...nodeStructureStore.selectedFlows]
    selectedFilters.locations = [...nodeStructureStore.selectedMonitoringLocations]
    selectedFilters.services = [...nodeStructureStore.selectedServices]
    serviceResults.value = nodeStructureStore.allServiceTypes.map(s => ({ _value: s.id, _text: s.name } as IAutocompleteItemType))
  }
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/styles/mixins/elevation';
@use '@featherds/table/scss/table';

.feather-drawer-custom-padding {
  padding: 20px;
  height: 100%;
  overflow: auto;
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

.category-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.category-autocomplete {
  flex: 1;
}

.category-add-btn {
  flex-shrink: 0;
}

.info-section {
  margin-bottom: 1em;

  .label {
    color: var(variables.$primary-text-on-surface);
  }

  .info-icon {
    cursor: pointer;
    font-size: 1.5em;
    margin-left: 0.5em;
    color: var(variables.$primary);

    &:hover {
      opacity: 0.8;
    }
  }
}
</style>
