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
  The editable part of an ad-hoc entry: its title and timespan. The graph itself is the
  builder's business, so the dialog links there; a changed graph comes back into the
  collection through the builder's own "Add to collection".
-->
<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Edit ad-hoc graph"
    width="34rem"
    data-test="adhoc-details"
    @update:visible="(value: boolean) => { if (!value) { emit('update:visible', false) } }"
  >
    <div class="details-body">
      <div class="current" data-test="adhoc-details-current">
        <span class="muted">Graph:</span>
        <strong>{{ graphTitle }}</strong>
        <router-link
          v-if="builderRoute"
          :to="builderRoute"
          target="_blank"
          data-test="adhoc-details-builder"
        >Open in the builder</router-link>
      </div>

      <FormField label="Title" for="adhoc-title" hint="Shown above the graph. Leave empty to use the graph's own title.">
        <OnmsInputText
          id="adhoc-title"
          v-model="title"
          :maxlength="255"
          fluid
          data-test="adhoc-title"
        />
      </FormField>
      <FormField label="Timespan" for="adhoc-timespan" required>
        <OnmsSelect
          inputId="adhoc-timespan"
          :modelValue="timespan"
          :options="timespans"
          optionLabel="label"
          optionValue="id"
          fluid
          data-test="adhoc-timespan"
          @update:modelValue="(value) => timespan = String(value ?? '')"
        />
      </FormField>
    </div>
    <div class="dialog-actions">
      <OnmsButton variant="ghost" label="Cancel" data-test="adhoc-cancel" @click="emit('update:visible', false)" />
      <OnmsButton label="Apply" :disabled="!timespan" data-test="adhoc-save" @click="save" />
    </div>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { GraphCollectionTimespan } from '@/types/graphCollection'
import { adhocBuilderRoute, adhocStateTitle } from './adhocCollectionState'
import { EditableGraph } from './editableGraph'

const props = defineProps<{
  visible: boolean
  /** The ad-hoc entry being changed. */
  graph: EditableGraph | null
  timespans: GraphCollectionTimespan[]
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  save: [graph: EditableGraph]
}>()

const title = ref('')
const timespan = ref('7_day')

const graphTitle = computed(() => adhocStateTitle(props.graph?.adhocState) || 'Custom performance graph')

const builderRoute = computed(() => (props.graph?.adhocState ? adhocBuilderRoute(props.graph.adhocState) : null))

watch(() => props.visible, (visible) => {
  if (visible) {
    title.value = props.graph?.title ?? ''
    timespan.value = props.graph?.timespan || '7_day'
  }
})

const save = () => {
  if (!props.graph || !timespan.value) {
    return
  }
  emit('save', { ...props.graph, title: title.value.trim(), timespan: timespan.value })
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.details-body {
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

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}
</style>
