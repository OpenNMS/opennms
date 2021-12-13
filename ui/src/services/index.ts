import {
  getNodes,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
} from './nodeService'

import { getFileNames, getFile, getSnippets, postFile, deleteFile, getFileExtensions } from './configService'
import { getLogs, getLog } from './logsService'
import { getWhoAmI } from './whoAmIService'
import { getEvents } from './eventService'
import { getNodeIfServices } from './ifService'
import { search } from './searchService'

export default {
  search,
  getNodes,
  getEvents,
  getNodeById,
  getNodeOutages,
  getNodeIfServices,
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
}
