<template>
  <div class="event-config-tab-container">
    <FeatherTabContainer class="tabs" v-model="store.activeTab">
      <template v-slot:tabs>
        <FeatherTab>View</FeatherTab>
        <FeatherTab>Upload Files</FeatherTab>
      </template>
      <FeatherTabPanel>
        <EventConfigSourceTable />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <EventConfigUploadFilesTab />
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import EventConfigSourceTable from './EventConfigSourceTable.vue'
import EventConfigUploadFilesTab from './EventConfigUploadFilesTab.vue'

const store = useEventConfigStore()
</script>

<style lang="scss" scoped></style>

