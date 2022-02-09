import {
  getNodes,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
} from './nodeService'

import { getProvisionDService, putProvisionDService, populateProvisionD } from './configurationService'
import { getAlarms, modifyAlarm } from './alarmService'

import { getGraphNodesNodes } from './graphService'

import { getFileNames, getFile, getSnippets, postFile, deleteFile, getFileExtensions } from './configService'
import { getLogs, getLog } from './logsService'
import { getWhoAmI } from './whoAmIService'
import { getEvents } from './eventService'
import { getNodeIfServices } from './ifService'
import { search } from './searchService'
import { getInfo } from './infoService'
import { getOpenApi } from './helpService'

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
  getProvisionDService,
  populateProvisionD,
  putProvisionDService
}
