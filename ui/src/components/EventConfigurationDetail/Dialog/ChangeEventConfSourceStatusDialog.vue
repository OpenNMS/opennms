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
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const store = useEventConfigDetailStore()
const labels = {
  title: 'Change Event Configuration Source Status'
}

const getMessage = () => {
  if (store.changeEventConfigSourceStatusDialogState.eventConfigSource && store.changeEventConfigSourceStatusDialogState.eventConfigSource.enabled) {
    return `This will disable the event configuration source: <strong>${store.changeEventConfigSourceStatusDialogState.eventConfigSource.filename}</strong> and disable all events associated with it.`
  } else {
    return `This will enable the event configuration source: <strong>${store.changeEventConfigSourceStatusDialogState.eventConfigSource?.filename}</strong> and enable all events associated with it.`
  }
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

