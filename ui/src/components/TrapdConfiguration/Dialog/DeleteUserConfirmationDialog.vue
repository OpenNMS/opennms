<template>
  <ConfirmationDialog
    v-if="props.index !== null && store.snmpV3Users[props.index]"
    :visible="props.visible"
    title="Snmpv3 User Delete Confirmation"
    action-button-text="Delete"
    @cancel="emit('close')"
    @ok="emit('confirm')"
  >
    <template #content>
      <p>Are you sure you want to delete this SNMPv3 user with security name "{{ store.snmpV3Users[props.index!]?.securityName }}"?</p>
      <p><strong>Note:</strong> This action cannot be undone.</p>
    </template>
  </ConfirmationDialog>
</template>

<script setup lang="ts">
import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'

const store = useTrapdConfigStore()

const props = defineProps<{
  visible: boolean,
  index: number | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'confirm'): void
}>()
</script>

<style scoped lang="scss"></style>
