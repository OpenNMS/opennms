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
import { Outage, OutagesApiResponse, QueryParameters, SORT } from '@/types'
import { queryParametersHandler, withNodeFilter } from './serviceHelpers'

// Currently-open outages: ifRegainedService is the epoch sentinel (== null).
const OPEN_FIQL = 'outage.ifRegainedService==1970-01-01T00:00:00.000-0000'

// null on failure so the panel shows an error line, not an all-clear
export const getCurrentOutages = async (limit = 12, extraFiql: string[] = []): Promise<Outage[] | null> => {
  try {
    const fiql = encodeURIComponent([OPEN_FIQL, ...extraFiql].join(';'))
    const resp = await v2.get(`/outages?_s=${fiql}&limit=${limit}`)
    if (resp.status === 204) {
      return []
    }
    return (resp.data?.outage ?? []) as Outage[]
  } catch (_err) {
    return null
  }
}

// The outage payload carries no service name of its own; it is the nested service type's.
export const outageServiceName = (o: Outage): string => o.monitoredService?.serviceType?.name ?? 'service'

/**
 * Outages across all nodes, from the v2 API: `_s` takes FIQL, and `totalCount` is the number of
 * matches, so it can drive a paginator.
 */
export const getOutages = async (queryParameters?: QueryParameters): Promise<OutagesApiResponse | false> => {
  const endpoint = '/outages'

  try {
    const resp = await v2.get(queryParameters ? queryParametersHandler(queryParameters, endpoint) : endpoint)

    // no content from server
    if (resp.status === 204) {
      return { outage: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (_err) {
    return false
  }
}

// How far back a node's recent outages reach by default: a week, as the legacy panel and v1
// outages/forNode did.
export const RECENT_OUTAGES_WINDOW_MS = 7 * 24 * 60 * 60 * 1000

// A FIQL timestamp. The offset is written as -0000 rather than +0000 or Z: a + in the query is
// read back as a space, and the date then fails to parse (HTTP 500).
const fiqlTimestamp = (epochMs: number) => new Date(epochMs).toISOString().replace('Z', '-0000')

/**
 * One node's recent outages, for the node page's Recent Outages panel. These are the rules v1
 * `outages/forNode` applied, as v2 FIQL: lost within `windowMs` or still open, perspective
 * outages left out, newest first. Unlike forNode, `totalCount` is the number of matches, so it can
 * drive a paginator. Any `_s` in `queryParameters` applies within those rules.
 */
export const getNodeOutages = async (
  nodeId: string,
  queryParameters?: QueryParameters,
  windowMs = RECENT_OUTAGES_WINDOW_MS
): Promise<OutagesApiResponse | false> => {
  const recent = `perspective==%00;(ifLostService=gt=${fiqlTimestamp(Date.now() - windowMs)},${OPEN_FIQL})`
  const _s = queryParameters?._s ? `${recent};(${queryParameters._s})` : recent

  return getOutages(withNodeFilter(nodeId, { orderBy: 'id', order: SORT.DESCENDING, ...queryParameters, _s }))
}
