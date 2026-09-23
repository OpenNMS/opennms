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
        data-test="availability-range"
        @update-time="onUpdateTime"
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
      <EmptyList
        v-else-if="hasLoaded && (!model || model.interfaces.length === 0)"
        :content="emptyContent"
        data-test="availability-empty"
      />
      <AvailabilityTimeline
        v-else-if="model && model.interfaces.length > 0"
        :model="model"
        :ticks="ticks"
        :base-href="baseHref"
        :node-id="node.id"
        :range-label="rangeLabel"
      />
    </template>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsSpinner } from '@opennms/onms-ui'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import AvailabilityTimeline from './AvailabilityTimeline.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TimeControls from '@/components/Common/TimeControls.vue'
import {
  DEFAULT_RANGE,
  relativeRangeOf,
  resolveRelativeRange,
  TIME_RANGE_OPTIONS
} from '@/components/Common/utils/timeRangeOptions'
import { useDelayedLoading } from './hooks/useDelayedLoading'
import API from '@/services'
import { Node, NodeAvailability, RelativeTimeRange, StartEndTime } from '@/types'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import {
  buildTimelineModel,
  nodeAvailabilityText,
  TimelineWindow
} from './availabilityTimelineModel'
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

/**
 * What the user picked, rather than the window it resolved to at the time.
 *
 * A relative range has to be re-resolved against the clock on every fetch. This panel outlives a
 * node change -- the details page keeps one instance across node ids -- so a window worked out once
 * at setup would go stale, and moving to another node an hour later would ask for the hour-old
 * window. A custom absolute range is a fixed window and is kept as one.
 */
type RangeSelection =
  | { kind: 'relative'; range: RelativeTimeRange }
  | { kind: 'absolute'; start: number; end: number }

const toMillis = (time: StartEndTime): TimelineWindow => ({
  start: Number(time.startTime) * 1000,
  end: Number(time.endTime) * 1000
})

const windowFor = (sel: RangeSelection): TimelineWindow =>
  sel.kind === 'relative' ? toMillis(resolveRelativeRange(sel.range)) : { start: sel.start, end: sel.end }

/**
 * The picker's own wording for a range, so the heading and the picker's button agree. Building a
 * label out of unit and amount instead gave 'last 1 hours' for Last hour, and 'last 24 hours' for
 * Last day while the button said 'LAST DAY'.
 */
const labelFor = (range: RelativeTimeRange | undefined): string => {
  if (!range) {
    return 'custom range'
  }

  const option = TIME_RANGE_OPTIONS.find((o) => {
    const r = relativeRangeOf(o)
    return r?.unit === range.unit && r?.amount === range.amount
  })

  if (option) {
    return option.label
  }

  // Not one of the offered options, so there is no label to borrow.
  const unit = range.amount === 1 ? range.unit.replace(/s$/, '') : range.unit
  return `Last ${range.amount} ${unit}`
}

// Seeded to the range TimeControls shows before anything is picked, so the heading and the button
// agree from the first paint.
const selection = ref<RangeSelection>({ kind: 'relative', range: DEFAULT_RANGE })
const rangeLabel = ref(labelFor(DEFAULT_RANGE))

// The window the rendered model describes: set from the fetch that produced it, so the axis and the
// bars cannot disagree.
const activeWindow = ref<TimelineWindow>(windowFor(selection.value))

const errorContent = {
  title: 'Availability unavailable',
  msg: 'Could not load availability for this node.'
}
const emptyContent = { msg: 'No monitored services on this node.' }

const model = computed(() =>
  availability.value ? buildTimelineModel(availability.value, timeline.value, activeWindow.value) : undefined)

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
  const { start, end } = requested

  startLoading()

  try {
    const [avail, outages] = await Promise.all([
      API.getNodeAvailabilityPercentage(String(id), start, end),
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
    availability.value = avail
    timeline.value = outages
  } finally {
    stopLoading()
  }
}

const onUpdateTime = (time: StartEndTime) => {
  // Replaced wholesale so the watcher below fires exactly once. TimeControls carries a relative
  // range when the pick came from the preset list, and only absolute times for a custom window.
  selection.value = time.range
    ? { kind: 'relative', range: time.range }
    : { kind: 'absolute', ...toMillis(time) }
  rangeLabel.value = labelFor(time.range)
}

// One watcher drives everything: mount, a node change and a range change each cause exactly one
// fetch, and a node and range changing in the same tick are coalesced into one.
//
// immediate, for the reason the previous version of this panel documented and which still holds:
// the details page keeps one instance of this panel across node ids and nothing resets it, so on a
// return visit to the same node a change-only watch would never fire at all. The very first run
// has no node id yet -- the node prop is empty until the page's own fetch resolves -- and returns
// early.
watch([() => props.node?.id, selection], fetchAll, { immediate: true })
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
</style>
