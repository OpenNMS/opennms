<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="original ? `Edit package ${original.name}` : 'Add package'"
    width="min(720px, 95vw)"
    data-test="wsman-event-log-package-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <p class="hint">
        A package reads its logs from every node whose interface matches the filter and carries the <em>WS-Man</em> service.
        Use several packages to read different logs, or at different frequencies, from different sets of hosts.
      </p>
      <FormField label="Name" for="package-name" required :error="nameProblem || undefined">
        <OnmsInputText id="package-name" v-model="name" :invalid="!!nameProblem" fluid data-test="name-input" />
      </FormField>
      <FormField label="Filter" for="package-filter" required :error="filterProblem || undefined" hint="An OpenNMS filter, e.g. IPADDR != '0.0.0.0' for every host, catincWindows-Servers, or IPADDR IPLIKE 10.1.*.*">
        <OnmsTextarea id="package-filter" v-model="filter" rows="3" fluid data-test="filter-input" />
      </FormField>
      <div class="preview-row">
        <OnmsButton variant="outlined" label="Preview matching nodes" :disabled="!filter.trim() || previewing" data-test="preview-button" @click="preview" />
        <span v-if="previewText" class="preview" :class="{ error: previewIsError }" data-test="preview-text">{{ previewText }}</span>
      </div>
      <ul v-if="previewMatches.length" class="matches" data-test="preview-matches">
        <li v-for="m in previewMatches" :key="m.nodeId">{{ m.label }} ({{ m.ipAddress }}<template v-if="m.location">, {{ m.location }}</template>)</li>
      </ul>
      <p v-if="!original" class="hint">The new package starts with the System and Application logs; adjust them on the tab afterwards.</p>
    </div>
    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton :label="original ? 'Save' : 'Add'" :disabled="!canSave || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsInputText, OnmsTextarea } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { defaultPackage, upsertPackage } from './wsmanEventLogForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanEventLogConfig, WsmanEventLogFilterPreview, WsmanEventLogPackage } from '@/types/wsmanAdmin'

const props = defineProps<{
  visible: boolean
  config: WsmanEventLogConfig
  original: WsmanEventLogPackage | null
}>()

const emit = defineEmits(['update:visible'])

const store = useWsmanAdminStore()

const name = ref('')
const filter = ref('')
const saving = ref(false)
const previewing = ref(false)
const errorText = ref('')
const previewText = ref('')
const previewIsError = ref(false)
const previewMatches = ref<WsmanEventLogFilterPreview['matches']>([])

const nameProblem = computed(() => {
  const trimmed = name.value.trim()
  if (!trimmed) {
    return 'A name is required.'
  }
  if (/[<>&"']/.test(trimmed)) {
    return 'The name must not contain markup or quote characters.'
  }
  const taken = props.config.packages.some(p => p.name === trimmed && p.name !== props.original?.name)
  return taken ? 'A package with this name exists.' : null
})

const filterProblem = computed(() => (filter.value.trim() ? null : 'A filter is required.'))
const canSave = computed(() => !nameProblem.value && !filterProblem.value)

watch(() => props.visible, (isVisible) => {
  if (!isVisible) {
    return
  }
  errorText.value = ''
  previewText.value = ''
  previewMatches.value = []
  name.value = props.original?.name ?? ''
  filter.value = props.original?.filter ?? defaultPackage().filter
})

watch(filter, () => {
  previewText.value = ''
  previewIsError.value = false
  previewMatches.value = []
})

// the filter is also checked on save; the preview is for seeing what it catches first
const preview = async () => {
  previewing.value = true
  previewMatches.value = []
  try {
    const result = await store.previewEventLogFilter(filter.value.trim())
    if (!result) {
      previewIsError.value = true
      previewText.value = 'The preview could not be requested.'
      return
    }
    if (!result.valid) {
      previewIsError.value = true
      previewText.value = result.error ?? 'The filter is invalid.'
      return
    }
    previewIsError.value = false
    previewText.value = `${result.readableNodes} node(s) with the WS-Man service would be read, out of ${result.matchedNodes} matching the filter.`
      + (result.error ? ` ${result.error}` : '')
    previewMatches.value = result.matches
  } finally {
    previewing.value = false
  }
}

const save = async () => {
  if (!canSave.value) {
    return
  }
  saving.value = true
  try {
    const pkg: WsmanEventLogPackage = props.original
      ? { ...props.original, name: name.value.trim(), filter: filter.value.trim() }
      : { ...defaultPackage(), name: name.value.trim(), filter: filter.value.trim() }
    const result = await store.saveEventLog(upsertPackage(props.config, props.original?.name ?? null, pkg))
    if (result.success) {
      emit('update:visible', false)
    } else {
      errorText.value = result.message
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;
}

.hint {
  margin: 0;
  color: var(--p-text-muted-color);
  font-size: 0.9rem;
}

.preview-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.preview {
  font-size: 0.9rem;

  &.error {
    color: var(--p-red-500, #e24c4c);
  }
}

.matches {
  margin: 0;
  padding-left: 1.25rem;
  max-height: 10rem;
  overflow: auto;
  font-size: 0.9rem;
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}
</style>
