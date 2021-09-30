//this file is copied from vue-ui.  new code are marked
import { Commit, Dispatch } from 'vuex'

export interface VuexContext {
  commit: Commit
  dispatch: Dispatch
}

//added by Jane
export interface MapNode {
  id: string
  coordinates: [number, number]
  foreignSource: string
  foreignId: string
  lable: string
  lableSource: any
  lastCapabilitiesScan: string
  primaryInterface: number
  sysObjectid: string
  sysDescription: string
  sysName: string
  sysContact: any
  sysLocation: any
  alarm: Alarm[]
}

//added by Jane
export interface Alarm {
  id: string
  severity: string
  nodeId: string
  nodeLabel: string
  uei: string
  count: number
  lastEvent: any
  logMessage: string
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
  //added by Jane
  lableSource: string
  lastCapabilitiesScan: string
  primaryInterface: number
  sysObjectid: string
  sysDescription: string
  sysName: string
  sysContact: string
  sysLocation: string
}

export interface ApiResponse {
  count: number
  offset: number
  totalCount: number
}

export interface NodeApiResponse extends ApiResponse {
  node: Node[]
}

export interface AlarmApiResponse extends ApiResponse {
  node: Alarm[]
}

export interface GraphNodesApiResponse {
  vertices: Vertice[]
  edges: Edge[]
}

//added by Jane
export interface Vertice {
  tooltipText: string
  namespace: string
  ipAddress: string
  x: string
  y: string
  id: string
  label: string
  iconKey: string
  nodeId: string
}

//added by Jane
export interface Edge {
  source: { namespace: string, id: number }
  target: { namespace: string, id: number }
}
export interface QueryParameters {
  limit?: number
  offset?: number
  _s?: string
  orderBy?: string
  order?: 'asc' | 'desc'
  [x: string]: any
}

export interface SortProps {
  filters: Object
  first: Number
  multiSortMeta: Object
  originalEvent: MouseEvent
  rows: Number
  sortField: string
  sortOrder: 1 | -1
}

export interface AlarmQueryParameters {
  ack?: boolean
  clear?: boolean
  escalate?: boolean
}

export interface AlarmModificationQueryVariable {
  pathVariable: string
  queryParameters:  AlarmQueryParameters
}
