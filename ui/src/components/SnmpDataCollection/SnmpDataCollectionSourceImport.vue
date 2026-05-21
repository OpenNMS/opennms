<template>
  <TableCard class="data-collection-source-import-container">
    <div class="header">
      <div class="title-container">
        <div class="title">
          <h3>Import Data Collection Source</h3>
        </div>
        <div class="sub">
          <p>
            Upload files in the XML format. You can select multiple files at once or upload all files in a specific
            folder.
          </p>
        </div>
      </div>
      <div class="action-container">
        <div class="section-left">
          <input
            type="file"
            accept=".xml"
            multiple
            @change="handleSourceFileUpload"
            data-test="snmp-data-collection-file-input"
            ref="sourceFileInput"
          />
          <input
            type="file"
            multiple
            webkitdirectory
            directory
            @change="handleSourceFolderUpload"
            data-test="snmp-data-collection-folder-input"
            ref="sourceFolderInput"
          />
          <FeatherButton
            secondary
            data-test="choose-file-button"
            @click="openFileDialog"
            :disabled="isLoading"
          >
            <FeatherIcon :icon="UploadFile" />
            Choose files to upload
          </FeatherButton>
          <FeatherButton
            secondary
            data-test="choose-folder-button"
            @click="openFolderDialog"
            :disabled="isLoading"
          >
            <FeatherIcon :icon="FolderAdd" />
            Choose folder to upload
          </FeatherButton>
        </div>
        <div class="section-right">
          <FeatherButton
            primary
            :disabled="shouldUploadDisabled"
            @click="uploadFiles"
            data-test="upload-button"
          >
            <FeatherSpinner v-if="isLoading" />
            <span v-else>Upload Files</span>
          </FeatherButton>
        </div>
      </div>
      <div
        class="profiles-row"
        v-if="availableProfiles.length"
        :class="{ 'profiles-row--required-empty': profileSelectionIsRequired && !selectedProfileNames.length }"
        data-test="profiles-section"
      >
        <div class="profiles-header">
          <span class="profiles-label">
            Add to profiles
            <span
              v-if="profileSelectionIsRequired"
              class="required-marker"
              aria-hidden="true"
            >*</span>
          </span>
          <span
            v-if="!hasConfigFile"
            class="profiles-count"
            data-test="profiles-count"
          >
            {{ selectedProfileNames.length }} of {{ availableProfiles.length }} selected
          </span>
        </div>
        <div class="profiles-list">
          <FeatherCheckbox
            v-for="profile in availableProfiles"
            :key="profile.id"
            :modelValue="selectedProfileNames.includes(profile.name)"
            :disabled="hasConfigFile"
            :data-test="`profile-checkbox-${profile.name}`"
            @update:modelValue="(checked: boolean | undefined) => toggleProfile(profile.name, checked === true)"
          >
            {{ profile.name }}
          </FeatherCheckbox>
        </div>
        <span
          v-if="hasConfigFile"
          class="profiles-hint config-hint"
          data-test="profiles-config-hint"
        >
          Profile assignments will be taken from the uploaded datacollection-config.xml.
        </span>
        <span
          v-else-if="hasNewSource && !selectedProfileNames.length"
          class="profiles-hint required-hint"
          data-test="profiles-hint"
        >
          Pick at least one profile — newly uploaded sources need a profile to land in.
        </span>
        <span
          v-else-if="sourceFiles.length && !hasNewSource && selectedProfileNames.length"
          class="profiles-hint update-only-hint"
          data-test="profiles-update-only-hint"
        >
          Picked profiles will also be added to these existing sources.
        </span>
        <span
          v-else-if="sourceFiles.length && !hasNewSource"
          class="profiles-hint update-only-hint"
          data-test="profiles-update-only-empty-hint"
        >
          Profile selection is optional for update-only uploads; existing sources keep their current
          profile memberships.
        </span>
      </div>
    </div>
    <div class="container">
      <table
        v-if="tableRecord.length"
        class="data-table"
        aria-label="SNMP Data Collection Sources Table"
      >
        <thead>
          <tr>
            <th>Source</th>
            <th>Action</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
          v-if="tableRecord.length"
        >
          <tr
            v-for="(file, index) in tableRecord"
            :key="index"
          >
            <td>
              <div class="file">
                <FeatherIcon :icon="Apps" />
                <span>{{ ellipsify(file.file.name, 39) }}</span>
                <FeatherChip
                  v-if="file.kind === 'config'"
                  class="kind-chip kind-config"
                  :data-test="`kind-chip-${file.file.name}`"
                >
                  Profiles config{{ file.profileNames?.length ? ` (${file.profileNames.length})` : '' }}
                </FeatherChip>
                <FeatherChip
                  v-else-if="file.kind === 'group'"
                  class="kind-chip kind-source"
                  :data-test="`kind-chip-${file.file.name}`"
                >
                  Source
                </FeatherChip>
                <FeatherChip
                  v-if="!file.isValid"
                  class="error-chip"
                >
                  {{ file.errors.join('. ') }}
                </FeatherChip>
                <FeatherChip
                  v-if="file.isDuplicate"
                  class="update-chip"
                  :data-test="`update-chip-${file.file.name}`"
                >
                  Will update existing source '{{ file.groupName }}'
                </FeatherChip>
                <FeatherIcon
                  v-if="!file.isValid"
                  :icon="Error"
                  class="error-icon"
                />
                <FeatherIcon
                  v-if="file.isValid && file.isDuplicate"
                  :icon="Refresh"
                  class="update-icon"
                />
                <FeatherIcon
                  v-if="file.isValid && !file.isDuplicate"
                  :icon="CheckCircle"
                  class="success-icon"
                />
              </div>
            </td>
            <td>
              <FeatherButton
                icon="Trash"
                data-test="remove-files-button"
                @click="removeFile(index)"
              >
                <FeatherIcon :icon="Delete" />
              </FeatherButton>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="tableRecord.length"
      >
        <FeatherPagination
          :modelValue="page"
          :pageSize="pageSize"
          :total="total"
          :pageSizes="[10, 20, 50, 100, 200]"
          @update:modelValue="onPageChange"
          @update:pageSize="onPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
      <div v-if="!tableRecord.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
    <div class="info-section">
      <h3>Instructions:</h3>
      <ul>
        <li>SNMP data collection source files must be in XML format with a .xml extension.</li>
        <li>When uploading using "Choose files to upload", you can select multiple files at once.</li>
        <li>When uploading using "Choose folder to upload", all files in the folder will be uploaded.</li>
        <li>Ensure that the XML files are well-formed and adhere to the expected schema.</li>
        <li>
          Files that are valid and ready for upload will be flagged with icon
          <FeatherIcon
            :icon="CheckCircle"
            class="success-icon-text"
          />.
        </li>
        <li>
          Files with duplicate names (excluding the .xml extension) will be flagged with icon
          <FeatherIcon
            :icon="Warning"
            class="warning-icon-text"
          />
          indicating renaming or overwriting is required. It can be done by clicking on the icon.
        </li>
        <li>
          Invalid files will be flagged with icon
          <FeatherIcon
            :icon="Error"
            class="error-icon-text"
          />
          and error messages indicating the issues found during validation of the file contents and schema compliance.
        </li>
      </ul>
    </div>
    <DataCollectionFilesUploadReportDialog
      :report="uploadFilesReport"
      :dialogVisible="uploadedDataCollectionFilesReportDialogState"
      @close="closeUploadReportDialog"
      @view="gotoViewTab"
    />
    <UploadedFileRenameDialog
      :visible="displayRenameDialog"
      :fileBucket="sourceFiles"
      :index="sourceFiles.findIndex(f => f.isDuplicate)"
      :alreadyExistsNames="store.uploadedSourceNames"
      @close="closeRenameDialog"
      @rename="renameFile"
      @overwrite="overwriteFile"
    />
  </TableCard>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { ellipsify } from '@/lib/utils'
