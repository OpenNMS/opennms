<template>
  <div class="snmp-config">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h2>Manage SNMP Configuration</h2>
      </div>
    </div>
    <div class="tabs">
      <SnmpConfigTabContainer />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SnmpConfigTabContainer from '@/components/SnmpConfiguration/SnmpConfigTabContainer.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useScvStore } from '@/stores/scvStore'
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { BreadCrumb } from '@/types'

const store = useSnmpConfigStore()
const menuStore = useMenuStore()
const scvStore = useScvStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage SNMP Configuration', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  store.resetState()
  store.fetchMonitoringLocations()
  store.populateSnmpConfig()
  scvStore.getAliases()
  scvStore.populate()
})
</script>

<style lang="scss" scoped>
.snmp-config {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
}
</style>
