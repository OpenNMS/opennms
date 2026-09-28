<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="plugin ? `Restart ${plugin.karName}?` : 'Restart plugin?'"
    width="min(640px, 95vw)"
    data-test="plugin-restart-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div v-if="plugin" class="dialog-body">
      <div v-if="errorText" class="callout error-callout" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <template v-if="outcome">
        <div v-if="outcome.state === 'started'" class="callout success-callout" role="status" data-test="restart-started">
          <strong>Started</strong>
          <span>{{ startedFeatures(outcome.plugin.features) }}</span>
        </div>
        <div v-if="startedUnhealthy(outcome)" class="callout warn-callout" role="alert" data-test="restart-unhealthy">
          <strong>Its health checks are not passing yet.</strong>
          <ul class="health-list" data-test="health-list">
            <li v-for="(line, index) in failingHealthLines(outcome.health)" :key="index">{{ line }}</li>
          </ul>
          <span>The plugin may still be initialising. If it does not recover, check karaf.log; a plugin that cannot restart in place needs a server restart.</span>
        </div>
        <PluginStartFailure v-else-if="outcome.state === 'failed'" :outcome="outcome" data-test="restart-failed">
          You can fix the cause and try Restart again, or unload the plugin.
        </PluginStartFailure>
        <div v-else-if="outcome.state !== 'started'" class="callout warn-callout" role="status" data-test="restart-not-started">
          {{ outcome.message || 'The plugin is not running yet; the table shows its state.' }}
        </div>
      </template>
      <div v-else class="callout info-callout" data-test="restart-callout">
        Restarts {{ startedFeatures(plugin.features) }}; the plugin is unavailable for a few seconds and comes back
        in the same mode.
      </div>
    </div>
    <template #footer>
      <template v-if="outcome">
        <OnmsButton label="Close" data-test="close-button" @click="emit('update:visible', false)" />
      </template>
      <template v-else>
        <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
        <OnmsButton label="Restart plugin" :disabled="!plugin || busy" :loading="busy" data-test="restart-button" @click="restart" />
      </template>
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'

import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { PluginEntry, PluginStartOutcome } from '@/types/pluginManagement'
import { failingHealthLines, startedFeatures, startedUnhealthy } from './pluginDisplay'
import PluginStartFailure from './PluginStartFailure.vue'

const props = defineProps<{
  visible: boolean
  plugin: PluginEntry | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'restarted', plugin: PluginEntry): void
}>()

const store = usePluginManagementStore()

const busy = ref(false)
const errorText = ref('')
// the answer of the last restart, shown in place of the confirmation until the dialog reopens
const outcome = ref<(PluginStartOutcome & { plugin: PluginEntry }) | null>(null)

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      errorText.value = ''
      outcome.value = null
    }
  }
)

const restart = async () => {
  if (!props.plugin) {
    return
  }
  busy.value = true
  errorText.value = ''
  try {
    const result = await store.restart(props.plugin.karName)
    if (result.success && result.payload) {
      outcome.value = { ...result.payload.startOutcome, plugin: result.payload.plugin }
      emit('restarted', result.payload.plugin)
    } else {
      errorText.value = result.message
    }
  } finally {
    busy.value = false
  }
}
</script>

<style lang="scss" scoped>
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;
}

.callout {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
}

.info-callout {
  border-left: 4px solid var(--p-blue-500, #1565c0);
  background: color-mix(in srgb, var(--p-blue-500, #1565c0) 10%, transparent);
}

.success-callout {
  border-left: 4px solid var(--p-green-500, #2e7d32);
  background: color-mix(in srgb, var(--p-green-500, #2e7d32) 10%, transparent);
}

.warn-callout {
  border-left: 4px solid var(--p-orange-500, #ef6c00);
  background: color-mix(in srgb, var(--p-orange-500, #ef6c00) 12%, transparent);
}

.error-callout {
  border-left: 4px solid var(--p-red-500, #c62828);
  background: color-mix(in srgb, var(--p-red-500, #c62828) 10%, transparent);
  color: var(--p-red-600, #dc2626);
}
.health-list {
  margin: 0;
  padding-left: 1.25rem;
  font-family: monospace;
  font-size: 0.85rem;
}
</style>
