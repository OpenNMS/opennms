<template>
  <div class="event-config-tab-container">
    <FeatherTabContainer class="tabs" v-model="store.activeTab">
      <template v-slot:tabs>
        <FeatherTab>View</FeatherTab>
        <FeatherTab>Upload Files</FeatherTab>
      </template>
      <FeatherTabPanel>
        <EventConfigurationTable />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <UploadFilesTab />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import EventConfigurationTable from './EventConfigurationTable.vue'
import UploadFilesTab from './UploadFilesTab.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'

const store = useEventConfigStore()
</script>

<style lang="scss" scoped></style>

