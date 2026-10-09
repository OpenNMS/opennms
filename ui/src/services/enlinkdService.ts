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

import { v2 } from './axiosInstances'
import { BridgeElement, CdpElement, IsisElement, LldpElement, NodeEnlinkdElements, OspfElement } from '@/types/enlinkd'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'

const endpoint = '/enlinkd'

// One element endpoint. An empty body (204) is "none found", not a failure.
const getElement = async <T>(kind: string, nodeId: string): Promise<{ ok: boolean, data?: T }> => {
  try {
    const resp = await v2.get(`${endpoint}/${kind}/${encodeURIComponent(nodeId)}`)

    return { ok: true, data: resp.status === 204 || !resp.data ? undefined : resp.data as T }
  } catch (_err) {
    return { ok: false }
  }
}

/**
 * What Enlinkd discovered about a node itself -- its LLDP, CDP, OSPF, IS-IS and bridge elements --
 * as the legacy node page's boxes show it. Five small requests in parallel rather than the
 * all-in-one /enlinkd/{node}, which also carries every neighbor link.
 *
 * Fails if any of the five fails; the payload still holds whatever did load.
 */
const getNodeEnlinkdElements = async (nodeId: string): Promise<ValidationResultWithPayload<NodeEnlinkdElements>> => {
  const [lldp, cdp, ospf, isis, bridges] = await Promise.all([
    getElement<LldpElement>('lldp_elems', nodeId),
    getElement<CdpElement>('cdp_elems', nodeId),
    getElement<OspfElement>('ospf_elems', nodeId),
    getElement<IsisElement>('isis_elems', nodeId),
    getElement<BridgeElement[]>('bridge_elems', nodeId)
  ])

  const payload: NodeEnlinkdElements = {
    lldp: lldp.data,
    cdp: cdp.data,
    ospf: ospf.data,
    isis: isis.data,
    bridges: Array.isArray(bridges.data) ? bridges.data : []
  }

  const ok = [lldp, cdp, ospf, isis, bridges].every(r => r.ok)

  return createResultWithPayload(ok, ok ? '' : `Unable to load all network information for node ${nodeId}`, payload)
}

export { getNodeEnlinkdElements }
