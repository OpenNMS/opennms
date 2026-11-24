<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="inventory-search">
    <div class="page-header">
      <div class="title-row">
        <h1 class="page-title" data-test="search-title">Search for Nodes</h1>
        <AboutDialogButton title="Search Inventory">
          <InventorySearchHelp />
        </AboutDialogButton>
      </div>
      <p class="page-subtitle">Search the inventory and open the matching nodes in the node list.</p>
    </div>

    <div class="card search-panel">
      <div class="criteria-grid">
        <SearchField
          class="criterion"
          label="Name containing"
          for="search-nodename"
          testId="name"
          help="Substring match on the node label; use * as a wildcard (e.g. core* or *sw*)."
          :error="errors.name"
          @search="search('name', 'nodename', { nodename: form.nodename })"
        >
          <OnmsInputText id="search-nodename" v-model="form.nodename" fluid :invalid="!!errors.name" data-test="nodename-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="TCP/IP Address like"
          for="search-iplike"
          testId="iplike"
          help="Flexible address match: * is any octet, a-b is a range and commas list values (e.g. 10.1.1-3,5.*). IPv4 or IPv6."
          :error="errors.iplike"
          @search="search('iplike', 'iplike', { iplike: form.iplike })"
        >
          <OnmsInputText id="search-iplike" v-model="form.iplike" placeholder="*.*.*.* or *:*:*:*:*:*:*:*" fluid :invalid="!!errors.iplike" data-test="iplike-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="System attribute"
          for="search-mib2-value"
          testId="system"
          help="Match an SNMP MIB-2 system value. 'contains' is a substring match; 'equals' is exact."
          :error="errors.system"
          @search="search('system', 'mib2ParmValue', { mib2Parm: form.mib2Parm, mib2ParmMatchType: form.mib2ParmMatchType, mib2ParmValue: form.mib2ParmValue })"
        >
          <OnmsSelect v-model="form.mib2Parm" :options="SYSTEM_ATTRIBUTES" optionLabel="label" optionValue="value" data-test="mib2-parm" />
          <OnmsSelect v-model="form.mib2ParmMatchType" :options="MATCH_TYPES" optionLabel="label" optionValue="value" data-test="mib2-match" />
          <OnmsInputText id="search-mib2-value" v-model="form.mib2ParmValue" fluid :invalid="!!errors.system" data-test="mib2-value" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Interface attribute"
          for="search-snmp-value"
          testId="interface"
          help="Match an SNMP interface value (ifAlias, ifName or ifDescr). 'contains' is a substring match; 'equals' is exact."
          :error="errors.interface"
          @search="search('interface', 'snmpParmValue', { snmpParm: form.snmpParm, snmpParmMatchType: form.snmpParmMatchType, snmpParmValue: form.snmpParmValue })"
        >
          <OnmsSelect v-model="form.snmpParm" :options="INTERFACE_ATTRIBUTES" optionLabel="label" optionValue="value" data-test="snmp-parm" />
          <OnmsSelect v-model="form.snmpParmMatchType" :options="MATCH_TYPES" optionLabel="label" optionValue="value" data-test="snmp-match" />
          <OnmsInputText id="search-snmp-value" v-model="form.snmpParmValue" fluid :invalid="!!errors.interface" data-test="snmp-value" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Location"
          for="search-location"
          testId="location"
          help="Restrict results to nodes monitored from the selected location."
          :error="errors.location"
          @search="search('location', 'monitoringLocation', { monitoringLocation: form.monitoringLocation })"
        >
          <OnmsSelect
            v-model="form.monitoringLocation"
            inputId="search-location"
            :options="locationOptions"
            :loading="loadingOptions"
            optionLabel="label"
            optionValue="value"
            filter
            fluid
            :invalid="!!errors.location"
            data-test="location-select"
          />
        </SearchField>

        <SearchField
          class="criterion"
          label="Providing service"
          for="search-service"
          testId="service"
          help="Restrict results to nodes that provide the selected monitored service."
          :error="errors.service"
          @search="search('service', 'monitoredService', { monitoredService: form.monitoredService })"
        >
          <OnmsSelect
            v-model="form.monitoredService"
            inputId="search-service"
            :options="serviceOptions"
            :loading="loadingOptions"
            optionLabel="label"
            optionValue="value"
            filter
            fluid
            :invalid="!!errors.service"
            data-test="service-select"
          />
        </SearchField>

        <SearchField
          class="criterion"
          label="MAC Address like"
          for="search-mac"
          testId="mac"
          help="Case-insensitive partial match on interface MAC addresses; separators are optional. The first six hex digits identify the vendor."
          :error="errors.mac"
          @search="search('mac', 'maclike', { maclike: form.maclike })"
        >
          <OnmsInputText id="search-mac" v-model="form.maclike" fluid :invalid="!!errors.mac" data-test="mac-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Foreign Source name"
          for="search-fs"
          testId="fs"
          help="Exact match on the provisioning foreign source name."
          :error="errors.fs"
          @search="search('fs', 'foreignSource', { foreignSource: form.foreignSource })"
        >
          <OnmsInputText id="search-fs" v-model="form.foreignSource" fluid :invalid="!!errors.fs" data-test="fs-input" />
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
          :error="errors.topology"
          @search="search('topology', 'topology', { topology: form.topology })"
        >
          <OnmsInputText id="search-topology" v-model="form.topology" fluid :invalid="!!errors.topology" data-test="topology-input" />
        </SearchField>

        <SearchField
          class="criterion"
          label="Asset field"
          for="search-field-value"
          testId="field"
          help="Search any asset inventory field by its exact value (no wildcards)."
          :error="errors.field"
          @search="search('field', 'assetFieldValue', { assetColumn: form.assetField, assetValue: form.assetFieldValue })"
        >
          <OnmsSelect v-model="form.assetField" :options="ASSET_FIELDS" optionLabel="label" optionValue="value" filter data-test="field-select" />
          <OnmsInputText id="search-field-value" v-model="form.assetFieldValue" placeholder="Exact value" fluid :invalid="!!errors.field" data-test="field-value" />
        </SearchField>
      </div>

      <div class="quick-links">
        <router-link :to="{ path: '/nodes' }" data-test="link-all-nodes">All nodes</router-link>
        <router-link :to="{ path: '/nodes', query: { nodesWithAssets: 'true' } }" data-test="link-all-assets">All nodes with asset info</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsInputText, OnmsSelect, useOnmsToast } from '@opennms/onms-ui'

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
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const router = useRouter()
const menuStore = useMenuStore()
const { showToast } = useOnmsToast()
const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Search Inventory', to: '#', position: 'last' }
])

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

