<!--
  Availability panel for the Node Availability page: every interface with monitored services and
  all their services, a page of interfaces at a time. Fetching, the range and the race between
  responses are the composable's, and the states NodeAvailabilityBody's, as for the Node Details
  panel; this panel only pages.

  The page mounts a new panel for each node (it shows none while the node loads), so a node starts
  at its first page with the default page size.
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

    <NodeAvailabilityBody
      :model="model"
      :ticks="ticks"
      :timeline="timeline"
      :range-label="rangeLabel"
      :base-href="baseHref"
      :node-id="node.id"
      :show-spinner="showSpinner"
      :is-fetching="isFetching"
      :load-failed="loadFailed"
      :has-loaded="hasLoaded"
      :total-interfaces="totalInterfaces"
    >
      <!-- In the footer, outside the states, so it stays in place while a page is fetched. -->
      <template #footer>
        <OnmsPaginator
          v-if="totalInterfaces > 0"
          class="availability-paginator"
          :first="first"
          :rows="rows"
          :total-records="totalInterfaces"
          :rows-per-page-options="[5, 10, 20, 50]"
          page-report="Interfaces {first}–{last} of {totalRecords}"
          data-test="availability-paginator"
          @page="onPage"
        />
      </template>
    </NodeAvailabilityBody>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { OnmsPaginator, OnmsTablePageEvent } from '@opennms/onms-ui'
import NodeDetailsPanel from '@/components/Nodes/NodeDetailsPanel.vue'
import NodeAvailabilityBody from '@/components/Nodes/NodeAvailabilityBody.vue'
import TimeControls from '@/components/Common/TimeControls.vue'
import { useNodeAvailability } from '@/components/Nodes/hooks/useNodeAvailability'
import { Node } from '@/types/index'

const props = defineProps<{
  baseHref: string
  node: Node
}>()

// The page of interfaces shown. The server computes figures for these only. The unit is the
// interface, each with all of its services, not the row -- so the paginator's report names it.
// Local to this panel, so each node, which gets a new panel, starts at its first page.
const first = ref(0)
const rows = ref(10)

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
</script>

<style lang="scss" scoped>
.availability-paginator {
  margin-top: 0.75rem;
}
</style>