import { getAllSnmpCollectionProfiles, uploadDataCollectionFiles } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionProfile, SnmpDataCollectionSourceUploadResponse, UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherChip } from '@featherds/chips'
import { FeatherIcon } from '@featherds/icon'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import Delete from '@featherds/icon/action/Delete'
import UploadFile from '@featherds/icon/action/UploadFile'
import FolderAdd from '@featherds/icon/file/FolderAdd'
import Apps from '@featherds/icon/navigation/Apps'
import Refresh from '@featherds/icon/navigation/Refresh'
import Error from '@featherds/icon/notification/Error'
import Warning from '@featherds/icon/notification/Warning'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSpinner } from '@featherds/progress'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import DataCollectionFilesUploadReportDialog from './Dialog/DataCollectionFilesUploadReportDialog.vue'
import UploadedFileRenameDialog from './Dialog/UploadedFileRenameDialog.vue'
import { isDuplicateFile, validateSnmpDataCollectionSourceFile } from './snmpDataCollectionSourceXmlValidator'

const store = useSnmpDataCollectionStore()
const sourceFolderInput = ref<HTMLInputElement | null>(null)
const sourceFileInput = ref<HTMLInputElement | null>(null)
const uploadFilesReport = ref<SnmpDataCollectionSourceUploadResponse>({} as SnmpDataCollectionSourceUploadResponse)
const sourceFiles = ref<UploadSnmpDataCollectionFileType[]>([])
const isLoading = ref(false)
const snackbar = useSnackbar()
const displayRenameDialog = ref(false)
const selectedIndex = ref<number | null>(null)
const uploadedDataCollectionFilesReportDialogState = ref(false)
const tableRecord = ref<UploadSnmpDataCollectionFileType[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const availableProfiles = ref<SnmpCollectionProfile[]>([])
const selectedProfileNames = ref<string[]>([])
const emptyListContent = {
  msg: 'No files selected for upload.'
}

// Display order: new sources at the top so they're easy to scan, then update
// rows below. Within each group preserve insertion order. The Source-of-truth
// array stays in insertion order so the rest of the code (pagination math,
// removeFile by index) keeps working off the same indices it always did.
const orderedSourceFiles = computed<UploadSnmpDataCollectionFileType[]>(() => {
  const newOnes: UploadSnmpDataCollectionFileType[] = []
  const updates: UploadSnmpDataCollectionFileType[] = []
  for (const f of sourceFiles.value) {
    if (f.isDuplicate) updates.push(f)
    else newOnes.push(f)
  }
  return [...newOnes, ...updates]
})

const fetchProfiles = async () => {
  availableProfiles.value = await getAllSnmpCollectionProfiles()
}

const toggleProfile = (name: string, checked: boolean) => {
  if (checked) {
    if (!selectedProfileNames.value.includes(name)) {
      selectedProfileNames.value = [...selectedProfileNames.value, name]
    }
  } else {
    selectedProfileNames.value = selectedProfileNames.value.filter((n) => n !== name)
  }
}

onMounted(() => {
  fetchProfiles()
})
const hasConfigFile = computed(() =>
  sourceFiles.value.some((f) => f.kind === 'config')
)

// "New" sources = group files whose name doesn't already exist in the DB.
// These are the only ones that need a profile pick to avoid orphaning;
// updates keep their existing profile memberships.
const hasNewSource = computed(() =>
  sourceFiles.value.some((f) => f.kind === 'group' && !f.isDuplicate)
)

// True when the picker is gating the Upload button (i.e., at least one new
// source is queued and there's no config file driving attachment instead).
// Drives the visual emphasis on the picker.
const profileSelectionIsRequired = computed(
  () => !hasConfigFile.value && hasNewSource.value
)

const shouldUploadDisabled = computed(() => {
  return (
    sourceFiles.value.length === 0 ||
    isLoading.value ||
    // Profile picker is required only when uploading new (non-update)
    // source files. A <datacollection-config> drives attachment via its
    // <include-collection> entries, so it bypasses the requirement.
    // Pure-update batches also bypass: the sources keep their existing
    // profile memberships on the server.
    (!hasConfigFile.value && hasNewSource.value && selectedProfileNames.value.length === 0) ||
    !sourceFiles.value.every(f => f.isValid)
  )
})

const removeFile = (index: number) => {
  // tableRecord is sliced from orderedSourceFiles (sorted: new before
  // updates), so its row index doesn't line up with sourceFiles. Resolve
  // back through the File reference to find the actual array position.
  const target = tableRecord.value[index]?.file
  if (!target) return
  const sourceIndex = sourceFiles.value.findIndex((f) => f.file === target)
  if (sourceIndex < 0) return
  sourceFiles.value.splice(sourceIndex, 1)
  total.value = sourceFiles.value.length
  tableRecord.value = orderedSourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
}

const openFileDialog = () => {
  sourceFileInput.value?.click()
}

const openFolderDialog = () => {
  sourceFolderInput.value?.click()
}

const onPageChange = (newPage: number) => {
  page.value = newPage
  tableRecord.value = orderedSourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
}

const onPageSizeChange = (newPageSize: number) => {
  pageSize.value = newPageSize
  page.value = 1
  tableRecord.value = orderedSourceFiles.value.slice(0, pageSize.value)
}

const isExistingSourceName = (groupName: string | undefined): boolean => {
  if (!groupName) return false
  const target = groupName.toLowerCase()
  return store.uploadedSourceNames.some((s) => s.name.toLowerCase() === target)
}

const handleSourceFileUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const files = Array.from(input.files)
    for (const file of files) {
      try {
        if (isDuplicateFile(file.name, sourceFiles.value)) {
          continue
        }
        const { isValid, errors, kind, groupName, profileNames } = await validateSnmpDataCollectionSourceFile(file)
        sourceFiles.value.push({
          file,
          isValid: isValid,
          errors: errors,
          kind,
          groupName,
          profileNames,
          // Config files don't have a group name and the server upserts them
          // by profile name, so duplicate-against-uploaded-sources doesn't apply.
          isDuplicate: kind === 'group' && isExistingSourceName(groupName)
        })
        if (!isValid) {
          snackbar.showSnackBar({
            msg: `Error processing file ${file.name}.`,
            error: true
          })
        }
      } catch (error) {
        console.error(`Error processing file ${file.name}:`, error)
        snackbar.showSnackBar({
          msg: `Error processing file ${file.name}.`,
          error: true
        })
      }
    }
    total.value = sourceFiles.value.length
    tableRecord.value = orderedSourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
    // Reset the input value to allow re-uploading the same file if needed
    input.value = ''
    input.files = null
  } else {
    console.warn('No files selected')
  }
}

const handleSourceFolderUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const files = Array.from(input.files)
    for (const file of files) {
      try {
        // Folder uploads include every file in the directory (READMEs,
        // .DS_Store, archives, etc.). Quietly skip anything that's not .xml
        // so the table stays focused on candidate sources.
        if (!file.name.toLowerCase().endsWith('.xml')) {
          continue
        }
        if (isDuplicateFile(file.name, sourceFiles.value)) {
          continue
        }
        const { isValid, errors, kind, groupName, profileNames } = await validateSnmpDataCollectionSourceFile(file)
        // Re-uploaded sources go into the table just like new ones, marked
        // as updates so the user can see what will be overwritten on the
        // server. Config files don't have a group name, so the existence
        // check doesn't apply to them.
        const isExistingUpdate = kind === 'group' && isExistingSourceName(groupName)
        sourceFiles.value.push({
          file,
          isValid: isValid,
          errors: errors,
          kind,
          groupName,
          profileNames,
          isDuplicate: isExistingUpdate
        })
        if (!isValid) {
          snackbar.showSnackBar({
            msg: `Error processing file ${file.name}.`,
            error: true
          })
        }
      } catch (error) {
        console.error(`Error processing file ${file.name}:`, error)
        snackbar.showSnackBar({
          msg: `Error processing file ${file.name}.`,
          error: true
        })
      }
    }
    total.value = sourceFiles.value.length
    tableRecord.value = orderedSourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)

    // Reset the input value to allow re-uploading the same file if needed
    input.value = ''
    input.files = null
  }
}

