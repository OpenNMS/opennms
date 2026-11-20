<!--
  Availability panel for the Node Availability page: every interface with monitored services and
  all their services, a page of interfaces at a time. Fetching, the range and the race between
  responses are the composable's, as for the Node Details panel; this panel only pages.
-->
<template>
  <NodeDetailsPanel title="Availability">
    <template #actions>
      <TimeControls
        label="Range:"
        :initial-label="initialRangeLabel"
        data-test="availability-range"
        @update-time="onUpdateTime"
      />
    </template>

    <div class="availability-summary">
      <span class="subtitle2">Availability ({{ rangeLabel.toLowerCase() }})</span>
      <AvailabilityBadge
        class="availability-summary__value"
        data-test="node-availability"
        :text="model ? nodeAvailabilityText(model) : '—'"
        :level="model ? nodeAvailabilityClass(model) : undefined"
      />
    </div>

    <OnmsSpinner v-if="showSpinner" />

    <template v-else-if="!isFetching">
      <EmptyList
        v-if="loadFailed"
        :content="errorContent"
        data-test="availability-error"
      />
      <!-- From the node's total, not from the page fetched. -->
      <EmptyList
        v-else-if="hasLoaded && totalInterfaces === 0"
        :content="emptyContent"
        data-test="availability-empty"
      />
      <template v-else-if="model && model.interfaces.length > 0">
        <p
          v-if="timeline?.truncated"
          class="availability-truncated"
          data-test="availability-truncated"
        >
          Showing the {{ timeline.count }} most recent outages. Some are not drawn.
        </p>
        <AvailabilityTimeline
          :model="model"
          :ticks="ticks"
          :base-href="baseHref"
          :node-id="node.id"
          :range-label="rangeLabel"
        />
      </template>
    </template>

    <!-- Outside the states above, so it stays in place while a page is fetched. -->
    <OnmsPaginator
      v-if="totalInterfaces > 0"
      class="availability-paginator"
      :first="first"
      :rows="rows"
      :total-records="totalInterfaces"
      :rows-per-page-options="[10, 25, 50, 100]"
      data-test="availability-paginator"
      @page="onPage"
    />
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { OnmsPaginator, OnmsSpinner, OnmsTablePageEvent } from '@opennms/onms-ui'
import NodeDetailsPanel from '@/components/Nodes/NodeDetailsPanel.vue'
import AvailabilityBadge from '@/components/Nodes/AvailabilityBadge.vue'
import AvailabilityTimeline from '@/components/Nodes/AvailabilityTimeline.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TimeControls from '@/components/Common/TimeControls.vue'
import { useNodeAvailability } from '@/components/Nodes/hooks/useNodeAvailability'
import { nodeAvailabilityClass, nodeAvailabilityText } from '@/components/Nodes/availabilityTimelineModel'
import { Node } from '@/types/index'

const props = defineProps<{
  baseHref: string
  node: Node
}>()

// The page of interfaces shown. The server computes figures for these only.
const first = ref(0)
const rows = ref(25)

// Another node starts at its first page. Sync, and set up before the composable, so the node change
// and the reset reach the composable as one change: one fetch, at offset 0, rather than one at the
// old node's page first.
watch(() => props.node?.id, () => {
  first.value = 0
}, { flush: 'sync' })

const {
  model,
  ticks,
  initialRangeLabel,
  rangeLabel,
  onUpdateTime,
  isFetching,
  showSpinner,
  loadFailed,
  hasLoaded,
  totalInterfaces,
  timeline
} = useNodeAvailability(() => props.node?.id, () => ({ limit: rows.value, offset: first.value }))

// The roster can shrink under the page shown (interfaces or services removed since the last
// fetch). Go to the last page there is, rather than show an empty one.
watch(totalInterfaces, (total) => {
  if (total > 0 && first.value >= total) {
    first.value = Math.floor((total - 1) / rows.value) * rows.value
  }
})

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  rows.value = event.rows
}

const errorContent = {
  title: 'Availability unavailable',
  msg: 'Could not load availability for this node.'
}
const emptyContent = { msg: 'No monitored services on this node.' }
</script>

<style lang="scss" scoped>
.availability-summary {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.availability-truncated {
  margin: 0 0 0.5rem;
  font-size: 0.8125rem;
  color: var(--onms-secondary-text-on-surface);
}

.availability-summary__value {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}

.availability-paginator {
  margin-top: 0.75rem;
}
</style>
