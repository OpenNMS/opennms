<template>
  <ConfirmationDialog
    :visible="store.changeEventConfigEventStatusDialogState.visible"
    title="Change Event Configuration Event Status"
    action-button-text="Save"
    @cancel="store.hideChangeEventConfigEventStatusDialog()"
    @ok="changeStatus()"
  >
    <template #content>
      <p v-html="getMessage()"></p>
      <p v-if="store.changeEventConfigEventStatusDialogState.eventConfigEvent?.vendor === VENDOR_OPENNMS">
        <strong
          >Note: Changing the status of an OpenNMS event configuration event may effect the OpenNMS system
          functionality.
        </strong>
      </p>
      <p><strong>Are you sure you want to proceed?</strong></p>
    </template>
  </ConfirmationDialog>
</template>

<script lang="ts" setup>
import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import useSnackbar from '@/composables/useSnackbar'
import { VENDOR_OPENNMS } from '@/lib/utils'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'

const store = useEventConfigDetailStore()
const snackbar = useSnackbar()

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
      snackbar.showSnackBar({ msg: 'No event configuration event selected', error: true })
    }
  } catch (error) {
    console.error('Error changing event configuration event status:', error)
    snackbar.showSnackBar({ msg: 'Failed to change event configuration event status', error: true })
  }
}
</script>

<style scoped lang="scss"></style>
