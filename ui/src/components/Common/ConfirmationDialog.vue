<template>
  <div class="confirmation-dialog">
    <OnmsDialog
      v-model:visible="isVisible"
      :header="props.title"
      :modal="true"
      :closable="false"
      @hide="onHide"
    >
      <div class="modal-body" :style="{ maxWidth: props.maxWidth, maxHeight: props.maxHeight }">
        <slot name="content"></slot>
      </div>
      <template #footer>
        <OnmsButton
          :label="props.actionButtonText || 'OK'"
          @click="onAction"
        />
        <OnmsButton
          variant="text"
          :label="props.cancelButtonText || 'Cancel'"
          @click="onCancel"
        />
      </template>
    </OnmsDialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'

const props = defineProps({
  maxHeight: { type: String, default: '20em' },
  maxWidth: { type: String, default: '50em' },
  title: { required: false, type: String },
  actionButtonText: { required: false, type: String },
  cancelButtonText: { required: false, type: String },
  visible: { required: true, type: Boolean }
})

const emit = defineEmits(['cancel', 'ok'])

const isVisible = ref(props.visible)

// Tracks whether a footer button already resolved the dialog, so the Dialog's
// `hide` event (which also fires on programmatic close) does not emit a second,
// spurious `cancel`. `hide` with no prior resolution means the user dismissed
// the dialog (Esc), which maps to `cancel`.
let resolved = false

const onAction = () => {
  resolved = true
  isVisible.value = false
  emit('ok')
}

const onCancel = () => {
  resolved = true
  isVisible.value = false
  emit('cancel')
}

const onHide = () => {
  if (!resolved) {
    emit('cancel')
  }
  resolved = false
}

watch(() => props.visible, (newVal) => {
  isVisible.value = newVal
  if (newVal) {
    resolved = false
  }
})
</script>

<style scoped lang="scss">
  .modal-body {
    overflow: auto;
    word-break: break-word;
  }
</style>
