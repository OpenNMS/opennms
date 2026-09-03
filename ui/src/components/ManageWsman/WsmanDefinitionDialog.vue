<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="index === null ? 'New Server Definition' : `Edit Server Definition ${index + 1}`"
    class="wsman-definition-dialog"
    width="min(960px, 95vw)"
    data-test="wsman-definition-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>

      <section>
        <h3 class="section-title">Applies to</h3>
        <p class="dialog-note">
          Agents whose address falls in a range, is listed, or matches an IPLIKE pattern use this
          definition. Definitions are checked in order and the first match wins.
        </p>
        <p v-if="!hasCriteria" class="field-error" data-test="criteria-error">Add at least one range, address, or pattern.</p>

        <div class="criteria-block">
          <div class="criteria-label">IP ranges</div>
          <div v-for="(r, i) in ranges" :key="`range-${i}`" class="range-row" :data-test="`range-row-${i}`">
            <OnmsInputText :modelValue="r.begin" placeholder="Begin" :invalid="!!rangeErrors[i]" data-test="range-begin" @update:modelValue="setRange(i, 'begin', $event ?? '')" />
            <span>–</span>
            <OnmsInputText :modelValue="r.end" placeholder="End" :invalid="!!rangeErrors[i]" data-test="range-end" @update:modelValue="setRange(i, 'end', $event ?? '')" />
            <OnmsIconButton :icon="Delete" severity="danger" :title="'Remove range'" :aria-label="'Remove range'" data-test="remove-range" @click="ranges.splice(i, 1)" />
            <small v-if="rangeErrors[i]" class="field-error">{{ rangeErrors[i] }}</small>
          </div>
          <OnmsButton variant="outlined" label="Add range" data-test="add-range" @click="ranges.push({ begin: '', end: '' })" />
        </div>

        <div class="criteria-block">
          <div class="criteria-label">Specific addresses</div>
          <div class="chips" data-test="specific-chips">
            <OnmsChip v-for="(ip, i) in specifics" :key="`s-${ip}`" :label="ip" removable @remove="specifics.splice(i, 1)" />
          </div>
          <div class="add-row">
            <OnmsInputText v-model="newSpecific" placeholder="10.0.0.5" :invalid="!!newSpecific && !isIpAddress(newSpecific)" data-test="specific-input" @keydown.enter.prevent="addSpecific" />
            <OnmsButton variant="outlined" label="Add" :disabled="!newSpecific.trim() || !isIpAddress(newSpecific)" data-test="add-specific" @click="addSpecific" />
          </div>
        </div>

        <div class="criteria-block">
          <div class="criteria-label">IPLIKE patterns</div>
          <div class="chips" data-test="ipmatch-chips">
            <OnmsChip v-for="(m, i) in ipMatches" :key="`m-${m}`" :label="m" removable @remove="ipMatches.splice(i, 1)" />
          </div>
          <div class="add-row">
            <OnmsInputText v-model="newIpMatch" placeholder="10.0.*.* or 10.0.1-5.* (IPv4 only)" :invalid="!!newIpMatch && !isIplikePattern(newIpMatch)" data-test="ipmatch-input" @keydown.enter.prevent="addIpMatch" />
            <OnmsButton variant="outlined" label="Add" :disabled="!newIpMatch.trim() || !isIplikePattern(newIpMatch)" data-test="add-ipmatch" @click="addIpMatch" />
          </div>
        </div>
      </section>

      <section>
        <h3 class="section-title">Settings</h3>
        <p class="dialog-note">Anything left unset is inherited from the agent defaults.</p>
        <WsmanSettingsFields
          v-model="form"
          idPrefix="wsman-definition"
          :errors="errors"
          :hasPassword="existing?.hasPassword ?? false"
          unsetLabel="Inherit default"
        />
      </section>
    </div>

    <template #footer>
      <OnmsButton variant="text" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton :label="index === null ? 'Create' : 'Save'" :disabled="!canSave || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsChip, OnmsDialog, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'

import WsmanSettingsFields from './WsmanSettingsFields.vue'
import {
  definitionToInput,
  emptySettingsForm,
  formToInput,
  isIpAddress,
  isIplikePattern,
  rangeProblem,
  settingsToForm,
  settingsToInput,
  validateSettingsForm
} from './wsmanForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanConfig, WsmanDefinitionInput, WsmanRange } from '@/types/wsmanAdmin'

const props = defineProps<{
  visible: boolean
  config: WsmanConfig
  // null creates a new definition (appended last)
  index: number | null
}>()

const emit = defineEmits(['update:visible'])

const store = useWsmanAdminStore()

const existing = computed(() => (props.index === null ? null : props.config.definitions[props.index] ?? null))

const form = ref(emptySettingsForm())
const ranges = ref<WsmanRange[]>([])
const specifics = ref<string[]>([])
const ipMatches = ref<string[]>([])
const newSpecific = ref('')
const newIpMatch = ref('')
const saving = ref(false)
const errorText = ref('')

const errors = computed(() => validateSettingsForm(form.value))
const rangeErrors = computed(() => ranges.value.map(r => rangeProblem(r.begin, r.end)))
const hasCriteria = computed(() => ranges.value.length + specifics.value.length + ipMatches.value.length > 0)
const canSave = computed(() =>
  hasCriteria.value && Object.keys(errors.value).length === 0 && rangeErrors.value.every(e => e === null))

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      const d = existing.value
      form.value = d ? settingsToForm(d) : emptySettingsForm()
      ranges.value = d ? d.ranges.map(r => ({ ...r })) : []
      specifics.value = d ? [...d.specifics] : []
      ipMatches.value = d ? [...d.ipMatches] : []
      newSpecific.value = ''
      newIpMatch.value = ''
      errorText.value = ''
    }
  }
)

const setRange = (i: number, key: keyof WsmanRange, value: string) => {
  ranges.value[i] = { ...ranges.value[i], [key]: value }
}

const addSpecific = () => {
  const v = newSpecific.value.trim()
  if (v && isIpAddress(v) && !specifics.value.includes(v)) {
    specifics.value.push(v)
  }
  newSpecific.value = ''
}

const addIpMatch = () => {
  const v = newIpMatch.value.trim()
  if (v && isIplikePattern(v) && !ipMatches.value.includes(v)) {
    ipMatches.value.push(v)
  }
  newIpMatch.value = ''
}

const save = async () => {
  saving.value = true
  try {
    const edited: WsmanDefinitionInput = {
      ...formToInput(form.value),
      ranges: ranges.value.map(r => ({ begin: r.begin.trim(), end: r.end.trim() })),
      specifics: [...specifics.value],
      ipMatches: [...ipMatches.value],
      sourceIndex: props.index
    }
    const definitions = props.config.definitions.map((d, i) => definitionToInput(d, i))
    if (props.index === null) {
      definitions.push(edited)
    } else {
      definitions[props.index] = edited
    }
    const error = await store.saveConfig({
      version: props.config.version,
      defaults: settingsToInput(props.config.defaults),
      definitions
    })
    if (error === null) {
      emit('update:visible', false)
    } else {
      errorText.value = error
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
  gap: 1.25rem;
  padding-top: 0.5rem;
}

.section-title {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
}

.dialog-note {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.criteria-block {
  margin-bottom: 0.75rem;
}

.criteria-label {
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.35rem;
}

.range-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 0.4rem;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 0.4rem;
}

.add-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.field-error {
  color: var(--p-red-500, #c62828);
  font-size: 0.85rem;
  margin: 0;
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
