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

<!-- The graphs of a resolved collection, graphsPerLine to a row. -->
<template>
  <div
    class="gc-grid"
    :style="{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }"
    data-test="gc-grid"
  >
    <GraphCollectionCell
      v-for="graph in collection.graphs"
      :key="cellKey(graph)"
      :graph="graph"
      :canvasId="cellKey(graph)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import GraphCollectionCell from './GraphCollectionCell.vue'
import { ResolvedGraphCollection, ResolvedGraphCollectionGraph } from '@/types/graphCollection'

const props = defineProps<{
  collection: ResolvedGraphCollection
}>()

// the server applies the system default, but guard against a 0 from an older build
const columns = computed(() => Math.max(1, props.collection.graphsPerLine || 1))

// The window is part of a prefab cell's key: a timespan override changes the data of every graph,
// and a chart that is only updated keeps its zoom and legend state from the previous window. An
// ad-hoc cell is left out of that: it evaluates its stored sources against the server when it
// mounts, which can mean one request per node, so it keeps its store and re-queries on its own.
// Its stored state is in the key instead, since the cell only decodes it on mount: when the
// five-minute refresh finds the entry edited, deleted or reordered, the cell at that index is
// remade for the new definition rather than keeping the old one's sources under the new title.
const cellKey = (graph: ResolvedGraphCollectionGraph) => {
  const prefix = `gc-${props.collection.id ?? props.collection.type}-${graph.index}`
  return graph.kind === 'adhoc' ? `${prefix}-adhoc-${graph.adhocState}` : `${prefix}-${graph.graphtype}-${graph.start}-${graph.end}`
}
</script>

<style scoped lang="scss">
.gc-grid {
  display: grid;
  gap: 1rem;
  margin-top: 1rem;
}

@media (max-width: 900px) {
  .gc-grid {
    grid-template-columns: minmax(0, 1fr) !important;
  }
}
</style>
