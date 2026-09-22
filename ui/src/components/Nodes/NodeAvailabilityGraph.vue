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
      >{{ model ? formatAvailability(model.availability) : '—' }}</span>
    </div>

    <OnmsSpinner v-if="showSpinner" />

    <template v-else-if="!isFetching">
      <EmptyList
        v-if="loadFailed"
        :content="errorContent"
        data-test="availability-error"
      />
      <EmptyList
        v-else-if="!model || model.interfaces.length === 0"
        :content="emptyContent"
        data-test="empty-list"
      />
      <AvailabilityTimeline
        v-else
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
import { DEFAULT_RANGE, resolveRelativeRange } from '@/components/Common/utils/timeRangeOptions'
import { useDelayedLoading } from './hooks/useDelayedLoading'
import API from '@/services'
import { Node, NodeAvailability, StartEndTime } from '@/types'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import {
  buildTimelineModel,
  formatAvailability,
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

// Seeded to the same range TimeControls shows before anything is picked. The two defaults have to
// agree or the button would name a window the panel is not showing; there is a test for it.
const startEnd = resolveRelativeRange(DEFAULT_RANGE)
const window = ref<TimelineWindow>({
  start: Number(startEnd.startTime) * 1000,
  end: Number(startEnd.endTime) * 1000
})
const rangeLabel = ref('Last day')

const errorContent = {
  title: 'Availability unavailable',
  msg: 'Could not load availability for this node.'
}
const emptyContent = { msg: 'No monitored services on this node.' }

const model = computed(() =>
  availability.value ? buildTimelineModel(availability.value, timeline.value, window.value) : undefined)

const ticks = computed(() => buildTicks(window.value))

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
  const { start, end } = window.value

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
    availability.value = avail
    timeline.value = outages
  } finally {
    stopLoading()
  }
}

const onUpdateTime = (time: StartEndTime) => {
  // Replaced wholesale so the watcher below fires exactly once. TimeControls works in seconds and
  // everything here is in milliseconds; convert once, at the boundary.
  window.value = {
    start: Number(time.startTime) * 1000,
    end: Number(time.endTime) * 1000
  }
  rangeLabel.value = time.range
    ? `Last ${time.range.amount} ${time.range.unit}`
    : 'custom range'
}

// One watcher drives everything: mount, a node change and a range change each cause exactly one
// fetch, and a node and range changing in the same tick are coalesced into one.
//
// immediate, for the reason the previous version of this panel documented and which still holds:
// the details page keeps one instance of this panel across node ids and nothing resets it, so on a
// return visit to the same node a change-only watch would never fire at all. The very first run
// has no node id yet -- the node prop is empty until the page's own fetch resolves -- and returns
// early.
watch([() => props.node?.id, window], fetchAll, { immediate: true })
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
