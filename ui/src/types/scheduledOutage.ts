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

// Mirrors org.opennms.netmgt.config.poller.outages.* (poll-outages.xml). The
// v1 REST serializes each JAXB list under its element name (time/node/
// interface/outage) and may collapse a single-element list to one object, so
// callers normalize with asArray() from scheduledOutagesService.

export type OutageType = 'specific' | 'daily' | 'weekly' | 'monthly'

export interface OutageTime {
  id?: string
  // weekday name (weekly) or day-of-month 1-31 (monthly); absent otherwise
  day?: string
  // 'dd-MMM-yyyy HH:mm:ss' for specific, 'HH:mm:ss' for daily/weekly/monthly
  begins: string
  ends: string
}

export interface OutageNode {
  id: number
}

export interface OutageInterface {
  // a valid IP address, or the literal 'match-any' (all nodes/interfaces)
  address: string
}

export interface ScheduledOutage {
  name: string
  type?: OutageType
  time?: OutageTime[]
  node?: OutageNode[]
  interface?: OutageInterface[]
}

export interface PackageRef {
  name: string
  applied: boolean
  // every outage-calendar name the package references; lets the list page
  // compute per-outage membership from a single name-less applies-to call
  calendars: string[]
}

// GET /sched-outages/{name}/applies-to (or /applies-to for a new outage)
export interface OutageApplicability {
  notifications: boolean
  notificationCalendars: string[]
  pollers: PackageRef[]
  collectors: PackageRef[]
  thresholders: PackageRef[]
}
