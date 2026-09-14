<template>
  <OnmsDrawer
    v-model:visible="drawerVisible"
    header="Advanced Node Filters"
    width="60em"
    data-test="left-drawer"
  >
    <div class="node-filters-drawer-custom-padding">
      <section>
        <h3>Advanced Filters</h3>
      </section>
      <div class="info-section">
        <span>Choose one or more attributes to find matching nodes.</span>
        <OnmsIcon
          :icon="InfoIcon"
          class="info-icon"
          @click="isMessageDialogVisible = true"
          data-test="advanced-filters-info-icon"
        />
      </div>
      <h4 class="title">Categories</h4>
      <div class="category-row">
        <FormField
          label="Categories"
          hint="You may select up to two category groups to filter nodes by"
          class="category-field"
          data-test="categories-multiselect"
        >
          <OnmsMultiSelect
            v-model="selectedFilters.categories"
            :options="categoryOptions"
            optionLabel="_text"
            dataKey="_value"
            filter
            display="chip"
            placeholder="Select categories"
            @update:modelValue="(items) => updateFilter('categories', items as IAutocompleteItemType[])"
          />
        </FormField>
        <OnmsIconButton
          v-if="!showSecondCategories"
          class="category-add-btn"
          :icon="AddIcon"
          aria-label="Add category group"
          @click="showSecondCategories = true"
        />
      </div>
      <div v-if="showSecondCategories" class="category-row">
        <FormField
          label="Additional Categories"
          class="category-field"
          data-test="categories2-multiselect"
        >
          <OnmsMultiSelect
            v-model="selectedFilters.categories2"
            :options="categoryOptions"
            optionLabel="_text"
            dataKey="_value"
            filter
            display="chip"
            placeholder="Select categories"
            @update:modelValue="(items) => updateFilter('categories2', items as IAutocompleteItemType[])"
          />
        </FormField>
        <OnmsIconButton
          class="category-add-btn"
          :icon="DeleteIcon"
          aria-label="Remove category group"
          @click="removeSecondCategories"
        />
      </div>
      <hr />
      <div class="spacer-large"></div>
      <div class="onms-row">
        <div class="onms-col-6">
          <FormField label="Monitoring Locations" data-test="locations-multiselect">
            <OnmsMultiSelect
              v-model="selectedFilters.locations"
              :options="locationOptions"
              optionLabel="_text"
              dataKey="_value"
              filter
              display="chip"
              placeholder="Select locations"
              @update:modelValue="(items) => updateFilter('locations', items as IAutocompleteItemType[])"
            />
          </FormField>
        </div>
        <div class="onms-col-6">
          <FormField label="Monitored Services" data-test="services-multiselect">
            <OnmsMultiSelect
              v-model="selectedFilters.services"
              :options="serviceOptions"
              optionLabel="_text"
              dataKey="_value"
              filter
              display="chip"
              placeholder="Select services"
              @update:modelValue="(items) => updateFilter('services', items as IAutocompleteItemType[])"
            />
          </FormField>
        </div>
      </div>
      <div class="onms-row">
        <div class="onms-col-6">
          <FormField
            label="IP Address / Pattern"
            :error="errors.ipAddress"
            data-test="ip-field"
          >
            <OnmsInputText
              class="filter-input"
              v-model="selectedFilters.ipAddress"
              :invalid="!!errors.ipAddress"
              data-test="ip-input"
            />
          </FormField>
        </div>
        <div class="onms-col-6">
          <FormField label="MAC Address" data-test="mac-field">
            <OnmsInputText
              class="filter-input"
              v-model="selectedFilters.macAddress"
              data-test="mac-input"
            />
          </FormField>
        </div>
      </div>
      <div class="onms-row">
        <div class="onms-col-6">
          <FormField label="Topology (CDP/LLDP)" data-test="topology-field">
            <OnmsInputText
              class="filter-input"
              v-model="selectedFilters.topology"
              data-test="topology-input"
            />
          </FormField>
        </div>
      </div>
      <div class="spacer-large"></div>
      <FormField label="Flows" data-test="flows-multiselect">
        <OnmsMultiSelect
          v-model="selectedFilters.flows"
          :options="flowOptions"
          optionLabel="_text"
          dataKey="_value"
          filter
          display="chip"
          placeholder="Select flows"
          @update:modelValue="(items) => updateFilter('flows', items as IAutocompleteItemType[])"
        />
      </FormField>
      <div class="spacer-medium"></div>
      <div class="onms-row">
        <div class="onms-col-12 toggle-row" data-test="down-only">
          <label for="down-only">Down nodes only (nodes with a down aggregate status)</label>
          <OnmsToggleSwitch
            v-model="selectedFilters.nodesWithDownAggregateStatus"
            inputId="down-only"
          />
        </div>
      </div>
      <div class="onms-row">
        <div class="onms-col-12 toggle-row" data-test="with-outages">
          <label for="with-outages">Nodes with current outages</label>
          <OnmsToggleSwitch
            v-model="selectedFilters.nodesWithOutages"
            inputId="with-outages"
          />
        </div>
      </div>
      <div class="onms-row">
        <div class="onms-col-12 toggle-row" data-test="with-assets">
          <label for="with-assets">Nodes with asset info only</label>
          <OnmsToggleSwitch
            v-model="selectedFilters.nodesWithAssets"
            inputId="with-assets"
          />
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
      <OnmsMessageDialog
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
            <p>You can filter by categories, monitoring locations, monitored services, IP address, MAC address, topology, flow type, down status, asset fields, current outages and multiple extended search parameters.</p>
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
            <p><strong>Nodes with current outages</strong></p>
            <p>Limits results to nodes that have one or more services currently in outage.</p>
            <br />
            <p><strong>Nodes with asset info only</strong></p>
            <p>Limits results to nodes that have at least one non-empty asset-record field.</p>
            <br />
            <p><strong>Asset Fields</strong></p>
            <p>Filter by one or more node asset-record fields (such as Building, Region, or Rack). Choose an asset field, enter a value, and click Add. Each added field is an exact match, and multiple asset fields are intersected (a node must match all of them).</p>
            <p>Enable "Featured Fields Only" to display a short list of featured asset fields. Disable it to choose from all possible fields.</p>
            <br />
            <p><strong>Extended Search</strong></p>
            <p>Extended search allows you to perform more complex queries across multiple fields and criteria, including requisition, asset, and SNMP fields.</p>
            <p>Choose a search type, then a search term, and click Add to add it as a filter. You may add multiple filters.</p>
            <p>This is a case-insensitive partial string match against the selected field.</p>
          </div>
        </template>
      </OnmsMessageDialog>
    </div>
    <template #footer>
      <div class="footer">
        <OnmsButton
          :disabled="isApplyDisabled"
          @click="applySelectedFilters"
        >
          Apply Filters
        </OnmsButton>
        <OnmsButton
          variant="outlined"
          @click="clearDrawerFilters"
        >
          Clear Filters
        </OnmsButton>
        <OnmsButton
          variant="outlined"
          @click="nodeListStore.closeInstancesDrawerModal()"
        >
          Close
        </OnmsButton>
      </div>
    </template>
  </OnmsDrawer>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, watch, watchEffect } from 'vue'
