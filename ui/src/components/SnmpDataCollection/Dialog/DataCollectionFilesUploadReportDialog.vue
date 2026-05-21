<template>
  <FeatherDialog
    v-model="isDialogVisible"
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
import { computed } from 'vue'
import { EventConfigFilesUploadResponse } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const props = defineProps<{
  report: EventConfigFilesUploadResponse,
  dialogVisible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void,
  (e: 'view'): void
}>()

const isDialogVisible = computed({
  get: () => props.dialogVisible,
  set: () => emit('close')
})

const closeDialog = async () => {
  emit('close')
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
  emit('view')
}
</script>

<style scoped lang="scss">
@use "@featherds/styles/themes/variables";

.text-danger {
  color: var(variables.$error);
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
  color: var(variables.$error);
}

.text-success {
  color: var(variables.$success);
}
</style>

