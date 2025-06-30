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

import { MainMenu, NotificationSummary } from '@/types/mainMenu'

// fake/mocked menu data to use for testing

export const defaultMainMenu = {
  baseHref: 'http://localhost:8980/opennms/',
  homeUrl: 'http://localhost:8980/opennms/index.jsp',
  formattedDateTime: '2022-10-13T19:49:29-04:00',
  formattedDate: '2022-10-13',
  formattedTime: '19:49:29 UTC-04',
  noticeStatus: 'Off',
  username: 'admin',
  menus: [
    {
      id: null,
      name: 'Search',
      url: 'element/index.jsp',
      locationMatch: 'element',
      roles: null,
      items: null
    },
    {
      id: null,
      name: 'Info',
      url: '#',
      locationMatch: null,
      roles: null,
      items: [
        {
          id: null,
          name: 'Nodes',
          url: 'element/nodeList.htm',
          locationMatch: 'nodelist',
          roles: null
        },
        {
          id: null,
          name: 'Assets',
          url: 'asset/index.jsp',
          locationMatch: 'asset',
          roles: null
        },
        {
          id: null,
          name: 'Path Outages',
          url: 'pathOutage/index.jsp',
          locationMatch: 'pathOutage',
          roles: null
        },
        {
          id: null,
          name: 'Device Configs',
          url: 'ui/index.html#/device-config-backup',
          locationMatch: 'configurationManagement',
          roles: ['ROLE_ADMIN' ,'ROLE_REST', 'ROLE_DEVICE_CONFIG_BACKUP']
        }
      ]
    },
    {
      id: null,
      name: 'Status',
      url: '#',
      locationMatch: null,
      roles: null,
      items: [
        {
          id: null,
          name: 'Events',
          url: 'event/index',
          locationMatch: 'event',
          roles: null
        },
        {
          id: null,
          name: 'Alarms',
          url: 'alarm/index.htm',
          locationMatch: 'alarm',
          roles: null
        },
        {
          id: null,
          name: 'Notifications',
          url: 'notification/index.jsp',
          locationMatch: 'notification',
          roles: null
        },
        {
          id: null,
          name: 'Outages',
          url: 'outage/index.jsp',
          locationMatch: 'outage',
          roles: null
        },
        {
          id: null,
          name: 'Surveillance',
          url: 'surveillance-view.jsp',
          locationMatch: 'surveillance-view',
          roles: null
        },
        {
          id: null,
          name: 'Heatmap',
          url: 'heatmap/index.jsp',
          locationMatch: 'heatmap',
          roles: null
        },
        {
          id: null,
          name: 'Trend',
          url: 'trend/index.jsp',
          locationMatch: 'trend',
          roles: null
        },
        {
          id: null,
          name: 'Application',
          url: 'application/index.jsp',
          locationMatch: 'application',
          roles: null
        }
      ]
    },
    {
      id: null,
      name: 'Reports',
      url: 'report/index.jsp',
      locationMatch: null,
      roles: null,
      items: [
        {
          id: null,
          name: 'Charts',
          url: 'charts/index.jsp',
          locationMatch: 'chart',
          roles: null
        },
        {
          id: null,
          name: 'Resource Graphs',
          url: 'graph/index.jsp',
          locationMatch: 'performance',
          roles: null
        },
        {
          id: null,
          name: 'KSC Reports',
          url: 'KSC/index.jsp',
          locationMatch: 'ksc',
          roles: null
        },
        {
          id: null,
          name: 'Database Reports',
          url: 'report/database/index.jsp',
          locationMatch: 'database-reports',
          roles: null
        },
        {
          id: null,
          name: 'Statistics',
          url: 'statisticsReports/index.htm',
          locationMatch: 'reports',
          roles: null
        }
      ]
    },
    {
      id: null,
      name: 'Dashboards',
      url: 'dashboards.htm',
      locationMatch: null,
      roles: null,
      items: [
        {
          id: null,
          name: 'Dashboard',
          url: 'dashboard.jsp',
          locationMatch: 'dashboard',
          roles: null
        },
        {
          id: null,
          name: 'Ops Board',
          url: 'vaadin-wallboard',
          locationMatch: 'vaadin-wallboard',
          roles: null
        }
      ]
    },
    {
      id: null,
      name: 'Maps',
      url: 'maps.htm',
      locationMatch: null,
      roles: null,
      items: [
        {
          id: null,
          name: 'Topology',
          url: 'topology',
          locationMatch: 'topology',
          roles: null
        },
        {
          id: null,
          name: 'Geographical',
          url: 'ui/index.html#/map',
          locationMatch: 'ui/index.html#/map',
          roles: null
        }
      ]
    }
  ],
  helpMenu: {
    id: null,
    name: 'Help',
    url: null,
    locationMatch: null,
    roles: null,
    items: [
      {
        id: null,
        name: 'Help',
        url: 'help/index.jsp',
        locationMatch: null,
        roles: null
      },
      {
        id: null,
        name: 'About',
        url: 'about/index.jsp',
        locationMatch: null,
        roles: null
      },
      {
        id: null,
        name: 'Support',
        url: 'support/index.jsp',
        locationMatch: null,
        roles: ['ROLE_ADMIN']
      }
    ]
  },
  selfServiceMenu: {
    id: null,
    name: 'admin',
    url: 'account/selfService/index.jsp',
    locationMatch: null,
    roles: null,
    items: [
      {
        id: null,
        name: 'Change Password',
        url: 'account/selfService/newPasswordEntry',
        locationMatch: null,
        roles: null
      },
      {
        id: null,
        name: 'Log Out',
        url: 'j_spring_security_logout',
        locationMatch: null,
        roles: null
      }
    ]
  },
  userNotificationMenu: {
    id: null,
    name: null,
    url: null,
    locationMatch: null,
    roles: null,
    items: [
      {
        id: 'user',
        name: null,
        url: 'notification/browse?acktype=unack&filter=user==admin',
        locationMatch: null,
        roles: null
      },
      {
        id: 'team',
        name: null,
        url: 'notification/browse?acktype=unack',
        locationMatch: null,
        roles: null
      },
      {
        id: 'oncall',
        name: 'On-Call Schedule',
        url: 'roles',
        locationMatch: null,
        roles: null
      }
    ]
  },
  provisionMenu: {
    id: null,
    name: 'Quick-Add Node',
    url: 'admin/ng-requisitions/quick-add-node.jsp#/',
    locationMatch: null,
    roles: [
      'ROLE_ADMIN',
      'ROLE_PROVISION'
    ]
  },
  flowsMenu: null,

  configurationMenu: {
    id: null,
    name: 'Configure OpenNMS',
    url: 'admin/index.jsp',
    locationMatch: null,
    roles: ['ROLE_ADMIN']
  }
} as MainMenu

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
