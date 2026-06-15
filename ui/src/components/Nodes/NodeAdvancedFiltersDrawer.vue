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
          data-test="advanced-filters-info-icon"
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
      <hr />
      <div class="spacer-large"></div>
      <div class="feather-row">
        <div class="feather-col-6">
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
        </div>
        <div class="feather-col-6">
          <FeatherAutocomplete
            class="filter-autocomplete"
            label="Monitored Services"
            type="multi"
            v-model="selectedFilters.services"
            :results="serviceResults"
            @search="handleServiceSearch"
            @update:modelValue="(items: any) => updateFilter('services', items)"
            text-prop="_text"
          ></FeatherAutocomplete>
        </div>
      </div>
      <div class="feather-row">
        <div class="feather-col-6">
          <FeatherInput
            class="filter-input"
            label="IP Address / Pattern"
            v-model="selectedFilters.ipAddress"
            :error="errors.ipAddress"
          />
        </div>
        <div class="feather-col-6">
          <FeatherInput
            class="filter-input last-filter-input"
            label="MAC Address"
            v-model="selectedFilters.macAddress"
          />
        </div>
      </div>
      <div class="feather-row">
        <div class="feather-col-6">
          <FeatherInput
            class="filter-input last-filter-input"
            label="Topology (CDP/LLDP)"
            v-model="selectedFilters.topology"
          />
        </div>
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
      <div class="spacer-medium"></div>
      <div class="feather-row">
        <div class="feather-col-12">
          <FeatherCheckbox
            v-model="selectedFilters.nodesWithDownAggregateStatus"
          >
            Down nodes only (nodes with a down aggregate status)
          </FeatherCheckbox>
        </div>
      </div>
      <div class="feather-row">
        <div class="feather-col-12">
          <FeatherCheckbox
            v-model="selectedFilters.nodesWithAssets"
          >
            Nodes with asset info only
          </FeatherCheckbox>
        </div>
      </div>
      <div class="spacer-medium"></div>
      <hr />
      <div class="spacer-medium"></div>
      <div>
        <h4 class="title">Asset Fields</h4>
        <div class="spacer-medium"></div>
        <AssetFilterPanel ref="assetFilterPanelRef" />
      </div>
      <div class="spacer-medium"></div>
      <hr />
      <div class="spacer-medium"></div>
      <div>
        <h4 class="title">Extended Search</h4>
        <div class="spacer-medium"></div>
        <ExtendedSearchPanel ref="extendedSearchPanelRef" />
      </div>
      <div class="footer">
        <FeatherButton
          primary
          :disabled="isApplyDisabled"
          @click="applySelectedFilters"
        >
          Apply Filters
        </FeatherButton>
        <FeatherButton
          secondary
          @click="clearDrawerFilters"
        >
          Clear Filters
        </FeatherButton>
        <FeatherButton
          secondary
          @click="nodeStructureStore.closeInstancesDrawerModal()"
        >
          Close
        </FeatherButton>
      </div>
      <MessageDialog
        :visible="isMessageDialogVisible"
        :relative="true"
        maxHeight="22em"
        maxWidth="50em"
        title="Advanced Filters"
        @close="isMessageDialogVisible = false"
      >
        <template #content>
          <div>
            <p>Use the advanced filters to find nodes that match specific criteria.</p>
            <p>You can filter by categories, monitoring locations, monitored services, IP address, MAC address, topology, flow type, down status, asset fields and multiple extended search parameters.</p>
            <br />
            <p><strong>Categories</strong></p>
            <p>You may select up to two category groups to filter nodes by. Each category group can contain multiple categories "unioned" together.</p>
            <p>Category groups are then "intersected" with each other to refine the search results.</p>
            <br />
            <p><strong>Monitoring Location / Monitored Service</strong></p>
            <p>To search by Monitoring Location or Monitored Service, click the down arrow and select the location or service you would like to search for.</p>
            <br />
            <p><strong>IP Address / IPLIKE</strong></p>
            <p>Searching by TCP/IP address uses a very flexible search format, allowing you to separate the four or eight (in case of IPv6) fields of a TCP/IP address into specific searches.</p>
            <p>An asterisk (*) in place of any octet matches any value for that octet.</p>
            <p>Ranges are indicated by two numbers separated by a dash (-), and commas (,) are used for list demarcation.</p>
            <br />
            <p>For example, the following search fields are all valid:</p>
            <ul>
              <li><code>10.0.*.*</code> (multiple wildcards)</li>
              <li><code>10.0.0.1-255</code> (range in last octet)</li>
              <li><code>10.0.0-255.1-255</code> (range in third and fourth octets)</li>
              <li><code>10.9.1-3.*</code> (range in third octet with wildcard in last octet)</li>
              <li><code>192.168.0,1.*</code> (comma list in third octet, wildcard in last octet)</li>
              <li><code>192.168.1,2,3-255.*</code> (comma + range combination, with wildcard in last octet)</li>
              <li><code>2001:0-ffff:*:*:*:*:*:*</code> (IPv6 range and wildcards)</li>
              <li><code>fc00,fe80:*:*:*:*:*:*:*</code> (IPv6 comma and wildcards)</li>
            </ul>
            <br />
            <p><strong>MAC Address</strong></p>
            <p>Searching by MAC Address allows you to find interfaces with hardware (MAC) addresses matching the search string. This is a case-insensitive partial string match. For example, you can find all interfaces with a specified manufacturer's code by entering the first 6 characters of the mac address. Octet separators (dash or colon) are optional.</p>
            <br />
            <p><strong>Topology</strong></p>
            <p>Searching by Topology allows you to find nodes which have CDP or LLDP neighbors matching the search string. This is a case-insensitive partial string match against the neighbor's system name, system description, and interface name.</p>
            <br />
            <p><strong>Flows</strong></p>
            <p>Filtering by Flows allows you to find nodes which have ingress flows, egress flows or no flows.</p>
            <br />
            <p><strong>Down nodes only</strong></p>
            <p>Limits results to nodes with a down aggregate status, i.e. nodes that have at least one active monitored service currently in outage.</p>
            <br />
            <p><strong>Nodes with asset info only</strong></p>
            <p>Limits results to nodes that have at least one non-empty asset-record field.</p>
            <br />
            <p><strong>Asset Fields</strong></p>
            <p>Filter by one or more node asset-record fields (such as Building, Region, or Rack). Choose an asset field, enter a value, and click Add. Each added field is an exact match, and multiple asset fields are intersected (a node must match all of them).</p>
            <br />
            <p><strong>Extended Search</strong></p>
            <p>Extended search allows you to perform more complex queries across multiple fields and criteria, including requisition, asset, and SNMP fields.</p>
            <p>Choose a search type, then a search term and click Add to add it as a filter. You may add multiple filters.</p>
            <p>This is a case-insensitive partial string match against the selected field.</p>
          </div>
        </template>
      </MessageDialog>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, watchEffect } from 'vue'
