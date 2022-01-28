import { Resource } from '@/types'
import { State } from './state'

const SAVE_RESOURCES = (state: State, resources: Resource[]) => {
  state.resources = resources
}

const SAVE_NODE_RESOURCE = (state: State, nodeResource: Resource) => {
  state.nodeResource = nodeResource
}

const SET_SEARCH_VALUE = (state: State, searchValue: string) => {
  state.searchValue = searchValue
}

export default {
  SAVE_RESOURCES,
  SAVE_NODE_RESOURCE,
  SET_SEARCH_VALUE
}
