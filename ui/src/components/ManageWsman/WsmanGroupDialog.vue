<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="original ? `Edit Group ${original.name}` : 'New Group'"
    width="min(1000px, 95vw)"
    data-test="wsman-group-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <div class="two-columns">
        <FormField label="Name" for="group-name" required :error="nameProblem || undefined">
          <OnmsInputText id="group-name" v-model="name" :invalid="!!nameProblem" fluid data-test="name-input" />
        </FormField>
        <FormField label="File" for="group-source" hint="The file the object is saved in. A new name creates a drop-in under wsman-datacollection.d/.">
        <OnmsSelect v-if="original" inputId="group-source" :modelValue="source" :options="sourceOptions" optionLabel="label" optionValue="value" disabled fluid data-test="source-select" />
        <OnmsSelect v-else inputId="group-source" v-model="source" :options="sourceOptions" optionLabel="label" optionValue="value" editable fluid data-test="source-select" />
        </FormField>
        <FormField label="Resource type" for="group-resource-type" required hint="node, or a resource type from wsman-datacollection resource types (e.g. dracPowerSupplyIndex)">
          <OnmsInputText id="group-resource-type" v-model="resourceType" fluid data-test="resource-type-input" />
        </FormField>
        <FormField label="Resource URI" for="group-resource-uri" required>
          <OnmsInputText id="group-resource-uri" v-model="resourceUri" placeholder="http://schemas.microsoft.com/wbem/wsman/1/wmi/root/cimv2/*" fluid data-test="resource-uri-input" />
        </FormField>
        <FormField label="Dialect" for="group-dialect" hint="Leave blank for the default (CQL); WQL is http://schemas.microsoft.com/wbem/wsman/1/WQL">
          <OnmsInputText id="group-dialect" v-model="dialect" fluid data-test="dialect-input" />
        </FormField>
        <FormField label="Filter" for="group-filter" hint="Query in the dialect, e.g. select LoadPercentage from Win32_Processor">
          <OnmsInputText id="group-filter" v-model="filter" fluid data-test="filter-input" />
        </FormField>
      </div>
      <p v-if="sourceProblem" class="field-error">{{ sourceProblem }}</p>

      <div class="attributes-header">
        <span class="section-title">Attributes</span>
        <OnmsButton variant="outlined" label="Add attribute" data-test="add-attribute" @click="attributes.push({ name: '', alias: '', type: 'gauge', indexOf: null, filter: null })" />
      </div>
      <p v-if="!attributes.length" class="field-error">At least one attribute is required.</p>
      <div v-for="(a, i) in attributes" :key="i" class="attribute-row" :data-test="`attribute-row-${i}`">
        <OnmsInputText :modelValue="a.name" placeholder="Property name" :invalid="!a.name.trim()" data-test="attribute-name" @update:modelValue="a.name = $event ?? ''" />
        <OnmsInputText :modelValue="a.alias" placeholder="Alias" :invalid="!a.alias.trim()" data-test="attribute-alias" @update:modelValue="a.alias = $event ?? ''" />
        <OnmsSelect :modelValue="a.type" :options="typeOptions" optionLabel="label" optionValue="value" data-test="attribute-type" @update:modelValue="a.type = $event as string" />
        <OnmsInputText :modelValue="a.indexOf ?? ''" placeholder="Index of (optional)" data-test="attribute-index-of" @update:modelValue="a.indexOf = ($event ?? '') || null" />
        <OnmsInputText :modelValue="a.filter ?? ''" placeholder="Filter (optional)" data-test="attribute-filter" @update:modelValue="a.filter = ($event ?? '') || null" />
        <OnmsIconButton :icon="Delete" severity="danger" title="Remove attribute" aria-label="Remove attribute" data-test="remove-attribute" @click="attributes.splice(i, 1)" />
      </div>
    </div>
    <template #footer>
      <OnmsButton variant="text" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton :label="original ? 'Save' : 'Create'" :disabled="!canSave || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsIconButton, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import FormField from '@/components/Common/FormField.vue'
import { ATTRIBUTE_TYPES, ROOT_FILE, fileInput, nameTaken, upsert } from './wsmanDataCollectionForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanAttributeInput, WsmanDataCollection, WsmanGroupInfo } from '@/types/wsmanAdmin'

const props = defineProps<{
  visible: boolean
  dataCollection: WsmanDataCollection
  original: WsmanGroupInfo | null
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
const resourceType = ref('node')
const resourceUri = ref('')
const dialect = ref('')
const filter = ref('')
const attributes = ref<WsmanAttributeInput[]>([])

const typeOptions = ATTRIBUTE_TYPES.map(t => ({ label: t, value: t }))

const nameProblem = computed(() => {
  if (!name.value.trim()) {
    return 'A name is required.'
  }
  return nameTaken(props.dataCollection, 'group', name.value, props.original?.name ?? null) ? 'A group with this name already exists.' : null
})
const canSave = computed(() => !nameProblem.value && !sourceProblem.value && !!resourceType.value.trim() && !!resourceUri.value.trim()
  && attributes.value.length > 0 && attributes.value.every(a => a.name.trim() && a.alias.trim() && a.type))

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    const o = props.original
    name.value = o?.name ?? ''
    source.value = o?.source ?? ROOT_FILE
    resourceType.value = o?.resourceType ?? 'node'
    resourceUri.value = o?.resourceUri ?? ''
    dialect.value = o?.dialect ?? ''
    filter.value = o?.filter ?? ''
    attributes.value = o ? o.attributes.map(a => ({ name: a.name, alias: a.alias, type: a.type ?? 'gauge', indexOf: a.indexOf, filter: a.filter })) : []
    errorText.value = ''
  }
})

const save = async () => {
  saving.value = true
  try {
    const file = source.value.trim()
    const input = fileInput(props.dataCollection, file)
    input.groups = upsert(input.groups, props.original?.name ?? null, {
      name: name.value.trim(),
      resourceType: resourceType.value.trim(),
      resourceUri: resourceUri.value.trim(),
      dialect: dialect.value.trim() || null,
      filter: filter.value.trim() || null,
      attributes: attributes.value.map(a => ({ name: a.name.trim(), alias: a.alias.trim(), type: a.type, indexOf: a.indexOf?.trim() || null, filter: a.filter?.trim() || null }))
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
.two-columns {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 0.75rem 1.25rem;
}

.attributes-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-weight: 600;
}

.attribute-row {
  display: grid;
  grid-template-columns: 1.2fr 1fr 0.7fr 1fr 1fr auto;
  gap: 0.4rem;
  align-items: center;
}
</style>
