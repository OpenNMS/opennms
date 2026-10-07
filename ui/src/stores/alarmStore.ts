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
import { withNodeFilter } from '@/services/serviceHelpers'
import { Alarm, QueryParameters } from '@/types'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { ref } from 'vue'

/**
 * Alarms in two independent slices, for the same reasons as the event store's:
 *
 * - `alarms` -- whatever the last unscoped query asked for, for an all-nodes alarm list.
 * - `nodeAlarms` -- one node's alarms, stamped with the node they belong to.
 *
 * The node slice defaults to every alarm the node has (`limit: 0`) rather than a page, because
 * the node's status is worked out from the whole set. A consumer that wants a page of a node's
 * alarms should page this slice, or pass its own limit knowing it narrows the set for everyone.
 */
export const useAlarmStore = defineStore('alarmStore', () => {
  const alarms = ref([] as Alarm[])
  const totalCount = ref(0)

  const nodeAlarms = ref([] as Alarm[])
  const nodeAlarmsTotalCount = ref(0)
  const nodeAlarmsNodeId = ref<string | undefined>(undefined)
  // The node whose latest fetch failed, if any. Kept by node id so that one node's failure never
  // reads as another's, and in the store so that every panel sharing the slice agrees on it.
  const nodeAlarmsFailedNodeId = ref<string | undefined>(undefined)

  // One request counter per slice; see the event store.
  let alarmsRequestId = 0
  let nodeAlarmsRequestId = 0

  const getAlarms = async (queryParameters?: QueryParameters): Promise<ValidationResultWithPayload<Alarm[]>> => {
    const requestId = ++alarmsRequestId

    const resp = await API.getAlarms(queryParameters)

    if (requestId !== alarmsRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    if (!resp) {
      return createResultWithPayload(false, 'Unable to load alarms')
    }

    alarms.value = resp.alarm
    totalCount.value = resp.totalCount

    return createResultWithPayload(true, '', resp.alarm)
  }

  /**
   * Fetch one node's alarms -- all of them unless `queryParameters` says otherwise. Any `_s` in
   * it is applied within the node. The slice is replaced only on success, so check
   * `nodeAlarmsNodeId` before showing it for a given node, and `nodeAlarmsFailedNodeId` to tell
   * a node still loading from one that failed to load.
   */
  const getNodeAlarms = async (nodeId: string, queryParameters?: QueryParameters): Promise<ValidationResultWithPayload<Alarm[]>> => {
    const requestId = ++nodeAlarmsRequestId

    // A fetch for another node starts afresh. A refresh of the failed node keeps saying so until
    // it answers, rather than flicking back to loading.
    if (nodeAlarmsFailedNodeId.value !== nodeId) {
      nodeAlarmsFailedNodeId.value = undefined
    }

    const resp = await API.getAlarms(withNodeFilter(nodeId, { limit: 0, ...queryParameters }))

    if (requestId !== nodeAlarmsRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    if (!resp) {
      nodeAlarmsFailedNodeId.value = nodeId

      return createResultWithPayload(false, `Unable to load alarms for node ${nodeId}`)
    }

    nodeAlarms.value = resp.alarm
    nodeAlarmsTotalCount.value = resp.totalCount
    nodeAlarmsNodeId.value = nodeId
    nodeAlarmsFailedNodeId.value = undefined

    return createResultWithPayload(true, '', resp.alarm)
  }

  return {
    alarms,
    totalCount,
    nodeAlarms,
    nodeAlarmsTotalCount,
    nodeAlarmsNodeId,
    nodeAlarmsFailedNodeId,
    getAlarms,
    getNodeAlarms
  }
})
