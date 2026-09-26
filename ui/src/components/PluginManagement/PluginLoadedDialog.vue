<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="result ? `Plugin ${result.plugin.karName} ${result.restartRequired ? 'staged' : 'loaded'}` : 'Plugin loaded'"
    width="min(720px, 95vw)"
    data-test="plugin-loaded-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div v-if="result" class="dialog-body">
      <p v-if="result.restartRequired" class="dialog-note" data-test="loaded-note-restart">
        The manifest sets <code>Karaf-Feature-Start: false</code>, so the container extracts the KAR now but its
        features start on the next restart; until then the plugin is listed as staged.
      </p>
      <p v-else class="dialog-note" data-test="loaded-note-auto">
        The container picks the KAR up from the deploy directory within seconds and starts its features; the boot
        file makes them start again on every later boot. No restart is needed; the table shows the plugin as
        installed once its features report started.
      </p>
      <div class="section-title">What was written</div>
      <ul class="written" data-test="written-list">
        <li>Deploy file <code>{{ result.plugin.fileName }}</code></li>
        <li v-if="result.plugin.bootFile">Boot file <code>{{ result.plugin.bootFile }}</code></li>
        <li v-else>No boot file: the KAR has no features to start automatically.</li>
        <li v-if="result.plugin.features.length">Features to start: {{ result.plugin.features.join(', ') }}</li>
      </ul>
      <template v-if="result.restartRequired">
        <div class="section-title">Restart OpenNMS</div>
        <RestartCommands :instructions="result.restartInstructions" />
      </template>
    </div>
    <template #footer>
      <OnmsButton label="Close" data-test="close-button" @click="emit('update:visible', false)" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'
import { PluginInstallResult } from '@/types/pluginManagement'
import RestartCommands from './RestartCommands.vue'

defineProps<{
  visible: boolean
  result: PluginInstallResult | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()
</script>

<style lang="scss" scoped>
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-top: 0.5rem;
}

.dialog-note {
  margin: 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
}

.written {
  margin: 0;
  padding-left: 1.25rem;
  font-size: 0.9rem;
  line-height: 1.6;
}
</style>
