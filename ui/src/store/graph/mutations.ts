import { PreFabGraph } from '@/types'
import { State } from './state'

const SAVE_DEFINITIONS = (state: State, definitions: string[]) => {
  state.definitions = definitions
}

const SAVE_DEFINITION_DATA = (state: State, definitionData: PreFabGraph) => {
  state.definitionData = definitionData
}

export default {
  SAVE_DEFINITIONS,
  SAVE_DEFINITION_DATA
}
