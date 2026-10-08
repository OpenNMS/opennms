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
 * The nodes matching an OpenNMS filter rule (`catincRouters & location='Default'`),
 * via the v1 endpoint: the filter engine lives there, not in v2.
 *
 * Count first, then fetch. A rule can match an entire estate, and the v1 endpoint
 * serialises full nodes with every join, so the first request asks for one node
 * and reads `totalCount` (the endpoint counts the whole match regardless of the
 * limit). Only a match within `max` is then fetched, and only up to `max`.
 *
 * The one failure worth telling apart is a rule the engine cannot parse, which the
 * server reports as 400 with a fixed message; everything else is `failed`. That
 * includes the server failing on a match too large to bind as an `IN` list, which
 * no request from here can avoid.
 */
const getNodesByFilterRule = async (rule: string, max: number): Promise<NodeFilterRuleResult> => {
  const fetch = (limit: number) => rest.get(endpoint, {
    params: { filterRule: rule, limit, orderBy: 'label', order: 'asc' }
  })

  try {
    const probe = await fetch(1)

    if (probe.status === 204 || !probe.data) {
      return { nodes: [] }
    }

    const total = (probe.data as NodeApiResponse).totalCount ?? 0

    if (total > max) {
      return { error: 'too-many', count: total }
    }

    if (total <= 1) {
      return { nodes: (probe.data as NodeApiResponse).node ?? [] }
    }

    const resp = await fetch(max)

    return { nodes: (resp.data as NodeApiResponse)?.node ?? [] }
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

/**
 * Node availability by interface and service. With `startMs`/`endMs` the figures cover that window;
 * without them they cover the last 24 hours, which is the legacy behaviour and still what every
 * caller but the availability panel wants.
 */
const getNodeAvailabilityPercentage = async (
  id: string,
  startMs?: number,
  endMs?: number
): Promise<NodeAvailability | false> => {
  try {
    const params = (startMs !== undefined && endMs !== undefined)
      ? { params: { start: startMs, end: endMs }}
      : undefined
    const resp: { data: NodeAvailability } = await rest.get(`/availability/nodes/${id}`, params)
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
