<template>
  <template v-if="store.selectedProfile">
  <div
    class="snmp-data-collection-profile-details"
  >
    <div class="header">
      <div class="title-container">
        <div class="back">
          <FeatherBackButton
            data-test="back-button"
            @click="goBack"
          >
            Go Back
          </FeatherBackButton>
        </div>
        <div class="title">
          <h1>{{ isCreateMode ? 'Create New Profile' : `Profile details for: ${store.selectedProfile.name}` }}</h1>
        </div>
        <div class="tag">
          <FeatherChip
            v-if="store.selectedProfile.enabled"
            class="enabled-tag"
            data-test="status-tag"
          >
            Enabled
          </FeatherChip>
          <FeatherChip
            v-if="!store.selectedProfile.enabled"
            class="disabled-tag"
            data-test="status-tag"
          >
            Disabled
          </FeatherChip>
        </div>
      </div>
    </div>
    <TableCard class="content">
      <div
        class="config-details-box"
        data-test="profile-details-box"
      >
        <div class="section-header">Profile Details</div>
        <div class="config-row">
          <div class="config-field">
            <span class="field-label">Name:</span>
            <FeatherInput
              v-if="isCreateMode"
              label="Profile Name"
              v-model="localName"
              :error="errors.name"
              data-test="profile-name-input"
              class="settings-input"
            />
            <span
              v-else
              class="field-value"
            >{{ store.selectedProfile.name }}</span>
          </div>
          <div class="config-field switch-field">
            <span class="field-label">Status:</span>
            <PToggleSwitch
              v-model="localEnabled"
              data-test="profile-enabled-switch"
            />
            <div class="tag">
              <FeatherChip
                v-if="localEnabled"
                class="enabled-tag"
                data-test="status-tag"
              >
                Enabled
              </FeatherChip>
              <FeatherChip
                v-if="!localEnabled"
                class="disabled-tag"
                data-test="status-tag"
              >
                Disabled
              </FeatherChip>
            </div>
          </div>
        </div>
        <div class="config-row">
          <div class="config-field">
            <span class="field-label">Creation Date:</span>
            <span class="field-value">{{
              store.selectedProfile.createdTime ?
              format(store.selectedProfile.createdTime, 'MM/dd/yyyy') : '--'
            }}</span>
          </div>
          <div class="config-field">
            <span class="field-label">Last Modified:</span>
            <span class="field-value">{{
              store.selectedProfile.lastModified ?
              format(store.selectedProfile.lastModified, 'MM/dd/yyyy') : '--'
            }}</span>
          </div>
        </div>
        <div class="config-row">
          <div class="config-field">
            <span class="field-label">Max Vars Per PDU:</span>
            <FeatherInput
              label="Max Vars Per PDU"
              v-model="localMaxVarsPerPdu"
              type="number"
              :error="errors.maxVarsPerPdu"
              data-test="max-vars-per-pdu"
              class="settings-input"
            />
          </div>
          <div class="config-field">
            <span class="field-label">Storage Flag:</span>
            <PSelect
              v-model="localStorageFlag"
              :options="storageFlagOptions"
              optionLabel="label"
              optionValue="value"
              :invalid="!!errors.storageFlag"
              data-test="storage-flag-select"
              class="settings-select"
            />
            <span
              v-if="errors.storageFlag"
              class="field-error"
            >{{ errors.storageFlag }}</span>
          </div>
        </div>
      </div>

      <div
        class="sources-box"
        data-test="sources-box"
      >
        <div class="section-header">Sources</div>
        <div>Add or remove sources from this profile.</div>
        <div class="autocomplete-row">
          <FeatherAutocomplete
            type="single"
            label="Add Source"
            textProp="name"
            :modelValue="selectedAutoSource"
            @update:modelValue="onSourceSelected"
            @search="onSourceSearch"
            :results="sourceSearchResults"
            data-test="add-source-autocomplete"
          />
        </div>

        <div class="sources-card">
          <PDataTable
            :value="sortedSources"
            scrollable
            scrollHeight="400px"
            :size="'small'"
            :virtualScrollerOptions="{ itemSize: 44 }"
            tableStyle="min-width: 50rem"
          >
            <PColumn field="name" style="width: 20%; height: 44px"></PColumn>
            <PColumn
              style="width: 4rem"
            >
              <template #body="{ data }">
                <FeatherButton
                  icon="Delete"
                  data-test="delete-source-button"
                  @click="removeSource(data.name)"
                >
                  <FeatherIcon :icon="Delete" />
                </FeatherButton>
              </template>
            </PColumn>
          </PDataTable>
        </div>
      </div>

      <hr />

      <div
        class="rrd-settings-box"
        data-test="rrd-settings-box"
      >
        <div class="section-header">RRD Settings</div>
        <div class="input-row">
          <FeatherInput
            label="RRD Step"
            v-model="localRrdStep"
            type="number"
            :error="errors.rrdStep"
            hint="RRD step size in seconds"
            data-test="rrd-step"
          />
        </div>
        <div class="rra-section">
          <div class="rra-header">
            <span class="rra-title">RRAs</span>
            <FeatherButton
              secondary
              icon="Add"
              data-test="add-rra-button"
              class="add-rra-button"
              @click="addRRA"
            >
              <FeatherIcon :icon="Add" />
              Add RRA
            </FeatherButton>
          </div>
          <PDataTable
            v-model:editingRows="editingRows"
            :value="localRRAs"
            editMode="row"
            dataKey="_id"
            @row-edit-save="onRowEditSave"
            data-test="rra-table"
          >
            <PColumn
              header="RRA"
              style="width: 4rem"
            >
              <template #body>
                <span>RRA</span>
              </template>
              <template #editor>
                <span>RRA</span>
              </template>
            </PColumn>
            <PColumn
              field="cf"
              header="Consolidation Function"
            >
              <template #editor="{ data }">
                <PSelect
                  v-model="data.cf"
                  :options="cfOptions"
                  optionLabel="label"
                  optionValue="value"
                />
              </template>
            </PColumn>
            <PColumn
              field="xff"
              header="XFF"
            >
              <template #editor="{ data }">
                <PInputNumber
                  v-model="data.xff"
                  :min="0"
                  :maxFractionDigits="6"
                />
              </template>
            </PColumn>
            <PColumn
              field="steps"
              header="Step"
            >
              <template #editor="{ data }">
                <PInputNumber
                  v-model="data.steps"
                  :min="1"
                  :step="1"
                />
              </template>
            </PColumn>
            <PColumn
              field="rows"
              header="Rows"
            >
              <template #editor="{ data }">
                <PInputNumber
                  v-model="data.rows"
                  :min="1"
                  :step="1"
                />
              </template>
            </PColumn>
            <PColumn
              header=""
              style="width: 4rem"
            >
              <template #body="{ data }">
                <FeatherButton
                  icon="Delete"
                  data-test="delete-rra-button"
                  @click="deleteRRA(data._id)"
                >
                  <FeatherIcon :icon="Delete" />
                </FeatherButton>
              </template>
            </PColumn>
            <PColumn
              :rowEditor="true"
              style="width: 8rem"
              bodyStyle="text-align: center"
              :pt="{
                pcRowEditorInit: {
                  root: { title: 'Edit' }
                }
              }"
            />
          </PDataTable>
        </div>
         <span
            v-if="errors.rrdRras"
            class="field-error"
          >{{ errors.rrdRras }}</span>
      </div>

      <div class="action-row">
        <FeatherButton
          secondary
          data-test="cancel-button"
          @click="goBack"
        >
          Cancel
        </FeatherButton>
        <FeatherButton
          v-if="!isCreateMode"
          secondary
          data-test="delete-button"
          @click="openDeleteCollectionProfileDialog"
        >
          Delete Profile
        </FeatherButton>
        <FeatherButton
          primary
          data-test="save-button"
          :disabled="isSaveDisabled"
          @click="saveProfile"
        >
          {{ isCreateMode ? 'Create Profile' : 'Save Profile' }}
        </FeatherButton>
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
    <FeatherButton
      primary
      @click="goBack"
    >
      Go Back
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useSnackbar from '@/composables/useSnackbar'
import { rraFromString, rraToString } from '@/lib/timeSeriesHelpers'
import { createSnmpCollectionProfile, updateDataCollectionProfile } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpProfileStorageFlagType } from '@/types/snmpDataCollection.d'
import { ConsolidationFunctionType, RRA } from '@/types/timeSeries.d'
import { CreateEditMode } from '@/types'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import Delete from '@featherds/icon/action/Delete'
import { FeatherInput } from '@featherds/input'

