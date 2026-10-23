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
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'

/**
 * Every core-poller outage overlapping a window, across every monitored service on one node.
 *
 * Returns null on failure rather than false: an all-green node legitimately has zero outages, and
 * the panel has to tell "nothing was down" from "the request failed" -- the healthy case is the
 * common one, so conflating them would hide errors behind a reassuring chart. Same reasoning as
 * availabilityService and outageService.

 */
export const getNodeOutageTimeline = async (
  nodeId: string | number,
  startMs: number,
  endMs: number
): Promise<NodeOutageTimeline | null> => {
  try {
    const resp = await v2.get(`/outages/timeline/${nodeId}`, {
      params: { start: startMs, end: endMs }
    })

    const data = resp.data as NodeOutageTimeline | undefined

    if (!data) {
      return null
    }

    // The endpoint always sends `outage`, but an empty list can come back absent depending on the
    // serializer, and every consumer iterates it.
    return { ...data, outage: data.outage ?? [] }
  } catch (_err) {
    return null
  }
}
