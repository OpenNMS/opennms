<template>
  <div class="snmp-data-collection-container">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Manage SNMP Data Collection Sources</h1>
      </div>
      <!-- Page-level export of the assembled <datacollection-config>. The
           per-source export lives in the table row dropdown — paired with
           this, an operator can grab the full pre-edit state on disk and
           push it back via the Import tab. -->
      <div class="header-actions">
        <OnmsButton
          variant="outlined"
          aria-haspopup="true"
          aria-controls="download-config-menu"
          data-test="download-config-button"
          @click="toggleDownloadMenu"
        >
          <OnmsIcon :icon="DownloadIcon" /> Download Data Collection Config
        </OnmsButton>
        <OnmsMenu
          id="download-config-menu"
          ref="downloadMenu"
          :items="downloadMenuItems"
        />
      </div>
    </div>
    <div class="tab-container">
      <OnmsTabs
        class="tabs"
        :value="store.activeTab"
        @update:value="onTabChange"
      >
        <OnmsTabList>
          <OnmsTab :value="0">Data Collection Sources</OnmsTab>
          <OnmsTab :value="1">Import Data Collection Sources</OnmsTab>
          <OnmsTab :value="2">Profiles</OnmsTab>
        </OnmsTabList>
        <OnmsTabPanels>
          <OnmsTabPanel :value="0">
            <SnmpDataCollectionSourcesTable />
          </OnmsTabPanel>
          <OnmsTabPanel :value="1">
            <SnmpDataCollectionSourceImport />
          </OnmsTabPanel>
          <OnmsTabPanel :value="2">
            <SnmpDataCollectionProfilesTable />
          </OnmsTabPanel>
        </OnmsTabPanels>
      </OnmsTabs>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SnmpDataCollectionProfilesTable from '@/components/SnmpDataCollection/SnmpDataCollectionProfilesTable.vue'
import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollection/SnmpDataCollectionSourceImport.vue'
import SnmpDataCollectionSourcesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourcesTable.vue'
import useSnackbar from '@/composables/useSnackbar'
import { downloadDatacollectionConfig } from '@/services/snmpDataCollectionService'
import { useMenuStore } from '@/stores/menuStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { BreadCrumb } from '@/types'
import { OnmsButton, OnmsIcon, OnmsMenu, OnmsMenuItem, OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
import DownloadIcon from '@/components/icons/action/DownloadFile.vue'

const menuStore = useMenuStore()
const store = useSnmpDataCollectionStore()
const snackbar = useSnackbar()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'SNMP Data Collection', to: '#', position: 'last' }
]))

const onTabChange = (value: string | number) => {
  store.activeTab = Number(value)
}

const downloadMenu = ref()
const downloadMenuItems = computed<OnmsMenuItem[]>(() => ([
  { label: 'Download XML', command: () => downloadConfig('xml') },
  { label: 'Download JSON', command: () => downloadConfig('json') }
]))

const toggleDownloadMenu = (event: Event) => {
  downloadMenu.value?.toggle(event)
}

const downloadConfig = async (format: 'xml' | 'json') => {
  try {
    const blob = await downloadDatacollectionConfig(format)
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `datacollection-config.${format}`
    link.click()
    window.URL.revokeObjectURL(link.href)
  } catch (_e) {
    snackbar.showSnackBar({
      msg: `Failed to download datacollection-config (${format}).`,
      error: true
    })
  }
}
</script>

<style lang="scss" scoped>
.snmp-data-collection-container {
  padding: 1.5rem;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.25rem;
    padding: 0.5rem 0;

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .tab-container {
    padding: 0.5rem;
  }
}
</style>
