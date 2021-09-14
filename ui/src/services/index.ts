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

export default {
  search,
  getNodes,
  getEvents,
  getNodeById,
  getNodeOutages,
  getNodeIfServices,
  getNodeIpInterfaces,
  getNodeSnmpInterfaces,
  getNodeAvailabilityPercentage
}
