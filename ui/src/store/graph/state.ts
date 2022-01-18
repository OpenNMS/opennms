import { GraphMetricsResponse, PreFabGraph } from '@/types'

export interface State {
  definitions: { id: string; definitions: string[]; label: string }[]
  definitionDataObjects: PreFabGraph[]
  graphMetrics: GraphMetricsResponse[]
  definitionsList: string[]
}

const state: State = {
  definitions: [],
  definitionDataObjects: [],
  graphMetrics: [],
  definitionsList: []
}

export default state
