<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Restart OpenNMS"
    width="min(720px, 95vw)"
    data-test="restart-instructions-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="dialog-body">
      <p class="dialog-note">
        The boot file under <code>featuresBoot.d</code> is read only at startup, so a restart finishes
        loading staged plugins and removing unloaded ones.
      </p>
      <RestartCommands v-if="instructions" :instructions="instructions" />
      <p v-else class="dialog-note" data-test="no-instructions">
        The restart instructions could not be read from the server. Restart the OpenNMS service the way it
        was installed, then check that the web interface answers again.
      </p>
    </div>
    <template #footer>
      <OnmsButton label="Close" data-test="close-button" @click="emit('update:visible', false)" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'
import { RestartInstructions } from '@/types/pluginManagement'
import RestartCommands from './RestartCommands.vue'

defineProps<{
  visible: boolean
  instructions: RestartInstructions | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()
</script>

<style lang="scss" scoped>
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;
}

.dialog-note {
  margin: 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}
</style>
