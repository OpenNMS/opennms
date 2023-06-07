import { VuexContext } from '@/types'
import API from '@/services'

const getOpenApi = async (context: VuexContext ) => {
  const openApi = await API.getOpenApi()
  context.commit('SET_OPEN_API', openApi)
  return openApi
}

const getOpenApiV1 = async (context: VuexContext ) => {
  const openApi = await API.getOpenApiV1()
  context.commit('SET_OPEN_API_V1', openApi)
  return openApi
}

export default {
  getOpenApi,
  getOpenApiV1
}
