<template>
  <div class="trap-configuration-container">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>TrapD Configuration</h1>
      </div>
    </div>
    <div class="tab-container">
      <FeatherTabContainer v-model="store.activeTab">
        <template v-slot:tabs>
          <FeatherTab>General Configuration</FeatherTab>
          <FeatherTab>SNMPv3 User Management</FeatherTab>
        </template>
        <FeatherTabPanel>
            <GeneralConfiguration />
        </FeatherTabPanel>
        <FeatherTabPanel>
          <SnmpV3UserManagement />
          <CreateSnmpV3User />
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>

<script setup lang="ts">
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import CreateSnmpV3User from '@/components/TrapConfiguration/CreateSnmpV3User.vue'
import GeneralConfiguration from '@/components/TrapConfiguration/GeneralConfiguration.vue'
import SnmpV3UserManagement from '@/components/TrapConfiguration/SnmpV3UserManagement.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useTrapConfigStore } from '@/stores/trapConfigStore'
import { BreadCrumb } from '@/types'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

const menuStore = useMenuStore()
const store = useTrapConfigStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Trap Configurations', to: '#', position: 'last' }
]))
</script>

<style lang="scss" scoped>
.trap-configuration-container {
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

