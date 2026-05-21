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
          <h1>{{ isCreateMode ? 'Create New Source' : `Source Details for ${store.selectedCollectionSource.name}` }}</h1>
        </div>
        <div
          v-if="!isCreateMode"
          class="tag"
        >
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
      <div
        v-if="!isCreateMode"
        class="action-container"
      >
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
            <FeatherInput
              v-if="isCreateMode"
              label="Source Name"
              v-model="localSourceName"
              :error="sourceNameError"
              data-test="source-name-input"
              class="source-name-input"
            />
            <span
              v-else
              class="field-value"
            >{{ store.selectedCollectionSource.name }}</span>
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
        <div class="config-row">
          <div class="config-field">
            <span class="field-label">Profiles:</span>
            <div class="profiles-field-content">
              <span class="field-value profiles-chips">
                <PChip
                  v-for="profile in drawerProfiles"
                  :key="profile.id"
                  :label="profile.name"
                />
                <span
                  v-if="drawerProfiles.length === 0"
                  class="no-profiles-text"
                >None</span>
              </span>
              <span
                v-if="isCreateMode && profilesError"
                class="profiles-error"
              >{{ profilesError }}</span>
            </div>
          </div>
          <div class="config-field">
            <PButton
              outlined
              label="Edit Profiles..."
              icon="pi pi-pen-to-square"
              iconPos="right"
              class="edit-profiles-btn"
              data-test="edit-profiles-button"
              @click="isProfilesDrawerVisible = true"
            />
          </div>
        </div>
      </div>
      <div
        v-if="!isCreateMode"
        class="tab-container"
      >
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
      <div
        v-if="isCreateMode"
        class="create-action-row"
      >
        <FeatherButton
          primary
          data-test="create-source-button"
          @click="onSaveSource"
        >
          Create Source
        </FeatherButton>
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
  <SnmpDataCollectionSourceProfilesDrawer
    :visible="isProfilesDrawerVisible"
    :source-name="store.selectedCollectionSource?.name ?? ''"
    :profiles="drawerProfiles"
    @close="isProfilesDrawerVisible = false"
    @saved="onProfilesSaved"
  />
</template>

<script setup lang="ts">
import TableCard from '@/components/Common/TableCard.vue'
import DeleteConfirmationDialog from '@/components/SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import SnmpDataCollectionChangeStatusDialog from '@/components/SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import MibGroupsTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/MibGroupsTable.vue'
import ResourceTypesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/ResourceTypesTable.vue'
import SystemDefinitionsTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/SystemDefinitionsTable.vue'
import SnmpDataCollectionSourceProfilesDrawer from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/Drawer/SnmpDataCollectionSourceProfilesDrawer.vue'
import useSnackbar from '@/composables/useSnackbar'
import { deleteSnmpCollectionSources, enableDisableSnmpDataCollectionSources, updateDataCollectionProfile } from '@/services/snmpDataCollectionService'
import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionProfile, SnmpCollectionSource } from '@/types/snmpDataCollection'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherInput } from '@featherds/input'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import { format } from 'date-fns-tz'
import ChipComponent from 'primevue/chip'
import ButtonComponent from 'primevue/button'

const PChip = ChipComponent
const PButton = ButtonComponent

const router = useRouter()
const route = useRoute()
const store = useSnmpDataCollectionDetailStore()
const sourcesStore = useSnmpDataCollectionStore()
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedCollectionSource = ref<{ id: number; name: string, enabled: boolean } | null>(null)
const snackbar = useSnackbar()
const isProfilesDrawerVisible = ref(false)

const mode = ref<CreateEditMode>(CreateEditMode.Edit)
const isCreateMode = computed(() => mode.value === CreateEditMode.Create)
const localSourceName = ref('')
const sourceNameError = ref('')
const profilesError = ref('')
const createModeProfiles = ref<SnmpCollectionProfile[]>([])

const drawerProfiles = computed(() =>
  isCreateMode.value
    ? createModeProfiles.value
    : sourcesStore.profilesForSource(store.selectedCollectionSource?.name ?? '')
)

