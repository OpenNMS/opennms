import { Resource } from '@/types'
import { State } from './state'

const getFilteredResourcesList = (state: State): Resource[] => {
  return state.resources.filter((resource) => resource.label.includes(state.searchValue))
}

export default {
  getFilteredResourcesList
}