import { format } from 'date-fns-tz'
import ToggleSwitchComponent from 'primevue/toggleswitch'
import DataTableComponent from 'primevue/datatable'
import type { DataTableRowEditSaveEvent } from 'primevue/datatable'
import ColumnComponent from 'primevue/column'
import InputNumberComponent from 'primevue/inputnumber'
import SelectComponent from 'primevue/select'

const PToggleSwitch = ToggleSwitchComponent
const PDataTable = DataTableComponent
const PColumn = ColumnComponent
const PInputNumber = InputNumberComponent
const PSelect = SelectComponent

interface SourceItem extends IAutocompleteItemType {
  name: string
  id: number
}

interface ProfileFormErrors {
  name?: string
  maxVarsPerPdu?: string
  rrdStep?: string
  storageFlag?: string
  rrdRras?: string
}

type EditableRRA = RRA & { _id: number }

const router = useRouter()
const route = useRoute()
const store = useSnmpDataCollectionStore()
const snackbar = useSnackbar()

const mode = ref<CreateEditMode>(CreateEditMode.Edit)
const isCreateMode = computed(() => mode.value === CreateEditMode.Create)

const localName = ref('')
const localEnabled = ref(false)
const localSourceNames = ref<string[]>([])
const localMaxVarsPerPdu = ref<string>('')
const localRrdStep = ref<string>('')
const localStorageFlag = ref<string>(SnmpProfileStorageFlagType.SELECT)
const errors = ref<ProfileFormErrors>({})
const isSaveDisabled = ref(false)

