import { SearchResult } from '@/types'
import { NodePoint } from '@/types/topology'
import { Edges, Node, Nodes } from 'v-network-graph'

export interface State {
  isTopologyView: boolean
  selectedView: string
  edges: Edges
  verticies: Nodes
  semanticZoomLevel: number
  isLeftDrawerOpen: boolean
  focusedNodeIds: string[]
  layout: Record<string, NodePoint>
  focusedSearchBarNodes: SearchResult[]
  defaultNode: Node | null
  highlightFocusedNodes: boolean,
  modalState: boolean
}

const state: State = {
  isTopologyView: false,
  selectedView: 'map',
  edges: {},
  verticies: {},
  semanticZoomLevel: 1,
  isLeftDrawerOpen: true,
  focusedNodeIds: [],
  layout: {},
  focusedSearchBarNodes: [],
  defaultNode: null,
  highlightFocusedNodes: false,
  modalState: false
}

export default state
