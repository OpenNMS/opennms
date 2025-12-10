<template>
  <FeatherDialog
    v-model="store.createEventConfigSourceDialogState.visible"
    :labels="labels"
    hide-close
    @hidden="store.hideCreateEventConfigSourceDialog()"
  >
    <div class="modal-body">
      <div>
        <FeatherInput
          label="Event Configuration Source Name"
          v-model="configName"
          :error="error"
        />
      </div>
      <div>
        <p>
          Please note that this source will be created with 0 event configurations. You can add event configurations after creation.
        </p>
      </div>
    </div>
    <template v-slot:footer>
      <FeatherButton @click="store.hideCreateEventConfigSourceDialog()"> Cancel </FeatherButton>
      <FeatherButton
        primary
        @click="handleSave"
        :disabled="!!error"
      >
        Create
      </FeatherButton>
    </template>
  </FeatherDialog>
</template>

<script lang="ts" setup>
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'

const configName = ref('')
const error = computed(() => {
  return configName.value.trim() === '' ? 'Event Configuration Source Name is required' : ''
})
const store = useEventConfigStore()
const labels = {
  title: 'Create Event Configuration Source'
}

const resetForm = () => {
  configName.value = ''
}

const handleSave = () => {
  if (configName.value.trim() === '') {
    return
  }
  // Emit create event with configName
  // You can handle the actual creation logic in the parent component
  resetForm()
  store.hideCreateEventConfigSourceDialog()
}
</script>

<style lang="scss" scoped></style>

