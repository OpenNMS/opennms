<template>
  <div class="event-config-tab-container">
    <Tabs
      class="tabs"
      :value="store.activeTab"
      @update:value="onTabChange"
    >
      <TabList>
        <Tab :value="0">View</Tab>
        <Tab :value="1">Upload Files</Tab>
      </TabList>
      <TabPanels>
        <TabPanel :value="0">
          <EventConfigSourceTable />
        </TabPanel>
        <TabPanel :value="1">
          <EventConfigUploadFilesTab />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

<script lang="ts" setup>
import { useEventConfigStore } from '@/stores/eventConfigStore'
import Tab from 'primevue/tab'
import TabList from 'primevue/tablist'
import TabPanel from 'primevue/tabpanel'
import TabPanels from 'primevue/tabpanels'
import Tabs from 'primevue/tabs'
import EventConfigSourceTable from './EventConfigSourceTable.vue'
import EventConfigUploadFilesTab from './EventConfigUploadFilesTab.vue'

const store = useEventConfigStore()

const onTabChange = (value: string | number) => {
  store.activeTab = Number(value)
}
</script>

<style lang="scss" scoped>
.event-config-tab-container {
  .tabs {
    :deep(.p-tab) {
      text-transform: uppercase;
    }
  }
}
</style>
