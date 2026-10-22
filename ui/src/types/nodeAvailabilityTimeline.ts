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

/**
 * Wire types for GET /api/v2/outages/timeline/{nodeId}.
 *
 * Kept out of the src/types/index.ts barrel deliberately: that file already carries
 * `NodeAvailability`, whose quoted kebab keys and one-element `services` tuple are the reason
 * every call site in the old availability panel fell back to `any`.
 */

/** One outage on one monitored service. All timestamps are epoch milliseconds. */
export interface NodeOutageTimelineEntry {
  id: number
  /**
   * The monitored service (ifservices) id. This is what the availability document reports as a
   * service `id`, so it is the key the two documents join on.
   */
  ifServiceId: number
  /** The IP interface id, joining to the availability document's interface `id`. */
  ipInterfaceId: number
  ipAddress: string
  /** The service *type* id, for building a service detail link. */
  serviceId: number
  serviceName: string
  ifLostService: number
  /**
   * Null while the service is still down. The endpoint emits a real null here -- the epoch-0
   * sentinel that the v2 `_s=` FIQL layer uses for "is null" does not reach this payload.
   */
  ifRegainedService: number | null
}

export interface NodeOutageTimeline {
  nodeId: number
  /** The window the server actually queried, echoed so the axis is laid out against it. */
  start: number
  end: number
  /** When the node was provisioned. Before this instant the node was not monitored at all. */
  nodeCreateTime: number
  count: number
  /**
   * True when more outages matched than the limit allowed, so these are the most recent ones and
   * the window is not fully described. The panel says so rather than present a partial strip as
   * complete.
   */
  truncated?: boolean
  outage: NodeOutageTimelineEntry[]
}
