<template>
  <div class="message-dialog">
    <OnmsDialog
      v-model:visible="isVisible"
      :header="props.title"
      :modal="true"
      :closable="false"
      :appendTo="props.relative ? 'self' : 'body'"
      :unsafePt="props.relative ? relativePt : undefined"
      @hide="onHide"
    >
      <div class="modal-body" :style="{ maxWidth: props.maxWidth, maxHeight: props.maxHeight }">
        <slot name="content"></slot>
      </div>
      <template #footer>
        <OnmsButton
          :label="props.actionButtonText || 'Close'"
          @click="onClose"
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
  actionButtonText: { required: false, type: String },
  title: { required: false, type: String },
  visible: { required: true, type: Boolean },
  relative: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])

const isVisible = ref(props.visible)

// When `relative`, render the dialog inside this component's DOM and scope the
// modal mask to the nearest positioned ancestor instead of the viewport, so the
// dialog appears within its container (e.g. a drawer) rather than full-screen —
// matching FeatherDialog's `relative` behavior.
const relativePt = {
  mask: { style: 'position: absolute' }
}

// Tracks whether the Close button already resolved the dialog, so the Dialog's
// `hide` event (which also fires on programmatic close) does not emit `close`
// twice. `hide` with no prior resolution means the user dismissed it (Esc).
let resolved = false

const onClose = () => {
  resolved = true
  isVisible.value = false
  emit('close')
}

const onHide = () => {
  if (!resolved) {
    emit('close')
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
