import API from '@/services'
import { Plugin, VuexContext } from '@/types'

const getPlugins = async (context: VuexContext) => {
  const plugins = await API.getPlugins()
  context.commit('SAVE_PLUGINS', plugins)
}

const updatePluginStatus = async (context: VuexContext, plugin: Plugin) => {
  await API.updatePluginStatus(plugin)
  context.dispatch('getPlugins')
}

export default {
  getPlugins,
  updatePluginStatus
}
