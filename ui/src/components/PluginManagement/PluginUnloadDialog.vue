<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="plugin ? `Unload ${plugin.karName}?` : 'Unload plugin?'"
    width="min(640px, 95vw)"
    data-test="plugin-unload-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div v-if="plugin" class="dialog-body">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <div class="callout" data-test="removal-callout">
        <strong>What will be removed</strong>
        <ul>
          <li><code>{{ plugin.fileName }}</code> from the deploy directory</li>
          <li v-if="plugin.bootFile">Boot file <code>{{ plugin.bootFile }}</code></li>
        </ul>
      </div>
      <div class="callout" data-test="stop-callout">
        <span v-if="plugin.features.length">
          Its features ({{ plugin.features.join(', ') }}) stop now; a restart completes the removal.
        </span>
        <span v-else>The container stops the plugin now; a restart completes the removal.</span>
      </div>
      <p v-if="plugin.status === 'unmanaged'" class="dialog-note" data-test="unmanaged-note">
        This plugin was found in the deploy directory but was not loaded through this page; unloading
        removes the file all the same, and any <code>featuresBoot.d</code> line that waits for this KAR is
        cleaned as well.
      </p>
      <FormField label="Type the KAR name to confirm" for="plugin-unload-confirm" required>
        <OnmsInputText id="plugin-unload-confirm" v-model="confirmation" :placeholder="plugin.karName" autocomplete="off" data-test="confirm-input" />
      </FormField>
    </div>
    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton severity="danger" label="Unload plugin" :disabled="!confirmed || busy" :loading="busy" data-test="unload-button" @click="unload" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsInputText } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { PluginEntry } from '@/types/pluginManagement'

const props = defineProps<{
  visible: boolean
  plugin: PluginEntry | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'unloaded', plugin: PluginEntry): void
}>()

const store = usePluginManagementStore()

const confirmation = ref('')
const busy = ref(false)
const errorText = ref('')

const confirmed = computed(() => props.plugin !== null && confirmation.value === props.plugin.karName)

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      confirmation.value = ''
      errorText.value = ''
    }
  }
)

const unload = async () => {
  if (!props.plugin || !confirmed.value) {
    return
  }
  busy.value = true
  try {
    const result = await store.unload(props.plugin.karName)
    if (result.success && result.payload) {
      emit('unloaded', result.payload.plugin)
      emit('update:visible', false)
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
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-orange-500, #ef6c00);
  background: color-mix(in srgb, var(--p-orange-500, #ef6c00) 10%, transparent);
  font-size: 0.9rem;

  ul {
    margin: 0.25rem 0 0 0;
    padding-left: 1.25rem;
  }
}

.dialog-note {
  margin: 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}
</style>