import { isIP } from 'is-ip'
import { isIplikePattern } from '@/components/Nodes/hooks/queryStringParser'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherButton } from '@featherds/button'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import AddIcon from '@featherds/icon/action/Add'
import DeleteIcon from '@featherds/icon/action/Delete'
import InfoIcon from '@featherds/icon/action/Info'
import MessageDialog from '../Common/MessageDialog.vue'
import ExtendedSearchPanel from './ExtendedSearchPanel.vue'
import AssetFilterPanel from './AssetFilterPanel.vue'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'

interface DrawerErrors {
  ipAddress?: string
}

type ExtendedSearchPanelInstance = InstanceType<typeof ExtendedSearchPanel>
type AssetFilterPanelInstance = InstanceType<typeof AssetFilterPanel>

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
const errors = ref<DrawerErrors>({})
const isApplyDisabled = ref(false)
// we already have items in memory, don't really need to use setTimeout at all,
// but will keep it just to have the pattern. Timeout can be minimal (5ms)
const TIMEOUT = 5

const nodeStructureStore = useNodeStructureStore()
const extendedSearchPanelRef = ref<ExtendedSearchPanelInstance | null>(null)
const assetFilterPanelRef = ref<AssetFilterPanelInstance | null>(null)
const showSecondCategories = ref(false)
const selectedFilters = reactive({
  categories: [] as IAutocompleteItemType[],
  categories2: [] as IAutocompleteItemType[],
  flows: [] as IAutocompleteItemType[],
  locations: [] as IAutocompleteItemType[],
  services: [] as IAutocompleteItemType[],
  ipAddress: '',
  macAddress: '',
  topology: '',
  nodesWithDownAggregateStatus: false,
  nodesWithAssets: false
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
      { _text: 'Ingress', _value: 'lastIngressFlow' },
      { _text: 'No Flows', _value: 'noFlows' }
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
  selectedFilters[key as 'categories' | 'categories2' | 'flows' | 'locations' | 'services'] = items
}

const removeSecondCategories = () => {
  showSecondCategories.value = false
  selectedFilters.categories2 = []
}

const validate = (): DrawerErrors => {
  const errs: DrawerErrors = {}
  const ip = selectedFilters.ipAddress
  if (ip && !isIP(ip) && !isIplikePattern(ip)) {
    errs.ipAddress = 'Enter a valid IPv4/IPv6 address or iplike pattern (e.g. 192.168.1.*, 10.0.0.1-255, 10.9.1-3.*, 192.168.0,1.*, 2001:db8:*:*:*:*:*:*)'
  }
  return errs
}

watchEffect(() => {
  errors.value = validate()
  isApplyDisabled.value = Object.keys(errors.value).length > 0
})

const applySelectedFilters = () => {
  nodeStructureStore.updateSelectedCategories(selectedFilters.categories)
  nodeStructureStore.updateSelectedCategories2(showSecondCategories.value ? selectedFilters.categories2 : [])
  nodeStructureStore.updateSelectedFlows(selectedFilters.flows)
  nodeStructureStore.updateSelectedMonitoringLocations(selectedFilters.locations)
  nodeStructureStore.updateSelectedServices(selectedFilters.services)
  nodeStructureStore.setFilterWithIpAddress(selectedFilters.ipAddress)
  nodeStructureStore.setFilterWithMacAddress(selectedFilters.macAddress)
  nodeStructureStore.setFilterWithDownAggregateStatus(selectedFilters.nodesWithDownAggregateStatus)
  nodeStructureStore.setFilterWithNodesWithAssets(selectedFilters.nodesWithAssets)
  nodeStructureStore.setFilterWithTopology(selectedFilters.topology)
  assetFilterPanelRef.value?.applyToStore()
  extendedSearchPanelRef.value?.applyToStore()
  nodeStructureStore.closeInstancesDrawerModal()
}

const clearDrawerFilters = async () => {
  await nodeStructureStore.clearAllFiltersAndSelections()
  selectedFilters.categories = []
  selectedFilters.categories2 = []
  selectedFilters.flows = []
  selectedFilters.locations = []
  selectedFilters.services = []
  selectedFilters.ipAddress = ''
  selectedFilters.macAddress = ''
  selectedFilters.topology = ''
  selectedFilters.nodesWithDownAggregateStatus = false
  selectedFilters.nodesWithAssets = false
  showSecondCategories.value = false
  assetFilterPanelRef.value?.resetFromStore()
  extendedSearchPanelRef.value?.resetFromStore()
}

watch(() => nodeStructureStore.drawerState.visible, (visible) => {
  if (visible) {
    selectedFilters.categories = [...nodeStructureStore.selectedCategories]
    selectedFilters.categories2 = [...nodeStructureStore.selectedCategories2]
    showSecondCategories.value = nodeStructureStore.selectedCategories2.length > 0
    selectedFilters.flows = [...nodeStructureStore.selectedFlows]
    selectedFilters.locations = [...nodeStructureStore.selectedMonitoringLocations]
    selectedFilters.services = [...nodeStructureStore.selectedServices]
    selectedFilters.ipAddress = nodeStructureStore.queryFilter.ipAddress ?? ''
    selectedFilters.macAddress = nodeStructureStore.queryFilter.macAddress ?? ''
    selectedFilters.topology = nodeStructureStore.queryFilter.topology ?? ''
    selectedFilters.nodesWithDownAggregateStatus = nodeStructureStore.queryFilter.nodesWithDownAggregateStatus ?? false
    selectedFilters.nodesWithAssets = nodeStructureStore.queryFilter.nodesWithAssets ?? false
    serviceResults.value = nodeStructureStore.allServiceTypes.map(s => ({ _value: s.id, _text: s.name } as IAutocompleteItemType))
    assetFilterPanelRef.value?.resetFromStore()
    extendedSearchPanelRef.value?.resetFromStore()
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

.filter-input {
  display: block;
  width: 100%;
}

.last-filter-input {
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
