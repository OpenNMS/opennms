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
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { useStore } from 'vuex'

const store = useStore()

defineProps({
  visible: {
    required: true,
    type: Boolean
  }
})

const modalDeviceConfigBackup = computed<DeviceConfigBackup>(() => store.state.deviceModule.modalDeviceConfigBackup)

const labels = reactive({
  title: 'DCB',
  close: 'Close'
})

watchEffect(() => labels.title = `Device Name: ${modalDeviceConfigBackup.value.deviceName}`)
</script>

<style scoped lang="scss">
.content {
  min-height: 300px;
  min-width: 550px;
  position: relative;
}
</style>
