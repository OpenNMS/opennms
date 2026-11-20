///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { computed, MaybeRefOrGetter, ref, toValue, watch } from 'vue'
import { useNodeStore } from '@/stores/nodeStore'
import API from '@/services/index'
import { NodeAvailability, StartEndTime } from '@/types/index'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import { selectionFromTime, selectionLabel, windowFor } from '../availabilityRange'
import { buildTimelineModel, TimelineWindow } from '../availabilityTimelineModel'
import { buildTicks } from '../availabilityTimelineAxis'
import { useDelayedLoading } from './useDelayedLoading'

/** The page of interfaces to ask for. The server computes figures only for these. */
export interface AvailabilityPaging {
  limit: number
  offset: number
}

/**
 * A node's availability over the range picked in the node store: the service roster with its
 * percentages, the outage timeline drawn under it, and the model that joins the two.
 *
 * Fetch rules:
 * - A node or range change fetches the roster and the outages, for a newly resolved window. Only
 *   their success sets activeWindow and rangeLabel.
 * - A paging-only change fetches the roster only, for the current activeWindow, and joins it to the
 *   cached outages. The window is not resolved again: a relative range like 'Last hour' resolved
 *   later is a different window, and the figures would not describe the bars beside them.
 * - Changes in the same tick are one fetch. If any of them is a node or range change, it is a full
 *   fetch for the current paging.
 * - A paging change with no outages cached for the current node and range -- the full fetch has not
 *   landed yet, or failed -- is a full fetch too. In particular a page change while a full fetch is
 *   in flight supersedes it with a new full fetch: the window a roster-only fetch would need is the
 *   one that fetch has not set yet.
 */
export const useNodeAvailability = (
  nodeId: MaybeRefOrGetter<number | string | undefined>,
  paging: MaybeRefOrGetter<AvailabilityPaging>
) => {
  const { isFetching, showSpinner, start: startLoading, stop: stopLoading } = useDelayedLoading()

  const availability = ref<NodeAvailability | undefined>(undefined)
  const timeline = ref<NodeOutageTimeline | null>(null)
  const loadFailed = ref(false)

  // A fetch has completed for some node. Without it a consumer cannot tell "nothing has been asked
  // for yet" from "asked, and the node has no monitored services", and would answer the first with
  // the empty state's claim about the second.
  const hasLoaded = ref(false)

  // The node's totals, from the last answer: not only the interfaces fetched.
  const totalInterfaces = ref(0)
  const totalServices = ref(0)

  const nodeStore = useNodeStore()

  /**
   * The selection lives in the node store rather than here, so it survives a node change: the
   * details page rebuilds its panel for each node, and a range held locally would be lost every
   * time the user moved on.
   */
  const selection = computed(() => nodeStore.availabilityRange)

  /** Seeds the picker's button, so a restored range is not contradicted by it on a fresh mount. */
  const initialRangeLabel = selectionLabel(selection.value)

  /**
   * The range the summary names, which is the one the displayed figure was fetched for, not the one
   * just picked. Updated with the data, alongside activeWindow.
   *
   * Setting it when the pick happens put the new range's name beside the old range's percentage
   * until the fetch landed -- 'Availability (last hour) 66.667%' where the figure was still the
   * day's. The picker's own button changes immediately, so the pick is still acknowledged at once.
   */
  const rangeLabel = ref(initialRangeLabel)

  // The window the rendered model describes: set from the fetch that produced it, so the axis and
  // the bars cannot disagree.
  const activeWindow = ref<TimelineWindow>(windowFor(selection.value))

  // The model for every interface fetched. A consumer that shows fewer rows cuts it; the node's
  // figure comes from the whole roster either way.
  const model = computed(() =>
    availability.value
      ? buildTimelineModel(availability.value, timeline.value, activeWindow.value)
      : undefined)

  const ticks = computed(() => buildTicks(activeWindow.value))

  // Monotonic per fetch, full or roster-only, as nodeStore does for its own. One counter for both
  // kinds, so a page response cannot overwrite a later node or range change, nor the reverse. The
  // details page keeps one instance across node ids, and the range or page can change before a
  // request has landed, so responses can race.
  let requestId = 0

  // True while timeline and activeWindow hold a successful full fetch for the current node and
  // range: only then can a roster-only fetch be joined to them. Cleared when a full fetch starts and
  // on any failure.
  let outagesCached = false

  const pageParams = () => {
    const { limit, offset } = toValue(paging)
    // withServices: the timeline drops interfaces with no monitored services, so the server leaves
    // them out before counting and paging -- otherwise they could take the places of shown ones.
    return { limit, offset, withServices: true }
  }

  const fail = () => {
    loadFailed.value = true
    outagesCached = false
    availability.value = undefined
    timeline.value = null
  }

  const applyRoster = (avail: NodeAvailability) => {
    loadFailed.value = false
    hasLoaded.value = true
    availability.value = avail
    // A server without the count sends every interface: count the ones the timeline would show.
    totalInterfaces.value = avail.ipinterfaceCount ?? avail.ipinterfaces.filter(iface => (iface.services ?? []).length > 0).length
    totalServices.value = avail['service-count'] ?? 0
  }

  const fetchAll = async (id: number | string) => {
    const myRequest = ++requestId
    // Resolved here rather than held in state, so a relative range follows the clock.
    const requested = windowFor(selection.value)
    const requestedLabel = selectionLabel(selection.value)
    const { start, end } = requested
    outagesCached = false

    startLoading()

    try {
      const [avail, outages] = await Promise.all([
        API.getNodeAvailabilityPercentage(String(id), start, end, pageParams()),
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
        fail()
        return
      }

      activeWindow.value = requested
      rangeLabel.value = requestedLabel
      timeline.value = outages
      outagesCached = true
      applyRoster(avail)
    } finally {
      stopLoading()
    }
  }

  const fetchRoster = async (id: number | string) => {
    const myRequest = ++requestId
    const { start, end } = activeWindow.value

    startLoading()

    try {
      const avail = await API.getNodeAvailabilityPercentage(String(id), start, end, pageParams())

      if (myRequest !== requestId) {
        return
      }

      if (!avail) {
        fail()
        return
      }

      applyRoster(avail)
    } finally {
      stopLoading()
    }
  }

  const onUpdateTime = (time: StartEndTime) => {
    // Replaced wholesale so the watcher below fires exactly once. TimeControls carries a relative
    // range when the pick came from the preset list, and only absolute times for a custom window.
    nodeStore.setAvailabilityRange(selectionFromTime(time))
  }

  // One watcher drives everything, so changes in the same tick are coalesced into one fetch. Paging
  // is watched by value, so a getter that builds a new object each time is not a change.
  //
  // immediate is what fetches on mount. The details page rebuilds its panel for each node rather
  // than feeding a new id to the existing one, so the node id never changes during that instance's
  // life and a change-only watch would never fire at all.
  watch(
    [() => toValue(nodeId), selection, () => toValue(paging).limit, () => toValue(paging).offset],
    ([id, sel], previous) => {
      if (!id) {
        return
      }

      const [prevId, prevSel] = previous ?? []
      const pagingOnly = id === prevId && sel === prevSel

      if (pagingOnly && outagesCached) {
        fetchRoster(id)
      } else {
        fetchAll(id)
      }
    },
    { immediate: true }
  )

  return {
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
    totalServices,
    timeline
  }
}
