<template>
  <div class="file-load" data-test="plugin-file-load">
    <p class="tab-note">
      Choose a KAR file and run the checks. Nothing is written until Load plugin is pressed; a failing
      check blocks the load and a warning has to be acknowledged first.
    </p>
    <div class="file-row">
      <input ref="fileInput" type="file" accept=".kar" data-test="plugin-file-input" @change="onFileChange" />
      <OnmsButton
        variant="outlined"
        label="Choose a .kar file"
        :disabled="disabled || checking"
        :title="disabled ? CONTAINER_UNAVAILABLE : undefined"
        data-test="choose-file"
        @click="fileInput?.click()"
      />
      <span v-if="file" class="file-name" data-test="selected-file">{{ file.name }} ({{ formatSize(file.size) }})</span>
      <span v-else class="file-name muted" data-test="no-file">No file selected</span>
      <OnmsButton
        label="Run checks"
        :disabled="!file || disabled || checking"
        :loading="checking"
        :title="disabled ? CONTAINER_UNAVAILABLE : undefined"
        data-test="run-checks"
        @click="runChecks"
      />
    </div>
    <p v-if="error" class="error" role="alert" data-test="load-error">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsButton } from '@opennms/onms-ui'

import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { KarInspection } from '@/types/pluginManagement'
import { CONTAINER_UNAVAILABLE, formatSize, uploadedSourceLine } from './pluginDisplay'

defineProps<{
  // the container is unavailable: nothing can be checked
  disabled: boolean
}>()

const emit = defineEmits<{
  (e: 'checked', inspection: KarInspection, source: string): void
  (e: 'reset'): void
}>()

const store = usePluginManagementStore()

const fileInput = ref<HTMLInputElement | null>(null)
const file = ref<File | null>(null)
const checking = ref(false)
const error = ref('')

const reset = () => {
  error.value = ''
  emit('reset')
}

// the input is cleared so choosing the same file again re-runs the checks
const onFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const chosen = input.files?.[0] ?? null
  input.value = ''
  reset()
  if (chosen && !chosen.name.toLowerCase().endsWith('.kar')) {
    file.value = null
    error.value = 'Choose a file with the .kar extension.'
    return
  }
  file.value = chosen
}

const runChecks = async () => {
  if (!file.value) {
    return
  }
  reset()
  checking.value = true
  try {
    const result = await store.check(file.value)
    if (result.success && result.payload) {
      emit('checked', result.payload, uploadedSourceLine(file.value.name, result.payload.size))
    } else {
      error.value = result.message
    }
  } finally {
    checking.value = false
  }
}

const clear = () => {
  file.value = null
  error.value = ''
}

defineExpose({ clear })
</script>

<style lang="scss" scoped>
.tab-note {
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.file-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;

  input {
    display: none;
  }
}

.file-name {
  font-size: 0.9rem;

  &.muted {
    color: var(--p-text-muted-color);
  }
}

.error {
  color: var(--p-red-500, #c62828);
  margin: 1rem 0 0 0;
  font-size: 0.9rem;
}
</style>
