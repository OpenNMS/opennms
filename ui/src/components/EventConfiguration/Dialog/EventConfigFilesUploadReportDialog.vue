<template>
  <ConfirmationDialog
    :visible="store.uploadedEventConfigFilesReportDialogState.visible"
    title="Upload Report"
    action-button-text="View Uploaded Files"
    cancel-button-text="Close"
    @cancel="closeDialog()"
    @ok="gotoViewTab()"
  >
    <template #content>
      <h4>Message:</h4>
      <p>{{ getUploadReportStatus() }}</p>
      <h4>Details:</h4>
      <div class="upload-report-scroll">
        <ul>
          <li
            v-for="(file, index) in report.success"
            :key="'success-' + index"
          >
            <span class="text-success">{{ file.file }}</span> - Successfully uploaded
          </li>
          <li
            v-for="(file, index) in report.errors"
            :key="'error-' + index"
          >
            <span class="text-danger">{{ file.file }}</span> - Failed to upload
          </li>
        </ul>
      </div>
    </template>
  </ConfirmationDialog>
</template>

<script setup lang="ts">
import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'

const store = useEventConfigStore()

const props = defineProps<{
  report: EventConfigFilesUploadResponse
}>()

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
.text-danger {
  color: var(--p-red-500);
}

.text-success {
  color: var(--p-green-500);
}

.upload-report-scroll {
  max-height: 50vh;
  overflow-y: auto;
  padding: 10px;
  margin-top: 8px;
  border-radius: 8px;
  border: 1px solid var(--p-content-border-color);
}

:deep(.p-dialog-content) {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
