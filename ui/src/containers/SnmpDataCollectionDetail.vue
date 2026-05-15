<template>
  <div
    class="snmp-data-collection-detail-container"
    v-if="store.selectedCollectionSource"
  >
    <div class="header">
      <div class="title-container">
        <div class="back">
          <FeatherBackButton
            data-test="back-button"
            @click="router.push({ name: 'SNMP Data Collection' })"
          >
            Go Back
          </FeatherBackButton>
        </div>
        <div class="title">
          <h1>{{ capitalize(store.selectedCollectionSource.name) }} Source Details</h1>
        </div>
        <div class="tag">
          <FeatherChip
            v-if="store.selectedCollectionSource.enabled"
            class="enabled-tag"
            data-test="status-tag"
          >
            Enabled
          </FeatherChip>
          <FeatherChip
            v-if="!store.selectedCollectionSource.enabled"
            class="disabled-tag"
            data-test="status-tag"
          >
            Disabled
          </FeatherChip>
        </div>
      </div>
      <div class="action-container">
        <FeatherButton
          v-if="!store.selectedCollectionSource.enabled"
          secondary
          data-test="enable-source"
          @click="openChangeStatusDialog(store.selectedCollectionSource)"
        >
          Enable Source
        </FeatherButton>
        <FeatherButton
          v-if="store.selectedCollectionSource.enabled"
          secondary
          data-test="disable-source"
          @click="openChangeStatusDialog(store.selectedCollectionSource)"
        >
          Disable Source
        </FeatherButton>
        <FeatherButton
          v-if="!isPluginSourced(store.selectedCollectionSource)"
          secondary
          data-test="delete-source"
          @click="openDeleteCollectionSourceDialog(store.selectedCollectionSource)"
        >
          Delete Source
        </FeatherButton>
      </div>
    </div>
    <TableCard class="content">
      <div
        class="config-details-box"
        data-test="config-box"
      >
        <div class="header">Source Details</div>
        <div class="config-row">
          <div class="config-field">
            <span class="field-label">Source:</span>
            <span class="field-value">{{ store.selectedCollectionSource.name }}</span>
          </div>
          <div class="config-field">
            <span class="field-label">Uploaded By:</span>
            <span class="field-value">{{ store.selectedCollectionSource.uploadedBy }}</span>
          </div>
        </div>
        <div class="config-row">
          <div class="config-field">
            <span class="field-label">Creation Date:</span>
            <span class="field-value">{{ store.selectedCollectionSource.createdTime &&
              format(store.selectedCollectionSource.createdTime, 'MM/dd/yyyy') }}</span>
          </div>
          <div class="config-field">
            <span class="field-label">Last Modified Date:</span>
            <span class="field-value">{{ store.selectedCollectionSource.lastModified &&
              format(store.selectedCollectionSource.lastModified, 'MM/dd/yyyy') }}</span>
          </div>
        </div>
      </div>
      <div class="tab-container">
        <FeatherTabContainer v-model="store.activeTab">
          <template v-slot:tabs>
            <FeatherTab>System Definitions</FeatherTab>
            <FeatherTab>MIB Groups</FeatherTab>
            <FeatherTab>Resource Types</FeatherTab>
          </template>
          <FeatherTabPanel>
            <SystemDefinitionsTable />
          </FeatherTabPanel>
          <FeatherTabPanel>
            <MibGroupsTable />
          </FeatherTabPanel>
          <FeatherTabPanel>
            <ResourceTypesTable />
          </FeatherTabPanel>
        </FeatherTabContainer>
      </div>
    </TableCard>
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
  <SnmpDataCollectionChangeStatusDialog
    :visible="isChangeStatusDialogVisible"
    :selected="selectedCollectionSource"
    type="source"
    :status="selectedCollectionSource?.enabled ? 'Disable' : 'Enable'"
    @close="closeChangeStatusDialog"
    @confirm="changeCollectionSourceStatus"
  />
</template>

