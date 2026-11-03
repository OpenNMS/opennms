<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!--
  Add a graph to a collection, or change one: the legacy "choose resource" and
  "edit details" pages folded into one dialog. Pick a node, one of its resources
  and one of the prefabricated graphs available for it, then give the entry a
  title and a timespan.
-->
<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="graph ? 'Edit graph' : 'Add graph'"
    width="44rem"
    data-test="graph-picker"
    @update:visible="(value: boolean) => { if (!value) { emit('update:visible', false) } }"
  >
    <div class="picker-body">
      <div class="current" v-if="resource" data-test="picker-current">
        <span class="muted">Resource:</span>
        <strong>{{ resource.label }}</strong>
        <span class="muted" v-if="resource.typeLabel">({{ resource.typeLabel }})</span>
        <OnmsButton variant="text" size="small" label="Change" data-test="picker-change" @click="startOver" />
      </div>

      <template v-if="!resource">
        <FormField label="Node" for="picker-node" hint="Start typing a node label.">
          <OnmsAutoComplete
            inputId="picker-node"
            :modelValue="nodeSelection"
            :suggestions="nodeSuggestions"
            optionLabel="label"
            placeholder="Search by node label"
            fluid
            data-test="picker-node"
            @complete="onNodeComplete"
            @update:modelValue="onNodePicked"
          />
        </FormField>

        <FormField v-if="node" label="Resource" for="picker-resource-filter">
          <OnmsSearchInput
            inputId="picker-resource-filter"
            :modelValue="resourceFilter"
            placeholder="Filter resources"
            dataTest="picker-resource-filter"
            @update:modelValue="(value) => resourceFilter = String(value ?? '')"
          />
          <p v-if="loadingResources" class="muted" data-test="picker-resources-loading">Loading resources...</p>
          <p v-else-if="!visibleResources.length" class="muted" data-test="picker-resources-empty">
            {{ resourceError || (resources.length ? 'No resource matches the filter.' : 'This node has no resources with graphs.') }}
          </p>
          <OnmsListbox
            v-else
            :options="visibleResources"
            :modelValue="null"
            dataKey="id"
            optionLabel="label"
            scrollHeight="14rem"
            aria-label="Resources"
            data-test="picker-resources"
            @update:modelValue="(value) => pickResource(value as PickedResource)"
          >
            <template #option="{ option }">
              <span class="option">
                <span>{{ asResource(option).label }}</span>
                <span class="muted">{{ asResource(option).typeLabel }}</span>
              </span>
            </template>
          </OnmsListbox>
        </FormField>
      </template>

      <template v-if="resource">
        <FormField label="Graph type" for="picker-graphtype" required :error="graphtypeError">
          <OnmsSelect
            inputId="picker-graphtype"
            :modelValue="graphtype"
            :options="graphtypeOptions"
            optionLabel="label"
            optionValue="id"
            :placeholder="loadingGraphtypes ? 'Loading graph types...' : (graphtypeOptions.length ? 'Choose a graph' : 'No graphs available')"
            :disabled="loadingGraphtypes"
            fluid
            data-test="picker-graphtype"
            @update:modelValue="(value) => graphtype = String(value ?? '')"
          />
        </FormField>
        <FormField label="Title" for="picker-title" hint="Shown above the graph. Leave empty to use the graph's own title.">
          <OnmsInputText
            id="picker-title"
            v-model="title"
            :maxlength="255"
            fluid
            data-test="picker-title"
          />
        </FormField>
        <FormField label="Timespan" for="picker-timespan" required>
          <OnmsSelect
            inputId="picker-timespan"
            :modelValue="timespan"
            :options="timespans"
            optionLabel="label"
            optionValue="id"
            fluid
            data-test="picker-timespan"
            @update:modelValue="(value) => timespan = String(value ?? '')"
          />
        </FormField>
      </template>
    </div>
    <div class="dialog-actions">
      <OnmsButton variant="ghost" label="Cancel" data-test="picker-cancel" @click="emit('update:visible', false)" />
      <OnmsButton
        :label="graph ? 'Apply' : 'Add'"
        :disabled="!canSave"
        data-test="picker-save"
        @click="save"
      />
    </div>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsAutoComplete, OnmsButton, OnmsDialog, OnmsInputText, OnmsListbox, OnmsSearchInput, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { getGraphDefinitionsByResourceId } from '@/services/graphService'
import { getResourceForNode } from '@/services/resourceService'
import { searchOutageNodes } from '@/services/scheduledOutagesService'
import { GraphCollectionTimespan } from '@/types/graphCollection'
import { EditableGraph } from './editableGraph'

interface NodeSuggestion { id: number, label: string }
interface PickedResource { id: string, label: string, typeLabel: string }

const props = defineProps<{
  visible: boolean
  /** The entry being changed, or null to add one. */
  graph: EditableGraph | null
  timespans: GraphCollectionTimespan[]
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  save: [graph: EditableGraph]
}>()

const nodeSelection = ref<NodeSuggestion | string | null>(null)
const nodeSuggestions = ref<NodeSuggestion[]>([])
const node = ref<NodeSuggestion | null>(null)
const resources = ref<PickedResource[]>([])
const resourceFilter = ref('')
const loadingResources = ref(false)
const resourceError = ref('')
const resource = ref<PickedResource | null>(null)
const graphtypes = ref<string[]>([])
const loadingGraphtypes = ref(false)
const graphtype = ref('')
const title = ref('')
const timespan = ref('7_day')
const submitted = ref(false)

