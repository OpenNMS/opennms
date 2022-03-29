import { SearchResult } from '@/types'
import { Edges, Node, Nodes } from 'v-network-graph'
import { State } from './state'

const SAVE_NODE_EDGES = (state: State, edges: Edges) => {
  state.edges = edges
}

const SAVE_NODE_VERTICIES = (state: State, verticies: Nodes) => {
  state.verticies = verticies
}

const SAVE_DEFAULT_NODE = (state: State, defaultNode: Node) => {
  state.defaultNode = defaultNode
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

const ADD_NODE_TO_FOCUS_IDS = (state: State, id: string) => {
  state.focusedNodeIds = [...state.focusedNodeIds, id]
}

const REMOVE_NODE_FROM_FOCUS_IDS = (state: State, id: string) => {
  state.focusedNodeIds = state.focusedNodeIds.filter((focusedId) => focusedId !== id)
}

const SET_FOCUSED_SEARCH_BAR_NODES = (state: State, nodes: SearchResult[]) => {
  state.focusedSearchBarNodes = nodes
}

const ADD_FOCUSED_SEARCH_BAR_NODE = (state: State, node: SearchResult) => {
  state.focusedSearchBarNodes = [...state.focusedSearchBarNodes, node]
}

const REMOVE_FOCUSED_SEARCH_BAR_NODE = (state: State, node: SearchResult) => {
  state.focusedSearchBarNodes = state.focusedSearchBarNodes.filter((focusedNode) => focusedNode.label !== node.label)
}

const SET_HIGHLIGHT_FOCUSED_NODES = (state: State, bool: boolean) => {
  state.highlightFocusedNodes = bool
}

export default {
  SAVE_NODE_EDGES,
  SAVE_NODE_VERTICIES,
  SET_SEMANTIC_ZOOM_LEVEL,
  SET_SELECTED_VIEW,
  SET_LEFT_DRAWER_OPEN,
  ADD_FOCUSED_NODE_IDS,
  ADD_NODE_TO_FOCUS_IDS,
  REMOVE_NODE_FROM_FOCUS_IDS,
  SET_FOCUSED_SEARCH_BAR_NODES,
  ADD_FOCUSED_SEARCH_BAR_NODE,
  REMOVE_FOCUSED_SEARCH_BAR_NODE,
  SAVE_DEFAULT_NODE,
  SET_HIGHLIGHT_FOCUSED_NODES
}
