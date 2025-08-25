<template>
  <div class="upload-files-tab">
    <div class="upload-section">
      <div class="title-container">
        <h2>Upload Event Configuration Files</h2>
      </div>
      <div class="section">
        <div class="selected-files-section">
          <div v-if="eventFiles.length > 0 || invalidFiles.length > 0">
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
                    <span>{{ element.name }}</span>
                  </div>
                  <div class="actions">
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
            <div
              v-for="(file, index) in invalidFiles"
              class="file"
              :key="file.name"
            >
              <div class="file-icon">
                <FeatherIcon :icon="Text" />
                <span class="invalid-text">{{ file.name }}</span>
              </div>
              <div class="actions">
                <FeatherTooltip :title="file.reason" v-slot="{ attrs, on }">
                  <FeatherIcon
                    :icon="Info"
                    v-bind="attrs"
                    v-on="on"
                    class="info-icon"
                  />
                </FeatherTooltip>
                <FeatherButton
                  icon="Trash"
                  @click="removeInvalidFile(index)"
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
    <EventConfigFilesUploadReportModal :report="uploadFilesReport" />
  </div>
</template>

<script setup lang="ts">
import { uploadEventConfigFiles } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigFilesUploadReponse } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherTooltip } from '@featherds/tooltip'
import { FeatherSpinner } from '@featherds/progress'
import Delete from '@featherds/icon/action/Delete'
import Text from '@featherds/icon/file/Text'
import Apps from '@featherds/icon/navigation/Apps'
import Info from '@featherds/icon/action/Info'
import Draggable from 'vuedraggable'
import EventConfigFilesUploadReportModal from './Modal/EventConfigFilesUploadReportModal.vue'
import { ref } from 'vue'

const eventConfFileInput = ref<HTMLInputElement | null>(null)
const uploadFilesReport = ref<EventConfigFilesUploadReponse>({} as EventConfigFilesUploadReponse)
const store = useEventConfigStore()
const eventFiles = ref<File[]>([])
const invalidFiles = ref<{ name: string; reason: string }[]>([])
const isLoading = ref(false)

const handleEventConfUpload = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const files = Array.from(input.files)

    for (const file of files) {
      if (
        eventFiles.value.find(f => f.name === file.name) ||
        invalidFiles.value.find(f => f.name === file.name)
      ) {
        continue
      }
      try {
        const text = await file.text()
        const parser = new DOMParser()
        const xmlDoc = parser.parseFromString(text, 'application/xml')
        if (xmlDoc.querySelector('parsererror')) {
          invalidFiles.value.push({ name: file.name, reason: 'Invalid XML format - file contains syntax errors' })
          continue
        }

        const eventsElement = xmlDoc.querySelector('events')
        const eventElements = xmlDoc.querySelectorAll('event')
        const validationErrors: string[] = []
        if (!eventsElement) {
          validationErrors.push('Missing <events> root element')
        } else {
          const xmlns = eventsElement.getAttribute('xmlns') || ''
          if (!xmlns.includes('opennms.org')) {
            validationErrors.push('Missing or invalid OpenNMS namespace in <events> element')
          }
        }

        if (eventElements.length === 0) {
          validationErrors.push('No <event> entries found within <events> element')
        }
        if (eventsElement && eventElements.length === 0) {
          const childElements = eventsElement.children
          if (childElements.length === 0) {
            validationErrors.push('Empty <events> element - no content found')
          } else {
            const childNames = Array.from(childElements).map(el => `<${el.tagName}>`).join(', ')
            validationErrors.push(`<events> element contains ${childNames} but no <event> elements`)
          }
        }
        if (eventElements.length > 0) {
          eventElements.forEach((event, idx) => {
            const uei = event.querySelector('uei')?.textContent?.trim()
            const label = event.querySelector('event-label')?.textContent?.trim()
            const severity = event.querySelector('severity')?.textContent?.trim()

            const eventErrors: string[] = []
            if (!uei) eventErrors.push('missing <uei>')
            if (!label) eventErrors.push('missing <event-label>')
            if (!severity) eventErrors.push('missing <severity>')
            
            if (eventErrors.length > 0) {
              validationErrors.push(`Event ${idx + 1}: ${eventErrors.join(', ')}`)
            }
          })
        }
        if (!file.name.endsWith('.events.xml') && !file.name.includes('event')) {
          validationErrors.push('File does not appear to be an event configuration file (expected .events.xml extension)')
        }
        if (text.trim().length === 0) {
          validationErrors.push('File is empty')
        }
        if (validationErrors.length > 0) {
          invalidFiles.value.push({
            name: file.name,
            reason: validationErrors.join('; ')
          })
        } else {
          eventFiles.value.push(file)
        }
        
      } catch (error) {
        console.error(`Error processing file ${file.name}:`, error)
        invalidFiles.value.push({ 
          name: file.name, 
          reason: `Error reading file content: ${error instanceof Error ? error.message : 'Unknown error'}` 
        })
      }
    }
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

const removeInvalidFile = (index: number) => {
  invalidFiles.value.splice(index, 1)
}

const uploadFiles = async () => {
  if (eventFiles.value.length === 0) {
    console.warn('No files to upload')
    return
  }
  if (!eventFiles.value.every(file => file.name.endsWith('.events.xml'))) {
    console.error('All files must be XML files')
    return
  }
  isLoading.value = true
  try {
    const response = await uploadEventConfigFiles(eventFiles.value)
    uploadFilesReport.value = {
      errors: [...response.errors],
      success: [...response.success],
      invalid: invalidFiles.value.map(f => ({
        file: f.name,
        reason: f.reason
      }))
    }
    isLoading.value = false
    eventFiles.value = [] 
    invalidFiles.value = []
    eventConfFileInput.value!.value = ''
    store.uploadedFilesReportModalState.visible = true
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

          .info-icon {
            color: var(variables.$error);
            cursor: pointer;
            height: 1.5em;
            width: 1.5em;
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