<template>
  <OnmsConfirmationDialog
    :visible="store.moveEventConfigEventDialogState.visible"
    title="Move Event"
    action-button-text="Move"
    @cancel="store.hideMoveEventConfigEventDialog()"
    @ok="moveEvent()"
  >
    <template #content>
      <p>
        Move <strong>{{ store.moveEventConfigEventDialogState.eventConfigEvent?.eventLabel }}</strong>
        to a new position within this source.
      </p>
      <FormField
        label="Position"
        :for="positionInputId"
        :hint="`1 = evaluated first · currently at position ${currentPosition} of ${store.eventsPagination.total}`"
      >
        <OnmsInputNumber
          v-model="positionValue"
          :input-id="positionInputId"
          :min="1"
          :max="store.eventsPagination.total"
          showButtons
          data-test="position-input"
        />
      </FormField>
    </template>
  </OnmsConfirmationDialog>
</template>

<script lang="ts" setup>
import { computed, ref, useId, watch } from 'vue'

import FormField from '@/components/Common/FormField.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { OnmsConfirmationDialog, OnmsInputNumber } from '@opennms/onms-ui'

const store = useEventConfigDetailStore()
const snackbar = useSnackbar()
const positionInputId = useId()

const positionValue = ref<number>(1)
const currentPosition = computed(() => store.moveEventConfigEventDialogState.eventConfigEvent?.eventOrder ?? 1)

watch(() => store.moveEventConfigEventDialogState.visible, (visible) => {
  if (visible) {
    positionValue.value = currentPosition.value
  }
})

const moveEvent = async () => {
  const eventConfigEvent = store.moveEventConfigEventDialogState.eventConfigEvent
  if (!eventConfigEvent) {
    console.error('No event configuration event selected')
    snackbar.showSnackBar({ msg: 'No event configuration event selected', error: true })
    return
  }
  const result = await store.moveEventConfigEvent(eventConfigEvent.id, {
    mode: 'position',
    position: positionValue.value
  })
  if (result.ok) {
    snackbar.showSnackBar({ msg: `Moved "${eventConfigEvent.eventLabel}" to position ${result.eventOrder}` })
  } else {
    snackbar.showSnackBar({ msg: result.message || 'Failed to move the event', error: true })
  }
  // The confirmation dialog closes itself on ok, so the store's visibility flag follows either way
  store.hideMoveEventConfigEventDialog()
}
</script>

<style scoped lang="scss"></style>
