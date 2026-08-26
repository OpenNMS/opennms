<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="inventory-search">
    <div class="card search-panel">
      <div class="page-header">
        <h2 class="headline3" data-test="search-title">Search for Nodes</h2>
        <AboutDialogButton title="Search Inventory">
          <InventorySearchHelp />
        </AboutDialogButton>
      </div>
      <p class="subtitle">Search the inventory and open the matching nodes in the node list.</p>

      <div class="criteria-grid">
        <SearchField
          class="criterion"
          label="Name containing"
          for="search-nodename"
          testId="name"
          help="Case-insensitive substring match on the node label. Use _ to match a single character and % to match any number."
          @search="goToNodes({ nodename: form.nodename })"
        >
          <OnmsInputText id="search-nodename" v-model="form.nodename" fluid data-test="nodename-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="TCP/IP Address like"
          for="search-iplike"
          testId="iplike"
          help="Flexible address match: * is any octet, a-b is a range and commas list values (e.g. 10.1.1-3,5.*). IPv4 or IPv6."
          @search="goToNodes({ iplike: form.iplike })"
        >
          <OnmsInputText id="search-iplike" v-model="form.iplike" placeholder="*.*.*.* or *:*:*:*:*:*:*:*" fluid data-test="iplike-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="System attribute"
          for="search-mib2-value"
          testId="system"
          help="Match an SNMP MIB-2 system value. 'contains' is a substring match; 'equals' is exact."
          @search="goToNodes({ mib2Parm: form.mib2Parm, mib2ParmMatchType: form.mib2ParmMatchType, mib2ParmValue: form.mib2ParmValue })"
        >
          <OnmsSelect v-model="form.mib2Parm" :options="SYSTEM_ATTRIBUTES" optionLabel="label" optionValue="value" data-test="mib2-parm" />
          <OnmsSelect v-model="form.mib2ParmMatchType" :options="MATCH_TYPES" optionLabel="label" optionValue="value" data-test="mib2-match" />
          <OnmsInputText id="search-mib2-value" v-model="form.mib2ParmValue" fluid data-test="mib2-value" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Interface attribute"
          for="search-snmp-value"
          testId="interface"
          help="Match an SNMP interface value (ifAlias, ifName or ifDescr). 'contains' is a substring match; 'equals' is exact."
          @search="goToNodes({ snmpParm: form.snmpParm, snmpParmMatchType: form.snmpParmMatchType, snmpParmValue: form.snmpParmValue })"
        >
          <OnmsSelect v-model="form.snmpParm" :options="INTERFACE_ATTRIBUTES" optionLabel="label" optionValue="value" data-test="snmp-parm" />
          <OnmsSelect v-model="form.snmpParmMatchType" :options="MATCH_TYPES" optionLabel="label" optionValue="value" data-test="snmp-match" />
          <OnmsInputText id="search-snmp-value" v-model="form.snmpParmValue" fluid data-test="snmp-value" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Location"
          for="search-location"
          testId="location"
          help="Restrict results to nodes monitored from the selected location."
          @search="goToNodes({ monitoringLocation: form.monitoringLocation })"
        >
          <OnmsSelect
            v-model="form.monitoringLocation"
            inputId="search-location"
            :options="locationOptions"
            optionLabel="label"
            optionValue="value"
            filter
            fluid
            data-test="location-select"
          />
        </SearchField>

        <SearchField
          class="criterion"
          label="Providing service"
          for="search-service"
          testId="service"
          help="Restrict results to nodes that provide the selected monitored service."
          @search="goToNodes({ monitoredService: form.monitoredService })"
        >
          <OnmsSelect
            v-model="form.monitoredService"
            inputId="search-service"
            :options="serviceOptions"
            optionLabel="label"
            optionValue="value"
            filter
            fluid
            data-test="service-select"
          />
        </SearchField>

        <SearchField
          class="criterion"
          label="MAC Address like"
          for="search-mac"
          testId="mac"
          help="Case-insensitive partial match on interface MAC addresses; separators are optional. The first six hex digits identify the vendor."
          @search="goToNodes({ maclike: form.maclike })"
        >
          <OnmsInputText id="search-mac" v-model="form.maclike" fluid data-test="mac-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Foreign Source name like"
          for="search-fs"
          testId="fs"
          help="Substring match on the provisioning foreign source name."
          @search="goToNodes({ foreignSource: form.foreignSource })"
        >
          <OnmsInputText id="search-fs" v-model="form.foreignSource" fluid data-test="fs-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Flows"
          for="search-flows"
          testId="flows"
          @search="goToNodes({ flows: form.flows })"
        >
          <OnmsSelect v-model="form.flows" inputId="search-flows" :options="FLOW_OPTIONS" optionLabel="label" optionValue="value" fluid data-test="flows-select" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Category"
          for="search-category"
          testId="category"
          help="Filter by the asset 'category' value, which is distinct from surveillance categories."
          @search="goToNodes({ assetColumn: 'category', assetValue: form.assetCategory })"
        >
          <OnmsSelect v-model="form.assetCategory" inputId="search-category" :options="ASSET_CATEGORIES" optionLabel="label" optionValue="value" fluid data-test="category-select" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Enhanced Linkd topology (CDP/LLDP)"
          for="search-topology"
          testId="topology"
          help="Substring match on CDP/LLDP neighbour data discovered by Enhanced Linkd."
          @search="goToNodes({ topology: form.topology })"
        >
          <OnmsInputText id="search-topology" v-model="form.topology" fluid data-test="topology-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Asset field"
          for="search-field-value"
          testId="field"
          help="Search any asset inventory field; the value is matched as a case-insensitive substring."
          @search="goToNodes({ assetColumn: form.assetField, assetValue: form.assetFieldValue })"
        >
          <OnmsSelect v-model="form.assetField" :options="ASSET_FIELDS" optionLabel="label" optionValue="value" filter data-test="field-select" />
          <OnmsInputText id="search-field-value" v-model="form.assetFieldValue" placeholder="Containing text" fluid data-test="field-value" />
        </SearchField>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import InventorySearchHelp from '@/components/InventorySearch/InventorySearchHelp.vue'
