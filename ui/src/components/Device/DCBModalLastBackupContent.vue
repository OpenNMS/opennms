<template>
  <div class="content">{{ modalDeviceConfigBackup.config }}</div>
</template>

<script setup lang="ts">
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const modalDeviceConfigBackup = computed<DeviceConfigBackup>(() => store.state.deviceModule.modalDeviceConfigBackup)
</script>

<style scoped lang="scss">
.content {
  max-width: 1000px;
  max-height: calc(100vh - 400px);
  overflow: auto;
}
</style>
