<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="header"
    width="min(720px, 95vw)"
    data-test="plugin-loaded-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div v-if="result" class="dialog-body" :data-outcome="result.startOutcome.state">
      <div v-if="result.startOutcome.state === 'started'" class="callout success-callout" role="status" data-test="outcome-started">
        Its features started: {{ startedFeatures(result.plugin.features) }}.
      </div>
      <PluginStartFailure v-else-if="result.startOutcome.state === 'failed'" :outcome="result.startOutcome" data-test="outcome-failed">
        You can fix the cause and use Restart on the plugin row, or unload it.
      </PluginStartFailure>
      <div v-else-if="result.startOutcome.state === 'timeout'" class="callout warn-callout" role="status" data-test="outcome-timeout">
        The container did not pick the KAR up within 30 seconds; it is staged and will start on the next restart
        or when you use Restart.
      </div>
      <div v-else-if="result.startOutcome.state === 'unavailable'" class="callout warn-callout" role="status" data-test="outcome-unavailable">
        {{ result.startOutcome.message || 'The plugin container is not available, so the KAR was staged only.' }}
        Its features start on the next restart; until then the plugin is listed as staged.
      </div>
      <div v-else class="callout info-callout" role="status" data-test="outcome-restart-required">
        {{ result.startOutcome.message || 'The manifest sets Karaf-Feature-Start: false, so the container extracts the KAR now but its features start on the next restart.' }}
        Until then the plugin is listed as staged.
      </div>
      <div v-if="startedUnhealthy(result.startOutcome)" class="callout warn-callout" role="alert" data-test="outcome-unhealthy">
        <strong>Its health checks are not passing yet.</strong>
        <ul class="health-list" data-test="health-list">
          <li v-for="(line, index) in failingHealthLines(result.startOutcome.health)" :key="index">{{ line }}</li>
        </ul>
        <span>The plugin may still be initialising; the table shows it as "Loaded, health failing" until the checks pass. If it does not recover, check karaf.log and use Restart on its row.</span>
      </div>
      <div class="section-title">What was written</div>
      <ul class="written" data-test="written-list">
        <li>Deploy file <code>{{ result.plugin.fileName }}</code></li>
        <li v-if="result.plugin.bootFile">Boot file <code>{{ result.plugin.bootFile }}</code></li>
        <li v-else>No boot file: the KAR has no features to start automatically.</li>
        <li v-if="result.plugin.features.length">Features to start: {{ result.plugin.features.join(', ') }}</li>
      </ul>
      <template v-if="needsServerRestart(result.startOutcome)">
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
import { computed } from 'vue'
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'
import { PluginInstallResult } from '@/types/pluginManagement'
import { failingHealthLines, needsServerRestart, startedFeatures, startedUnhealthy } from './pluginDisplay'
import PluginStartFailure from './PluginStartFailure.vue'
import RestartCommands from './RestartCommands.vue'

const props = defineProps<{
  visible: boolean
  result: PluginInstallResult | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const header = computed(() => {
  if (!props.result) {
    return 'Plugin loaded'
  }
  const name = props.result.plugin.karName
  switch (props.result.startOutcome.state) {
    case 'started': return startedUnhealthy(props.result.startOutcome) ? `Plugin ${name} loaded, but its health checks are not passing` : `Plugin ${name} loaded`
    case 'failed': return `Plugin ${name} loaded, but it did not start`
    default: return `Plugin ${name} staged`
  }
})
</script>

<style lang="scss" scoped>
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-top: 0.5rem;
}

.callout {
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
}

.success-callout {
  border-left: 4px solid var(--p-green-500, #2e7d32);
  background: color-mix(in srgb, var(--p-green-500, #2e7d32) 10%, transparent);
}

.warn-callout {
  border-left: 4px solid var(--p-orange-500, #ef6c00);
  background: color-mix(in srgb, var(--p-orange-500, #ef6c00) 12%, transparent);
}

.info-callout {
  border-left: 4px solid var(--p-blue-500, #1565c0);
  background: color-mix(in srgb, var(--p-blue-500, #1565c0) 10%, transparent);
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
}

.warn-callout {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.health-list {
  margin: 0;
  padding-left: 1.25rem;
  font-family: monospace;
  font-size: 0.85rem;
}

.written {
  margin: 0;
  padding-left: 1.25rem;
  font-size: 0.9rem;
  line-height: 1.6;
}
</style>
