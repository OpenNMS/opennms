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
      <p class="hint">Records with this Event ID get their own UEI, and a severity if you set one.</p>
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

      <div class="definition-header">
        <span class="section-title">Event definition</span>
        <span v-if="definitionLoading" class="hint" data-test="definition-loading">Looking up the UEI…</span>
        <span v-else-if="definitionUnavailable" class="hint definition-missing" data-test="definition-unavailable">The UEI could not be looked up.</span>
        <span v-else-if="definitionLocked" class="hint" data-test="definition-locked">Defined in {{ definition?.sourceName }}; edit it on the Event Configuration page.</span>
        <span v-else-if="definition?.exists" class="hint" data-test="definition-exists">Defined in {{ definition.sourceName }}</span>
        <span v-else-if="definition" class="hint definition-missing" data-test="definition-missing">No definition yet: the event has no label and cannot raise an alarm</span>
      </div>
      <div v-if="!definitionLocked" class="definition-toggle">
        <OnmsCheckbox :modelValue="saveDefinition" inputId="mapping-save-definition" binary data-test="save-definition" @update:modelValue="toggleSaveDefinition" />
        <label for="mapping-save-definition">{{ definition?.exists ? 'Update the event definition' : 'Create the event definition' }}</label>
      </div>
      <template v-if="saveDefinition && !definitionLocked">
        <div class="two-columns">
          <FormField label="Label" for="definition-label" required :error="labelProblem || undefined">
            <OnmsInputText id="definition-label" :modelValue="draft.label ?? ''" :invalid="!!labelProblem" fluid data-test="definition-label" @update:modelValue="editDraft('label', $event ?? '')" />
          </FormField>
          <FormField label="Definition severity" for="definition-severity" required>
            <OnmsSelect inputId="definition-severity" :modelValue="draft.severity ?? undefined" :options="SEVERITY_OPTIONS" fluid data-test="definition-severity" @update:modelValue="editDraft('severity', ($event as string | undefined) ?? null)" />
          </FormField>
        </div>
        <FormField label="Log message" for="definition-logmsg" hint="Shown in the event list. %parm[computerName]% and %parm[message]% are filled from the record. Empty uses the label.">
          <OnmsInputText id="definition-logmsg" :modelValue="draft.logMessage ?? ''" fluid data-test="definition-logmsg" @update:modelValue="editDraft('logMessage', $event ?? '')" />
        </FormField>
        <FormField label="Description" for="definition-descr" hint="Empty uses the label.">
          <OnmsTextarea id="definition-descr" :modelValue="draft.description ?? ''" rows="2" fluid data-test="definition-descr" @update:modelValue="editDraft('description', $event ?? '')" />
        </FormField>
        <div class="definition-toggle">
          <OnmsToggleSwitch :modelValue="draft.alarm" inputId="definition-alarm" data-test="definition-alarm" @update:modelValue="editDraft('alarm', $event)" />
          <label for="definition-alarm">Raise an alarm</label>
        </div>
        <div v-if="draft.alarm" class="two-columns">
          <FormField label="Alarm type" for="definition-alarm-type">
            <OnmsSelect inputId="definition-alarm-type" :modelValue="draft.alarmType" :options="ALARM_TYPE_OPTIONS" optionLabel="label" optionValue="value" fluid data-test="definition-alarm-type" @update:modelValue="editDraft('alarmType', ($event as number | undefined) ?? null)" />
          </FormField>
          <FormField label="Reduction key" for="definition-reduction-key" hint="One alarm per key; the default is one per node.">
            <OnmsInputText id="definition-reduction-key" :modelValue="draft.reductionKey ?? ''" fluid data-test="definition-reduction-key" @update:modelValue="editDraft('reductionKey', $event ?? '')" />
          </FormField>
        </div>
      </template>
    </div>
    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton :label="originalIndex === null ? 'Add' : 'Save'" :disabled="!canSave || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { OnmsButton, OnmsCheckbox, OnmsDialog, OnmsInputNumber, OnmsInputText, OnmsSelect, OnmsTextarea, OnmsToggleSwitch } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { ALARM_TYPE_OPTIONS, SEVERITY_OPTIONS, defaultDefinition, defaultMapping, upsertMapping } from './wsmanEventLogForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanEventLogConfig, WsmanEventLogDefinition, WsmanEventLogMapping } from '@/types/wsmanAdmin'

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
// where a new mapping landed once its save went through, so a retry after a
// failed definition save replaces it instead of appending a second one
const savedIndex = ref<number | null>(null)

// the definition behind the UEI as stored (null until looked up), and the draft the form edits
const definition = ref<WsmanEventLogDefinition | null>(null)
const definitionLoading = ref(false)
const definitionUnavailable = ref(false)
const saveDefinition = ref(false)
const saveDefinitionTouched = ref(false)
const draft = ref<WsmanEventLogDefinition>(defaultDefinition(defaultMapping()))
const draftTouched = ref(false)
let lookupTimer: ReturnType<typeof setTimeout> | null = null
// bumped per lookup so a slow response for an earlier UEI is dropped
let lookupSequence = 0
let requestedUei: string | null = null

