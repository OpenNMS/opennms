import { PowerGrid } from '@/components/Topology/topology.constants'
import { IdLabelProps, SearchResult } from '@/types'
import { TopologyGraphList } from '@/types/topology'
import { Edges, Node, Nodes } from 'v-network-graph'
import { State } from './state'

const SAVE_EDGES = (state: State, edges: Edges) => {
  state.edges = edges
}

const SAVE_VERTICES = (state: State, vertices: Nodes) => {
  state.vertices = vertices
}

const SAVE_TOPOLOGY_GRAPHS = (state: State, topologyGraphs: TopologyGraphList[]) => {
  state.topologyGraphs = topologyGraphs
}

const SAVE_DEFAULT_OBJECTS = (state: State, defaultObjects: Node[]) => {
  state.defaultObjects = defaultObjects
}

const SET_SEMANTIC_ZOOM_LEVEL = (state: State, SML: number) => {
  state.semanticZoomLevel = SML
}

const SET_SELECTED_VIEW = (state: State, view: string) => {
  state.isTopologyView = view !== 'map' ? true : false
  state.selectedView = view
}

const SET_SELECTED_DISPLAY = (state: State, display: string) => {
  // close the powergrid sidebar if different display selected
  if (display !== PowerGrid) {
    state.isRightDrawerOpen = false
  }

  state.selectedDisplay = display
}

const SET_LEFT_DRAWER_OPEN = (state: State, bool: boolean) => {
  state.isLeftDrawerOpen = bool
}

const SET_RIGHT_DRAWER_OPEN = (state: State, bool: boolean) => {
  state.isRightDrawerOpen = bool
}

const ADD_FOCUS_OBJECTS = (state: State, ids: IdLabelProps[]) => {
  state.focusObjects = ids
}

const ADD_FOCUS_OBJECT = (state: State, id: IdLabelProps) => {
  state.focusObjects = [...state.focusObjects, id]
}

const REMOVE_FOCUS_OBJECT = (state: State, id: string) => {
  state.focusObjects = state.focusObjects.filter((obj) => obj.id !== id)
}

const SET_HIGHLIGHT_FOCUSED_OBJECTS = (state: State, bool: boolean) => {
  state.highlightFocusedObjects = bool
}

const SET_MODAL_STATE = (state: State, bool: boolean) => {
  state.modalState = bool
}

const UPDATE_NODE_ICONS = (state: State, nodeIdIconKey: Record<string, string>) => {
  state.nodeIcons = { ...state.nodeIcons, ...nodeIdIconKey }
}

const SET_CONTAINER_AND_NAMESPACE = (
  state: State,
  { container, namespace }: { container: string; namespace: string }
) => {
  state.container = container
  state.namespace = namespace
}

const SAVE_IDS_WITH_SUBLAYERS = (state: State, idsWithSubLayers: string[]) => {
  state.idsWithSubLayers = idsWithSubLayers
}

export default {
  SAVE_EDGES,
  SAVE_VERTICES,
  SET_SEMANTIC_ZOOM_LEVEL,
  SET_SELECTED_VIEW,
  SET_SELECTED_DISPLAY,
  SET_LEFT_DRAWER_OPEN,
  SET_RIGHT_DRAWER_OPEN,
  ADD_FOCUS_OBJECTS,
  ADD_FOCUS_OBJECT,
  REMOVE_FOCUS_OBJECT,
  SAVE_DEFAULT_OBJECTS,
  SET_HIGHLIGHT_FOCUSED_OBJECTS,
  SET_MODAL_STATE,
  UPDATE_NODE_ICONS,
  SAVE_TOPOLOGY_GRAPHS,
  SET_CONTAINER_AND_NAMESPACE,
  SAVE_IDS_WITH_SUBLAYERS
}
