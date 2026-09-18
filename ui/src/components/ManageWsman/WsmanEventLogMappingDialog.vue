<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="originalIndex === null ? 'Add event mapping' : `Edit mapping for Event ID ${mapping.eventId}`"
    width="min(640px, 95vw)"
    data-test="wsman-event-log-mapping-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <p class="hint">Records with this Event ID get their own UEI, and a severity if you set one. Add an event definition for the UEI so it is labelled and can raise an alarm.</p>
      <div class="two-columns">
        <FormField label="Event ID" for="mapping-event-id" required :error="idProblem || undefined">
          <OnmsInputNumber inputId="mapping-event-id" v-model="mapping.eventId" :min="0" :useGrouping="false" fluid data-test="event-id-input" />
        </FormField>
        <FormField label="Severity" for="mapping-severity" hint="Empty keeps the severity of the record's level.">
          <OnmsSelect inputId="mapping-severity" v-model="mapping.severity" :options="SEVERITY_OPTIONS" showClear placeholder="From the level" fluid data-test="severity-select" />
        </FormField>
        <FormField label="Only in log" for="mapping-logfile" hint="Empty matches the Event ID in every log.">
          <OnmsInputText id="mapping-logfile" :modelValue="mapping.logfile ?? ''" fluid data-test="logfile-input" @update:modelValue="mapping.logfile = ($event ?? '') || null" />
        </FormField>
        <FormField label="Only from source" for="mapping-source" hint="The provider name, e.g. Service Control Manager.">
          <OnmsInputText id="mapping-source" :modelValue="mapping.source ?? ''" fluid data-test="source-input" @update:modelValue="mapping.source = ($event ?? '') || null" />
        </FormField>
      </div>
      <FormField label="UEI" for="mapping-uei" required :error="ueiProblem || undefined">
        <OnmsInputText id="mapping-uei" v-model="mapping.uei" :invalid="!!ueiProblem" fluid data-test="uei-input" />
      </FormField>
    </div>
    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton :label="originalIndex === null ? 'Add' : 'Save'" :disabled="!canSave || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsInputNumber, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { SEVERITY_OPTIONS, defaultMapping, upsertMapping } from './wsmanEventLogForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanEventLogConfig, WsmanEventLogMapping } from '@/types/wsmanAdmin'

const props = defineProps<{
  visible: boolean
  config: WsmanEventLogConfig
  packageName: string
  originalIndex: number | null
  original: WsmanEventLogMapping | null
}>()

const emit = defineEmits(['update:visible'])

const store = useWsmanAdminStore()

const mapping = ref<WsmanEventLogMapping>(defaultMapping())
const saving = ref(false)
const errorText = ref('')

const idProblem = computed(() => (Number.isInteger(mapping.value.eventId) && mapping.value.eventId >= 0 ? null : 'An Event ID is required.'))
const ueiProblem = computed(() => (/^uei\.\S+$/.test((mapping.value.uei ?? '').trim()) ? null : 'A UEI starting with uei. is required.'))
const canSave = computed(() => !idProblem.value && !ueiProblem.value)

watch(() => props.visible, (isVisible) => {
  if (!isVisible) {
    return
  }
  errorText.value = ''
  mapping.value = props.original ? { ...props.original } : defaultMapping()
})

const save = async () => {
  if (!canSave.value) {
    return
  }
  saving.value = true
  try {
    const next: WsmanEventLogMapping = { ...mapping.value, uei: mapping.value.uei.trim() }
    const result = await store.saveEventLog(upsertMapping(props.config, props.packageName, props.originalIndex, next))
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
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem;
}

.hint {
  margin: 0;
  color: var(--p-text-muted-color);
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
