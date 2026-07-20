<template>
  <div
    class="snmp-data-collection-detail-container"
    v-if="store.selectedCollectionSource"
  >
    <div class="header">
      <div class="title-container">
        <div class="back">
          <PButton
            text
            class="back-button"
            data-test="back-button"
            @click="router.push({ name: 'SNMP Data Collection' })"
          >
            <FeatherIcon :icon="ArrowBack" />
            Go Back
          </PButton>
        </div>
        <div class="title">
          <h1>{{ isCreateMode ? 'Create New Source' : `Source Details for ${store.selectedCollectionSource.name}` }}</h1>
        </div>
        <div
          v-if="!isCreateMode"
          class="tag"
        >
          <PTag
            v-if="store.selectedCollectionSource.enabled"
            class="enabled-tag"
            value="Enabled"
            data-test="status-tag"
          />
          <PTag
            v-if="!store.selectedCollectionSource.enabled"
            class="disabled-tag"
            value="Disabled"
            data-test="status-tag"
          />
        </div>
      </div>
      <div
        v-if="!isCreateMode"
        class="action-container"
      >
        <PButton
          v-if="!store.selectedCollectionSource.enabled"
          outlined
          label="Enable Source"
          data-test="enable-source"
          @click="openChangeStatusDialog(store.selectedCollectionSource)"
        />
        <PButton
          v-if="store.selectedCollectionSource.enabled"
          outlined
          label="Disable Source"
          data-test="disable-source"
          @click="openChangeStatusDialog(store.selectedCollectionSource)"
        />
        <PButton
          v-if="!isPluginSourced(store.selectedCollectionSource)"
          outlined
          label="Delete Source"
          data-test="delete-source"
          @click="openDeleteCollectionSourceDialog(store.selectedCollectionSource)"
        />
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
            <div
              v-if="isCreateMode"
              class="source-name-input"
            >
              <InputText
                v-model="localSourceName"
                :invalid="!!sourceNameError"
                placeholder="Source Name"
                data-test="source-name-input"
                fluid
              />
              <small
                v-if="sourceNameError"
                class="field-error"
              >{{ sourceNameError }}</small>
            </div>
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
        <PTabs
          class="tabs"
          v-model:value="store.activeTab">
          <PTabList>
            <PTab :value="0">System Definitions</PTab>
            <PTab :value="1">MIB Groups</PTab>
            <PTab :value="2">Resource Types</PTab>
          </PTabList>
          <PTabPanels>
            <PTabPanel :value="0">
              <SystemDefinitionsTable />
            </PTabPanel>
            <PTabPanel :value="1">
              <MibGroupsTable />
            </PTabPanel>
            <PTabPanel :value="2">
              <ResourceTypesTable />
            </PTabPanel>
          </PTabPanels>
        </PTabs>
      </div>
      <div
        v-if="isCreateMode"
        class="create-action-row"
      >
        <PButton
          data-test="create-source-button"
          label="Create Source"
          @click="onSaveSource"
        />
      </div>
    </TableCard>
  </div>
  <div
    v-else
    class="not-found-container"
  >
    <p>No data found.</p>
    <PButton
      label="Go Back"
      @click="router.push({ name: 'SNMP Data Collection' })"
    />
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

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
import { FeatherIcon } from '@featherds/icon'
import ArrowBack from '@featherds/icon/navigation/ArrowBack'
import { format } from 'date-fns-tz'
import ButtonComponent from 'primevue/button'
import ChipComponent from 'primevue/chip'
import InputText from 'primevue/inputtext'
import TabComponent from 'primevue/tab'
import TabListComponent from 'primevue/tablist'
import TabPanelComponent from 'primevue/tabpanel'
import TabPanelsComponent from 'primevue/tabpanels'
import TabsComponent from 'primevue/tabs'
import TagComponent from 'primevue/tag'

const PChip = ChipComponent
const PButton = ButtonComponent
const PTag = TagComponent
const PTabs = TabsComponent
const PTabList = TabListComponent
const PTab = TabComponent
const PTabPanels = TabPanelsComponent
const PTabPanel = TabPanelComponent

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
      router.push({ name: 'SNMP Data Collection Source Detail', params: { id: newId }})
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

          :deep(.p-tag-label) {
            color: #0B720C !important;
          }
        }

        .disabled-tag {
          margin: 0 !important;
          border-radius: 4px;
          background-color: #7575751F;

          :deep(.p-tag-label) {
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
    border: 1px solid var(--p-content-border-color);

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
            color: var(--p-red-500);
          }

          .field-error {
            display: block;
            color: var(--p-red-500);
            font-size: 0.8em;
            margin-top: 0.25em;
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

      .tabs {
        :deep(.p-tab) {
          text-transform: uppercase;
        }
      }
    }

    .create-action-row {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid var(--p-content-border-color);
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
  border-color: var(--p-content-border-color) !important;
  color: var(--p-primary-color) !important;
}
</style>
