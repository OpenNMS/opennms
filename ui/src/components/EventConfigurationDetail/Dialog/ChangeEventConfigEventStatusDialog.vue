<template>
  <div class="change-event-conf-event-status-dialog">
    <FeatherDialog
      v-model="store.changeEventConfigEventStatusDialogState.visible"
      :labels="labels"
      hide-close
      @hidden="store.hideChangeEventConfigEventStatusDialog()"
    >
      <div class="modal-body">
        <p v-html="getMessage()"></p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="store.hideChangeEventConfigEventStatusDialog()"> Cancel </FeatherButton>
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
  title: 'Change Event Configuration Event Status'
}

const getMessage = () => {
  const isEnabled = store.changeEventConfigEventStatusDialogState.eventConfigEvent?.enabled
  const eventLabel = store.changeEventConfigEventStatusDialogState.eventConfigEvent?.eventLabel || ''
  const sourceName = store.selectedSource?.name || ''
  const action = isEnabled ? 'disable' : 'enable'
  return `This will ${action} the event configuration event: <strong>${eventLabel}</strong> with source name: <strong>${sourceName}</strong>.`
}

const changeStatus = async () => {
  try {
    if (store.changeEventConfigEventStatusDialogState.eventConfigEvent) {
      const eventId = store.changeEventConfigEventStatusDialogState.eventConfigEvent.id
      if (store.changeEventConfigEventStatusDialogState.eventConfigEvent.enabled) {
        await store.disableEventConfigEvent(eventId)
      } else {
        await store.enableEventConfigEvent(eventId)
      }
      await store.hideChangeEventConfigEventStatusDialog()
    } else {
      console.error('No event configuration event selected')
    }
  } catch (error) {
    console.error('Error changing event configuration event status:', error)
  }
}
</script>

<style scoped lang="scss"></style>