const storageFlagOptions = Object.values(SnmpProfileStorageFlagType).map(v => ({
  label: v.charAt(0).toUpperCase() + v.slice(1),
  value: v
}))

const selectedAutoSource = ref<SourceItem | undefined>(undefined)
const sourceSearchResults = ref<SourceItem[]>([])

const sortedSources = computed(() => [...localSourceNames.value].sort((a, b) => a.localeCompare(b)).map(name => ({ name })))

let nextRRAId = 0
const localRRAs = ref<EditableRRA[]>([])
const editingRows = ref<EditableRRA[]>([])
const cfOptions = Object.values(ConsolidationFunctionType).map(v => ({ label: v, value: v }))

const showDeleteConfirmation = ref(false)

const goBack = () => {
  router.push({ name: 'SNMP Data Collection' })
}

const openDeleteCollectionProfileDialog = () => {
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  showDeleteConfirmation.value = false
  const id = store.selectedProfile?.id
  if (id == null) { return }

  const success = await store.removeSnmpCollectionProfiles([id])

  if (success) {
    await store.fetchSnmpCollectionProfiles()
    snackbar.showSnackBar({ msg: `Profile '${store.selectedProfile?.name ?? id}' deleted successfully.` })
    goBack()
  } else {
    snackbar.showSnackBar({ msg: `Failed to delete profile.`, error: true })
  }
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
}

const removeSource = (name: string) => {
  localSourceNames.value = localSourceNames.value.filter(s => s !== name)
}

const onSourceSearch = (query: string) => {
  const q = query.toLowerCase()
  sourceSearchResults.value = store.uploadedSourceNames
    .filter(s => !localSourceNames.value.includes(s.name))
    .filter(s => s.name.toLowerCase().includes(q))
    .map(s => ({ name: s.name, id: s.id }))
}

