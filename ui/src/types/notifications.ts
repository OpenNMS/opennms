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

// Sent notifications served by /rest/notifications — DB-backed, and
// distinct from the notification *configuration* in types/notificationConfig.ts.
// Field names follow the wire format of the v1 REST serializer, which differs
// from the entity/DB names (id, textMessage, ackUser/ackTime, destinations).

import { ServiceType } from '@/types'

export type NotificationAckType = 'unack' | 'ack' | 'all'

export type NotificationQueryPreset = 'yourOutstanding' | 'teamOutstanding' | 'allOutstanding' | 'allAcknowledged' | 'userSearch'

// AckType enum on the Java model; always NOTIFICATION for these records.
export type NotificationAckKind = 'UNSPECIFIED' | 'ALARM' | 'NOTIFICATION'

export interface OnmsNotificationDestination {
  id?: number
  userId?: string
  media?: string | null
  contactInfo?: string | null
  notifyTime?: number | string | null
  autoNotify?: string | null
}

export interface OnmsNotification {
  id: number
  notificationName?: string | null
  subject?: string | null
  textMessage?: string | null
  numericMessage?: string | null
  uei?: string | null
  severity?: string | null
  pageTime?: number | string | null
  // The Java model's Acknowledgeable getters mirror its own fields, so the
  // serializer emits duplicates: ackTime/ackUser = respondTime/answeredBy and
  // ackId = id. Nulls are omitted.
  respondTime?: number | string | null
  answeredBy?: string | null
  ackTime?: number | string | null
  ackUser?: string | null
  ackId?: number | null
  type?: NotificationAckKind | null
  eventId?: number | null
  nodeId?: number | null
  nodeLabel?: string | null
  ipAddress?: string | null
  serviceType?: ServiceType | null
  queueId?: string | null
  destinations?: OnmsNotificationDestination[]
}

export interface NotificationBrowseFilter {
  acktype: NotificationAckType
  user: string | null
}

export interface NotificationBrowseResult {
  notifications: OnmsNotification[]
  totalCount: number
}
