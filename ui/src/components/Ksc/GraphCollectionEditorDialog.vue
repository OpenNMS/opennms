<template>
  <OnmsDialog
    :visible="true"
    :header="isEdit ? 'Edit Report' : 'New Report'"
    width="min(900px, 95vw)"
    data-test="ksc-report-editor"
    @update:visible="(v) => { if (!v) emit('close') }"
  >
    <div class="report-form">
      <FormField label="Title" :required="true" :error="errors.title">
        <OnmsInputText
          :modelValue="title"
          :maxlength="TITLE_MAX"
          :invalid="Boolean(errors.title)"
          placeholder="Report title"
          data-test="report-title-input"
          @update:modelValue="(v) => { title = String(v ?? ''); errors.title = undefined }"
        />
      </FormField>

      <div class="options-row">
        <FormField label="Graphs per line" class="per-line">
          <OnmsInputNumber
            :modelValue="graphsPerLine"
            :min="1"
            :max="8"
            :step="1"
            data-test="report-per-line"
            @update:modelValue="(v) => (graphsPerLine = (v as number) ?? 1)"
          />
        </FormField>
        <label class="toggle-field">
          <OnmsToggleSwitch
            :modelValue="showTimespan"
            data-test="report-show-timespan"
            @update:modelValue="(v) => (showTimespan = v)"
          />
          <span>Show timespan controls</span>
        </label>
        <label class="toggle-field">
          <OnmsToggleSwitch
            :modelValue="showGraphtype"
            data-test="report-show-graphtype"
            @update:modelValue="(v) => (showGraphtype = v)"
          />
          <span>Show graph-type controls</span>
        </label>
      </div>

      <div class="graphs-section">
        <div class="graphs-header">
          <span class="section-title">Graphs</span>
          <OnmsButton label="Add Graph" data-test="report-add-graph" @click="addGraph" />
        </div>

        <table v-if="workingGraphs.length" class="graphs-table" data-test="report-graphs-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Resource</th>
              <th>Graph type</th>
              <th>Timespan</th>
              <th class="actions-col">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(graph, index) in workingGraphs" :key="index" :data-test="`report-graph-row-${index}`">
              <td>{{ graph.title }}</td>
              <td class="mono">{{ shortResource(graph) }}</td>
              <td>{{ graph.graphtype }}</td>
              <td>{{ graph.timespan }}</td>
              <td class="actions-col">
                <OnmsIconButton
                  variant="text"
                  :icon="ArrowUp"
                  title="Move up"
                  aria-label="Move graph up"
                  :disabled="index === 0"
                  :data-test="`report-graph-up-${index}`"
                  @click="move(index, -1)"
                />
                <OnmsIconButton
                  variant="text"
                  :icon="ArrowDown"
                  title="Move down"
                  aria-label="Move graph down"
                  :disabled="index === workingGraphs.length - 1"
                  :data-test="`report-graph-down-${index}`"
                  @click="move(index, 1)"
                />
                <OnmsButton
                  variant="text"
                  label="Edit"
                  :data-test="`report-graph-edit-${index}`"
                  @click="editGraph(index)"
                />
                <OnmsButton
                  variant="text"
                  label="Remove"
                  :data-test="`report-graph-remove-${index}`"
                  @click="removeGraph(index)"
                />
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="no-graphs" data-test="report-no-graphs">No graphs yet. Add one to build the report.</div>
      </div>
    </div>

    <template #footer>
      <OnmsButton variant="outlined" label="Cancel" data-test="report-cancel" @click="emit('close')" />
      <OnmsButton label="Save" :disabled="saving" data-test="report-save" @click="save" />
    </template>
  </OnmsDialog>

  <KscGraphEditorDialog
    v-if="graphEditorVisible"
    :graph="editingIndex !== null ? workingGraphs[editingIndex] : null"
    @close="graphEditorVisible = false"
    @save="onGraphSaved"
  />
</template>

<script setup lang="ts">
import { computed, PropType, ref } from 'vue'

