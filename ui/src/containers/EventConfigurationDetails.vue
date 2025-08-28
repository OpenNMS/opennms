<template>
  <div
    class="event-config-container"
    v-if="config"
  >
    <div class="header">
      <h1>Event Configuration Details</h1>
    </div>

    <div class="config-details-box">
      <div class="config-row">
        <div class="config-field name-field">
          <span class="field-label">Name:</span>
          <span class="field-value">{{ config?.filename }}</span>
        </div>
        <div class="config-field description-field">
          <span class="field-label">Description:</span>
          <span class="field-value">{{ config?.description }}</span>
        </div>
      </div>
      <div class="config-row">
        <div class="config-field vendor-field">
          <span class="field-label">Vendor:</span>
          <span class="field-value">{{ config?.vendor }}</span>
        </div>
      </div>
      <div class="config-row vertical-fields">
        <div class="config-field">
          <span class="field-label">File Order:</span>
          <span class="field-value">{{ config?.fileOrder }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Event Count:</span>
          <span class="field-value">{{ config?.eventCount }}</span>
        </div>
      </div>
    </div>

    <div class="event-table-section">
      <EventConfigEventTable />
    </div>
  </div>
  <div
    v-else
    class="not-found-container"
  >
    <p>No event configuration found.</p>
    <FeatherButton primary @click="$router.go(-1)"> Go Back </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import EventConfigEventTable from '@/components/EventConfigurationDetail/EventConfigEventTable.vue'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { EventConfSourceMetadata } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'

const store = useEventConfigDetailStore()
const router = useRoute()
const config = ref<EventConfSourceMetadata>()

onMounted(async () => {
  if (Number(router.params.id) === store.selectedSource?.id) {
    config.value = store.selectedSource
    await store.fetchEventsBySourceId()
  }
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.event-config-container {
  margin: 0 auto;
  padding: 20px;

  .header {
    margin-bottom: 20px;
  }

  .config-details-box {
    border: 1px solid var($primary);
    border-radius: 4px;
    padding: 20px;
    background: white;
    margin-bottom: 30px;

    .config-row {
      display: flex;
      margin-bottom: 15px;

      .config-field {
        display: flex;
        align-items: center;
        margin-right: 40px;

        .field-label {
          font-weight: bold;
          margin-right: 10px;
          color: #555;
          min-width: 80px;
        }

        .field-value {
          color: #333;
        }
      }

      .vertical-fields {
        flex-direction: column;
        gap: 10px;
      }

      .name-field {
        min-width: 500px;
      }

      .description-field {
        min-width: 300px;
      }

      .vendor-field {
        min-width: 500px;
      }
    }
  }
}

.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 25px;

  p {
    @include headline3;
    margin: 0;
  }
}
</style>

