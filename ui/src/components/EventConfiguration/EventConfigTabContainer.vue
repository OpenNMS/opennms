<template>
  <div class="event-config-tab-container">
    <OnmsTabs
      class="tabs"
      :value="store.activeTab"
      @update:value="onTabChange"
    >
      <OnmsTabList>
        <OnmsTab :value="0">View</OnmsTab>
        <OnmsTab :value="1">Upload Files</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">
          <EventConfigSourceTable />
        </OnmsTabPanel>
        <OnmsTabPanel :value="1">
          <EventConfigUploadFilesTab />
        </OnmsTabPanel>
      </OnmsTabPanels>
    </OnmsTabs>
  </div>
</template>

<script lang="ts" setup>
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
import EventConfigSourceTable from './EventConfigSourceTable.vue'
import EventConfigUploadFilesTab from './EventConfigUploadFilesTab.vue'

const store = useEventConfigStore()

const onTabChange = (value: string | number) => {
  store.activeTab = Number(value)
}
</script>
