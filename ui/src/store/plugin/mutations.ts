import { Plugin } from '@/types'
import { State } from './state'

const SAVE_PLUGINS = (state: State, plugins: Plugin[]) => {
  state.plugins = plugins
  state.enabledPlugins = plugins.filter((plugin) => plugin.enabled)
}

export default {
  SAVE_PLUGINS
}
