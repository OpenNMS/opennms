import { Edges, Nodes } from 'v-network-graph'
import { State } from './state'

const SAVE_NODE_EDGES = (state: State, edges: Edges) => {
  state.edges = edges
}

const SAVE_NODE_VERTICIES = (state: State, verticies: Nodes) => {
  state.verticies = verticies
}

const SET_SEMANTIC_ZOOM_LEVEL = (state: State, SML: number) => {
  state.semanticZoomLevel = SML
}

const SET_SELECTED_VIEW = (state: State, view: string) => {
  state.isTopologyView = view !== 'map' ? true : false
  state.selectedView = view
}

const SET_LEFT_DRAWER_OPEN = (state: State, bool: boolean) => {
  state.isLeftDrawerOpen = bool
}

const ADD_FOCUSED_NODE_IDS = (state: State, ids: string[]) => {
  state.focusedNodeIds = ids
}

export default {
  SAVE_NODE_EDGES,
  SAVE_NODE_VERTICIES,
  SET_SEMANTIC_ZOOM_LEVEL,
  SET_SELECTED_VIEW,
  SET_LEFT_DRAWER_OPEN,
  ADD_FOCUSED_NODE_IDS
}
