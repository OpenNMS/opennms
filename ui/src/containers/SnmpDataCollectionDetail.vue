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
        <FeatherButton
          primary
          data-test="enable-disable-source"
        >
          {{ store.selectedCollectionSource.enabled ? 'Disable Source' : 'Enable Source' }}
        </FeatherButton>
        <FeatherButton
          primary
          data-test="delete-source"
          @click="openDeleteCollectionSourceDialog(store.selectedCollectionSource)"
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
    </div>
    <Transition name="fade">
      <div v-if="!store.mibGroupDrawerState.visible && !store.resourceTypeDrawerState.visible">
        <SystemDefinitionsTable />
      </div>
    </Transition>
    <Transition name="fade">
      <div v-if="!store.mibGroupDrawerState.visible && !store.resourceTypeDrawerState.visible">
        <MibGroupsTable />
      </div>
    </Transition>
    <Transition name="fade">
      <div v-if="!store.mibGroupDrawerState.visible && !store.resourceTypeDrawerState.visible">
        <ResourceTypesTable />
      </div>
    </Transition>
    <Transition name="fade">
      <div v-if="store.mibGroupDrawerState.visible">
        <MibGroupForm />
      </div>
    </Transition>
    <Transition name="fade">
      <div v-if="store.resourceTypeDrawerState.visible">
        <ResourceTypeForm />
      </div>
    </Transition>
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
  <DeleteConfirmationDialog
    :visible="isDeleteDialogVisible"
    :selected="selectedCollectionSource"
    :type="'source'"
    @close="closeDeleteCollectionSourceDialog"
    @confirm="deleteCollectionSource"
  />
</template>

<script setup lang="ts">
import DeleteConfirmationDialog from '@/components/SnmpDataCollectionDetail/Dialog/DeleteConfirmationDialog.vue'
import MibGroupForm from '@/components/SnmpDataCollectionDetail/MibGroupForm.vue'
import MibGroupsTable from '@/components/SnmpDataCollectionDetail/MibGroupsTable.vue'
import ResourceTypeForm from '@/components/SnmpDataCollectionDetail/ResourceTypeForm.vue'
import ResourceTypesTable from '@/components/SnmpDataCollectionDetail/ResourceTypesTable.vue'
import SystemDefinitionsTable from '@/components/SnmpDataCollectionDetail/SystemDefinitionsTable.vue'
import useSnackbar from '@/composables/useSnackbar'
import { deleteSnmpCollectionSources } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { format } from 'date-fns-tz'

const router = useRouter()
const route = useRoute()
const store = useSnmpDataCollectionDetailStore()
const isDeleteDialogVisible = ref(false)
const selectedCollectionSource = ref<{ id: number; name: string } | null>(null)
const snackbar = useSnackbar()

const openDeleteCollectionSourceDialog = (collectionSource: { id: number; name: string } | null) => {
  selectedCollectionSource.value = collectionSource
  isDeleteDialogVisible.value = true
}

const closeDeleteCollectionSourceDialog = () => {
  selectedCollectionSource.value = null
  isDeleteDialogVisible.value = false
}

const deleteCollectionSource = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'source' &&
    selected?.id &&
    selected?.id === selectedCollectionSource.value?.id &&
    selected?.name === selectedCollectionSource.value?.name &&
    store.selectedCollectionSource?.id === selectedCollectionSource.value?.id
  ) {
    const success = await deleteSnmpCollectionSources([selectedCollectionSource.value?.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `Collection Source '${selectedCollectionSource.value?.name}' deleted successfully.`
      })
      router.push({ name: 'SNMP Data Collection' })
    } else {
      snackbar.showSnackBar({
        msg: `Failed to delete Collection Source '${selectedCollectionSource.value?.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to delete Collection Source '${selected?.name}'.`,
      error: true
    })
  }
}

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

