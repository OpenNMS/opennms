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
  } catch (err) {
    return false
  }
}
