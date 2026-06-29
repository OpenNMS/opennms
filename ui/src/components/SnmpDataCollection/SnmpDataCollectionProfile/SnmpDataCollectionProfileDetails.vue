<template>
  <template v-if="store.selectedProfile">
  <div
    class="snmp-data-collection-profile-details"
  >
    <div class="header">
      <div class="title-container">
        <div class="back">
          <PButton
            text
            class="back-button"
            data-test="back-button"
            @click="goBack"
          >
            <FeatherIcon :icon="ArrowBack" />
            Go Back
          </PButton>
        </div>
        <div class="title">
          <h1>{{ isCreateMode ? 'Create New Profile' : `Profile details for: ${store.selectedProfile.name}` }}</h1>
        </div>
        <div class="tag">
          <PTag
            v-if="store.selectedProfile.enabled"
            class="enabled-tag"
            value="Enabled"
            data-test="status-tag"
          />
          <PTag
            v-if="!store.selectedProfile.enabled"
            class="disabled-tag"
            value="Disabled"
            data-test="status-tag"
          />
        </div>
      </div>
    </div>
    <TableCard class="content">
      <PTabs v-model:value="activeTab" class="tabs">
        <PTabList>
          <PTab :value="0">Profile Details</PTab>
          <PTab :value="1">Sources</PTab>
          <PTab :value="2">RRD Settings</PTab>
        </PTabList>
        <PTabPanels>
          <PTabPanel :value="0">
            <ProfileDetailsTab
              :configDetails="configDetailsModel"
              @update:configDetails="onConfigDetailsUpdate"
              :isCreateMode="isCreateMode"
              :errors="errors"
            />
          </PTabPanel>
          <PTabPanel :value="1">
            <ProfileSourcesTab
              :sources="localSourceNames"
              @update:sources="localSourceNames = $event"
            />
          </PTabPanel>
          <PTabPanel :value="2">
            <ProfileRrdSettingsTab
              :rrdSettings="rrdSettingsModel"
              @update:rrdSettings="onRrdSettingsUpdate"
              :errors="errors"
            />
          </PTabPanel>
        </PTabPanels>
      </PTabs>

      <div class="action-row">
        <PButton
          outlined
          data-test="cancel-button"
          @click="goBack"
        >
          Cancel
        </PButton>
        <PButton
          v-if="!isCreateMode"
          outlined
          data-test="delete-button"
          @click="openDeleteCollectionProfileDialog"
        >
          Delete Profile
        </PButton>
        <PButton
          data-test="save-button"
          :disabled="isSaveDisabled"
          @click="saveProfile"
        >
          {{ isCreateMode ? 'Create Profile' : 'Save Profile' }}
        </PButton>
      </div>
    </TableCard>
  </div>
  <ConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Profile"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>Are you sure you want to delete the profile <strong>{{ store.selectedProfile?.name }}</strong>? This action cannot be undone.</p>
    </template>
  </ConfirmationDialog>
  </template>
  <div
    v-else
    class="not-found-container"
  >
    <p>No data found.</p>
    <PButton @click="goBack">
      Go Back
    </PButton>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ProfileDetailsTab from './ProfileDetailsTab.vue'
import ProfileSourcesTab from './ProfileSourcesTab.vue'
import ProfileRrdSettingsTab from './ProfileRrdSettingsTab.vue'
import useSnackbar from '@/composables/useSnackbar'
import { rraFromString, rraToString } from '@/lib/timeSeriesHelpers'
import { createSnmpCollectionProfile, updateDataCollectionProfile } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpProfileStorageFlagType } from '@/types/snmpDataCollection'
import type { ConfigDetailsModel, EditableRRA, ProfileFormErrors, RrdSettingsModel } from '@/types/snmpDataCollection'
import { CreateEditMode } from '@/types'
import { FeatherIcon } from '@featherds/icon'
import ArrowBack from '@featherds/icon/navigation/ArrowBack'
import ButtonComponent from 'primevue/button'
import TabComponent from 'primevue/tab'
import TabListComponent from 'primevue/tablist'
import TabPanelComponent from 'primevue/tabpanel'
import TabPanelsComponent from 'primevue/tabpanels'
import TabsComponent from 'primevue/tabs'
import TagComponent from 'primevue/tag'

