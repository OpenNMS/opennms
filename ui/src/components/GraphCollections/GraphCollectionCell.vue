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
  One graph of a graph collection's view: the legacy custom view's cell. The header
  carries the graph's own title, the window, and links to the resource, its parent
  and the single-graph detail page; the body is the prefabricated graph, or a
  placeholder when the server says the entry cannot be drawn.
-->
<template>
  <div class="gc-cell" data-test="gc-cell" :data-pdf-caption="pdfCaption">
    <div class="gc-cell__header">
      <div class="gc-cell__title-block">
        <div class="gc-cell__title" data-test="gc-title">{{ heading }}</div>
        <div class="gc-cell__window" data-test="gc-window">
          From {{ formatGraphInstant(graph.start) }} to {{ formatGraphInstant(graph.end) }}
        </div>
      </div>
      <div class="gc-cell__resource">
        <template v-if="graph.resource?.parent">
          <span class="gc-cell__type">{{ graph.resource.parent.typeLabel ?? 'Parent' }}:</span>
          <ResourceLink :resource="graph.resource.parent" data-test="gc-parent" />
        </template>
        <template v-if="graph.resource">
          <span class="gc-cell__type">{{ graph.resource.typeLabel ?? 'Resource' }}:</span>
          <ResourceLink :resource="graph.resource" data-test="gc-resource" />
        </template>
        <router-link
          v-if="detailRoute"
          :to="detailRoute"
          target="_blank"
          class="gc-cell__detail"
          data-test="gc-detail"
        >Detail</router-link>
      </div>
    </div>

    <div v-if="graph.valid && graph.resource" class="gc-cell__graph">
      <Graph
        :canvasId="canvasId"
        :definition="graph.graphtype"
        :resourceId="graph.resource.id"
        :time="time"
        :label="graph.resource.label"
        :isSingleGraph="true"
      />
    </div>
    <div v-else class="gc-cell__placeholder" role="note" data-test="gc-placeholder">
      <strong>This graph cannot be drawn.</strong>
      <span v-if="graph.error">{{ graph.error }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { RouterLink } from 'vue-router'
import Graph from '@/components/Resources/Graph.vue'
import { GraphCollectionResource, ResolvedGraphCollectionGraph } from '@/types/graphCollection'
import { formatGraphInstant, graphTimeFor, legacyHref, spaRouteForLegacyLink } from './graphCollectionTime'

const props = defineProps<{
  graph: ResolvedGraphCollectionGraph
  /** Unique within the page: two cells may draw the same graph of the same resource. */
  canvasId: string
}>()

// The resource's label, linked the way the legacy view did it: to the node page when the
// resource is a node, to the legacy page the server names otherwise, or not at all.
const ResourceLink = (p: { resource: GraphCollectionResource }, { attrs }: { attrs: Record<string, unknown> }) => {
  const link = p.resource.link
  if (!link) {
    return h('span', attrs, p.resource.label)
  }
  const route = spaRouteForLegacyLink(link)
  if (route) {
    return h(RouterLink, { ...attrs, to: route }, () => p.resource.label)
  }
  return h('a', { ...attrs, href: legacyHref(link) }, p.resource.label)
}
// declared so the data-test attribute falls through to the rendered element instead of becoming a prop
ResourceLink.props = ['resource']

const heading = computed(() => props.graph.title || props.graph.prefabGraphTitle || props.graph.graphtype)

const time = computed(() => graphTimeFor(props.graph.start, props.graph.end))

// the cell header as plain text, which the collection's PDF export prints above the graph
const pdfCaption = computed(() => {
  const resource = props.graph.resource
  const resourceLine = [resource?.parent, resource]
    .filter((r): r is GraphCollectionResource => !!r)
    .map(r => `${r.typeLabel ?? 'Resource'}: ${r.label}`)
    .join('    ')
  return [
    heading.value,
    `From ${formatGraphInstant(props.graph.start)} to ${formatGraphInstant(props.graph.end)}`,
    resourceLine
  ].filter(Boolean).join('\n')
})

// The single-graph page: one prefabricated graph for one resource (NMS-15711), not every graph of
// the resource. Resource labels can hold slashes, so the segments are encoded by hand.
const detailRoute = computed(() => {
  const resource = props.graph.resource
  if (!resource || !props.graph.valid) {
    return null
  }
  const segments = [resource.label, props.graph.graphtype, resource.id].map(encodeURIComponent)
  return { path: `/resource-graphs/graphs/${segments.join('/')}` }
})
</script>

<style scoped lang="scss">
.gc-cell {
  border: 1px solid var(--p-content-border-color);
  border-radius: 4px;
  background: var(--p-content-background);
  display: flex;
  flex-direction: column;
  min-width: 0;

  &__header {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
    padding: 0.5rem 0.75rem;
    background: var(--p-datatable-header-cell-background);
    border-bottom: 1px solid var(--p-content-border-color);
    font-size: 0.875rem;
  }

  &__title {
    font-weight: 600;
    overflow-wrap: anywhere;
  }

  &__window {
    color: var(--p-text-muted-color);
  }

  &__resource {
    display: grid;
    grid-template-columns: auto auto;
    column-gap: 0.4rem;
    align-content: start;
    text-align: right;
    overflow-wrap: anywhere;
  }

  &__type {
    color: var(--p-text-muted-color);
  }

  &__detail {
    grid-column: 1 / -1;
  }

  &__graph {
    min-width: 0;
  }

  &__placeholder {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    padding: 1.5rem 1rem;
    color: var(--p-text-muted-color);
  }
}
</style>
