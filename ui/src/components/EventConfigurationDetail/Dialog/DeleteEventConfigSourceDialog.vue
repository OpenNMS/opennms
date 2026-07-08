<template>
  <ConfirmationDialog
    :visible="store.deleteEventConfigSourceDialogState.visible"
    title="Delete Event Configuration Source"
    action-button-text="Delete"
    @cancel="store.hideDeleteEventConfigSourceDialog()"
    @ok="deleteEventConfigSource()"
  >
    <template #content>
      <p>
        This will delete the event configuration source:
        <strong>{{ store.deleteEventConfigSourceDialogState.eventConfigSource?.name }}</strong>
      </p>
      <p>
        <strong>Note:</strong> This event configuration source has
        <strong>{{ store.deleteEventConfigSourceDialogState.eventConfigSource?.eventCount }}</strong> events
        associated with it and will be deleted.
      </p>
      <p><strong>Are you sure you want to proceed?</strong></p>
    </template>
  </ConfirmationDialog>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'

import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import useSnackbar from '@/composables/useSnackbar'
import { deleteEventConfigSourceById } from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'

const store = useEventConfigDetailStore()
const { showSnackBar } = useSnackbar()
const router = useRouter()

const deleteEventConfigSource = async () => {
  if (store.deleteEventConfigSourceDialogState.eventConfigSource === null) {
    return
  }
  try {
    const response = await deleteEventConfigSourceById(store.deleteEventConfigSourceDialogState.eventConfigSource.id)
    if (!response) {
      console.error('Failed to delete event configuration source')
      showSnackBar({ msg: 'Failed to delete event configuration source', error: true })
      return
    }
    store.hideDeleteEventConfigSourceDialog()
    router.push({ name: 'Event Configuration' })
  } catch (error) {
    console.error('Error deleting event configuration source:', error)
    showSnackBar({ msg: 'Failed to delete event configuration source', error: true })
  }
}
</script>

<style scoped lang="scss"></style>
