import { DisplayType } from '@/components/Topology/topology.constants'
import API from '@/services'
import { IdLabelProps, QueryParameters, VuexContext } from '@/types'
import { SZLRequest, TopologyGraphList, VerticesAndEdges } from '@/types/topology'
import { Edges, Nodes } from 'v-network-graph'
import { State } from './state'
import getters from './getters'

interface ContextWithState extends VuexContext {
  state: State
}

/**
 * Parses a response from one of many calls
 * that contain vertices and edges.
 * 
 * Calls are either initial GET calls, or 
 * POST calls with SZL/Focus
 * 
 * @param resp VerticesAndEdges 
 * @param context VuexContext
 * 
 * Whether to add the edges or not. 
 * We may not want to if they contain links to sublayer nodes that are unavailable on this response.
 * @param preventLinks boolean  
 */
const parseVerticesAndEdges = (resp: VerticesAndEdges, context: VuexContext, preventLinks = false) => {
  const edges: Edges = {}
  const vertices: Nodes = {}

  if (!preventLinks) {
    for (const edge of resp.edges) {
      edges[edge.label] = { source: edge.source.id, target: edge.target.id }
    }
  }

  for (const vertex of resp.vertices) {
    vertices[vertex.id] = {
      name: vertex.label,
      id: vertex.id,
      tooltip: vertex.tooltipText,
      label: vertex.label,
      icon: 'generic_icon',
      namespace: vertex.namespace
    }
  }

  if (resp.defaultFocus && resp.defaultFocus.vertexIds.length) {
    const defaultIds = resp.defaultFocus.vertexIds.map((obj) => obj.id)

    if (defaultIds.length) {
      const defaultObjects = defaultIds.map((id) => vertices[id])
      context.commit('SAVE_DEFAULT_OBJECTS', defaultObjects)
    }
  }

  if (resp.focus) {
    const defaultIds = resp.focus.vertices

    if (defaultIds.length) {
      const defaultObjects = defaultIds.map((id) => vertices[id])
      context.commit('SAVE_DEFAULT_OBJECTS', defaultObjects)
    }
  }

  context.commit('SAVE_EDGES', edges)
  context.commit('SAVE_VERTICES', vertices)
  context.dispatch('updateObjectFocusedProperty')
  context.dispatch('updateVerticesIconPaths')
  context.dispatch('updateSubLayerIndicator')
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
    parseVerticesAndEdges(topologyGraph, context, true) // true to prevent adding edges here

    // save which ids have sublayers, to show indicator
    const idsWithSubLayers = topologyGraph.edges.map((edge) => edge.id.split('.')[0])
    context.commit('SAVE_IDS_WITH_SUBLAYERS', idsWithSubLayers)

    // set focus to the defaults
    context.dispatch('replaceFocusObjects', context.state.defaultObjects)
  }
}

const setSemanticZoomLevel = (context: ContextWithState, SML: number) => {
  context.commit('SET_SEMANTIC_ZOOM_LEVEL', SML)
  context.dispatch('getObjectDataByLevelAndFocus')
}

const getObjectDataByLevelAndFocus = async (context: ContextWithState) => {
  let resp: false | VerticesAndEdges

  try {
    const SZLRequest: SZLRequest = {
      semanticZoomLevel: context.state.semanticZoomLevel,
      verticesInFocus: context.state.focusObjects.map((obj) => obj.id)
    }
  
    if (context.state.selectedDisplay !== DisplayType.powergrid) {
      resp = await API.getNodesTopologyDataByLevelAndFocus(SZLRequest)
    } else {
      resp = await API.getPowergridTopologyDataByLevelAndFocus(
        context.state.container,
        context.state.namespace,
        SZLRequest
      )
    }
  
    if (resp) {
      parseVerticesAndEdges(resp, context)
    }
  } catch(err) {
    // error handling
  }
}

const changeIcon = (context: ContextWithState, nodeIdIconKey: Record<string, string>) => {
  context.commit('UPDATE_NODE_ICONS', nodeIdIconKey)
  context.dispatch('updateVerticesIconPaths')
}

/**
 * Saves menu selections
 */

// map, d3, circle, etc.
const setSelectedView = (context: VuexContext, view: string) => {
  context.commit('SET_SELECTED_VIEW', view)
}

