<template>
  <PToast
    :group="SNACKBAR_GROUP_CENTER"
    position="bottom-center"
  />
  <PToast
    :group="SNACKBAR_GROUP_START"
    position="bottom-left"
  />
</template>

<script setup lang="ts">
import Toast from 'primevue/toast'
import { SNACKBAR_GROUP_CENTER, SNACKBAR_GROUP_START } from '@/composables/useSnackbar'

const PToast = Toast
</script>
