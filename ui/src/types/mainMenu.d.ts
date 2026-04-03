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

export interface MenuItem {
  type?: string         // 'item', 'header', 'separator', 'plugins'. default is 'item'
  id: string | null
  name: string | null
  url: string | null
  isExternalLink?: boolean | null
  locationMatch: string | null
  action?: string | null
  linkTarget?: string | null
  icon?: string | null
  roles: string[] | null
  items?: MenuItem[] | null
  requiredSystemProperties?: [{ name: string, value: string }] | null

  // not in Rest API, used for menu creation
  onClick?: () => void
}

export interface MainMenu {
  templateName: string
  baseHref: string
  homeUrl: string
  formattedDateTime: string
  formattedDate: string
  formattedTime: string
  noticeStatus: string
  username: string
  baseNodeUrl: string
  copyrightDates: string
  version: string
  zenithConnectEnabled: boolean
  zenithConnectBaseUrl: string
  zenithConnectRelativeUrl: string
  displayAddNodeButton?: boolean
  sideMenuInitialExpand?: boolean
  
  menus: MenuItem[]
  helpMenu: MenuItem | null
  selfServiceMenu: MenuItem | null
  userNotificationMenu: MenuItem | null
  provisionMenu: MenuItem | null
  configurationMenu: MenuItem | null
}

export interface NoticeStatusDisplay {
  icon: string
  iconComponent: object | null
  colorClass: string
  title: string
}

export interface OnmsServiceType {
  id: number
  name: string
}

export interface OnmsNotification {
  id: number
  ipAddress: string
  nodeLabel: string
  notificationName: string
  pageTime: Date
  serviceType: OnmsServiceType | null
  severity: string
}

export interface NotificationItem {
  offset: number
  count: number
  totalCount: number
  notification: OnmsNotification[]
}

export interface NotificationSummary {
  totalCount: number
  totalUnacknowledgedCount: number
  user: string
  userUnacknowledgedCount: number
  teamUnacknowledgedCount: number
  userUnacknowledgedNotifications: NotificationItem
}
