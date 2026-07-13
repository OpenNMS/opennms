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

import IconDashboard from '@/components/icons/action/Dashboard.vue'
import IconHelp from '@/components/icons/action/Help.vue'
import IconHome from '@/components/icons/action/Home.vue'
import IconLock from '@/components/icons/action/Lock.vue'
import IconLogout from '@/components/icons/action/LogOut.vue'
import IconLocation from '@/components/icons/action/Location.vue'
import IconManageProfile from '@/components/icons/action/ManageProfile.vue'
import IconPerson from '@/components/icons/action/Person.vue'
import IconSearch from '@/components/icons/action/Search.vue'
import IconContactSupport from '@/components/icons/action/ContactSupport.vue'
import IconUnlock from '@/components/icons/action/Unlock.vue'
import IconView from '@/components/icons/action/View.vue'
import IconViewDetails from '@/components/icons/action/ViewDetails.vue'
import IconWorkflow from '@/components/icons/action/Workflow.vue'

import IconColumnChart from '@/components/icons/datavis/ColumnChart.vue'
import IconLineChart from '@/components/icons/datavis/LineChart.vue'

import IconApiConfig from '@/components/icons/network/ApiConfig.vue'
import IconApiEndpoints from '@/components/icons/network/ApiEndpoints.vue'
import IconBuild from '@/components/icons/network/Build.vue'
import IconConfiguration from '@/components/icons/network/Configuration.vue'
import IconDistributedMonitoring from '@/components/icons/network/DistributedMonitoring.vue'
import IconInstances from '@/components/icons/network/Instances.vue'
import IconLogsAlt from '@/components/icons/network/LogsAlt.vue'
import IconInventory from '@/components/icons/network/Inventory.vue'
import IconInventoryAlt from '@/components/icons/network/InventoryAlt.vue'
import IconMonitoring from '@/components/icons/network/Monitoring.vue'
import IconNetworkConnection from '@/components/icons/network/Connection.vue'
import IconNetworkServer from '@/components/icons/network/Server.vue'
import IconNodes from '@/components/icons/network/Nodes.vue'

const IconCategories = ['action', 'datavis', 'network']

const useMenuIcons = () => {
  // iconId should be a specifier from our Onms icons, example:
  // actions/accountCircle
  const getIcon = (iconId?: string | null) => {
    const arr = (iconId ?? '').split('/')

    if (arr.length === 2) {
      const path = arr[0] || ''
      const item = arr[1].match(/[A-Za-z0-9]+/) ? arr[1] : ''

      if (path.length > 0 && IconCategories.includes(path) && item.length > 0) {

        if (path === 'action') {
          switch (item) {
            case 'Dashboard': return IconDashboard
            case 'Help': return IconHelp

            case 'Home': return IconHome
            case 'Location': return IconLocation
            case 'Lock': return IconLock
            case 'Logout': return IconLogout
            case 'ManageProfile': return IconManageProfile
            case 'Person': return IconPerson
            case 'Search': return IconSearch
            case 'ContactSupport': return IconContactSupport
            case 'Unlock': return IconUnlock
            case 'View': return IconView
            case 'ViewDetails': return IconViewDetails
            case 'Workflow': return IconWorkflow
            default: return null
          }
        } else if (path === 'datavis') {
          switch (item) {
            case 'ColumnChart': return IconColumnChart
            case 'LineChart': return IconLineChart
            default: return null
          }
        } else if (path === 'network') {
          switch (item) {
            case 'ApiConfig': return IconApiConfig
            case 'ApiEndpoints': return IconApiEndpoints
            case 'Build': return IconBuild
            case 'Configuration': return IconConfiguration
            case 'Connection': return IconNetworkConnection
            case 'DistributedMonitoring': return IconDistributedMonitoring
            case 'Instances': return IconInstances
            case 'Inventory': return IconInventory
            case 'InventoryAlt': return IconInventoryAlt
            case 'LogsAlt': return IconLogsAlt
            case 'Monitoring': return IconMonitoring
            case 'Server': return IconNetworkServer
            case 'Nodes': return IconNodes
            default: return null
          }
        }
      }
    }

    return null
  }

  return { getIcon }
}

export default useMenuIcons
