import { VuexContext } from '@/types'
// import API from '@/services'
import { State } from './state'
import { MainMenuDefinition } from '@/types/mainMenu'

interface ContextWithState extends VuexContext {
  state: State
}

const defaultMainMenuDefinition = {
  displayAdminLink: true,
  countNoticesAssignedToUser: 0,
  countNoticesAssignedToOtherThanUser: 1,
  noticesAssignedToUserLink: '/opennms/notification/browse?acktype=unack&filter=user==admin1',
  noticesAssignedToOtherThanUserLink: '/opennms/notification/browse?acktype=unack',
  noticeStatus: 'off',
  adminLink: '/opennms/admin/index.jsp',
  rolesLink: '/opennms/roles',
  searchLink: '/opennms/element/index.jsp',
  selfServiceLink: '/opennms/account/selfService/',
  quickAddNodeLink: '/opennms/admin/ng-requisitions/quick-add-node.jsp',
  username: 'admin1',
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
      icon: 'Person',
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
  // TODO: Replace with actual API call
  // const mainMenuDefinition = await API.getMainMenu()
  const mainMenuDefinition = defaultMainMenuDefinition
  context.commit('SAVE_MAIN_MENU_DEFINITION', mainMenuDefinition)
}

export default {
  getMainMenuDefinition
}