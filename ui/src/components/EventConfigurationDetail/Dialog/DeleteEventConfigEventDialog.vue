<template>
  <div class="delete-event-config-source-dialog">
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
          <strong>{{ store.selectedSource?.filename }}</strong>
        </p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="store.hideDeleteEventConfigEventDialog()"> Cancel </FeatherButton>
        <FeatherButton
          primary
          @click="deleteEventConfigEvent()"
        >
          Delete
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const store = useEventConfigDetailStore()
const labels = {
  title: 'Delete Event Configuration Event'
}

const deleteEventConfigEvent = async () => {
  try {
    // TODO: Call API to delete the event configuration source
    // await api.deleteEventConfigSource(store.deleteEventConfigSourceModalState.eventConfigSource.id);

    // After successful deletion, hide the modal and refresh the list
    store.hideDeleteEventConfigEventDialog()
    store.resetEventsPagination()
    await store.fetchEventsBySourceId()
  } catch (error) {
    console.error('Error deleting event configuration source:', error)
  }
}
</script>

<style scoped lang="scss">
.delete-event-config-source-dialog {
  .modal-body {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
}
</style>

