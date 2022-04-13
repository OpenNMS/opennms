import { IdLabelProps, SearchResult } from '@/types'
import { NodePoint, TopologyGraphList } from '@/types/topology'
import { Edges, Node, Nodes } from 'v-network-graph'

export interface State {
  isTopologyView: boolean
  selectedView: string
  selectedDisplay: string
  edges: Edges
  vertices: Nodes
  semanticZoomLevel: number
  isLeftDrawerOpen: boolean
  isRightDrawerOpen: boolean
  focusObjects: IdLabelProps[]
  layout: Record<string, NodePoint>
  focusedSearchBarNodes: SearchResult[]
  defaultObjects: Node[] | null
  highlightFocusedObjects: boolean
  modalState: boolean
  nodeIcons: Record<string, string>
  topologyGraphs: TopologyGraphList[]
  container: string
  namespace: string
  idsWithSubLayers: string[]
}

const state: State = {
  isTopologyView: false,
  selectedView: 'map',
  selectedDisplay: 'linkd',
  edges: {},
  vertices: {},
  semanticZoomLevel: 1,
  isLeftDrawerOpen: true,
  isRightDrawerOpen: false,
  focusObjects: [],
  layout: {},
  focusedSearchBarNodes: [],
  defaultObjects: null,
  highlightFocusedObjects: false,
  modalState: false,
  nodeIcons: {},
  topologyGraphs: [],
  container: '',
  namespace: '',
  idsWithSubLayers: []
}

export default state
