<template>
  <div class="delete-event-config-source-modal">
    <FeatherDialog
      v-model="store.deleteEventConfigSourceModalState.visible"
      :labels="labels"
      hide-close
      @hidden="store.hideDeleteEventConfigSourceModal()"
    >
      <div class="modal-body">
        <p>
          This will delete the event configuration source:
          <strong>{{ store.deleteEventConfigSourceModalState.eventConfigSource?.filename }}</strong>
        </p>
        <p>
          <strong>Note:</strong> This event configuration source has
          <strong>{{ store.deleteEventConfigSourceModalState.eventConfigSource?.eventCount }}</strong> events associated
          with it and will be deleted.
        </p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="store.hideDeleteEventConfigSourceModal()"> Cancel </FeatherButton>
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
import { deleteEventConfigSourceById } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const store = useEventConfigStore()
const labels = {
  title: 'Delete Event Configuration Source'
}

const deleteEventConfigSource = async () => {
  if (store.deleteEventConfigSourceModalState.eventConfigSource === null) {
    return
  }
  try {
    const response = await deleteEventConfigSourceById(store.deleteEventConfigSourceModalState.eventConfigSource.id)
    if (!response) {
      console.error('Failed to delete event configuration source')
      return
    }
    store.hideDeleteEventConfigSourceModal()
    store.resetSourcesPagination()
    await store.fetchEventConfigs()
  } catch (error) {
    console.error('Error deleting event configuration source:', error)
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