const PButton = ButtonComponent
const PTabs = TabsComponent
const PTabList = TabListComponent
const PTab = TabComponent
const PTabPanels = TabPanelsComponent
const PTabPanel = TabPanelComponent
const PTag = TagComponent

const router = useRouter()
const route = useRoute()
const store = useSnmpDataCollectionStore()
const snackbar = useSnackbar()

const mode = ref<CreateEditMode>(CreateEditMode.Edit)
const isCreateMode = computed(() => mode.value === CreateEditMode.Create)

const activeTab = ref(0)

const localName = ref('')
const localEnabled = ref(false)
const localSourceNames = ref<string[]>([])
const localMaxVarsPerPdu = ref<string>('')
const localRrdStep = ref<string>('')
const localStorageFlag = ref<string>(SnmpProfileStorageFlagType.SELECT)
const errors = ref<ProfileFormErrors>({})
const isSaveDisabled = ref(false)

let nextRRAId = 0
const localRRAs = ref<EditableRRA[]>([])

const showDeleteConfirmation = ref(false)

const configDetailsModel = computed<ConfigDetailsModel>(() => ({
  name: localName.value,
  enabled: localEnabled.value,
  maxVarsPerPdu: localMaxVarsPerPdu.value,
  storageFlag: localStorageFlag.value
}))

const onConfigDetailsUpdate = (val: ConfigDetailsModel) => {
  localName.value = val.name
  localEnabled.value = val.enabled
  localMaxVarsPerPdu.value = val.maxVarsPerPdu
  localStorageFlag.value = val.storageFlag
}

const rrdSettingsModel = computed<RrdSettingsModel>(() => ({
  rrdStep: localRrdStep.value,
  rras: localRRAs.value
}))

const onRrdSettingsUpdate = (val: RrdSettingsModel) => {
  localRrdStep.value = val.rrdStep
  localRRAs.value = val.rras
}

const goBack = () => {
  router.push({ name: 'SNMP Data Collection' })
}

const openDeleteCollectionProfileDialog = () => {
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  showDeleteConfirmation.value = false
  const id = store.selectedProfile?.id
  if (id == null) {
    return
  }

  const success = await store.removeSnmpCollectionProfiles([id])

  if (success) {
    await store.fetchSnmpCollectionProfiles()
    snackbar.showSnackBar({ msg: `Profile '${store.selectedProfile?.name ?? id}' deleted successfully.` })
    goBack()
  } else {
    snackbar.showSnackBar({ msg: 'Failed to delete profile.', error: true })
  }
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
}

const validateProfile = (): ProfileFormErrors => {
  const errs: ProfileFormErrors = {}

  if (isCreateMode.value && !localName.value.trim()) {
    errs.name = 'Profile name is required'
  }

  const maxVars = localMaxVarsPerPdu.value
  if (maxVars !== '' && maxVars !== null && maxVars !== undefined) {
    const num = Number(maxVars)
    if (!Number.isInteger(num) || num <= 0) {
      errs.maxVarsPerPdu = 'Must be an integer greater than 0'
    }
  }

  const step = localRrdStep.value
  if (step === '' || step === null || step === undefined) {
    errs.rrdStep = 'RRD Step is required'
  } else {
    const num = Number(step)
    if (!Number.isInteger(num) || num <= 0) {
      errs.rrdStep = 'Must be an integer greater than 0'
    }
  }

  if (localRRAs.value.length === 0) {
    errs.rrdRras = 'At least one RRA is required'
  }

  if (!Object.values(SnmpProfileStorageFlagType).includes(localStorageFlag.value as SnmpProfileStorageFlagType)) {
    errs.storageFlag = 'Storage Flag is required'
  }

  return errs
}

