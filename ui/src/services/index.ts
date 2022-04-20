import {
  getNodes,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
} from './nodeService'

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

import { getFileNames, getFile, getSnippets, postFile, deleteFile, getFileExtensions } from './configService'

import { getAlarms, modifyAlarm } from './alarmService'
import { getEvents } from './eventService'
import { getNodeIfServices } from './ifService'
import { search } from './searchService'
import { getLogs, getLog } from './logsService'
import { getWhoAmI } from './whoAmIService'
import { getInfo } from './infoService'
import { getOpenApi } from './helpService'
import { getResources, getResourceForNode } from './resourceService'
import { getPlugins } from './pluginService'

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
  getGraphNodesNodes,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage,
  getLog,
  getLogs,
  getFile,
  postFile,
  getWhoAmI,
  deleteFile,
  getSnippets,
  getFileNames,
  getFileExtensions,
  getOpenApi,
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
  getHistoryByIpInterface
}
