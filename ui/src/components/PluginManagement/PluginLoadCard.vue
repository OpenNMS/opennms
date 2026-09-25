<template>
  <OnmsCard class="plugin-load-card" data-test="plugin-load-card">
    <template #title>
      <span class="card-title">Load a plugin</span>
    </template>
    <template #content>
      <p class="card-note">
        Choose a KAR file and run the checks. Nothing is written until Load plugin is pressed; a failing
        check blocks the load and a warning has to be acknowledged first.
      </p>
      <div class="file-row">
        <input ref="fileInput" type="file" accept=".kar" data-test="plugin-file-input" @change="onFileChange" />
        <OnmsButton
          variant="outlined"
          label="Choose a .kar file"
          :disabled="disabled || busy"
          :title="disabled ? CONTAINER_UNAVAILABLE : undefined"
          data-test="choose-file"
          @click="fileInput?.click()"
        />
        <span v-if="file" class="file-name" data-test="selected-file">{{ file.name }} ({{ formatSize(file.size) }})</span>
        <span v-else class="file-name muted" data-test="no-file">No file selected</span>
        <OnmsButton
          label="Run checks"
          :disabled="!file || disabled || busy"
          :loading="checking"
          :title="disabled ? CONTAINER_UNAVAILABLE : undefined"
          data-test="run-checks"
          @click="runChecks"
        />
      </div>
      <p v-if="error" class="error" role="alert" data-test="load-error">{{ error }}</p>

      <template v-if="inspection">
        <dl class="summary" data-test="inspection-summary">
          <div><dt>KAR name</dt><dd data-test="summary-kar-name">{{ inspection.karName }}</dd></div>
          <div><dt>Size</dt><dd data-test="summary-size">{{ formatSize(inspection.size) }}</dd></div>
          <div><dt>SHA-256</dt><dd data-test="summary-sha256" :title="inspection.sha256">{{ shortSha(inspection.sha256) }}</dd></div>
          <div><dt>Features</dt><dd data-test="summary-features">{{ featureSummary }}</dd></div>
          <div><dt>Bundles</dt><dd data-test="summary-bundles">{{ inspection.bundles.length }}</dd></div>
          <div><dt>Karaf-Feature-Start</dt><dd data-test="summary-feature-start">{{ inspection.manifest['Karaf-Feature-Start'] ?? NOT_SET }}</dd></div>
        </dl>
        <PluginChecksTable :checks="inspection.checks" />
        <p v-if="hasFailures" class="error" data-test="fail-note">A check failed, so this KAR cannot be loaded.</p>
        <div v-else-if="hasWarnings" class="ack-row">
          <OnmsCheckbox v-model="acknowledged" inputId="plugin-ack-warnings" data-test="ack-warnings" />
          <label for="plugin-ack-warnings">I understand the warnings above</label>
        </div>
        <div class="actions">
          <OnmsButton
            label="Load plugin"
            :disabled="!canLoad"
            :loading="installing"
            :title="loadTitle"
            data-test="load-plugin"
            @click="load"
          />
        </div>
      </template>
    </template>
  </OnmsCard>
  <PluginLoadedDialog v-model:visible="showLoaded" :result="loaded" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { OnmsButton, OnmsCard, OnmsCheckbox } from '@opennms/onms-ui'

import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { KarInspection, PluginInstallResult } from '@/types/pluginManagement'
import PluginChecksTable from './PluginChecksTable.vue'
import PluginLoadedDialog from './PluginLoadedDialog.vue'
import { CONTAINER_UNAVAILABLE, NOT_SET, formatSize, shortSha } from './pluginDisplay'

const props = defineProps<{
  // the container is unavailable: nothing can be checked or loaded
  disabled: boolean
}>()

const store = usePluginManagementStore()

const fileInput = ref<HTMLInputElement | null>(null)
const file = ref<File | null>(null)
const inspection = ref<KarInspection | null>(null)
const checking = ref(false)
const installing = ref(false)
const acknowledged = ref(false)
const error = ref('')
const loaded = ref<PluginInstallResult | null>(null)
const showLoaded = ref(false)

const busy = computed(() => checking.value || installing.value)
const hasFailures = computed(() => inspection.value?.checks.some(c => c.level === 'FAIL') ?? false)
const hasWarnings = computed(() => inspection.value?.checks.some(c => c.level === 'WARN') ?? false)
const canLoad = computed(() =>
  inspection.value !== null && !props.disabled && !busy.value && !hasFailures.value && (!hasWarnings.value || acknowledged.value))

const loadTitle = computed(() => {
  if (props.disabled) {
    return CONTAINER_UNAVAILABLE
  }
  if (hasFailures.value) {
    return 'A check failed'
  }
  if (hasWarnings.value && !acknowledged.value) {
    return 'Acknowledge the warnings first'
  }
  return undefined
})

const featureSummary = computed(() => {
  const features = inspection.value?.features ?? []
  return features.length ? features.map(f => (f.version ? `${f.name} ${f.version}` : f.name)).join(', ') : NOT_SET
})

const reset = () => {
  inspection.value = null
  acknowledged.value = false
  error.value = ''
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
      inspection.value = result.payload
    } else {
      error.value = result.message
    }
  } finally {
    checking.value = false
  }
}

const load = async () => {
  if (!inspection.value || !canLoad.value) {
    return
  }
  installing.value = true
  error.value = ''
  try {
    const result = await store.install({
      uploadToken: inspection.value.uploadToken,
      karName: inspection.value.karName,
      acknowledgeWarnings: hasWarnings.value
    })
    if (result.success && result.payload) {
      loaded.value = result.payload
      showLoaded.value = true
      file.value = null
      reset()
    } else {
      error.value = result.message
    }
  } finally {
    installing.value = false
  }
}
</script>

<style lang="scss" scoped>
.plugin-load-card {
  padding: 25px;
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

.file-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 1rem;

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
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
}

.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.5rem 1.5rem;
  margin: 0 0 1rem 0;
  font-size: 0.9rem;

  dt {
    font-weight: 600;
  }

  dd {
    margin: 0;
    word-break: break-word;
  }
}

.ack-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}
</style>
