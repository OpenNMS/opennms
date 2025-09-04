<template>
  <div class="delete-event-config-source-modal">
    <FeatherDialog
      v-model="store.deleteEventConfigSourceDialogState.visible"
      :labels="labels"
      hide-close
      @hidden="store.hideDeleteEventConfigSourceDialog()"
    >
      <div class="modal-body">
        <p>
          This will delete the event configuration source:
          <strong>{{ store.deleteEventConfigSourceDialogState.eventConfigSource?.filename }}</strong>
        </p>
        <p>
          <strong>Note:</strong> This event configuration source has
          <strong>{{ store.deleteEventConfigSourceDialogState.eventConfigSource?.eventCount }}</strong> events
          associated with it and will be deleted.
        </p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="store.hideDeleteEventConfigSourceDialog()"> Cancel </FeatherButton>
        <FeatherButton
          primary
          @click="deleteEventConfigSource()"
        >
          Delete
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { deleteEventConfigSourceById } from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const store = useEventConfigDetailStore()
const { showSnackBar } = useSnackbar()
const router = useRouter()
const labels = {
  title: 'Delete Event Configuration Source'
}

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
    router.push({ name: 'EventConfiguration' })
  } catch (error) {
    console.error('Error deleting event configuration source:', error)
    showSnackBar({ msg: 'Failed to delete event configuration source', error: true })
  }
}
</script>

<style scoped lang="scss">
.delete-event-config-source-modal {
  .modal-body {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
}
</style>