import { isIP } from 'is-ip'
import { isIplikePattern } from '@/components/Nodes/hooks/queryStringParser'
import { IAutocompleteItemType } from '@/types'
import { OnmsButton, OnmsDrawer, OnmsIcon, OnmsIconButton, OnmsInputText, OnmsMessageDialog, OnmsMultiSelect, OnmsToggleSwitch } from '@opennms/onms-ui'
import AddIcon from '@opennms/onms-ui/icons/action/Add.vue'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import FormField from '@/components/Common/FormField.vue'
import ExtendedSearchPanel from './ExtendedSearchPanel.vue'
import AssetFilterPanel from './AssetFilterPanel.vue'
import { useNodeListStore } from '@/stores/nodeListStore'

interface DrawerErrors {
  ipAddress?: string
}

type ExtendedSearchPanelInstance = InstanceType<typeof ExtendedSearchPanel>
type AssetFilterPanelInstance = InstanceType<typeof AssetFilterPanel>

const isMessageDialogVisible = ref(false)
const errors = ref<DrawerErrors>({})
const isApplyDisabled = ref(false)

const nodeListStore = useNodeListStore()
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
  nodesWithAssets: false,
  nodesWithOutages: false
})

const drawerVisible = computed({
  get: () => nodeListStore.drawerState.visible,
  set: (val: boolean) => {
    if (!val) {
      nodeListStore.closeInstancesDrawerModal()
    }
  }
})

// Full option lists for each MultiSelect (the same lists the old @search
// handlers filtered over). MultiSelect performs its own client-side filtering.
const categoryOptions = computed<IAutocompleteItemType[]>(() => {
  const categoriesArray = Array.isArray(nodeListStore.categories)
    ? nodeListStore.categories
    : []
  return categoriesArray.map(c => ({ _text: c.name, _value: c.id } as IAutocompleteItemType))
})

const locationOptions = computed<IAutocompleteItemType[]>(() =>
  nodeListStore.monitoringLocations.map(location => ({
    _text: location.name,
    _value: location.name,
    name: location.name
  } as IAutocompleteItemType))
)

