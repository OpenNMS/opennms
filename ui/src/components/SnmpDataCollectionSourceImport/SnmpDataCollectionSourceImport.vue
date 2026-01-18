<template>
  <TableCard class="card">
    <div class="title">
      <h2>Selected Files</h2>
    </div>
    <div class="section">
      <div class="selected-files-section">
        <div v-if="sourceFiles.length > 0">
          <Draggable
            v-model="sourceFiles"
            item-key="value"
            handle=".drag-handle"
            class="columns-drag-container"
          >
            <template #item="{ element, index }">
              <div class="file">
                <div class="file-icon">
                  <FeatherIcon :icon="Text" />
                  <span>
                    {{ ellipsify(element.file.name, 39) }}
                  </span>
                </div>
                <div class="actions">
                  <FeatherTooltip
                    v-if="element.isDuplicate"
                    :title="'File is a duplicate of another file that has been already uploaded.'"
                    v-slot="{ attrs, on }"
                  >
                    <FeatherIcon
                      :icon="Warning"
                      v-bind="attrs"
                      v-on="on"
                      class="warning-icon"
                    />
                  </FeatherTooltip>
                  <FeatherTooltip
                    v-if="element.isValid && !element.isDuplicate"
                    :title="'File is valid'"
                    v-slot="{ attrs, on }"
                  >
                    <FeatherIcon
                      :icon="CheckCircle"
                      v-bind="attrs"
                      v-on="on"
                      class="success-icon"
                    />
                  </FeatherTooltip>
                  <FeatherTooltip
                    v-if="!element.isValid"
                    :title="element.errors.map((error: string) => `${error}. `).join('\n')"
                    v-slot="{ attrs, on }"
                  >
                    <FeatherIcon
                      :icon="Error"
                      v-bind="attrs"
                      v-on="on"
                      class="error-icon"
                    />
                  </FeatherTooltip>
                  <FeatherButton
                    icon="Apps"
                    text
                  >
                    <FeatherIcon
                      class="close-icon drag-handle"
                      :icon="Apps"
                    />
                  </FeatherButton>
                  <FeatherButton
                    icon="Trash"
                    data-test="remove-files-button"
                    @click="removeFile(index)"
                  >
                    <FeatherIcon :icon="Delete" />
                  </FeatherButton>
                </div>
              </div>
            </template>
          </Draggable>
        </div>
        <div v-else>
          <p>No files selected</p>
        </div>
      </div>
      <div class="upload-action-section">
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
          @click="openFileDialog"
          :disabled="isLoading"
        >
          Choose files to upload
        </FeatherButton>
        <FeatherButton
          @click="openFolderDialog"
          :disabled="isLoading"
        >
          Choose folder to upload
        </FeatherButton>
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
    <DataCollectionFilesUploadReportDialog
      :report="uploadFilesReport"
      :dialogVisible="uploadedDataCollectionFilesReportDialogState"
      @close="closeUploadReportDialog"
      @view="gotoViewTab"
    />
  </TableCard>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { ellipsify } from '@/lib/utils'
import { SnmpDataCollectionSourceUploadResponse, UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import Delete from '@featherds/icon/action/Delete'
import Text from '@featherds/icon/file/Text'
import Apps from '@featherds/icon/navigation/Apps'
import Error from '@featherds/icon/notification/Error'
import Warning from '@featherds/icon/notification/Warning'
import { FeatherSpinner } from '@featherds/progress'
import { FeatherTooltip } from '@featherds/tooltip'
import Draggable from 'vuedraggable'
import TableCard from '../Common/TableCard.vue'
import { isDuplicateFile, validateSnmpDataCollectionSourceFile } from './snmpDataCollectionSourceXmlValidator'
import { uploadDataCollectionFiles } from '@/services/snmpDataCollectionService'
import DataCollectionFilesUploadReportDialog from './Dialog/DataCollectionFilesUploadReportDialog.vue'

const sourceFolderInput = ref<HTMLInputElement | null>(null)
const sourceFileInput = ref<HTMLInputElement | null>(null)
const uploadFilesReport = ref<SnmpDataCollectionSourceUploadResponse>({} as SnmpDataCollectionSourceUploadResponse)
const sourceFiles = ref<UploadSnmpDataCollectionFileType[]>([])
const isLoading = ref(false)
const snackbar = useSnackbar()
const router = useRouter()
const uploadedDataCollectionFilesReportDialogState = ref(false)
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
}

const openFileDialog = () => {
  sourceFileInput.value?.click()
}

const openFolderDialog = () => {
  sourceFolderInput.value?.click()
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
          isDuplicate: isDuplicateFile(file.name, sourceFiles.value)
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
        const { isValid, errors } = await validateSnmpDataCollectionSourceFile(file)
        sourceFiles.value.push({
          file,
          isValid: isValid,
          errors: errors,
          isDuplicate: isDuplicateFile(file.name, sourceFiles.value)
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
    isLoading.value = false
    sourceFiles.value = []
    sourceFileInput.value!.value = ''
    uploadedDataCollectionFilesReportDialogState.value = true
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
  uploadedDataCollectionFilesReportDialogState.value = false
  router.push({ name: 'SNMP Data Collection' })
}
</script>

<style scoped lang="scss">
@use "@featherds/styles/themes/variables";

.card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;

  .section {
    display: flex;
    align-items: flex-start;
    gap: 10px;

    .selected-files-section {
      border: 1px solid var(variables.$border-on-surface);
      border-radius: 5px;
      padding: 10px;
      width: 500px;
      height: 500px;
      overflow: auto;

      .file {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px;
        border-bottom: 1px solid var(variables.$border-on-surface);
        margin-bottom: 5px;

        .file-icon {
          display: flex;
          align-items: center;
          gap: 10px;

          svg {
            font-size: 1.5rem;
          }

          span {
            font-size: 1rem;
          }

          .invalid-text {
            color: var(variables.$error);
          }
        }

        .actions {
          display: flex;
          align-items: center;
          gap: 10px;

          button {
            margin: 0px;
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
      }
    }

    .upload-action-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;

      input {
        display: none;
      }

      button {
        width: 100%;
        margin-left: 0;

        :deep(.spinner) {
          height: 1.5rem !important;
          width: 1.5rem !important;
        }
      }
    }
  }
}
</style>

