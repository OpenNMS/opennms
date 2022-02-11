import { GraphMetricsResponse, PreFabGraph } from '@/types'

export interface State {
  definitions: { id: string; definitions: string[]; label: string }[]
  definitionDataObjects: PreFabGraph[]
  graphMetrics: GraphMetricsResponse[]
  definitionsList: string[]
  nameOrderMap: { [name: string]: number }
}

const state: State = {
  definitions: [],
  definitionDataObjects: [],
  graphMetrics: [],
  definitionsList: [],
  nameOrderMap: {}
}

export default state
