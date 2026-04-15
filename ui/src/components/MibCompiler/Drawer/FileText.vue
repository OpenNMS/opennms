<template>
  <FeatherDrawer
    data-test="scv-drawer"
    @hidden="onClose"
    v-model="isVisible"
    :labels="labels"
    width="70em"
  >
    <div class="modal-body">
      <slot name="content"></slot>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { FeatherDrawer } from '@featherds/drawer'

const props = defineProps({
  title: { required: true, type: String },
  visible: { required: true, type: Boolean }
})

const emit = defineEmits(['hidden'])

const isVisible = ref(props.visible)

const labels = computed(() => {
  return {
    title: props.title
  }
})

const onClose = () => {
  isVisible.value = false
  emit('hidden')
}

watch([() => props.visible], ([newVal]) => {
  isVisible.value = newVal
})
</script>

<style lang="scss" scoped>
.modal-body {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>

