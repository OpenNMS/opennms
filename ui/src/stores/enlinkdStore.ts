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
import { NodeEnlinkdElements } from '@/types/enlinkd'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'
import { ref } from 'vue'

/**
 * Enlinkd discovery data. For now one node's elements (LLDP, CDP, OSPF, IS-IS, bridge) for the
 * Node Details Network tab, stamped with the node they belong to, as the other stores' node
 * slices are; neighbor links can join it when something needs them.
 */
export const useEnlinkdStore = defineStore('enlinkdStore', () => {
  const nodeElements = ref<NodeEnlinkdElements | undefined>(undefined)
  const nodeElementsNodeId = ref<string | undefined>(undefined)

  // Sequences requests: a slow answer for a node the user has left must not land under this one.
  let nodeElementsRequestId = 0

  /**
   * Fetch a node's elements. The slice is replaced whenever the answer arrives, even a partial
   * one -- what did load is still this node's -- so check `nodeElementsNodeId` before showing it.
   */
  const getNodeElements = async (nodeId: string): Promise<ValidationResultWithPayload<NodeEnlinkdElements>> => {
    const requestId = ++nodeElementsRequestId

    const result = await API.getNodeEnlinkdElements(nodeId)

    if (requestId !== nodeElementsRequestId) {
      return createResultWithPayload(false, 'Superseded by a newer request')
    }

    nodeElements.value = result.payload
    nodeElementsNodeId.value = nodeId

    return result
  }

  return {
    nodeElements,
    nodeElementsNodeId,
    getNodeElements
  }
})
