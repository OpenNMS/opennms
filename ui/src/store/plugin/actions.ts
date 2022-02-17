import API from '@/services'
import { VuexContext } from '@/types'

const getPlugins = async (context: VuexContext) => {
  const plugins = await API.getPlugins()
  context.commit('SAVE_PLUGINS', plugins)
}

const getEnabledPlugins = async (context: VuexContext) => {
  const plugins = await API.getEnabledPlugins()
  context.commit('SAVE_ENABLED_PLUGINS', plugins)
}

const togglePlugin = async (context: VuexContext, id: string) => {
  const updatedPlugin = await API.togglePlugin(id)
  context.commit('UPDATE_PLUGINS', updatedPlugin)
  context.dispatch('getEnabledPlugins')
}

export default {
  getPlugins,
  togglePlugin,
  getEnabledPlugins
}
