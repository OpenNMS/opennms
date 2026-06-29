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
        <Button
          outlined
          aria-haspopup="true"
          aria-controls="download-config-menu"
          data-test="download-config-button"
          @click="toggleDownloadMenu"
        >
          <FeatherIcon :icon="DownloadIcon" /> Download Data Collection Config
        </Button>
        <Menu
          id="download-config-menu"
          ref="downloadMenu"
          :model="downloadMenuItems"
          popup
        />
      </div>
    </div>
    <div class="tab-container">
      <Tabs
        class="tabs"
        :value="store.activeTab"
        @update:value="onTabChange"
      >
        <TabList>
          <Tab :value="0">Data Collection Sources</Tab>
          <Tab :value="1">Import Data Collection Sources</Tab>
          <Tab :value="2">Profiles</Tab>
        </TabList>
        <TabPanels>
          <TabPanel :value="0">
            <SnmpDataCollectionSourcesTable />
          </TabPanel>
          <TabPanel :value="1">
            <SnmpDataCollectionSourceImport />
          </TabPanel>
          <TabPanel :value="2">
            <SnmpDataCollectionProfilesTable />
          </TabPanel>
        </TabPanels>
      </Tabs>
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
import { FeatherIcon } from '@featherds/icon'
import DownloadIcon from '@featherds/icon/action/DownloadFile'
import Button from 'primevue/button'
import type { MenuItem } from 'primevue/menuitem'
import Menu from 'primevue/menu'
import Tab from 'primevue/tab'
import TabList from 'primevue/tablist'
import TabPanel from 'primevue/tabpanel'
import TabPanels from 'primevue/tabpanels'
import Tabs from 'primevue/tabs'

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
const downloadMenuItems = computed<MenuItem[]>(() => ([
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
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 60px 40px 25px 40px;

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .tab-container {
    padding: 0px 40px 0px 40px;

    .tabs {
      :deep(.p-tab) {
        text-transform: uppercase;
      }
    }
  }
}
</style>
