import { PowerGrid } from '@/components/Topology/topology.constants'
import API from '@/services'
import { IdLabelProps, QueryParameters, VuexContext } from '@/types'
import { SZLRequest, VerticesAndEdges } from '@/types/topology'
import { Edges, Nodes } from 'v-network-graph'
import { State } from './state'

interface ContextWithState extends VuexContext {
  state: State
}

const parseVerticesAndEdges = (resp: VerticesAndEdges, context: VuexContext) => {
  const edges: Edges = {}
  const vertices: Nodes = {}

  for (const edge of resp.edges) {
    edges[edge.label] = { source: edge.source.id, target: edge.target.id }
  }

  for (const vertex of resp.vertices) {
    vertices[vertex.id] = {
      name: vertex.label,
      id: vertex.id,
      tooltip: vertex.tooltipText,
      label: vertex.label,
      icon: 'generic_icon'
    }
  }

  if (resp.defaultFocus && resp.defaultFocus.vertexIds.length) {
    const defaultId = resp.defaultFocus.vertexIds[0].id
    if (defaultId) {
      const defaultObjects = vertices[defaultId]
      context.commit('SAVE_DEFAULT_OBJECTS', [defaultObjects])
    }
  }

  if (resp.focus) {
    const defaultId = resp.focus.vertices[0]
    if (defaultId) {
      const defaultObjects = vertices[defaultId]
      context.commit('SAVE_DEFAULT_OBJECTS', [defaultObjects])
    }
  }

  context.commit('SAVE_NODE_EDGES', edges)
  context.commit('SAVE_NODE_VERTICIES', vertices)
  context.dispatch('updateObjectFocusedProperty')
  context.dispatch('updateVerticesIconPaths')
}

const getVerticesAndEdges = async (context: VuexContext, queryParameters?: QueryParameters) => {
  const resp = await API.getVerticesAndEdges(queryParameters)
  if (resp) {
    parseVerticesAndEdges(resp, context)
  }
}

const getTopologyGraphs = async (context: VuexContext) => {
  const topologyGraphs = await API.getTopologyGraphs()
  context.commit('SAVE_TOPOLOGY_GRAPHS', topologyGraphs)
}

const getTopologyGraphByContainerAndNamespace = async (
  context: ContextWithState,
  { containerId, namespace }: Record<string, string>
) => {
  const topologyGraph = await API.getTopologyGraphByContainerAndNamespace(containerId, namespace)
  if (topologyGraph) {
    context.commit('SET_CONTAINER_AND_NAMESPACE', { container: containerId, namespace })
    parseVerticesAndEdges(topologyGraph, context)
    context.dispatch('addFocusObjects', context.state.defaultObjects)
  }
}

const setSemanticZoomLevel = (context: ContextWithState, SML: number) => {
  context.commit('SET_SEMANTIC_ZOOM_LEVEL', SML)
  context.dispatch('getObjectDataByLevelAndFocus')
}

const getObjectDataByLevelAndFocus = async (context: ContextWithState) => {
  let resp: false | VerticesAndEdges

  const SZLRequest: SZLRequest = {
    semanticZoomLevel: context.state.semanticZoomLevel,
    verticesInFocus: context.state.focusObjects.map((obj) => obj.id)
  }

  if (context.state.selectedDisplay !== PowerGrid) {
    resp = await API.getNodesTopologyDataByLevelAndFocus(SZLRequest)
  } else {
    resp = await API.getPowerGridTopologyDataByLevelAndFocus(
      context.state.container,
      context.state.namespace,
      SZLRequest
    )
  }

  if (resp) {
    parseVerticesAndEdges(resp, context)
  }
}

const setSelectedView = (context: VuexContext, view: string) => {
  context.commit('SET_SELECTED_VIEW', view)
}

const setSelectedDisplay = (context: VuexContext, display: string) => {
  context.commit('SET_SELECTED_DISPLAY', display)
}

const updateObjectFocusedProperty = (context: ContextWithState) => {
  const vertices = context.state.verticies
  const edges = context.state.edges
  const focusedIds = context.state.focusObjects.map((obj) => obj.id)

  for (const vertex of Object.values(vertices)) {
    if (focusedIds.includes(vertex.id)) {
      vertex.focused = true
    } else {
      vertex.focused = false
    }
  }

  for (const edge of Object.values(edges)) {
    if (focusedIds.includes(edge.target) && focusedIds.includes(edge.source)) {
      edge.focused = true
    } else {
      edge.focused = false
    }
  }

  context.commit('SAVE_NODE_VERTICIES', vertices)
  context.commit('SAVE_NODE_EDGES', edges)
}

const highlightFocusedNodes = (context: ContextWithState, bool: boolean) => {
  context.commit('SET_HIGHLIGHT_FOCUSED_NODES', bool)
}

const changeIcon = (context: ContextWithState, nodeIdIconKey: Record<string, string>) => {
  context.commit('UPDATE_NODE_ICONS', nodeIdIconKey)
  context.dispatch('updateVerticesIconPaths')
}

const updateVerticesIconPaths = (context: ContextWithState) => {
  const vertices = context.state.verticies
  const nodeIcons = context.state.nodeIcons

  for (const [id, iconKey] of Object.entries(nodeIcons)) {
    vertices[id]['icon'] = iconKey
  }

  context.commit('SAVE_NODE_VERTICIES', vertices)
}

/**
 * Focus
 */
const addFocusObjects = (context: ContextWithState, objects: IdLabelProps[]) => {
  context.commit('ADD_FOCUS_OBJECTS', objects)
  context.dispatch('getObjectDataByLevelAndFocus')
}

const addFocusObject = (context: VuexContext, object: IdLabelProps) => {
  context.commit('ADD_FOCUS_OBJECT', object)
  context.dispatch('getObjectDataByLevelAndFocus')
}

const removeFocusObject = (context: VuexContext, nodeId: string) => {
  context.commit('REMOVE_FOCUS_OBJECT', nodeId)
  context.dispatch('getObjectDataByLevelAndFocus')
}

/**
 * Left and right drawer states
 */
const openLeftDrawer = (context: VuexContext) => context.commit('SET_LEFT_DRAWER_OPEN', true)
const closeLeftDrawer = (context: VuexContext) => context.commit('SET_LEFT_DRAWER_OPEN', false)
const openRightDrawer = (context: VuexContext) => context.commit('SET_RIGHT_DRAWER_OPEN', true)
const closeRightDrawer = (context: VuexContext) => context.commit('SET_RIGHT_DRAWER_OPEN', false)

/**
 * Modal state
 */
const setModalState = (context: VuexContext, bool: boolean) => context.commit('SET_MODAL_STATE', bool)

export default {
  getVerticesAndEdges,
  setSemanticZoomLevel,
  setSelectedView,
  setSelectedDisplay,
  openLeftDrawer,
  closeLeftDrawer,
  openRightDrawer,
  closeRightDrawer,
  addFocusObject,
  addFocusObjects,
  getObjectDataByLevelAndFocus,
  removeFocusObject,
  highlightFocusedNodes,
  updateObjectFocusedProperty,
  setModalState,
  changeIcon,
  updateVerticesIconPaths,
  getTopologyGraphs,
  getTopologyGraphByContainerAndNamespace
}