const uploadFiles = async () => {
  if (sourceFiles.value.length === 0) {
    console.warn('No files to upload')
    return
  }
  if (!sourceFiles.value.every(f => f.file.name.endsWith('.xml'))) {
    snackbar.showSnackBar({
      msg: 'All files must be XML files with .xml extension',
      error: true
    })
    return
  }
  isLoading.value = true
  try {
    const response = await uploadDataCollectionFiles(
      sourceFiles.value.filter(f => f.isValid).map(f => f.file),
      selectedProfileNames.value
    )
    uploadFilesReport.value = {
      errors: [...response.errors],
      success: [...response.success]
    }
    sourceFiles.value = []
    tableRecord.value = []
    total.value = 0
    sourceFileInput.value!.value = ''
    store.fetchAllSourcesNames()
    store.fetchSnmpCollectionSources()
    uploadedDataCollectionFilesReportDialogState.value = true
    isLoading.value = false
  } catch (err) {
    console.error(err)
    isLoading.value = false
    snackbar.showSnackBar({
      msg: 'Error uploading files',
      error: true
    })
  }
}

const closeUploadReportDialog = () => {
  uploadedDataCollectionFilesReportDialogState.value = false
}

const gotoViewTab = () => {
  store.activeTab = 0
  uploadedDataCollectionFilesReportDialogState.value = false
}

