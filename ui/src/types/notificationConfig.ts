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

// JSON mirrors of the config models behind /api/v2/notification-config.
// Field names follow the XML element/attribute names in notifications.xml,
// destinationPaths.xml and notificationCommands.xml — the files stay the
// system of record, this API is a 1:1 view of them.

export type NotifdStatus = 'on' | 'off'

export interface DestinationPathTarget {
  interval?: string
  name: string
  autoNotify?: string
  command: string[]
}

export interface DestinationPathEscalate {
  delay: string
  target: DestinationPathTarget[]
}

export interface DestinationPath {
  name: string
  'initial-delay'?: string
  target: DestinationPathTarget[]
  escalate?: DestinationPathEscalate[]
}

// Path outages (critical paths) — DB-backed (pathoutage table), unlike the
// XML-backed config above. Wire shapes of /api/v2/notification-config/path-outages.
export interface PathOutage {
  nodeId: number
  nodeLabel?: string | null
  criticalPathIp?: string | null
  criticalPathServiceName?: string | null
}

export interface PathOutagePreview {
  totalCount: number
  nodes: PathOutage[]
}

export interface PathOutageRequest {
  rule: string
  criticalIp?: string
  criticalSvc?: string
}
