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
  Add the graph shown on a resource graphs page to a graph collection, either an existing one or a
  new one named here. Replaces the legacy AngularJS "add to KSC" modal.
-->
<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Add to Graph Collection"
    width="34rem"
    data-test="add-to-collection"
    @update:visible="(value: boolean) => { if (!value) { emit('update:visible', false) } }"
  >
    <div class="dialog-body">
      <p v-if="loadError" class="error" data-test="atc-load-error">{{ loadError }}</p>

      <div v-if="problems.length" class="problems" role="alert" data-test="atc-problems">
        <p v-for="problem in problems" :key="problem">{{ problem }}</p>
      </div>

      <div class="resource" data-test="atc-resource">
        <span class="muted">Resource:</span> <strong>{{ resourceLabel }}</strong>
        <span class="muted">Graph:</span> <strong>{{ graphtype }}</strong>
      </div>

      <FormField label="Title" for="atc-title" hint="Shown above the graph. Leave empty to use the graph's own title.">
        <OnmsInputText id="atc-title" v-model="title" :maxlength="255" fluid data-test="atc-title" />
      </FormField>

      <FormField label="Timespan" for="atc-timespan" required>
        <OnmsSelect
          inputId="atc-timespan"
          :modelValue="timespan"
          :options="timespans"
          optionLabel="label"
          optionValue="id"
          :disabled="loading"
          fluid
          data-test="atc-timespan"
          @update:modelValue="(value) => timespan = String(value ?? '')"
        />
      </FormField>

      <FormField label="Collection" for="atc-target" required :error="targetError">
        <OnmsSelect
          inputId="atc-target"
          :modelValue="target"
          :options="targetOptions"
          optionLabel="label"
          optionValue="id"
          :placeholder="loading ? 'Loading collections...' : 'Choose a collection'"
          :disabled="loading"
          fluid
          data-test="atc-target"
          @update:modelValue="(value) => target = value === null || value === undefined ? null : String(value)"
        />
        <p v-if="alreadyIn" class="muted" data-test="atc-duplicate">
          This graph is already in "{{ alreadyIn }}". Adding it again shows it twice.
        </p>
      </FormField>

      <FormField v-if="target === NEW_COLLECTION" label="New collection title" for="atc-new-title" required :error="newTitleError">
        <OnmsInputText id="atc-new-title" v-model="newTitle" :maxlength="255" fluid data-test="atc-new-title" />
      </FormField>
    </div>
    <div class="dialog-actions">
      <OnmsButton variant="ghost" label="Cancel" data-test="atc-cancel" @click="emit('update:visible', false)" />
      <OnmsButton label="Add" :disabled="!canSave" data-test="atc-save" @click="submit" />
    </div>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import {
  addGraphToCollection,
  createGraphCollection,
  getGraphCollections,
  getGraphCollectionTimespans
} from '@/services/graphCollectionsService'
import { GraphCollection, GraphCollectionGraph, GraphCollectionTimespan } from '@/types/graphCollection'

const NEW_COLLECTION = 'new'

const props = defineProps<{
  visible: boolean
  resourceId: string
  resourceLabel: string
  /** The prefabricated graph name. */
  graphtype: string
  /** The graph's own title, offered as the default entry title. */
  graphTitle: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  added: [collection: GraphCollection]
}>()

const { showSuccess } = useActionFeedback()

const collections = ref<GraphCollection[]>([])
const timespans = ref<GraphCollectionTimespan[]>([])
const loading = ref(false)
const loadError = ref('')
const title = ref('')
const timespan = ref('1_day')
const target = ref<string | null>(null)
const newTitle = ref('')
const submitted = ref(false)
const saving = ref(false)
const problems = ref<string[]>([])

const targetOptions = computed(() => [
  ...collections.value.map(c => ({ id: String(c.id), label: c.title })),
  { id: NEW_COLLECTION, label: 'New collection...' }
])

const targetCollection = computed(() =>
  target.value && target.value !== NEW_COLLECTION ? collections.value.find(c => String(c.id) === target.value) ?? null : null)

// the legacy modal refused a graph already in the report; a second entry with another timespan is
// a legitimate use, so this only says so
const alreadyIn = computed(() =>
  targetCollection.value?.graphs.some(g => g.resourceId === props.resourceId && g.graphtype === props.graphtype)
    ? targetCollection.value.title
    : '')

const targetError = computed(() => submitted.value && !target.value ? 'Choose a collection.' : '')
const newTitleError = computed(() =>
  submitted.value && target.value === NEW_COLLECTION && !newTitle.value.trim() ? 'A title is required.' : '')

const canSave = computed(() => !loading.value && !loadError.value && !saving.value && !!timespan.value)

const load = async () => {
  loading.value = true
  loadError.value = ''
  const [list, spans] = await Promise.all([getGraphCollections(), getGraphCollectionTimespans()])
  loading.value = false
  if (!list.success || !spans.success) {
    loadError.value = list.success ? spans.message : list.message
    return
  }
  collections.value = list.payload ?? []
  timespans.value = spans.payload ?? []
  // with nothing to add to yet, the only choice is a new collection
  if (!collections.value.length) {
    target.value = NEW_COLLECTION
  }
}

const reset = () => {
  title.value = props.graphTitle
  timespan.value = '1_day'
  target.value = null
  newTitle.value = ''
  submitted.value = false
  problems.value = []
  collections.value = []
  load()
}

watch(() => props.visible, (visible) => {
  if (visible) {
    reset()
  }
}, { immediate: true })

const submit = async () => {
  submitted.value = true
  problems.value = []
  if (!target.value || (target.value === NEW_COLLECTION && !newTitle.value.trim())) {
    return
  }
  const graph: GraphCollectionGraph = {
    kind: 'prefab',
    title: title.value.trim(),
    resourceId: props.resourceId,
    graphtype: props.graphtype,
    timespan: timespan.value
  }
  saving.value = true
  const result = target.value === NEW_COLLECTION
    ? await createGraphCollection({
      title: newTitle.value.trim(),
      showTimespanButton: false,
      showGraphtypeButton: false,
      graphsPerLine: 0,
      graphs: [graph]
    })
    : await addGraphToCollection(Number(target.value), graph)
  saving.value = false
  if (!result.success || !result.payload) {
    problems.value = result.errors?.length ? result.errors : [result.message]
    return
  }
  const saved = result.payload.collection
  showSuccess(`Added "${graph.title || props.graphtype}" to "${saved.title}".`)
  emit('added', saved)
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;
}

.resource {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 0.5rem;
  overflow-wrap: anywhere;
}

.muted {
  color: var(--p-text-muted-color);
}

.error {
  color: var(--p-red-500, #d32f2f);
}

.problems {
  border: 1px solid var(--p-red-500, #d32f2f);
  border-radius: 4px;
  padding: 0.5rem 0.75rem;

  p {
    margin: 0.15rem 0;
    color: var(--p-red-500, #d32f2f);
  }
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}
</style>
