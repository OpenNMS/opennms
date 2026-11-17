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

    <div class="availability-summary">
      <span class="subtitle2">Availability ({{ rangeLabel.toLowerCase() }})</span>
      <span
        class="availability-summary__value"
        data-test="node-availability"
      >{{ model ? nodeAvailabilityText(model) : '—' }}</span>
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
        <p
          v-if="isPartial"
          class="availability-partial"
          data-test="availability-partial"
        >
          Node has {{ countText }}, only some shown here. View them all on the
          <a
            :href="availabilityPageHref"
            data-test="availability-page-link"
          >Availability page</a>.
        </p>
      </template>
    </template>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsIconButton, OnmsSpinner } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import AvailabilityTimeline from './AvailabilityTimeline.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TimeControls from '@/components/Common/TimeControls.vue'
import { useNodeStore } from '@/stores/nodeStore'
import { selectionFromTime, selectionLabel, windowFor } from '@/components/Nodes/availabilityRange'
import { useDelayedLoading } from './hooks/useDelayedLoading'
import { plural } from '@/lib/utils'
import API from '@/services/index'
import { Node, NodeAvailability, StartEndTime } from '@/types/index'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import {
  buildTimelineModel,
  limitTimelineRows,
  nodeAvailabilityText,
  TimelineWindow
} from '@/components/Nodes/availabilityTimelineModel'
import { buildTicks } from './availabilityTimelineAxis'

const props = defineProps<{
  baseHref: string
  node: Node
}>()

const { isFetching, showSpinner, start: startLoading, stop: stopLoading } = useDelayedLoading()

const availability = ref<NodeAvailability | undefined>(undefined)
const timeline = ref<NodeOutageTimeline | null>(null)
const loadFailed = ref(false)

// A fetch has completed for some node. Without it the panel cannot tell "nothing has been asked
// for yet" from "asked, and the node has no monitored services", and would answer the first with
// the empty state's claim about the second.
const hasLoaded = ref(false)

const nodeStore = useNodeStore()

/**
 * The selection lives in the node store rather than here, so it survives a node change: the page
 * rebuilds this panel for each node, and a range held locally would be lost every time the user
 * moved on.
 */
const selection = computed(() => nodeStore.availabilityRange)

/** Seeds the picker's button, so a restored range is not contradicted by it on a fresh mount. */
const initialRangeLabel = selectionLabel(selection.value)

/**
 * The range the summary names, which is the one the displayed figure was fetched for, not the one
 * just picked. Updated with the data, alongside activeWindow.
 *
 * Setting it when the pick happens put the new range's name beside the old range's percentage until
 * the fetch landed -- 'Availability (last hour) 66.667%' where the figure was still the day's. The
 * picker's own button changes immediately, so the pick is still acknowledged at once.
 */
const rangeLabel = ref(initialRangeLabel)

// The window the rendered model describes: set from the fetch that produced it, so the axis and the
// bars cannot disagree.
const activeWindow = ref<TimelineWindow>(windowFor(selection.value))

const errorContent = {
  title: 'Availability unavailable',
  msg: 'Could not load availability for this node.'
}
const emptyContent = { msg: 'No monitored services on this node.' }

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

// The node's totals, from the last answer: not only the interfaces fetched.
const totalInterfaces = ref(0)
const totalServices = ref(0)

const isPartial = computed(() => totalInterfaces.value + totalServices.value > MAX_ROWS)

const countText = computed(() => `${plural(totalInterfaces.value, 'interface')} and ${plural(totalServices.value, 'service')}`)

// The legacy page lists every interface and service. The message and the button both go there.
const availabilityPageHref = computed(() => `${props.baseHref}element/availability.jsp?node=${props.node.id}`)

const onViewDetailsClick = () => {
  window.location.assign(availabilityPageHref.value)
}

// The node's figure comes from the whole roster; only the rows are cut.
const model = computed(() =>
  availability.value
    ? limitTimelineRows(buildTimelineModel(availability.value, timeline.value, activeWindow.value), MAX_ROWS)
    : undefined)

const ticks = computed(() => buildTicks(activeWindow.value))

// Monotonic per fetch, as nodeStore does for its own. Needed twice over here: the details page
// keeps one instance of this panel across node ids, and the range can change before a request for
// the same node has landed, so two responses can race.
let requestId = 0

const fetchAll = async () => {
  const id = props.node?.id

  if (!id) {
    return
  }

  const myRequest = ++requestId
  // Resolved here rather than held in state, so a relative range follows the clock.
  const requested = windowFor(selection.value)
  const requestedLabel = selectionLabel(selection.value)
  const { start, end } = requested

  startLoading()

  try {
    const [avail, outages] = await Promise.all([
      // withServices: the timeline drops interfaces with no monitored services, so the server leaves
      // them out before counting and limiting -- otherwise they could take the places of shown ones.
      API.getNodeAvailabilityPercentage(String(id), start, end, { limit: FETCH_INTERFACES, offset: 0, withServices: true }),
      API.getNodeOutageTimeline(id, start, end)
    ])

    // Superseded by a later fetch: drop the result. The stop() below still has to run, because
    // useDelayedLoading is reference counted and every start() needs its stop().
    if (myRequest !== requestId) {
      return
    }

    // Either failing is a failure: without the roster there are no rows, and without the outages
    // every row would render as fully available, which is a claim rather than an absence.
    if (!avail || !outages) {
      loadFailed.value = true
      availability.value = undefined
      timeline.value = null
      return
    }

    loadFailed.value = false
    hasLoaded.value = true
    activeWindow.value = requested
    rangeLabel.value = requestedLabel
    availability.value = avail
    timeline.value = outages
    // A server without the count sends every interface: count the ones the timeline would show.
    totalInterfaces.value = avail.ipinterfaceCount ?? avail.ipinterfaces.filter(iface => (iface.services ?? []).length > 0).length
    totalServices.value = avail['service-count'] ?? 0
  } finally {
    stopLoading()
  }
}

const onUpdateTime = (time: StartEndTime) => {
  // Replaced wholesale so the watcher below fires exactly once. TimeControls carries a relative
  // range when the pick came from the preset list, and only absolute times for a custom window.
  nodeStore.setAvailabilityRange(selectionFromTime(time))
}

// One watcher drives everything: mount, a node change and a range change each cause exactly one
// fetch, and a node and range changing in the same tick are coalesced into one.
//
// immediate is what actually fetches. The page rebuilds this panel for each node rather than
// feeding a new id to the existing one, so props.node.id never changes during an instance's life
// and a change-only watch would never fire at all.
watch([() => props.node?.id, selection], () => {
  fetchAll()
}, { immediate: true })
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

.availability-partial {
  margin: 0.5rem 0 0;
  font-size: 0.8125rem;
  color: var(--onms-secondary-text-on-surface);
}

.availability-summary__value {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}
</style>
