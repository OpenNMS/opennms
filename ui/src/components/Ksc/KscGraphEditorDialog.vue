<template>
  <OnmsDialog
    :visible="true"
    :header="isEdit ? 'Edit Graph' : 'Add Graph'"
    width="min(720px, 95vw)"
    data-test="ksc-graph-editor"
    @update:visible="(v) => { if (!v) emit('close') }"
  >
    <div class="graph-form">
      <div v-if="generalError" class="form-error" data-test="graph-general-error">{{ generalError }}</div>

      <FormField label="Resource" :required="true">
        <div class="resource-current" v-if="resourceId && !pickerOpen">
          <span class="resource-label" data-test="graph-resource-current">{{ resourceLabel || resourceId }}</span>
          <OnmsButton variant="text" label="Change" data-test="graph-change-resource" @click="openPicker" />
        </div>
        <div v-else class="resource-picker">
          <OnmsSelect
            :modelValue="selectedNode"
            :options="topResources"
            optionLabel="label"
            optionValue="name"
            placeholder="Select a node/resource"
            :fluid="true"
            data-test="graph-node-select"
            @update:modelValue="(v) => onNodeSelected(v as string)"
          />
          <OnmsSpinner v-if="loadingChildren" />
          <OnmsSelect
            v-else-if="childResources.length"
            :modelValue="resourceId"
            :options="childResources"
            optionLabel="label"
            optionValue="id"
            placeholder="Select a resource to graph"
            :fluid="true"
            data-test="graph-resource-select"
            @update:modelValue="(v) => onResourceSelected(v as string)"
          />
          <div v-else-if="selectedNode" class="hint" data-test="graph-no-children">This node exposes no graphable resources.</div>
        </div>
        <small v-if="errors.resource" class="field-error" data-test="graph-resource-error">{{ errors.resource }}</small>
      </FormField>

      <FormField label="Graph type" :required="true">
        <OnmsSpinner v-if="loadingDefinitions" />
        <OnmsSelect
          v-else
          :modelValue="graphtype"
          :options="definitions"
          placeholder="Select a graph"
          :fluid="true"
          :disabled="!resourceId || !definitions.length"
          :invalid="Boolean(errors.graphtype)"
          data-test="graph-type-select"
          @update:modelValue="(v) => onGraphtypeSelected(v as string)"
        />
        <small v-if="resourceId && !loadingDefinitions && !definitions.length" class="hint" data-test="graph-no-defs">
          No prefab graphs are available for this resource.
        </small>
        <small v-if="graphTypeTitle" class="hint" data-test="graph-type-title">{{ graphTypeTitle }}</small>
        <small v-if="errors.graphtype" class="field-error" data-test="graph-type-error">{{ errors.graphtype }}</small>
      </FormField>

      <FormField label="Title" :required="true">
        <OnmsInputText
          :modelValue="title"
          :maxlength="TITLE_MAX"
          :invalid="Boolean(errors.title)"
          placeholder="Shown above the graph"
          data-test="graph-title-input"
          @update:modelValue="(v) => (title = String(v ?? ''))"
        />
        <small v-if="errors.title" class="field-error" data-test="graph-title-error">{{ errors.title }}</small>
      </FormField>

      <FormField label="Timespan" :required="true">
        <OnmsSelect
          :modelValue="timespan"
          :options="timespanOptions"
          :fluid="true"
          data-test="graph-timespan-select"
          @update:modelValue="(v) => (timespan = String(v ?? '7_day'))"
        />
      </FormField>
    </div>

    <template #footer>
      <OnmsButton variant="outlined" label="Cancel" data-test="graph-cancel" @click="emit('close')" />
      <OnmsButton label="Apply" data-test="graph-apply" @click="apply" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, ref } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText, OnmsSelect, OnmsSpinner } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import API from '@/services'
import { decodeResourceId } from '@/components/Ksc/utils/kscResource'
import { KSC_TIMESPAN_OPTIONS } from '@/components/Ksc/utils/kscTimespan'
import { Resource } from '@/types'
import { KscGraph } from '@/types/ksc'

const TITLE_MAX = 255

const props = defineProps({
  graph: {
    type: Object as PropType<KscGraph | null>,
    default: null
  }
})

const emit = defineEmits<{
  close: []
  save: [graph: KscGraph]
}>()

const isEdit = computed(() => props.graph !== null)
const timespanOptions = KSC_TIMESPAN_OPTIONS

