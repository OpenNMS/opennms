<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="graph-collection-editor">
    <TableCard>
      <div class="header">
        <div class="title-block">
          <div class="card-title" data-test="editor-title">{{ heading }}</div>
          <div class="subtitle">Give the collection a title, choose how it is laid out, and add the graphs it shows.</div>
        </div>
        <div class="header-actions">
          <OnmsButton variant="ghost" label="Cancel" data-test="cancel" @click="goBack" />
          <OnmsButton :label="isNew ? 'Create' : 'Save'" :disabled="saving || loading || !!loadError" data-test="save" @click="save()" />
        </div>
      </div>

      <p v-if="loadError" class="error" data-test="load-error">{{ loadError }}</p>

      <template v-if="!loadError">
        <div v-if="problems.length" class="problems" role="alert" data-test="problems">
          <p v-for="problem in problems" :key="problem">{{ problem }}</p>
        </div>

        <div class="options">
          <FormField label="Title" for="gc-title" required :error="titleError" class="title-field">
            <OnmsInputText id="gc-title" v-model="form.title" :maxlength="255" fluid data-test="title" />
          </FormField>
          <FormField label="Graphs per row" for="gc-per-row" hint="0 uses the system default.">
            <OnmsInputNumber
              inputId="gc-per-row"
              :modelValue="form.graphsPerLine"
              :min="0"
              :max="12"
              showButtons
              data-test="graphs-per-line"
              @update:modelValue="(value) => form.graphsPerLine = value ?? 0"
            />
          </FormField>
          <div class="toggles">
            <label class="toggle">
              <OnmsCheckbox inputId="gc-show-timespan" v-model="form.showTimespanButton" data-test="show-timespan" />
              <span>Offer a timespan override on the view page</span>
            </label>
            <label class="toggle">
              <OnmsCheckbox inputId="gc-show-graphtype" v-model="form.showGraphtypeButton" data-test="show-graphtype" />
              <span>Offer a graph type override on the view page</span>
            </label>
          </div>
        </div>

        <div class="graphs-header">
          <div class="card-title">Graphs</div>
          <OnmsButton icon="pi pi-plus" label="Add graph" data-test="add-graph" @click="openPicker(null)" />
        </div>

        <OnmsTable v-if="graphs.length" :value="graphs" dataKey="key" data-test="graphs-table">
          <OnmsColumn header="#">
            <template #body="{ index }">{{ index + 1 }}</template>
          </OnmsColumn>
          <OnmsColumn header="Title">
            <template #body="{ data }">
              <span class="wrap-cell" data-test="graph-title">{{ data.title || '(graph title)' }}</span>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Resource">
            <template #body="{ data }">
              <span class="wrap-cell" data-test="graph-resource">
                <template v-if="data.parentLabel">{{ data.parentLabel }} / </template>{{ data.resourceLabel ?? data.resourceId ?? legacyAddress(data) }}
              </span>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Graph type">
            <template #body="{ data }"><span class="wrap-cell">{{ data.graphtype }}</span></template>
          </OnmsColumn>
          <OnmsColumn header="Timespan">
            <template #body="{ data }">{{ timespanLabel(data.timespan) }}</template>
          </OnmsColumn>
          <OnmsColumn header="Status">
            <template #body="{ data }">
              <span v-if="data.valid === false" class="error" data-test="graph-problem">{{ data.error || 'Cannot be drawn.' }}</span>
              <span v-else class="muted">OK</span>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Actions">
            <template #body="{ data, index }">
              <div class="action-container">
                <OnmsIconButton :icon="Edit" :tooltip="'Edit this graph'" aria-label="Edit this graph" data-test="edit-graph" @click="openPicker(data)" />
                <OnmsIconButton :icon="KeyboardArrowUp" :disabled="index === 0" :tooltip="'Move up'" aria-label="Move up" data-test="move-up" @click="move(index, -1)" />
                <OnmsIconButton :icon="KeyboardArrowDown" :disabled="index === graphs.length - 1" :tooltip="'Move down'" aria-label="Move down" data-test="move-down" @click="move(index, 1)" />
                <OnmsIconButton :icon="Delete" severity="danger" :tooltip="'Remove this graph'" aria-label="Remove this graph" data-test="remove-graph" @click="remove(index)" />
              </div>
            </template>
          </OnmsColumn>
        </OnmsTable>
        <p v-else-if="!loading" class="muted" data-test="no-graphs">No graphs yet. Use Add graph to pick a resource and one of its graphs.</p>
      </template>
    </TableCard>

    <GraphPickerDialog
      :visible="pickerOpen"
      :graph="pickerGraph"
      :timespans="timespans"
      @update:visible="(value: boolean) => pickerOpen = value"
      @save="applyPicked"
    />

    <OnmsConfirmationDialog
      :visible="showLeaveConfirm"
      title="Discard Changes"
      actionButtonText="Discard changes"
      cancelButtonText="Keep editing"
      @ok="settleLeave(true)"
      @cancel="settleLeave(false)"
    >
      <template #content>
        <p>This collection has changes that are not saved. Leave the editor and discard them?</p>
      </template>
    </OnmsConfirmationDialog>

    <OnmsDialog
      :visible="showStaleDialog"
      modal
      header="Changed by someone else"
      width="30rem"
      data-test="stale-dialog"
      @update:visible="(value: boolean) => { if (!value) { showStaleDialog = false } }"
    >
      <p>
        This collection was changed since you opened it. Overwrite it with your version, or reload the
        current version and lose your changes?
      </p>
      <div class="dialog-actions">
        <OnmsButton variant="ghost" label="Keep editing" data-test="stale-keep" @click="showStaleDialog = false" />
        <OnmsButton variant="outlined" label="Reload" data-test="stale-reload" @click="reloadAfterStale" />
        <OnmsButton severity="danger" label="Overwrite" data-test="stale-overwrite" @click="save(true)" />
      </div>
    </OnmsDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { OnmsButton, OnmsCheckbox, OnmsColumn, OnmsConfirmationDialog, OnmsDialog, OnmsIconButton, OnmsInputNumber, OnmsInputText, OnmsTable } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import KeyboardArrowDown from '@opennms/onms-ui/icons/hardware/KeyboardArrowDown.vue'