const idProblem = computed(() => {
  const id = mapping.value.eventId
  return id !== null && Number.isInteger(id) && id >= 1 ? null : 'An Event ID of 1 or more is required.'
})
const ueiProblem = computed(() => {
  const uei = (mapping.value.uei ?? '').trim()
  if (!/^uei\.\S+$/.test(uei)) {
    return 'A UEI starting with uei. is required.'
  }
  return uei.endsWith('/') ? 'Add a name after the last /.' : null
})
const definitionLocked = computed(() => !!definition.value?.exists && !definition.value.editable)
const labelProblem = computed(() => (saveDefinition.value && !definitionLocked.value && !(draft.value.label ?? '').trim() ? 'A label is required.' : null))
const canSave = computed(() => !idProblem.value && !ueiProblem.value && !labelProblem.value)

const clearLookupTimer = () => {
  if (lookupTimer) {
    clearTimeout(lookupTimer)
    lookupTimer = null
  }
}

const editDraft = <K extends keyof WsmanEventLogDefinition>(key: K, value: WsmanEventLogDefinition[K]) => {
  draft.value[key] = value
  draftTouched.value = true
}

const toggleSaveDefinition = (value: boolean) => {
  saveDefinition.value = value
  saveDefinitionTouched.value = true
}

const applyLookup = (found: WsmanEventLogDefinition | null, uei: string) => {
  definition.value = found
  definitionUnavailable.value = found === null
  if (found?.exists) {
    if (!draftTouched.value || draft.value.uei !== uei) {
      draft.value = { ...found }
    }
  } else if (!draftTouched.value) {
    draft.value = defaultDefinition({ ...mapping.value, uei })
  } else {
    draft.value.uei = uei
  }
  if (found === null || (found.exists && !found.editable)) {
    saveDefinition.value = false
  } else if (!saveDefinitionTouched.value) {
    // a new UEI defaults to creating its definition; an existing one is only touched on request
    saveDefinition.value = !found.exists
  }
}

const lookupDefinition = async () => {
  if (!props.visible) {
    return
  }
  const uei = mapping.value.uei.trim()
  requestedUei = uei
  if (ueiProblem.value) {
    lookupSequence++
    definition.value = null
    definitionUnavailable.value = false
    definitionLoading.value = false
    return
  }
  const sequence = ++lookupSequence
  definitionLoading.value = true
  try {
    const found = await store.getEventLogDefinition(uei)
    if (sequence === lookupSequence) {
      applyLookup(found, uei)
    }
  } finally {
    if (sequence === lookupSequence) {
      definitionLoading.value = false
    }
  }
}

watch(() => props.visible, (isVisible) => {
  clearLookupTimer()
  lookupSequence++
  definitionLoading.value = false
  if (!isVisible) {
    return
  }
  errorText.value = ''
  savedIndex.value = null
  mapping.value = props.original ? { ...props.original } : defaultMapping()
  definition.value = null
  definitionUnavailable.value = false
  saveDefinition.value = false
  saveDefinitionTouched.value = false
  draft.value = defaultDefinition(mapping.value)
  draftTouched.value = false
  lookupDefinition()
})

watch(() => mapping.value.uei, (uei) => {
  if (!props.visible) {
    return
  }
  clearLookupTimer()
  if (uei.trim() === requestedUei) {
    return
  }
  lookupTimer = setTimeout(() => {
    lookupTimer = null
    lookupDefinition()
  }, 400)
})

onBeforeUnmount(clearLookupTimer)

const save = async () => {
  if (!canSave.value) {
    return
  }
  saving.value = true
  try {
    if (lookupTimer || definitionLoading.value) {
      clearLookupTimer()
      await lookupDefinition()
      if (!canSave.value) {
        return
      }
    }
    const next: WsmanEventLogMapping = { ...mapping.value, uei: mapping.value.uei.trim() }
    const index = savedIndex.value ?? props.originalIndex
    const appendIndex = props.config.packages.find(p => p.name === props.packageName)?.eventMappings.length ?? null
    const result = await store.saveEventLog(upsertMapping(props.config, props.packageName, index, next))
    if (!result.success) {
      errorText.value = result.message
      return
    }
    if (index === null) {
      savedIndex.value = appendIndex
    }
    if (saveDefinition.value && !definitionLocked.value) {
      const saved = await store.saveEventLogDefinition({ ...draft.value, uei: next.uei })
      if (!saved.success) {
        errorText.value = `The mapping is saved, but its event definition is not: ${saved.message}`
        await lookupDefinition()
        return
      }
    }
    emit('update:visible', false)
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

.definition-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--p-content-border-color);
}

.section-title {
  font-weight: 600;
}

.definition-missing {
  color: var(--p-orange-600, #d97706);
}

.definition-toggle {
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