const title = ref('')
const timespan = ref('7_day')
const resourceId = ref('')
const resourceLabel = ref('')
const graphtype = ref('')
const graphTypeTitle = ref('')

const topResources = ref<Resource[]>([])
const childResources = ref<Resource[]>([])
const selectedNode = ref('')
const definitions = ref<string[]>([])

const pickerOpen = ref(true)
const loadingChildren = ref(false)
const loadingDefinitions = ref(false)
const generalError = ref('')

const errors = ref<{ resource?: string, graphtype?: string, title?: string }>({})

const loadDefinitions = async (id: string) => {
  loadingDefinitions.value = true
  try {
    const resp = await API.getGraphDefinitionsByResourceId(id)
    definitions.value = resp?.name ?? []
  } finally {
    loadingDefinitions.value = false
  }
}

const openPicker = () => {
  pickerOpen.value = true
}

const onNodeSelected = async (name: string) => {
  selectedNode.value = name
  childResources.value = []
  resourceId.value = ''
  resourceLabel.value = ''
  definitions.value = []
  graphtype.value = ''
  graphTypeTitle.value = ''
  if (!name) {
    return
  }
  loadingChildren.value = true
  try {
    const node = await API.getResourceForNode(name)
    childResources.value = node?.children?.resource ?? []
  } finally {
    loadingChildren.value = false
  }
}

const onResourceSelected = async (id: string) => {
  resourceId.value = id
  const match = childResources.value.find(r => r.id === id)
  resourceLabel.value = match?.label ?? id
  graphtype.value = ''
  graphTypeTitle.value = ''
  errors.value.resource = undefined
  await loadDefinitions(id)
}

const onGraphtypeSelected = async (name: string) => {
  graphtype.value = name
  errors.value.graphtype = undefined
  graphTypeTitle.value = ''
  if (!title.value.trim() && resourceLabel.value) {
    title.value = `${resourceLabel.value}`
  }
  if (name) {
    const def = await API.getDefinitionData(name)
    if (def?.title) {
      graphTypeTitle.value = def.title
    }
  }
}

const validate = (): boolean => {
  const next: { resource?: string, graphtype?: string, title?: string } = {}
  if (!resourceId.value) {
    next.resource = 'Choose a resource to graph.'
  }
  if (!graphtype.value) {
    next.graphtype = 'Choose a graph type.'
  }
  if (!title.value.trim()) {
    next.title = 'A title is required.'
  } else if (title.value.length > TITLE_MAX) {
    next.title = `Keep the title under ${TITLE_MAX} characters.`
  }
  errors.value = next
  return Object.keys(next).length === 0
}

const apply = () => {
  generalError.value = ''
  if (!validate()) {
    return
  }
  // Address by resourceId and clear the legacy node/domain fields, matching how
  // new KSC graphs are stored; preserve any existing external link.
  emit('save', {
    title: title.value.trim(),
    timespan: timespan.value,
    graphtype: graphtype.value,
    resourceId: resourceId.value,
    nodeId: null,
    nodeSource: null,
    domain: null,
    interfaceId: null,
    extlink: props.graph?.extlink ?? null
  })
}

onMounted(async () => {
  topResources.value = (await API.getResources())?.resource ?? []
  if (props.graph) {
    title.value = props.graph.title ?? ''
    timespan.value = props.graph.timespan || '7_day'
    graphtype.value = props.graph.graphtype ?? ''
    const decoded = decodeResourceId(props.graph.resourceId)
    if (decoded) {
      resourceId.value = decoded
      resourceLabel.value = decoded
      pickerOpen.value = false
      await loadDefinitions(decoded)
      if (graphtype.value) {
        const def = await API.getDefinitionData(graphtype.value)
        if (def?.title) {
          graphTypeTitle.value = def.title
        }
      }
    }
  }
})
</script>

<style scoped lang="scss">
.graph-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-width: 0;
}

.resource-current {
  display: flex;
  align-items: center;
  gap: 0.5rem;

  .resource-label {
    font-family: monospace;
    word-break: break-all;
  }
}

.resource-picker {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.hint {
  color: var(--p-text-muted-color);
  display: block;
  margin-top: 0.25rem;
}

.field-error {
  color: var(--p-red-500, #d32f2f);
  display: block;
  margin-top: 0.25rem;
}

.form-error {
  color: var(--p-red-500, #d32f2f);
  border: 1px solid var(--p-red-500, #d32f2f);
  border-radius: 4px;
  padding: 0.5rem 0.75rem;
}
</style>
