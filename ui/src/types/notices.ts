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

// Notices (sent notifications) served by /rest/notifications — DB-backed,
// distinct from the notification *configuration* in types/notificationConfig.ts.
// Field names follow the wire format of the v1 REST serializer, which differs
// from the entity/DB names (id, textMessage, ackUser/ackTime, destinations).

export type NoticeAckType = 'unack' | 'ack' | 'all'

export type NoticeQueryPreset = 'yourOutstanding' | 'teamOutstanding' | 'allOutstanding' | 'allAcknowledged' | 'userSearch'

export interface OnmsNoticeServiceType {
  id?: number
  name?: string
}

export interface OnmsNoticeDestination {
  id?: number
  userId?: string
  media?: string | null
  contactInfo?: string | null
  notifyTime?: number | string | null
  autoNotify?: string | null
}

export interface OnmsNotice {
  id: number
  subject?: string | null
  textMessage?: string | null
  uei?: string | null
  severity?: string | null
  pageTime?: number | string | null
  ackTime?: number | string | null
  ackUser?: string | null
  eventId?: number | null
  nodeId?: number | null
  nodeLabel?: string | null
  ipAddress?: string | null
  serviceType?: OnmsNoticeServiceType | null
  queueId?: string | null
  destinations?: OnmsNoticeDestination[]
}

export interface NoticeBrowseFilter {
  acktype: NoticeAckType
  user: string | null
}

export interface NoticeBrowseResult {
  notices: OnmsNotice[]
  totalCount: number
}