import KeyboardArrowUp from '@opennms/onms-ui/icons/hardware/KeyboardArrowUp.vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import GraphPickerDialog from '@/components/GraphCollections/GraphPickerDialog.vue'
import { EditableGraph, fromResolvedCollection, toEditableGraphs, toStoredGraph } from '@/components/GraphCollections/editableGraph'
import useActionFeedback from '@/composables/useActionFeedback'
import {
  createGraphCollection,
  getGraphCollection,
  getGraphCollectionTimespans,
  getResolvedGraphCollection,
  getResolvedSyntheticGraphCollection,
  isStaleResult,
  updateGraphCollection
} from '@/services/graphCollectionsService'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { GraphCollection, GraphCollectionTimespan, SyntheticGraphCollectionKind } from '@/types/graphCollection'

/**
 * Create or edit a graph collection. Editing takes the collection `id`; a new collection may start
 * from a copy of a stored one (`?from=<id>`) or from an on-the-fly node collection
 * (`?fromNode=<id>`, `?fromNodeSource=<fs:fid>`, `?fromDomain=<name>`), as the legacy
 * "create from existing" and "customize this node report" actions did.
 */
const props = defineProps<{
  id?: string
}>()

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const { withSpinner, report } = useActionFeedback()

const isNew = computed(() => !props.id)

const form = reactive({
  title: '',
  showTimespanButton: false,
  showGraphtypeButton: false,
  graphsPerLine: 0
})
const graphs = ref<EditableGraph[]>([])
const timespans = ref<GraphCollectionTimespan[]>([])
let etag: string | null = null

const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const submitted = ref(false)
const problems = ref<string[]>([])
const showStaleDialog = ref(false)

const heading = computed(() => isNew.value ? 'New Graph Collection' : `Edit ${form.title || 'Graph Collection'}`)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: menuStore.mainMenu.homeUrl, isAbsoluteLink: true },
  { label: 'Graph Collections', to: '/graph-collections' },
  { label: isNew.value ? 'New' : 'Edit', to: '#', position: 'last' }
])

const titleError = computed(() => submitted.value && !form.title.trim() ? 'A title is required.' : '')

const timespanLabel = (id: string) => timespans.value.find(t => t.id === id)?.label ?? id

// an older file may address a resource the pre-resourceId way
const legacyAddress = (graph: EditableGraph) =>
  [graph.nodeId && `node ${graph.nodeId}`, graph.nodeSource && `node ${graph.nodeSource}`, graph.domain && `domain ${graph.domain}`, graph.interfaceId]
    .filter(Boolean).join(' ')

