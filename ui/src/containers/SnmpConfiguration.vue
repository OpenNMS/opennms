<template>
  <div class="snmp-config">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Manage SNMP Configuration</h1>
      </div>
    </div>
    <div class="tabs">
      <SnmpConfigTabContainer />
    </div>
  </div>
</template>

<script lang="ts" setup>
import SnmpConfigTabContainer from '@/components/SnmpConfiguration/SnmpConfigTabContainer.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const store = useSnmpConfigStore()
const menuStore = useMenuStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage SNMP Configuration', to: '#', position: 'last' }
  ]
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
