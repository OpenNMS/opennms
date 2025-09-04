<template>
  <div class="change-event-conf-event-status-dialog">
    <FeatherDialog
      v-model="store.changeEventConfigSourceStatusDialogState.visible"
      :labels="labels"
      hide-close
      @hidden="store.hideChangeEventConfigSourceStatusDialog()"
    >
      <div class="modal-body">
        <p v-html="getMessage()"></p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="store.hideChangeEventConfigSourceStatusDialog()"> Cancel </FeatherButton>
        <FeatherButton
          primary
          @click="changeStatus()"
        >
          Save
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
  title: 'Change Event Configuration Source Status'
}

const getMessage = () => {
  const isEnabled = store.changeEventConfigSourceStatusDialogState.eventConfigSource?.enabled
  const filename = store.changeEventConfigSourceStatusDialogState.eventConfigSource?.filename || ''
  const action = isEnabled ? 'disable' : 'enable'
  return `This will ${action} the event configuration source: <strong>${filename}</strong> and ${action} all events associated with it.`
}

const changeStatus = async () => {
  try {
    if (store.changeEventConfigSourceStatusDialogState.eventConfigSource) {
      const sourceId = store.changeEventConfigSourceStatusDialogState.eventConfigSource.id
      if (store.changeEventConfigSourceStatusDialogState.eventConfigSource.enabled) {
        await store.disableEventConfigSource(sourceId)
      } else {
        await store.enableEventConfigSource(sourceId)
      }
      store.hideChangeEventConfigSourceStatusDialog()
    } else {
      console.error('No event configuration event selected')
    }
  } catch (error) {
    console.error('Error changing event configuration event status:', error)
  }
}
</script>

<style scoped lang="scss"></style>

