import API from '@/services'
import { VuexContext } from '@/types'

const getPlugins = async (context: VuexContext) => {
  const plugins = await API.getPlugins()
  context.commit('SAVE_PLUGINS', plugins)
}

export default {
  getPlugins
}
