<template>
  <Diff :prev="config1.config" :current="config2.config" :theme="theme" :mode="mode" />
</template>

<script setup lang="ts">
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { PropType } from 'vue'
import { useStore } from 'vuex'
type Mode = 'split' | 'unified'

const store = useStore()

defineProps({
  config1: {
    type: Object as PropType<DeviceConfigBackup>,
    required: true
  },
  config2: {
    type: Object as PropType<DeviceConfigBackup>,
    required: true
  },
  mode: {
    type: String as PropType<Mode>,
    default: 'split'
  }
})

const theme = computed(() => {
  const theme = store.state.appModule.theme
  if (theme === 'open-dark') return 'dark'
  return 'light'
})
</script>
