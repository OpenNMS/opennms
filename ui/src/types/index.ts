import { Commit, Dispatch } from 'vuex'
import { SORT } from '@featherds/table'

export interface VuexContext {
  commit: Commit
  dispatch: Dispatch
}

export interface SearchResultResponse {
  label?: string
  context: {
    name: string
    weight: number
  }
  empty: boolean
  more: boolean
  results: {
    identifier: string
    label: string
    matches: any
    properties: any
    url: string
    weight: number
  }[]
}

export interface ApiResponse {
  count: number
  offset: number
  totalCount: number
}

export interface NodeApiResponse extends ApiResponse {
  node: Node[]
}
export interface EventApiResponse extends ApiResponse {
  event: Event[]
}

export interface SnmpInterfaceApiResponse extends ApiResponse {
  snmpInterface: SnmpInterface[]
}

export interface IpInterfaceApiResponse extends ApiResponse {
  ipInterface: IpInterface[]
}

export interface OutagesApiResponse extends ApiResponse {
  outage: Outage[]
}

export interface IfServiceApiResponse extends ApiResponse {
  'monitored-service': IfService[]
}

export interface Node {
  location: string
  type: string
  label: string
  id: string
  assetRecord: any
  categories: Category[]
  createTime: number
  foreignId: string
  foreignSource: string
  lastEgressFlow: any
  lastIngressFlow: any
}

export interface Event {
  createTime: number
  description: string
  display: string
  id: number
  label: string
  location: string
  log: string
  logMessage: string
  nodeId: number
  nodeLabel: string
  parameters: Array<any>
  severity: string
  source: string
  time: number
  uei: string
}

export interface SnmpInterface {
  collect: boolean
  collectFlag: string
  collectionUserSpecified: boolean
  hasEgressFlows: boolean
  hasFlows: boolean
  hasIngressFlows: boolean
  id: number
  ifAdminStatus: number
  ifAlias: any
  ifDescr: any
  ifIndex: number
  ifName: any
  ifOperStatus: number
  ifSpeed: number
  ifType: number
  lastCapsdPoll: number
  lastEgressFlow: any
  lastIngressFlow: any
  lastSnmpPoll: number
  physAddr: any
  poll: boolean
}

export interface IpInterface {
  ifIndex: string
  isManaged: null | string
  id: string
  ipAddress: string
  isDown: boolean
  lastCapsdPoll: number
  lastEgressFlow: any
  lastIngressFlow: any
  monitoredServiceCount: number
  nodeId: number
  snmpInterface: SnmpInterface
  snmpPrimary: string
  hostName: string
}

export interface Outage {
  nodeId: number
  ipAddress: string
  serviceIs: number
  nodeLabel: string
  location: string
  hostname: string
  serviceName: string
  outageId: number
}

export interface IfService {
  id: string
  ipAddress: string
  ipInterfaceId: number
  isDown: false
  isMonitored: boolean
  node: string
  serviceName: string
  status: string
  statusCode: string
}

export interface Category {
  authorizedGroups: string[]
  id: number
  name: string
}

export interface QueryParameters {
  limit?: number
  offset?: number
  _s?: string
  orderBy?: string
  order?: SORT
  [x: string]: any
}

export interface FeatherSortObject {
  property: string
  value: SORT
}

export interface SortProps extends FeatherSortObject {
  filters: Object
  first: Number
  multiSortMeta: Object
  originalEvent: MouseEvent
  rows: Number
  sortField: string
  sortOrder: 1 | -1
}

export interface NodeAvailability {
  availability: number
  id: number
  ipinterfaces: {
    address: string
    availability: number
    id: number
    services: [
      {
        id: number
        name: string
        availability: number
      }
    ]
  }[]
  'service-count': number
  'service-down-count': number
}

export interface BreadCrumb {
  label: string
  to: string
  position?: string
}
