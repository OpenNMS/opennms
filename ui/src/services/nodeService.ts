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
  SnmpInterfaceApiResponse,
  QueryParameters,
  IpInterfaceApiResponse,
  NodeAvailability,
  NodeCriticalPath
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'
import { createResultWithPayload, ValidationResultWithPayload } from '@/types/validation'

const endpoint = '/nodes'

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
 *
 * `page` asks for one page of the node's interfaces (in address order, as the server sorts them):
 * only that page's figures are computed, each being a query of its own. `ipinterfaceCount` in the
 * answer is the node's total, for a paginator. `withServices` counts and pages only the interfaces
 * with monitored services.
 */
const getNodeAvailabilityPercentage = async (
  id: string,
  startMs?: number,
  endMs?: number,
  page?: { limit: number, offset: number, withServices?: boolean }
): Promise<NodeAvailability | false> => {
  try {
    const params: Record<string, number> = {}

    if (startMs !== undefined && endMs !== undefined) {
      params.start = startMs
      params.end = endMs
    }

    if (page) {
      params.limit = page.limit
      params.offset = page.offset
    }

    const query: Record<string, number | boolean> = page?.withServices ? { ...params, withServices: true } : params

    const resp: { data: NodeAvailability } = await rest.get(`/availability/nodes/${id}`, Object.keys(query).length ? { params: query } : undefined)

    return resp.data
  } catch (_err) {
    return false
  }
}

/**
 * The node's critical path, for the Path Outage panel. The payload is null when the node has none
 * of its own (204); a failed request is a failed result, so the two can be told apart.
 */
const getNodeCriticalPath = async (id: string): Promise<ValidationResultWithPayload<NodeCriticalPath | null>> => {
  try {
    const resp = await v2.get(`/nodes/${id}/criticalPath`)

    return createResultWithPayload(true, '', resp.status === 204 || !resp.data ? null : resp.data as NodeCriticalPath)
  } catch (_err) {
    return createResultWithPayload<NodeCriticalPath | null>(false, `Unable to load the critical path for node ${id}`)
  }
}

export {
  getNodes,
  getNodeCriticalPath,
  getNodeById,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
}
