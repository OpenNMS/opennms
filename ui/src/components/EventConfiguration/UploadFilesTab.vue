<template>
  <div class="upload-files-tab">
    <div class="upload-section">
      <div class="title-container">
        <h2>Upload Event Configuration Files</h2>
      </div>
      <div class="section">
        <div class="selected-files-section">
          <div v-if="eventFiles.length > 0">
            <div
              v-for="(file, index) in eventFiles"
              class="file"
              :key="file.name"
            >
              <div class="file-icon">
                <FeatherIcon :icon="Text" />
                <span>{{ file.name }}</span>
              </div>
              <div class="remove">
                <FeatherButton
                  icon="Trash"
                  data-test="remove-files-button"
                  @click="removeFile(index)"
                >
                  <FeatherIcon :icon="Delete" />
                </FeatherButton>
              </div>
            </div>
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
            :disabled="eventFiles.length === 0 || isLoading"
            @click="uploadFiles"
            data-test="upload-button"
          >
            <FeatherSpinner v-if="isLoading" />
            <span v-else>Upload Files</span>
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { uploadEventConfigFiles } from '@/services/eventConfigService'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Text from '@featherds/icon/file/Text'
import { FeatherSpinner } from '@featherds/progress'

const eventConfFileInput = ref<HTMLInputElement | null>(null)
const eventFiles = ref<File[]>([])
const isLoading = ref(false)

const handleEventConfUpload = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    eventFiles.value = Array.from(new Set([...eventFiles.value, ...Array.from(input.files)]))    // You can add further processing of the file here, like uploading it to a server
  } else {
    console.warn('No files selected')
  }
}

const openFileDialog = () => {
  eventConfFileInput.value?.click()
}

const removeFile = (index: number) => {
  eventFiles.value.splice(index, 1)
}

const uploadFiles = async () => {
  // You can add further processing of the files here, like uploading them to a server
  if (eventFiles.value.length === 0) {
    console.warn('No files to upload')
    return
  }
  if (!eventFiles.value.every(file => file.name.endsWith('.xml'))) {
    console.error('All files must be XML files')
    return
  }
  isLoading.value = true
  try {
    await uploadEventConfigFiles(eventFiles.value)
  } catch (err) {
    console.error(err)
  } finally {
    isLoading.value = false
    eventFiles.value = [] // Clear the files after upload
    eventConfFileInput.value!.value = '' // Reset the input field
  }
}
</script>

<style scoped lang="scss">
@use "@featherds/styles/themes/variables";

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

