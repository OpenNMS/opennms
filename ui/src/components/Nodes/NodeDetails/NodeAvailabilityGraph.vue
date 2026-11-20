<!--
  Availability panel for the node details page.

  The timeline is rendered here from JSON rather than fetched as a server-drawn PNG, so it follows
  the page theme, resizes with the panel, and carries a link and a description per outage. Two
  documents make it up: the availability resource supplies the service roster and the percentages,
  and the outage timeline supplies the bars. They join on the monitored service id.
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
      <OnmsIconButton
        aria-label="View Availability"
        tooltip="View Availability"
        data-test="availability-details-button"
        :icon="IconViewDetails"
        @click="onViewDetailsClick"
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
      <template #after-timeline>
        <p
          v-if="isPartial"
          class="availability-partial"
          data-test="availability-partial"
        >
          Node has {{ countText }}, only some shown here. View them all on the
          <router-link
            :to="nodeAvailabilityRoute(node.id)"
            data-test="availability-page-link"
          >Availability page</router-link>.
        </p>
      </template>
    </NodeAvailabilityBody>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { OnmsIconButton } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import NodeDetailsPanel from '@/components/Nodes/NodeDetailsPanel.vue'
import NodeAvailabilityBody from '@/components/Nodes/NodeAvailabilityBody.vue'
import TimeControls from '@/components/Common/TimeControls.vue'
import { useNodeAvailability } from '@/components/Nodes/hooks/useNodeAvailability'
import { nodeAvailabilityRoute } from '@/lib/nodeDetailsRoute'
import { plural } from '@/lib/utils'
import { Node } from '@/types/index'
import { limitTimelineRows } from '@/components/Nodes/availabilityTimelineModel'

const props = defineProps<{
  baseHref: string
  node: Node
}>()

/**
 * The most rows the timeline shows: a heading per interface plus one per service. More would take
 * too much of the page, so a bigger node shows its first rows and a link to the Availability page,
 * which lists them all.
 */
const MAX_ROWS = 8

/**
 * Interfaces to ask for. Each takes at least two rows (its heading and a service), so no more than
 * this can show. The roster's figures are a database query each -- per interface and per service --
 * so the server computes only these, rather than every interface on a node that may have hundreds.
 */
const FETCH_INTERFACES = MAX_ROWS / 2

// Fetching, the range in the node store, and the race between responses all live in the
// composable, and the states are NodeAvailabilityBody's; this panel only cuts the rows.
const {
  model: fullModel,
  ticks,
  initialRangeLabel,
  rangeLabel,
  onUpdateTime,
  isFetching,
  showSpinner,
  loadFailed,
  hasLoaded,
  totalInterfaces,
  totalServices,
  timeline
} = useNodeAvailability(() => props.node?.id, { limit: FETCH_INTERFACES, offset: 0 })

const isPartial = computed(() => totalInterfaces.value + totalServices.value > MAX_ROWS)

// "Monitored": the count is of the interfaces with monitored services only (withServices), which the
// IP Interfaces table on the same tab may outnumber.
const countText = computed(() => `${plural(totalInterfaces.value, 'monitored interface')} and ${plural(totalServices.value, 'service')}`)

const router = useRouter()

// The Availability page lists every interface and service. The message and the button both go there.
const onViewDetailsClick = () => {
  router.push(nodeAvailabilityRoute(props.node.id))
}

// The node's figure comes from the whole roster; only the rows are cut.
const model = computed(() => fullModel.value ? limitTimelineRows(fullModel.value, MAX_ROWS) : undefined)
</script>

<style lang="scss" scoped>
.availability-partial {
  margin: 0.5rem 0 0;
  font-size: 0.8125rem;
  color: var(--onms-secondary-text-on-surface);
}
</style>
