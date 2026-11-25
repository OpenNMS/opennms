<template>
  <div class="callout error-callout" role="alert" data-test="start-failure">
    <strong data-test="start-failure-message">{{ outcome.message || 'The plugin did not start.' }}</strong>
    <div v-if="lines.length" class="diagnostics" data-test="start-failure-diagnostics">{{ lines.join('\n') }}</div>
    <span class="hint" data-test="start-failure-hint"><slot /></span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { PluginStartOutcome } from '@/types/pluginManagement'
import { diagnosticLines } from './pluginDisplay'

const props = defineProps<{
  outcome: PluginStartOutcome
}>()

const lines = computed(() => diagnosticLines(props.outcome.diagnostics))
</script>

<style lang="scss" scoped>
.callout {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
}

.error-callout {
  border-left: 4px solid var(--p-red-500, #c62828);
  background: color-mix(in srgb, var(--p-red-500, #c62828) 10%, transparent);
}

.diagnostics {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  background: var(--p-content-hover-background, rgba(0, 0, 0, 0.05));
  font-family: monospace;
  font-size: 0.85rem;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 16rem;
  overflow: auto;
}

.hint {
  color: var(--p-text-muted-color);
}
</style>