// linkd, powergrid, etc.
const setSelectedDisplay = async (context: ContextWithState, display: string) => {
  context.commit('SET_SELECTED_DISPLAY', display)

  const graphsToDisplay = getters.getGraphsDisplay(context.state)
  
  context.commit('SAVE_TOPOLOGY_GRAPHS_DISPLAY', graphsToDisplay)
  context.commit('SAVE_TOPOLOGY_GRAPHS_SUB_LAYERS', graphsToDisplay.graphs)

  switch(display) {
    case DisplayType.powergrid:
      if(graphsToDisplay.graphs?.length) {
        await context.dispatch('getTopologyGraphByContainerAndNamespace', { containerId: graphsToDisplay.id, namespace: graphsToDisplay.graphs[0].namespace })
      }
      break
    case DisplayType.vmware:
      context.commit('SET_CONTAINER_AND_NAMESPACE', {})
      break
    default:
      await context.dispatch('getVerticesAndEdges')
      context.dispatch('replaceFocusObjects', context.state.defaultObjects)
      context.commit('SET_CONTAINER_AND_NAMESPACE', {})

  }
}

/**
 * Focus
 */
const replaceFocusObjects = (context: ContextWithState, objects: IdLabelProps[] | Node[]) => {
  context.commit('ADD_FOCUS_OBJECTS', objects)
  context.dispatch('getObjectDataByLevelAndFocus')
}

const addFocusObject = (context: VuexContext, object: IdLabelProps | Node) => {
  context.commit('ADD_FOCUS_OBJECT', object)
  context.dispatch('getObjectDataByLevelAndFocus')
}

const removeFocusObject = (context: VuexContext, nodeId: string) => {
  context.commit('REMOVE_FOCUS_OBJECT', nodeId)
  context.dispatch('getObjectDataByLevelAndFocus')
}

const highlightFocusedObjects = (context: ContextWithState, bool: boolean) => {
  context.commit('SET_HIGHLIGHT_FOCUSED_OBJECTS', bool)
}

const useDefaultFocus = (context: ContextWithState) => {
  const defaultFocusObjects = context.state.defaultObjects
  context.dispatch('replaceFocusObjects', defaultFocusObjects)
}

/**
 * Left and right drawer states
 */
const openLeftDrawer = (context: VuexContext) => context.commit('SET_LEFT_DRAWER_OPEN', true)
const closeLeftDrawer = (context: VuexContext) => context.commit('SET_LEFT_DRAWER_OPEN', false)
const setRightDrawerState = (context: VuexContext, bool: boolean) => context.commit('SET_RIGHT_DRAWER_OPEN', bool)

/**
 * Modal state
 */
const setModalState = (context: VuexContext, bool: boolean) => context.commit('SET_MODAL_STATE', bool)

/**
 * Network graph custom property updates.
 * Run every time after parsing the vertices and edges.
 */

// prop for whether object is focused or not
const updateObjectFocusedProperty = (context: ContextWithState) => {
  const vertices = context.state.vertices
  const edges = context.state.edges
  
  try {
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

    context.commit('SAVE_VERTICES', vertices)
    context.commit('SAVE_EDGES', edges)
  } catch(err) {
    // error handling
  }
}

// icon path prop
const updateVerticesIconPaths = (context: ContextWithState) => {
  const vertices = context.state.vertices
  const nodeIcons = context.state.nodeIcons

  for (const [id, iconKey] of Object.entries(nodeIcons)) {
    if (vertices[id]) {
      vertices[id]['icon'] = iconKey
    }
  }

  context.commit('SAVE_VERTICES', vertices)
}

const updateSubLayerIndicator = (context: ContextWithState) => {
  const idsWithSubLayers = context.state.idsWithSubLayers
  const { graphs = [] }: TopologyGraphList = getters.getGraphsDisplay(context.state)
  const vertices = context.state.vertices

  for (const graph of graphs) {
    for (const vertex of Object.values(vertices)) {
      // if vertex has sublayer and is within graph namespace
      if (idsWithSubLayers.includes(vertex.id) && vertex.namespace === graph.namespace) {
        // add the the next layer object for the context nav
        if (graphs[graph.index + 1]) {
          vertex['subLayer'] = graphs[graph.index + 1]
        }
      }
    }
  }

  context.commit('SAVE_VERTICES', vertices)
}

export default {
  getVerticesAndEdges,
  setSemanticZoomLevel,
  setSelectedView,
  setSelectedDisplay,
  setModalState,
  openLeftDrawer,
  closeLeftDrawer,
  addFocusObject,
  useDefaultFocus,
  removeFocusObject,
  replaceFocusObjects,
  setRightDrawerState,
  getObjectDataByLevelAndFocus,
  highlightFocusedObjects,
  updateObjectFocusedProperty,
  changeIcon,
  updateVerticesIconPaths,
  updateSubLayerIndicator,
  getTopologyGraphs,
  getTopologyGraphByContainerAndNamespace
}
