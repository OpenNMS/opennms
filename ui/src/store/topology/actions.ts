import API from '@/services'
import { QueryParameters, VuexContext } from '@/types'
import { NetworkGraphEdge, NetworkGraphVertex } from '@/types/topology'

const getVerticesAndEdges = async (context: VuexContext, queryParameters?: QueryParameters) => {
  const resp = await API.getVerticesAndEdges(queryParameters)
  if (resp) {
    const edges: Record<string, NetworkGraphEdge> = {}
    const vertices: Record<string, NetworkGraphVertex> = {}

    for (const edge of resp.edges) {
      edges[edge.label] = { source: edge.source.id, target: edge.target.id }
    }

    for (const vertex of resp.vertices) {
      vertices[vertex.nodeID] = { name: vertex.label }
    }

    context.commit('SAVE_NODE_EDGES', edges)
    context.commit('SAVE_NODE_VERTICIES', vertices)
  }
}

const setSemanticZoomLevel = (context: VuexContext, SML: number) => {
  context.commit('SET_SEMANTIC_ZOOM_LEVEL', SML)
}

const setSelectedView = (context: VuexContext, view: string) => {
  context.commit('SET_SELECTED_VIEW', view)
}

export default {
  getVerticesAndEdges,
  setSemanticZoomLevel,
  setSelectedView
}