// Bumped by every new lookup and by Change, so a slow answer to an earlier node or resource pick
// cannot land on top of a later one.
let lookup = 0

// the listbox option slot is untyped
const asResource = (option: unknown) => option as PickedResource

const visibleResources = computed(() => {
  const term = resourceFilter.value.trim().toLowerCase()
  return term
    ? resources.value.filter(r => r.label.toLowerCase().includes(term) || r.typeLabel.toLowerCase().includes(term))
    : resources.value
})

// The stored graph type stays selectable even when the resource no longer offers it, so an entry
// can be re-timed without being re-pointed; the editor flags it as invalid either way.
const graphtypeOptions = computed(() => {
  const names = [...graphtypes.value]
  if (graphtype.value && !names.includes(graphtype.value)) {
    names.unshift(graphtype.value)
  }
  return names.map(id => ({ id, label: id }))
})

const graphtypeError = computed(() => {
  if (resource.value && !loadingGraphtypes.value && !graphtypeOptions.value.length) {
    return 'No graphs are available for this resource.'
  }
  return submitted.value && !graphtype.value ? 'Choose a graph type.' : ''
})

const canSave = computed(() => !!resource.value && !!graphtype.value && !!timespan.value && !loadingGraphtypes.value)

// the node typeahead; the outage picker's search is a plain label search and fits here too
const onNodeComplete = async (query: string) => {
  nodeSuggestions.value = await searchOutageNodes(query)
}

// A child that reports its graph attributes but has none (a bare container) cannot be graphed;
// one that does not report them at all is kept, since only the graph list can tell.
const graphable = (child: { rrdGraphAttributes?: Record<string, unknown> }) =>
  child.rrdGraphAttributes === undefined || Object.keys(child.rrdGraphAttributes).length > 0

const onNodePicked = async (value: unknown) => {
  nodeSelection.value = value as NodeSuggestion | string | null
  if (!value || typeof value === 'string') {
    return
  }
  const current = ++lookup
  node.value = value as NodeSuggestion
  resources.value = []
  resourceFilter.value = ''
  resourceError.value = ''
  loadingResources.value = true
  const nodeResource = await getResourceForNode(String(node.value.id))
  if (current !== lookup) {
    return
  }
  loadingResources.value = false
  if (nodeResource === null) {
    // the service answers null for a node without resources and for a failed read alike
    resourceError.value = 'No resources were found for this node, or they could not be read.'
    return
  }
  resources.value = (nodeResource.children?.resource ?? [])
    .filter(graphable)
    .map(r => ({ id: r.id, label: r.label, typeLabel: r.typeLabel }))
}

const loadGraphtypes = async (resourceId: string) => {
  const current = ++lookup
  loadingGraphtypes.value = true
  const response = await getGraphDefinitionsByResourceId(resourceId)
  if (current !== lookup) {
    return
  }
  loadingGraphtypes.value = false
  graphtypes.value = response.name ?? []
  if (graphtypes.value.length === 1 && !graphtype.value) {
    graphtype.value = graphtypes.value[0]
  }
}

const pickResource = async (picked: PickedResource | null) => {
  if (!picked) {
    return
  }
  resource.value = picked
  graphtype.value = ''
  await loadGraphtypes(picked.id)
}

const startOver = () => {
  lookup++
  loadingResources.value = false
  loadingGraphtypes.value = false
  resource.value = null
  graphtypes.value = []
  graphtype.value = ''
  node.value = null
  nodeSelection.value = null
  resources.value = []
  resourceError.value = ''
}

const reset = () => {
  startOver()
  submitted.value = false
  title.value = props.graph?.title ?? ''
  timespan.value = props.graph?.timespan || '7_day'
  if (props.graph?.resourceId) {
    resource.value = {
      id: props.graph.resourceId,
      label: props.graph.resourceLabel ?? props.graph.resourceId,
      typeLabel: props.graph.resourceTypeLabel ?? ''
    }
    graphtype.value = props.graph.graphtype
    loadGraphtypes(props.graph.resourceId)
  }
}

watch(() => props.visible, (visible) => {
  if (visible) {
    reset()
  }
})

const save = () => {
  submitted.value = true
  if (!canSave.value || !resource.value) {
    return
  }
  // An entry moved to another resource got there through a node pick, so that node is its parent
  // now, and the pre-resourceId address an older file gave the old resource no longer applies.
  const repointed = resource.value.id !== props.graph?.resourceId
  emit('save', {
    ...(props.graph ?? { key: 0 }),
    ...(repointed ? { parentLabel: node.value?.label, nodeId: null, nodeSource: null, domain: null, interfaceId: null } : {}),
    kind: 'prefab',
    title: title.value.trim(),
    resourceId: resource.value.id,
    resourceLabel: resource.value.label,
    resourceTypeLabel: resource.value.typeLabel,
    graphtype: graphtype.value,
    timespan: timespan.value,
    // a re-pointed or re-typed entry is valid until the server says otherwise on the next load
    valid: true,
    error: null
  })
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.picker-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;
}

.current {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.muted {
  color: var(--p-text-muted-color);
}

.option {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}
</style>
