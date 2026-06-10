<template>
  <div class="trap-configuration-container">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h2>Trap Configuration</h2>
      </div>
    </div>
    <div class="tab-container">
      <FeatherTabContainer v-model="store.activeTab">
        <template v-slot:tabs>
          <FeatherTab>General Configuration</FeatherTab>
          <FeatherTab>SNMPv3 User Management</FeatherTab>
          <FeatherTab>Advanced</FeatherTab>
        </template>
        <FeatherTabPanel>
          <GeneralConfiguration />
        </FeatherTabPanel>
        <FeatherTabPanel>
          <SnmpV3UserManagement />
          <CreateSnmpV3User />
        </FeatherTabPanel>
        <FeatherTabPanel>
          <TrapdAdvancedConfiguration />
        </FeatherTabPanel>
      </FeatherTabContainer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import CreateSnmpV3User from '@/components/TrapdConfiguration/CreateSnmpV3User.vue'
import GeneralConfiguration from '@/components/TrapdConfiguration/GeneralConfiguration.vue'
import TrapdAdvancedConfiguration from '@/components/TrapdConfiguration/TrapdAdvancedConfiguration.vue'
import SnmpV3UserManagement from '@/components/TrapdConfiguration/SnmpV3UserManagement.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useMenuStore } from '@/stores/menuStore'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { BreadCrumb } from '@/types'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

const menuStore = useMenuStore()
const store = useTrapdConfigStore()
const { showSnackBar } = useSnackbar()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Trap Configuration', to: '#', position: 'last' }
]))

onMounted(async () => {
  try {
    await store.fetchTrapConfig()
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Failed to retrieve trapd configuration.'
    showSnackBar({ msg, error: true })
  }
})
</script>

<style lang="scss" scoped>
.trap-configuration-container {
  padding: 1.5em;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.25em;
    padding: 0;
  }

  .tab-container {
    padding: 0;
  }
}
</style>
