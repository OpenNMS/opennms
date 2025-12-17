<template>
  <div
    class="event-config-container"
    v-if="store.selectedSource"
  >
    <div class="header">
      <div class="title-container">
        <div>
          <FeatherBackButton
            data-test="back-button"
            @click="router.push({ name: 'Event Configuration' })"
          >
            Go Back
          </FeatherBackButton>
        </div>
        <div>
          <h1>Event Configuration Details</h1>
        </div>
      </div>
      <div class="action-container">
        <FeatherButton
          primary
          data-test="add-event"
          @click="onAddEventClick(store.selectedSource)"
        >
          Add Event
        </FeatherButton>
        <FeatherButton
          primary
          @click="store.showChangeEventConfigSourceStatusDialog(store.selectedSource)"
          data-test="enable-disable-source"
        >
          {{ store.selectedSource.enabled ? 'Disable Source' : 'Enable Source' }}
        </FeatherButton>
        <FeatherButton
          primary
          @click="store.showDeleteEventConfigSourceDialog(store.selectedSource)"
          data-test="delete-source"
          v-if="store.selectedSource.vendor !== VENDOR_OPENNMS"
        >
          Delete Source
        </FeatherButton>
      </div>
    </div>

    <div
      class="config-details-box"
      data-test="config-box"
    >
      <div class="config-row">
        <div class="config-field">
          <span class="field-label">Name:</span>
          <span class="field-value">{{ store.selectedSource.name }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Uploaded By:</span>
          <span class="field-value">{{ store.selectedSource.uploadedBy }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Creation Date:</span>
          <span class="field-value">{{ store.selectedSource.createdTime && format(store.selectedSource.createdTime, 'MM/dd/yyyy') }}</span>
        </div>
      </div>
      <div class="config-row">
        <div class="config-field">
          <span class="field-label">Vendor:</span>
          <span class="field-value">{{ store.selectedSource.vendor }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Status:</span>
          <span class="field-value">{{ store.selectedSource.enabled ? 'Enabled' : 'Disabled' }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Last Modified Date:</span>
          <span class="field-value">{{ store.selectedSource.lastModified && format(store.selectedSource.lastModified, 'MM/dd/yyyy') }}</span>
        </div>
      </div>
      <div class="config-row">
        <div class="config-field">
          <span class="field-label">Event Count:</span>
          <span class="field-value">{{ store.selectedSource.eventCount }}</span>
        </div>
      </div>
    </div>
    <div class="event-table-section">
      <EventConfigEventTable />
    </div>
    <DeleteEventConfigSourceDialog />
    <ChangeEventConfigSourceStatusDialog />
  </div>
  <div
    v-else
    class="not-found-container"
  >
    <p>No event configuration found.</p>
    <FeatherButton
      primary
      @click="router.push({ name: 'Event Configuration' })"
    >
      Go Back
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import ChangeEventConfigSourceStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigSourceStatusDialog.vue'
import DeleteEventConfigSourceDialog from '@/components/EventConfigurationDetail/Dialog/DeleteEventConfigSourceDialog.vue'
import EventConfigEventTable from '@/components/EventConfigurationDetail/EventConfigEventTable.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'
import { getDefaultEventConfigEvent, useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigSource } from '@/types/eventConfig'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { format } from 'date-fns-tz'

const store = useEventConfigDetailStore()
const router = useRouter()
const route = useRoute()

const onAddEventClick = (source: EventConfigSource) => {
  const modificationStore = useEventModificationStore()
  modificationStore.setSelectedEventConfigSource(source, CreateEditMode.Create, getDefaultEventConfigEvent())
  router.push({
    name: 'Event Configuration Create'
  })
}

onMounted(async () => {
  if (route.params.id) {
    await store.fetchSourceById(route.params.id as string)
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
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    .title-container {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .action-container {
      display: flex;
      align-items: center;
      gap: 10px;

      button {
        margin: 0;
      }
    }
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
        flex: 1;
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

