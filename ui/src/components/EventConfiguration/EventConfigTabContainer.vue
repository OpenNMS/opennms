<template>
  <div class="event-config-tab-container">
    <FeatherTabContainer class="tabs">
      <template v-slot:tabs>
        <FeatherTab>View</FeatherTab>
        <FeatherTab>Event Configuration Details</FeatherTab>
      </template>
      <FeatherTabPanel>
        <EventConfigurationTable />
      </FeatherTabPanel>
      <FeatherTabPanel>
        <p>This page is under construction.</p>
        <p>For now, you can configure events using the OpenNMS web interface.</p>
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script lang="ts" setup>
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import EventConfigurationTable from './EventConfigurationTable.vue'
</script>

<style lang="scss" scoped></style>

