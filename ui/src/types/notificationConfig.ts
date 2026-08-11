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

// JSON mirrors of the JAXB config models behind /rest/notification-config.
// Field names follow the XML element/attribute names in notifications.xml,
// destinationPaths.xml and notificationCommands.xml — the files stay the
// system of record, this API is a 1:1 view of them.

export type NotifdStatus = 'on' | 'off'

export interface EventNotificationRule {
  strict?: boolean
  value?: string
}
export interface RuleMatch {
  ipAddress: string
  services: string[]
}
export interface RuleValidation {
  valid: boolean
  error?: string
  matchCount: number
  matches: RuleMatch[]
}

export interface EventNotificationParameter {
  name: string
  value: string
}

export interface EventNotificationVarbind {
  vbname: string
  vbvalue: string
}

export interface EventNotification {
  name: string
  status: NotifdStatus
  writeable?: boolean
  uei: string
  description?: string
  rule?: EventNotificationRule | string
  'notice-queue'?: string
  destinationPath: string
  'text-message'?: string
  subject?: string
  'numeric-message'?: string
  'event-severity'?: string
  parameter?: EventNotificationParameter[]
  varbind?: EventNotificationVarbind
}

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

export interface UeiSuggestion {
  uei: string
  eventLabel: string
}
