import {
  getNodes,
  getNodeById,
  getNodeOutages,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
} from './nodeService'

import { getEvents } from './eventService'
import { getNodeIfServices } from './ifService'
import { search } from './searchService'
import { getLocations } from './locationsService'
import { scanIPRanges, detectSNMPAvailable, provision } from './inventoryService'
import { getFileNames, getFile, getSnippets, postFile } from './configService'
import { getLogs, getLog } from './logsService'
import { getSummary } from './summaryService'

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
  scanIPRanges,
  detectSNMPAvailable,
  provision,
  getLocations,
  getFileNames,
  getFile,
  postFile,
  getSnippets,
  getLogs,
  getLog,
  getSummary
}
