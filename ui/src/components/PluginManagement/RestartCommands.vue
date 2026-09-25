<template>
  <div class="restart-commands" data-test="restart-commands">
    <div class="command">
      <div class="command-label">Package installation (RPM or DEB)</div>
      <code data-test="restart-packages">{{ instructions.packages }}</code>
    </div>
    <div class="command">
      <div class="command-label">Container</div>
      <code data-test="restart-container">{{ instructions.container }}</code>
    </div>
    <div v-if="instructions.healthCheck" class="command">
      <div class="command-label">Health check after the restart</div>
      <code data-test="restart-health-check">{{ instructions.healthCheck }}</code>
    </div>
    <p v-if="instructions.note" class="note" data-test="restart-note">{{ instructions.note }}</p>
  </div>
</template>

<script setup lang="ts">
import { RestartInstructions } from '@/types/pluginManagement'

defineProps<{
  instructions: RestartInstructions
}>()
</script>

<style lang="scss" scoped>
.restart-commands {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.command-label {
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

code {
  display: block;
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  background: var(--p-content-hover-background, rgba(0, 0, 0, 0.05));
  font-size: 0.85rem;
  white-space: pre-wrap;
  word-break: break-all;
}

.note {
  margin: 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}
</style>
