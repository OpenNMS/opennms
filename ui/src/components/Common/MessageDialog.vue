<template>
  <div class="message-dialog">
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
        <FeatherButton
          primary
          @click="onClose">
          Close
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
  visible: { required: true, type: Boolean }
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
</style>