const saveProfile = async () => {
  const validationErrors = validateProfile()
  errors.value = validationErrors

  if (Object.keys(validationErrors).length > 0) {
    snackbar.showSnackBar({
      msg: Object.values(validationErrors).join(' '),
      error: true
    })
    return
  }

  const profileName = isCreateMode.value ? localName.value.trim() : store.selectedProfile!.name
  const profileBase = {
    name: profileName,
    enabled: localEnabled.value,
    sourceNames: localSourceNames.value,
    maxVarsPerPdu: localMaxVarsPerPdu.value !== '' ? Number(localMaxVarsPerPdu.value) : undefined,
    rrdStep: Number(localRrdStep.value),
    rrdRras: localRRAs.value.map(rraToString),
    storageFlag: localStorageFlag.value
  }

  if (isCreateMode.value) {
    const success = await createSnmpCollectionProfile(profileBase)

    if (success) {
      await store.fetchSnmpCollectionProfiles()
      snackbar.showSnackBar({ msg: `Profile '${profileName}' created successfully.` })
      goBack()
    } else {
      snackbar.showSnackBar({ msg: `Failed to create profile '${profileName}'.`, error: true })
    }
  } else {
    const profile = { ...store.selectedProfile!, ...profileBase }
    const success = await updateDataCollectionProfile(profile)

    if (success) {
      await store.fetchSnmpCollectionProfiles()
      snackbar.showSnackBar({ msg: `Profile '${profileName}' updated successfully.` })
    } else {
      snackbar.showSnackBar({ msg: `Failed to update profile '${profileName}'.`, error: true })
    }
  }
}

watchEffect(() => {
  errors.value = validateProfile()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

onMounted(async () => {
  if (!store.profiles.length) {
    await store.fetchSnmpCollectionProfiles()
  }

  if (route.params.id === 'create') {
    mode.value = CreateEditMode.Create
    store.selectedProfile = {
      id: 0,
      name: '',
      enabled: true,
      sourceNames: [],
      rrdStep: 300,
      rrdRras: [],
      storageFlag: SnmpProfileStorageFlagType.SELECT
    }
  } else if (route.params.id) {
    const profileId = Number(route.params.id as string)
    store.selectedProfile = store.profiles.find(profile => profile.id === profileId) || null
  }

  if (!store.uploadedSourceNames.length) {
    await store.fetchAllSourcesNames()
  }

  if (store.selectedProfile) {
    localEnabled.value = store.selectedProfile.enabled
    localSourceNames.value = [...store.selectedProfile.sourceNames]
    localMaxVarsPerPdu.value = store.selectedProfile.maxVarsPerPdu != null
      ? String(store.selectedProfile.maxVarsPerPdu)
      : ''
    localRrdStep.value = store.selectedProfile.rrdStep ? String(store.selectedProfile.rrdStep) : ''
    localStorageFlag.value = store.selectedProfile.storageFlag || SnmpProfileStorageFlagType.SELECT
    localRRAs.value = store.selectedProfile.rrdRras.flatMap((s) => {
      try {
        return [{ ...rraFromString(s), _id: nextRRAId++ }]
      } catch {
        return []
      }
    })
  }
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.snmp-data-collection-profile-details {
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
    }
  }

  .tag {
    .enabled-tag {
      margin: 0 !important;
      border-radius: 1em;
      background-color: #0B720C1F;
      border: 2px solid #0B720C;

      :deep(.p-tag-label) {
        color: #0B720C !important;
      }
    }

    .disabled-tag {
      margin: 0 !important;
      border-radius: 1em;
      background-color: #7575751F;
      border: 2px solid #757575;

      :deep(.p-tag-label) {
        color: #757575 !important;
      }
    }
  }

  .content {
    margin-top: 10px;
    padding: 25px;
    border: 1px solid var(--p-content-border-color);

    .action-row {
      display: flex;
      justify-content: flex-end;
      gap: 0.5rem;
      margin-top: 0;
      padding-top: 20px;
      border-top: 1px solid var(--p-content-border-color);
    }

    .tabs {
      :deep(.p-tab) {
        text-transform: uppercase;
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

<style lang="scss">
@use '@featherds/styles/themes/variables';

.p-select-overlay {
  font-family: var(--feather-font-family);
}

// The Select overlay is teleported to body, so :deep() can't reach it.
// Un-layered global CSS here wins over PrimeVue's @layer primevue styles.
.open-dark {
  .p-select-overlay {
    background: var(variables.$surface);
    color: var(variables.$primary-text-on-surface);
    border-color: var(variables.$border-on-surface);
  }

  .p-select-label {
    color: var(variables.$primary-text-on-surface);
  }

  .p-select-option {
    color: var(variables.$primary-text-on-surface);

    &.p-select-option-selected,
    &:not(.p-disabled):hover {
      background: rgba(255, 255, 255, 0.06);
    }
  }
}
</style>