import { OnmsButton, OnmsDialog, OnmsIconButton, OnmsInputNumber, OnmsInputText, OnmsToggleSwitch } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import KscGraphEditorDialog from '@/components/Ksc/KscGraphEditorDialog.vue'
import ArrowUp from '@opennms/onms-ui/icons/hardware/KeyboardArrowUp.vue'
import ArrowDown from '@opennms/onms-ui/icons/hardware/KeyboardArrowDown.vue'
import { decodeResourceId } from '@/components/Ksc/utils/kscResource'
import { useKscStore } from '@/stores/kscStore'
import { KscGraph, KscReport } from '@/types/ksc'

const TITLE_MAX = 255

const props = defineProps({
  report: {
    type: Object as PropType<KscReport | null>,
    default: null
  }
})

const emit = defineEmits<{
  close: []
  saved: []
}>()

const store = useKscStore()

const isEdit = computed(() => props.report !== null && props.report.id !== null && props.report.id !== undefined)

const title = ref(props.report?.label ?? '')
const graphsPerLine = ref<number>(props.report?.graphs_per_line ?? 1)
const showTimespan = ref<boolean>(Boolean(props.report?.show_timespan_button))
const showGraphtype = ref<boolean>(Boolean(props.report?.show_graphtype_button))
// Deep copy so edits and reordering don't mutate the store's report until Save.
const workingGraphs = ref<KscGraph[]>((props.report?.kscGraph ?? []).map(g => ({ ...g })))

const graphEditorVisible = ref(false)
const editingIndex = ref<number | null>(null)
const saving = ref(false)
const errors = ref<{ title?: string }>({})

const shortResource = (graph: KscGraph): string => {
  const id = decodeResourceId(graph.resourceId)
  if (id) {
    return id.length > 48 ? `…${id.slice(-48)}` : id
  }
  return graph.nodeId ? `node ${graph.nodeId}` : (graph.domain ?? '-')
}

const addGraph = () => {
  editingIndex.value = null
  graphEditorVisible.value = true
}

const editGraph = (index: number) => {
  editingIndex.value = index
  graphEditorVisible.value = true
}

const onGraphSaved = (graph: KscGraph) => {
  if (editingIndex.value !== null) {
    workingGraphs.value.splice(editingIndex.value, 1, graph)
  } else {
    workingGraphs.value.push(graph)
  }
  graphEditorVisible.value = false
}

const removeGraph = (index: number) => {
  workingGraphs.value.splice(index, 1)
}

const move = (index: number, delta: number) => {
  const target = index + delta
  if (target < 0 || target >= workingGraphs.value.length) {
    return
  }
  const graphs = workingGraphs.value
  const [moved] = graphs.splice(index, 1)
  graphs.splice(target, 0, moved)
}

const validate = (): boolean => {
  errors.value = {}
  if (!title.value.trim()) {
    errors.value.title = 'A report title is required.'
  } else if (title.value.length > TITLE_MAX) {
    errors.value.title = `Keep the title under ${TITLE_MAX} characters.`
  }
  return !errors.value.title
}

const save = async () => {
  if (!validate()) {
    return
  }
  saving.value = true
  try {
    const payload: KscReport = {
      id: props.report?.id ?? null,
      label: title.value.trim(),
      show_timespan_button: showTimespan.value,
      show_graphtype_button: showGraphtype.value,
      graphs_per_line: graphsPerLine.value,
      kscGraph: workingGraphs.value
    }
    const ok = await store.saveReport(payload)
    if (ok) {
      emit('saved')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.report-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.options-row {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;

  .per-line {
    width: 150px;
  }
}

.toggle-field {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  cursor: pointer;
}

.graphs-section {
  .graphs-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
  }

  .section-title {
    font-weight: 700;
  }
}

.graphs-table {
  width: 100%;
  border-collapse: collapse;

  th,
  td {
    text-align: left;
    padding: 0.5rem;
    border-bottom: 1px solid var(--p-content-border-color);
    vertical-align: middle;
  }

  th {
    font-weight: 600;
    color: var(--p-text-muted-color);
  }

  .mono {
    font-family: monospace;
    word-break: break-all;
  }

  .actions-col {
    white-space: nowrap;
  }
}

.no-graphs {
  padding: 1rem;
  color: var(--p-text-muted-color);
}
</style>
