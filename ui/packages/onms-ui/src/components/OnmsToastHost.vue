<template>
  <Toast
    :group="ONMS_TOAST_GROUP_CENTER"
    position="bottom-center"
  />
  <Toast
    :group="ONMS_TOAST_GROUP_START"
    position="bottom-left"
  />
</template>

<script setup lang="ts">
import Toast from 'primevue/toast'
import { ONMS_TOAST_GROUP_CENTER, ONMS_TOAST_GROUP_START } from '../composables/useOnmsToast'

// Seam toast outlet (NMS-20029). Mount exactly once, near the app root.
// Fire toasts with useOnmsToast().showToast(...).
</script>
