<template>
  <OnmsConfirmationDialog
    :visible="store.uploadedEventConfigFilesReportDialogState.visible"
    title="Upload Report"
    action-button-text="View Uploaded Files"
    cancel-button-text="Close"
    @cancel="closeDialog()"
    @ok="gotoViewTab()"
  >
    <template #content>
      <p
        class="status-line"
        data-test="report-status"
      >{{ getUploadReportStatus() }}</p>

      <ul
        v-if="summaryLines.length"
        class="summary-list"
        data-test="report-summary"
      >
        <li
          v-for="(line, index) in summaryLines"
          :key="'summary-' + index"
          :class="line.kind"
        >{{ line.text }}</li>
      </ul>

      <div
        v-if="errorEntries.length"
        class="error-section"
        data-test="report-errors"
      >
        <h4>Not uploaded:</h4>
        <ul>
          <li
            v-for="(file, index) in errorEntries"
            :key="'error-' + index"
          >
            <span class="text-danger">{{ file.file }}</span> &mdash; {{ file.error }}
          </li>
        </ul>
      </div>

      <div
        v-if="uploadedFiles.length"
        class="files-section"
      >
        <OnmsButton
          variant="text"
          size="small"
          data-test="toggle-file-list"
          @click="showFiles = !showFiles"
        >{{ showFiles ? 'Hide' : 'Show' }} the {{ uploadedFiles.length }} uploaded {{ uploadedFiles.length === 1 ? 'source' : 'sources' }}</OnmsButton>
        <div
          v-if="showFiles"
          class="upload-report-scroll"
          data-test="report-file-list"
        >
          <ul>
            <li
              v-for="(file, index) in uploadedFiles"
              :key="'success-' + index"
            >
              <span class="text-success">{{ file.file }}</span>
              <span
                v-if="file.eventCount !== undefined"
                class="event-count"
              > &mdash; {{ file.eventCount }} {{ file.eventCount === 1 ? 'event' : 'events' }}</span>
            </li>
          </ul>
        </div>
      </div>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsConfirmationDialog } from '@opennms/onms-ui'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'

const store = useEventConfigStore()

const props = defineProps<{
  report: EventConfigFilesUploadResponse
}>()

const showFiles = ref(false)

// each opening starts collapsed: at hundreds of files the list is reference, not reading
watch(() => store.uploadedEventConfigFilesReportDialogState.visible, () => {
  showFiles.value = false
})

// plain uploaded sources; entries carrying a message are steps (e.g. the applied source order)
const uploadedFiles = computed(() => (props.report.success ?? []).filter(entry => !entry.message))
const stepEntries = computed(() => (props.report.success ?? []).filter(entry => entry.message))
const errorEntries = computed(() => props.report.errors ?? [])

// the exceptions and the roll-up carry the information; 232 identical lines do not
const summaryLines = computed(() => {
  const lines: Array<{ kind: string; text: string }> = []
  const files = uploadedFiles.value
  if (files.length > 0) {
    const events = files.reduce((sum, file) => sum + (file.eventCount ?? 0), 0)
    const sources = files.length === 1 ? '1 source uploaded' : `${files.length} sources uploaded`
    lines.push({ kind: 'ok', text: events > 0 ? `${sources} · ${events} event definitions` : sources })
  }
  stepEntries.value.forEach((entry) => {
    lines.push({ kind: 'ok', text: entry.message as string })
  })
  if (errorEntries.value.length > 0) {
    lines.push({
      kind: 'bad',
      text: errorEntries.value.length === 1 ? '1 file was not uploaded' : `${errorEntries.value.length} files were not uploaded`
    })
  }
  return lines
})

const closeDialog = async () => {
  await store.fetchEventConfigs()
  store.uploadedEventConfigFilesReportDialogState.visible = false
}

const getUploadReportStatus = () => {
  const { success = [], errors = [] } = props.report

  if (success.length > 0 && errors.length === 0) {
    return 'All files uploaded successfully.'
  } else if (errors.length > 0 && success.length === 0) {
    return 'All files failed to upload.'
  } else if (success.length > 0 && errors.length > 0) {
    return 'Some files uploaded successfully, while others failed.'
  } else {
    return 'No files were uploaded.'
  }
}

const gotoViewTab = async () => {
  store.uploadedEventConfigFilesReportDialogState.visible = false
  await store.fetchEventConfigs()
  store.resetActiveTab()
}
</script>

<style scoped lang="scss">
.status-line {
  margin: 0 0 10px;
  font-weight: 600;
}

.summary-list {
  margin: 0 0 12px;
  padding-left: 1.2em;

  li {
    margin: 2px 0;
  }

  .ok {
    color: var(--onms-success, #0b720c);
  }

  .bad {
    color: var(--p-red-500);
  }
}

.error-section {
  margin-bottom: 12px;

  h4 {
    margin: 0 0 6px;
  }

  ul {
    margin: 0;
    padding-left: 1.2em;
  }
}

.text-danger {
  color: var(--p-red-500);
}

.text-success {
  color: var(--p-green-500);
}

.event-count {
  color: var(--p-text-muted-color);
}

.files-section {
  button {
    margin: 0 0 6px;
    padding-left: 0;
  }
}

.upload-report-scroll {
  max-height: 40vh;
  overflow-y: auto;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid var(--p-content-border-color);

  ul {
    margin: 0;
    padding-left: 1.2em;
  }
}

:deep(.p-dialog-content) {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
