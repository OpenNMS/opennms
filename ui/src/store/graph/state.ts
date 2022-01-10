import { GraphMetricsResponse, PreFabGraph } from '@/types'

export interface State {
  definitions: { id: string, definitions: string[] }[]
  definitionDataObjects: PreFabGraph[]
  graphMetrics: GraphMetricsResponse[]
}

const state: State = {
  definitions: [],
  definitionDataObjects: [],
  graphMetrics: []
}

export default state
