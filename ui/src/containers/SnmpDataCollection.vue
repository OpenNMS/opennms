<template>
  <div class="snmp-data-collection-container">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Manage SNMP Data Collection Sources</h1>
      </div>
      <div class="action">
        <FeatherButton primary> Create New Data Collection Source </FeatherButton>
        <FeatherButton secondary> Import Data Collection Source </FeatherButton>
      </div>
    </div>
    <div class="container">
        <SourcesTable />         
    </div>
  </div>
</template>

<script lang="ts" setup>
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SourcesTable from '@/components/SnmpDataCollection/SourcesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { FeatherButton } from '@featherds/button'

const menuStore = useMenuStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'SNMP Data Collection', to: '#', position: 'last' }
]))
</script>

<style lang="scss" scoped>
.snmp-data-collection-container {
    padding: 20px;

    .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }
}
</style>