const queryValue = (name: string): string | null => {
  const value = route.query[name]
  const single = Array.isArray(value) ? value[0] : value
  return single || null
}

const applyCollection = (collection: GraphCollection) => {
  form.title = collection.title
  form.showTimespanButton = !!collection.showTimespanButton
  form.showGraphtypeButton = !!collection.showGraphtypeButton
  form.graphsPerLine = collection.graphsPerLine ?? 0
}

// What Save would write, to tell whether leaving would lose anything.
const snapshot = () => JSON.stringify({ ...form, graphs: graphs.value.map(toStoredGraph) })
let pristine = ''

const loadStored = async (id: number, asCopy: boolean) => {
  const [stored, resolved] = await Promise.all([getGraphCollection(id), getResolvedGraphCollection(id)])
  if (!stored.success || !stored.payload) {
    loadError.value = stored.message
    return
  }
  applyCollection(stored.payload.collection)
  graphs.value = toEditableGraphs(stored.payload.collection.graphs, resolved.payload?.collection.graphs)
  if (asCopy) {
    form.title = `Copy of ${form.title}`.slice(0, 255)
    etag = null
  } else {
    etag = stored.payload.etag
  }
}

const loadSynthetic = async (kind: SyntheticGraphCollectionKind, key: string) => {
  const result = await getResolvedSyntheticGraphCollection(kind, key)
  if (!result.success || !result.payload) {
    loadError.value = result.message
    return
  }
  // the on-the-fly collection offers both overrides, and the legacy customize kept that
  form.title = result.payload.title
  form.showTimespanButton = !!result.payload.showTimespanButton
  form.showGraphtypeButton = !!result.payload.showGraphtypeButton
  form.graphsPerLine = 0
  graphs.value = fromResolvedCollection(result.payload)
}

// set once Save succeeds, so the navigation to the view is not questioned
let leaveApproved = false

// back to a blank new collection; the same component instance serves new, copy and edit routes
const resetForm = () => {
  form.title = ''
  form.showTimespanButton = false
  form.showGraphtypeButton = false
  form.graphsPerLine = 0
  graphs.value = []
  etag = null
  submitted.value = false
  problems.value = []
  leaveApproved = false
}

// the collection this route edits, copies or starts from, if any
const loadTarget = (): Promise<void> => {
  const from = queryValue('from')
  const fromNode = queryValue('fromNode')
  const fromNodeSource = queryValue('fromNodeSource')
  const fromDomain = queryValue('fromDomain')
  if (props.id) {
    return loadStored(Number(props.id), false)
  } else if (from) {
    return loadStored(Number(from), true)
  } else if (fromNode) {
    return loadSynthetic('node', fromNode)
  } else if (fromNodeSource) {
    return loadSynthetic('nodeSource', fromNodeSource)
  } else if (fromDomain) {
    return loadSynthetic('domain', fromDomain)
  }
  return Promise.resolve()
}

const load = async () => {
  loading.value = true
  loadError.value = ''
  resetForm()
  await withSpinner(async () => {
    // the timespan list does not depend on the collection, so both go out at once
    const [spans] = await Promise.all([getGraphCollectionTimespans(), loadTarget()])
    timespans.value = spans.payload ?? []
    if (!spans.success) {
      loadError.value = spans.message
    }
  })
  pristine = snapshot()
  loading.value = false
}

onMounted(load)

// a navigation from one editor route to another reuses this instance, so reload for the new target
watch(() => [props.id, route.query.from, route.query.fromNode, route.query.fromNodeSource, route.query.fromDomain], load)

// graph list editing
const pickerOpen = ref(false)
const pickerGraph = ref<EditableGraph | null>(null)
let nextKey = 1_000_000

const openPicker = (graph: EditableGraph | null) => {
  pickerGraph.value = graph
  pickerOpen.value = true
}

const applyPicked = (picked: EditableGraph) => {
  const index = pickerGraph.value ? graphs.value.findIndex(g => g.key === pickerGraph.value?.key) : -1
  if (index >= 0) {
    graphs.value.splice(index, 1, picked)
  } else {
    graphs.value.push({ ...picked, key: nextKey++ })
  }
}

const move = (index: number, delta: number) => {
  const target = index + delta
  if (target < 0 || target >= graphs.value.length) {
    return
  }
  const list = [...graphs.value]
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
  graphs.value = list
}

const remove = (index: number) => {
  graphs.value.splice(index, 1)
}

