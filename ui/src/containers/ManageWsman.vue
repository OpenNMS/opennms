<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-wsman-container">
    <h1 class="page-title">Manage WS-Man</h1>
    <WsmanHelpPanel />

    <p v-if="store.loadError" class="error" data-test="load-error">
      Failed to load the WS-Man configuration. Check that <code>wsman-config.xml</code> is readable, then reload the page.
    </p>

    <OnmsTabs v-else-if="store.config" v-model:value="activeTab">
      <OnmsTabList>
        <OnmsTab :value="0" data-test="tab-defaults">Agent Defaults</OnmsTab>
        <OnmsTab :value="1" data-test="tab-definitions">Definitions ({{ store.config.definitions.length }})</OnmsTab>
        <OnmsTab :value="2" data-test="tab-data-collection">Data Collection</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <WsmanDefaultsCard :settings="store.config.defaults" />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <WsmanDefinitionsTable :definitions="store.config.definitions" />
        </OnmsTabPanel>
        <OnmsTabPanel :value="2">
          <p class="placeholder" data-test="data-collection-placeholder">
            WS-Man data collection is configured in <code>wsman-datacollection-config.xml</code> and the
            <code>wsman-datacollection.d/</code> directory. Managing collections and system definitions
            from this page is not available yet.
          </p>
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import WsmanDefaultsCard from '@/components/ManageWsman/WsmanDefaultsCard.vue'
import WsmanDefinitionsTable from '@/components/ManageWsman/WsmanDefinitionsTable.vue'
import WsmanHelpPanel from '@/components/ManageWsman/WsmanHelpPanel.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useWsmanAdminStore()
const activeTab = ref(0)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Manage WS-Man', to: '#', position: 'last' }
])

onMounted(async () => {
  await store.getConfig()
})
</script>

<style lang="scss" scoped>
.manage-wsman-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.error {
  color: var(--p-red-500, #c62828);
  margin: 0;
}

.placeholder {
  margin: 0;
  font-size: 0.95rem;
  color: var(--p-text-muted-color);
}
</style>
