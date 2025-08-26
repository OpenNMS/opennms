<template>
  <FeatherDialog
    v-model="store.uploadedFilesReportModalState.visible"
    :labels="{ title: 'Upload Report', close: 'Close' }"
    hide-close
    @hidden="closeDialog"
  >
    <div>
      <h4>Message:</h4>
      <p>{{ getUploadReportStatus() }}</p>
      <h4>Details:</h4>
      <ul>
        <li
          v-for="(file, index) in report.success"
          :key="index"
        >
          <span class="text-success">{{ file.file }}</span> - Successfully uploaded
        </li>
        <li
          v-for="(file, index) in report.errors"
          :key="index"
        >
          <span class="text-danger">{{ file.file }}</span> - Failed to upload
        </li>
      </ul>
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
import { EventConfigFilesUploadReponse } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const store = useEventConfigStore()

const props = defineProps<{
  report: EventConfigFilesUploadReponse
}>()

const closeDialog = () => {
  store.uploadedFilesReportModalState.visible = false
}

const getUploadReportStatus = () => {
  const { success = [], errors = [] } = props.report

  if (success.length > 0 && errors.length > 0) {
    return 'Some files were successfully uploaded, while others failed.'
  } else if (success.length > 0 && errors.length === 0) {
    return 'All files were successfully uploaded.'
  } else if (errors.length > 0 && success.length === 0) {
    return 'All files failed to upload.'
  } else {
    return 'No files were uploaded.'
  }
}

const gotoViewTab = () => {
  store.uploadedFilesReportModalState.visible = false
  store.resetActiveTab()
}
</script>

<style scoped lang="scss"></style>