import SearchField from '@/components/InventorySearch/SearchField.vue'
import {
  ASSET_CATEGORIES,
  ASSET_FIELDS,
  FLOW_OPTIONS,
  INTERFACE_ATTRIBUTES,
  MATCH_TYPES,
  Option,
  SYSTEM_ATTRIBUTES
} from '@/components/InventorySearch/options'
import API from '@/services'
import { BreadCrumb } from '@/types'

const router = useRouter()

const breadcrumbs: BreadCrumb[] = [
  { label: 'Home', to: '/' },
  { label: 'Search Inventory', to: '/inventory-search' }
]

const form = reactive({
  nodename: '',
  iplike: '',
  mib2Parm: SYSTEM_ATTRIBUTES[0].value,
  mib2ParmMatchType: MATCH_TYPES[0].value,
  mib2ParmValue: '',
  snmpParm: INTERFACE_ATTRIBUTES[0].value,
  snmpParmMatchType: MATCH_TYPES[0].value,
  snmpParmValue: '',
  monitoringLocation: '',
  monitoredService: '',
  maclike: '',
  foreignSource: '',
  flows: FLOW_OPTIONS[0].value,
  topology: '',
  assetCategory: ASSET_CATEGORIES[0].value,
  assetField: ASSET_FIELDS[0].value,
  assetFieldValue: ''
})

const locationOptions = ref<Option[]>([])
const serviceOptions = ref<Option[]>([])

// Route the chosen criterion into the node list, which already tracks these
// query keys; blank values are dropped so an empty field doesn't over-filter.
const goToNodes = (query: Record<string, string>) => {
  const cleaned: Record<string, string> = {}
  for (const [key, value] of Object.entries(query)) {
    if (value != null && String(value).trim() !== '') {
      cleaned[key] = String(value).trim()
    }
  }
  router.push({ path: '/nodes', query: cleaned })
}

onMounted(async () => {
  const locations = await API.getMonitoringLocations()
  if (locations) {
    locationOptions.value = locations.location
      .map(loc => loc.name)
      .filter((name): name is string => !!name)
      .map(name => ({ label: name, value: name }))
  }
  const services = await API.getServiceTypes()
  serviceOptions.value = services
    .map(svc => svc.name)
    .filter((name): name is string => !!name)
    .map(name => ({ label: name, value: name }))
})
</script>

<style scoped lang="scss">
.inventory-search {
  .card {
    padding: 1rem 1.25rem;
  }

  // fill the content width so the two columns use the horizontal space and
  // the whole form fits on a laptop screen without scrolling; capped so it
  // doesn't sprawl on ultrawide displays.
  .search-panel {
    max-width: 90rem;
  }

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .subtitle {
    margin: 0 0 1rem 0;
    color: var(--p-text-muted-color);
  }

  .criteria-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0.75rem 2rem;
  }

  .criterion {
    min-width: 0;
  }

  // collapse to one column once two would crowd the multi-control searches
  @media (max-width: 60rem) {
    .criteria-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }
}
</style>
