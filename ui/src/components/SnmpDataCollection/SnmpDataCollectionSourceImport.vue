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
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Events Table"
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
                  v-if="!file.isValid"
                  class="error-chip"
                >
                  {{ file.errors.join('. ') }}
                </FeatherChip>
                <FeatherChip
                  v-if="file.isDuplicate"
                  class="warning-chip"
                >
                  File with the same name already exists. Please rename or choose to overwrite.
                </FeatherChip>
                <FeatherIcon
                  v-if="!file.isValid"
                  :icon="Error"
                  class="error-icon"
                />
                <FeatherIcon
                  v-if="file.isDuplicate"
                  :icon="Warning"
                  class="warning-icon"
                  @click="openFileRenameDialog(index)"
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
        <li>Event configuration files must be in XML format with a .xml extension.</li>
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
import { uploadDataCollectionFiles } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpDataCollectionSourceUploadResponse, UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherIcon } from '@featherds/icon'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import Delete from '@featherds/icon/action/Delete'
import UploadFile from '@featherds/icon/action/UploadFile'
import FolderAdd from '@featherds/icon/file/FolderAdd'
import Apps from '@featherds/icon/navigation/Apps'
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
const emptyListContent = {
  msg: 'No files selected for upload.'
}
const shouldUploadDisabled = computed(() => {
  return (
    sourceFiles.value.length === 0 ||
    isLoading.value ||
    !sourceFiles.value.every(f => f.isValid) ||
    sourceFiles.value.some(f => f.isDuplicate)
  )
})

const removeFile = (index: number) => {
  sourceFiles.value.splice(index, 1)
  total.value = sourceFiles.value.length
  tableRecord.value = sourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
}

const openFileDialog = () => {
  sourceFileInput.value?.click()
}

const openFolderDialog = () => {
  sourceFolderInput.value?.click()
}

const onPageChange = (newPage: number) => {
  page.value = newPage
  tableRecord.value = sourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
}

const onPageSizeChange = (newPageSize: number) => {
  pageSize.value = newPageSize
  page.value = 1
  tableRecord.value = sourceFiles.value.slice(0, pageSize.value)
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
        const { isValid, errors } = await validateSnmpDataCollectionSourceFile(file)
        sourceFiles.value.push({
          file,
          isValid: isValid,
          errors: errors,
          isDuplicate: store.uploadedSourceNames.map(source => source.name.replace('.xml', '').toLowerCase()).includes(file.name.replace('.xml', '').toLowerCase())
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
    tableRecord.value = sourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
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
        if (isDuplicateFile(file.name, sourceFiles.value)) {
          continue
        }
        const isAlreadyUploaded = store.uploadedSourceNames
          .map(source => source.name.replace('.xml', '').toLowerCase())
          .includes(file.name.replace('.xml', '').toLowerCase())

        if (isAlreadyUploaded) {
          snackbar.showSnackBar({
            msg: `File ${file.name} has already been uploaded. Skipping.`,
            error: true
          })
          continue
        }
        const { isValid, errors } = await validateSnmpDataCollectionSourceFile(file)
        sourceFiles.value.push({
          file,
          isValid: isValid,
          errors: errors,
          isDuplicate: store.uploadedSourceNames.map(source => source.name.replace('.xml', '').toLowerCase()).includes(file.name.replace('.xml', '').toLowerCase())
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
    tableRecord.value = sourceFiles.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)

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
    const response = await uploadDataCollectionFiles(sourceFiles.value.filter(f => f.isValid).map(f => f.file))
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
    sourceFiles.value[selectedIndex.value] = {
      file: newFile,
      isValid: validationResult.isValid,
      errors: validationResult.errors,
      isDuplicate: store.uploadedSourceNames.map(source => source.name.replace('.xml', '').toLowerCase()).includes(newFileName.replace('.xml', '').toLowerCase())
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
    sourceFiles.value = sourceFiles.value.map(file => ({
      ...file,
      isDuplicate: newNames.map(source => source.name.replace('.xml', '').toLowerCase()).includes(file.file.name.replace('.xml', '').toLowerCase())
    }))
  }, { immediate: true, deep: true }
)

onMounted(async () => {
  await store.fetchAllSourcesNames()
})
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
      padding: 40px 0px 20px 0px;

      .section-left {
        display: flex;
        gap: 10px;

        input {
          display: none;
        }
      }

      .section-right {
        display: flex;
        gap: 10px;

        button {
          :deep(.spinner) {
            height: 1.5rem !important;
            width: 1.5rem !important;
          }
        }
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

