import { PreFabGraph } from '@/types'

export interface State {
  definitions: string[]
  definitionData: PreFabGraph
}

const state: State = {
  definitions: [],
  definitionData: {} as PreFabGraph
}

export default state