const openFileRenameDialog = (index: number) => {
  displayRenameDialog.value = true
  selectedIndex.value = index
}

const closeRenameDialog = () => {
  displayRenameDialog.value = false
  selectedIndex.value = null
}

const renameFile = async (newFileName: string) => {
  if (selectedIndex.value !== null && selectedIndex.value >= 0 && selectedIndex.value < sourceFiles.value.length) {
    const fileToRename = sourceFiles.value[selectedIndex.value]
    const newFile = new File([fileToRename.file], newFileName, { type: fileToRename.file.type })
    const validationResult = await validateSnmpDataCollectionSourceFile(newFile)
    // Renaming the file does not change the <datacollection-group name>
    // inside the XML, so duplicate state is determined by the parsed
    // groupName, not the new filename.
    sourceFiles.value[selectedIndex.value] = {
      file: newFile,
      isValid: validationResult.isValid,
      errors: validationResult.errors,
      groupName: validationResult.groupName,
      isDuplicate: isExistingSourceName(validationResult.groupName)
    }
    closeRenameDialog()
  } else {
    console.error('Invalid index for renaming file')
  }
}

const overwriteFile = () => {
  if (selectedIndex.value !== null && selectedIndex.value >= 0 && selectedIndex.value < sourceFiles.value.length) {
    sourceFiles.value[selectedIndex.value].isDuplicate = false
    closeRenameDialog()
  } else {
    console.error('Invalid index for overwriting file')
  }
}

watch(
  () => store.uploadedSourceNames,
  (newNames) => {
    const existing = new Set(newNames.map((s) => s.name.toLowerCase()))
    sourceFiles.value = sourceFiles.value.map((file) => ({
      ...file,
      isDuplicate: !!file.groupName && existing.has(file.groupName.toLowerCase())
    }))
  }, { immediate: true, deep: true }
)

onMounted(async () => {
  await store.fetchAllSourcesNames()
})

// Tabs are kept-alive in this page, so onMounted only fires once. Re-fetch
// the source-names cache every time this tab becomes active so that any
// deletes/uploads done in the Sources tab are reflected in duplicate
// detection here.
watch(
  () => store.activeTab,
  (tab) => {
    // Tab index 1 = Import Data Collection Sources
    if (tab === 1) {
      store.fetchAllSourcesNames()
    }
  }
)
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';


