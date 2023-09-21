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
import { getProvisionDService, putProvisionDService, populateProvisionD } from './configurationService'
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
  getFileNames,
  getFileExtensions,
  getOpenApiV1,
  getOpenApi,
  getProvisionDService,
  populateProvisionD,
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
  setUsageStatisticsStatus
}
