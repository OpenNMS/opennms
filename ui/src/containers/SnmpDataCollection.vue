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
      <!-- Page-level export of the assembled <datacollection-config>. The
           per-source export lives in the table row dropdown — paired with
           this, an operator can grab the full pre-edit state on disk and
           push it back via the Import tab. -->
      <div class="header-actions">
        <FeatherDropdown>
          <template v-slot:trigger="{ attrs, on }">
            <FeatherButton
              secondary
              v-bind="attrs"
              v-on="on"
              data-test="download-config-button"
            >
              <FeatherIcon :icon="DownloadIcon" /> Download Data Collection Config
            </FeatherButton>
          </template>
          <FeatherDropdownItem
            data-test="download-config-xml"
            @click="downloadConfig('xml')"
          >
            Download XML
          </FeatherDropdownItem>
          <FeatherDropdownItem
            data-test="download-config-json"
            @click="downloadConfig('json')"
          >
            Download JSON
          </FeatherDropdownItem>
        </FeatherDropdown>
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
import useSnackbar from '@/composables/useSnackbar'
import { downloadDatacollectionConfig } from '@/services/snmpDataCollectionService'
import { useMenuStore } from '@/stores/menuStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { BreadCrumb } from '@/types'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import DownloadIcon from '@featherds/icon/action/DownloadFile'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

const menuStore = useMenuStore()
const store = useSnmpDataCollectionStore()
const snackbar = useSnackbar()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'SNMP Data Collection', to: '#', position: 'last' }
]))

const downloadConfig = async (format: 'xml' | 'json') => {
  try {
    const blob = await downloadDatacollectionConfig(format)
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `datacollection-config.${format}`
    link.click()
    window.URL.revokeObjectURL(link.href)
  } catch (e) {
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
  }
}
</style>