.data-collection-source-import-container {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    .title-container {
      .title {
        h2 {
          margin: 0;
          @include typography.headline3;
        }
      }

      .sub {
        p {
          margin: 0;
        }
      }
    }

    .action-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 40px 0px 12px 0px;

      .section-left {
        display: flex;
        gap: 10px;

        input {
          display: none;
        }
      }

      .section-right {
        display: flex;
        align-items: center;

        button {
          :deep(.spinner) {
            height: 1.5rem !important;
            width: 1.5rem !important;
          }
        }
      }
    }

    // Profiles row sits directly beneath the action row. The card surface is
    // intentionally subtle (light neutral fill, thin neutral border) so it
    // reads as a deliberate control group without competing with the table
    // below. When picker selection is required but empty we shift to a pale
    // warning tint + amber left-edge — visible but not jarring, with text
    // contrast preserved on the lighter background.
    .profiles-row {
      display: grid;
      grid-template-columns: 1fr;
      gap: 6px;
      margin: 12px 0 16px;
      padding: 12px 14px;
      background-color: rgba(0, 0, 0, 0.03);
      border: 1px solid var(--feather-border-on-surface);
      border-left: 3px solid transparent;
      border-radius: 4px;
      transition: background-color 0.15s ease, border-color 0.15s ease;

      &--required-empty {
        // Pale amber tint (~9% alpha) so checkbox borders and labels stay
        // legible regardless of the underlying theme. Border-left uses the
        // matching warning hue so the eye lands on the gating control.
        background-color: rgba(255, 193, 7, 0.09);
        border-left-color: #F57C00;
      }

      .profiles-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
      }

      .profiles-label {
        @include typography.subtitle2;
        color: var(--feather-primary-text-on-surface);

        .required-marker {
          color: #C62828;
          margin-left: 2px;
        }
      }

      .profiles-count {
        @include typography.caption;
        color: var(--feather-secondary-text-on-surface);
      }

      .profiles-list {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0 16px;
      }

      .profiles-hint {
        @include typography.body-small;
        color: var(--feather-secondary-text-on-surface);
      }

      .profiles-hint.required-hint {
        color: #C62828;
      }
    }
  }

  .container {
    table {
      width: 100%;
      border: 1px solid var(--feather-border-on-surface);
      @include table.table;

      thead {
        background: var(variables.$background);
        text-transform: uppercase;

        th:first-child {
          width: 85%;
        }
      }

      td {
        white-space: nowrap;
        box-shadow: none;
        border-bottom: 1px solid var(variables.$border-on-surface);

        div {
          border-radius: 5px;
          padding: 0px 5px 0px 5px;
        }

        .file {
          display: flex;
          align-items: center;
          gap: 10px;

          .kind-chip {
            min-width: none;
            max-width: none;
            border-radius: 4px;

            &.kind-source {
              background-color: #0B720C1F;
              :deep(span) { color: #0B720C !important; }
            }

            &.kind-config {
              background-color: #1976D21F;
              :deep(span) { color: #1976D2 !important; }
            }
          }

          .error-chip {
            background-color: #A5021F33;
            color: #A5021F;
            min-width: none;
            max-width: none;
          }

          .warning-chip {
            background-color: #FBE94733;
            color: #FBE947;
            min-width: none;
            max-width: none;

          }

          // Re-uploaded source files are upserts, not errors. Use a calm
          // info-blue treatment so it reads as "this will update an existing
          // record" rather than "you have a problem".
          .update-chip {
            background-color: #1976D21F;
            min-width: none;
            max-width: none;
            border-radius: 4px;
            :deep(span) { color: #1976D2 !important; }
          }

          .update-icon {
            color: #1976D2;
            height: 2em;
            width: 2em;
          }

          .success-icon {
            color: var(variables.$success);
            cursor: pointer;
            height: 2em;
            width: 2em;
          }

          .error-icon {
            color: var(variables.$error);
            cursor: pointer;
            height: 2em;
            width: 2em;
          }

          .warning-icon {
            color: var(variables.$major);
            cursor: pointer;
            height: 2em;
            width: 2em;
          }
        }

        .action-container {
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }
    }

    .alerts-pagination {
      display: flex;
      justify-content: center;
      padding: 30px 0px 0px 0px;
    }

    .feather-pagination {
      border: none !important;
    }
  }

  .info-section {
    .success-icon-text {
      color: var(variables.$success);
      vertical-align: middle;
      height: 2em;
      width: 2em;
    }

    .error-icon-text {
      color: var(variables.$error);
      vertical-align: middle;
      height: 2em;
      width: 2em;
    }

    .warning-icon-text {
      color: var(variables.$major);
      vertical-align: middle;
      height: 2em;
      width: 2em;
    }
  }
}
</style>

