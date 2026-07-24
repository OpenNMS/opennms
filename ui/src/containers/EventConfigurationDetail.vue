<template>
  <div
    class="event-config-container"
    v-if="store.selectedSource"
  >
    <div class="header">
      <div class="title-container">
        <div>
          <OnmsButton
            variant="text"
            data-test="back-button"
            @click="router.push({ name: 'Event Configuration' })"
          >
            <OnmsIcon :icon="ArrowBack" />
            Go Back
          </OnmsButton>
        </div>
        <div>
          <h1>Manage Event Config for a Source</h1>
        </div>
      </div>
      <div class="action-container">
        <OnmsButton
          label="Add Event Config"
          data-test="add-event-config"
          @click="onAddEventClick(store.selectedSource)"
        />
        <OnmsButton
          :label="store.selectedSource.enabled ? 'Disable Source' : 'Enable Source'"
          @click="store.showChangeEventConfigSourceStatusDialog(store.selectedSource)"
          data-test="enable-disable-source"
        />
        <OnmsButton
          label="Delete Source"
          @click="store.showDeleteEventConfigSourceDialog(store.selectedSource)"
          data-test="delete-source"
          v-if="store.selectedSource.vendor !== VENDOR_OPENNMS"
        />
      </div>
    </div>

    <div
      class="config-details-box"
      data-test="config-box"
    >
      <div class="config-row">
        <div class="config-field">
          <span class="field-label">Source:</span>
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
    <OnmsButton
      label="Go Back"
      @click="router.push({ name: 'Event Configuration' })"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import ChangeEventConfigSourceStatusDialog from '@/components/EventConfigurationDetail/Dialog/ChangeEventConfigSourceStatusDialog.vue'
import DeleteEventConfigSourceDialog from '@/components/EventConfigurationDetail/Dialog/DeleteEventConfigSourceDialog.vue'
import EventConfigEventTable from '@/components/EventConfigurationDetail/EventConfigEventTable.vue'
import { VENDOR_OPENNMS } from '@/lib/utils'
import { getDefaultEventConfigEvent, useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigSource } from '@/types/eventConfig'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import ArrowBack from '@/components/icons/navigation/ArrowBack.vue'
import { OnmsButton } from '@opennms/onms-ui'
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
    store.refreshEventConfigEvents()
    if (store.selectedSource) {
      await store.fetchEventsBySourceId()
    }
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/onms-typography' as *;

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
    border: 1px solid var(--p-primary-color);
    border-radius: 4px;
    padding: 20px;
    background: var(--p-content-background);
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
          color: var(--p-text-muted-color);
          min-width: 80px;
        }

        .field-value {
          color: var(--p-text-muted-color);
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
    @include onms-headline3;
    margin: 0;
  }
}
</style>
