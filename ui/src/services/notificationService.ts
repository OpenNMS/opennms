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

import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { NotificationAckType, NotificationBrowseResult, OnmsNotification } from '@/types/notifications'
import { rest } from './axiosInstances'

// Mirrors NotificationSummary (v1 NotificationRestService /notifications/summary).
export interface NotificationSummary {
  user?: string
  totalCount: number
  totalUnacknowledgedCount: number
  userUnacknowledgedCount: number
  teamUnacknowledgedCount: number
}

const empty = (): NotificationSummary => ({
  totalCount: 0,
  totalUnacknowledgedCount: 0,
  userUnacknowledgedCount: 0,
  teamUnacknowledgedCount: 0
})

export const getNotificationSummary = async (): Promise<NotificationSummary | false> => {
  try {
    const resp = await rest.get('/notifications/summary')
    if (resp.status === 204) {
      return empty()
    }
    return resp.data as NotificationSummary
  } catch (_err) {
    return false
  }
}

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/notifications'

interface BrowseNotificationsParams {
  acktype: NotificationAckType
  user?: string | null
  excludeUser?: string | null
  limit: number
  offset: number
}

const browseNotifications = async (params: BrowseNotificationsParams): Promise<NotificationBrowseResult> => {
  try {
    startSpinner()
    const query = new URLSearchParams()
    query.set('limit', String(params.limit))
    query.set('offset', String(params.offset))
    query.set('orderBy', 'pageTime')
    query.set('order', 'desc')
    if (params.acktype === 'unack') {
      query.set('answeredBy', 'null')
    } else if (params.acktype === 'ack') {
      query.set('answeredBy', 'notnull')
    }
    if (params.user) {
      query.set('usersNotified.userId', params.user)
    }
    if (params.excludeUser) {
      query.set('excludeNotifiedUser', params.excludeUser)
    }
    const resp = await rest.get(`${endpoint}?${query.toString()}`)
    // 204 No Content when nothing matches
    if (!resp.data || typeof resp.data !== 'object') {
      return { notifications: [], totalCount: 0 }
    }
    const raw = resp.data.notification ?? []
    const notifications: OnmsNotification[] = Array.isArray(raw) ? raw : [raw]
    return { notifications, totalCount: Number(resp.data.totalCount ?? notifications.length) }
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load notifications.' })
    return { notifications: [], totalCount: 0 }
  } finally {
    stopSpinner()
  }
}

const acknowledgeNotification = async (notificationId: number, ack: boolean): Promise<boolean> => {
  try {
    startSpinner()
    await rest.put(`${endpoint}/${notificationId}`, new URLSearchParams({ ack: String(ack) }), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    showSnackBar({ msg: `Notification ${notificationId} ${ack ? 'acknowledged' : 'unacknowledged'}.` })
    return true
  } catch (_err) {
    showSnackBar({ msg: `Failed to ${ack ? 'acknowledge' : 'unacknowledge'} notification ${notificationId}.` })
    return false
  } finally {
    stopSpinner()
  }
}

export { acknowledgeNotification, browseNotifications }
