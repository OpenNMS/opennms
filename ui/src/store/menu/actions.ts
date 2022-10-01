import { VuexContext } from '@/types'
// import API from '@/services'
import { State } from './state'
import { MainMenuDefinition } from '@/types/mainMenu'

interface ContextWithState extends VuexContext {
  state: State
}

const fakeMainMenuDefinition = {
  noticeStatus: 'off',
  displayAdminLink: true,
  username: 'admin1',
  countNoticesAssignedToUser: 0,
  countNoticesAssignedToOtherThanUser: 1,
  selfServiceLink: '/opennms/account/selfService/',
  noticesAssignedToUserLink: '/opennms/notification/browse?acktype=unack&filter=user=={{ username }}',
  noticesAssignedToOtherThanUserLink: '/opennms/notification/browse?acktype=unack',
  rolesLink: '/opennms/roles',
  quickAddNodeLink: '/opennms/admin/ng-requisitions/quick-add-node.jsp',
  adminLink: '/opennms/admin/index',

  menuItems: [
    {
      name: 'Info',
      items: [
        {
          name: 'Nodes',
          url: '/opennms/element/nodeList.htm'
        },
        {
          name: 'Assets',
          url: '/opennms/asset/index.jsp'
        },
        {
          name: 'Path Outages',
          url: '/opennms/pathOutage/index.jsp'
        },
        {
          name: 'Device Configs',
          url: '/opennms/ui/index.html#/device-config-backup',
          isVueLink: true
        }
      ]
    },
    {
      name: 'Status',
      items: [
        {
          name: 'Events',
          url: '/opennms/event/index'
        },
        {
          name: 'Alarms',
          url: '/opennms/alarm/index.htm'
        },
        {
          name: 'Notifications',
          url: '/opennms/notification/index.jsp'
        },
        {
          name: 'Outages',
          url: '/opennms/outage/index.jsp'
        },
        {
          name: 'Surveillance',
          url: '/opennms/surveillance-view.jsp'
        },
        {
          name: 'Heatmap',
          url: '/opennms/heatmap/index.jsp'
        },
        {
          name: 'Trend',
          url: '/opennms/trend/index.jsp'
        },
        {
          name: 'Application',
          url: '/opennms/application/index.jsp'
        }
      ]
    },
    {
      name: 'Reports',
      items: [
        {
          name: 'Charts',
          url: '/opennms/charts/index.jsp'
        },
        {
          name: 'Resource Graphs',
          url: '/opennms/graph/index.jsp'
        },
        {
          name: 'KSC Reports',
          url: '/opennms/KSC/index.jsp'
        },
        {
          name: 'Database Reports',
          url: '/opennms/report/database/index.jsp'
        },
        {
          name: 'Statistics',
          url: '/opennms/statisticsReports/index.htm'
        }
      ]
    },
    {
      name: 'Dashboards',
      items: [
        {
          name: 'Dashboard',
          url: '/opennms/dashboard.jsp'
        },
        {
          name: 'Ops Board',
          url: '/opennms/vaadin-wallboard'
        }
      ]
    },
    {
      name: 'Maps',
      items: [
        {
          name: 'Topology',
          url: '/opennms/topology'
        },
        {
          name: 'Geographical',
          url: '/opennms/node-maps'
        }
      ]
    },
    {
      name: 'Help',
      items: [
        {
          name: 'Help',
          url: '/opennms/help/index.jsp'
        },
        {
          name: 'About',
          url: '/opennms/about/index.jsp'
        },
        {
          name: 'Support',
          url: '/opennms/support/index.jsp'
        }
      ]
    },
    {
      name: 'admin1',
      url: '/opennms/account/selfService/',
      items: [
        {
          name: 'Account',
          url: '/opennms/account/selfService/'
        },
        {
          name: 'Change Password',
          url: '/opennms/account/selfService/newPasswordEntry'
        },
        {
          name: 'Log Out',
          url: '/opennms/j_spring_security_logout'
        }
      ]
    }
  ]
} as MainMenuDefinition

const getMainMenuDefinition = async (context: ContextWithState) => {
  // const mainMenuDefinition = await API.getMainMenu()
  console.log('DEBUG in getMainMenuDefinition')
  const mainMenuDefinition = fakeMainMenuDefinition
  context.commit('SAVE_MAIN_MENU_DEFINITION', mainMenuDefinition)
}

export default {
  getMainMenuDefinition
}