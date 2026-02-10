<template>
  <div class="delete-event-config-source-modal">
    <FeatherDialog
      v-model="isVisible"
      :labels="labels"
      hide-close
      @hidden="emit('close')"
    >
      <div class="modal-body">
        <p>
          This will delete the event configuration source:
          <strong>{{ props.eventConfigSource?.name }}</strong>
        </p>
        <p>
          <strong>Note:</strong> This event configuration source has
          <strong>{{ props.eventConfigSource?.eventCount }}</strong> events
          associated with it and will be deleted.
        </p>
        <p><strong>Are you sure you want to proceed?</strong></p>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="emit('close')"> Cancel </FeatherButton>
        <FeatherButton
          primary
          @click="confirmDelete"
        >
          Delete
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const props = defineProps<{
    visible: boolean

}>()

const emit = defineEmits<{
    (e: 'close'): void
}>()

const isVisible = ref(props.visible)

watch(() => props.visible, (newVal) => {
    isVisible.value = newVal
})

watch(isVisible, (newVal) => {
    if (!newVal) {
        emit('close')
    }
})

const confirmDelete = () => {
    emit('close')
}
</script>

<style lang="scss" scoped></style>

