import API from '@/services'
import { QueryParameters, VuexContext } from '@/types'
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
    vertices[vertex.id] = { name: vertex.label }
  }

  context.commit('SAVE_NODE_EDGES', edges)
  context.commit('SAVE_NODE_VERTICIES', vertices)
}

const getVerticesAndEdges = async (context: VuexContext, queryParameters?: QueryParameters) => {
  const resp = await API.getVerticesAndEdges(queryParameters)
  if (resp) {
    parseVerticesAndEdges(resp, context)
  }
}

const setSemanticZoomLevel = async (context: ContextWithState, SML: number) => {
  context.commit('SET_SEMANTIC_ZOOM_LEVEL', SML)
  const SZLRequest: SZLRequest = { semanticZoomLevel: SML, verticesInFocus: context.state.focusedNodeIds }
  await context.dispatch('getTopologyDataByLevelAndFocus', SZLRequest)
}

const addFocusedNodeIds = async (context: ContextWithState, nodeIds: string[]) => {
  context.commit('ADD_FOCUSED_NODE_IDS', nodeIds)
  const SZLRequest: SZLRequest = { semanticZoomLevel: context.state.semanticZoomLevel, verticesInFocus: nodeIds }
  await context.dispatch('getTopologyDataByLevelAndFocus', SZLRequest)
}

const getTopologyDataByLevelAndFocus = async (context: VuexContext, payload: SZLRequest) => {
  const resp = await API.getTopologyDataByLevelAndFocus(payload)
  if (resp) {
    parseVerticesAndEdges(resp, context)
  }
}

const setSelectedView = (context: VuexContext, view: string) => {
  context.commit('SET_SELECTED_VIEW', view)
}

const openLeftDrawer = (context: VuexContext) => {
  context.commit('SET_LEFT_DRAWER_OPEN', true)
}

const closeLeftDrawer = (context: VuexContext) => {
  context.commit('SET_LEFT_DRAWER_OPEN', false)
}

export default {
  getVerticesAndEdges,
  setSemanticZoomLevel,
  setSelectedView,
  openLeftDrawer,
  closeLeftDrawer,
  addFocusedNodeIds,
  getTopologyDataByLevelAndFocus
}
