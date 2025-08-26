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
          <strong>Note:</strong> This EventConf source has
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
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const store = useEventConfigStore()
const labels = {
  title: 'Delete EventConf Source'
}

const deleteEventConfigSource = async () => {
  try {
    // Call API to delete the event configuration source
    // await api.deleteEventConfigSource(store.deleteEventConfigSourceModalState.eventConfigSource.id);

    // After successful deletion, hide the modal and refresh the list
    store.hideDeleteEventConfigSourceModal()
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

