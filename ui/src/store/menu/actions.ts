import { VuexContext } from '@/types'
import API from '@/services'
import { State } from './state'
import { MainMenu, NotificationSummary } from '@/types/mainMenu'

interface ContextWithState extends VuexContext {
  state: State
}

// Set this to true to use local/fake data instead of making API call
const useFakeMenuData = false
const useFakeUserNotificationData = false

const defaultMainMenu = {
  baseHref: 'http://localhost:8980/opennms/',
  homeUrl: 'http://localhost:8980/opennms/index.jsp',
  formattedTime: '2022-10-13T19:49:29-04:00',
  noticeStatus: 'Off',
  username: 'admin',
  menus: [
    {
      id: null,
      className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
      name: 'Search',
      url: 'element/index.jsp',
      locationMatch: 'element',
      icon: null,
      iconType: null,
      isIconOnly: null,
      isVueLink: null,
      roles: null,
      items: null
    },
    {
      id: null,
      className: 'org.opennms.web.navigate.MenuDropdownNavBarEntry',
      name: 'Info',
      url: '#',
      locationMatch: null,
      icon: null,
      iconType: null,
      isIconOnly: null,
      isVueLink: null,
      roles: null,
      items: [
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Nodes',
          url: 'element/nodeList.htm',
          locationMatch: 'nodelist',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Assets',
          url: 'asset/index.jsp',
          locationMatch: 'asset',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Path Outages',
          url: 'pathOutage/index.jsp',
          locationMatch: 'pathOutage',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Device Configs',
          url: 'ui/index.html#/device-config-backup',
          locationMatch: 'configurationManagement',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: true,
          roles: 'ROLE_ADMIN,ROLE_REST,ROLE_DEVICE_CONFIG_BACKUP'
        }
      ]
    },
    {
      id: null,
      className: 'org.opennms.web.navigate.MenuDropdownNavBarEntry',
      name: 'Status',
      url: '#',
      locationMatch: null,
      icon: null,
      iconType: null,
      isIconOnly: null,
      isVueLink: null,
      roles: null,
      items: [
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Events',
          url: 'event/index',
          locationMatch: 'event',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Alarms',
          url: 'alarm/index.htm',
          locationMatch: 'alarm',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Notifications',
          url: 'notification/index.jsp',
          locationMatch: 'notification',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Outages',
          url: 'outage/index.jsp',
          locationMatch: 'outage',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Surveillance',
          url: 'surveillance-view.jsp',
          locationMatch: 'surveillance-view',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Heatmap',
          url: 'heatmap/index.jsp',
          locationMatch: 'heatmap',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Trend',
          url: 'trend/index.jsp',
          locationMatch: 'trend',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Application',
          url: 'application/index.jsp',
          locationMatch: 'application',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        }
      ]
    },
    {
      id: null,
      className: 'org.opennms.web.navigate.MenuDropdownNavBarEntry',
      name: 'Reports',
      url: 'report/index.jsp',
      locationMatch: null,
      icon: null,
      iconType: null,
      isIconOnly: null,
      isVueLink: null,
      roles: null,
      items: [
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Charts',
          url: 'charts/index.jsp',
          locationMatch: 'chart',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Resource Graphs',
          url: 'graph/index.jsp',
          locationMatch: 'performance',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'KSC Reports',
          url: 'KSC/index.jsp',
          locationMatch: 'ksc',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Database Reports',
          url: 'report/database/index.jsp',
          locationMatch: 'database-reports',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Statistics',
          url: 'statisticsReports/index.htm',
          locationMatch: 'reports',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        }
      ]
    },
    {
      id: null,
      className: 'org.opennms.web.navigate.MenuDropdownNavBarEntry',
      name: 'Dashboards',
      url: 'dashboards.htm',
      locationMatch: null,
      icon: null,
      iconType: null,
      isIconOnly: null,
      isVueLink: null,
      roles: null,
      items: [
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Dashboard',
          url: 'dashboard.jsp',
          locationMatch: 'dashboard',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Ops Board',
          url: 'vaadin-wallboard',
          locationMatch: 'vaadin-wallboard',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        }
      ]
    },
    {
      id: null,
      className: 'org.opennms.web.navigate.MenuDropdownNavBarEntry',
      name: 'Maps',
      url: 'maps.htm',
      locationMatch: null,
      icon: null,
      iconType: null,
      isIconOnly: null,
      isVueLink: null,
      roles: null,
      items: [
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Topology',
          url: 'topology',
          locationMatch: 'topology',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        },
        {
          id: null,
          className: 'org.opennms.web.navigate.LocationBasedNavBarEntry',
          name: 'Geographical',
          url: 'ui/index.html#/map',
          locationMatch: 'ui/index.html#/map',
          icon: null,
          iconType: null,
          isIconOnly: null,
          isVueLink: null,
          roles: null
        }
      ]
    }
  ],
  helpMenu: {
    id: null,
    className: null,
    name: 'Help',
    url: null,
    locationMatch: null,
    icon: null,
    iconType: null,
    isIconOnly: null,
    isVueLink: null,
    roles: null,
    items: [
      {
        id: null,
        className: null,
        name: 'Help',
        url: 'help/index.jsp',
        locationMatch: null,
        icon: 'fa-question-circle',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: null
      },
      {
        id: null,
        className: null,
        name: 'About',
        url: 'about/index.jsp',
        locationMatch: null,
        icon: 'fa-info-circle',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: null
      },
      {
        id: null,
        className: null,
        name: 'Support',
        url: 'support/index.jsp',
        locationMatch: null,
        icon: 'fa-life-ring',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: [
          'ROLE_ADMIN'
        ]
      }
    ]
  },
  selfServiceMenu: {
    id: null,
    className: null,
    name: 'admin',
    url: 'account/selfService/index.jsp',
    locationMatch: null,
    icon: 'fa-user',
    iconType: 'fa',
    isIconOnly: null,
    isVueLink: null,
    roles: null,
    items: [
      {
        id: null,
        className: null,
        name: 'Change Password',
        url: 'account/selfService/newPasswordEntry',
        locationMatch: null,
        icon: 'fa-key',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: null
      },
      {
        id: null,
        className: null,
        name: 'Log Out',
        url: 'j_spring_security_logout',
        locationMatch: null,
        icon: 'fa-sign-out',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: null
      }
    ]
  },
  userNotificationMenu: {
    id: null,
    className: null,
    name: null,
    url: null,
    locationMatch: null,
    icon: null,
    iconType: null,
    isIconOnly: null,
    isVueLink: null,
    roles: null,
    items: [
      {
        id: 'user',
        className: null,
        name: null,
        url: 'notification/browse?acktype=unack&filter=user==admin',
        locationMatch: null,
        icon: 'fa-user',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: null
      },
      {
        id: 'team',
        className: null,
        name: null,
        url: 'notification/browse?acktype=unack',
        locationMatch: null,
        icon: 'fa-users',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: null
      },
      {
        id: 'oncall',
        className: null,
        name: 'On-Call Schedule',
        url: 'roles',
        locationMatch: null,
        icon: 'fa-calendar',
        iconType: 'fa',
        isIconOnly: null,
        isVueLink: null,
        roles: null
      }
    ]
  },
  provisionMenu: {
    id: null,
    className: null,
    name: 'Quick-Add Node',
    url: 'admin/ng-requisitions/quick-add-node.jsp#/',
    locationMatch: null,
    icon: 'fa-plus-circle',
    iconType: 'fa',
    isIconOnly: null,
    isVueLink: null,
    roles: [
      'ROLE_ADMIN',
      'ROLE_PROVISION'
    ]
  },
  flowsMenu: null,

  configurationMenu: {
    id: null,
    className: null,
    name: 'Configure OpenNMS',
    url: 'admin/index.jsp',
    locationMatch: null,
    icon: 'fa-cogs',
    iconType: 'fa',
    isIconOnly: null,
    isVueLink: null,
    roles: [
      'ROLE_ADMIN'
    ]
  },
  notices: {
    countUser: null,
    countNonUser: null,
    linkUser: null,
    linkNonUser: null,
    status: 'Off'
  }
} as MainMenu

const defaultNotificationSummary = {
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

const getMainMenu = async (context: ContextWithState) => {
  // for using local data for dev/debugging purposes
  if (useFakeMenuData) {
    context.commit('SAVE_MAIN_MENU', defaultMainMenu)
    return
  }

  const resp = await API.getMainMenu()

  if (resp) {
    const mainMenu = resp as MainMenu
    context.commit('SAVE_MAIN_MENU', mainMenu)
  }
}

const getNotificationSummary = async (context: ContextWithState) => {
  // for using local data for dev/debugging purposes
  if (useFakeUserNotificationData) {
    context.commit('SAVE_NOTIFICATION_SUMMARY', defaultNotificationSummary)
    return
  }

  const resp = await API.getNotificationSummary()

  if (resp) {
    const notificationSummary = resp as NotificationSummary
    context.commit('SAVE_NOTIFICATION_SUMMARY', notificationSummary)
  }
}

export default {
  getMainMenu,
  getNotificationSummary
}