<script setup lang="ts">
import TableCard from '@/components/Common/TableCard.vue'
import DeleteConfirmationDialog from '@/components/SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import SnmpDataCollectionChangeStatusDialog from '@/components/SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import MibGroupsTable from '@/components/SnmpDataCollectionDetail/MibGroupsTable.vue'
import ResourceTypesTable from '@/components/SnmpDataCollectionDetail/ResourceTypesTable.vue'
import SystemDefinitionsTable from '@/components/SnmpDataCollectionDetail/SystemDefinitionsTable.vue'
import useSnackbar from '@/composables/useSnackbar'
import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { deleteSnmpCollectionSources, enableDisableSnmpDataCollectionSources } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import { format } from 'date-fns-tz'
import { capitalize } from 'lodash'

const router = useRouter()
const route = useRoute()
const store = useSnmpDataCollectionDetailStore()
const sourcesStore = useSnmpDataCollectionStore()
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedCollectionSource = ref<{ id: number; name: string, enabled: boolean } | null>(null)
const snackbar = useSnackbar()

const openDeleteCollectionSourceDialog = (collectionSource: { id: number; name: string, enabled: boolean } | null) => {
  selectedCollectionSource.value = collectionSource
  isDeleteDialogVisible.value = true
}

const openChangeStatusDialog = (collectionSource: { id: number; name: string, enabled: boolean } | null) => {
  selectedCollectionSource.value = collectionSource
  isChangeStatusDialogVisible.value = true
}

const closeDeleteCollectionSourceDialog = () => {
  selectedCollectionSource.value = null
  isDeleteDialogVisible.value = false
}

const closeChangeStatusDialog = () => {
  selectedCollectionSource.value = null
  isChangeStatusDialogVisible.value = false
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
      // Refresh the all-source-names cache used by the Import tab's
      // duplicate detection — otherwise this just-deleted source still
      // appears as a "will update" row when re-uploaded.
      await sourcesStore.fetchAllSourcesNames()
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

const changeCollectionSourceStatus = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'source' &&
    selected?.id &&
    selected?.id === selectedCollectionSource.value?.id &&
    selected?.name === selectedCollectionSource.value?.name
  ) {
    const updatedStatus = !selectedCollectionSource.value?.enabled
    const success = await enableDisableSnmpDataCollectionSources(updatedStatus, [selectedCollectionSource.value?.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `Collection Source '${selectedCollectionSource.value?.name}' ${updatedStatus ? 'enabled' : 'disabled'} successfully.`
      })
      await store.fetchCollectionSourceById(String(selectedCollectionSource.value?.id))
      selectedCollectionSource.value = null
      isChangeStatusDialogVisible.value = false
    } else {
      snackbar.showSnackBar({
        msg: `Failed to ${updatedStatus ? 'enable' : 'disable'} Collection Source '${selectedCollectionSource.value?.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to change status for Collection Source '${selected?.name}'.`,
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
  padding: 45px;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 20px 0px;

    .title-container {
      display: flex;
      align-items: center;
      gap: 20px;

      .title {
        h1 {
          @include headline1;
          margin: 0;
        }
      }

      .tag {
        .enabled-tag {
          margin: 0 !important;
          border-radius: 4px;
          background-color: #0B720C1F;

          :deep(span) {
            color: #0B720C !important;
          }
        }

        .disabled-tag {
          margin: 0 !important;
          border-radius: 4px;
          background-color: #7575751F;

          :deep(span) {
            color: #757575 !important;
          }
        }
      }
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

  .content {
    margin-top: 10px;
    padding: 25px;
    border: 1px solid var(--feather-border-on-surface);

    .config-details-box {
      .header {
        @include headline3;
        margin-bottom: 20px;
      }

      .config-row {
        display: flex;

        .config-field {
          display: flex;
          align-items: center;
          flex: 1;
          margin-right: 40px;

          .field-label {
            @include headline4;
            margin-right: 10px;
            color: #1E1E1E;
            min-width: 80px;
          }

          .field-value {
            @include body-large;
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

    .tab-container {
      margin-top: 25px;
      padding: 10px;
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

