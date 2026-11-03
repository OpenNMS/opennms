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
import { getActiveOutageNamesForNode } from '@/services/scheduledOutagesService'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { ref } from 'vue'

/**
 * Scheduled outages -- planned maintenance windows from poll-outages.xml, not service outages.
 *
 * Holds the names of the outages in effect right now for one node, stamped with that node, for the Node
 * Details page. "In effect" is decided by the server (the window covers the current time in its
 * time zone, and the calendar names the node or covers one of its interfaces), so this slice is
 * only as current as its last fetch. The Scheduled Outages admin pages keep their own state.
 */
export const useScheduledOutageStore = defineStore('scheduledOutageStore', () => {
  const nodeActiveOutageNames = ref([] as string[])
  const nodeActiveOutagesNodeId = ref<string | undefined>(undefined)

  // Sequences requests, as in the other stores: a slow answer for a node the user has left must
  // not land under the one they are on.
  let nodeActiveOutagesRequestId = 0

  /**
   * Fetch the names of the scheduled outages currently affecting a node. The slice is replaced only on
   * success, so check `nodeActiveOutagesNodeId` before showing it for a given node.
   */
  const getNodeActiveOutages = async (nodeId: string): Promise<ValidationResultWithPayload<string[]>> => {
    const requestId = ++nodeActiveOutagesRequestId

    const names = await getActiveOutageNamesForNode(nodeId)

    if (requestId !== nodeActiveOutagesRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    if (!names) {
      return createResultWithPayload(false, `Unable to load scheduled outages for node ${nodeId}`)
    }

    nodeActiveOutageNames.value = names
    nodeActiveOutagesNodeId.value = nodeId

    return createResultWithPayload(true, '', names)
  }

  return {
    nodeActiveOutageNames,
    nodeActiveOutagesNodeId,
    getNodeActiveOutages
  }
})
