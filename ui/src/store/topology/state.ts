import { SearchResult } from '@/types'
import { NodePoint } from '@/types/topology'
import { Edges, Nodes } from 'v-network-graph'

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
  focusedSearchBarNodes: []
}

export default state
