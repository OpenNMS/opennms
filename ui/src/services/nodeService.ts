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

import { v2, rest } from './axiosInstances'
import {
  NodeApiResponse,
  NodeFilterRuleResult,
  SnmpInterfaceApiResponse,
  QueryParameters,
  IpInterfaceApiResponse,
  NodeAvailability,
  OutagesApiResponse
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'
import { orderBy } from 'lodash'

const endpoint = '/nodes'

/**
 * Every node matching an OpenNMS filter rule (`catincRouters & location='Default'`),
 * via the v1 endpoint: the filter engine lives there, not in v2.
 *
 * Unbounded on purpose: this is the Grafana plugin's `nodeFilter()` and a rule is
 * meant to name a whole set, so paging it would silently drop part of the answer.
 * The one failure worth telling apart is a rule the engine cannot parse, which the
 * server reports as 400 with a fixed message; everything else is `failed`.
 */
const getNodesByFilterRule = async (rule: string): Promise<NodeFilterRuleResult> => {
  try {
    const resp = await rest.get(endpoint, {
      params: { filterRule: rule, limit: 0, orderBy: 'label', order: 'asc' }
    })

    if (resp.status === 204 || !resp.data) {
      return { nodes: [] }
    }

    return { nodes: (resp.data as NodeApiResponse).node ?? [] }
  } catch (err) {
    const status = (err as { response?: { status?: number }})?.response?.status
    return { error: status === 400 ? 'invalid' : 'failed' }
  }
}

const getNodes = async (queryParameters?: QueryParameters): Promise<NodeApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)

    // no content from server
    if (resp.status === 204) {
      return { node: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

const getNodeById = async (id: string): Promise<any> => {
  try {
    const resp = await v2.get(`${endpoint}/${id}`)
    return resp.data
  } catch (_err) {
    return false
  }
}

const getNodeSnmpInterfaces = async (
  id: string,
  queryParameters?: QueryParameters
): Promise<SnmpInterfaceApiResponse | false> => {
  const snmpInterfaceEndpoint = `${endpoint}/${id}/snmpinterfaces`
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, snmpInterfaceEndpoint)
  }

  try {
    const resp = await v2.get(`${endpointWithQueryString || snmpInterfaceEndpoint}`)

    // no content from server
    if (resp.status === 204) {
      return { snmpInterface: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

const getNodeIpInterfaces = async (
  id: string,
  queryParameters?: QueryParameters
): Promise<IpInterfaceApiResponse | false> => {
  const ipInterfaceEndpoint = `${endpoint}/${id}/ipinterfaces`
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, ipInterfaceEndpoint)
  }

  try {
    const resp = await v2.get(`${endpointWithQueryString || ipInterfaceEndpoint}`)

    // no content from server
    if (resp.status === 204) {
      return { ipInterface: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

const getNodeAvailabilityPercentage = async (id: string): Promise<NodeAvailability | false> => {
  try {
    const resp: { data: NodeAvailability } = await rest.get(`/availability/nodes/${id}`)
    resp.data.ipinterfaces = orderBy(resp.data.ipinterfaces, 'address')

    return resp.data
  } catch (_err) {
    return false
  }
}

const getNodeOutages = async (id: string, queryParameters?: QueryParameters): Promise<OutagesApiResponse | false> => {
  const outagesEndpoint = `/outages/forNode/${id}`
  let outagesEndpointWithQueryString = ''

  if (queryParameters) {
    outagesEndpointWithQueryString = queryParametersHandler(queryParameters, outagesEndpoint)
  }

  try {
    const resp = await rest.get(outagesEndpointWithQueryString || outagesEndpoint)

    return resp.data
  } catch (_err) {
    return false
  }
}

export {
  getNodes,
  getNodesByFilterRule,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
}