// saving
const body = (): GraphCollection => ({
  title: form.title.trim(),
  showTimespanButton: form.showTimespanButton,
  showGraphtypeButton: form.showGraphtypeButton,
  graphsPerLine: form.graphsPerLine,
  graphs: graphs.value.map(toStoredGraph)
})

const save = async (overwrite = false) => {
  submitted.value = true
  problems.value = []
  showStaleDialog.value = false
  if (!form.title.trim()) {
    return
  }
  saving.value = true
  await withSpinner(async () => {
    const result = isNew.value
      ? await createGraphCollection(body())
      : await updateGraphCollection(Number(props.id), body(), overwrite ? null : etag)
    if (!result.success) {
      if (isStaleResult(result)) {
        showStaleDialog.value = true
      } else if (result.errors?.length) {
        problems.value = result.errors
      } else {
        problems.value = [result.message]
      }
      return
    }
    report(result, isNew.value ? `Created "${form.title.trim()}".` : `Saved "${form.title.trim()}".`)
    leaveApproved = true
    // the leave guard refuses to leave mid-save, so the save is over before the navigation
    saving.value = false
    const savedId = result.payload?.collection.id ?? Number(props.id)
    router.push(`/graph-collections/${savedId}`)
  })
  saving.value = false
}

const reloadAfterStale = async () => {
  showStaleDialog.value = false
  problems.value = []
  await load()
}

// a collection that could not be loaded has no view to go back to
const goBack = () => {
  router.push(isNew.value || loadError.value ? '/graph-collections' : `/graph-collections/${props.id}`)
}

// unsaved changes
const showLeaveConfirm = ref(false)
let leaveResolvers: Array<(leave: boolean) => void> = []

const isDirty = () => !leaveApproved && !loading.value && !loadError.value && snapshot() !== pristine

// Every navigation while the editor is up either leaves it or moves it to another collection, and
// this one instance serves the new, copy and edit routes. A guard bound to a route record
// (onBeforeRouteLeave, onBeforeRouteUpdate) stays with the record matched when the editor was set
// up and misses the moves between those records, so the check sits on the router itself for as
// long as the editor is mounted.
const removeGuard = router.beforeEach(() => {
  if (saving.value) {
    return false
  }
  if (!isDirty()) {
    return true
  }
  showLeaveConfirm.value = true
  return new Promise<boolean>((resolve) => {
    leaveResolvers.push(resolve)
  })
})
onBeforeUnmount(removeGuard)

const settleLeave = (leave: boolean) => {
  showLeaveConfirm.value = false
  const resolvers = leaveResolvers
  leaveResolvers = []
  resolvers.forEach(resolve => resolve(leave))
}

// menubar links, reload and closing the tab bypass the router
const onBeforeUnload = (event: BeforeUnloadEvent) => {
  if (isDirty()) {
    event.preventDefault()
    event.returnValue = ''
  }
}
window.addEventListener('beforeunload', onBeforeUnload)
onBeforeUnmount(() => window.removeEventListener('beforeunload', onBeforeUnload))
</script>

<style scoped lang="scss">
.graph-collection-editor {
  padding: 0 2px 2rem 2px;

  :deep(.table-card) {
    padding: 1.5rem;
  }

  .header,
  .graphs-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 1rem;
  }

  .graphs-header {
    margin-top: 1.5rem;
    align-items: center;
  }

  .header-actions {
    display: flex;
    gap: 0.75rem;
  }

  .card-title {
    font-size: 1.25rem;
    font-weight: 600;
    overflow-wrap: anywhere;
  }

  .subtitle,
  .muted {
    color: var(--p-text-muted-color);
  }

  .options {
    display: flex;
    flex-wrap: wrap;
    gap: 1.5rem;
    align-items: flex-start;
  }

  .title-field {
    flex: 1 1 20rem;
  }

  .toggles {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    padding-top: 1.75rem;
  }

  .toggle {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
  }

  .wrap-cell {
    display: inline-block;
    max-width: 22rem;
    overflow-wrap: anywhere;
  }

  .action-container {
    display: flex;
    gap: 0.25rem;
  }

  .problems {
    border: 1px solid var(--p-red-500, #d32f2f);
    border-radius: 4px;
    padding: 0.5rem 0.75rem;
    margin-bottom: 1rem;

    p {
      margin: 0.15rem 0;
      color: var(--p-red-500, #d32f2f);
    }
  }

  .error {
    color: var(--p-red-500, #d32f2f);
  }
}

// teleported dialogs cannot sit under the page class
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}
</style>
