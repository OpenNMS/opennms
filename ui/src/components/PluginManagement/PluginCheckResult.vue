<template>
  <template v-if="inspection">
    <dl class="summary" data-test="inspection-summary">
      <div class="source"><dt>Source</dt><dd data-test="summary-source">{{ source }}</dd></div>
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
    <p v-if="error" class="error" role="alert" data-test="load-error">{{ error }}</p>
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
  <PluginLoadedDialog v-model:visible="showLoaded" :result="loaded" />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsCheckbox } from '@opennms/onms-ui'

import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { KarInspection, PluginInstallResult } from '@/types/pluginManagement'
import PluginChecksTable from './PluginChecksTable.vue'
import PluginLoadedDialog from './PluginLoadedDialog.vue'
import { CONTAINER_UNAVAILABLE, NOT_SET, formatSize, shortSha } from './pluginDisplay'

// The part of the load flow both tabs share: the inspection of a KAR the
// server already holds, the checks, the acknowledgement and the load itself.
const props = defineProps<{
  inspection: KarInspection | null
  // where the KAR came from, as a sentence for the summary
  source: string
  // the container is unavailable: nothing can be loaded
  disabled: boolean
}>()

const emit = defineEmits<{
  (e: 'loaded', result: PluginInstallResult): void
}>()

const store = usePluginManagementStore()

const installing = ref(false)
const acknowledged = ref(false)
const error = ref('')
const loaded = ref<PluginInstallResult | null>(null)
const showLoaded = ref(false)

const hasFailures = computed(() => props.inspection?.checks.some(c => c.level === 'FAIL') ?? false)
const hasWarnings = computed(() => props.inspection?.checks.some(c => c.level === 'WARN') ?? false)
const canLoad = computed(() =>
  props.inspection !== null && !props.disabled && !installing.value && !hasFailures.value && (!hasWarnings.value || acknowledged.value))

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
  const features = props.inspection?.features ?? []
  return features.length ? features.map(f => (f.version ? `${f.name} ${f.version}` : f.name)).join(', ') : NOT_SET
})

watch(() => props.inspection, () => {
  acknowledged.value = false
  error.value = ''
})

const load = async () => {
  if (!props.inspection || !canLoad.value) {
    return
  }
  installing.value = true
  error.value = ''
  try {
    const result = await store.install({
      uploadToken: props.inspection.uploadToken,
      karName: props.inspection.karName,
      acknowledgeWarnings: hasWarnings.value
    })
    if (result.success && result.payload) {
      loaded.value = result.payload
      showLoaded.value = true
      emit('loaded', result.payload)
    } else {
      error.value = result.message
    }
  } finally {
    installing.value = false
  }
}
</script>

<style lang="scss" scoped>
.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 0.5rem 1.5rem;
  margin: 1rem 0;
  font-size: 0.9rem;

  .source {
    grid-column: 1 / -1;
  }

  dt {
    font-weight: 600;
  }

  dd {
    margin: 0;
    word-break: break-word;
  }
}

.error {
  color: var(--p-red-500, #c62828);
  margin: 1rem 0 0 0;
  font-size: 0.9rem;
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
