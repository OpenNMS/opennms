<template>
  <div class="confirmation-dialog">
    <FeatherDialog
      v-model="isVisible"
      :labels="labels"
      hide-close
      @hidden="onClose"
    >
      <div class="modal-body">
        <slot name="content"></slot>
      </div>
      <template v-slot:footer>
        <FeatherButton @click="onClose">Cancel</FeatherButton>
        <FeatherButton
          primary
          @click="onAction"
        >
          {{ props.actionButtonText || 'OK' }}
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'

const props = defineProps({
  title: { required: false, type: String },
  actionButtonText: { required: false, type: String },
  visible: { required: true, type: Boolean }
})

const emit = defineEmits(['cancel', 'ok'])

const isVisible = ref(props.visible)

const labels = computed(() => {
  return {
    title: props.title
  }
})

const onClose = () => {
  isVisible.value = false
  emit('cancel')
}

const onAction = () => {
  isVisible.value = false
  emit('ok' )
}

watch ([() => props.visible], ([newVal]) => {
  isVisible.value = newVal
})
</script>

<style scoped lang="scss">
</style>
