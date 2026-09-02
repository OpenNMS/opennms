<template>
  <OnmsCard class="wsman-defaults" data-test="wsman-defaults">
    <template #title>
      <div class="card-header">
        <span class="card-title">Agent Defaults</span>
        <OnmsButton label="Edit" icon="pi pi-pencil" variant="outlined" size="small" data-test="edit-defaults" @click="emit('edit')" />
      </div>
    </template>
    <template #content>
      <p class="card-note">
        Applied to every WS-Man agent unless a definition overrides the setting. A dash means the
        setting is not present in <code>wsman-config.xml</code> and the built-in default applies.
      </p>
      <dl class="settings">
        <template v-for="row in SETTING_ROWS" :key="row.key">
          <dt>{{ row.label }}</dt>
          <dd :data-test="`default-${row.key}`">{{ formatSetting(settings, row.key) }}</dd>
        </template>
      </dl>
    </template>
  </OnmsCard>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsCard } from '@opennms/onms-ui'
import { WsmanAgentSettings } from '@/types/wsmanAdmin'
import { SETTING_ROWS, formatSetting } from './wsmanDisplay'

defineProps<{
  settings: WsmanAgentSettings
}>()

const emit = defineEmits<{
  (e: 'edit'): void
}>()
</script>

<style lang="scss" scoped>
.wsman-defaults {
  padding: 25px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.card-note {
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.settings {
  display: grid;
  grid-template-columns: max-content 1fr;
  column-gap: 2rem;
  row-gap: 0.4rem;
  margin: 0;
  max-width: 640px;

  dt {
    font-weight: 600;
  }

  dd {
    margin: 0;
  }
}
</style>