type FormKey = keyof typeof form

// per-criterion inline error, keyed by the SearchField testId
const errors = reactive<Record<string, string | undefined>>({})
const requiredFields: Record<string, FormKey> = {}

const locationOptions = ref<Option[]>([])
const loadingOptions = ref(true)
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

// Each criterion searches on its own; refuse a blank value inline rather than
// silently listing every node.
const search = (criterion: string, required: FormKey, query: Record<string, string>) => {
  requiredFields[criterion] = required
  if (String(form[required] ?? '').trim() === '') {
    errors[criterion] = criterion === 'location' || criterion === 'service' ? 'Select a value to search for.' : 'Enter a value to search for.'
    return
  }
  errors[criterion] = undefined
  goToNodes(query)
}

watch(form, () => {
  for (const [criterion, key] of Object.entries(requiredFields)) {
    if (errors[criterion] && String(form[key] ?? '').trim() !== '') {
      errors[criterion] = undefined
    }
  }
})

onMounted(async () => {
  try {
    const [locations, services] = await Promise.all([API.getMonitoringLocations(), API.getServiceTypes()])
    if (locations) {
      locationOptions.value = locations.location
        .map(loc => loc.name)
        .filter((name): name is string => !!name)
        .map(name => ({ label: name, value: name }))
    } else {
      showToast({ message: 'Failed to load monitoring locations.', severity: 'error' })
    }
    serviceOptions.value = services
      .map(svc => svc.name)
      .filter((name): name is string => !!name)
      .map(name => ({ label: name, value: name }))
  } finally {
    loadingOptions.value = false
  }
})
</script>

<style scoped lang="scss">
.inventory-search {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;

  .card {
    padding: 1.5rem;
  }

  // fill the content width so the two columns use the horizontal space and
  // the whole form fits on a laptop screen without scrolling; capped so it
  // doesn't sprawl on ultrawide displays.
  .search-panel {
    max-width: 90rem;
  }

  .title-row {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }

  .page-title {
    font-size: 1.4rem;
    font-weight: 600;
    margin: 0;
  }

  .page-subtitle {
    margin: 0.25rem 0 0 0;
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

  .quick-links {
    display: flex;
    gap: 1.5rem;
    margin-top: 1.25rem;
    padding-top: 0.75rem;
    border-top: 1px solid var(--p-content-border-color, rgba(127, 127, 127, 0.2));
  }

  // collapse to one column once two would crowd the multi-control searches
  @media (max-width: 60rem) {
    .criteria-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }
}
</style>
