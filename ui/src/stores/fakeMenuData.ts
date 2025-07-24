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

import { NotificationSummary } from '@/types/mainMenu'

// fake/mocked data to use for testing
export const defaultNotificationSummary = {
  totalCount: 10,
  totalUnacknowledgedCount: 8,
  userUnacknowledgedCount: 3,
  teamUnacknowledgedCount: 5,
  user: 'admin',
  userUnacknowledgedNotifications: {
    offset: 0,
    count: 3,
    totalCount: 3,
    notification: [
      {
        id: 1,
        ipAddress: '127.0.0.1',
        nodeLabel: 'localhost',
        notificationName: 'name1',
        pageTime: new Date(),
        serviceType: {
          name: 'service1'
        },
        severity: 'major'
      },
      {
        id: 2,
        ipAddress: '127.0.0.1',
        nodeLabel: 'localhost',
        notificationName: 'name2',
        pageTime: new Date(),
        serviceType: {
          name: 'service2'
        },
        severity: 'minor'
      },
      {
        id: 3,
        ipAddress: '127.0.0.1',
        nodeLabel: 'localhost',
        notificationName: 'name3',
        pageTime: new Date(),
        serviceType: {
          name: 'service2'
        },
        severity: null
      }
    ]
  }
} as NotificationSummary
