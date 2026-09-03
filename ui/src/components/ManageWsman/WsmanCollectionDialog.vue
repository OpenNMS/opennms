<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="original ? `Edit Collection ${original.name}` : 'New Collection'"
    width="min(720px, 95vw)"
    data-test="wsman-collection-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <FormField label="Name" for="collection-name" required :error="nameProblem || undefined">
        <OnmsInputText id="collection-name" v-model="name" :invalid="!!nameProblem" fluid data-test="name-input" />
      </FormField>
      <FormField label="File" for="collection-source" hint="The file the object is saved in.">
        <OnmsSelect v-if="original" inputId="collection-source" :modelValue="source" :options="sourceOptions" optionLabel="label" optionValue="value" disabled fluid data-test="source-select" />
        <OnmsSelect v-else inputId="collection-source" v-model="source" :options="sourceOptions" optionLabel="label" optionValue="value" editable fluid data-test="source-select" />
      </FormField>
      <p v-if="sourceProblem" class="field-error">{{ sourceProblem }}</p>
      <FormField label="RRD step (seconds)" for="collection-step" required :error="stepProblem || undefined">
        <OnmsInputNumber inputId="collection-step" v-model="rrdStep" :min="1" :useGrouping="false" :invalid="!!stepProblem" fluid data-test="step-input" />
      </FormField>
      <FormField label="RRAs" required>
        <StringListEditor v-model="rras" placeholder="RRA:AVERAGE:0.5:1:2016" hint="RRA:AVERAGE|MIN|MAX|LAST:xff:steps:rows" :validator="isRra" dataTest="rras" />
        <p v-if="!rras.length" class="field-error">At least one RRA is required.</p>
      </FormField>
      <label class="check-row">
        <OnmsCheckbox v-model="includeAll" binary data-test="include-all" />
        <span>Include all system definitions</span>
      </label>
      <FormField v-if="!includeAll" label="Included system definitions" for="collection-sysdefs" :error="!includedSystemDefinitions.length ? 'Name at least one system definition, or include all.' : undefined">
        <OnmsMultiSelect inputId="collection-sysdefs" v-model="includedSystemDefinitions" :options="sysDefOptions" optionLabel="label" optionValue="value" filter fluid data-test="sysdefs-select" />
      </FormField>
    </div>
    <template #footer>
      <OnmsButton variant="text" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton :label="original ? 'Save' : 'Create'" :disabled="!canSave || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsCheckbox, OnmsDialog, OnmsInputNumber, OnmsInputText, OnmsMultiSelect, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import StringListEditor from './StringListEditor.vue'
import { ROOT_FILE, fileInput, isRra, nameTaken, upsert } from './wsmanDataCollectionForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanCollectionInfo, WsmanDataCollection } from '@/types/wsmanAdmin'

const props = defineProps<{
  visible: boolean
  dataCollection: WsmanDataCollection
  // null creates a new collection
  original: WsmanCollectionInfo | null
}>()

const emit = defineEmits(['update:visible'])

const store = useWsmanAdminStore()

const saving = ref(false)
const errorText = ref('')
const source = ref(ROOT_FILE)

const sourceOptions = computed(() => props.dataCollection.sources.map(s => ({ label: s, value: s })))

const sourceProblem = computed(() => {
  const s = source.value.trim()
  if (!s) {
    return 'A file is required.'
  }
  if (!props.dataCollection.sources.includes(s) && !/^[A-Za-z0-9][A-Za-z0-9._-]*\.xml$/.test(s)) {
    return 'A new file must be a plain name ending in .xml, e.g. custom.xml.'
  }
  return null
})

const name = ref('')
const rrdStep = ref<number | null>(300)
const rras = ref<string[]>([])
const includeAll = ref(true)
const includedSystemDefinitions = ref<string[]>([])

const sysDefOptions = computed(() => props.dataCollection.systemDefinitions.map(s => ({ label: `${s.name} (${s.source})`, value: s.name })))

const nameProblem = computed(() => {
  if (!name.value.trim()) {
    return 'A name is required.'
  }
  return nameTaken(props.dataCollection, 'collection', name.value, props.original?.name ?? null) ? 'A collection with this name already exists.' : null
})
const stepProblem = computed(() => (rrdStep.value === null || rrdStep.value < 1 ? 'The step must be at least 1 second.' : null))
const canSave = computed(() => !nameProblem.value && !sourceProblem.value && !stepProblem.value && rras.value.length > 0 && (includeAll.value || includedSystemDefinitions.value.length > 0))

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    const o = props.original
    name.value = o?.name ?? ''
    source.value = o?.source ?? ROOT_FILE
    rrdStep.value = o?.rrdStep ?? 300
    rras.value = o ? [...o.rras] : ['RRA:AVERAGE:0.5:1:2016', 'RRA:AVERAGE:0.5:12:1488', 'RRA:AVERAGE:0.5:288:366', 'RRA:MAX:0.5:288:366', 'RRA:MIN:0.5:288:366']
    includeAll.value = o ? o.includeAllSystemDefinitions : true
    includedSystemDefinitions.value = o ? [...o.includedSystemDefinitions] : []
    errorText.value = ''
  }
})

const save = async () => {
  saving.value = true
  try {
    const file = source.value.trim()
    const input = fileInput(props.dataCollection, file)
    input.collections = upsert(input.collections, props.original?.name ?? null, {
      name: name.value.trim(),
      rrdStep: rrdStep.value,
      rras: [...rras.value],
      includeAllSystemDefinitions: includeAll.value,
      includedSystemDefinitions: includeAll.value ? [] : [...includedSystemDefinitions.value]
    })
    const error = await store.saveDataCollectionFile(file, input)
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
  gap: 1rem;
  padding-top: 0.5rem;
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}

.check-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.field-error {
  color: var(--p-red-500, #c62828);
  font-size: 0.85rem;
  margin: 0;
}
</style>