const onSaveSource = async () => {
  sourceNameError.value = ''
  profilesError.value = ''

  let isValid = true

  if (!localSourceName.value.trim()) {
    sourceNameError.value = 'Source Name is required.'
    isValid = false
  } else if (sourcesStore.sources.some((s: SnmpCollectionSource) => s.name === localSourceName.value.trim())) {
    sourceNameError.value = 'A Source with this name already exists.'
    isValid = false
  }

  if (createModeProfiles.value.length === 0) {
    profilesError.value = 'At least 1 Profile is required.'
    isValid = false
  }

  if (isValid) {
    const profileNames = createModeProfiles.value.map((p: SnmpCollectionProfile) => p.name)
    const newId = await sourcesStore.createSnmpDataCollectionSource(localSourceName.value.trim(), profileNames)

    if (newId !== null) {
      snackbar.showSnackBar({ msg: `Source '${localSourceName.value.trim()}' created successfully.` })
      mode.value = CreateEditMode.Edit
      await Promise.all([
        store.fetchCollectionSourceById(String(newId)),
        sourcesStore.fetchAllSourcesNames(),
        sourcesStore.fetchSnmpCollectionProfiles()
      ])
      router.push({ name: 'SNMP Data Collection Source Detail', params: { id: newId } })
    } else {
      snackbar.showSnackBar({ msg: `Failed to create Source '${localSourceName.value.trim()}'.`, error: true })
    }
  }
}

const onProfilesSaved = async (newProfiles: SnmpCollectionProfile[]) => {
  isProfilesDrawerVisible.value = false

  if (isCreateMode.value) {
    createModeProfiles.value = newProfiles
    return
  }

  const sourceName = store.selectedCollectionSource?.name ?? ''
  const originalProfiles = sourcesStore.profilesForSource(sourceName)
  const originalIds = new Set(originalProfiles.map((p: SnmpCollectionProfile) => p.id))
  const newIds = new Set(newProfiles.map((p: SnmpCollectionProfile) => p.id))

  const toAdd = newProfiles.filter((p: SnmpCollectionProfile) => !originalIds.has(p.id))
  const toRemove = originalProfiles.filter((p: SnmpCollectionProfile) => !newIds.has(p.id))

  const results: boolean[] = []

  for (const profile of toAdd) {
    results.push(await updateDataCollectionProfile({ ...profile, sourceNames: [...profile.sourceNames, sourceName] }))
  }
  for (const profile of toRemove) {
    results.push(await updateDataCollectionProfile({ ...profile, sourceNames: profile.sourceNames.filter((n: string) => n !== sourceName) }))
  }

  await sourcesStore.fetchSnmpCollectionProfiles()

  if (results.length > 0 && !results.every(r => r)) {
    snackbar.showSnackBar({
      msg: 'Failed to update one or more profiles. The list has been refreshed to reflect the current server state.',
      error: true
    })
  }
}

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

const initPage = async (id: string | string[]) => {
  const idStr = Array.isArray(id) ? id[0] : id

  if (idStr === 'create') {
    mode.value = CreateEditMode.Create
    createModeProfiles.value = []
    store.selectedCollectionSource = {
      id: 0,
      name: '',
      vendor: '',
      description: '',
      enabled: true,
      createdTime: new Date(),
      lastModified: new Date(),
      uploadedBy: ''
    } as SnmpCollectionSource
  } else if (Number(idStr) > 0) {
    mode.value = CreateEditMode.Edit
  }

  if (idStr) {
    await store.fetchCollectionSourceById(idStr)
  }

  await sourcesStore.fetchSnmpCollectionProfiles()
}

onMounted(() => initPage(route.params.id))

watch(() => route.params.id, (id: string | string[]) => {
  if (id) {
    initPage(id)
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
            color: var(--feather-secondary-text-on-surface);
            min-width: 80px;
          }

          .field-value {
            @include body-large;
          }

          .profiles-chips {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;

            :deep(.p-chip-label) {
              color: var(--feather-primary-text-on-surface);
            }
          }

          .no-profiles-text {
            color: var(--feather-secondary-text-on-surface);
          }

          .profiles-field-content {
            display: flex;
            flex-direction: column;
            gap: 4px;
          }

          .profiles-error {
            @include body-small;
            color: var(--feather-error);
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

    .create-action-row {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid var(--feather-border-on-surface);
    }
  }

  .source-name-input {
    max-width: 20em;
    min-width: 16em;
    margin-top: 1.5em;
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

.edit-profiles-btn {
  text-transform: uppercase;
  letter-spacing: var(--feather-button-letter-spacing);
  border-color: var(--feather-border-on-surface) !important;
  color: var(--feather-primary) !important;
  font-size: var(--feather-button-font-size);
  font-weight: var(--feather-button-font-weight);

  :deep(.p-button-label) {
    font-weight: var(--feather-button-font-weight);
    font-size: var(--feather-button-font-size);
  }
}
</style>
