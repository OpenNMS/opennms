import {
  getNodes,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
} from './nodeService'

import { getGraphDefinitionsByResourceId, getDefinitionData, getGraphMetrics, getPreFabGraphs } from './graphService'

import {
  getDeviceConfigBackups,
  backupDeviceConfig,
  downloadDeviceConfigs,
  getVendorOptions,
  getOsImageOptions,
  getHistoryByIpInterface
} from './deviceService'

import { getAlarms, modifyAlarm } from './alarmService'
import { getEvents } from './eventService'
import { getNodeIfServices } from './ifService'
import { search } from './searchService'
import { getLocations } from './locationsService'
import { scanIPRanges, detectSNMPAvailable, provision } from './inventoryService'
import { getFileNames, getFile, getSnippets, postFile, deleteFile, getFileExtensions } from './configService'
import { getLogs, getLog } from './logsService'
import { getWhoAmI } from './whoAmIService'
import { getInfo } from './infoService'
import { getOpenApi } from './helpService'
import { getResources, getResourceForNode } from './resourceService'
import {
  getVerticesAndEdges,
  getNodesTopologyDataByLevelAndFocus,
  getPowerGridTopologyDataByLevelAndFocus,
  getTopologyGraphs,
  getTopologyGraphByContainerAndNamespace
} from './topologyService'
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
  getVerticesAndEdges,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage,
  scanIPRanges,
  detectSNMPAvailable,
  provision,
  getLocations,
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
  getTopologyGraphs,
  getTopologyGraphByContainerAndNamespace,
  getNodesTopologyDataByLevelAndFocus,
  getPowerGridTopologyDataByLevelAndFocus,
  getDeviceConfigBackups,
  backupDeviceConfig,
  downloadDeviceConfigs,
  getVendorOptions,
  getOsImageOptions,
  getHistoryByIpInterface
}
