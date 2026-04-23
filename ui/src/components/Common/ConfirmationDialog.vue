<template>
  <div class="confirmation-dialog">
    <FeatherDialog
      v-model="isVisible"
      :labels="labels"
      hide-close
      @hidden="onCancel"
    >
      <div class="modal-body" :style="{ maxWidth: props.maxWidth, maxHeight: props.maxHeight }">
        <slot name="content"></slot>
      </div>
      <template v-slot:footer>
        <FeatherButton
          primary
          @click="onAction"
        >
          {{ props.actionButtonText || 'OK' }}
        </FeatherButton>
        <FeatherButton @click="onCancel">{{ props.cancelButtonText || 'Cancel' }}</FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

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

const labels = computed(() => {
  return {
    title: props.title
  }
})

const onCancel = () => {
  isVisible.value = false
  emit('cancel')
}

const onAction = () => {
  isVisible.value = false
  emit('ok')
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