const serviceOptions = computed<IAutocompleteItemType[]>(() =>
  nodeListStore.allServiceTypes.map(s => ({ _value: s.id, _text: s.name } as IAutocompleteItemType))
)

const flowOptions = computed<IAutocompleteItemType[]>(() => [
  { _text: 'Egress', _value: 'lastEgressFlow' } as IAutocompleteItemType,
  { _text: 'Ingress', _value: 'lastIngressFlow' } as IAutocompleteItemType,
  { _text: 'No Flows', _value: 'noFlows' } as IAutocompleteItemType
])

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
  nodeListStore.updateSelectedCategories(selectedFilters.categories)
  nodeListStore.updateSelectedCategories2(showSecondCategories.value ? selectedFilters.categories2 : [])
  nodeListStore.updateSelectedFlows(selectedFilters.flows)
  nodeListStore.updateSelectedMonitoringLocations(selectedFilters.locations)
  nodeListStore.updateSelectedServices(selectedFilters.services)
  nodeListStore.setFilterWithIpAddress(selectedFilters.ipAddress)
  nodeListStore.setFilterWithMacAddress(selectedFilters.macAddress)
  nodeListStore.setFilterWithDownAggregateStatus(selectedFilters.nodesWithDownAggregateStatus)
  nodeListStore.setFilterWithNodesWithAssets(selectedFilters.nodesWithAssets)
  nodeListStore.setFilterWithNodesWithOutages(selectedFilters.nodesWithOutages)
  nodeListStore.setFilterWithTopology(selectedFilters.topology)
  assetFilterPanelRef.value?.applyToStore()
  extendedSearchPanelRef.value?.applyToStore()
  nodeListStore.closeInstancesDrawerModal()
}

const clearDrawerFilters = async () => {
  await nodeListStore.clearAllFiltersAndSelections()
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
  selectedFilters.nodesWithOutages = false
  showSecondCategories.value = false
  assetFilterPanelRef.value?.resetFromStore()
  extendedSearchPanelRef.value?.resetFromStore()
}

watch(() => nodeListStore.drawerState.visible, (visible) => {
  if (visible) {
    selectedFilters.categories = [...nodeListStore.selectedCategories]
    selectedFilters.categories2 = [...nodeListStore.selectedCategories2]
    showSecondCategories.value = nodeListStore.selectedCategories2.length > 0
    selectedFilters.flows = [...nodeListStore.selectedFlows]
    selectedFilters.locations = [...nodeListStore.selectedMonitoringLocations]
    selectedFilters.services = [...nodeListStore.selectedServices]
    selectedFilters.ipAddress = nodeListStore.queryFilter.ipAddress ?? ''
    selectedFilters.macAddress = nodeListStore.queryFilter.macAddress ?? ''
    selectedFilters.topology = nodeListStore.queryFilter.topology ?? ''
    selectedFilters.nodesWithDownAggregateStatus = nodeListStore.queryFilter.nodesWithDownAggregateStatus ?? false
    selectedFilters.nodesWithAssets = nodeListStore.queryFilter.nodesWithAssets ?? false
    selectedFilters.nodesWithOutages = nodeListStore.queryFilter.nodesWithOutages ?? false
    assetFilterPanelRef.value?.resetFromStore()
    extendedSearchPanelRef.value?.resetFromStore()
  }
})

defineExpose({
  selectedFilters,
  isApplyDisabled,
  drawerVisible,
  showSecondCategories
})
</script>

<style lang="scss" scoped>
@use '@/styles/onms-tokens' as variables;
@use '@/styles/onms-typography' as *;

// No height/overflow here on purpose: PrimeVue's own `.p-drawer-content` is the
// drawer's scroll container. A second scroller nested inside it drew its own
// scrollbar next to the page's (NMS-20182).
.node-filters-drawer-custom-padding {
  padding: 20px;

  // The onms grid `gap` only spaces columns, so consecutive filter rows were
  // cramped vertically. Give each stacked row/group breathing room.
  .onms-row,
  .category-row {
    margin-bottom: 1.5rem;
  }
}

.spacer-large {
  margin-bottom: 2rem;
}

.spacer-medium {
  margin-bottom: 1.25rem;
}

.footer {
  display: flex;
  gap: 0.5rem;
  padding-top: 20px;
}

.filter-input {
  display: block;
  width: 100%;
}

// Label on the left, ToggleSwitch pushed to the right edge. Both rows are
// full-width (onms-col-12), so the switches line up vertically across rows.
.toggle-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.category-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.category-field {
  flex: 1;
}

.category-add-btn {
  flex-shrink: 0;
  // Line the icon button up with the MultiSelect (drop it below the FormField
  // label) and size it to match the control rather than the tiny default glyph.
  margin-top: 1.25rem;
  height: 3rem;
  width: 3rem;
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
