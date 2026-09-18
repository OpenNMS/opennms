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

import { rest } from './axiosInstances'

// The external flow-graph URL for one SNMP interface, from the legacy
// GET /rest/flows/flowGraphUrl?exporterNode=&ifIndex=.
//
// The URL is a template configured server-side (org.opennms.netmgt.flows.rest / flowGraphUrl,
// e.g. 'http://grafana:3000/dashboard/flows?node=$nodeId&interface=$ifIndex') with $nodeId,
// $ifIndex, $start and $end substituted per request. It points at a separate tool, so it is
// absolute and must NOT be prefixed with the OpenNMS baseHref.
//
// There is no bulk form: the endpoint takes one interface at a time and runs a flow count for
// it, so callers resolve a URL when the user asks for one rather than for every row up front.
export interface FlowGraphUrlInfo {
  flowGraphUrl: string
  flowCount: number
}

/**
 * Resolves the flow graph URL for one interface, or '' when the server has no flowGraphUrl
 * configured -- the default, in which case the endpoint answers 204 with no body.
 *
 * Request failures propagate: "not configured" and "the lookup failed" are different things to
 * tell the user about.
 */
export const getFlowGraphUrl = async (nodeId: string | number, ifIndex: string | number) => {
  const resp = await rest.get<FlowGraphUrlInfo>('/flows/flowGraphUrl', {
    params: { exporterNode: nodeId, ifIndex }
  })

  if (resp.status === 204) {
    return ''
  }

  return resp.data?.flowGraphUrl ?? ''
}
