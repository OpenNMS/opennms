<template>
  <FeatherDialog
    v-model="store.uploadedEventConfigFilesReportDialogState.visible"
    :labels="{ title: 'Upload Report', close: 'Close' }"
    hide-close
    @hidden="closeDialog"
  >
    <div>
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
    </div>
    <template v-slot:footer>
      <FeatherButton @click="closeDialog"> Close </FeatherButton>
      <FeatherButton
        primary
        @click="gotoViewTab"
      >
        View Uploaded Files
      </FeatherButton>
    </template>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/typography";

.text-danger {
  color: var($error);
}
.upload-report-scroll {
  max-height: 50vh;
  overflow-y: auto;
  padding: 10px;
  margin-top: 8px;
  border-radius: 8px;
  border: 1px solid var(variables.$border-on-surface);
}

:deep(.feather-dialog-content) {
  max-height: 70vh; 
  overflow-y: auto;
}

.text-danger {
  color: var($error);
}

.text-success {
  color: var($success);
}
</style>

