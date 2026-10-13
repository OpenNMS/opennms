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

import { defineStore } from 'pinia'
import API from '@/services'
import { nodePageOf, withNodeFilter } from '@/services/serviceHelpers'
import { Event, NodePage, QueryParameters } from '@/types'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { ref } from 'vue'

/**
 * Events in two independent slices:
 *
 * - `events` -- whatever the last unscoped query asked for, for an all-nodes event list.
 * - `nodeEvents` -- one node's events, for the Node Details page, stamped with the node they
 *   belong to.
 *
 * They are kept apart because the store is global and outlives any one page: with a single
 * slice, an event list page and the node page would each leave the other's results behind. The
 * node stamp lets a panel tell this node's events from the previous node's, which a failed fetch
 * would otherwise leave on screen.
 */
export const useEventStore = defineStore('eventStore', () => {
  const events = ref([] as Event[])
  const totalCount = ref(0)

  const nodeEvents = ref([] as Event[])
  const nodeEventsTotalCount = ref(0)
  const nodeEventsNodeId = ref<string | undefined>(undefined)
  // The page the node slice holds, so a table's paginator can match the rows on screen.
  const nodeEventsPage = ref<NodePage | undefined>(undefined)
  // The node whose latest fetch failed, if any; see the alarm store.
  const nodeEventsFailedNodeId = ref<string | undefined>(undefined)

  // Monotonic ids sequencing each slice's requests: a table fires one per page and, on the node
  // page, one per node as the user moves between them, so a slow response from a superseded
  // request must not land under the current paginator state. One counter per slice, so a request
  // for one never discards a response for the other.
  let eventsRequestId = 0
  let nodeEventsRequestId = 0

  const getEvents = async (queryParameters?: QueryParameters): Promise<ValidationResultWithPayload<Event[]>> => {
    const requestId = ++eventsRequestId

    const resp = await API.getEvents(queryParameters)

    if (requestId !== eventsRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    if (!resp) {
      return createResultWithPayload(false, 'Unable to load events')
    }

    events.value = resp.event
    totalCount.value = resp.totalCount

    return createResultWithPayload(true, '', resp.event)
  }

  /**
   * Fetch one node's events. `queryParameters` carries paging and sorting; any `_s` in it is
   * applied within the node. The slice is replaced only on success, so check `nodeEventsNodeId`
   * before showing it for a given node.
   */
  const getNodeEvents = async (nodeId: string, queryParameters?: QueryParameters): Promise<ValidationResultWithPayload<Event[]>> => {
    const requestId = ++nodeEventsRequestId

    if (nodeEventsFailedNodeId.value !== nodeId) {
      nodeEventsFailedNodeId.value = undefined
    }

    const resp = await API.getEvents(withNodeFilter(nodeId, queryParameters))

    if (requestId !== nodeEventsRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    if (!resp) {
      nodeEventsFailedNodeId.value = nodeId

      return createResultWithPayload(false, `Unable to load events for node ${nodeId}`)
    }

    nodeEvents.value = resp.event
    nodeEventsTotalCount.value = resp.totalCount
    nodeEventsNodeId.value = nodeId
    nodeEventsPage.value = nodePageOf(queryParameters)
    nodeEventsFailedNodeId.value = undefined

    return createResultWithPayload(true, '', resp.event)
  }

  return {
    events,
    totalCount,
    nodeEvents,
    nodeEventsTotalCount,
    nodeEventsNodeId,
    nodeEventsPage,
    nodeEventsFailedNodeId,
    getEvents,
    getNodeEvents
  }
})
