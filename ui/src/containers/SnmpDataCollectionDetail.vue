<template>
  <div
    class="snmp-data-collection-detail-container"
    v-if="store.selectedCollectionSource"
  >
    <div class="header">
      <div class="title-container">
        <div>
          <FeatherBackButton
            data-test="back-button"
            @click="router.push({ name: 'SNMP Data Collection' })"
          >
            Go Back
          </FeatherBackButton>
        </div>
        <div>
          <h1>Data Collection Source Details</h1>
        </div>
      </div>
      <div class="action-container">
        <!-- <FeatherButton
          primary
          data-test="add-event-config"
        >
          Add Event Config
        </FeatherButton> -->
        <FeatherButton
          primary
          data-test="enable-disable-source"
        >
          {{ store.selectedCollectionSource.enabled ? 'Disable Source' : 'Enable Source' }}
        </FeatherButton>
        <FeatherButton
          primary
          data-test="delete-source"
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
          <span class="field-label">Source:</span>
          <span class="field-value">{{ store.selectedCollectionSource.name }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Uploaded By:</span>
          <span class="field-value">{{ store.selectedCollectionSource.uploadedBy }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Creation Date:</span>
          <span class="field-value">{{ store.selectedCollectionSource.createdTime &&
            format(store.selectedCollectionSource.createdTime, 'MM/dd/yyyy') }}</span>
        </div>
      </div>
      <div class="config-row">
        <div class="config-field">
          <span class="field-label">Vendor:</span>
          <span class="field-value">{{ store.selectedCollectionSource.vendor }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Status:</span>
          <span class="field-value">{{ store.selectedCollectionSource.enabled ? 'Enabled' : 'Disabled' }}</span>
        </div>
        <div class="config-field">
          <span class="field-label">Last Modified Date:</span>
          <span class="field-value">{{ store.selectedCollectionSource.lastModified &&
            format(store.selectedCollectionSource.lastModified, 'MM/dd/yyyy') }}</span>
        </div>
      </div>
      <div class="config-row">
        <!-- <div class="config-field">
          <span class="field-label">Event Count:</span>
          <span class="field-value">{{ store.selectedCollectionSource.eventCount }}</span>
        </div> -->
      </div>
    </div>
    <div class="system-defs-container">
      <SystemDefinitionsTable />
    </div>
    <div class="resource-types-container">
      <ResourceTypesTable />
    </div>
    <div class="mib-groups-container">
      <MibGroupsTable />
    </div>
  </div>
  <div
    v-else
    class="not-found-container"
  >
    <p>No data found.</p>
    <FeatherButton
      primary
      @click="router.push({ name: 'SNMP Data Collection' })"
    >
      Go Back
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import MibGroupsTable from '@/components/SnmpDataCollectionDetail/MibGroupsTable.vue'
import ResourceTypesTable from '@/components/SnmpDataCollectionDetail/ResourceTypesTable.vue'
import SystemDefinitionsTable from '@/components/SnmpDataCollectionDetail/SystemDefinitionsTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { format } from 'date-fns-tz'

const router = useRouter()
const route = useRoute()
const store = useSnmpDataCollectionDetailStore()

onMounted(async () => {
  if (route.params.id) {
    await store.fetchCollectionSourceById(route.params.id as string)
  }
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.snmp-data-collection-detail-container {
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

