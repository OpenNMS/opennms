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

      <form class="criterion" data-test="criterion-name" @submit.prevent="goToNodes({ nodename: form.nodename })">
        <FormField label="Name containing" for="search-nodename">
          <div class="control-row">
            <OnmsInputText id="search-nodename" v-model="form.nodename" fluid data-test="nodename-input" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by name" data-test="nodename-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-iplike" @submit.prevent="goToNodes({ iplike: form.iplike })">
        <FormField label="TCP/IP Address like" for="search-iplike">
          <div class="control-row">
            <OnmsInputText id="search-iplike" v-model="form.iplike" placeholder="*.*.*.* or *:*:*:*:*:*:*:*" fluid data-test="iplike-input" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by IP address" data-test="iplike-search" />
          </div>
        </FormField>
      </form>

      <form
        class="criterion"
        data-test="criterion-system"
        @submit.prevent="goToNodes({ mib2Parm: form.mib2Parm, mib2ParmMatchType: form.mib2ParmMatchType, mib2ParmValue: form.mib2ParmValue })"
      >
        <FormField label="System attribute" for="search-mib2-value">
          <div class="control-row">
            <OnmsSelect v-model="form.mib2Parm" :options="SYSTEM_ATTRIBUTES" optionLabel="label" optionValue="value" data-test="mib2-parm" />
            <OnmsSelect v-model="form.mib2ParmMatchType" :options="MATCH_TYPES" optionLabel="label" optionValue="value" data-test="mib2-match" />
            <OnmsInputText id="search-mib2-value" v-model="form.mib2ParmValue" fluid data-test="mib2-value" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by system attribute" data-test="mib2-search" />
          </div>
        </FormField>
      </form>

      <form
        class="criterion"
        data-test="criterion-interface"
        @submit.prevent="goToNodes({ snmpParm: form.snmpParm, snmpParmMatchType: form.snmpParmMatchType, snmpParmValue: form.snmpParmValue })"
      >
        <FormField label="Interface attribute" for="search-snmp-value">
          <div class="control-row">
            <OnmsSelect v-model="form.snmpParm" :options="INTERFACE_ATTRIBUTES" optionLabel="label" optionValue="value" data-test="snmp-parm" />
            <OnmsSelect v-model="form.snmpParmMatchType" :options="MATCH_TYPES" optionLabel="label" optionValue="value" data-test="snmp-match" />
            <OnmsInputText id="search-snmp-value" v-model="form.snmpParmValue" fluid data-test="snmp-value" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by interface attribute" data-test="snmp-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-location" @submit.prevent="goToNodes({ monitoringLocation: form.monitoringLocation })">
        <FormField label="Location" for="search-location">
          <div class="control-row">
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
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by location" data-test="location-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-service" @submit.prevent="goToNodes({ monitoredService: form.monitoredService })">
        <FormField label="Providing service" for="search-service">
          <div class="control-row">
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
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by service" data-test="service-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-mac" @submit.prevent="goToNodes({ maclike: form.maclike })">
        <FormField label="MAC Address like" for="search-mac">
          <div class="control-row">
            <OnmsInputText id="search-mac" v-model="form.maclike" fluid data-test="mac-input" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by MAC address" data-test="mac-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-fs" @submit.prevent="goToNodes({ foreignSource: form.foreignSource })">
        <FormField label="Foreign Source name like" for="search-fs">
          <div class="control-row">
            <OnmsInputText id="search-fs" v-model="form.foreignSource" fluid data-test="fs-input" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by foreign source" data-test="fs-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-flows" @submit.prevent="goToNodes({ flows: form.flows })">
        <FormField label="Flows" for="search-flows">
          <div class="control-row">
            <OnmsSelect v-model="form.flows" inputId="search-flows" :options="FLOW_OPTIONS" optionLabel="label" optionValue="value" fluid data-test="flows-select" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by flow data" data-test="flows-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-topology" @submit.prevent="goToNodes({ topology: form.topology })">
        <FormField label="Enhanced Linkd topology (CDP/LLDP)" for="search-topology">
          <div class="control-row">
            <OnmsInputText id="search-topology" v-model="form.topology" fluid data-test="topology-input" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by topology" data-test="topology-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-category" @submit.prevent="goToNodes({ assetColumn: 'category', assetValue: form.assetCategory })">
        <FormField label="Category" for="search-category">
          <div class="control-row">
            <OnmsSelect v-model="form.assetCategory" inputId="search-category" :options="ASSET_CATEGORIES" optionLabel="label" optionValue="value" fluid data-test="category-select" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by asset category" data-test="category-search" />
          </div>
        </FormField>
      </form>

      <form class="criterion" data-test="criterion-field" @submit.prevent="goToNodes({ assetColumn: form.assetField, assetValue: form.assetFieldValue })">
        <FormField label="Asset field" for="search-field-value">
          <div class="control-row">
            <OnmsSelect v-model="form.assetField" :options="ASSET_FIELDS" optionLabel="label" optionValue="value" filter data-test="field-select" />
            <OnmsInputText id="search-field-value" v-model="form.assetFieldValue" placeholder="Containing text" fluid data-test="field-value" />
            <OnmsButton type="submit" icon="pi pi-search" aria-label="Search by asset field" data-test="field-search" />
          </div>
        </FormField>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsButton, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import InventorySearchHelp from '@/components/InventorySearch/InventorySearchHelp.vue'
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

  .search-panel {
    max-width: 48rem;
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

  .criterion {
    margin-bottom: 0.75rem;
  }

  .control-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    :deep(.p-select),
    :deep(input) {
      min-width: 0;
    }
  }
}
</style>
