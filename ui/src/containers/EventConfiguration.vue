<template>
  <div class="event-config">
    <div class="header">
      <div class="heading">
        <h1>Event Configuration</h1>
      </div>
      <div class="action">
        <FeatherButton
          primary
          @click="store.showCreateEventConfigSourceDialog"
        >
          Create New Event Configuration
        </FeatherButton>
      </div>
    </div>
    <div class="tabs">
      <EventConfigTabContainer />
    </div>
  </div>
  <CreateEventConfigurationDialog />
</template>

<script lang="ts" setup>
import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'

const store = useEventConfigStore()
</script>

<style lang="scss" scoped>
.event-config {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
}
</style>

