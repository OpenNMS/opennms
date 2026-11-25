<!--
  The body of an availability panel, shared by the Node Details panel and the Node Availability page:
  the summary line with the node's figure, then the spinner, the error or empty state, or the
  timeline. The fetching is the caller's (useNodeAvailability); this only words the states.

  Slots:
  - after-timeline: shown under the timeline, and only with it (the Node Details panel's "only some
    shown" message).
  - footer: shown after the states, whatever they are, so it stays in place while a page is fetched
    (the Node Availability page's paginator).
-->
<template>
  <div class="availability-summary">
    <span class="subtitle2">Availability ({{ rangeLabel.toLowerCase() }})</span>
    <!-- The data-test overrides the badge's own on purpose: the smoke tests select the node figure by it. -->
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
    <!-- From the node's total, not from the interfaces fetched. -->
    <EmptyList
      v-else-if="hasLoaded && totalInterfaces === 0"
      :content="emptyContent"
      data-test="availability-empty"
    />
    <template v-else-if="model && model.interfaces.length > 0 && nodeId !== undefined">
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
        :node-id="nodeId"
        :range-label="rangeLabel"
      />
      <slot name="after-timeline" />
    </template>
  </template>

  <slot name="footer" />
</template>

<script setup lang="ts">
import { OnmsSpinner } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import AvailabilityBadge from './AvailabilityBadge.vue'
import AvailabilityTimeline from './AvailabilityTimeline.vue'
import { nodeAvailabilityClass, nodeAvailabilityText, TimelineModel } from './availabilityTimelineModel'
import { TimelineTick } from './availabilityTimelineAxis'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'

defineProps<{
  /** The rows to draw. The Node Details panel passes it cut to its row limit; the node figure is the node's either way. */
  model?: TimelineModel
  ticks: TimelineTick[]
  timeline: NodeOutageTimeline | null
  /** The range the figures were fetched for, e.g. 'Last day'. */
  rangeLabel: string
  baseHref: string
  /**
   * Optional: the body can be shown before a node is known, and then has nothing but its summary to
   * draw. The timeline, which links by node id, needs it -- and has no model to draw until a fetch
   * for a node has landed anyway.
   */
  nodeId?: string | number
  showSpinner: boolean
  isFetching: boolean
  loadFailed: boolean
  hasLoaded: boolean
  /** The node's total of interfaces with monitored services, not only the ones fetched. */
  totalInterfaces: number
}>()

defineSlots<{
  'after-timeline'?: () => unknown
  footer?: () => unknown
}>()

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

.availability-summary__value {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}

.availability-truncated {
  margin: 0 0 0.5rem;
  font-size: 0.8125rem;
  color: var(--onms-secondary-text-on-surface);
}
</style>
