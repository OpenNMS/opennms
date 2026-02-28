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
    </div>
    <div class="tab-container">
      <FeatherTabContainer v-model="store.activeTab">
        <template v-slot:tabs>
          <FeatherTab>Data Collection Sources</FeatherTab>
          <FeatherTab>Import Data Collection Sources</FeatherTab>
        </template>
        <FeatherTabPanel>
          <SnmpDataCollectionSourcesTable />
        </FeatherTabPanel>
        <FeatherTabPanel>
          <SnmpDataCollectionSourceImport />
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>

<script lang="ts" setup>
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollection/SnmpDataCollectionSourceImport.vue'
import SnmpDataCollectionSourcesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourcesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { BreadCrumb } from '@/types'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

const menuStore = useMenuStore()
const store = useSnmpDataCollectionStore()
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
    padding: 60px 40px 25px 40px;
  }

  .tab-container {
    padding: 0px 40px 0px 40px;
  }
}
</style>

