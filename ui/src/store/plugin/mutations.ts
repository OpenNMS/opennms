import { Plugin } from '@/types'
import { State } from './state'

const SAVE_PLUGINS = (state: State, plugins: Plugin[]) => {
  state.plugins = plugins
}

const SAVE_ENABLED_PLUGINS = (state: State, enabledPlugins: Plugin[]) => {
  state.enabledPlugins = enabledPlugins
}

const UPDATE_PLUGINS = (state: State, updatedPlugin: Plugin) => {
  state.plugins = state.plugins.map((plugin) => {
    if (plugin.extensionID === updatedPlugin.extensionID) {
      return { ...plugin, ...updatedPlugin }
    }
    return plugin
  })

  // remove the plugin script/styles if disabled
  if (updatedPlugin.enabled === false) {
    const baseUrl = import.meta.env.VITE_BASE_REST_URL
    const cssUrl = `${baseUrl}/plugins/ui-extension/css/${updatedPlugin.extensionID}`
    const scriptUrl = `${baseUrl}/plugins/ui-extension/module/${updatedPlugin.extensionID}?path=${updatedPlugin.resourceRootPath}/${updatedPlugin.moduleFileName}`

    const scriptToRemove = document.querySelector(`script[src='${scriptUrl}']`)
    const cssToRemove = document.querySelector(`link[href='${cssUrl}']`)

    if (scriptToRemove) scriptToRemove.remove()
    if (cssToRemove) cssToRemove.remove()
  }
}

export default {
  SAVE_PLUGINS,
  UPDATE_PLUGINS,
  SAVE_ENABLED_PLUGINS
}
