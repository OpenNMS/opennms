import { SearchResult } from '@/types'
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
  focusedNodeIds: string[]
  layout: Record<string, NodePoint>
  focusedSearchBarNodes: SearchResult[]
  defaultNode: Node | null
  highlightFocusedNodes: boolean,
  modalState: boolean
  nodeIcons: Record<string, string>
  topologyGraphs: TopologyGraphList[]
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
  focusedNodeIds: [],
  layout: {},
  focusedSearchBarNodes: [],
  defaultNode: null,
  highlightFocusedNodes: false,
  modalState: false,
  nodeIcons: {},
  topologyGraphs: []
}

export default state
