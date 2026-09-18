<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="original ? `Edit log ${original.name}` : 'Add log'"
    width="min(720px, 95vw)"
    data-test="wsman-event-log-log-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <div class="two-columns">
        <FormField label="Log" for="log-name" required :error="nameProblem || undefined" hint="System, Application, Security, or another classic log; the Applications and Services logs are not supported yet.">
          <OnmsInputText id="log-name" v-model="log.name" :invalid="!!nameProblem" fluid data-test="name-input" />
        </FormField>
        <FormField label="Frequency" for="log-interval">
          <OnmsSelect inputId="log-interval" v-model="log.interval" :options="intervalOptions" optionLabel="label" optionValue="value" fluid data-test="interval-select" />
        </FormField>
        <FormField label="Records per poll" for="log-max" required :error="maxProblem || undefined" hint="More than this waits for the next poll and raises a warning event.">
          <OnmsInputNumber inputId="log-max" v-model="log.maxRecords" :min="1" :max="10000" :useGrouping="false" fluid data-test="max-records-input" />
        </FormField>
        <FormField label="First poll reads the last" for="log-lookback" required :error="lookbackProblem || undefined" hint="30s, 15m, 1h or 2d. Later polls continue from the last record seen.">
          <OnmsInputText id="log-lookback" v-model="log.lookback" :invalid="!!lookbackProblem" fluid data-test="lookback-input" />
        </FormField>
        <FormField label="Levels" for="log-levels" hint="Leave empty to keep every level.">
          <OnmsMultiSelect inputId="log-levels" v-model="levels" :options="LEVEL_OPTIONS" placeholder="All levels" fluid data-test="levels-select" />
        </FormField>
        <FormField label="Only these Event IDs" for="log-include" :error="includeProblem || undefined" hint="Comma-separated; empty keeps every ID.">
          <OnmsInputText id="log-include" :modelValue="log.includeEventIds ?? ''" fluid data-test="include-input" @update:modelValue="log.includeEventIds = ($event ?? '') || null" />
        </FormField>
        <FormField label="Never these Event IDs" for="log-exclude" :error="excludeProblem || undefined">
          <OnmsInputText id="log-exclude" :modelValue="log.excludeEventIds ?? ''" fluid data-test="exclude-input" @update:modelValue="log.excludeEventIds = ($event ?? '') || null" />
        </FormField>
      </div>
      <div class="enabled-row">
        <OnmsToggleSwitch v-model="log.enabled" inputId="log-enabled" data-test="enabled-switch" />
        <label for="log-enabled">Read this log</label>
      </div>
    </div>
    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton :label="original ? 'Save' : 'Add'" :disabled="!canSave || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsInputNumber, OnmsInputText, OnmsMultiSelect, OnmsSelect, OnmsToggleSwitch } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { DURATION, INTERVAL_OPTIONS, LEVEL_OPTIONS, defaultLog, joinCsv, splitCsv, upsertLog } from './wsmanEventLogForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanEventLogConfig, WsmanEventLogLog } from '@/types/wsmanAdmin'

const props = defineProps<{
  visible: boolean
  config: WsmanEventLogConfig
  packageName: string
  original: WsmanEventLogLog | null
}>()

const emit = defineEmits(['update:visible'])

const store = useWsmanAdminStore()

const log = ref<WsmanEventLogLog>(defaultLog())
const levels = ref<string[]>([])
const saving = ref(false)
const errorText = ref('')

// keep a non-standard interval from the file selectable rather than silently changing it
const intervalOptions = computed(() => {
  if (INTERVAL_OPTIONS.some(o => o.value === log.value.interval)) {
    return INTERVAL_OPTIONS
  }
  return [{ label: `Every ${log.value.interval / 1000} seconds`, value: log.value.interval }, ...INTERVAL_OPTIONS]
})

const nameProblem = computed(() => {
  const name = log.value.name.trim()
  if (!name) {
    return 'A log name is required.'
  }
  const pkg = props.config.packages.find(p => p.name === props.packageName)
  const taken = pkg?.logs.some(l => l.name.toLowerCase() === name.toLowerCase() && l.name !== props.original?.name)
  return taken ? 'This log is already listed in the package.' : null
})

const maxProblem = computed(() => (log.value.maxRecords >= 1 && log.value.maxRecords <= 10000 ? null : 'Between 1 and 10000.'))
const lookbackProblem = computed(() => (DURATION.test(log.value.lookback ?? '') ? null : 'Use a value like 30s, 15m, 1h or 2d.'))
const idsProblem = (csv: string | null) => (splitCsv(csv).every(v => /^\d+$/.test(v)) ? null : 'Comma-separated numbers only.')
const includeProblem = computed(() => idsProblem(log.value.includeEventIds))
const excludeProblem = computed(() => idsProblem(log.value.excludeEventIds))

const canSave = computed(() => !nameProblem.value && !maxProblem.value && !lookbackProblem.value && !includeProblem.value && !excludeProblem.value)

watch(() => props.visible, (isVisible) => {
  if (!isVisible) {
    return
  }
  errorText.value = ''
  log.value = props.original ? { ...props.original } : defaultLog()
  levels.value = splitCsv(log.value.levels)
})

const save = async () => {
  if (!canSave.value) {
    return
  }
  saving.value = true
  try {
    const next: WsmanEventLogLog = { ...log.value, name: log.value.name.trim(), lookback: log.value.lookback.trim(), levels: joinCsv(levels.value) }
    const result = await store.saveEventLog(upsertLog(props.config, props.packageName, props.original?.name ?? null, next))
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

.two-columns {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem;
}

.enabled-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
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
