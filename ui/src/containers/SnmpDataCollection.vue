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
        <FeatherButton
          primary
          @click="goToCreateSource"
        >
          Create New Data Collection Source
        </FeatherButton>
        <FeatherButton
          secondary
          @click="goToImportSource"
        >
          Import Data Collection Source
        </FeatherButton>
      </div>
    </div>
    <div class="container">
      <SnmpDataCollectionSourcesTable />
    </div>
  </div>
</template>

<script lang="ts" setup>
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SnmpDataCollectionSourcesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourcesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { FeatherButton } from '@featherds/button'

const menuStore = useMenuStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)
const router = useRouter()

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'SNMP Data Collection', to: '#', position: 'last' }
]))

const goToCreateSource = () => {
  // router.push({ name: 'Snmp Data Collection Create' })
  console.log('Create New Data Collection Source clicked')
}

const goToImportSource = () => {
  router.push({ name: 'SNMP Data Collection Import' })
}
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

