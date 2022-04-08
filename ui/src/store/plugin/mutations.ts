import { Plugin } from '@/types'
import { State } from './state'

const SAVE_PLUGINS = (state: State, plugins: Plugin[]) => {
  state.plugins = plugins
}

export default {
  SAVE_PLUGINS
}
