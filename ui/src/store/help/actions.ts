import { VuexContext } from '@/types'
import API from '@/services'

const getOpenApi = async (context: VuexContext) => {
  const openApi = await API.getOpenApi()
  context.commit('SET_OPEN_API', openApi)
  return openApi
}

export default {
  getOpenApi
}
