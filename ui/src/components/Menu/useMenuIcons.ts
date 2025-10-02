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

import IconDashboard from '@featherds/icon/action/Dashboard'
import IconHelp from '@featherds/icon/action/Help'
import IconHome from '@featherds/icon/action/Home'
import IconLock from '@featherds/icon/action/Lock'
import IconLogout from '@featherds/icon/action/LogOut'
import IconLocation from '@featherds/icon/action/Location'
import IconManageProfile from '@featherds/icon/action/ManageProfile'
import IconPerson from '@featherds/icon/action/Person'
import IconSearch from '@featherds/icon/action/Search'
import IconContactSupport from '@featherds/icon/action/ContactSupport'
import IconUnlock from '@featherds/icon/action/Unlock'
import IconView from '@featherds/icon/action/View'
import IconViewDetails from '@featherds/icon/action/ViewDetails'
import IconWorkflow from '@featherds/icon/action/Workflow'

import IconColumnChart from '@featherds/icon/datavis/ColumnChart'
import IconLineChart from '@featherds/icon/datavis/LineChart'

import IconApiConfig from '@featherds/icon/network/ApiConfig'
import IconApiEndpoints from '@featherds/icon/network/ApiEndpoints'
import IconBuild from '@featherds/icon/network/Build'
import IconConfiguration from '@featherds/icon/network/Configuration'
import IconDistributedMonitoring from '@featherds/icon/network/DistributedMonitoring'
import IconInstances from '@featherds/icon/network/Instances'
import IconLogsAlt from '@featherds/icon/network/LogsAlt'
import IconInventory from '@featherds/icon/network/Inventory'
import IconInventoryAlt from '@featherds/icon/network/InventoryAlt'
import IconMonitoring from '@featherds/icon/network/Monitoring'
import IconNetworkConnection from '@featherds/icon/network/Connection'
import IconNetworkServer from '@featherds/icon/network/Server'
import IconNodes from '@featherds/icon/network/Nodes'

const IconCategories = ['action', 'datavis', 'network']

const useMenuIcons = () => {
  // iconId should be a specifier from Feather, example:
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