const onSourceSelected = (item: IAutocompleteItemType | IAutocompleteItemType[] | undefined) => {
  const source = item as SourceItem | undefined
  if (source && !Array.isArray(source)) {
    localSourceNames.value.push(source.name)
    selectedAutoSource.value = undefined
    sourceSearchResults.value = []
  }
}

const addRRA = () => {
  localRRAs.value.push({
    _id: nextRRAId++,
    cf: ConsolidationFunctionType.AVERAGE,
    xff: 0.5,
    steps: 1,
    rows: 1
  })
}

const deleteRRA = (id: number) => {
  localRRAs.value = localRRAs.value.filter(r => r._id !== id)
}

const onRowEditSave = (event: DataTableRowEditSaveEvent) => {
  localRRAs.value[event.index] = { ...event.newData } as EditableRRA
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
    localRRAs.value = store.selectedProfile.rrdRras.flatMap(s => {
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
      border-color: #0B720C;
      border-width: 2px;

      :deep(span) {
        color: #0B720C !important;
      }
    }

    .disabled-tag {
      margin: 0 !important;
      border-radius: 1em;
      background-color: #7575751F;
      border-color: #757575;
      border-width: 2px;

      :deep(span) {
        color: #757575 !important;
      }
    }
  }

  .content {
    margin-top: 10px;
    padding: 25px;
    border: 1px solid var(--feather-border-on-surface);

    .config-details-box,
    .sources-box {
      margin-bottom: 30px;
    }

    .sources-box .sources-card {
      :deep(.p-datatable-thead) {
        display: none;
      }

      :deep(.p-datatable-tbody > tr) {
        background-color: var(--feather-surface);
        color: var(--feather-primary-text-on-surface);
      }

      :deep(.p-datatable-tbody > tr > td) {
        border-color: var(--feather-border-on-surface);
        color: var(--feather-primary-text-on-surface);
      }
    }

    .section-header {
      @include headline3;
      margin-bottom: 16px;
    }

    .config-row {
      display: flex;
      align-items: center;

      .config-field {
        display: flex;
        align-items: center;
        flex: 1;
        margin-right: 40px;

        .field-label {
          @include headline4;
          margin-right: 10px;
          color: var(--feather-secondary-text-on-surface);
          min-width: 110px;
        }

        .field-value {
          @include body-large;
        }

        &.switch-field {
          gap: 12px;

          .switch-label {
            @include body-large;
          }
        }
      }
    }

    .input-row {
      max-width: 300px;
    }

    .settings-input {
      max-width: 20em;
      min-width: 16em;
      margin-top: 1.5em;
    }

    .settings-select {
      background-color: var(--feather-background);
      color: var(--feather-secondary-text-on-surface);
      min-width: 160px;
    }

    .field-error {
      color: var(--feather-error);
      font-size: 0.8em;
      margin-left: 8px;
    }

    .autocomplete-row {
      max-width: 300px;
      margin-top: 16px;
    }

    .rra-section {
      margin-top: 20px;

      :deep(.p-datatable-thead > tr > th) {
        background-color: var(--feather-background);
        border-bottom: 1px solid var(--feather-border-on-surface);
        color: var(--feather-secondary-text-on-surface);
        text-transform: uppercase;
      }

      :deep(.p-datatable-tbody > tr) {
        background-color: var(--feather-surface);
        color: var(--feather-primary-text-on-surface);
      }

      :deep(.p-datatable-tbody > tr > td) {
        border-color: var(--feather-border-on-surface);
        color: var(--feather-primary-text-on-surface);
      }

      :deep(.p-select) {
        background-color: var(--feather-surface);
        border-color: var(--feather-border-on-surface);
        color: var(--feather-primary-text-on-surface);
      }

      :deep(.p-select-label) {
        color: var(--feather-primary-text-on-surface);
      }

      .rra-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;

        .rra-title {
          @include headline4;
          color: var(--feather-secondary-text-on-surface);
        }

        .add-rra-button {
          border-radius: 0;
          border: 1px solid var(--feather-primary);
          width: auto;
          padding: 0.5em 1em;
        }
      }
    }

    .action-row {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid var(--feather-border-on-surface);
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
