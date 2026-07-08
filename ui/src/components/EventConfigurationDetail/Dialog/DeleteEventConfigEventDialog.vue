<template>
  <ConfirmationDialog
    :visible="store.deleteEventConfigEventDialogState.visible"
    title="Delete Event Configuration Event"
    action-button-text="Delete"
    @cancel="store.hideDeleteEventConfigEventDialog()"
    @ok="deleteEventConfigEvent(store.deleteEventConfigEventDialogState?.eventConfigEvent?.id)"
  >
    <template #content>
      <p>
        This will delete the event configuration event:
        <strong>{{ store.deleteEventConfigEventDialogState.eventConfigEvent?.eventLabel }}</strong>
        with source name:
        <strong>{{ store.selectedSource?.name }}</strong>
      </p>
      <p><strong>Are you sure you want to proceed?</strong></p>
    </template>
  </ConfirmationDialog>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'

import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import useSnackbar from '@/composables/useSnackbar'
import { deleteEventConfigEventBySourceId } from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'

const store = useEventConfigDetailStore()
const router = useRouter()
const { showSnackBar } = useSnackbar()

const deleteEventConfigEvent = async (id?: number) => {
  if (!id || !store.selectedSource?.id) {
    showSnackBar({ msg: 'Missing source or event ID', error: true })
    return
  }

  try {
    const result = await deleteEventConfigEventBySourceId(store.selectedSource.id, [id])
    if (result) {
      showSnackBar({ msg: 'Event configuration event deleted successfully', error: false })
      store.resetEventConfigEvents()
      store.hideDeleteEventConfigEventDialog()
      if (store.selectedSource.eventCount === 0) {
        router.push({ name: 'Event Configuration' })
      } else {
        await store.fetchEventsBySourceId()
      }
    } else {
      showSnackBar({ msg: 'Failed to delete event configuration event', error: true })
    }
  } catch (error) {
    console.error('Error deleting event configuration event:', error)
    showSnackBar({ msg: 'Failed to delete event configuration event', error: true })
  }
}
</script>

<style scoped lang="scss"></style>
