<template>
  <div class="upload-files-tab">
    <div class="upload-section">
      <div class="title-container">
        <h2>Upload Event Configuration Files</h2>
      </div>
      <div class="section">
        <div class="selected-files-section">
          <div v-if="eventFiles.length > 0">
            <Draggable
              v-model="eventFiles"
              item-key="value"
              handle=".drag-handle"
              class="columns-drag-container"
            >
              <template #item="{ element, index }">
                <div class="file">
                  <div class="file-icon">
                    <FeatherIcon :icon="Text" />
                    <span>
                      {{ element.file.name.length > 39 ? element.file.name.slice(0, 36) + "..." : element.file.name }}
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
                        @click="openFileRenameDialog(index)"
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
                      :title="element.errors.join('.\n ')"
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
            @change="handleEventConfUpload"
            data-test="event-conf-upload-input"
            ref="eventConfFileInput"
          />
          <FeatherButton
            @click="openFileDialog"
            :disabled="isLoading"
          >
            Choose files to upload
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
    </div>
    <EventConfigFilesUploadReportDialog :report="uploadFilesReport" />
    <UploadedFileRenameDialog
      :visible="displayRenameDialog"
      :fileBucket="eventFiles"
      :index="eventFiles.findIndex(f => f.isDuplicate)"
      :alreadyExistsNames="store.uploadedSourceNames"
      @close="closeRenameDialog"
      @rename="renameFile"
    />
  </div>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { uploadEventConfigFiles } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigFilesUploadResponse, UploadEventFileType } from '@/types/eventConfig'
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
import EventConfigFilesUploadReportDialog from './Dialog/EventConfigFilesUploadReportDialog.vue'
import { isDuplicateFile, MAX_FILES_UPLOAD, validateEventConfigFile } from './eventConfigXmlValidator'
import UploadedFileRenameDialog from './Dialog/UploadedFileRenameDialog.vue'

const eventConfFileInput = ref<HTMLInputElement | null>(null)
const uploadFilesReport = ref<EventConfigFilesUploadResponse>({} as EventConfigFilesUploadResponse)
const store = useEventConfigStore()
const isLoading = ref(false)
const snackbar = useSnackbar()
const eventFiles = ref<UploadEventFileType[]>([])
const displayRenameDialog = ref(false)
const selectedIndex = ref<number | null>(null)
const shouldUploadDisabled = computed(() => {
  return (
    eventFiles.value.length === 0 ||
    isLoading.value ||
    !eventFiles.value.every(f => f.isValid) ||
    eventFiles.value.some(f => f.isDuplicate)
  )
})

const handleEventConfUpload = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const files = Array.from(input.files)
    for (const file of files) {
      try {
        if (isDuplicateFile(file.name, eventFiles.value)) {
          continue
        }
        const validationResult = await validateEventConfigFile(file)
        if (eventFiles.value.length >= MAX_FILES_UPLOAD) {
          snackbar.showSnackBar({
            msg: `You can upload a maximum of ${MAX_FILES_UPLOAD} files at a time.`,
            error: true
          })
          break
        } else {
          eventFiles.value.push({
            file,
            isValid: validationResult.isValid,
            errors: validationResult.errors,
            isDuplicate: store.uploadedSourceNames.map(name => name.toLowerCase()).includes(file.name.toLowerCase()) || false
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

const openFileDialog = () => {
  eventConfFileInput.value?.click()
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
  if (selectedIndex.value !== null && selectedIndex.value >= 0 && selectedIndex.value < eventFiles.value.length) {
    const fileToRename = eventFiles.value[selectedIndex.value]
    const newFile = new File([fileToRename.file], newFileName, { type: fileToRename.file.type })
    const validationResult = await validateEventConfigFile(newFile)
    eventFiles.value[selectedIndex.value] = {
      file: newFile,
      isValid: validationResult.isValid,
      errors: validationResult.errors,
      isDuplicate: store.uploadedSourceNames.map(name => name.toLowerCase()).includes(newFileName.toLowerCase())
    }
    closeRenameDialog()
  } else {
    console.error('Invalid index for renaming file')
  }
}

const removeFile = (index: number) => {
  eventFiles.value.splice(index, 1)
}

const uploadFiles = async () => {
  if (eventFiles.value.length === 0) {
    console.warn('No files to upload')
    return
  }
  if (!eventFiles.value.every(f => f.file.name.endsWith('.events.xml'))) {
    console.error('All files must be XML files')
    return
  }
  isLoading.value = true
  try {
    const response = await uploadEventConfigFiles(eventFiles.value.filter(f => f.isValid).map(f => f.file))
    uploadFilesReport.value = {
      errors: [...response.errors],
      success: [...response.success]
    }
    isLoading.value = false
    eventFiles.value = []
    eventConfFileInput.value!.value = ''
    store.uploadedEventConfigFilesReportDialogState.visible = true
  } catch (err) {
    console.error(err)
    isLoading.value = false
  }
}
</script>

<style scoped lang="scss">
@use "@featherds/styles/themes/variables";
@import "@featherds/styles/themes/variables";

.upload-files-tab {
  background: var(variables.$surface);
  width: 100%;
  padding: 25px;
  border-radius: 5px;
  margin-top: 10px;

  .upload-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 20px;

    .section {
      display: flex;
      align-items: flex-start;
      gap: 10px;
    }

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
            color: var($error);
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

