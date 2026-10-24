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
  An ad-hoc graph inside a graph collection: the builder state stored with the entry,
  evaluated against the server as it stands now and drawn over the window the
  collection gives it. Each cell owns a store instance and a definition of its own, so
  several can share a page without sharing filters, series or in-flight queries.
-->
<template>
  <div class="gc-adhoc" data-test="gc-adhoc">
    <p v-if="decodeError" class="gc-adhoc__error" role="note" data-test="gc-adhoc-error">{{ decodeError }}</p>
    <AdhocChart
      v-else
      :canvasId="canvasId"
      :config="config"
      :measurements="store.measurements"
      :time="time"
      :loading="restoring || store.queryLoading"
      :error="status"
    />
  </div>
</template>

<script setup lang="ts">
import { getActivePinia } from 'pinia'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import AdhocChart from '@/components/AdhocGraphs/AdhocChart.vue'
import { useAdhocGraphDefinition } from '@/components/AdhocGraphs/useAdhocGraphDefinition'
import { decodeAdhocState } from '@/components/AdhocGraphs/utils/adhocUrlState'
import { defineAdhocGraphStore } from '@/stores/adhocGraphStore'
import { adhocStateToQuery } from './adhocCollectionState'
import { graphTimeFor } from './graphCollectionTime'

const props = defineProps<{
  /** The stored builder state: the share link's query string without its window. */
  adhocState: string
  /** The window, in epoch milliseconds. */
  start: number
  end: number
  /** Unique within the page; also names this cell's store. */
  canvasId: string
}>()

const store = defineAdhocGraphStore(`adhocGraph:${props.canvasId}`)()
const { config, time, tooManySeriesMessage, updateTime, runQuery, restore } = useAdhocGraphDefinition(store)

const restoring = ref(true)
const decodeError = ref('')

// The chart's own empty states tell the user to pick datasources, which assumes the builder's
// picker is on the page. Here the sources are stored, so say what became of them instead.
const status = computed(() => {
  if (tooManySeriesMessage.value) {
    return tooManySeriesMessage.value
  }
  if (store.nodeFilterError) {
    return `The stored node filter could not be applied: ${store.nodeFilterError}`
  }
  if (store.queryError) {
    return store.queryError
  }
  if (!restoring.value && !config.series.length) {
    return 'The stored sources match nothing now. Open the graph in the builder to check them.'
  }
  return ''
})

// The collection's window is absolute, so there is no relative range to slide.
const draw = () => {
  updateTime(graphTimeFor(props.start, props.end))
  runQuery()
}

onMounted(async () => {
  const restored = decodeAdhocState(adhocStateToQuery(props.adhocState))
  if (!restored) {
    decodeError.value = 'The stored graph definition could not be read.'
    restoring.value = false
    return
  }
  await restore(restored)
  restoring.value = false
  draw()
})

watch(() => [props.start, props.end], () => {
  if (!restoring.value && !decodeError.value) {
    draw()
  }
})

// The store was made for this cell alone, so it goes with it; Pinia keeps a disposed store's
// state until told otherwise.
onBeforeUnmount(() => {
  store.$dispose()
  const pinia = getActivePinia()
  if (pinia) {
    delete pinia.state.value[store.$id]
  }
})
</script>

<style scoped lang="scss">
.gc-adhoc {
  min-width: 0;
}

.gc-adhoc__error {
  padding: 1.5rem 1rem;
  color: var(--p-text-muted-color);
}
</style>
