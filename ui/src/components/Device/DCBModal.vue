<template>
  <!-- eslint-disable-next-line vue/no-mutating-props -->
  <FeatherDialog v-model="visible" :labels="labels" @update:modelValue="$emit('close')">
    <div class="content">
      <slot name="content" />
    </div>
  </FeatherDialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
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

const labels = ref({
  title: `DeviceName: ${modalDeviceConfigBackup.value.deviceName}`,
  close: 'Close'
})
</script>

<style scoped lang="scss">
.content {
  width: 500px;
  position: relative;
}
</style>
