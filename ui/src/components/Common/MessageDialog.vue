<template>
  <div class="message-dialog">
    <FeatherDialog
      v-model="isVisible"
      :labels="labels"
      :relative="props.relative"
      hide-close
      @hidden="onClose"
    >
      <div class="modal-body" :style="{ maxWidth: props.maxWidth, maxHeight: props.maxHeight }">
        <slot name="content"></slot>
      </div>
      <template v-slot:footer>
        <FeatherButton
          primary
          @click="onClose">
          {{ props.actionButtonText || 'Close' }}
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'

import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

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

const labels = computed(() => {
  return {
    title: props.title
  }
})

const onClose = () => {
  isVisible.value = false
  emit('close')
}

watch ([() => props.visible], ([newVal]) => {
  isVisible.value = newVal
})
</script>

<style scoped lang="scss">
  .modal-body {
    overflow: auto;
    word-break: break-word;
  }
</style>
