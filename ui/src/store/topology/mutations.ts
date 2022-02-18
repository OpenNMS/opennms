import { NetworkGraphEdge, NetworkGraphVertex } from '@/types/topology'
import { State } from './state'

const SAVE_NODE_EDGES = (state: State, edges: Record<string, NetworkGraphEdge>) => {
  state.edges = edges
}

const SAVE_NODE_VERTICIES = (state: State, verticies: Record<string, NetworkGraphVertex>) => {
  state.verticies = verticies
}

const SET_SEMANTIC_ZOOM_LEVEL = (state: State, SML: number) => {
  state.semanticZoomLevel = SML
}

const SET_SELECTED_VIEW = (state: State, view: string) => {
  state.isTopologyView = view !== 'map' ? true : false
  state.selectedView = view
}

export default {
  SAVE_NODE_EDGES,
  SAVE_NODE_VERTICIES,
  SET_SEMANTIC_ZOOM_LEVEL,
  SET_SELECTED_VIEW
}
