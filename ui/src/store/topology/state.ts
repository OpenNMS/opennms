import { IdLabelProps, SearchResult } from '@/types'
import { NodePoint, TopologyGraphList } from '@/types/topology'
import { Edges, Node, Nodes } from 'v-network-graph'

export interface State {
  isTopologyView: boolean
  selectedView: string
  selectedDisplay: string
  edges: Edges
  verticies: Nodes
  semanticZoomLevel: number
  isLeftDrawerOpen: boolean
  isRightDrawerOpen: boolean
  focusObjects: IdLabelProps[]
  layout: Record<string, NodePoint>
  focusedSearchBarNodes: SearchResult[]
  defaultObjects: Node[] | null
  highlightFocusedNodes: boolean
  modalState: boolean
  nodeIcons: Record<string, string>
  topologyGraphs: TopologyGraphList[]
  container: string
  namespace: string
}

const state: State = {
  isTopologyView: false,
  selectedView: 'map',
  selectedDisplay: 'linkd',
  edges: {},
  verticies: {},
  semanticZoomLevel: 1,
  isLeftDrawerOpen: true,
  isRightDrawerOpen: false,
  focusObjects: [],
  layout: {},
  focusedSearchBarNodes: [],
  defaultObjects: null,
  highlightFocusedNodes: false,
  modalState: false,
  nodeIcons: {},
  topologyGraphs: [],
  container: '',
  namespace: ''
}

export default state
