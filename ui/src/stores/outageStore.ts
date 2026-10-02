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
import { Outage, QueryParameters } from '@/types'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { ref } from 'vue'

/**
 * Service outages in two independent slices, for the same reasons as the event and alarm stores:
 *
 * - `outages` -- whatever the last unscoped query asked for, for an all-nodes outage list.
 * - `nodeOutages` -- one node's recent outages (lost in the last week or still open, perspective
 *   outages left out, as the legacy Recent Outages panel showed), stamped with the node.
 *
 * Both come from v2 `/outages`, so `totalCount` is the number of matches in either.
 *
 * Not to be confused with scheduled outages (planned maintenance windows), which have their own
 * store. The dashboard's Outages panel calls the service directly and keeps its data out of here.
 */
export const useOutageStore = defineStore('outageStore', () => {
  const outages = ref([] as Outage[])
  const totalCount = ref(0)

  const nodeOutages = ref([] as Outage[])
  const nodeOutagesTotalCount = ref(0)
  const nodeOutagesNodeId = ref<string | undefined>(undefined)

  // One request counter per slice; see the event store.
  let outagesRequestId = 0
  let nodeOutagesRequestId = 0

  const getOutages = async (queryParameters?: QueryParameters): Promise<ValidationResultWithPayload<Outage[]>> => {
    const requestId = ++outagesRequestId

    const resp = await API.getOutages(queryParameters)

    if (requestId !== outagesRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    if (!resp) {
      return createResultWithPayload(false, 'Unable to load outages')
    }

    outages.value = resp.outage
    totalCount.value = resp.totalCount

    return createResultWithPayload(true, '', resp.outage)
  }

  /**
   * Fetch one node's recent outages. `queryParameters` carries paging and sorting; any `_s` in it
   * applies within the node's recent outages. The slice is replaced only on success, so check
   * `nodeOutagesNodeId` before showing it for a given node.
   */
  const getNodeOutages = async (nodeId: string, queryParameters?: QueryParameters): Promise<ValidationResultWithPayload<Outage[]>> => {
    const requestId = ++nodeOutagesRequestId

    const resp = await API.getNodeOutages(nodeId, queryParameters)

    if (requestId !== nodeOutagesRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    if (!resp) {
      return createResultWithPayload(false, `Unable to load outages for node ${nodeId}`)
    }

    nodeOutages.value = resp.outage
    nodeOutagesTotalCount.value = resp.totalCount
    nodeOutagesNodeId.value = nodeId

    return createResultWithPayload(true, '', resp.outage)
  }

  return {
    outages,
    totalCount,
    nodeOutages,
    nodeOutagesTotalCount,
    nodeOutagesNodeId,
    getOutages,
    getNodeOutages
  }
})
