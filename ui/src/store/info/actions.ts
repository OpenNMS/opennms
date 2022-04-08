import { VuexContext } from '@/types'
import API from '@/services'

const getInfo = async (context: VuexContext) => {
  const info = await API.getInfo()
  context.commit('SET_INFO', info)
}

export default {
  getInfo
}
