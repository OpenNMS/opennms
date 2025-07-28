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

import {
  getNodes,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
} from './nodeService'
import { getCategories } from './categoryService'
import { getMonitoringLocations } from './monitoringLocationService'
import { getProvisionDService, putProvisionDService } from './configurationService'
import {
  getGraphNodesNodes,
  getGraphDefinitionsByResourceId,
  getDefinitionData,
  getGraphMetrics,
  getPreFabGraphs
} from './graphService'

import {
  getDeviceConfigBackups,
  backupDeviceConfig,
  downloadDeviceConfigs,
  getVendorOptions,
  getOsImageOptions,
  getHistoryByIpInterface
} from './deviceService'

import { getMainMenu, getNotificationSummary } from './menuService'
import { getMainMonitoringSystem } from './monitoringSystemService'
import { getFileNames, getFile, getSnippets, postFile, deleteFile, getFileExtensions } from './configService'
import { getAliases, getCredentialsByAlias, addCredentials, updateCredentials } from './scvService'

import { getAlarms, modifyAlarm } from './alarmService'
import { getEvents } from './eventService'
import { getNodeIfServices } from './ifService'
import { getIpInterfaces, getNodeIpInterfaceQuery } from './ipInterfaceService'
import { search } from './searchService'
import { getLogs, getLog } from './logsService'
import { getWhoAmI } from './whoAmIService'
import { getInfo } from './infoService'
import { getOpenApiV1, getOpenApi } from './helpService'
import { getResources, getResourceForNode } from './resourceService'
import { getPlugins } from './pluginService'
import {
  getUsageStatistics,
  getUsageStatisticsMetadata,
  getUsageStatisticsStatus,
  setUsageStatisticsStatus
} from './usageStatisticsService'
import {
  addZenithRegistration,
  getZenithRegistrations
} from './zenithConnectService'

export default {
  search,
  getInfo,
  getNodes,
  getAlarms,
  getEvents,
  modifyAlarm,
  getNodeById,
  getNodeOutages,
  getNodeIfServices,
  getIpInterfaces,
  getNodeIpInterfaceQuery,
  getGraphNodesNodes,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage,
  getCategories,
  getMonitoringLocations,
  getLog,
  getLogs,
  getFile,
  postFile,
  getWhoAmI,
  deleteFile,
  getSnippets,
  getMainMenu,
  getNotificationSummary,
  getMainMonitoringSystem,
  getFileNames,
  getFileExtensions,
  getOpenApiV1,
  getOpenApi,
  getProvisionDService,
  putProvisionDService,
  getResources,
  getGraphMetrics,
  getPreFabGraphs,
  getDefinitionData,
  getResourceForNode,
  getGraphDefinitionsByResourceId,
  getPlugins,
  getDeviceConfigBackups,
  backupDeviceConfig,
  downloadDeviceConfigs,
  getVendorOptions,
  getOsImageOptions,
  getHistoryByIpInterface,
  getAliases,
  getCredentialsByAlias,
  addCredentials,
  updateCredentials,
  getUsageStatistics,
  getUsageStatisticsMetadata, 
  getUsageStatisticsStatus,
  setUsageStatisticsStatus,
  addZenithRegistration,
  getZenithRegistrations
}
