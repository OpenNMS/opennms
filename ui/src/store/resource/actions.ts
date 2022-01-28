import API from '@/services'
import { VuexContext } from '@/types'

const getResources = async (context: VuexContext) => {
  const resp = await API.getResources()
  if (resp) {
    context.commit('SAVE_RESOURCES', resp.resource)
  }
}

const getResourcesForNode = async (context: VuexContext, name: string) => {
  const resp = await API.getResourceForNode(name)
  if (resp) {
    context.commit('SAVE_NODE_RESOURCE', resp)
  }
}

const setSearchValue = (context: VuexContext, searchValue: string) => {
  context.commit('SET_SEARCH_VALUE', searchValue)
}

export default {
  getResources,
  getResourcesForNode,
  setSearchValue
}
