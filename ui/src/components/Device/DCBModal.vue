<template>
  <!-- eslint-disable-next-line vue/no-mutating-props -->
  <FeatherDialog :modelValue="visible" relative :labels="labels" @update:modelValue="$emit('close')">
    <div class="content">
      <slot name="content" />
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { FeatherDialog } from '@featherds/dialog'
import { useDeviceStore } from '@/stores/deviceStore'

const deviceStore = useDeviceStore()

defineProps({
  visible: {
    required: true,
    type: Boolean
  }
})

const labels = reactive({
  title: 'DCB',
  close: 'Close'
})

watchEffect(() => labels.title = `Device Name: ${deviceStore.modalDeviceConfigBackup.deviceName}`)
</script>

<style scoped lang="scss">
.content {
  min-height: 300px;
  min-width: 550px;
  position: relative;
}
</style>
