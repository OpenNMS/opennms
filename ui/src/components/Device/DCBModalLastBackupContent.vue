<template>{{ modalDeviceConfigBackup.config }}</template>

<script setup lang="ts">
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const modalDeviceConfigBackup = computed<DeviceConfigBackup>(() => store.state.deviceModule.modalDeviceConfigBackup)
</script>
