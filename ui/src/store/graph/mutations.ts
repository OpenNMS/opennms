import { GraphMetricsResponse, PreFabGraph } from '@/types'
import { State } from './state'

const SAVE_DEFINITIONS = (state: State, definitions: { id: string; definitions: string[]; label: string }[]) => {
  state.definitions = definitions
}

const SAVE_DEFINITIONS_LIST = (state: State, definitionsList: string[]) => {
  state.definitionsList = definitionsList
}

const SAVE_DEFINITION_DATA = (state: State, definitionData: PreFabGraph) => {
  state.definitionDataObjects = [...state.definitionDataObjects, definitionData]
}

const SAVE_GRAPH_METRICS = (state: State, graphMetrics: GraphMetricsResponse) => {
  state.graphMetrics = [...state.graphMetrics, graphMetrics]
}

const SAVE_NAME_ORDER_MAP = (state: State, preFabGraphs: PreFabGraph[]) => {
  const nameOrderMap: { [name: string]: number } = {}

  for (const graph of preFabGraphs) {
    nameOrderMap[graph.name] = graph.order
  }

  state.nameOrderMap = nameOrderMap
}

export default {
  SAVE_DEFINITIONS,
  SAVE_DEFINITION_DATA,
  SAVE_GRAPH_METRICS,
  SAVE_DEFINITIONS_LIST,
  SAVE_NAME_ORDER_MAP
}
