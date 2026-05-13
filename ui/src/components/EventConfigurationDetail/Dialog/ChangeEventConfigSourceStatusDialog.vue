<template>
  <ConfirmationDialog
    :visible="store.changeEventConfigSourceStatusDialogState.visible"
    title="Change Event Configuration Source Status"
    action-button-text="Save"
    @cancel="store.hideChangeEventConfigSourceStatusDialog()"
    @ok="changeStatus()"
  >
    <template #content>
      <p v-html="getMessage()"></p>
      <p v-if="store.changeEventConfigSourceStatusDialogState.eventConfigSource?.vendor === VENDOR_OPENNMS">
        <strong>Note: Changing the status of an OpenNMS event configuration source may effect the OpenNMS system functionality. </strong>
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
const { showSnackBar } = useSnackbar()

const getMessage = () => {
  const isEnabled = store.changeEventConfigSourceStatusDialogState.eventConfigSource?.enabled
  const filename = store.changeEventConfigSourceStatusDialogState.eventConfigSource?.name || ''
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
      showSnackBar({ msg: 'No event configuration source selected', error: true })
    }
  } catch (error) {
    console.error('Error changing event configuration event status:', error)
    showSnackBar({ msg: 'Error changing event configuration source status', error: true })
  }
}
</script>

<style scoped lang="scss"></style>
