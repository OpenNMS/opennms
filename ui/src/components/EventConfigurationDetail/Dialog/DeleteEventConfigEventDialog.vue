<template>
  <div class="delete-event-config-event-dialog">
    <FeatherDialog
      v-model="store.deleteEventConfigEventDialogState.visible"
      :labels="labels"
      hide-close
      @hidden="store.hideDeleteEventConfigEventDialog()"
    >
      <div class="modal-body">
        <p>
          This will delete the event configuration event:
          <strong>{{ store.deleteEventConfigEventDialogState.eventConfigEvent?.eventLabel }}</strong>
          with source name:
          <strong>{{ store.selectedSource?.name }}</strong>
        </p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="store.hideDeleteEventConfigEventDialog()"> Cancel </FeatherButton>
        <FeatherButton
          primary
          @click="deleteEventConfigEvent(store.deleteEventConfigEventDialogState?.eventConfigEvent?.id)"
        >
          Delete
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { deleteEventConfigEventBySourceId } from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const store = useEventConfigDetailStore()
const { showSnackBar } = useSnackbar()
const labels = {
  title: 'Delete Event Configuration Event'
}

const deleteEventConfigEvent = async (id?: number) => {
  if (!id || !store.selectedSource?.id) {
    showSnackBar({ msg: 'Missing source or event ID', error: true })
    return
  }

  try {
    const result = await deleteEventConfigEventBySourceId(store.selectedSource.id, [id])
    if (result) {
      store.hideDeleteEventConfigEventDialog()
      store.resetEventsPagination()
      await store.fetchEventsBySourceId()
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

